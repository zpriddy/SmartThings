definition(
    name: "ZP SmartPresence To WEB API",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "Enable a Smart Presence Sensor for a House Sitter",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Categories/presence.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Categories/presence@2x.png"
)

preferences {

	section("Select Presence Sensor") {
		input "presence", "capability.presenceSensor", title: "Which presence sensors?", multiple: false, required: true
	}
    section("Remote Info"){
    	input "remotePresence", "text", title: "Sensor Name", required: true
        input "authCode", "text", title: "Auth Code", required: true
        input "endpointURL", "text", title: "Endpoint URL", required: true
    }

}

def installed()
{
	subscribe(presence, "presence", presenceHandler)
	subscribe(app,presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presence, "presence", presenceHandler)
	subscribe(app,presenceHandler)
	log.debug "App Updated"
}



def presenceHandler(evt) {

	def presenceState = presence.currentValue("presence")
    
    if(presenceState == "not present")
    {
        postPresence("notpresent")
    }
    else
    {
        postPresence("present")
    }

}

def postPresence(state){
	def params = [
    	uri: "https://graph.api.smartthings.com",
        path: endpointURL,
        headers: [
       		Authorization:" Bearer " + authCode
        ],
        body: [
        	deviceId: remotePresence,
            state: state
        ]
    ]
    
    log.debug params
    
	def data = [deviceId: remotePresence, state: state]
    

    
    httpPostJson(params){ resp ->   
            log.debug "response data: ${resp.data}"
        }
}