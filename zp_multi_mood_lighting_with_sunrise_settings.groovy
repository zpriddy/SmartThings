/**
 *  Hue Multi Mood Lighting with Sunrise Sunset settings 
 *
 *  Author: Zachary Priddy - me@zpriddy.com
 *  *
 *  Date: 2014-02-21
 */
definition(
    name: "ZP Hue Multi Mood Lighting - Sunrise Sunset",
    namespace: "zpriddy",
    author: "Zachary Priddy  me@zpriddy.com",
    description: "Sets the colors and brightness level of your Philips Hue lights to match your mood.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png"
)

//*****************************************************************************
// This is menu settings
//*****************************************************************************
preferences {
	page(name: "mainPage", title: "Adjust the color of your Hue lights to match your mood.", install: true, uninstall: true)
	page(name: "dayHueSettings")
	page(name: "nightHueSettings")
	page(name: "transitionSettings")
	
	//page(name: "timeIntervalInput", title: "Only during a certain time") {
	//	section {
	//		input "starting", "time", title: "Starting", required: false
	//		input "ending", "time", title: "Ending", required: false
	//	}
	//}
}

def mainPage() 
{
	dynamicPage(name: "mainPage") 
	{

		def anythingSet = anythingSet()
		if (anythingSet) 
		{

			section("Trigger Hue light settings when...")
			{
				

				ifSet "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
				ifSet "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
				ifSet "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
				ifSet "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
				ifSet "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
				ifSet "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
				ifSet "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
				ifSet "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
				ifSet "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
				ifSet "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
				ifSet "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
				ifSet "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
				//ifSet "timeOfDay", "time", title: "At a Scheduled Time", required: false
			}
		}
		section(anythingSet ? "Select additional Hue lighting triggers" : "Set the lighting mood when...", hideable: anythingSet, hidden: true)
		{
			ifUnset "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
			ifUnset "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
			ifUnset "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
			ifUnset "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
			ifUnset "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
			ifUnset "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
			ifUnset "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
			ifUnset "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
			ifUnset "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
			ifUnset "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
			ifUnset "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
			ifUnset "triggerModes", "mode", title: "System Changes Mode", description: "Select mode(s)", required: false, multiple: true
			//ifUnset "timeOfDay", "time", title: "At a Scheduled Time", required: false
		}
		section("Hue Bridge")
			{
				input "hueBridge", "capability.Refresh", title: "Hue Bridge", required: true
			}

		section("Day Hue Settings..")
		{
			href(name: "dayHueSettings", page: "dayHueSettings", title: "Day Hue Settings", description: "Open Settings", state: "complete")
		}

		section("Night Hue Settings..")
		{
			href(name: "nightHueSettings", page: "nightHueSettings", title: "Night Hue Settings", description: "Open Settings", state: "complete")
		}
		section("Transition Settings..")
		{
			href(name: "transitionSettings", page: "transitionSettings", title: "Transition Settings", description: "Open Settings", state: "complete")
		}
		section("When Hues are turnned off set them to this color before turnning off", hideable: true, hidden: true)
			{
				input "defaultOffColor", "enum", title: "Hue Color1?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "defaultOffLevel", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
			}



		

		section("More options", hideable: true, hidden: true) 
		{
			input "frequency", "decimal", title: "Minimum time between actions (defaults to every event)", description: "Minutes", required: false
			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
			input "oncePerDay", "bool", title: "Only once per day", required: false, defaultValue: false
		}
		section([mobileOnly:true]) 
		{
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
		}

		
	}
}

def dayHueSettings()
{
	dynamicPage(name: "dayHueSettings", title: "Set daytime settings for Hue lights.")
	{
		section("Day Hue Group 1") 
		{
			input "dayHues1", "capability.colorControl", title: "Which Hue Bulbs?", required:false, multiple:true
		}
        section("Day Hue Group 2") 
        {
			input "dayHues2", "capability.colorControl", title: "Which Hue Bulbs?", required:false, multiple:true
		}
        section("Day Hue To Turn Off") 
        {
			input "dayHuesOff", "capability.colorControl", title: "Which Hue Bulbs?", required:false, multiple:true
		}
		section("Day Color Group 1")
			{
				input "dayColor1", "enum", title: "Hue Color1?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "dayLightLevel1", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
			}
        section("Day Color Group 2")
			{
				input "dayColor2", "enum", title: "Hue Color2?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "dayLightLevel2", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
			}
	}
}

def nightHueSettings()
{
	dynamicPage(name: "nightHueSettings", title: "Set night settings for Hue lights.")
	{
		section("Night Hue Group 1") 
		{
			input "nightHues1", "capability.colorControl", title: "Which Hue Bulbs?", required:false, multiple:true
		}
        section("Night Hue Group 2") 
        {
			input "nightHues2", "capability.colorControl", title: "Which Hue Bulbs?", required:false, multiple:true
		}
        section("Night Hue To Turn Off") 
        {
			input "nightHuesOff", "capability.colorControl", title: "Which Hue Bulbs?", required:false, multiple:true
		}
		section("Night Color Group 1")
			{
				input "nightColor1", "enum", title: "Hue Color1?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "nightLightLevel1", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
			}
        section("Night Color Group 2")
			{
				input "nightColor2", "enum", title: "Hue Color2?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "nightLightLevel2", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
			}
	}
}
def transitionSettings()
{
	dynamicPage(name: "transitionSettings", title: "Transition Settings SOME OPTIONS NOT WORKING")
	{
		section("Enable Transition")
		{
			input(name: "transition", type: "bool", title: "-Gradually- chnage the color of lights from day to night and night to day", description: null, required: false, defaultValue: "true")
			input(name: "transitionOnly", type: "bool", title: "-Only transition if state of lights is in previous state (No changes)-", description: null, required: false, defaultValue: "true")
		}
		section ("Sunrise offset (optional)...") 
		{
			input "sunriseOffsetValue", "text", title: "HH:MM", required: false
			input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
		}
		section ("Sunset offset (optional)...") 
		{
			input "sunsetOffsetValue", "text", title: "HH:MM", required: false
			input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
		}
		section ("Zip code (optional, defaults to location coordinates)...")
		{
			input "zipCode", "text", required: false
		}
		section ("Transition durration")
		{
			input(name: "duration", type: "number", title: "For this many minutes", description: "30", required: false, defaultValue: 30)
		}
	}
}


private anythingSet() {
	for (name in ["motion","contact","contactClosed","acceleration","mySwitch","mySwitchOff","arrivalPresence","departurePresence","smoke","water","button1","triggerModes","timeOfDay"]) {
		if (settings[name]) {
			return true
		}
	}
	return false
}

private ifUnset(Map options, String name, String capability) {
	if (!settings[name]) {
		input(options, name, capability)
	}
}

private ifSet(Map options, String name, String capability) {
	if (settings[name]) {
		input(options, name, capability)
	}
}




def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()

}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(app, appTouchHandler)
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
	subscribe(motion, "motion.active", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(mySwitchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(smoke, "smoke.detected", eventHandler)
	subscribe(smoke, "smoke.tested", eventHandler)
	subscribe(smoke, "carbonMonoxide.detected", eventHandler)
	subscribe(water, "water.wet", eventHandler)
	subscribe(button1, "button.pushed", eventHandler)

	//Stuff for sunrise / sunset
	subscribe(location, "position", locationPositionChange)
	subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
	subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)

	astroCheck()

	if (triggerModes) {
		subscribe(location, modeChangeHandler)
	}

	if (timeOfDay) {
		schedule(timeOfDay, scheduledTimeHandler)
	}
}

//***********************************************************************************
// Random Functions
//***********************************************************************************


private isNightTime()
{
	if(getSunriseAndSunset().sunrise.time > now() || getSunriseAndSunset().sunset.time < now())
	{
    	log.debug "It is night time"
    	return true
    }
    else 
    {
    	log.debug "It is day time"
    	return false
    }
}

def zp_pollHues()
{
	hueBridge.refresh()

	dayHues1*.refresh()
	dayHues2*.refresh()
	dayHuesOff*.refresh()

	nightHues1*.refresh()
	nightHues2*.refresh()
	nightHuesOff*.refresh()

}

//***********************************************************************************
// Main App Functions
//***********************************************************************************

private takeAction(evt) {

	if (frequency) 
	{
		state[frequencyKey(evt)] = now()
	}

	def zHues1 = dayHues1
	def zHues2 = dayHues2
	def zHuesOff = dayHuesOff

	def zHue1Color = dayColor1
	def zHue1Level = dayLightLevel1

	def zHue2Color = dayColor2
	def zHue2Level = dayLightLevel2

	//Configure Things for night time
	if(isNightTime())
	{
		zHues1 = nightHues1
		zHues2 = nightHues2
		zHuesOff = nightHuesOff

		zHue1Color = nightColor1
		zHue1Level = nightLightLevel1

		zHue2Color = nightColor2
		zHue2Level = nightLightLevel2
	}

	def zGroupHue = getHueColor(zHue1Color)
	def zGroupSaturation = getHueSat(zHue1Color)
	def zGroupLevel = zHue1Level

	zHues1.each
	{ hue ->

		hue.setColor([hue: zGroupHue, saturation: zGroupSaturation, level: zHue1Level as int])
		pause(350)
	}

	zGroupHue = getHueColor(zHue2Color)
	zGroupSaturation = getHueSat(zHue2Color)
	zGroupLevel = zHue1Leve2

	zHues2.each
	{ hue ->

		hue.setColor([hue: zGroupHue, saturation: zGroupSaturation, level: zHue1Level as int])
		pause(350)
	}

	zHuesOff.each
	{ hue-> 

		if(defaultOffColor && defaultOffLevel)
		{
			hue.setColor([hue: getHueColor(defaultOffColor), saturation: getHueSat(defaultOffColor), level: defaultOffLevel, switch: "off"])
			hue.off() //Redundent I know
		}
		else
		{
			hue.off()
		}

		pause(350)

	}

}

//***********************************************************************************
// ZP Color Fade
//***********************************************************************************

/*
def zp_colorFadeStart()
{

	log.debug "Starting fading..."

	zp_setLevelsInState()
	zp_setPrevHue()

	atomicState.running = true
	atomicState.start = new Date().getTime()
	schedule("0 1 * * * ?", "healthCheck")
	//healthCheck()


	zp_increment()

}

def zp_colorFadeStop() {
	log.trace "STOP"

	atomicState.running = false
	atomicState.start = 0

	unschedule("healthCheck")
}
private healthCheck() 
{
	log.trace "'Fading' healthCheck"

	if (!atomicState.running) {
		return
	}
	//runIn(20, "healthCheck", [overwrite: true])
	
	
	zp_increment()

	zp_setPrevHue()

	//pause(300)

	
}

def zp_setPrevHue()
{
	def zHues1 = dayHues1
	def zHues2 = dayHues2
	def zHuesOff = dayHuesOff

	def prevLevels = [:]
	def prevSaturation = [:]
	def prevHue = [:]

	//Configure Things for night time
	if(isNightTime)
	{
		zHues1 = nightHues1
		zHues2 = nightHues2
		zHuesOff = nightHuesOff
	}

	zp_pollHues()

	zHues1.each
	{ hue -> 

		log.debug "Setting Prev Vaule for $hue.label"

		def hueIsOff = hue.currentValue("switch") == "off"
		def zCurrentLevel = hueIsOff ? 0 : hue.currentValue("level")
		def zCurrentSaturation = hueIsOff ? 100 : hue.currentValue("saturation") as int
		def zCurrentHue = hueIsOff ? 70 : hue.currentValue ("hue") 

		prevLevels[hue.id] = zCurrentLevel
		prevSaturation[hue.id] = zCurrentSaturation
		prevHue[hue.id] = zCurrentHue

		if(hueIsOff)
		{
			hue.setColor([hue: zCurrentHue, saturation: zCurrentSaturation, level: zCurrentLevel, switch: "off"])
		}

		log.debug "$hue.label - Prev Values = Hue: $zCurrentHue Saturation: $zCurrentSaturation Level: $zCurrentLevel"

	}

	zHues2.each
	{ hue -> 

		log.debug "Setting Prev Vaule for $hue.label"

		def hueIsOff = hue.currentValue("switch") == "off"
		def zCurrentLevel = hueIsOff ? 0 : hue.currentValue("level")
		def zCurrentSaturation = hueIsOff ? 100 : hue.currentValue("saturation") as int
		def zCurrentHue = hueIsOff ? 70 : hue.currentValue ("hue") 

		prevLevels[hue.id] = zCurrentLevel
		prevSaturation[hue.id] = zCurrentSaturation
		prevHue[hue.id] = zCurrentHue

		if(hueIsOff)
		{
			hue.setColor([hue: zCurrentHue, saturation: zCurrentSaturation, level: zCurrentLevel, switch: "off"])
		}

		log.debug "$hue.label - Prev Values = Hue: $zCurrentHue Saturation: $zCurrentSaturation Level: $zCurrentLevel"

	}

	zHuesOff.each
	{ hue -> 

		log.debug "Setting Prev Vaule for $hue.label"

		def hueIsOff = hue.currentValue("switch") == "off"
		def zCurrentLevel = hueIsOff ? 0 : hue.currentValue("level")
		def zCurrentSaturation = hueIsOff ? 100 : hue.currentValue("saturation") as int
		def zCurrentHue = hueIsOff ? 70 : hue.currentValue ("hue") 

		prevLevels[hue.id] = zCurrentLevel
		prevSaturation[hue.id] = zCurrentSaturation
		prevHue[hue.id] = zCurrentHue

		if(hueIsOff)
		{
			hue.setColor([hue: zCurrentHue, saturation: zCurrentSaturation, level: zCurrentLevel, switch: "off"])
		}

		log.debug "$hue.label - Prev Values = Hue: $zCurrentHue Saturation: $zCurrentSaturation Level: $zCurrentLevel"
	}

	atomicState.prevLevels = prevLevels
	atomicState.prevHue = prevHue
	atomicState.prevSaturation = prevSaturation

	log.debug "Finished setting previous values"

}

private zp_increment()
{
	if(!atomicState.running)
	{
		return
	}
	def percentComplete = completionPercentage()

	log.debug "Percent Complete = $percentComplete"

	if (percentComplete > 99) 
	{
		percentComplete = 99
	}

	zp_updateHues(percentComplete)

	//LEFT OFF HERE

	if (percentComplete < 99) 
	{

		def runAgain = stepDuration()

		log.debug "Rescheduling to run again in ${runAgain} seconds"

		runIn(runAgain, 'healthCheck', [overwrite: true])

	}
	else
	{
		zp_colorFadeStop()
	}


}

def zp_updateHues(percentComplete)
{
	def zHues1 = dayHues1
	def zHues2 = dayHues2
	def zHuesOff = dayHuesOff

	def okayToFade = [:]

	zp_pollHues()

	//Configure Things for night time
	if(isNightTime)
	{
		zHues1 = nightHues1
		zHues2 = nightHues2
		zHuesOff = nightHuesOff
	}

	zHues1.each
	{ hue -> 

		def nextLevel = zp_dynamicLevel(hue, percentComplete)
		def nextSaturation = zp_dynamicSaturation(hue, percentComplete)
		def nextHue = zp_dynamicHue(hue, percentComplete)

		//log.debug "Next Level = $nextLevel"

		//LEFT OFF HERE

		def tempLevel = hue.currentValue("level") as int
		def tempSaturation = hue.currentValue("saturation") as int
		def tempHue = hue.currentValue("hue") as int

		def tempPrevLevels = atomicState.prevLevels[hue.id]
		def tempPrevHue = atomicState.prevHue[hue.id]
		def tempPrevSaturation = atomicState.prevSaturation[hue.id]

		log.debug "$hue.label - Level: $tempPrevLevels - $tempLevel"
		log.debug "$hue.label - Hue: $tempPrevHue - $tempHue"
		log.debug "$hue.label - Saturation: $tempPrevSaturation - $tempSaturation"

		pause(100)

		
		log.debug atomicState.okayToFade[hue.id]

		



		if(atomicState.okayToFade[hue.id] == true)
		{
			log.debug "Okay To Fade"
			if( tempHue >= (tempPrevHue - 10) && tempHue <= (tempPrevHue + 10))
			{
				log.debug "Passed One"
			
				if( tempLevel >= (tempPrevLevels - 4) && tempLevel <= (tempPrevLevels + 4))
				{
					log.debug "Passed Two"
				
					if( tempSaturation >= (tempPrevSaturation - 4) && tempSaturation <= (tempPrevSaturation + 4))
					{
						log.debug "Passed 3"
						

						hue.setColor([hue: nextHue, saturation: nextSaturation, level: nextLevel])
						log.debug "Writing new values to $hue.label"
						okayToFade[hue.id] = true;
					}
					else
					{
						okayToFade[hue.id] = false
						
					}
				}
				else
				{
					okayToFade[hue.id] = false
					
				}
			}
			else
			{
				okayToFade[hue.id] = false
				
			}
		}
		else
		{
			log.debug "No loger fading $hue.label"
		}

		
		
		pause(200)

	}
	zHues2.each
	{ hue -> 

		def nextLevel = zp_dynamicLevel(hue, percentComplete)
		def nextSaturation = zp_dynamicSaturation(hue, percentComplete)
		def nextHue = zp_dynamicHue(hue, percentComplete)

		//log.debug "Next Level = $nextLevel"

		//LEFT OFF HERE

		def tempLevel = hue.currentValue("level") as int
		def tempSaturation = hue.currentValue("saturation") as int
		def tempHue = hue.currentValue("hue") as int

		def tempPrevLevels = atomicState.prevLevels[hue.id]
		def tempPrevHue = atomicState.prevHue[hue.id]
		def tempPrevSaturation = atomicState.prevSaturation[hue.id]

		log.debug "$hue.label - Level: $tempPrevLevels - $tempLevel"
		log.debug "$hue.label - Hue: $tempPrevHue - $tempHue"
		log.debug "$hue.label - Saturation: $tempPrevSaturation - $tempSaturation"

		pause(100)

		atomicState.okayToFade[hue.id] = false

		
		if(atomicState.okayToFade[hue.id] == true)
		{
			log.debug "Okay To Fade"
			if( tempHue >= (tempPrevHue - 10) && tempHue <= (tempPrevHue + 10))
			{
				log.debug "Passed One"
			
				if( tempLevel >= (tempPrevLevels - 4) && tempLevel <= (tempPrevLevels + 4))
				{
					log.debug "Passed Two"
				
					if( tempSaturation >= (tempPrevSaturation - 4) && tempSaturation <= (tempPrevSaturation + 4))
					{
						log.debug "Passed 3"
						

						hue.setColor([hue: nextHue, saturation: nextSaturation, level: nextLevel])
						log.debug "Writing new values to $hue.label"
						okayToFade[hue.id] = true;
					}
					else
					{
						okayToFade[hue.id] = false
						
					}
				}
				else
				{
					okayToFade[hue.id] = false
					
				}
			}
			else
			{
				okayToFade[hue.id] = false
				
			}
		}
		else
		{
			log.debug "No loger fading $hue.label"
		}
		
		pause(200)

	}
	zHuesOff.each
	{ hue -> 

		def nextLevel = zp_dynamicLevel(hue, percentComplete)
		def nextSaturation = zp_dynamicSaturation(hue, percentComplete)
		def nextHue = zp_dynamicHue(hue, percentComplete)


		//log.debug "Next Level = $nextLevel"

		//LEFT OFF HERE

		def tempLevel = hue.currentValue("level") as int
		def tempSaturation = hue.currentValue("saturation") as int
		def tempHue = hue.currentValue("hue") as int

		def tempPrevLevels = atomicState.prevLevels[hue.id]
		def tempPrevHue = atomicState.prevHue[hue.id]
		def tempPrevSaturation = atomicState.prevSaturation[hue.id]

		log.debug "$hue.label - Level: $tempPrevLevels - $tempLevel"
		log.debug "$hue.label - Hue: $tempPrevHue - $tempHue"
		log.debug "$hue.label - Saturation: $tempPrevSaturation - $tempSaturation"

		pause(100)

		atomicState.okayToFade[hue.id] = false

		if(atomicState.okayToFade[hue.id] == true)
		{
			log.debug "Okay To Fade"
			if( tempHue >= (tempPrevHue - 10) && tempHue <= (tempPrevHue + 10))
			{
				log.debug "Passed One"
			
				if( tempLevel >= (tempPrevLevels - 4) && tempLevel <= (tempPrevLevels + 4))
				{
					log.debug "Passed Two"
				
					if( tempSaturation >= (tempPrevSaturation - 4) && tempSaturation <= (tempPrevSaturation + 4))
					{
						log.debug "Passed 3"
						log.debug "Writing new values to $hue.label"
						okayToFade[hue.id] = true;

						if(nextLevel > 1)
						{
							hue.setColor([hue: nextHue, saturation: nextSaturation, level: nextLevel])
						}
						else
						{
							hue.off()
						}
					}
					else
					{
						okayToFade[hue.id] = false
						
					}
				}
				else
				{
					okayToFade[hue.id] = false
					
				}
			}
			else
			{
				okayToFade[hue.id] = false
				
			}
		}
		else
		{
			log.debug "No loger fading $hue.label"
		}
		
		pause(200)

	}

	


	atomicState.okayToFade = okayToFade

}

int zp_dynamicLevel(hue, percentComplete)
{
	log.debug "Starting dynamic Level for $hue.label"
	def start = atomicState.startLevels[hue.id] as int
	def end = atomicState.endLevels[hue.id] as int
	log.debug "$hue.label - Start: $start - End: $end"

	if(!percentComplete)
	{
		return start
	}

	int totalDiff = end - start
	log.debug "**********************TOTAL DIFF: $totalDiff"
	def actualPercentage = percentComplete / 100
	if(actualPercentage <= 0)
	{
		actualPercentage = 0.0001
	}
	def percentOfTotalDiff = (totalDiff * actualPercentage)
	log.debug "**********************PERCENT TOTAL DIFF: $percentOfTotalDiff"

	int zDynamicLevel = (start + percentOfTotalDiff) 
	log.debug "Dynamic Level = $zDynamicLevel"
	return zDynamicLevel

}

int zp_dynamicHue(hue, percentComplete)
{
	log.debug "Starting dynamic Hue for $hue.label"
	def start = atomicState.startHue[hue.id] as int
	def end = atomicState.endHue[hue.id] as int
	log.debug "$hue.label - Start: $start - End: $end"

	if(!percentComplete)
	{
		return start
	}

	def totalDiff = end - start
	def actualPercentage = percentComplete / 100
	if(actualPercentage <= 0)
	{
		actualPercentage = 0.0001
	}
	def percentOfTotalDiff = totalDiff * actualPercentage

	int zDynamicHue = (start + percentOfTotalDiff)

	log.debug "Dynamic Hue = $zDynamicHue"
	return zDynamicHue

}

int zp_dynamicSaturation(hue, percentComplete)
{
	log.debug "Starting dynamic Saturation for $hue.label"
	def start = atomicState.startSaturation[hue.id]
	def end = atomicState.endSaturation[hue.id]
	log.debug "$hue.label - Start: $start - End: $end"

	if(!percentComplete)
	{
		return start
	}

	def totalDiff = end - start
	def actualPercentage = percentComplete / 100
	if(actualPercentage <= 0)
	{
		actualPercentage = 0.0001
	}
	def percentOfTotalDiff = totalDiff * actualPercentage

	int zDynamicSaturation = (start + percentOfTotalDiff) 

	log.debug "Dynamic Saturation = $zDynamicSaturation"
	return zDynamicSaturation

}

def zp_setLevelsInState()
{
	def zHues1 = dayHues1
	def zHues2 = dayHues2
	def zHuesOff = dayHuesOff 

	def startLevels = [:]
	def startHue = [:]
	def startSaturation = [:]

	def endLevels = [:]
	def endHue = [:]
	def endSaturation = [:]

	def okayToFade = [:]
	

	//Configure Things for night time
	if(isNightTime())
	{
		zHues1 = nightHues1
		zHues2 = nightHues2
		zHuesOff = nightHuesOff
	}

	zp_pollHues()

	zHues1.each
	{ hue ->

		okayToFade[hue.id] = true

		def hueIsOff = hue.currentValue("switch") == "off"
		def zCurrentLevel = hueIsOff ? 0 : hue.currentValue("level")
		def zCurrentSaturation = hueIsOff ? 100 : hue.currentValue("saturation") as int
		def zCurrentHue = hueIsOff ? 70 : hue.currentValue ("hue") 

		if(hueIsOff)
		{
			hue.setColor([hue: zCurrentHue, saturation: zCurrentSaturation, level: zCurrentLevel, switch: "off"])
		}

		startLevels[hue.id] = zCurrentLevel
		startSaturation[hue.id] = zCurrentSaturation
		startHue[hue.id] = zCurrentHue

		if(isNightTime())
		{
			endLevels[hue.id] = nightLightLevel1
			endHue[hue.id] = getHueColor(nightColor1)
			endSaturation[hue.id] = getHueSat(nightColor1)
		}
		else
		{
			endLevels[hue.id] = dayLightLevel1
			endHue[hue.id] = getHueColor(dayColor1)
			endSaturation[hue.id] = getHueSat(dayColor1)
		}

		log.debug "$hue.label - Current Values = Hue: $zCurrentHue Saturation: $zCurrentSaturation Level: $zCurrentLevel"
	}
	zHues2.each
	{ hue ->

		okayToFade[hue.id] = true

		def hueIsOff = hue.currentValue("switch") == "off"
		def zCurrentLevel = hueIsOff ? 0 : hue.currentValue("level")
		def zCurrentSaturation = hueIsOff ? 100 : hue.currentValue("saturation") as int
		def zCurrentHue = hueIsOff ? 70 : hue.currentValue ("hue") 

		startLevels[hue.id] = zCurrentLevel
		startSaturation[hue.id] = zCurrentSaturation
		startHue[hue.id] = zCurrentHue

		if(hueIsOff)
		{
			hue.setColor([hue: zCurrentHue, saturation: zCurrentSaturation, level: zCurrentLevel, switch: "off"])
		}

		if(isNightTime())
		{
			endLevels[hue.id] = nightLightLevel2
			endHue[hue.id] = getHueColor(nightColor2)
			endSaturation[hue.id] = getHueSat(nightColor2)
		}
		else
		{
			endLevels[hue.id] = dayLightLevel2
			endHue[hue.id] = getHueColor(dayColor2)
			endSaturation[hue.id] = getHueSat(dayColor2)
		}

		log.debug "$hue.label - Current Values = Hue: $zCurrentHue Saturation: $zCurrentSaturation Level: $zCurrentLevel"
	}

	zHuesOff.each
	{ hue ->

		okayToFade[hue.id] = true

		def hueIsOff = hue.currentValue("switch") == "off"
		def zCurrentLevel = hueIsOff ? 0 : hue.currentValue("level")
		def zCurrentSaturation = hueIsOff ? 100 : hue.currentValue("saturation") as int
		def zCurrentHue = hueIsOff ? 70 : hue.currentValue ("hue") 

		startLevels[hue.id] = zCurrentLevel
		startSaturation[hue.id] = zCurrentSaturation
		startHue[hue.id] = zCurrentHue

		if(hueIsOff)
		{
			hue.setColor([hue: zCurrentHue, saturation: zCurrentSaturation, level: zCurrentLevel, switch: "off"])
		}

		if(isNightTime())
		{
			endLevels[hue.id] = 0
			endHue[hue.id] = zCurrentHue
			endSaturation[hue.id] = zCurrentSaturation
		}
		else
		{
			endLevels[hue.id] = 0
			endHue[hue.id] = zCurrentHue
			endSaturation[hue.id] = zCurrentSaturation
		}

		log.debug "$hue.label - Current Values = Hue: $zCurrentHue Saturation: $zCurrentSaturation Level: $zCurrentLevel"
	}

	atomicState.startLevels = startLevels
	atomicState.startHue = startHue
	atomicState.startSaturation = startSaturation

	atomicState.endLevels = endLevels
	atomicState.endHue = endHue
	atomicState.endSaturation = endSaturation

	atomicState.prevLevels = startLevels
	atomicState.prevHue = startHue
	atomicState.prevSaturation = startSaturation

	atomicState.okayToFade = okayToFade

	log.debug "OKAY TO FADE $atomicState.okayToFade"

}

private int sanitizeInt(i, int defaultValue = 0) {
	try {
		if (!i) {
			return defaultValue
		} else {
			return i as int
		}
	}
	catch (Exception e) {
		log.debug e
		return defaultValue
	}
}

private completionDelaySeconds() {
	int completionDelayMinutes = sanitizeInt(completionDelay)
	int completionDelaySeconds = (completionDelayMinutes * 60)
	return completionDelaySeconds ?: 0
}

private stepDuration() {
	int minutes = sanitizeInt(duration, 30)
	int stepDuration = (minutes * 60) / 100
	return stepDuration ?: 1
}

def completionPercentage() {
	log.trace "checkingTime"

	if (!atomicState.running) {
		return
	}

	int now = new Date().getTime()
	int diff = now - atomicState.start
	int totalRunTime = totalRunTimeMillis()
	int percentOfRunTime = (diff / totalRunTime) * 100
	log.debug "percentOfRunTime: ${percentOfRunTime}"

	percentOfRunTime
}

int totalRunTimeMillis() {
	int minutes = sanitizeInt(duration, 30)
	def seconds = minutes * 60
	def millis = seconds * 1000
	return millis as int
}
*/

//***********************************************************************************
// END ZP Color Fade
//***********************************************************************************

//***********************************************************************************
// ZP SUNRISE SUNSET CODE
//***********************************************************************************


private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}

def locationPositionChange(evt) {
	log.trace "locationChange()"
	astroCheck()
}

def sunriseSunsetTimeHandler(evt) {
	log.trace "sunriseSunsetTimeHandler()"
	astroCheck()
}

def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)

	def now = new Date()
	def riseTime = s.sunrise
	def setTime = s.sunset
	log.debug "riseTime: $riseTime"
	log.debug "setTime: $setTime"

	if (state.riseTime != riseTime.time) {
		unschedule("sunriseHandler")

		if(riseTime.before(now)) {
			riseTime = riseTime.next()
		}

		state.riseTime = riseTime.time

		log.info "scheduling sunrise handler for $riseTime"
		schedule(riseTime, sunriseHandler)
	}

	if (state.setTime != setTime.time) {
		unschedule("sunsetHandler")

	    if(setTime.before(now)) {
		    setTime = setTime.next()
	    }
/**
 *  Hue Multi Mood Lighting with Sunrise Sunset settings 
 *
 *  Author: Zachary Priddy - me@zpriddy.com
 *  *
 *  Date: 2014-02-21
 */
definition(
    name: "ZP Testing",
    namespace: "zpriddy",
    author: "Zachary Priddy  me@zpriddy.com",
    description: "Sets the colors and brightness level of your Philips Hue lights to match your mood.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png"
)

preferences {
	page name:"setupInit"
	page name:"setupConfigure"
	page name:"setupModeTypes"
	page name:"setModeNames"
	page name:"modeSettings"
}

//***********************************************************************************
// Configuration Functions
//***********************************************************************************

def setupInit() {
	TRACE("setupInit()")
	log.debug "Current Settings: ${settings}"

	if (state.installed) {
		//return setupModeTypes()
        return setupConfigure()
	} else {
		return setupConfigure()
	}
}

def setupConfigure() {
	TRACE("setupConfigure()")

	def modeHelp = "Each mode will have different settings for Night and Day so you won't need Away Day and Away Night. This SmartApp will handel it on its own."

	def textNumOfHelp = 
		"You can switch between two modes for each type. How many types " +
		"do you have?"

	def inputNumModeTypes = [
		name: 			"numModeTypes",
		type: 			"number",
		title: 			"How many mode types?",
		defaultValue: 	"3",
		required: 		true
	]


	def pageProperties = [
		name: 			"setupConfigure",
		title: 			"General Setup",
		nextPage: 		"setModeNames",
		uninstall: 		true
	]

/*
	def pageProperties = [
		name: 			"setupConfigure",
		title: 			"Number of Mode Types",
        install: 		true,
		uninstall: 		false
	]
    */
	return dynamicPage(pageProperties) {
		section("Number of Mode Types") {
			paragraph modeHelp
			input inputNumModeTypes
		}
		section("Hue Bulbs")
		{
			input "hasHue", "boolean", title: "Do you have Hue lights?", defaultValue: false, required: true
		}

		section ("Sunrise offset (optional)...") {
			input "sunriseOffsetValue", "text", title: "HH:MM", required: false
			input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
		}
		section ("Sunset offset (optional)...") {
			input "sunsetOffsetValue", "text", title: "HH:MM", required: false
			input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
		}
		section ("Zip code (optional, defaults to location coordinates)...") {
			input "zipCode", "text", required: false
		}
        /*
		section( "Notifications" ) {
			input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
			input "phoneNumber", "phone", title: "Send a text message?", required: false
		}
        */
	}
}

def setModeNames()
{
	log.debug "Setting Mode Names"

	return dynamicPage(name: "setModeNames", title: "Set Mode Names", nextPage: "setupModeTypes"){
		def sampleNames = [ "", "Home", "Away", "Vaction", "Sleeping", "Alarm"]
		for (int n = 1; n <= numModeTypes; n++){
			section("Name for Mode ${n}"){
				input name: "m${n}_name", type: "text", tite: "Mode Name", defaultValue: m${n}_name? sampleNames[n]?.value, required: true
			}
		}
		section("Help..", hideable: true, hidden: true)
		{
			paragraph "This is where you set each mode name. This will help you remember what mode you are configureing settings for. ie: Away, Day, etc... \nRemember each mode has both a night and day setting built in."
		}
	}


}

def setupModeTypes() {
	TRACE("setupModeTypes()")

	def modeHelp = "Each mode will have different settings for Night and Day so you won't need Away Day and Away Night. This SmartApp will handle it on its own."

	

	def pageProperties = [
		name: 		"setupModeTypes",
		title: 		"Configure Modes",
		install: 	true,
		uninstall: 	state.installed
	]

	return dynamicPage(pageProperties) {
		for (int n = 1; n <= numModeTypes; n++) {
			def name = settings."m${n}_name"

			section()
			{
				
				href(name: "modeSettings", page: "modeSettings?modeNumber=$n", title: "Settings for $name Mode", description: "Open Settings", state: "complete")
			}

		}
		section("Help..", hideable: true, hidden: true)
		{
			paragraph "This is where you set each mode name. This will help you remember what mode you are configureing settings for. ie: Away, Day, etc... \nRemember each mode has both a night and day setting built in."
		}

	}
}

def modeSettings(params)
{
	def textModeDesc =
		"This is the mode that will trigger these settings"

	def textDayLightsOff =
		"These are the lights that will be turnned off during the day"

	def textDayDim =
		"These lights will be set the dimmer value set below. There are two groups of dimmable lights."

	def textDayHue =
		"These Hue lights will be set the colors set below. There are two groups of Hue lights."

	def textNightLightsOff =
		"These are the lights that will be turnned off during the night"

	def textNightDim =
		"These lights will be set the dimmer value set below. There are two groups of dimmable lights."

	def textNightHue =
		"These Hue lights will be set the colors set below. There are two groups of Hue lights."



	def textOnSunsetHelp =
		"These switches will turn on at sunset"

	def textOffSunsetHelp =
		"These switches will turn off at sunset"

	def n = params?.modeNumber
	def name = settings."m${n}_name"
	def hueSettings = settings.hasHue

	
	dynamicPage(name: "modeSettings", title: "Set settings for ${name}"){
		section("Mode Type ${name}", hideable:false) 
		{
			paragraph textDayHelp
			input "m${n}_mode", "mode", title: "Mode", required: true
		}
		section("Day Settings For ${name}", hideable: true, hidden: false)
		{
			paragraph textDayLightsOff
			input "m${n}_dayLightsOff", "capability.switch", title: "Day Lights Off",  multiple: true, required: false
			paragraph textDayDim
			input "m${n}_dayDim1", "capability.switchLevel", title: "Day Dimmer Lights (Group 1)", multiple: true, required: false
			input "m${n}_dayDim1Level", "number", title: "Set Dimmer To This Level (Group 1)", required: false
			paragraph ""
			input "m${n}_dayDim2", "capability.switchLevel", title: "Day Dimmer Lights (Group 2)", multiple: true, required: false
			input "m${n}_dayDim2Level", "number", title: "Set Dimmer To This Level (Group 2)", required: false
			
			
			if(hueSettings == "true")
			{
				paragraph textDayHue
				input "m${n}_dayHue1", "capability.switchLevel", title: "Day Hue Lights (Group 1)", multiple: true, required: false
				input "m${n}_dayHue1Color", "enum", title: "Day Hue Color (Group 1)", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "m${n}_dayHue1Level", "number", title: "Set Hue To This Level (Group 1)", required: false
				paragraph ""
				input "m${n}_dayHue2", "capability.switchLevel", title: "Day Hue Lights (Group 2)", multiple: true, required: false
				input "m${n}_dayHue2Color", "enum", title: "Day Hue Color (Group 2)", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "m${n}_dayHue2Level", "number", title: "Set Dimmer To This Level (Group 2)", required: false
			}
		}
		section("Night Settings For ${name}", hideable: true, hidden: false)
		{
			paragraph textNightLightsOff
			input "m${n}_nightLightsOff", "capability.switch", title: "Night Lights Off",  multiple: true, required: false
			paragraph textNightDim
			input "m${n}_nightDim1", "capability.switchLevel", title: "Night Dimmer Lights (Group 1)", multiple: true, required: false
			input "m${n}_nightDim1Level", "number", title: "Set Dimmer To This Level (Group 1)", required: false
			paragraph ""
			input "m${n}_nightDim2", "capability.switchLevel", title: "Night Dimmer Lights (Group 2)", multiple: true, required: false
			input "m${n}_nightDim2Level", "number", title: "Set Dimmer To This Level (Group 2)", required: false
			
			
			if(hueSettings == "true")
			{
				paragraph textNightHue
				input "m${n}_nightHue1", "capability.switchLevel", title: "Night Hue Lights (Group 1)", multiple: true, required: false
				input "m${n}_nightHue1Color", "enum", title: "Night Hue Color (Group 1)", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Nightlight":"Nightlight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "m${n}_nightHue1Level", "number", title: "Set Hue To This Level (Group 1)", required: false
				paragraph ""
				input "m${n}_nightHue2", "capability.switchLevel", title: "Night Hue Lights (Group 2)", multiple: true, required: false
				input "m${n}_nightHue2Color", "enum", title: "Night Hue Color (Group 2)", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Nightlight":"Nightlight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "m${n}_nightHue2Level", "number", title: "Set Dimmer To This Level (Group 2)", required: false
			}
		}

		section("Help..", hideable: true, hidden: true)
		{
			paragraph "This is where you set each mode name. This will help you remember what mode you are configureing settings for. ie: Away, Day, etc... \nRemember each mode has both a night and day setting built in."
		}
	}
	
	
}

//***********************************************************************************
// Installation Functions
//***********************************************************************************

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
	TRACE("initialize()")

    state.installed = true

    if (settings.zipCode == null) {
    	settings.zipCode = location.zipCode
    }

    state.numModeTypes = numModeTypes

    scheduleSunriseSunset()
    TRACE("End init")
}

/*
def setupModeType(n) {
	TRACE("setupModeType($n)")
	def modeType = [:]
	modeType.dayMode = settings."m${n}_dayMode"
	modeType.nightMode = settings."m${n}_nightMode"

	state.modeTypes.push(modeType)
    state.sunriseArray.push(modeType.nightMode)
    state.sunsetArray.push(modeType.dayMode)
}

def getModeTypeDevices(n) {
	if (n >= state.numModeTypes) {
    	return null
    }
    n++

    def devices = [:]

	devices.sunriseOnSwitches = settings."m${n}_sunriseOnSwitches"
	devices.sunriseOffSwitches = settings."m${n}_sunriseOffSwitches"
	devices.sunsetOnSwitches = settings."m${n}_sunsetOnSwitches"
	devices.sunsetOffSwitches = settings."m${n}_sunsetOffSwitches"

    return devices
}
*/

//***********************************************************************************
// Scheduling Functions
//***********************************************************************************

def scheduleSunriseSunset() {
	TRACE("scheduleSunriseSunset()")
    def srOff = sunriseOffset()
    def ssOff = sunsetOffset()
    log.debug("srOff: $srOff , ssOff: $ssOff")

	def sunriseSunset = getSunriseAndSunset(zipCode: settings.zipCode)

	def sunriseTime = sunriseSunset.sunrise
	def sunsetTime = sunriseSunset.sunset

	def sunriseScheduleTime = getSunriseWithOffset(srOff)
	def sunsetScheduleTime = getSunsetWithOffset(ssOff)


	log.debug("sunriseScheduleTime $sunriseScheduleTime , sunsetScheduleTime $sunsetScheduleTime")

    def localData = getWeatherFeature('geolookup', settings.zipCode as String)

    def timezone = TimeZone.getTimeZone(localData.location.tz_long)

    log.debug( "Sunset today is at $sunsetTime" )
    log.debug( "Sunrise today is at $sunriseTime" )

    unschedule()
    if(sunriseScheduleTime < sunsetScheduleTime)
    {
    	log.debug "Scheduling Sunrise"    
    	schedule(sunriseScheduleTime, sunrise)
    }
    else
    {
    	log.debug "Scheduling Sunset"
    	schedule(sunsetScheduleTime, sunset)
    }

    //Taking this out to save resources. Each time sunset ot sunrise happens it will reschedle the next one
    //schedule(timeTodayAfter(new Date(), '01:00', timezone), scheduleSunriseSunset)
}

def getSunriseWithOffset(srOff) {
	def srOffTime = getSunriseAndSunset(zipCode: settings.zipCode, sunriseOffset:srOff)
    //log.debug(srOffTime)
    return srOffTime.sunrise
}

def getSunsetWithOffset(ssOff) {
	def ssOffTime = getSunriseAndSunset(zipCode: settings.zipCode, sunsetOffset:ssOff)
	return ssOffTime.sunset
}

def sunriseOffset() {
	//log.debug("settings.sunriseOffsetValue ${settings.sunriseOffsetValue}")
    //log.debug("settings.sunriseOffsetDir ${settings.sunriseOffsetDir}")
	if ((settings.sunriseOffsetValue != null) && (settings.sunriseOffsetDir != null)) {
		def offsetString = ""
		if (settings.sunriseOffsetDir == 'Before') {
			offsetString = "-"
		}
		offsetString += settings.sunriseOffsetValue
		return offsetString
	} else {
		return "00:00"
	}
}

def sunsetOffset() {
	//log.debug("settings.sunsetOffsetValue ${settings.sunsetOffsetValue}")
    //log.debug("settings.sunsetOffsetDir ${settings.sunsetOffsetDir}")
    //log.debug((settings.sunsetOffsetValue != null) && (settings.sunsetOffsetDir != null))
	if ((settings.sunsetOffsetValue != null) && (settings.sunsetOffsetDir != null)) {
		def offsetString = ""
		if (settings.sunsetOffsetDir == 'Before') {
			offsetString = "-"
		}
		offsetString += settings.sunsetOffsetValue
		return offsetString
	} else {
		return "00:00"
	}
}

def isDayTime()
{
	def srOff = sunriseOffset()
    def ssOff = sunsetOffset()
    log.debug("srOff: $srOff , ssOff: $ssOff")

    def sunriseScheduleTime = getSunriseWithOffset(srOff)
	def sunsetScheduleTime = getSunsetWithOffset(ssOff)


	if(sunriseScheduleTime > now() || sunsetScheduleTime < now())
	{
    	log.debug "It is night time"
    	state.dayTime = false
    	return false
    }
    else 
    {
    	log.debug "It is day time"
    	state.dayTime = true
    	return true
    }
}


//***********************************************************************************
// Event Functions
//***********************************************************************************

def sunrise() {
	state.dayTime = true
	TRACE("sunrise()")

	/*
	def currentMode = location.mode
	def n = state.sunriseArray.indexOf(currentMode)
    log.debug("currentMode $currentMode sunriseArray ${state.sunriseArray}")
	if (n >= 0) {
		def modeType = state.modeTypes[n]
        log.debug("sunrise modeType $modeType")
        def devices = getModeTypeDevices(n)
        def onSwitches = devices.sunriseOnSwitches
        def offSwitches = devices.sunriseOffSwitches
		if (onSwitches != null) {
        	onSwitches.on()
        }
        if (offSwitches != null) {
        	offSwitches.off()
        }
		changeMode(modeType.dayMode)
	}
	*/
}

def sunset() {
	state.dayTime = true
	TRACE("sunset()")

	/*

	def currentMode = location.mode
	def n = state.sunsetArray.indexOf(currentMode)
	if (n >= 0) {
		def modeType = state.modeTypes[n]
        def devices = getModeTypeDevices(n)
        def onSwitches = devices.sunsetOnSwitches
        def offSwitches = devices.sunsetOffSwitches
		if (onSwitches != null) {
        	onSwitches.on()
        }
        if (offSwitches != null) {
        	offSwitches.off()
        }
		changeMode(modeType.nightMode)
	}
	*/
}


//I dont need mode changing

def changeMode(newMode) {
	if (newMode && location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
			log.debug("has changed the mode to '${newMode}'")
		}
		else {
			log.debug("tried to change to undefined mode '${newMode}'")
		}
	}
}

// debug

def TRACE(msg) {
	log.debug msg
    //log.debug("state $state")
}
		state.setTime = setTime.time

		log.info "scheduling sunset handler for $setTime"
	    schedule(setTime, sunsetHandler)
	}
}

//***********************************************************************************
// ZP HUE CODE
//***********************************************************************************

int getHueColor(color)
{
	def hueColor =  0

	switch(color) 
	{

		case "White":
			hueColor = 52
			break;
		case "Daylight":
			hueColor = 53
			break;
		case "Soft White":
			hueColor = 23
			break;
		case "Warm White":
			hueColor = 20
			break;
		case "Blue":
			hueColor = 70
			break;
		case "Green":
			hueColor = 39
			break;
		case "Yellow":
			hueColor = 25
			break;
		case "Orange":
			hueColor = 10
			break;
		case "Purple":
			hueColor = 75
			break;
		case "Pink":
			hueColor = 83
			break;
		case "Red":
			hueColor = 100
			break;
	}
	return hueColor
}

int getHueSat(color)
{
	def hueSaturation = 100

	switch(color) 
	{
		case "White":
			hueSaturation = 19
			break;
		case "Daylight":
			hueSaturation = 91
			break;
		case "Soft White":
			hueSaturation = 56
			break;
		case "Warm White":
			hueSaturation = 80 //83
			break;
	}
	return hueSaturation
}


//***********************************************************************************
// END ZP HUE CODE
//***********************************************************************************



//HANDELERS

def eventHandler(evt) {
	log.trace "eventHandler($evt.name: $evt.value)"
	if (allOk) {
		log.trace "allOk"
		def lastTime = state[frequencyKey(evt)]
		if (oncePerDayOk(lastTime)) {
			if (frequency) {
				if (lastTime == null || now() - lastTime >= frequency * 60000) {
					takeAction(evt)
				}
			}
			else {
				takeAction(evt)
			}
		}
		else {
			log.debug "Not taking action because it was already taken today"
		}
	}
}

def modeChangeHandler(evt) {
	log.trace "modeChangeHandler $evt.name: $evt.value ($triggerModes)"
	if (evt.value in triggerModes) {
		eventHandler(evt)
	}
}

def scheduledTimeHandler() {
	log.trace "scheduledTimeHandler()"
	eventHandler(null)
}

def appTouchHandler(evt) {
	takeAction(evt)

	//DEBUGING USE
	//zp_fadeStart()
	//log.debug "Auto Update"
	//schedule("20 * * * * ?", zp_pollHues)
	//setLevelsInState()
	//zp_colorFadeStart()
}

private frequencyKey(evt) {
	"lastActionTimeStamp"
}

private dayString(Date date) {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	if (location.timeZone) {
		df.setTimeZone(location.timeZone)
	}
	else {
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	}
	df.format(date)
}

private oncePerDayOk(Long lastTime) {
	def result = true
	if (oncePerDay) {
		result = lastTime ? dayString(new Date()) != dayString(new Date(lastTime)) : true
		log.trace "oncePerDayOk = $result"
	}
	result
}

// TODO - centralize somehow
private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private timeIntervalLabel()
{
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}


def sunriseHandler(evt) {
	log.info "Executing sunrise handler"
	if(transition)
	{
		takeAction(evt)
	}
}

def sunsetHandler(evt) {
	log.info "Executing sunset handler"
	if(transition)
	{
		takeAction(evt)
	}
}
