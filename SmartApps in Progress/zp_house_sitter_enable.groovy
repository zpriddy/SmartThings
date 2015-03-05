/**
 *  House Sitter Enable
 *
 *  This uses a SmartThings SmartPresense Sensor and a virtual switch and a
 *	ZP Virtual Presense Device.
 *
 *	This allowes you to have a SmartPresense Sensor that you can give to a 
 *	house sitter so that your house will react to them. 
 * 
 *	The ZP Virtual Presence is what you use for HelloHome actions and presence 
 *	based actions. The presence sensor will be the same as the SmartPresence 
 *	sensor when the enable virtal switch is on. It will display as away all 
 *	the time when the enable switch is off. This allowes HelloHome actions to
 *	work as normal when you have no house sitter and leave the tag on your 
 *	key rack. 
 */
definition(
    name: "House Sitter Enabler",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "Enable a Smart Presence Sensor for a House Sitter",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Categories/presence.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Categories/presence@2x.png"
)

preferences {
	section("Select Real Presence Sensor") {
		input "presenceReal", "capability.presenceSensor", title: "Real?", multiple: true
	}
	section("Select Virtal Presence Sensor") {
		input "presenceVirt", "capability.presenceSensor",title: "Virtual", multiple: false
	}
	section("Select House Sitter Virtual Switch") {
		input "enableSwitch", "capability.switch", title: "Enable Switch", multiple: false
	}
}

def installed()
{
	subscribe(presenceReal, "presence", presenceHandler)
	subscribe(enableSwitch, "switch", presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presenceReal, "presence", presenceHandler)
	subscribe(enableSwitch, "switch", presenceHandler)
	log.debug "App Updated"
}

def presenceHandler(evt)
{
	log.debug "Event presenceHandler"

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
