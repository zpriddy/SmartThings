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
		input "zOutlet", "capability.powerMeter", title: "What outlet to monitor?", required: true, multiple:false, submitOnChange: true
	}
	section("Power Draw Settings")
	{
		input "offWatts", "number", title: "What is the power draw in watts when powered off? - Some devices still draw some power", required: true, multiple:false

		paragraph "Some devices draw more powere when initally powered on, for example a rice maker will draw more powere when making the rice vs keeping it warm. If you would like the timer to reset if the power draw goes to a set level, enter that level here. (For example if you start to make more rice, the timer will reset) Otherwise leave it blank"
		input "resetWatts", "number", title: "How many watts does it draw when initally powered on?", required: false, multiple:false
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
        paragraph "The message will say that the ${zOutlet} has been powered on since: and the time last reset. You can add a custom message here."
        input "msg","text", title: "What do you want to say in the message?", required: false

	}



}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(meter, "power", meterHandler)
}





def sendMessage(msg) {
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
