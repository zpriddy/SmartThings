/**
 *  It's Too Cold
 *
 *  Author: SmartThings
 */
definition(
    name: "Room Window AC Temperature Control",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "Monitor the temperature and when it drops below your setting get a text and/or turn off a fan.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Monitor the temperature...") {
		input "temperatureSensor1", "capability.temperatureMeasurement"
	}
	section("Only run when this door is closed..."){
		input "contactSwitch", "capability.contactSensor", title: "Room Door?"
	}
	section("Delay turnning off switch for this many minutes after door is opened...") {
		input "delayMinutes", "number", title: "Delay?"
	}
	section("Unless this switch or virtual switch is on...") {
		input "overrideSwitch", "capability.switch", title: "Enable Switch", multiple: false
	}
	section("Turn off when the temperature drops below...") {
		input "temperatureLow", "number", title: "Temperature?"
	}
    section("Turn on when the temperature goes above...") {
		input "temperatureHigh", "number", title: "Temperature?"
	}
	section("Control this switch... (For AC or Fan)") {
		input "switch1", "capability.switch", required: false
	}
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
        }
    }
	
}

def installed() {
	subscribe(contactSwitch, "contact", doorHandler)
    subscribe(overrideSwitch, "switch", doorHandler)
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def updated() {
	unsubscribe()
	subscribe(contactSwitch, "contact", doorHandler)
    subscribe(overrideSwitch, "switch", doorHandler)
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def doorHandler(evt)
{
	log.debug "Change in door or override switch status";

	def overrideEnable = overrideSwitch.currentValue("switch")
	def doorState = contactSwitch.currentContact

	if(overrideEnable == "off" && doorState == "open")
	{
		state.overrideEnable = False
		runIn(delayMinutes * 60, turnOffDoor);
	}
	else if(overrideEnable == "off" && doorState == "closed")
	{
		state.overrideEnable = False
		state.forceOff = False
	}
	else
	{
		log.debug "Override Enabled.. Leaving On"
		state.overrideEnable = True
		state.forceOff = False
	}
}

def turnOffDoor()
{
	def overrideEnable = overrideSwitch.currentValue("switch")
	def doorState = contactSwitch.currentContact

	if(doorState == "open" && overrideEnable == "off")
	{
		state.forceOff = True
		switch1?.off()
	}
	else
	{
		state.forceOff = False
	}
}


def temperatureHandler(evt) {
	log.trace "temperature: $evt.value, $evt"

	def tooCold = temperatureLow
    def tooHot = temperatureHigh
	def mySwitch = settings.switch1
	def forcedOff = state.forceOff

	// TODO: Replace event checks with internal state (the most reliable way to know if an SMS has been sent recently or not).
	if (evt.doubleValue <= tooCold && forcedOff == False) {
		log.debug "Checking how long the temperature sensor has been reporting <= $tooCold"

		// Don't send a continuous stream of text messages
		def deltaMinutes = 10 // TODO: Ask for "retry interval" in prefs?
		def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
		def recentEvents = temperatureSensor1.eventsSince(timeAgo)?.findAll { it.name == "temperature" }
		log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
		def alreadySentSms = recentEvents.count { it.doubleValue <= tooCold } > 1

		if (alreadySentSms) {
			log.debug "SMS already sent to $phone1 within the last $deltaMinutes minutes"
			// TODO: Send "Temperature back to normal" SMS, turn switch off
		} else {
			log.debug "Temperature dropped below $tooCold:  sending SMS to $phone1 and activating $mySwitch"
			send("${temperatureSensor1.displayName} is too cold, reporting a temperature of ${evt.value}${evt.unit?:"F"}")
			switch1?.off()
		}
	}
    else if (evt.doubleValue >= tooHot && forcedOff == False) {
		log.debug "Checking how long the temperature sensor has been reporting <= $tooCold"

		// Don't send a continuous stream of text messages
		def deltaMinutes = 10 // TODO: Ask for "retry interval" in prefs?
		def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
		def recentEvents = temperatureSensor1.eventsSince(timeAgo)?.findAll { it.name == "temperature" }
		log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
		def alreadySentSms = recentEvents.count { it.doubleValue <= tooCold } > 1

		if (alreadySentSms) {
			log.debug "SMS already sent to $phone1 within the last $deltaMinutes minutes"
			// TODO: Send "Temperature back to normal" SMS, turn switch off
		} else {
			log.debug "Temperature went above $tooHot:  sending SMS to $phone1 and activating $mySwitch"
			send("${temperatureSensor1.displayName} is too hot, reporting a temperature of ${evt.value}${evt.unit?:"F"}")
			switch1?.on()
		}
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