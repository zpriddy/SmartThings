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
	definition (name: "ZP Pet Water Fountain", namespace: "zpriddy", author: "zpriddy") {
		capability "Indicator"
		capability "Switch"
		capability "Sensor"
        capability "Water Sensor"
        
		command "wet"
        command "dry"

	}

	simulator {

	}

	tiles(scale: 2) {
    	multiAttributeTile(name:"water", type: "water", width: 6, height: 4){
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState "wet", label:'FULL', icon:"st.alarm.water.dry", backgroundColor:"#53a7c0"
				attributeState "dry", label:'EMPTY', icon:"st.alarm.water.water", backgroundColor:"#ff0000"
			}
		}
        
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: '${name}', action: "on", icon: "st.switches.switch.off", backgroundColor: "#ff0000"
    		state "on", label: '${name}', action: "off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
}

		main(["water"])
		details(["water","switch"])
	}
}

def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on")
    log.info "Water Fountain On"
}


def off() {
	sendEvent(name: "switch", value: "off")
    log.info "Water Fountain Off"
}


def dry() {
	sendEvent(name: "water", value: "dry")
    log.info "Water Fountain Empty"
}

def wet() {
	sendEvent(name: "water", value: "wet")
    log.info "Water Fountain Full"
}