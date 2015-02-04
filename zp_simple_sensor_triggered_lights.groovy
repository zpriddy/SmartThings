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
    name: "ZP Sensor Triggered Lights",
    namespace: "zpriddy",
    author: "Zachary Priddy  me@zpriddy.com",
    description: "Changes lights based on sensor actions",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences {
	page name:"setupInit"
	page name:"setupConfigure"
	page name:"setupActions"
	page name:"setActionNames"
	page name:"actionSettings"
}

//***********************************************************************************
// Configuration Functions
//***********************************************************************************

def setupInit() {
	TRACE("setupInit()")
	log.debug "Current Settings: ${settings}"

	if (state.installed) {
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

	def inputNumOfActions = [
		name: 			"numOfActions",
		type: 			"number",
		title: 			"How many Sensor - Switch actions?",
		defaultValue: 	"3",
		required: 		true
	]


	def pageProperties = [
		name: 			"setupConfigure",
		title: 			"General Setup",
		nextPage: 		"setActionNames",
		uninstall: 		true
	]

	return dynamicPage(pageProperties) {
		section("Number of Sensor Actions") {
			paragraph modeHelp
			input inputNumOfActions
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

def setActionNames()
{
	log.debug "Setting Action  Names"

	return dynamicPage(name: "setActionNames", title: "Set Action Names", nextPage: "setupActions"){
		def sampleNames = [ "Front Door", "Back Door", "Living Room Motion", "Kitchen Motion",]
		for (int n = 1; n <= numOfActions; n++){
			section("Name for Action ${n}"){
				input name: "a${n}_name", type: "text", tite: "Action Name", defaultValue: (settings."a${n}_name") ?  settings."a${n}_name" : sampleNames[n]?.value, required: true
			}
		}
		section("Help..", hideable: true, hidden: true)
		{
			paragraph "This is where you give each action sent a name so you can tell them apart."
		}
	}


}

def setupActions() {
	TRACE("setupActions()")

	def modeHelp = "Each action will have different settings for Night and Day so you won't need Away Day and Away Night. This SmartApp will handle it on its own."

	

	def pageProperties = [
		name: 		"setupActions",
		title: 		"Configure Actions",
		install: 	true,
		uninstall: 	state.installed
	]

	return dynamicPage(pageProperties) {
		for (int n = 1; n <= numOfActions; n++) {
			def name = settings."a${n}_name"

			section()
			{
				
				href(name: "actionSettings", page: "actionSettings?actionNumber=$n", title: "Settings for $name Mode", description: "Open Settings", state: "complete")
			}

		}
		section("Help..", hideable: true, hidden: true)
		{
			paragraph "This is where you configure each action set. \nRemember each mode has both a night and day setting built in."
		}

	}
}

def actionSettings(params)
{
	def n = params?.actionNumber
	if(!n)
	{
		n = state.configNumber
	}
	state.configNumber = n 
	log.debug n

	def name = settings."a${n}_name"
	//def hueSettings = settings.hasHue

	dynamicPage(name: "actionSettings", title: "Set settings for ${name}"){
		def anythingSet = anythingSet()
		if (anythingSet) 
		{

			section("Trigger actions for $name")
			{
				

				ifSet "a${n}_motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
				ifSet "a${n}_contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
				ifSet "a${n}_contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
				ifSet "a${n}_acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
				ifSet "a${n}_mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
				ifSet "a${n}_mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
				ifSet "a${n}_arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
				ifSet "a${n}_departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
				ifSet "a${n}_smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
				ifSet "a${n}_water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
				ifSet "a${n}_button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
				ifSet "a${n}_triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
				//ifSet "timeOfDay", "time", title: "At a Scheduled Time", required: false
			}
		}
		section(anythingSet ? "Select additional triggers" : "Set the lighting mood when...", hideable: anythingSet, hidden: true)
		{
			ifUnset "a${n}_motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
			ifUnset "a${n}_contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
			ifUnset "a${n}_contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
			ifUnset "a${n}_acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
			ifUnset "a${n}_mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
			ifUnset "a${n}_mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
			ifUnset "a${n}_arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
			ifUnset "a${n}_departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
			ifUnset "a${n}_smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
			ifUnset "a${n}_water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
			ifUnset "a${n}_button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
			ifUnset "a${n}_triggerModes", "mode", title: "System Changes Mode", description: "Select mode(s)", required: false, multiple: true
			//ifUnset "timeOfDay", "time", title: "At a Scheduled Time", required: false
		}

		section("Daytime Dimmers")
		{
			input "a${n}_dayDimmers","capability.switchLevel", title: "Daytime Dimmers", multiple: true, required: false
			input "a${n}_dayDimmerLevel", "number", title: "Set Dimmer To This Level", required: false
			if(hasHue == "true")
			{
				input "a${n}_dayHueColor", "enum", title: "Day Hue Color", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
			}

			input "a${n}_durationDay", "number", title: "How Long to apply these settings? (seconds)", required: false

			input "a${n}_resetAfterDelay", "boolean", title: "Set back to orginal settings after delay", defaultValue: true, submitOnChange: true

			if(settings."a${n}_resetAfterDelay" == "false")
			{
				input "a${n}_dayDimmerLevelAfter", "number", title: "Set Dimmer To This Level", required: false
				if(hasHue == "true")
				{
					input "a${n}_dayHueColorAfter", "enum", title: "Day Hue Color", required: false, multiple:false, options: [
						["Soft White":"Soft White - Default"],
						["White":"White - Concentrate"],
						["Daylight":"Daylight - Energize"],
						["Warm White":"Warm White - Relax"],
						"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				}
			}

		}

		section("Settings for Night")
		{
			input "a${n}_sameForNight", "boolean", title: "Use same settings for night", defaultValue: true, submitOnChange: true

			if(settings."a${n}_sameForNight" == "false")
			{
				input "a${n}_nightDimmers","capability.switchLevel", title: "Nighttime Dimmers", multiple: true, required: false
				input "a${n}_nightDimmerLevel", "number", title: "Set Dimmer To This Level", required: false
				if(hasHue == "true")
				{
					input "a${n}_nightHueColor", "enum", title: "Night Hue Color", required: false, multiple:false, options: [
						["Soft White":"Soft White - Default"],
						["White":"White - Concentrate"],
						["Nightlight":"Nightlight - Energize"],
						["Warm White":"Warm White - Relax"],
						"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				}

				input "a${n}_durationNight", "number", title: "How Long to apply these settings? (seconds)", required: false

				input "a${n}_resetAfterDelay", "boolean", title: "Set back to orginal settings after delay", defaultValue: true, submitOnChange: true

				if(settings."a${n}_resetAfterDelay" == "false")
				{
					input "a${n}_nightDimmerLevelAfter", "number", title: "Set Dimmer To This Level", required: false
					if(hasHue == "true")
					{
						input "a${n}_nightHueColorAfter", "enum", title: "Night Hue Color", required: false, multiple:false, options: [
							["Soft White":"Soft White - Default"],
							["White":"White - Concentrate"],
							["Daylight":"Daylight - Energize"],
							["Warm White":"Warm White - Relax"],
							"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
					}
				}

			}
		}

	}

}


private anythingSet() {
	def n = state.configNumber
	for (name in ["a${n}_motion","a${n}_contact","a${n}_contactClosed","a${n}_acceleration","a${n}_mySwitch","a${n}_mySwitchOff","a${n}_arrivalPresence","a${n}_departurePresence","a${n}_smoke","a${n}_nater","a${n}_button1","a${n}_triggerModes","a${n}_timeOfDay"]) {
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

    //scheduleSunriseSunset()
    subscribeToEvents()
    TRACE("End init")
}

def subscribeToEvents()
{
	subscribe(location, modeChangeHandler)
}

def TRACE(msg) {
	log.debug msg
    //log.debug("state $state")
}

