/**
* 
* File: ZP_Sunrise.groovy
* Last Modified: 2016-01-05 19:59:28
*
*  Zachary Priddy
*  https://zpriddy.com
*  me@zpriddy.com
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
  name: "ZP Sunrise",
  namespace: "zpriddy",
  author: "zpriddy",
  description: "Changes mode at a specific time of day.",
  category: "Mode Magic",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences {
  section("At this time every day") {
    input "timeOfDay", "time", title: "Time of Day"
    input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    input "sunriseTime", "number", title: "How many minutes do you want the sunrise to last?", required: true, defaultValue: 15
  }
  section("On mode changes") {
    input "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
  }

  section("Change These Lights") {
    input "sunLights", "capability.colorControl", title: "What lights do you want to be the Sun? (Whites)", required:true, multiple:true
    input "sunriseLights", "capability.colorControl", title: "What lights do you want to be the sunrise lights? (Yellows)", required:true, multiple:true
    input "skyLights", "capability.colorControl", title: "What lights do you want to be the sky lights? (Blues)", required:true, multiple:true
  }
  section( "Notifications" ) {
    input("recipients", "contact", title: "Send notifications to") {
      input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
      input "phoneNumber", "phone", title: "Send a text message?", required: false
    }
  }
}

def installed() {
  initialize()
}

def updated() {
  unschedule()
  initialize()
}

def initialize() {
  subscribe(app, appTouchHandler)

  if (triggerModes) {
    subscribe(location, modeChangeHandler)
  }

  if (timeOfDay) {
    schedule(timeOfDay, sunriseHandler)
  }

  state.currentStage = 0
}

def sunriseHandler(evt) {
  if(daysOk) {
    log.trace "Days Okay"
    sunrise()
  } 
}

def appTouchHandler(evt) {
  log.trace "App Touched"
  sunrise()
}

def modeChangeHandler(evt) {
  log.trace "modeChangeHandler $evt.name: $evt.value ($triggerModes)"
  if (evt.value in triggerModes) {
    sunrise()
  }
}

def sunrise() {
  int numberOfStages = 4
  numberOfStages = numberOfStages - 1 
  int sunriseSeconds = sunriseTime * 60
  int stage1Seconds = 2
  int sunriseRemainder = sunriseSeconds - stage1Seconds
  int stage2Seconds = sunriseRemainder / numberOfStages
  int stage3Seconds = sunriseRemainder / numberOfStages
  int stage4Seconds = sunriseRemainder - stage2Seconds - stage3Seconds

  log.trace "Sunrise Length: $sunriseSeconds"
  log.trace "Stage 1: $stage1Seconds Seconds"
  log.trace "Stage 2: $stage2Seconds Seconds"
  log.trace "Stage 3: $stage3Seconds Seconds"
  log.trace "Stage 4: $stage4Seconds Seconds"

  state.stage1Seconds = stage1Seconds
  state.stage2Seconds = stage2Seconds
  state.stage3Seconds = stage3Seconds
  state.stage4Seconds = stage4Seconds
  state.numberOfStages = numberOfStages

  log.debug getColor(1,"sunLights")

  state.currentStage = 1 

  setLights()

}

def setLights() {
  def currentStage = state.currentStage

  log.debug "Current Stage: $currentStage"

  if(currentStage <= 2) {
    //Only set the sun lights in stage one
    sunLights.each {
      hue -> 
      hue.setColor(getColor(currentStage,"sunLights"))
      pause(100)
    }
  }

  skyLights.each {
    hue ->
    hue.setColor(getColor(currentStage,"skyLights"))
    pause(100)
  }

  sunriseLights.each {
    hue ->
    hue.setColor(getColor(currentStage,"sunriseLights"))
    pause(100)
  }

  state.currentStage = currentStage + 1

  if(state.currentStage <= state.numberOfStages + 1) {
    runIn(getStageSeconds(currentStage),setLights)
  }

}

int getStageSeconds(stage) {
  if(stage == 1) {
    return state.stage1Seconds
  }
  else if (stage == 2) {
    return state.stage2Seconds
  }
  else if (stage == 3) {
    return state.stage3Seconds
  }
  else if (stage == 4) {
    return state.stage4Seconds
  }
}

def getColor(stage, group) {
  if(group == "sunLights") {
    if(stage == 1) {
      int sunSeconds = state.stage2Seconds * (state.numberOfStages - 1)
      return [hue: 53, saturation: 91, level: 1, transitiontime: getStageSeconds(stage)]
    }
    if(stage == 2) {
      int sunSeconds = state.stage2Seconds * (state.numberOfStages - 1)
      return [hue: 53, saturation: 91, level: 100, transitiontime: sunSeconds]
    }
  }

  if(group == "skyLights") {
    if(stage == 1) {
      return [hue: 90, saturation: 100, level: 1, transitiontime: getStageSeconds(stage)]
    }
    if(stage == 2) {
      return [hue: 80, saturation: 99, level: 31, transitiontime: getStageSeconds(stage)]
    }
    if(stage == 3) {
      return [hue: 72, saturation: 85, level: 66, transitiontime: getStageSeconds(stage)]
    }
    if(stage == 4) {
      return [hue: 66, saturation: 58, level: 100, transitiontime: getStageSeconds(stage)]
    }
  }

  if(group == "sunriseLights") {
    if(stage == 1) {
      return [hue: 87, saturation: 100, level: 1, transitiontime: getStageSeconds(stage)]
    }
    if(stage == 2) {
      return [hue: 3, saturation: 99, level: 31, transitiontime: getStageSeconds(stage)]
    }
    if(stage == 3) {
      return [hue: 15, saturation: 55, level: 66, transitiontime: getStageSeconds(stage)]
    }
    if(stage == 4) {
      return [hue: 53, saturation: 58, level: 100, transitiontime: getStageSeconds(stage)]
    }
  }

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