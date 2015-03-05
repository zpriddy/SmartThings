/**
 *  ZP Virtual Presense
 *
 *	This is a virtual presense sensor that you can set to emulate a 
 *	presence sensor. 
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

metadata {
	// Automatically generated. Make future change here.
	definition (name: "ZP Virtual Presence", namespace: "zpriddy", author: "zpriddy") {

		capability "Presence Sensor"
		capability "Sensor"
        
        command "present"
        command "notPresent"

	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0"
			state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff"
		}
		
		main "presence"
		details(["presence"])
	}
}

def parse(String description) {
	def results
	results
}


private String parseName(String description) {
	if (description?.startsWith("presence: ")) {
		return "presence"
	}
	null
}

private String parseValue(String description) {
	if (description?.startsWith("presence: "))
	{
		if (description?.endsWith("1"))
		{
			return "present"
		}
		else if (description?.endsWith("0"))
		{
			return "not present"
		}
	}

	description
}

private parseDescriptionText(String linkText, String value, String description) {
	switch(value) {
		case "present": return "$linkText has arrived"
		case "not present": return "$linkText has left"
		default: return value
	}
}

private getState(String value) {
	def state = value
	if (value == "present") {
		state = "arrived"
	}
	else if (value == "not present") {
		state = "left"
	}

	state
}

def present()
{
	log.debug "Setting value to present"
	sendEvent(name:"presence", value:"present")
}

def notPresent()
{
	log.debug "Setting value to not present"
	sendEvent(name:"presence", value:"not present")
}








