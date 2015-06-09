/**
 *  Light Follows Me
 *
 *  Author: SmartThings
 */

definition(
    name: "Motion Activated Lights with Override Switch",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "Turn your lights on when motion is detected and then off again once the motion stops for a set period of time unless an override switch is enabled.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet-luminance@2x.png"
)

preferences {
	section("Turn on when there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("And off when there's been no movement for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Turn on/off light(s)..."){
		input "switches", "capability.switchLevel", multiple: true
	}
	section("Set light level to..") {
		input "lightLevel", "number", title: "LightLevel?"
	}
	section("Don't change lights if this switch or virtual switch is on..") {
		input "overrideSwitch", "capability.switch", title: "Enable Switch", multiple: false
	}
}

def installed() {
	subscribe(motion1, "motion", motionHandler)
}

def updated() {
	unsubscribe()
	subscribe(motion1, "motion", motionHandler)
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	def overrideEnable = overrideSwitch.currentSwitch
	log.debug "Override Switch: $overrideEnable"
	if(overrideEnable == "off")
	{
		if (evt.value == "active") {
			log.debug "turning on lights"
			switches.on()
		} else if (evt.value == "inactive") {
			runIn(minutes1 * 60, scheduleCheck, [overwrite: false])
		}
	}
	else
	{
		log.debug "Override is ON"
	}
}

def scheduleCheck() {
	log.debug "schedule check"
	def motionState = motion1.currentState("motion")
	def overrideEnable = overrideSwitch.currentSwitch
    if (motionState.value == "inactive") {
        def elapsed = now() - motionState.rawDateCreated.time
    	def threshold = 1000 * 60 * minutes1 - 1000
    	if (elapsed >= threshold) 
    	{
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning lights off"
            if(overrideEnable == "off")
            {
            	switches.off()
            }
            else
            {
            	log.debug "Override is ON"
            }
    	} 
    	else 
    	{
        	log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } 
    else 
    {
    	log.debug "Motion is active, do nothing and wait for inactive"
    }
}