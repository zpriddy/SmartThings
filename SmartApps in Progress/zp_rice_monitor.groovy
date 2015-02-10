/**
 * ZP Rice Monitor - AKA Energy Monitor Over Time
 *
 */

definition(
    name: "ZP Rice Monitor",
    namespace: "zpriddy",
    author: "Zachary Priddy  me@zpriddy.com",
    description: "Changes lights based on sensor actions",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences{

	section("Monitor this metered outlet..")
	{
		input "zOutlet", "capability.powerMeter", title: "What outlet to monitor?", required: true, multiple:false
	}
	section("Power Draw Settings")
	{
		input "offWatts", "number", title: "What is the power draw in watts when powered off? - Some devices still draw some power", required: true, multiple:false, defaultValue: 0

		paragraph "Some devices draw more powere when initally powered on, for example a rice maker will draw more powere when making the rice vs keeping it warm. If you would like the timer to reset if the power draw goes to a set level, enter that level here. (For example if you start to make more rice, the timer will reset) Otherwise leave it blank"
		input "resetWatts", "number", title: "How many watts does it draw when initally powered on?", required:false, multiple:false
		input "idleWatts", "number", title: "How many watts does it draw when in idle? (Or just keeping food warm)", required: false, multiple:false
	}
	section("Auto Off Settings")
	{
		input "offAfterHours", "number", title: "Auto off after how many hours?", required: true, multiple:false
	}
	section("Alerts")
	{
		input "alertOffset", "number", title: "How many hours do you want to wait before getting the first alert?", defaultValue:2, required: true, multiple: false

		input "alertHours", "number", title: "How many hours do you want to wait between alerts?", defaultValue:4, required: true, multiple: false

		input "doNotAlertMode", "mode", title: "Do not alert when in these modes..", description: "Select mode(s)", required: false, multiple: true

		input("recipients", "contact", title: "Send notifications to") 
		{
            input(name: "sms", type: "phone", title: "Send A Text To", description: null, required: false)
            input(name: "pushNotification", type: "bool", title: "Send a push notification", description: null, defaultValue: true)
        }
        paragraph "The deault message will say that the outlet has been powered on for X hours.. You can add a custom message before the standard message."
        input "msg","text", title: "What do you want to say in the message?", required: false, defaultValue: ""

	}



}

def appPressed(evt)
{
	//When the app is pressed turn on the outlet and start going..
	log.debug "BUTTON PRESSED"
	zOutlet.on()
	countReset()
}

def countReset()
{
	unschedule()
	zOutlet.reset()
	state.currentEnergy = zOutlet.currentValue("energy")
	state.prevEnergy = state.currentEnergy
	state.notify = true
	state.count = 0
	state.countHours = 0
	state.startTime = now()
	state.offTime = now() + (offAfterHours * 60 * 60 * 1000) 
	state.firstAlert = now() + (alertOffset * 60 * 60 * 1000)
	state.nextAlert = 0
	runEvery5Minutes(checkStatus)
	//schedule("*/5 * * * * ?", checkStatus)

	//Date printDates = new Date(now())
	//log.debug "NOW: $printDates"
	//printDates = new Date(state.firstAlert)
	//log.debug "FIRST ALERT: $printDates"
	//printDates = new Date(state.offTime)
	//log.debug "OFF TIME: $printDates"
	
}

def checkStatus()
{
	state.prevEnergy = state.currentEnergy
	zOutlet.refresh()
	pause(300)
	log.debug now()
	log.debug state.startTime
	long diffTime = now() - state.startTime
	log.debug diffTime
	float count = diffTime / 1000 / 60 / 60
	log.debug count
	state.countHours = count as double
	log.debug "HOURS ON: $state.countHours"

	def currentPower = zOutlet.currentValue("power")
	state.currentEnergy = zOutlet.currentValue("energy")
	log.debug "Current Energy $state.currentEnergy"
	log.debug "Previous Energy $state.prevEnergy"
	log.debug "CURRENT POWER $currentPower"


	if(prevEnergy < currentEnergy && zOutlet.currentValue("switch") == "on" && state.notify )
	{
		log.debug "The device is powered on"
		if(resetWatts != null && currentPower > (resetWatts*0.90) && state.countHours > 0.50)
		{
			state.count ++
			if(state.count >= 2)
			{
				log.debug "The timer is resetting - It has been 30 min since the last reset and the watts are 90% of the reset value"
				countReset()
			}
		}

		if(now() >= state.offTime)
		{
			if( prevEnergy < currentEnergy && currentPower > offWatts)
			{
				sendMessage("Turning off the $zOutlet")
				zOutlet.off()
				state.notify = false
			}
			else
			{
				log.debug "OUTLET ALREADY OFF"
			}
		}

		if(now() >= state.firstAlert)
		{
			def modeOkay = !doNotAlertMode || !doNotAlertMode.contains(location.mode) && state.notify
			if(modeOkay)
			{
				sendMessage(msg)
			}
			state.nextAlert = now() + (alertHours * 60 * 60 * 1000)

		}

		if(now() >= state.nextAlert && state.nextAlert != 0)
		{
			def modeOkay = !doNotAlertMode || !doNotAlertMode.contains(location.mode) && state.notify
			if(modeOkay)
			{
				sendMessage(msg)
			}
			state.nextAlert = now() + (alertHours * 60 * 60 * 1000)
		}


	}
	else if (countHours > 1)
	{	
		log.debug "Device is off"
		state.notify = false
	}


}

def installed() 
{
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() 
{
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() 
{
	subscribe(app, appPressed)
}





def sendMessage(message) {
	def msg = message + "The ${settings.zOutlet} has been powered on for ${state.countHours} hours."
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sms) {
            sendSms(sms, msg)
        }
        if (pushNotification) {
            sendPush(msg)
        }
    }
}

