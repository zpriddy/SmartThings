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

def setupInit() {
	TRACE("setupInit()")
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
				input name: "m${n}_name", type: "text", tite: "Mode Name", defaultValue: sampleNames[n]?.value, required: true
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

// installed/updated/init

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

    

    //log.debug("init modeTypes ${state.modeTypes}")

    scheduleSunriseSunset()
    TRACE("End init")
}

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

// schedule

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
    schedule(sunriseScheduleTime, sunrise)
    schedule(sunsetScheduleTime, sunset)
    schedule(timeTodayAfter(new Date(), '01:00', timezone), scheduleSunriseSunset)
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


// events

def sunrise() {
	state.dayTime = true
	TRACE("sunrise()")

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
}

def sunset() {
	state.dayTime = true
	TRACE("sunset()")

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