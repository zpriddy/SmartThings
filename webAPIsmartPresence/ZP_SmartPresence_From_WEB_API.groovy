definition(
    name: "ZP SmartPresence From WEB API",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "Enable a Smart Presence Sensor for a House Sitter",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Categories/presence.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Categories/presence@2x.png"
)

preferences {

	section("Select Virtal Presence Sensor") {
		input "presences", "capability.presenceSensor", title: "Which presence sensors?", multiple: true, required: true
	}
}

def installed()
{
	subscribe(presences, "presence", presenceHandler)
	subscribe(app,presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presences, "presence", presenceHandler)
	subscribe(app,presenceHandler)
	log.debug "App Updated"
}


//
// Mappings
//
mappings {
    path("/presence") {
        action: [
            GET: "getPresence",
            POST: "postPresence"
        ]
    }
}



def getPresence() {
    def deviceId = request.JSON?.deviceId
    log.debug "getPresence ${deviceId}"

    if (deviceId) {
    
        def whichPresence = presences.find { it.displayName == deviceId }
        if (!whichPresence) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "deviceType":whichPresence.name,
                "state": whichPresence.currentValue("presence")]
        }
    }

    def result = [:]
    presences.each {
        result[it.displayName] = [
            "state": it.currentValue("presence"),
            "deviceType":it.name
            ]}

    return result
}

def postPresence() {
    def deviceId = request.JSON?.deviceId
    def state = request.JSON?.state
    log.debug "postPresence ${deviceId}"
    log.debug "postPresence ${state}"

    if (deviceId) {
    
        def whichPresence = presences.find { it.displayName == deviceId }
        if (!whichPresence) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            if(state == "present")
            {
                whichPresence.present()
                log.debug "Present"
            }
            else
            {
                whichPresence.notPresent()
            }
        }
    }

}



def presenceHandler(evt) {

}