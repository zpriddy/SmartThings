/**
 *  ZP Litter Robot
 *
 *  Author: ZPriddy
 */
definition(
    name: "ZP Litter Robot SmartApp",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "Monitors a contact switch that is installed on a Litter Robot and when the selected number of cycles compleate it will send a notification",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	section("Litter Robot Cycle Sensor") {
		input "robotSensor", "capability.contactSensor"
	}
	section("Litter Robot Virtual Device"){
		input "litterRobot", "capability.switchLevel", title: "Litter Robot Virtual Device"
	}
	section("Alert after this many cycles") {
		input "maxCycles", "number", title: "Max Cycles?"
	}
    section("Auto Shutoff Options"){
    	input "robotSwitch","capability.switch",title:"Litter Robot Switch", required:false
        input "shutoffCycles", "number", title: "Shutoff After This Many Cycles"
    }
	
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
        }
    }
	
}

def installed() {
	state.cycleInProgress = false
	subscribe(robotSensor, "contact", robotHandler)
    subscribe(litterRobot, "cycleCount", cycleHandler)
    subscribe(litterRobot, "eswitch", eswitchHandler)
    subscribe(litterRobot, "status", statusHandler)
}

def updated() {
	unsubscribe()
    state.cycleInProgress = false
	subscribe(robotSensor, "contact", robotHandler)
    subscribe(litterRobot, "cycleCount", cycleHandler)
    subscribe(litterRobot, "eswitch", eswitchHandler)
    subscribe(litterRobot, "status", statusHandler)

}

def statusHandler(evt)
{
	if (evt.value == "MANUAL CLEANING")
    {
    	robotSwitch.off()
        pause(2000)
        robotSwitch.on()
        litterRobot.man_clean_on()
  	}

}
def eswitchHandler(evt)
{
	if(litterRobot.currentValue("status") != "CLEANING" || litterRobot.currentValue("status") != "MANUAL CLEANING")
    {
        if (evt.value == "off")
        {
            if (litterRobot.currentValue("cycleCount") < shutoffCycles)
            {
                send("ALERT: LITTER ROBOT HAS BEEN TURNNED OFF!")
                runIn(2 * 60 * 60, offReminder);
            }
            robotSwitch.off()
        }
        if (evt.value == "on")
        {
            send("Litter Robot has been turned back on!")
            unschedule(offReminder)
            robotSwitch.on()
        }
 	}
}
def robotHandler(evt)
{
	robotSwitch.on()
	if (evt.value == "open" && state.cycleInProgress == false )
    {
    	log.trace "New Cycle Starting.."
    	litterRobot.cycleStart()
        state.cycleInProgress = true
        log.trace "Counting Cycle in 3 minutes."
        runIn(3 * 60, countCycle);
    }
    if (evt.value == "closed" && state.cycleInProgress == false)
    {
    	litterRobot.on()
    }
}

def countCycle()
{
	log.trace "Counting Cycle"
	if (robotSensor.currentContact == "closed")
    {
    	litterRobot.setLevel(litterRobot.currentValue("cycleCount") + 1)
        litterRobot.cycleEnd()
        state.cycleInProgress = false
    }
    else 
    {
    	runIn(5 * 60, countCycleError);
    }
}

def countCycleError()
{
	log.trace "Counting Cycle"
	if (robotSensor.currentContact == "closed")
    {
    	litterRobot.setLevel(litterRobot.currentValue("cycleCount") + 1)
        litterRobot.cycleEnd()
        state.cycleInProgress = false
    }
    else 
    {
    	send("Litter Robot did not finish cycle. Please check on it.")
    }
}

def cycleHandler(evt)
{
	log.trace "LitterRobot Cycles = ${litterRobot.currentValue("cycleCount")}"
	if (litterRobot.currentValue("cycleCount") >= maxCycles && litterRobot.currentValue("cycleCount") < shutoffCycles)
    {
    	send("Litter Robot has reached ${litterRobot.currentValue("cycleCount")} Cycles. It is time to empty it!")
    }
    if (litterRobot.currentValue("cycleCount") >= shutoffCycles)
    {
    	send("ALERT: LITTER ROBOT HAS BEEN TURNNED OFF! - Litter Robot has reached ${litterRobot.currentValue("cycleCount")} Cycles")
    	litterRobot.eswitchOff()
        robotSwitch.off()
        runIn(2 * 60 * 60, offReminder);
    }
}

def offReminder()
{
	send("REMINDER: LITTER ROBOT HAS BEEN TURNNED OFF! - Please check on status")
    runIn(2 * 60 * 60, offReminder);
}


private send(msg) {
    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone1) {
            log.debug("sending text message")
            sendSms(phone1, msg)
        }
    }

    log.debug msg
}