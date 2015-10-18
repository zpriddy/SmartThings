/**
 *  Whole House Fan
 *
 *  Copyright 2014 Brian Steere
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
definition(
    name: "ZP Whole House Fan",
    namespace: "zpriddy",
    author: "Brian Steere & Zachary Priddy",
    description: "Toggle a whole house fan (switch) when: Outside is cooler than inside, Inside is above x temp, Thermostat is off",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan%402x.png"
)


preferences {
	section("Outdoor") {
		input "outTemp", "capability.temperatureMeasurement", title: "Outdoor Thermometer", required: true
	}
    
    section("Indoor") {
    	input "inTemp", "capability.temperatureMeasurement", title: "Indoor Thermometer", required: true
        input "minTemp", "number", title: "Minimum Indoor Temperature"
        input "fans", "capability.switch", title: "Vent Fan", multiple: true, required: true
    }
    
    section("Thermostat") {
    	input "thermostat", "capability.thermostat", title: "Thermostat"
    }
    
    section("Windows/Doors") {
    	paragraph "[Optional] Only turn on the fan if at least one of these is open"
        input "checkContacts", "boolean", title: "Check windows/doors", default: false, required: true 
    	input "contacts", "capability.contactSensor", title: "Windows/Doors", multiple: true, required: false
        input "numWindows", "number", title: "This many windows need to be open to run", default: 1, required: true
    }
    section("Advanced Options") {
    	paragraph "These are still in testing and beta"
		input "autoThermoOff", "boolean", title: "Turn thermosta off when the number of windows above are opened?", default: false, required: true
        input "indoorFan", "boolean", title: "Do you have an indoor fan that you would like to turn on as well when the windows are open?", default: false, required: true
        input "indoorFans", "capability.switch", title: "IndoorFan", multiple: false, required: false
        input "indoorFanSpeedControl", "boolean", title: "Do you have a 3 speed switch on the indoor fan?", default: false, required: true
        input "indoorFanSpeedSetting", "enum", title: "What speed when windows are opened?", options: ['LOW', 'MEDIUM', 'HIGH'], required: false 
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	state.fanRunning = false;
    state.firstWindow = true; 
    
    subscribe(outTemp, "temperature", "checkThings");
    subscribe(inTemp, "temperature", "checkThings");
    //subscribe(thermostat, "thermostatMode", "checkContacts");
    subscribe(contacts, "contact", "checkContacts");
}

def checkContacts(evt)
{
	def shouldRun = true;
    
    if(settings.checkContacts == 'true')
    {
    	def windowCount = 0;
        settings.contacts?.each {
        	if (it.currentContact == 'open')
            {
            	windowCount = windowCount + 1
 			}
        }
        if (windowCount < settings.numWindows)
        {
        	shouldRun = false
        }
        
        //Auto Thermostat Off Settings
        if (settings.autoThermoOff == 'true')
        {
            if (windowCount == 1)
            {
            	
                
                if ( state.firstWindow == true)
                {
                	log.warn "FIRST WINDOW OPEN"
                    state.lastThermo = settings.thermostat.currentValue("thermostatMode")
                    log.debug "Last Thermo Setting: $state.lastThermo"
                }
                log.debug 'Turnning Thermostat Off'
                settings.thermostat.off()
            }
            if (windowCount == 0)
            {
            	log.warn "LAST WINDOW CLOSED"
                log.debug "Setting Thermostat back to: $state.lastThermo" 
                if (state.lastThermo == 'cool')
                {
                    settings.thermostat.cool()
                }
                else if (state.lastThermo == 'range')
                {
                    settings.thermostat.range()
                }
                else if (state.lastThermo == 'heat')
                {
                    settings.thermostat.heat()
                }
                state.firstWindow == false
            }

        }

        //Indoor Fan Settings
        if (settings.indoorFan == 'true')
        {
            log.debug "Indoor Fan Settings"
            if (windowCount == 1)
            {
                if (state.firstWindow == true)
                {
                    state.lastFanSwitch = settings.indoorFans.currentValue('switch')
                    log.debug "Last Fan Switch: $state.lastFanSwitch"
                }
                if ( settings.indoorFanSpeedControl == 'true' )
                {
                    if (state.firstWindow == true)
                    {
                        state.lastFanSpeed = settings.indoorFans.currentValue('currentSpeed')
                        log.debug "Last Fan Speed: $state.lastFanSpeed"
                    }
                    log.debug "New Indoor Fan Setting: $settings.indoorFanSpeedSetting"
                    if ( settings.indoorFanSpeedSetting == 'LOW' )
                    {
                        settings.indoorFans.lowSpeed()
                    }
                    else if ( settings.indoorFanSpeedSetting == 'MEDIUM' )
                    {
                        settings.indoorFans.medSpeed()
                    }
                    else if ( settings.indoorFanSpeedSetting == 'HIGH' )
                    {
                        settings.indoorFans.highSpeed()
                    }

                }

                settings.indoorFans.on()

            }

            if (windowCount == 0)
            {
                if (state.lastFanSwitch == 'off')
                {
                    log.debug "Last Fan Switch: $state.lastFanSwitch"
                    settings.indoorFans.off()
                }
                else if ( settings.indoorFanSpeedControl == 'true')
                {
                    log.debug "Last Fan Speed: $state.lastFanSpeed"
                    if ( state.lastFanSpeed == 'LOW' )
                    {
                        settings.indoorFans.lowSpeed()
                    }
                    else if ( state.lastFanSpeed == 'MEDIUM' )
                    {
                        settings.indoorFans.medSpeed()
                    }
                    else if ( state.lastFanSpeed == 'HIGH' )
                    {
                        settings.indoorFans.highSpeed()
                    }

                }
                
                
            }
            
            

        }
        
        if (windowCount > 0)
        {
        	log.debug "Window Count: $windowCount - Setting first window to false"
        	state.firstWindow = false
        }
        else
        {
            state.firstWindow = true
        }
           
        
    }
    
    
    
    
	  checkThings(evt)
    
    
    
}


def checkThings(evt) {
	def outsideTemp = settings.outTemp.currentTemperature
    def insideTemp = settings.inTemp.currentTemperature
    def thermostatMode = settings.thermostat.currentThermostatMode
    //def somethingOpen = settings.checkContacts == 'No' || settings.contacts?.each { it.currentContact == 'open' }
    
    
    def shouldRun = true;
    
    if(settings.checkContacts == 'true')
    {
    	def windowCount = 0;
        settings.contacts?.each {
        	if (it.currentContact == 'open')
            {
            	windowCount = windowCount + 1
 			}
        }
        if (windowCount < settings.numWindows)
        {
        	shouldRun = false
        }
    }
    
    
    
    /*
    if(thermostatMode != 'off') {
    	log.debug "Not running due to thermostat mode"
        if(settings.autoThermoOff == 'true')
        {
        	log.debug 'going to turn thermostat off'
        }
        else
        {
    		shouldRun = false;
        }
    }
    */
    
    if(insideTemp < outsideTemp) {
    	log.debug "Not running due to insideTemp < outdoorTemp"
    	shouldRun = false;
    }
    
    if(insideTemp < settings.minTemp) {
    	log.debug "Not running due to insideTemp < minTemp"
    	shouldRun = false;
    }
    
    if(shouldRun && !state.fanRunning) {
    	fans.on();
        state.fanRunning = true;
    } else if(!shouldRun && state.fanRunning) {
    	fans.off();
        state.fanRunning = false;
    }
}