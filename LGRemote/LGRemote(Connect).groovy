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
 *  Lgremote Service Manager
 *
 *  Author: SmartThings
 */
definition(
	name: "LGremote (Connect)",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Allows you to control your Lgremote from the SmartThings app. Perform basic functions like play, pause, stop, change track, and check artist and song name from the Things screen.",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/lgremote.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/lgremote@2x.png"
)

preferences {
	page(name:"mainPage", title:"LG TV Setup", content:"mainPage", refreshTimeout:5)
	page(name:"lgremoteDiscovery", title:"Lgremote Device Setup", content:"lgremoteDiscovery", refreshTimeout:5)
	page(name:"verifyTVcode", title:"Lgremote Verify Setup", content:"verifyTVcode", refreshTimeout:5)
}

def mainPage() {
	if(canInstallLabs()) {
		def lgremotes = lgremotesDiscovered()
		if (state.verified && lgremotes) {
			return verifyTVcode()
		} else {
			return lgremoteDiscovery()
		}
	} else {
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

		return dynamicPage(name:"bridgeDiscovery", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section("Upgrade") {
				paragraph "$upgradeNeeded"
			}
		}
	}
}
//PAGES
def lgremoteDiscovery(params=[:])
{
	
	int lgremoteRefreshCount = !state.lgremoteRefreshCount ? 0 : state.lgremoteRefreshCount as int
	state.lgremoteRefreshCount = lgremoteRefreshCount + 1
	def refreshInterval = 3

	def options = lgremotesDiscovered() ?: []

	def numFound = options.size() ?: 0

	if(!state.subscribe) {
		log.trace "subscribe to location"
		subscribe(location, null, locationHandler, [filterEvents:false])
		state.subscribe = true
	}

	//lgremote discovery request every 5 //25 seconds
	if((lgremoteRefreshCount % 8) == 0) {
		discoverLgremotes()
	}

	//setup.xml request every 3 seconds except on discoveries
	if(((lgremoteRefreshCount % 1) == 0) && ((lgremoteRefreshCount % 8) != 0)) {
		verifyLgremoteTv()
	}

	return dynamicPage(name:"lgremoteDiscovery", title:"Discovery Started!", nextPage:"verifyTVcode", refreshInterval:refreshInterval, install:true, uninstall: true) {
		section("Please wait while we discover your Lgremote. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedLgremote", "enum", required:false, title:"Select Lgremote (${numFound} found)", multiple:true, options:options
		}
	}
	
}

def verifyTVcode()
{
	int linkRefreshcount = !state.linkRefreshcount ? 0 : state.linkRefreshcount as int
	state.linkRefreshcount = linkRefreshcount + 1
	def refreshInterval = 3

	def nextPage = ""
	def title = "Verify Your TV"
	def paragraphText = "Enter the code on your TV"
	//if (state.username) { //if discovery worked
	//	nextPage = "bulbDiscovery"
	//	title = "Success! - click 'Next'"
	//	paragraphText = "Linking to your hub was a success! Please click 'Next'!"
	//}

	//if((linkRefreshcount % 2) == 0 && !state.username) {
	//	sendDeveloperReq()
	//}

	return dynamicPage(name:"verifyTVcode", title:title, nextPage:nextPage, refreshInterval:refreshInterval) {
		section("Verify TV") {

			paragraph """${paragraphText}"""
			input "tvcode", "number", title: "What is the code deisplayed on the TV", required: true, multiple: false
		}
	}
}

private discoverLgremotes()
{
	//consider using other discovery methods
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:MediaRenderer:1", physicalgraph.device.Protocol.LAN))
}


private verifyLgremoteTv() {
	def devices = getLgremoteTv().findAll { it?.value?.verified != true }

	if(devices) {
		log.warn "UNVERIFIED TVS!: $devices"
	}

	devices.each {
		verifyLgremote((it?.value?.ip + ":" + it?.value?.port), it?.value?.ssdpPath)
	}
}

private verifyLgremote(String deviceNetworkId, String ssdpPath) {

	log.trace "dni: $deviceNetworkId"
	String ip = getHostAddress(deviceNetworkId)

	log.trace "ip:" + ip

	if(!ssdpPath){
		ssdpPath = "/"
	}

	sendHubCommand(new physicalgraph.device.HubAction("""GET $ssdpPath HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
}

Map lgremotesDiscovered() {
	def vlgremotes = getVerifiedLgremoteTv()
	def map = [:]
	vlgremotes.each {
		def value = "${it.value.name}"
		def key = it.value.ip + ":" + it.value.port
		map["${key}"] = value
	}
	map
}

def getLgremoteTv()
{
	state.lgremotes = state.lgremotes ?: [:]
}

def getVerifiedLgremoteTv()
{
	getLgremoteTv().findAll{ it?.value?.verified == true }
}

def installed() {
	log.trace "Installed with settings: ${settings}"
	state.verified = false 
	initialize()}

def updated() {
	log.trace "Updated with settings: ${settings}"
	unschedule()
	initialize()
}

def uninstalled() {
	def devices = getChildDevices()
	log.trace "deleting ${devices.size()} Lgremote"
	devices.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}

def initialize() {
	// remove location subscription aftwards
	unsubscribe()
	state.subscribe = false

	unschedule()
	scheduleActions()

	if (selectedLgremote) {
		addLgremote()
	}

	scheduledActionsHandler()
}

def scheduledActionsHandler() {
	log.trace "scheduledActionsHandler()"
	syncDevices()
	refreshAll()

	// TODO - for auto reschedule
	if (!state.threeHourSchedule) {
		scheduleActions()
	}
}

private scheduleActions() {
	def sec = Math.round(Math.floor(Math.random() * 60))
	def min = Math.round(Math.floor(Math.random() * 60))
	def hour = Math.round(Math.floor(Math.random() * 3))
	def cron = "$sec $min $hour/3 * * ?"
	log.debug "schedule('$cron', scheduledActionsHandler)"
	schedule(cron, scheduledActionsHandler)

	// TODO - for auto reschedule
	state.threeHourSchedule = true
	state.cronSchedule = cron
}

private syncDevices() {
	log.trace "Doing Lgremote Device Sync!"
	//runIn(300, "doDeviceSync" , [overwrite: false]) //schedule to run again in 5 minutes

	if(!state.subscribe) {
		subscribe(location, null, locationHandler, [filterEvents:false])
		state.subscribe = true
	}

	discoverLgremotes()
}

private refreshAll(){
	log.trace "refreshAll()"
	childDevices*.refresh()
	log.trace "/refreshAll()"
}

def addLgremote() {
	def tvs = getVerifiedLgremoteTv()
	def runSubscribe = false
	selectedLgremote.each { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			def newTv = tvs.find { (it.value.ip + ":" + it.value.port) == dni }
			log.trace "newTv = $newTv"
			log.trace "dni = $dni"
			d = addChildDevice("smartthings", "Lgremote Tv", dni, newTv?.value.hub, [label:"${newTv?.value.name} Lgremote"])
			log.trace "created ${d.displayName} with id $dni"

			d.setModel(newTv?.value.model)
			log.trace "setModel to ${newTv?.value.model}"

			runSubscribe = true
		} else {
			log.trace "found ${d.displayName} with id $dni already exists"
		}
	}
}

def locationHandler(evt) {
	def description = evt.description
	def hub = evt?.hubId

	def parsedEvent = parseEventMessage(description)
	parsedEvent << ["hub":hub]

	if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:MediaRenderer:1"))
	{ //SSDP DISCOVERY EVENTS

		log.trace "lgremote found"
		def lgremotes = getLgremoteTv()

		if (!(lgremotes."${parsedEvent.ssdpUSN.toString()}"))
		{ //lgremote does not exist
			lgremotes << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
		}
		else
		{ // update the values

			log.trace "Device was already found in state..."

			def d = lgremotes."${parsedEvent.ssdpUSN.toString()}"
			boolean deviceChangedValues = false

			if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
				d.ip = parsedEvent.ip
				d.port = parsedEvent.port
				deviceChangedValues = true
				log.trace "Device's port or ip changed..."
			}

			if (deviceChangedValues) {
				def children = getChildDevices()
				children.each {
					if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
						log.trace "updating dni for device ${it} with mac ${parsedEvent.mac}"
						it.setDeviceNetworkId((parsedEvent.ip + ":" + parsedEvent.port)) //could error if device with same dni already exists
					}
				}
			}
		}
	} else if (parsedEvent.headers && parsedEvent.body) {
		// LGREMOTE RESPONSES
		log.trace "BODY PARESE"
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def type = (headerString =~ /Content-Type:.*/) ? (headerString =~ /Content-Type:.*/)[0] : null
		if (type?.contains("xml")) {
			// description.xml response (application/xml)
			def body = parseXmlBody(parsedEvent.body)
			log.warn body
			log.warn body?.device?.modelName?.text()
			if (body?.device?.modelName?.text().startsWith("LG TV") && !body?.device?.modelName?.text().toLowerCase().contains("bridge") && !body?.device?.modelName?.text().contains("Sub")) {
				def lgremotes = getLgremoteTv()
				def tv = lgremotes.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (tv) {
					tv.value << [name:body?.device?.modelName?.text(),model:body?.device?.modelName?.text(), serialNumber:body?.device?.serialNum?.text(), verified: true]
				} else {
					log.error "/xml/device_description.xml returned a device that didn't exist"
				}
			}
		}
	} else {
		log.trace "cp desc: " + description
	}
}

private def parseXmlBody(def body) {
	def decodedBytes = body.decodeBase64()
	def bodyString
	try {
		bodyString = new String(decodedBytes)
	} catch (Exception e) {
		// Keep this log for debugging StringIndexOutOfBoundsException issue
		log.error("Exception decoding bytes in lgremote connect: ${decodedBytes}")
		throw e
	}
	return new XmlSlurper().parseText(bodyString)
}

private def parseEventMessage(Map event) {
	//handles lgremote attribute events
	return event
}

private def parseEventMessage(String description) {
	def event = [:]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('devicetype:')) {
			def valueString = part.split(":")[1].trim()
			event.devicetype = valueString
		}
		else if (part.startsWith('mac:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.mac = valueString
			}
		}
		else if (part.startsWith('networkAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ip = valueString
			}
		}
		else if (part.startsWith('deviceAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.port = valueString
			}
		}
		else if (part.startsWith('ssdpPath:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ssdpPath = valueString
			}
		}
		else if (part.startsWith('ssdpUSN:')) {
			part -= "ssdpUSN:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpUSN = valueString
			}
		}
		else if (part.startsWith('ssdpTerm:')) {
			part -= "ssdpTerm:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpTerm = valueString
			}
		}
		else if (part.startsWith('headers')) {
			part -= "headers:"
			def valueString = part.trim()
			if (valueString) {
				event.headers = valueString
			}
		}
		else if (part.startsWith('body')) {
			part -= "body:"
			def valueString = part.trim()
			if (valueString) {
				event.body = valueString
			}
		}
	}

	event
}


/////////CHILD DEVICE METHODS
def parse(childDevice, description) {
	def parsedEvent = parseEventMessage(description)

	if (parsedEvent.headers && parsedEvent.body) {
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())
		log.trace "parse() - ${bodyString}"

		def body = new groovy.json.JsonSlurper().parseText(bodyString)
	} else {
		log.trace "parse - got something other than headers,body..."
		return []
	}
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress(d) {
	def parts = d.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}
