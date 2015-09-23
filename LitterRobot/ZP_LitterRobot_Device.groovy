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
	definition (name: "ZP Litter Robot", namespace: "zpriddy", author: "zpriddy") {
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        
        
        attribute "eswitch", "enum", ["on", "off"]
        attribute "status", "enum", ["IDLE", "CLEANING","OFF"]
        attribute "cycleCount","number"
        
        command "reset"
        command "man_cycle"
        command "man_clean"
        command "man_clean_on"
        command "eswitchOn"
        command "eswitchOff"
        command "cycleStart"
        command "cycleEnd"

	}

	simulator {

	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
			tileAttribute ("status", key: "PRIMARY_CONTROL") {
				attributeState "IDLE", label:'IDLE', icon:"st.camera.camera", backgroundColor:"#79b821"
				attributeState "CLEANING", label:'CLEANING', icon:"st.camera.camera", backgroundColor:"#ffa81e"
                attributeState "MANUAL CLEANING", label:'MANUAL CLEANING', icon:"st.camera.camera", backgroundColor:"#ffa81e"
                attributeState "OFF", label:'OFF', icon:"st.camera.camera", backgroundColor:"#ff0000"
			}
            tileAttribute ("cycleCount", key: "SECONDARY_CONTROL") {
				attributeState "cycleCount", label:'${currentValue} Cycles'
			}
		}
        
		standardTile("count", "cycleCount", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state("count", label: '${currentValue} Cycles')
		}


		standardTile("reset", "device.switch", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "", label:"Reset", action:"reset", icon:"st.secondary.refresh-icon"
		}
        standardTile("addcycle", "device.switch", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "", label:"Cycle Count+", action:"man_cycle", icon:"st.thermostat.thermostat-up"
		}
        
        standardTile("mancycle", "device.switch", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "", label:"Manual Cycle", action:"man_clean", icon:"st.secondary.refresh-icon"
		}
        
        standardTile("status2", "device.switch", decoration: "flat") {
            state "on", label:'No cycle Running'
            state "off", label:'Cycle Running'
        }
        
        standardTile("eswitch", "eswitch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: '${name}', action: "eswitchOn", icon: "st.switches.switch.off", backgroundColor: "#ff0000"
    		state "on", label: '${name}', action: "eswitchOff", icon: "st.switches.switch.on", backgroundColor: "#79b821"
}

		main(["count"])
		details(["status", "reset", "count","mancycle","addcycle","eswitch"])
	}
}

def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on")
    log.info "LitterRobot On"
}

def cycleStart() {
	sendEvent(name: "status", value: "CLEANING")
}

def cycleEnd() {
	sendEvent(name: "status", value: "IDLE")
     man_cycle()
}

def eswitchOn() {
	sendEvent(name: "eswitch", value: "on")
    sendEvent(name: "status", value: "IDLE")
    log.info "Eswitch On"
}

def eswitchOff() {
	sendEvent(name: "eswitch", value: "off")
    sendEvent(name: "status", value: "OFF")
    log.info "Eswitch Off"
}

def off() {
	sendEvent(name: "switch", value: "off")
    log.info "LitterRobot Off"
}



def man_cycle() {
    if(device.currentValue("cycleCount") == null)
    {
    	count_cycle(0)
    }
    count_cycle(device.currentValue("cycleCount") + 1)

}

def man_clean() {
	sendEvent(name: "status", value: "MANUAL CLEANING")
}

def man_clean_on() {
	sendEvent(name: "status", value: "MANUAL CLEANING")
    sendEvent(name: "eswitch", value: "on")
}
def count_cycle(val){
	sendEvent(name:"cycleCount",value:val)
}


def reset() {
	// SET COUNT TO 0
    log.trace "SET COUNT TO 0"
    count_cycle(0)
    sendEvent(name: "eswitch", value: "on")
    sendEvent(name: "status", value: "IDLE")
}



