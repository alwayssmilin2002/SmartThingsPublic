/**
 *  Test Vent
 *
 *  Copyright 2020 Patrick Lonberger
 *
 */
definition(
    name: "Test Vent",
    namespace: "alwayssmilin2002",
    author: "Patrick Lonberger",
    description: "Test",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

/**
 *  Garage Vent Fans with Disable
 *
 *  Copyright 2015 Bruce Ravenel
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


preferences {
	section("Select Thermometers") {
		input "insideTemp", "capability.temperatureMeasurement", title: "Indside thermometer?", required: true
		input "outsideTemp", "capability.temperatureMeasurement", title: "Outside thermometer?", required: true
	}
	section("Select Vent Fan Switch") {
    		input "ventSwitch", "capability.switch", title: "Which vent switch?", required: true
	}
	section("Set Temperatures") {
    		input "baseTemp", "decimal", title: "Minimum temperature required?", required: true, defaultValue: 55
    		input "differential", "decimal", title: "How many degrees differential?", required: true, defaultValue: 2
	}
	section("Select Disable Switch") {
		input "disabled", "capability.switch", title: "Which switch to disable", required: true, multiple: false
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
	subscribe(insideTemp, "temperature", tempHandler)
	subscribe(outsideTemp, "temperature", tempHandler)
	subscribe(disabled, "switch", disableHandler)
	ventSwitch.off()
	state.ventOff = true
	state.disabled = (disabled) ? disabled.currentSwitch == "on" : false
	evalTemps()
}

def evalTemps() {
	def inTemp = insideTemp.currentTemperature
	def outTemp = outsideTemp.currentTemperature
	log.debug "evalTemps in: $inTemp out: $outTemp"
	if((inTemp > outTemp + differential) && (inTemp > baseTemp) && state.ventOff) {
		ventSwitch.on()
		state.ventOff = false
	}
	if((inTemp <= outTemp || inTemp < baseTemp) && !state.ventOff) {
		ventSwitch.off()
		state.ventOff = true
	}
}

def disableHandler(evt) {
	state.disabled = evt.value == "on"
}

def tempHandler(evt) {
	log.debug "tempHandler: $evt.displayName $evt.value, disabled: $state.disabled"
	if(state.disabled) return
	evalTemps()
}