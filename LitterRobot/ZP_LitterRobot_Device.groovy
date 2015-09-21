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
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        
        command "reset"
        command "man_cycle"

	}

	simulator {

	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'IDLE', icon:"st.camera.camera", backgroundColor:"#79b821"
				attributeState "off", label:'CLEANING', icon:"st.camera.camera", backgroundColor:"#ffa81e"
			}
            tileAttribute ("level", key: "SECONDARY_CONTROL") {
				attributeState "level", label:'${currentValue} Cycles'
			}
		}
        
		standardTile("count", "device.level", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state("count", label: '${currentValue} Cycles')
		}


		standardTile("reset", "device.switch", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "", label:"Reset", action:"reset", icon:"st.secondary.refresh-icon"
		}
        standardTile("addcycle", "device.switch", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "", label:"Cycle", action:"man_cycle", icon:"st.secondary.refresh-icon"
		}
        
        standardTile("status", "device.switch", decoration: "flat") {
            state "on", label:'No cycle Running'
            state "off", label:'Cycle Running'
        }

		main(["count"])
		details(["switch", "reset", "count","addcycle"])
	}
}

def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on")
    log.info "LitterRobot On"
}

def off() {
	sendEvent(name: "switch", value: "off")
    log.info "LitterRobot Off"
}

def man_cycle() {
	log.trace device.currentValue("switch")
	if (device.currentValue("switch") == 'on')
    	{
        	off()
            setLevel(device.currentValue("level") + 1)
        }
       else
       {
       		on()
       }
}	


def setLevel(val){
    log.info "setLevel $val"

    if (val < 0){
    	val = 0
    }
    
    if (val == 0){ // I liked that 0 = off
    	sendEvent(name:"level",value:val)
    }
    else
    {
    	on()
    	sendEvent(name:"level",value:val)
    	sendEvent(name:"switch.setLevel",value:val) // had to add this to work if apps subscribed to
                                                    // setLevel event. "Dim With Me" was one.
    }
}
def reset() {
	// SET COUNT TO 0
    log.trace "SET COUNT TO 0"
    setLevel(0)
    on()
    
}


