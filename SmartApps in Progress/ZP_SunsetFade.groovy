/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Sunrise, Sunset
 *
 *  Author: SmartThings
 *
 *  Date: 2013-04-30
 */
definition(
    name: "Sunset Fade",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "Changes hue color from daylight to selected color at sunset",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png"
)



preferences {
	page name: "mainPage"
}

def mainPage(){
	dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
        section("Control these bulbs...") 
        {
            input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:true, multiple:true
        }
        section("Choose color to trnaisition to...")
        {
            input "color", "enum", title: "Hue Color?", required: false, multiple:false, options: [
                ["Soft White":"Soft White - Default"],
                ["White":"White - Concentrate"],
                ["Daylight":"Daylight - Energize"],
                ["Warm White":"Warm White - Relax"],
                "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
            input "lightLevel", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
        }
        section("Choose your transistion time in minutes..")
        {
            input "transistiontime", "number", title: "Transition Time between day and night settings in minutes.. (max 60min)", required: true, defaultValue: 5
        }
        section("Pick other triggers")
        {
            input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
            input "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
        }
        section("More Options")
        {
        	input "modes", "mode", title: "Only when mode is", multiple: true, required: false
        	input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
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

def installed() 
{
	initialize()
}

def updated() 
{
	unsubscribe()
	initialize()
}

def initialize()
{
	subscribe(location, "sunset", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(app, eventHandler)

	if (triggerModes) {
		subscribe(location, eventHandler)
	}
}

def eventHandler(evt)
{
	if (allOk){
		log.trace "modeChangeHandler $evt.name: $evt.value ($triggerModes)"

		hues.each{
			int dimLevel = it.currentValue("level")
			int currenthue = it.currentValue("hue")
			log.debug "Current Level: $dimLevel - Target Level: $lightLevel"
			int targetLevel = lightLevel.toInteger()
			int levelDifference = targetLevel - dimLevel
			log.debug "Level Difference $levelDifference"

			int firstStepLevel = dimLevel + (levelDifference / 3)
			int firstTransTime = (transistiontime * 60 / 3)

			log.debug "TransTime1: $firstTransTime"

			it.setColor([hue: 14, saturation: 5, level: firstStepLevel, transitiontime: firstTransTime])

			runIn(firstTransTime, secondFade)

		}
	}
}

private getAllOk() {
	modeOk && daysOk
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

def secondFade(){


	hues.each{


		def hueColor = 0
		def saturation = 100

		switch(color) {
			case "White":
				hueColor = 52
				saturation = 19
				break;
			case "Daylight":
				hueColor = 53
				saturation = 91
				break;
			case "Soft White":
				hueColor = 23
				saturation = 56
				break;
			case "Warm White":
				hueColor = 20
				saturation = 80 //83
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

		int secondTransTime = (transistiontime * 60 / 3) * 2 

		log.debug "TransTime2: $secondTransTime"
		log.trace hueColor
		log.trace saturation

		it.setColor([hue: hueColor, saturation: saturation, level: lightLevel.toInteger(), transitiontime: secondTransTime])

	}
}

