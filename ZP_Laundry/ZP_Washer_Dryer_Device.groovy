/**
 *  Copyright 2015 Zachary Priddy
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
 */
metadata {
	definition (name: "ZP Washer / Dryer", namespace: "zpriddy", author: "zpriddy") {
		capability "Indicator"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Acceleration Sensor"
        capability "Switch"
        
        
        attribute "washer", "enum", ["idle", "active", "finished"]
        attribute "dryer", "enum", ["idle", "active", "finished"]
        attribute "status", "enum", ["IDLE", "DRYING", "WASHING","WASHING & DRYING", "FINISHED"]
        
        command "washer_start"
        command "dryer_start"
        command "washer_end"
        command "washer_open"
        command "dryer_end"
        command "initalize"


	}

	simulator {

	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
			tileAttribute ("status", key: "PRIMARY_CONTROL") {
				attributeState "IDLE", label:'IDLE', icon:"st.Appliances.appliances1", backgroundColor:"#79b821"
				attributeState "WASHING", label:'WASHING', icon:"st.Appliances.appliances1", backgroundColor:"#ffa81e"
                attributeState "DRYING", label:'DRYING', icon:"st.Appliances.appliances1", backgroundColor:"#ffa81e"
                attributeState "WASHING & DRYING", label:'W & D', icon:"st.Appliances.appliances1", backgroundColor:"#ffa81e"
                attributeState "FINISHED", label:'FINISHED', icon:"st.Appliances.appliances1", backgroundColor:"#ff0000"
			}
		}
        
		standardTile("washer", "device.washer", height: 3, width: 3, canChangeIcon: true, canChangeBackground: true, decoration: "flat") {
			state "idle", label:"Washer Idle", icon:"st.Appliances.appliances1", backgroundColor: "#69AF1A"
            state "active", label:"Washing", icon:"st.Appliances.appliances1", backgroundColor: "#ffa81e"
            state "finished", label:"Washer Done", icon:"st.Appliances.appliances1", backgroundColor: "#ff0000"
		}
        
        standardTile("dryer", "device.dryer", height: 3, width: 3, canChangeIcon: true, canChangeBackground: true, decoration: "flat") {
			state "idle", label:"Dryer Idle", icon:"st.Appliances.appliances1", backgroundColor: "#69AF1A"
            state "active", label:"Drying", icon:"st.Appliances.appliances1", backgroundColor: "#ffa81e"
            state "finished", label:"Dryer Done", icon:"st.Appliances.appliances1", backgroundColor: "#ff0000"
		}
        standardTile("notification", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Notify Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Notify On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}
        
     

		main(["status"])
		details(["status", "washer", "dryer","notification"])
	}
}

def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on")
    runIn(5,off)
}

def off() {
	sendEvent(name: "switch", value: "off")
}

def washer_start() {
	if (device.currentValue("dryer") == null){
    	sendEvent(name: "dryer", value: "idle")
    }
    
	sendEvent(name: "washer", value: "active")
    log.debug device.currentValue("dryer")
    if ( device.currentValue("dryer") == "active" )
    {
		sendEvent(name: "status", value: "WASHING & DRYING")
    }
    else
    {
    	sendEvent(name: "status", value: "WASHING")
    }
}

def washer_end() {
	sendEvent(name: "washer", value: "finished")

	if ( device.currentValue("dryer") == "active" )
    {
		sendEvent(name: "status", value: "DRYING")
    }
    else 
    {
    	sendEvent(name: "status", value: "FINISHED")
    }

}

def washer_open()
{
	sendEvent(name: "washer", value: "idle")
    
    if ( device.currentValue("dryer") == "active" )
    {
		sendEvent(name: "status", value: "DRYING")
    }
    else 
    {
    	sendEvent(name: "status", value: "IDLE")
    }
}

def dryer_start() {
	if (device.currentValue("washer") == null){
    	sendEvent(name: "washer", value: "idle")
    }
	sendEvent(name: "dryer", value: "active")
    if ( device.currentValue("washer") == "active" )
    {
		sendEvent(name: "status", value: "WASHING & DRYING")
    }
    else
    {
    	sendEvent(name: "status", value: "DRYING")
    }
}

def dryer_end() {
	sendEvent(name: "dryer", value: "idle")

	if ( device.currentValue("washer") == "active" )
    {
		sendEvent(name: "status", value: "WASHING")
    }
    else if ( device.currentValue("washer") == "finished" )
    {
    	sendEvent(name: "status", value: "FINISHED")
    }
    else
    {
    	sendEvent(name: "status", value: "IDLE")
    }

}

def initalize(){
	sendEvent(name: "status", value: "IDLE")
    sendEvent(name: "washer", value: "idle")
    sendEvent(name: "dryer", value: "idle")
}


