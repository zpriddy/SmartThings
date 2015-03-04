/**
 *  Hue Multi Mood Lighting with Sunrise Sunset settings 
 *
 *  Author: Zachary Priddy - me@zpriddy.com
 *  
 *  Date: 2015-02-01
 * 
 *  Credit given to Eric Roberts for his work on Better Sunrise/Sunset
 */
definition(
    name: "ZP Master Mode Lighting",
    namespace: "zpriddy",
    author: "Zachary Priddy  me@zpriddy.com",
    description: "Sets the colors and brightness level of your Philips Hue lights to match your mood.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
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

	}
}

def setModeNames()
{
	log.debug "Setting Mode Names"

	return dynamicPage(name: "setModeNames", title: "Set Mode Names", nextPage: "setupModeTypes"){
		def sampleNames = [ "", "Home", "Away", "Vaction", "Sleeping", "Alarm"]
		for (int n = 1; n <= numModeTypes; n++){
			section("Name for Mode ${n}"){
				input name: "m${n}_name", type: "text", tite: "Mode Name", defaultValue: (settings."m${n}_name") ?  settings."m${n}_name" : sampleNames[n]?.value, required: true
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
		"These are the switches that will be turnned off during the day"

	def textDayLightsOn = 
		"These are the switches that will be turnned on during the day"

	def textDayDim =
		"These lights will be set the dimmer value set below. There are two groups of dimmable lights."

	def textDayHue =
		"These Hue lights will be set the colors set below. There are two groups of Hue lights."

	def textNightLightsOff =
		"These are the switches that will be turnned off during the night"

	def textNightLightsOn = 
		"These are the switches that will be turnned on during the day"

	def textNightDim =
		"These lights will be set the dimmer value set below. There are two groups of dimmable lights."

	def textNightHue =
		"These Hue lights will be set the colors set below. There are two groups of Hue lights."

	def textAutoChange =
		"Do you want this mode to auto change to night/day settings on sunset/sunrise. If not it will only chnage when the mode changes."


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
		section("Auto Chnage Day/Night", hideable:false) 
		{
			paragraph textAutoChange
			input "m${n}_auto", "boolean", title: "Auto Chnage", required: true
			paragraph "If your lights have Transition Time capability, you can set this value"
			input "m${n}_autoTransTime", "number", title: "Transition Time between day and night settings in minutes.. (max 60min)", required: false, defaultValue: 5
		}
		section("Day Settings For ${name}", hideable: true, hidden: false)
		{
			paragraph "If your lights have Transition Time capability, you can set this value"
			input "m${n}_dayTransTime", "number", title: "Transition Time when chaning to this mode (sec)", required: false, defaultValue: 3
			paragraph textDayLightsOff
			input "m${n}_dayLightsOff", "capability.switch", title: "Day Switches Off",  multiple: true, required: false
			paragraph textDayLightsOn
			input "m${n}_dayLightsOn", "capability.switch", title: "Day Switches On",  multiple: true, required: false
			paragraph textDayDim
			input "m${n}_dayDim1", "capability.switchLevel", title: "Day Dimmer Lights (Group 1)", multiple: true, required: false
			input "m${n}_dayDim1Level", "number", title: "Set Dimmer To This Level (Group 1)", required: false
			paragraph ""
			input "m${n}_dayDim2", "capability.switchLevel", title: "Day Dimmer Lights (Group 2)", multiple: true, required: false
			input "m${n}_dayDim2Level", "number", title: "Set Dimmer To This Level (Group 2)", required: false
			
			
			if(hueSettings == "true")
			{
				paragraph textDayHue
				input "m${n}_dayHue1", "capability.colorControl", title: "Day Hue Lights (Group 1)", multiple: true, required: false
				input "m${n}_dayHue1Color", "enum", title: "Day Hue Color (Group 1)", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "m${n}_dayHue1Level", "number", title: "Set Hue To This Level (Group 1)", required: false
				paragraph ""
				input "m${n}_dayHue2", "capability.colorControl", title: "Day Hue Lights (Group 2)", multiple: true, required: false
				input "m${n}_dayHue2Color", "enum", title: "Day Hue Color (Group 2)", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "m${n}_dayHue2Level", "number", title: "Set Hue To This Level (Group 2)", required: false
			}
		}
		section("Night Settings For ${name}", hideable: true, hidden: false)
		{
			paragraph "If your lights have Transition Time capability, you can set this value"
			input "m${n}_nightTransTime", "number", title: "Transition Time when chaning to this mode (sec)", required: false, defaultValue: 3
			paragraph textNightLightsOff
			input "m${n}_nightLightsOff", "capability.switch", title: "Night Switches Off",  multiple: true, required: false
			paragraph textNightLightsOn
			input "m${n}_nightLightsOn", "capability.switch", title: "Night Switches On",  multiple: true, required: false
			paragraph textNightDim
			input "m${n}_nightDim1", "capability.switchLevel", title: "Night Dimmer Lights (Group 1)", multiple: true, required: false
			input "m${n}_nightDim1Level", "number", title: "Set Dimmer To This Level (Group 1)", required: false
			paragraph ""
			input "m${n}_nightDim2", "capability.switchLevel", title: "Night Dimmer Lights (Group 2)", multiple: true, required: false
			input "m${n}_nightDim2Level", "number", title: "Set Dimmer To This Level (Group 2)", required: false
			
			
			if(hueSettings == "true")
			{
				paragraph textNightHue
				input "m${n}_nightHue1", "capability.colorControl", title: "Night Hue Lights (Group 1)", multiple: true, required: false
				input "m${n}_nightHue1Color", "enum", title: "Night Hue Color (Group 1)", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "m${n}_nightHue1Level", "number", title: "Set Hue To This Level (Group 1)", required: false
				paragraph ""
				input "m${n}_nightHue2", "capability.colorControl", title: "Night Hue Lights (Group 2)", multiple: true, required: false
				input "m${n}_nightHue2Color", "enum", title: "Night Hue Color (Group 2)", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "m${n}_nightHue2Level", "number", title: "Set Hue To This Level (Group 2)", required: false
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
	subscribeToEvents()
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
    subscribeToEvents()
    TRACE("End init")
    
    state.transitiontime = 4
}

def subscribeToEvents()
{
	subscribe(location, modeChangeHandler)
	subscribe(app, modeChangeHandler)
}


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
    if(sunriseScheduleTime < sunsetScheduleTime  && !isDayTime())
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


	if(sunriseScheduleTime.time > now() || sunsetScheduleTime.time < now())
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
// Hue Functions
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
// Event Functions
//***********************************************************************************

def sunrise() {
	state.dayTime = true
	scheduleSunriseSunset()
	TRACE("sunrise()")
	modeAutoChange()

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
	state.dayTime = false
	scheduleSunriseSunset()
	TRACE("sunset()")
	modeAutoChange()

}

def modeChangeHandler(evt) {
	log.trace "modeChangeHandler $evt.name: $evt.value ($triggerModes)"
	for (int n = 1; n <= numModeTypes; n++)
	{
		def name = settings."m${n}_name"
		def modeName = settings."m${n}_mode"

		log.debug "EVENT VALUE: $evt.value"
		log.debug "MODE NAME $modeName"

		if(isDayTime()) //&& state.daytime == "true"
		{
			state.transitiontime = settings."m${n}_dayTransTime"
		}
		else
		{
			state.transitiontime = settings."m${n}_nightTransTime"
		}

		if(evt.value in modeName)
		{
			modeChnage(n)
		}

	}

	log.debug "Finished Changing All"
}

def modeAutoChange(evt)
{
	for (int n = 1; n <= numModeTypes; n++)
	{
		def name = settings."m${n}_name"
		def modeName = settings."m${n}_mode"
		def autoChnage = settings."m${n}_auto"

		if(location.mode in modeName)
		{
			if(autoChnage == "true")
			{
            	if(settings."m${n}_autoTransTime")
            	{
            		log.debug settings."m${n}_autoTransTime"
            		state.transitiontime = (settings."m${n}_autoTransTime" as Integer) * 60

            	}
            	else
            	{
            		state.transitiontime = 4
            	}
				modeChnage(n)
			}
		}

	}
}

def modeAutoChange()
{
	for (int n = 1; n <= numModeTypes; n++)
	{
		def name = settings."m${n}_name"
		def modeName = settings."m${n}_mode"
		def autoChnage = settings."m${n}_auto"

		if(location.mode in modeName)
		{
			if(autoChnage == "true")
			{
            	if(settings."m${n}_autoTransTime")
            	{
            		log.debug settings."m${n}_autoTransTime"
            		state.transitiontime = (settings."m${n}_autoTransTime" as Integer) * 60

            	}
            	else
            	{
            		state.transitiontime = 4
            	}
				modeChnage(n)
			}
		}

	}
}

def modeChnage(modeNumber)
{
	

	def n = modeNumber

	if(isDayTime() ) //&& state.daytime == "true"
	{
		log.debug "Changing lights for daytime"

		state.lightsOff = settings."m${n}_dayLightsOff"
		state.lightsOn = settings."m${n}_dayLightsOn"
		state.lightsDim1 = settings."m${n}_dayDim1"
		state.lightsDim1Level = settings."m${n}_dayDim1Level"
		state.lightsDim2 = settings."m${n}_dayDim2"
		state.lightsDim2Level = settings."m${n}_dayDim2Level"

		if(settings.hasHue == "true")
		{
			state.hue1 = settings."m${n}_dayHue1"
			state.hue1Color = settings."m${n}_dayHue1Color"
			state.hue1Level = settings."m${n}_dayHue1Level"

			state.hue2 = settings."m${n}_dayHue2"
			state.hue2Color = settings."m${n}_dayHue2Color"
			state.hue2Level = settings."m${n}_dayHue2Level"
		}

	}
	else
	{
		log.debug "Changing lights for night time"

		state.lightsOff = settings."m${n}_nightLightsOff"
		state.lightsOn= settings."m${n}_nightLightsOn"
		state.lightsDim1 = settings."m${n}_nightDim1"
		state.lightsDim1Level = settings."m${n}_nightDim1Level"
		state.lightsDim2 = settings."m${n}_nightDim2"
		state.lightsDim2Level = settings."m${n}_nightDim2Level"

		if(settings.hasHue == "true")
		{
			state.hue1 = settings."m${n}_nightHue1"
			state.hue1Color = settings."m${n}_nightHue1Color"
			state.hue1Level = settings."m${n}_nightHue1Level"

			state.hue2 = settings."m${n}_nightHue2"
			state.hue2Color = settings."m${n}_nightHue2Color"
			state.hue2Level = settings."m${n}_nightHue2Level"
		}
	}

	//Start Chnaging The Lights

	log.debug "Starting to chnage lights"

	int tt = state.transitiontime
    log.debug tt


	state.lightsOff.each{
		light ->
        boolean hasTransTime = false 
        for(capability in light.capabilities)
        { 
            if(capability.toString().contains("Test Capability"))
            {
                hasTransTime = true
                break
                    }

        }
		if(hasTransTime)
		{
			light.off(tt)
		}
		else
		{
			light.off()
		}
		pause(100)
	}

	state.lightsOn.each{
		light ->
        boolean hasTransTime = false 
        for(capability in light.capabilities)
        { 
            if(capability.toString().contains("Test Capability"))
            {
                hasTransTime = true
                break
                    }

        }
		if(hasTransTime)
		{
			light.on(tt)
		}
		else
		{
			light.on()
		}
		pause(100)
	}

	state.lightsDim1.each{
		light ->
        boolean hasTransTime = false 
        for(capability in light.capabilities)
        { 
            if(capability.toString().contains("Test Capability"))
            {
                hasTransTime = true
                break
                    }

        }
		if(hasTransTime)
		{
			light.setLevel(state.lightsDim1Level as int, tt)
		}
		else
		{
			light.setLevel(state.lightsDim1Level as int)
		}
		pause(100)
	}

	state.lightsDim2.each{
		light ->
        boolean hasTransTime = false 
        for(capability in light.capabilities)
        { 
            if(capability.toString().contains("Test Capability"))
            {
                hasTransTime = true
                break
                    }

        }
		if(hasTransTime)
		{
			light.setLevel(state.lightsDim2Level as int, tt)
		}
		else
		{
			light.setLevel(state.lightsDim2Level as int)
		}
		pause(100)
	}

	if(settings.hasHue == "true")
	{
		def hueColor  = getHueColor(state.hue1Color)
		def hueSaturation = getHueSat(state.hue1Color)
		def hueLevel = state.hue1Level
        
        

		state.hue1.each{
			hue ->
            boolean hasTransTime = false 
            for(capability in hue.capabilities)
            { 
            	if(capability.toString().contains("Test Capability"))
                {
                	hasTransTime = true
                    break
               	}
                	
            }

			if(hasTransTime)
			{
				hue.setColor([hue: hueColor, saturation: hueSaturation, level: hueLevel, transitiontime: tt])
			}
			else
			{
				hue.setColor([hue: hueColor, saturation: hueSaturation, level: hueLevel])
			}
			pause(350)

		}

		hueColor  = getHueColor(state.hue2Color)
		hueSaturation = getHueSat(state.hue2Color)
		hueLevel = state.hue2Level

		state.hue2.each{
			hue ->
            boolean hasTransTime = false 
            for(capability in hue.capabilities)
            { 
            	if(capability.toString().contains("Test Capability"))
                {
                	hasTransTime = true
                    break
               	}
                	
            }
			if(hasTransTime)
			{
				hue.setColor([hue: hueColor, saturation: hueSaturation, level: hueLevel, transitiontime: tt])
			}
			else
			{
				hue.setColor([hue: hueColor, saturation: hueSaturation, level: hueLevel])
			}
			pause(350)

		}
	}
    
    
    //state.transitiontime = 4

	log.debug "Finished changing lights"
	scheduleSunriseSunset()

}


// debug

def TRACE(msg) {
	log.debug msg
    //log.debug("state $state")
}