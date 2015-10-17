/**
 *  ZP Pet Water Fountain
 *
 *  Author: ZPriddy
 */
definition(
    name: "ZP Pet Water Fountain",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "Monitors a contact switch that is installed on a Litter Robot and when the selected number of cycles compleate it will send a notification",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	section("Pet Water Fountain Sensor") {
		input "waterSensor", "capability.waterSensor"
	}

	section("Pet Water Fountain Virtual Device"){
		input "waterFountain", "capability.waterSensor", title: "Pet Water Fountain Virtual Device"
	}
	section("Notify Every X Hours") {
		input "hoursDelay", "number", title: "Hour Delay"
	}
    section("Auto Shutoff Options"){
    	input "waterSwitch","capability.switch",title:"Pet Water Fountain Switch", required:false
    }
	
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
        }
    }
	
}

def installed() {
	subscribe(waterSensor, "water", waterLevelHandler)
    subscribe(waterSwitch, "switch", waterSwitchHandler)
    subscribe(waterFountain, "switch", waterSwitchHandler)
}

def updated() {
	unsubscribe()
	subscribe(waterSensor, "water", waterLevelHandler)
    subscribe(waterSwitch, "switch", waterSwitchHandler)
    subscribe(waterFountain, "switch", waterSwitchHandler)

}

def waterLevelHandler(evt) {
	if (evt.value == "dry")
    {
    	log.debug "Water Sensor Dry"
		runIn(3 * 60, notifyEmpty);
  	}
    if (evt.value == "wet")
    {
    	waterFountain.wet()
    	log.debug "Water Sensor Wet"
    	unschedule(notifyEmpty)
        waterFountain.on()
    }

}


def notifyEmpty() {
	send("Pet Water Fountain is EMPTY! Check on it soon!")
    runIn(hoursDelay * 60 * 60, notifyEmpty);
    waterFountain.dry()
    waterFountain.off()
    waterSwitch.off()
}

def waterSwitchHandler(evt) {
	log.debug "Switch Handler" 
	def waterLevel = waterSensor.currentValue("water")
    log.debug waterLevel
    log.debug evt.value
    if (evt.value == "on")
    {
        if (waterLevel == "dry")
        {
			send("Pet Water Fountain is still EMPTY! It cant be turned on!")
            waterFountain.off()
            waterSwitch.off()

        }
        else
        {
            waterFountain.on()
            waterSwitch.on()
        }
		//Other case is not needed because it would be done in waterLevel Handler
    }
    if (evt.value == "off")
    {
        waterFountain.off()

        waterSwitch.off()

    }
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