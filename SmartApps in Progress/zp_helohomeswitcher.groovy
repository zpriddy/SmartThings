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
 *  Big Turn OFF
 *
 *  Author: SmartThings
 */
definition(
    name: "HelloHome Selection Switch",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "When a helloHome switch is turned on.. All others turn off",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("Select HelloHome Indacator Switches") {
		input "switches", "capability.switch", multiple: true
	}
}

def installed()
{
	subscribe(switches, "switch.on", switchHandler)
}

def updated()
{
	unsubscribe()
	subscribe(switches, "switch.on", switchHandler)
}


def switchHandler(evt) {
	log.debug "changedLocationMode: $evt.displayName"
	switches.each{
		if(it.displayName == evt.displayName)
		{
			log.trace "$it.displayName - ON"
		}
		else
		{
			log.trace "$it.displayName - OFF"
			it.off()
		}
	}
}


