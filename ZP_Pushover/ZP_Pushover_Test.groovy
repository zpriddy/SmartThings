/**
* 
*	File: ZP_Pushover_Test.groovy
*	Last Modified: 2016-01-04 22:29:24
*
*  Zachary Priddy
*  https://zpriddy.com
*  me@zpriddy.com
*
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
*
*/

definition(
    name: "ZP Pushover Test",
    namespace: "zpriddy",
    author: "zpriddy",
    description: "Test Pushover Notification",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Kids/kids8-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Kids/kids8-icn@2x.png"
)

preferences {
  section("Pushover Device") {
    input ("pushoverDevice", "capability.notification")
    input("testMessageText", "text", title: "Test Message Text", description: "Test Message Text", defaultValue: "This is a test message from SmartThings!")
    input("testMessagePriority", "enum", title: "Test Message Priority", options: ["Low", "Normal", "High", "Emergency"], defaultValue:"Normal")
  }
}

def installed() {
  subscribe(app, appTouchHandler)
}

def updated() {
  unsubscribe()
  subscribe(app, appTouchHandler)
}

def appTouchHandler(evt) {
  pushoverDevice.sendMessage("$testMessageText", "$testMessagePriority")
}
