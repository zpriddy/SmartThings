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
	def textActionModes =
		"These are the modes that this action will apply to"

	def textSelectSensor =
		"These are the sensors that will trigger this action."

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

	def textAfterEvent =
		"This is to configure actions a period ofter an Event happens."

	def textCustomValues =
		"If you would like to set custom values for after event, enable this switch. Otherwise the lights will reset to the previous values"

	def textTimeDelay = 
		"This is the amount of time to wait before applying after event settings"

	def textDifferentSettings = 
		"Apply different settings for nightime?"

	def n = params?.actionNumber
	if(!n)
	{
		n = state.configNumber
	}
	state.configNumber = n 
	log.debug n

	def name = settings."a${n}_name"
	def hueSettings = settings.hasHue

	
	dynamicPage(name: "actionSettings", title: "Set settings for ${name}"){
		section("Trigger Settings For ${name}", hideable:true, hidden:false) 
		{
			paragraph textActionModes
			input "a${n}_mode", "mode", title: "Modes", multiple: true, required: true

			paragraph textDifferentSettings
			input "a${n}_differentSettings", "boolean", title: "Different Settings for Night?", defaultValue: false, submitOnChange: true

			paragraph textSelectSensor
			input "a${n}_motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
			input "a${n}_contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
			input "a${n}_contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
			input "a${n}_acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
			input "a${n}_mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
			input "a${n}_mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
			input "a${n}_button1", "capability.button", title: "Button Press", required:false, multiple:true

		}
		section("${name} Settings For Event Action (Day)", hideable: true, hidden: false)
		{
			paragraph textDaySwitchOff
			input "a${n}_daySwitchOff", "capability.switch", title: "Day Switches Off",  multiple: true, required: false
			paragraph textDaySwitchOn
			input "a${n}_daySwitchOn", "capability.switch", title: "Day Switches On",  multiple: true, required: false
			paragraph textDayDim
			input "a${n}_dayDim", "capability.switchLevel", title: "Day Dimmer Lights", multiple: true, required: false
			input "a${n}_dayDimLevel", "number", title: "Set Dimmer To This Level", required: false
			
			
			
			if(hueSettings == "true")
			{
				paragraph textDayHue
				input "a${n}_dayHue", "capability.switchLevel", title: "Day Hue Lights", multiple: true, required: false
				input "a${n}_dayHueColor", "enum", title: "Day Hue Color", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "a${n}_dayHueLevel", "number", title: "Set Hue To This Level", required: false
				
			}
		}
		section("${name} Settings For After Event Action (Day)", hideable: true, hidden: false)
		{
			paragraph textAfterEvent
			input "a${n}_afterEventEnable", "boolean", title: "Enable actions after event?", defaultValue: false, submitOnChange: true

			if(settings."a${n}_afterEventEnable" == "true")
			{
				paragraph textCustomValues
				input "a${n}_afterEventCustomValues", "boolean", title: "Set custom values after event?", defaultValue: false, submitOnChange: true

				paragraph textTimeDelay
				input "a${n}_durationDay", "number", title: "Time Delay Seconds?", required: false

				if(settings."a${n}_afterEventCustomValues" == "true")
				{
					paragraph textDaySwitchOff
					input "a${n}_dayAfterSwitchOff", "capability.switch", title: "Day Switches Off After Event",  multiple: true, required: false
					paragraph textDaySwitchOn
					input "a${n}_dayAfterSwitchOn", "capability.switch", title: "Day Switches On After Event",  multiple: true, required: false
					paragraph textDayDim
					input "a${n}_dayAfterDim", "capability.switchLevel", title: "Day Dimmer Lights After Event", multiple: true, required: false
					input "a${n}_dayAfterDimLevel", "number", title: "Set Dimmer To This Level After Event", required: false
					
					
					
					if(hueSettings == "true")
					{
						paragraph textDayHue
						input "a${n}_dayAfterHue", "capability.switchLevel", title: "Day Hue Lights After Event", multiple: true, required: false
						input "a${n}_dayAfterHueColor", "enum", title: "Day Hue Color After Event", required: false, multiple:false, options: [
							["Soft White":"Soft White - Default"],
							["White":"White - Concentrate"],
							["Daylight":"Daylight - Energize"],
							["Warm White":"Warm White - Relax"],
							"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						input "a${n}_dayAfterHueLevel", "number", title: "Set Hue To This Level After Event", required: false
						
					}
				}
			}
		}

		if(settings."a${n}_differentSettings" == "true")
		{
			section("${name} Settings For Event Action (Night)", hideable: true, hidden: false)
			{
				paragraph textNightSwitchOff
				input "a${n}_nightSwitchOff", "capability.switch", title: "Night Switches Off",  multiple: true, required: false
				paragraph textNightSwitchOn
				input "a${n}_nightSwitchOn", "capability.switch", title: "Night Switches On",  multiple: true, required: false
				paragraph textNightDim
				input "a${n}_nightDim", "capability.switchLevel", title: "Night Dimmer Lights", multiple: true, required: false
				input "a${n}_nightDimLevel", "number", title: "Set Dimmer To This Level", required: false
				
				
				
				if(hueSettings == "true")
				{
					paragraph textNightHue
					input "a${n}_nightHue", "capability.switchLevel", title: "Night Hue Lights", multiple: true, required: false
					input "a${n}_nightHueColor", "enum", title: "Night Hue Color", required: false, multiple:false, options: [
						["Soft White":"Soft White - Default"],
						["White":"White - Concentrate"],
						["Nightlight":"Nightlight - Energize"],
						["Warm White":"Warm White - Relax"],
						"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
					input "a${n}_nightHueLevel", "number", title: "Set Hue To This Level", required: false
					
				}
			}
			section("${name} Settings For After Event Action (Night)", hideable: true, hidden: false)
			{

				if(settings."a${n}_afterEventEnable" == "true")
				{

					paragraph textTimeDelay
					input "a${n}_durationNight", "number", title: "Time Delay Seconds?", required: false

					if(settings."a${n}_afterEventCustomValues" == "true")
					{
						paragraph textNightSwitchOff
						input "a${n}_nightAfterSwitchOff", "capability.switch", title: "Night Switches Off After Event",  multiple: true, required: false
						paragraph textNightSwitchOn
						input "a${n}_nightAfterSwitchOn", "capability.switch", title: "Night Switches On After Event",  multiple: true, required: false
						paragraph textNightDim
						input "a${n}_nightAfterDim", "capability.switchLevel", title: "Night Dimmer Lights After Event", multiple: true, required: false
						input "a${n}_nightAfterDimLevel", "number", title: "Set Dimmer To This Level After Event", required: false
						
						
						
						if(hueSettings == "true")
						{
							paragraph textNightHue
							input "a${n}_nightAfterHue", "capability.switchLevel", title: "Night Hue Lights After Event", multiple: true, required: false
							input "a${n}_nightAfterHueColor", "enum", title: "Night Hue Color After Event", required: false, multiple:false, options: [
								["Soft White":"Soft White - Default"],
								["White":"White - Concentrate"],
								["Daylight":"Daylight - Energize"],
								["Warm White":"Warm White - Relax"],
								"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
							input "a${n}_nightAfterHueLevel", "number", title: "Set Hue To This Level After Event", required: false
							
						}
					}
				}
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