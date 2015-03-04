/**
 *  Toggle Nest Home/Away state based on my presence
 *
 *  Author: Gus Perez
 */
definition(
    name: "House Sitter",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "Enable a Smart Presence Sensor for a House Sitter",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Categories/presence.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Categories/presence@2x.png"
)

preferences {
	section("Select Real Presence Sensor") {
		input "presenceReal", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Select Virtal Presence Sensor") {
		input "presenceVirt", "capability.presenceSensor", multiple: false
	}
	section("Select House Sitter Virtual Switch") {
		input "enableSwitch", "capability.switch", multiple: false
	}
}

def installed()
{
	subscribe(presenceReal, "presence", presenceHandler)
	subscribe(enableSwitch, "switch", presenceHandler)
	subscribe(app,presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presenceReal, "presence", presenceHandler)
	subscribe(enableSwitch, "switch", presenceHandler)
	subscribe(app,presenceHandler)
	log.debug "App Updated"
}

def presenceHandler(evt)
{
	log.debug "Event presenceHandler"
	/*
	log.debug "presenceHandler $evt.name: $evt.value"
	def current = presence1.currentValue("presence")
	log.debug current
	def presenceValue = presence1.find { it.currentPresence == "present" }
	log.debug presenceValue
	
    if (presenceValue) {
		//nest1.present()
        runIn(300, setNestToPresent)
		log.debug "Someone's home!"
	}
	else {
		nest1.away()
		log.debug "Everyone's away."
	}
	*/
	def realPresence = presenceReal.currentValue("presence")[0]
	def switchEnable = enableSwitch.currentValue("switch")

	log.debug "Real Presence: $realPresence"
	log.debug "Enable Switch $switchEnable"

	if(switchEnable == "off")
	{
		presenceVirt.notPresent()
	}
	else
	{
		if(realPresence == "present")
		{
			presenceVirt.present()
		}
		else
		{
			presenceVirt.notPresent()
		}
	}


}
/*
def setNestToPresent()
{
	nest1.present()
}
*/