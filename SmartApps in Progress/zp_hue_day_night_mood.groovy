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
    name: "ZP Hue Day Night Mood",
    namespace: "zpriddy",
    author: "Zachary Priddy  me@zpriddy.com",
    description: "Sets the colors and brightness level of your Philips Hue lights to match your mood.",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences {
	page name:"setupInit"
    page name:"setupConfigure"
	page name:"setupDay"
	page name:"setupNight"
}

def setupInit() {
	
	log.debug "Current Settings: ${settings}"

	if (state.installed) {
		//return setupModeTypes()
        return setupConfigure()
	} else {
		return setupConfigure()
	}
}

def setupConfigure() 
{
	def pageProperties = [
    	name: "setupConfigure",	
        title: "General Setup", 
        nextPage: "setupDay", 
        uninstall: true	
        ]

	return dynamicPage(pageProperties) {
		section("Select HelloHome Indacator Switche") {
			input "switches", "capability.switch", multiple: true
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

def setupDay()
{
	def pageProperties = [
			name: 			"setupDay",
			title: 			"Day Setup",
			nextPage: 		"setupNight",
			uninstall: 		true
		]

	return dynamicPage(pageProperties) {
    	section("Day Options") {
        	input "dayTransTime", "number", title: "Transition Time when chaning to this mode (sec)", required: false, defaultValue: 4
        	input "dayLightsOff", "capability.switch", title: "Day Switches Off",  multiple: true, required: false
        
        }
        section("Set Hue 1 Day Settings") {
            input "dayHue1", "capability.colorControl", title: "Day Hue Lights (Group 1)", multiple: true, required: false
            input "dayHue1Color", "enum", title: "Day Hue Color (Group 1)", required: false, multiple:false, options: [
                ["Soft White":"Soft White - Default"],
                ["White":"White - Concentrate"],
                ["Daylight":"Daylight - Energize"],
                ["Warm White":"Warm White - Relax"],
                "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
            input "dayHue1Level", "number", title: "Set Hue To This Level (Group 1)", required: false
        }
        section("Set Hue 2 Day Settings") {
            input "dayHue2", "capability.colorControl", title: "Day Hue Lights (Group 2)", multiple: true, required: false
            input "dayHue2Color", "enum", title: "Day Hue Color (Group 2)", required: false, multiple:false, options: [
                ["Soft White":"Soft White - Default"],
                ["White":"White - Concentrate"],
                ["Daylight":"Daylight - Energize"],
                ["Warm White":"Warm White - Relax"],
                "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
            input "dayHue2Level", "number", title: "Set Hue To This Level (Group 2)", required: false
        }
        section("Set Hue 3 Day Settings") {
            input "dayHue3", "capability.colorControl", title: "Day Hue Lights (Group 3)", multiple: true, required: false
            input "dayHue3Color", "enum", title: "Day Hue Color (Group 3)", required: false, multiple:false, options: [
                ["Soft White":"Soft White - Default"],
                ["White":"White - Concentrate"],
                ["Daylight":"Daylight - Energize"],
                ["Warm White":"Warm White - Relax"],
                "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
            input "dayHue3Level", "number", title: "Set Hue To This Level (Group 3)", required: false
        }
    }
}

def setupNight()
{
	def pageProperties = [
			name: 			"setupNight",
			title: 			"Night Setup",
			uninstall: 		true
		]
	return dynamicPage(pageProperties) {
    	section("Night Options") {
        	input "nightTransTime", "number", title: "Transition Time when chaning to this mode (sec)", required: false, defaultValue: 4
        	input "nightLightsOff", "capability.switch", title: "Night Switches Off",  multiple: true, required: false
        
        }
        section("Set Hue 1 Night Settings") {
            input "nightHue1", "capability.colorControl", title: "Night Hue Lights (Group 1)", multiple: true, required: false
            input "nightHue1Color", "enum", title: "Night Hue Color (Group 1)", required: false, multiple:false, options: [
                ["Soft White":"Soft White - Default"],
                ["White":"White - Concentrate"],
                ["Daylight":"Daylight - Energize"],
                ["Warm White":"Warm White - Relax"],
                "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
            input "nightHue1Level", "number", title: "Set Hue To This Level (Group 1)", required: false
        }
        section("Set Hue 2 Night Settings") {
            input "nightHue2", "capability.colorControl", title: "Night Hue Lights (Group 2)", multiple: true, required: false
            input "nightHue2Color", "enum", title: "Night Hue Color (Group 2)", required: false, multiple:false, options: [
                ["Soft White":"Soft White - Default"],
                ["White":"White - Concentrate"],
                ["Daylight":"Daylight - Energize"],
                ["Warm White":"Warm White - Relax"],
                "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
            input "nightHue2Level", "number", title: "Set Hue To This Level (Group 2)", required: false
        }
        section("Set Hue 3 Night Settings") {
            input "nightHue3", "capability.colorControl", title: "Night Hue Lights (Group 3)", multiple: true, required: false
            input "dayHue3Color", "enum", title: "Night Hue Color (Group 3)", required: false, multiple:false, options: [
                ["Soft White":"Soft White - Default"],
                ["White":"White - Concentrate"],
                ["Daylight":"Daylight - Energize"],
                ["Warm White":"Warm White - Relax"],
                "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
            input "nightHue3Level", "number", title: "Set Hue To This Level (Group 3)", required: false
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
	subscribe(switches, "switch.on", moodLightHandler)
	subscribe(app, moodLightHandler)
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
// Action Functions
//***********************************************************************************

def moodLightHandler(evt)
{

}

