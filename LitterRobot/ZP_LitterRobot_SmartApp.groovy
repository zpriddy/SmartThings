/**
 * ZP Litter Robot SmartApp
 *
 *  Copyright 2015 Zachary Priddy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
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
    
    section("Litter Robot Drawer Sensor") {
		input "resetSensor", "capability.contactSensor"
	}
    
	section("Litter Robot Virtual Device"){
		input "litterRobot", "capability.actuator", title: "Litter Robot Virtual Device"
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
	initalize()
}

def updated() {
	unsubscribe()
	initalize()
}

def initalize() {
	state.cycleInProgress = false

	subscribe(robotSensor, "contact.open", sensorOpenHandler)
	subscribe(robotSensor, "contact.closed", sensorClosedHandler)

    subscribe(resetSensor, "contact", resetHandler)
    subscribe(litterRobot, "cycleCount", cycleHandler)
    subscribe(litterRobot, "eswitch", eswitchHandler)
    subscribe(litterRobot, "status", statusHandler)
}


def sensorOpenHandler(evt) {
	if(state.cycleInProgress == false) {
		log.trace "Starting Litter Robot Cycle"
		state.cycleInProgress = true
		litterRobot.cycleStart()
		runIn(5*60,countCycleError, [overwrite: true])
	}
	else {
		log.trace "Litter robot sensor open state again.."
	}
}

def sensorClosedHandler(evt) {
	if(state.cycleInProgress == true) {
		log.trace "Sensor closed: checking if still losed in 10 seconds. "
		runIn(10, countCycle, [overwrite: true])
	}	
	else {
		log.error "ERROR: SENSOR CLOSED BEFORE CYCLED STARTED"
	}
}

def statusHandler(evt)
{
	if (evt.value == "MANUAL CLEANING")
    {
    	state.cycleInProgress = false
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
                send("ALERT: LITTER ROBOT HAS BEEN TURNED OFF!")
                runIn(2 * 60 * 60, offReminder);
            }
            robotSwitch.off()
        }
        if (evt.value == "on")
        {
            send("Litter Robot has been turned back on!")
            unschedule("offReminder")
            robotSwitch.on()
        }
 	}
}


def countCycle()
{
	if (robotSensor.currentContact == "closed")
    {
    	log.trace "Counting Cycle"
        litterRobot.cycleEnd()
        state.cycleInProgress = false
        unschedule("countCycleError")
    }
    else 
    {
    	log.trace "Litter robot no longer closed.. Moving on."
    }
}

def countCycleError()
{
	log.trace "Counting Cycle"
	if (robotSensor.currentContact == "closed")
    {
        litterRobot.cycleEnd()
        state.cycleInProgress = false
    }
    else 
    {
    	state.cycleInProgress = false
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
    	send("ALERT: LITTER ROBOT HAS BEEN TURNED OFF! - Litter Robot has reached ${litterRobot.currentValue("cycleCount")} Cycles")
    	litterRobot.eswitchOff()
        robotSwitch.off()
        runIn(2 * 60 * 60, offReminder);
    }
}

def resetHandler(evt)
{
	unschedule("offReminder")

	if (evt.value == "open")
    {
    	robotSwitch.off()
        runIn(10 * 60, openReminder);
    }
    if (evt.value == "closed")
    {
    	robotSwitch.on()
        litterRobot.reset()
        unschedule(openReminder)
    }
}

def openReminder()
{
	
	send("REMINDER: LITTER ROBOT DRAWER IS STILL OPEN AND POWERED OFF")
	runIn(10 * 60, openReminder);
}

def offReminder()
{
	send("REMINDER: LITTER ROBOT HAS BEEN TURNED OFF! - Please check on status")
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