/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Laundry Monitor
 *
 *  Author: SmartThings
 *
 *  Sends a message and (optionally) turns on or blinks a light to indicate that laundry is done.
 *
 *  Date: 2013-02-21
 */

definition(
	name: "ZP Laundry Monitor",
	namespace: "zpriddy",
	author: "zpriddy",
	description: "Sends a message and (optionally) turns on or blinks a light to indicate that laundry is done.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/FunAndSocial/App-HotTubTuner.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/FunAndSocial/App-HotTubTuner%402x.png"
)

preferences {
	section("Washer / Dryer Virtual Device"){
		input "washerdryer", "capability.accelerationSensor", title: "Washer / Dryer Virtual Device"
	}
	section("Dryer Sensor"){
		input "dryer", "capability.accelerationSensor"
	}
	section("Washer Sensor"){
		input "washer", "capability.accelerationSensor"
	}
	section("Washer Door"){
		input "washerDoor", "capability.contactSensor"
	}

	section( "Notifications" ) {
		input("recipients", "contact", title: "Send notifications to") {
			input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
			input "phone1", "phone", title: "Send a Text Message?", required: false
		}
	}
	
	section("Time thresholds (in minutes, optional)"){
		input "cycleTime", "decimal", title: "Minimum cycle time", required: false, defaultValue: 10
		input "fillTime", "decimal", title: "Time to fill tub", required: false, defaultValue: 5
		input "dryerCycleTime" , "decimal", title: "Dryer Min cycle time", required: false, defaultValue: 40
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

def initialize() {

	subscribe(washer, "acceleration", washerHandler)
	subscribe(dryer, "acceleration", dryerHandler)
	subscribe(washerDoor, "contact", contactHandeler)
	
	state.washerIsRunning = false
	state.dryerIsRunning = false
	state.dryerNotification = false
	state.washerNotification = false
	
	washerdryer.initalize()
}


def dryerHandler(evt) {
	if (evt.value == 'active') {
		if (state.dryerIsRunning == false) {
			log.trace "Starting Dryer"
			state.dryerIsRunning = true
			state.dryerStartedAt = now()
			washerdryer.dryer_start()
		}
		else {
			unschedule("dryerCheckFinished")
			log.trace "Dryer still active"
		}
	}
	else {
		log.trace "Dryer inactive.. Checking in 2 minutes."
		runIn(60*2,dryerCheckFinished, [overwrite: true])
	}
}

def dryerCheckFinished() {
	log.trace "Checking if dryer is finished."
	def sensorStates = dryer.statesSince("acceleration", new Date((now() - 110000) as Long))
	if (!sensorStates.find{it.value == "active"}){
		def duration = now() - state.dryerStartedAt
		def cycleTimeMsec = dryerCycleTime ? dryerCycleTime * 60000 : 600000
		if (duration >= cycleTimeMsec) {
			log.trace "Finished Drying"
			washerdryer.dryer_end()
			state.dryerNotification = true
			state.dryerIsRunning = false
			//TODO: Send Dryer Notification
            sendNotification()
		}
		else {
			log.trace "Dryer Stop FP"
			state.dryerNotification = false
			state.dryerIsRunning = false
			washerdryer.dryer_end()
		}
	}
}

def washerHandler(evt) {
	if (evt.value == 'active') {
		if (state.washerIsRunning == false) {
			log.trace "Starting Washer"
			state.washerIsRunning = true
			state.washerStartedAt = now()
			washerdryer.washer_start()
		}
		else {
			unschedule("washerCheckFinished")
			log.trace "Washer still active"
		}
	}
	else {
		log.trace "Washer inactive.. Checking in after filltime delay."
		runIn(60*fillTime,washerCheckFinished, [overwrite: true])
	}
}

def washerCheckFinished() {
	log.trace "Checking if washer is finished."
	def sensorStates = washer.statesSince("acceleration", new Date((now() - 60000 * fillTime) as Long))
	if (!sensorStates.find{it.value == "active"}){
		def duration = now() - state.washerStartedAt
		def cycleTimeMsec = cycleTime ? cycleTime * 60000 : 1800000
		log.trace cycleTimeMsec
		if (duration >= cycleTimeMsec) {
			log.trace "Finished Washing"
			washerdryer.washer_end()
			state.washerNotification = true
			state.washerIsRunning = false
			//TODO: Send Washer Notification
            sendNotification()
		}
		else {
			log.trace "Washer Stop FP"
			state.washerNotification = false
			state.washerIsRunning = false
			washerdryer.washer_open()
		}
	}
}

def contactHandeler(evt){
	if (evt.value == "open")
	{
		log.trace "Washer Door Opened"
		
		washerdryer.washer_open()
		state.washerIsRunning = false
		state.washerNotification = false
		state.dryerNotification = false

		unschedule("sendNotification")
	}
}

def sendNotification() {
	if (state.washerIsRunning == true){
		if (state.dryerNotification == true) {
			send("Dryer is finished")
		}
	}
	else if (state.dryerIsRunning == true) {
		log.trace "Not sending notification because dryer is still running.."
	}
	else {
		if (state.dryerNotification && state.washerNotification) {
			send("The washer and Dryer are finsihed..")
			runIn(60*60, sendNotification)
		}
		else if (state.washerNotification) {
			send("The washer is finished")
			runIn(60*60, sendNotification)
		}
		else if (state.dryerNotification) {
			send("The dryer is finished.")
		}
	}
}

private send(msg) {
		if (location.contactBookEnabled) {
			log.debug("sending notifications to: ${recipients?.size()}")
			sendNotificationToContacts(msg, recipients)
		}
		else {
			if (sendPushMessage != "No") {
				log.debug("sending push message")
				sendPush(msg)
			}
			if (phone1) {
				log.debug("sending text message")
				sendSms(phone1, msg)
			}
		}
		log.debug msg
}