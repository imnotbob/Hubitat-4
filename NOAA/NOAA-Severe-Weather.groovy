/*  **************** NOAA Weather Alerts ****************
 *
 *  Hubitat Import URL: https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/NOAA/NOAA-Severe-Weather.groovy
 *
 *  Copyright 2019 Aaron Ward
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 * Last Update: 07/09/2021
 */

static String version() { return "4.0.016" }


import groovy.json.JsonSlurper
import groovy.transform.Field
//import groovy.json.*
//import java.util.regex.*
import java.text.SimpleDateFormat
//import java.text.ParseException
//import java.util.Date
//import groovy.time.*

definition(
	name:"NOAA Weather Alerts",
	namespace: "aaronward",
	author: "Aaron Ward",
	description: "NOAA Weather Alerts Application ",
	category: "Weather",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	documentationLink: "https://github.com/imnotbob/Hubitat-4/blob/master/NOAA/README.md",
	singleInstance: true,
	oauth: false,
	pausable: true)

preferences {
	page name: "mainPage", title: "", install: true, uninstall: false
	page name: "NotificationPage", title: "", install: false, uninstall: false, nextPage: "mainPage"
	page name: "ConfigPage", title: "", install: false, uninstall: false, nextPage: "mainPage"
	page name: "AdvConfigPage", title: "", install: false, uninstall: false, nextPage: "mainPage"
	page name: "RestrictionsPage", title: "", install: false, uninstall: false, nextPage: "mainPage"
	page name: "SettingsPage", title: "", install: false, uninstall: true, nextPage: "mainPage"

}

@SuppressWarnings('unused')
def mainPage() {
	dynamicPage(name: "mainPage") {
		installCheck()
		if((String)state.appInstalled == 'COMPLETE') {
			section(UIsupport("logo","")) {
				if((Boolean)pushovertts || (Boolean)musicmode || (Boolean)speechmode || (Boolean)echoSpeaks2) href(name: "NotificationPage", title: "${UIsupport("configured","")} Setup Notification Device(s)", required: false, page: "NotificationPage", description: "Select TTS and PushOver Devices")
				else href(name: "NotificationPage", title: "${UIsupport("attention","")} Setup Notification Device(s)", required: false, page: "NotificationPage", description: "Select TTS and PushOver Devices")

				if(whatAlertSeverity || whatPoll || alertCustomMsg) href(name: "ConfigPage", title: "${UIsupport("configured","")} Weather Alert Settings", required: false, page: "ConfigPage", description: "Change default settings for weather alerts to monitor")
				else  href(name: "ConfigPage", title: "${UIsupport("attention","")} Weather Alert Settings", required: false, page: "ConfigPage", description: "Change default settings for weather alerts to monitor")

				if(myWeatherAlert || whatAlertUrgency || whatAlertCertainty) href(name: "AdvConfigPage", title: "${UIsupport("configured","")} Advanced Alert Settings", required: false, page: "AdvConfigPage", description: "Add additional detailed weather settings to monitor")
				else href(name: "AdvConfigPage", title: "Advanced Alert Settings", required: false, page: "AdvConfigPage", description: "Add additional detailed weather settings to monitor")

				if((Boolean)modesYes || (Boolean)switchYes || (Boolean)modeSeverityYes || (Boolean)pushoverttsalways) href(name: "RestrictionsPage", title: "${UIsupport("configured","")} Restrictions", required: false, page: "RestrictionsPage", description: "Setup restriction options")
				else href(name: "RestrictionsPage", title: "Restrictions", required: false, page: "RestrictionsPage", description: "Setup restriction options")
				href(name: "SettingsPage", title: "Settings", required: false, page: "SettingsPage", description: "Modify NOAA Weather Alerts Application Settings, Logging, Test")
				paragraph UIsupport("line","")
				paragraph UIsupport("footer","")
			}
		}
	}
}

@SuppressWarnings('unused')
def NotificationPage() {
	buildEventsList()
	dynamicPage(name: "NotificationPage") {
		section(UIsupport("logo","")) {
			paragraph UIsupport("header", " Setup Notification Device(s)")
			paragraph "Select a communication(s) method, notification and restore volume levels, use a switch with advanced settings."
			// PushOver Devices
			input "pushovertts", "bool", title: "Use 'Pushover' device(s)?", required: false, defaultValue: false, submitOnChange: true
			if((Boolean)pushovertts){ input "pushoverdevice", "capability.notification", title: "PushOver Device", required: true, multiple: true}
			else app.removeSetting('pushoverdevice')

			// audioNotification Music Speakers (Sonos, etc)
			input(name: "musicmode", type: "bool", defaultValue: false, title: "Use audioNotification Speaker(s) for TTS?", description: "audioNotification Speaker(s)?", submitOnChange: true)
			if ((Boolean)musicmode) input "musicspeaker", "capability.audioNotification", title: "Choose audioNotification speaker(s)", required: false, multiple: true, submitOnChange: true
			else app.removeSetting('musicspeaker')

			// Speech Speakers
			input(name: "speechmode", type: "bool", defaultValue: false, title: "Use speechSynthesis Speaker(s) for TTS? (Google, Alexa TTS, etc)", description: "Speech Speaker(s)?", submitOnChange: true)
			if ((Boolean)speechmode) {
				input "speechspeaker", "capability.speechSynthesis", title: "Choose speechSynthesis speaker(s)", required: false, multiple: true, submitOnChange: true
				input(name: "speechdelay", type: "bool", defaultValue: false, title: "Place delays between commands to speechSynthesis Speaker(s)", description: "Speech Speaker(s)?", submitOnChange: true)
			} else {
				app.removeSetting('speechspeaker')
				app.removeSetting('speechdelay')
			}

			// Echo Speaks devices
			input (name: "echoSpeaks2", type: "bool", defaultValue: false, title: "Use Echo Speaks device(s) for Announcement TTS?", description: "Echo Speaks device?", submitOnChange: true)
			if((Boolean)echoSpeaks2) input "echospeaker", "capability.musicPlayer", title: "Choose Echo Speaks Device(s)", required: false, multiple: true, submitOnChange: true
			else app.removeSetting('echospeaker')

			// Master Volume settings
			if((Boolean)echoSpeaks2 || (Boolean)speechmode || (Boolean)musicmode) input "speakervolume", "number", title: "Notification Volume Level: (Leave blank to use current volume setting)", description: "0-100%", required: false, submitOnChange: true
			if((Boolean)echoSpeaks2 || (Boolean)speechmode) input "speakervolRestore", "number", title: "Restore Volume Level: (Leave blank to restore previous value)", description: "0-100", required: false, submitOnChange: true
			else app.removeSetting('speakervolRestore')

			if(!(Boolean)echoSpeaks2 && !(Boolean)speechmode && !(Boolean)musicmode) { app.removeSetting('speakervolRestore'); app.removeSetting('speakervolume') }

			// Switch to set when alert active
			input (name: "UsealertSwitch", type: "bool", title: "Use a switch to turn ON with Alert?", required: false, defaultValue: false, submitOnChange: true)
			if((Boolean)UsealertSwitch) {
				input (name: "alertSwitch", type: "capability.switch", title: "Select a switch to turn ON with Alert?", multiple: false, required: false, defaultValue: false, submitOnChange: true)
				input (name:"alertSwitchOff", type: "bool", title: "Turn off switch when all Alerts expire?", required: false, defaultValue: false, submitOnChange: true)
				input (name:"alertSwitchWeatherType", type: "bool", title: "Turn off switch if certain weather alert types expire?", required: false, defaultValue: false, submitOnChange: true)
				if((Boolean)alertSwitchWeatherType) input "alertSwitchWeatherTypeWatch", "enum", title: "Watch for a specific Weather event(s)?", required: false, multiple: true, submitOnChange: true, options: (List)state.eventTypes
			} else {
				app.removeSetting('alertSwitch')
				app.removeSetting('alertSwitchOff')
				app.removeSetting('alertSwitchWeatherType')
				app.removeSetting('alertSwitchWeatherTypeWatch')
			}
			// Disable Tile updates
			input (name: "disableTile", type: "bool", defaultValue: false, title: "Disable updates of Tile Device to display alerts?", description: "Disable tile device?", submitOnChange: true)
		}
	}
}

@SuppressWarnings('unused')
def ConfigPage() {
	dynamicPage(name: "ConfigPage") {
		section(UIsupport("logo","")) {
			paragraph UIsupport("header", " Alert Settings")
			paragraph "Configure NOAA to look for specific alert severities, how often to poll for weather information, repeat alerts, use custom coordinates and customize the alert message sent to notification device(s)."
			input name: "whatAlertSeverity", type: "enum", title: "Weather Severity(s) to gather in poll: ",
				options: [
					"unknown": "Unknown",
					"minor": "Minor",
					"moderate": "Moderate",
					"severe": "Severe",
					"extreme": "Extreme"], required: true, multiple: true, defaultValue: "severe"
			input name: "whatPoll", type: "enum", title: "Poll Frequency: ", options: ["1": "1 Minute", "5": "5 Minutes", "10": "10 Minutes", "15": "15 Minutes", "60": "60 Minutes"], required: true, multiple: false, defaultValue: "5"
			input "repeatYes", "bool", title: "Repeat Alert?", require: false, defaultValue: false, submitOnChange: true
			if((Boolean)repeatYes) {
				input name:"repeatTimes", type: "number", title: "Number of times to repeat the alert?", require: false, defaultValue: 1, submitOnChange:true
				input name:"repeatMinutes", type: "number", title: "Number of minutes between each repeating alert?", require: false, defaultValue: 15, submitOnChange:true
			}
			input name: "useCustomCords", type: "bool", title: "Use Custom Coordinates?", require: false, defaultValue: false, submitOnChange: true
			if(useCustomCords) {
				paragraph "Below coordinates are acquired from your Hubitat Hub. Enter your custom coordinates:"
				input name:"customlatitude", type:"text", title: "Latitude coordinate:", require: false, defaultValue: "${location.latitude}", submitOnChange: true
				input name:"customlongitude", type:"text", title: "Longitude coordinate:", require: false, defaultValue: "${location.longitude}", submitOnChange: true
			}
			input name:"useAlertIntro", type: "bool", title: "Use a pre-notification message for TTS device(s)?", require: false, defaultValue: false, submitOnChange: true
			if((Boolean)useAlertIntro) input name:"AlertIntro", type: "text", title: "Alert pre-notification message:", require: false, defaultValue:"Attention, Attention"
			input name: "alertCustomMsg", type: "text", title: "Custom Alert Message (use customization instructions):", require: false, defaultValue: "{alertseverity} Weather Alert for the following counties: {alertarea} {alertdescription} This is the end of this Weather Announcement.", submitOnChange: true
		}
		section("Alert Message Customization Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Alert message variables:</b>"
			paragraph "{alertseverity} = alertseverity"
			paragraph "{alertcertainty} = alert certainty of occuring"
			paragraph "{alerturgency} = alert urgency"
			paragraph "{alertevent} = alert event type"
			paragraph "{alertheadline} = alert headline"
			paragraph "{alertdescription} = alert description"
			paragraph "{alertinstruction} = alert instructions"
			paragraph "{alertarea} = counties, cities or area"
			paragraph " "
			paragraph "<b>Example:</b>{alertseverity} weather alert. Certainty is {alertcertainty}. Urgency is {alerturgency}. {alertheadline}. {alertinstruction}. This is the end of the weather announcement."
		}
	}
}

@SuppressWarnings('unused')
def AdvConfigPage() {
	buildEventsList()
	dynamicPage(name: "AdvConfigPage") {
		section(UIsupport("logo","")) {
			paragraph UIsupport("header", " Advanced Alert Settings")
			paragraph "Use with caution as below settings may cause undesired results. Only select what you would like to refine in your alerts. Reference <a href='https://www.weather.gov/documentation/services-web-api?prevfmt=application%2Fcap%2Bxml/default/get_alerts#/default/get_alerts' target='_blank'>Weather.gov API</a> and use the API response test button below to determine your desired results."
			input "myWeatherAlert", "enum", title: "Filter results for specific Weather event(s)?", required: false, multiple: true, submitOnChange: true, options: (List)state.eventTypes
			input name: "whatAlertUrgency", type: "enum", title: "Poll only for a specific Alert Urgency: ", multiple: true, submitOnChange: true,
				options: [
					"immediate": "Immediate",
					"expected": "Expected",
					"future": "Future"
				]

			input name: "whatAlertCertainty", type: "enum", title: "Poll only for specific Alert Certainty: ", required: false, multiple: true, submitOnChange: true,
				options: [
					"possible": "Possible",
					"likely": "Likely",
					"observed": "Observed"
				]
		}
	}
}

@SuppressWarnings('unused')
def RestrictionsPage() {
	buildEventsList()
	dynamicPage(name: "RestrictionsPage") {
		section(UIsupport("logo","")) {
			paragraph UIsupport("header", " Restrictions")
			paragraph "Restrict notifications based on modes or a switch. Override restrictions if the alert is a certain severity or weather type. For notifications that are restricted, if a PushOver device is enabled alerts can still be sent but not over TTS."
			input "modesYes", "bool", title: "Enable restriction of notifications by current mode(s)?", required: true, defaultValue: false, submitOnChange: true
			if((Boolean)modesYes) input(name:"modes", type: "mode", title: "Restrict notifications when current mode is:", multiple: true, required: false, submitOnChange: true)
			input "switchYes", "bool", title: "Restrict notifications using a switch?", required: true, defaultValue: false, submitOnChange: true
			if((Boolean)switchYes) input "restrictbySwitch", "capability.switch", title: "Use a switch to restrict notfications?", required: false, multiple: false, defaultValue: null, submitOnChange: true
			paragraph "<br>"
			paragraph UIsupport("header", " Overrides")
			paragraph "<br>Below settings will ignore restrictions above based on either weather severity type or weather type."
			input "modeSeverityYes", "bool", title: "Ignore restrictions for certain severity types?", required: false, defaultValue: false, submitOnChange: true
			if((Boolean)modeSeverityYes) input name: "modeSeverity", type: "enum", title: "Severity option(s) that will ignore restrictions: ",
				options: [
					"Minor": "Minor",
					"Moderate": "Moderate",
					"Severe": "Severe",
					"Extreme": "Extreme"], required: true, multiple: true, defaultValue: "Severe"

			input "modeWeatherType", "bool", title: "Ignore restrictions for certain weather types?", required: false, defaultValue: false, submitOnChange: true

			if((Boolean)modeWeatherType) input name: "WeatherType", type: "enum", title: "Select weather type to ignore restrictions: ", required: true, multiple:true, submitOnChange: true, options: (List)state.eventTypes
			paragraph "<hr>"
			if((Boolean)pushovertts) input "pushoverttsalways", "bool", title: "Enable Pushover notifications even when restricted?", required: false, defaultValue: false, submitOnChange: true
			else app.removeSetting('pushoverttsalways')
		}
	}
}

@SuppressWarnings('unused')
def SettingsPage() {
	dynamicPage(name: "SettingsPage") {
		section(UIsupport("logo","")) {
			paragraph UIsupport("header", " Settings")
			paragraph "Enable logging, run a test alert, if errors reset the applications state settings and test your weather alert configurations."
			input "logEnable", "bool", title: "Enable Debug Logging?", required: false, defaultValue: false, submitOnChange: true
			if((Boolean)logEnable) input "logMinutes", "number", title: "Log for the following number of minutes (0=logs always on):", range: "0..300", required: false, defaultValue:15, submitOnChange: true
			input "runTest", "bool", title: "Run a test Alert?", required: false, defaultValue: false, submitOnChange: true
			if(runTest) {
				app.updateSetting("runTest",[value:"false",type:"bool"])
				app.removeSetting('runTest')
				runtestAlert()
			}
			input "init", "bool", title: "Reset current application state?", required: false, defaultValue: false, submitOnChange: true
			if(init) {
				app.updateSetting("init",[value:"false",type:"bool"])
				app.removeSetting("init")
				unschedule()
				log.warn "NOAA Weather Alerts application state is being reset."
				initialize()
			}
			input "debug", "bool", title: "Debug alert configuration - if expired alerts are available, use those alerts? (only enable this with the test config option below)", required: false, defaultValue: false, submitOnChange: true
			input "getAPI", "bool", title: "Test alert configuration and display weather.gov API response?", required: false, defaultValue: false, submitOnChange: true
			if(getAPI) {
				getAlertMsgSync()
				app.updateSetting("getAPI",[value:"false",type:"bool"])
				app.removeSetting("getAPI")
				app.updateSetting("debug",[value:"false",type:"bool"])
				app.removeSetting("debug")

				String myId=app.getId()
				if(!ListofAlertsFLD[myId] && (List)state.ListofAlerts) ListofAlertsFLD[myId] = (List)state.ListofAlerts // on hub restart or code reload
				List<Map>mListofAlertsFLD = ListofAlertsFLD[myId]
				if(mListofAlertsFLD) {
					Boolean restrictionSwitch = ((Boolean)switchYes && restrictbySwitch != null && restrictbySwitch.currentState("switch").value == "on")
					Boolean restrictionMode = ((Boolean)modesYes && modes != null && modes.contains(location.mode))
					Boolean overrideRestSeverity = ((Boolean)modeSeverityYes && modeSeverity != null)
					Boolean overrideRestWeather = ((Boolean)modeWeatherType && WeatherType != null)
					//Boolean alertSwitchReset = ((Boolean)alertSwitchWeatherType && alertSwitchWeatherTypeWatch != null && alertSwitchWeatherTypeWatch.contains(mListofAlertsFLD[0].alertevent))
					//def testresult = (!(result || result2) || result3 || result4) ? true : false
					Date date = new Date()
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a")
					String testConfig = ""
					String customMsg = alertCustomMsg
					if((Boolean)useAlertIntro) customMsg = AlertIntro+', '+alertCustomMsg

					temp = "<hr><br>Current poll of Weather API: ${sdf.format(date)}<br/><br/>URI: <a href='${state.wxURI}' target=_blank>${state.wxURI}</a><br><br>AlertMSG Built based on configuration:<br><br>${customMsg}<br><br>"
					temp += "<table border=0><tr colspan=2><td>Current Restriction Settings:</td></tr>"
					temp += "<tr><td>Switch:</td><td>${restrictionSwitch ? "Active for ${restrictbySwitch}" : "Inactive"}</td></tr>"
					temp += "<tr><td>Mode:</td><td>${restrictionMode ? "Active for ${modes}" : "Inactive"}</td></tr>"
					temp += "<tr><td>Severity Overrides Restrictions:</td><td>${overrideRestSeverity ? "Enabled for ${modeSeverity}" : "Disabled"}</td></tr>"
					temp += "<tr><td>Weather Type Overrides Restrictions:</td><td>${overrideRestWeather ? "Enabled for ${WeatherType}" : "Disabled"}</td></tr></table></br>"
					paragraph temp
					for(y=0;y<mListofAlertsFLD.size();y++) {
						String testalertmsg
						overrideRestSeverity = ((Boolean)modeSeverityYes && modeSeverity != null && modeSeverity.contains(mListofAlertsFLD[y].alertseverity))
						overrideRestWeather = ((Boolean)modeWeatherType && WeatherType != null && WeatherType.contains(mListofAlertsFLD[y].alertevent))
						if(!restrictionSwitch && !restrictionMode && !overrideRestSeverity && !overrideRestWeather) {
						//if((!restrictionSwitch || !restrictionMode) && (!(Boolean)modeSeverityYes || !(Boolean)modeWeatherType)) {
							if((Boolean)pushovertts) testalertmsg = "alert would be announced on TTS and PushOver device(s)."
							else testalertmsg = "alert would be announced on TTS device(s)."
							testalertmsg += " No restricitons active."
						}else{
							if (overrideRestSeverity || overrideRestWeather) {
								if((Boolean)pushovertts) testalertmsg = "alert would be announced on TTS and PushOver device(s)"
								else testalertmsg = "alert would be announced only on TTS device(s)"
								testalertmsg += " - Restrictions override active."
							}else{
								if((Boolean)pushovertts && (Boolean)pushoverttsalways) testalertmsg = "alert would be announced only on PushOver device(s). Alert restricted with pushover always override."
								else testalertmsg = "alert would not be announced. Alert restricted."
							}
						}
						testConfig +="<table border=1px><tr><td colspan='2'>Alert ${y+1}/${mListofAlertsFLD.size()} - ${testalertmsg}</td></tr>"
						testConfig += "<tr><td>Field Name</td><td>Value</td></tr><tr><td>Severity</td><td>${mListofAlertsFLD[y].alertseverity}</td></tr>"
						testConfig += "<tr><td>Area</td><td>${mListofAlertsFLD[y].alertarea}</td></tr>"
						testConfig += "<tr><td>Sent</td><td>${mListofAlertsFLD[y].alertsent}</td></tr>"
						testConfig += "<tr><td>Effective</td><td>${mListofAlertsFLD[y].alerteffective}</td></tr>"
						testConfig += "<tr><td>Expires</td><td>${mListofAlertsFLD[y].alertexpires}</td></tr>"
						testConfig += "<tr><td>Status</td><td>${mListofAlertsFLD[y].alertstatus}</td></tr>"
						testConfig += "<tr><td>Message Type</td><td>${mListofAlertsFLD[y].alertmessagetype}</td></tr>"
						testConfig += "<tr><td>Category</td><td>${mListofAlertsFLD[y].alertcategory}</td></tr>"
						testConfig += "<tr><td>Certainty</td><td>${mListofAlertsFLD[y].alertcertainty}</td></tr>"
						testConfig += "<tr><td>Urgency</td><td>${mListofAlertsFLD[y].alerturgency}</td></tr>"
						testConfig += "<tr><td>Sender Name</td><td>${mListofAlertsFLD[y].alertsendername}</td></tr>"
						testConfig += "<tr><td>Event Type</td><td>${mListofAlertsFLD[y].alertevent}</td></tr>"
						testConfig += "<tr><td>Headline</td><td>${mListofAlertsFLD[y].alertheadline}</td></tr>"
						testConfig += "<tr><td>Description</td><td>${mListofAlertsFLD[y].alertdescription}</td></tr>"
						testConfig += "<tr><td>Instruction</td><td>${mListofAlertsFLD[y].alertinstruction}</td></tr></table>"
					}
					paragraph testConfig
				}
				else paragraph "There are no reported weather alerts in your area, the weather alerts available have expired, the api.weather.gov api is not available, or you need to change NOAA Weather Alert options to acquire desired results.<br><br>Current URI: <a href='${state.wxURI}' target=_blank>${state.wxURI}</a>"
			}
		}
	}
}

// Main Application Routines
def main() {
	// Get the alert message
	issueGetAlertMsg()
}

@SuppressWarnings('unused')
void callRefreshTile(){
	def noaaTile = getChildDevice("NOAA")
	if(noaaTile) noaaTile.refreshTile()
}

void alertNow(Integer y, String alertmsg, Boolean repeatCheck, Map msgMap=null){
	String myId=app.getId()
	if(!ListofAlertsFLD[myId] && (List)state.ListofAlerts) ListofAlertsFLD[myId] = (List)state.ListofAlerts // on hub restart or code reload
	List<Map> mListofAlertsFLD = ListofAlertsFLD[myId]

	// check restrictions based on Modes and Switches
	Boolean restrictionSwitch = ((Boolean)switchYes && restrictbySwitch != null && restrictbySwitch.currentState("switch").value == "on")
	Boolean restrictionMode = ((Boolean)modesYes && modes != null && modes.contains(location.mode))
	Boolean overrideRestSeverity = (y!=null && (Boolean)modeSeverityYes && modeSeverity != null && mListofAlertsFLD && modeSeverity.contains(mListofAlertsFLD[y]?.alertseverity))
	Boolean overrideRestWeather = (y!=null && (Boolean)modeWeatherType && WeatherType != null && mListofAlertsFLD && WeatherType.contains(mListofAlertsFLD[y]?.alertevent))
	if((Boolean)logEnable) log.debug "Restrictions on?  Modes: ${restrictionMode}, Switch: ${restrictionSwitch}, Severity Override: ${overrideRestSeverity}, Weather Type Override: ${overrideRestWeather}"

	Boolean alertWmatch = false
	if(alertmsg!=(String)null){
		// no restrictions
		if((Boolean)UsealertSwitch && alertSwitch && (Boolean)alertSwitchWeatherType && alertSwitchWeatherTypeWatch && mListofAlertsFLD && y!=null &&
				alertSwitchWeatherTypeWatch.contains(mListofAlertsFLD[y].alertevent) ) alertWmatch=true

		if(!restrictionSwitch && !restrictionMode && !overrideRestSeverity && !overrideRestWeather) {//(!(Boolean)modeSeverityYes && !(Boolean)modeWeatherType)) {
			if((Boolean)logEnable) log.info "Sending alert: ${alertmsg}"
			if (!msgMap || (msgMap && !(Boolean)msgMap.alertPushed)) pushNow(alertmsg, repeatCheck)
			if(msgMap) { msgMap.alertPushed=true; msgMap.alertAnnounced=true }
			if((Boolean)UsealertSwitch && alertSwitch) alertSwitch.on()
			if(alertWmatch) state.alertWeatherMatch = (String)mListofAlertsFLD[y].alertexpires
			talkNow(alertmsg, repeatCheck)
		}else{
			if(overrideRestSeverity || overrideRestWeather) {
				if((Boolean)logEnable) log.info "Sending alert (override active): ${alertmsg}"
				if (!msgMap || (msgMap && !(Boolean)msgMap.alertPushed)) pushNow(alertmsg, repeatCheck)
				if(msgMap) { msgMap.alertPushed=true; msgMap.alertAnnounced=true }
				if((Boolean)UsealertSwitch && alertSwitch) alertSwitch.on()
				if(alertWmatch) state.alertWeatherMatch = (String)mListofAlertsFLD[y].alertexpires
				talkNow(alertmsg, repeatCheck)
			}else{
				if((Boolean)pushoverttsalways) {
					if((Boolean)logEnable) log.info "Sending alert to pushover, Restrictions are enabled but PushoverTTS always override enabled: ${alertmsg}"
					if (!msgMap || (msgMap && !(Boolean)msgMap.alertPushed)) pushNow(alertmsg, repeatCheck)
					if(msgMap) { msgMap.alertPushed=true }
				}else if((Boolean)logEnable) log.info "Not sending alert, Restrictions are enabled."
			}
		}
	}
	walertCheck(alertmsg)
	if(alertWmatch && (String)state.alertWeatherMatch) {
		Date dt = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", (String)state.alertWeatherMatch)
		Long sec = (dt.getTime() - now()) / 1000
		if(sec > 0L) {
			runIn(sec, "walertCheck")
			log.info "Scheduling check in $sec seconds:"
		}
	}
}

void walertCheck(String alertmsg="a"){
	String myId=app.getId()
	if(!ListofAlertsFLD[myId] && (List)state.ListofAlerts) ListofAlertsFLD[myId] = (List)state.ListofAlerts // on hub restart or code reload
	List<Map> mListofAlertsFLD = ListofAlertsFLD[myId] ?: []

	Boolean alertSwitchReset = false
	if((String)state.alertWeatherMatch) {
		Boolean alertReset = true
		for(y=0;y<mListofAlertsFLD.size();y++) {
			if((Boolean)UsealertSwitch && alertSwitch && (Boolean)alertSwitchWeatherType && alertSwitchWeatherTypeWatch && mListofAlertsFLD && y!=null &&
				alertSwitchWeatherTypeWatch.contains(mListofAlertsFLD[y].alertevent) ) alertReset=false
		}
		if(alertReset) { state.alertWeatherMatch=(String)null; alertSwitchReset=true }
	}
	if((alertSwitchReset || alertmsg==(String)null) && (Boolean)UsealertSwitch && alertSwitch && ((Boolean)alertSwitchOff || alertSwitchReset)){
		if((Boolean)UsealertSwitch && alertSwitch && alertSwitch.currentState("switch").value == "on"){
			String amsg="turning off switch due to: "
			if(alertSwitchReset) amsg += "weather alert ended"
			else amsg+= "alerts ended"
			log.info amsg
			alertSwitch.off()
		}
	}
	if((alertSwitchReset || alertmsg==(String)null)){
		state.alertWeatherMatch=(String)null
		unschedule(walertCheck)
	}
}

void repeatNow(Boolean newmsg=false){
	Boolean doRefresh=true
	if((Boolean)repeatYes && (String)state.repeatmsg && repeatMinutes > 0) {
		if(!newmsg && state.rptCount >= state.rptNum){
			state.repeat = false
			state.repeatmsg = (String)null
			if((Boolean)logEnable) log.debug "Finished repeating alerts."
		}else{
			if(newmsg){
				state.rptCount = 0
				state.rptNum = repeatTimes!=null ? repeatTimes.toInteger() : 1
				state.repeat = true
				doRefresh=false
				if((Boolean)logEnable) log.debug "Starting repeating alerts."
			}else{
				if((Boolean)state.repeat) {
					if((Boolean)logEnable) log.debug "Sending repeat message"
					alertNow(0, (String)state.repeatmsg, true)
					//runIn(1,callRefreshTile)
				}
			}
			if((Boolean)state.repeat && (Integer)state.rptCount < (Integer)state.rptNum){
				state.rptCount = (Integer)state.rptCount + 1
				if((Boolean)logEnable) log.debug "Scheduling repeating alert in ${repeatMinutes} minute(s). This is ${state.rptCount}/${state.rptNum} repeated alert(s). Repeat State: ${state.repeat}"
				runIn(repeatMinutes.toInteger()*60,repeatNow)
			}
		}
	}else{
		if((Boolean)logEnable) log.debug "Repeat not enabled or no message Enabled: $repeatYes msg: $state.repeatmsg State: ${state.repeat} Minutes: ${repeatMinutes}"
		//match state
		state.repeat = false
		state.repeatmsg = (String)null
		//runIn(1,callRefreshTile)
	}
	if(doRefresh) runIn(1,callRefreshTile)
}

@Field volatile static Map<String,List> ListofAlertsFLD=[:]

void issueGetAlertMsg() {
	Map result = getResponseURL(true)
}

void getAlertMsgSync() {
	Map result = getResponseURL(false)
	finishAlertMsg(result)
	walertCheck()
}

void finishAlertMsg(Map result){
	List<Map> ListofAlerts = []
	List<Map> expireList = []

	String myId=app.getId()
	if(!ListofAlertsFLD[myId] && (List)state.ListofAlerts) ListofAlertsFLD[myId] = (List)state.ListofAlerts // on hub restart or code reload
	List<Map> mListofAlertsFLD = ListofAlertsFLD[myId] ?: []

	Boolean hadAlerts=false
	if(mListofAlertsFLD.size()>0) hadAlerts=true

	if(result) {
		Boolean IsnewList=false
		Date date = new Date()
		String timestamp = date.format("yyyy-MM-dd'T'HH:mm:ssXXX")
		Date dt1 = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", timestamp)

		for(i=0; i<result.features.size();i++) {
			Map msgMap=null
//			debug=true
//			alertmsg=[]
			String alertexpires

			Map feat=(Map)((List)result.features)[i]
			//alert expiration

			Boolean replacedAt=false
			Boolean useEnds=false
			if(feat.properties.replacedAt) {
				alertexpires = (String)feat.properties.replacedAt
				replacedAt=true
			} else if(feat.properties.ends) {
				alertexpires = (String)feat.properties.ends
				useEnds=true
			} else alertexpires = (String)feat.properties.expires

			//if specific weatheralerts is chosen
			String t0=settings.myWeatherAlert
			if(t0==(String)null || t0=="") msgMap = buildAlertMap(feat)
			else if(t0.contains((String)feat.properties.event)) msgMap = buildAlertMap(feat)

			Boolean expired=false
			//if alert has expired ignore alert
			Date dt = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", alertexpires)
			if(dt1.getTime() > dt.getTime()) { expired=true }
			if((Boolean)logEnable) log.debug "filtered: ${msgMap == null ? "true" : "false"} expired: ${expired}  alertexpires ${alertexpires}  replacedAt: $replacedAt useEnds: $useEnds    now: ${timestamp}  ${feat.properties.severity}  ${feat.properties.event}"

			if(msgMap!=null){
				if(!expired || settings.debug) {
					Boolean isNewNotice=false
					if(mListofAlertsFLD.size() > 0) {
						Map fndMsg = (Map)mListofAlertsFLD.find { ((String)it.alertid).contains((String)msgMap.alertid) }
						if(fndMsg) { msgMap = fndMsg }
						else isNewNotice = true
					} else {
						isNewNotice = true
						IsnewList = true
					}
					if((Boolean)logEnable){
						if(isNewNotice) log.debug "Valid ${msgMap.alertid} is new in ListofAlerts: ${IsnewList}"
						else log.debug "Valid ${msgMap.alertid} exists in ListofAlerts"
					}
					if(isNewNotice){ msgMap.alertPushed=false; msgMap.alertAnnounced = false }
					msgMap.expired=expired
					ListofAlerts << msgMap
				} else {
					msgMap.expired=expired
					msgMap.alertPushed=true
					msgMap.alertAnnounced = true
					expireList << msgMap
				}
			} // skipped the message due to filter
		} //end of for statement

//ERS
		mListofAlertsFLD = ListofAlerts
		state.ListofAlerts = ListofAlerts
		ListofAlertsFLD[myId] = mListofAlertsFLD
		ListofAlertsFLD = ListofAlertsFLD

		if(ListofAlerts) {
			if((Boolean)logEnable) log.debug "ListofAlerts is (${ListofAlerts.size()}) ${ListofAlerts}"
		} else { state.remove('ListofAlerts'); state.remove('alertAnnounced') }

		if(mListofAlertsFLD) {
			Boolean fixedRepeat=false
			Boolean schedTile=false
			for(y=0;y<mListofAlertsFLD.size();y++) {
				Map msgMap = (Map)mListofAlertsFLD[y]
				if(msgMap && !(Boolean)msgMap.expired) {
					if(!(Boolean)msgMap.alertAnnounced || !(Boolean)msgMap.alertPushed) {
						Boolean everDid = ((Boolean)msgMap.alertAnnounced || (Boolean)msgMap.alertPushed)
						alertNow(y, (String)msgMap.alertmsg, false, msgMap)
						//msgMap.alertPushed=true
						//msgMap.alertAnnounced=true
						mListofAlertsFLD[y] = msgMap
						ListofAlertsFLD[myId] = mListofAlertsFLD
						ListofAlertsFLD=ListofAlertsFLD
						state.ListofAlerts = mListofAlertsFLD
						if(!everDid && (Boolean)repeatYes && !fixedRepeat){
							fixedRepeat=true
							state.repeatmsg = (String)msgMap.alertmsg
							repeatNow(true)
						}
						schedTile=true
					}
					if((Boolean)repeatYes && !fixedRepeat){
						fixedRepeat=true
						if((String)state.repeatmsg != (String)msgMap.alertmsg) schedTile=true
						state.repeatmsg = (String)msgMap.alertmsg // in case messages moved around in order
					}
				}
			}
			if(schedTile) {
				runIn(1,callRefreshTile)
			}
		} else if((Boolean)logEnable) log.info "No new alerts.  Waiting ${whatPoll.toInteger()} minute(s) before next poll..."
	} else if((Boolean)logEnable) log.info "null result..."

	if(result!=null){ // deal with network outage; don't drop alerts.
		if(!ListofAlerts){
			if(hadAlerts && (Boolean)logEnable) log.debug "ending alerts"
			if((Boolean)UsealertSwitch && alertSwitch && alertSwitch.currentState("switch").value == "on") alertNow(null, (String)null, false) // maybe Switch.off()
			if((Boolean)state.repeat){
				unschedule(repeatNow)
				runIn(1,callRefreshTile)
			}
			state.repeat = false
			state.repeatmsg = (String)null
		}
	}
}

Map buildAlertMap(Map result) {
	String alertexpires
	//build new entry for map
	if(result.properties.replacedAt)alertexpires = (String)result.properties.replacedAt
	else if(result.properties.ends) alertexpires = (String)result.properties.ends
	else alertexpires = (String)result.properties.expires
	String alertarea
	alertarea = (String)result.properties.areaDesc
	alertarea = alertRemoveStates(alertarea)
	alertarea = alertFormatArea(alertarea)
	String alertheadline
	alertheadline = (String)result.properties.headline
	alertheadline = alertFormatStates(alertheadline)
	alertheadline = alertRemoveTimeZone(alertheadline)
	alertheadline = alertFormatText(alertheadline)
	String alertdescription
	alertdescription = (String)result.properties.description
	alertdescription = alertFormatStates(alertdescription)
	alertdescription = alertRemoveTimeZone(alertdescription)
	alertdescription = alertFormatText(alertdescription)
	String alertinstruction
	if(result.properties.instruction==null) alertinstruction = alertdescription
	else {
		alertinstruction = (String)result.properties.instruction
		alertinstruction = alertFormatStates(alertinstruction)
		alertinstruction = alertRemoveTimeZone(alertinstruction)
		alertinstruction = alertFormatText(alertinstruction)
	}
	String alertmsg
	alertmsg = alertCustomMsg
	try {alertmsg = alertmsg.replace("{alertarea}","${alertarea}") }
	  catch (ignored) {}
	try {alertmsg = alertmsg.replace("{alertseverity}","${result.properties.severity}") }
	  catch (ignored) {}
	try {alertmsg = alertmsg.replace("{alertcertainty}","${result.properties.certainty}") }
	  catch (ignored) {}
	try {alertmsg = alertmsg.replace("{alerturgency}","${result.properties.urgency}") }
	  catch (ignored) {}
	try {alertmsg = alertmsg.replace("{alertheadline}","${alertheadline}") }
	  catch (ignored) {}
	try {alertmsg = alertmsg.replace("{alertdescription}","${alertdescription}") }
	  catch (ignored) {}
	try {alertmsg = alertmsg.replace("{alertinstruction}","${alertinstruction}") }
	  catch (ignored) {}
	try {alertmsg = alertmsg.replace("{alertevent}","${result.properties.event}") }
	  catch (ignored) {}
	try {alertmsg = alertmsg.replaceAll("\n"," ") }
	  catch (ignored) {}
	try {alertmsg = alertmsg.trim().replaceAll("[ ]{2,}", ", ") }
	  catch (ignored) {}
	alertmsg = alertmsg.replaceAll("\\s+", " ")

	return [alertid:result.properties.id, alertseverity:result.properties.severity, alertarea:alertarea, alertsent:result.properties.sent, alerteffective:result.properties.effective, alertonset:result.properties.onset, alertexpires:alertexpires, alertstatus:result.properties.status, alertmessagetype:result.properties.messageType, alertcategory:result.properties.category, alertcertainty:result.properties.certainty, alerturgency:result.properties.urgency, alertsendername:result.properties.senderName, alertheadline:alertheadline, alertdescription:alertdescription, alertinstruction:alertinstruction, alertevent:result.properties.event, alertmsg:alertmsg]
}

static String alertFormatStates(String msg) {
	msg = msg.replaceAll("/AL/","Alabama")
	msg = msg.replaceAll("/AK/","Alaska")
	msg = msg.replaceAll("/AZ/","Arizona")
	msg = msg.replaceAll("/AR/","Arkansas")
	msg = msg.replaceAll("/CA/","California")
	msg = msg.replaceAll("/CO/","Colorado")
	msg = msg.replaceAll("/CT/","Connecticut")
	msg = msg.replaceAll("/DE/","Deleware")
	msg = msg.replaceAll("/FL/","Florida")
	msg = msg.replaceAll("/GA/","Georgia")
	msg = msg.replaceAll("/HI/","Hawaii")
	msg = msg.replaceAll("/ID/","Idaho")
	msg = msg.replaceAll("/IL/","Illinois")
	msg = msg.replaceAll("/IN/","Indiana")
	msg = msg.replaceAll("/IA/","Iowa")
	msg = msg.replaceAll("/KS/","Kansas")
	msg = msg.replaceAll("/KY/","Kentucky")
	msg = msg.replaceAll("/LA/","Louisiana")
	msg = msg.replaceAll("/ME/","Maine")
	msg = msg.replaceAll("/MA/","Massachusetts")
	msg = msg.replaceAll("/MD/","Maryland")
	msg = msg.replaceAll("/MI/","Michigan")
	msg = msg.replaceAll("/MN/","Minnesota")
	msg = msg.replaceAll("/MS/","Mississippi")
	msg = msg.replaceAll("/MO/","Missouri")
	msg = msg.replaceAll("/MT/","Montana")
	msg = msg.replaceAll("/NE/","Nebraska")
	msg = msg.replaceAll("/NV/","Nevada")
	msg = msg.replaceAll("/NH/","New Hampshire")
	msg = msg.replaceAll("/NJ/","New Jersey")
	msg = msg.replaceAll("/NM/","New Mexico")
	msg = msg.replaceAll("/NY/","New York")
	msg = msg.replaceAll("/NC/","North Carolina")
	msg = msg.replaceAll("/ND/","North Dakota")
	msg = msg.replaceAll("/OH/","Ohio")
	msg = msg.replaceAll("/OK/","Oklahoma")
	msg = msg.replaceAll("/OR/","Oregon")
	msg = msg.replaceAll("/PA/","Pennsylvania")
	msg = msg.replaceAll("/RI/","Rhode Island")
	msg = msg.replaceAll("/SC/","South Carolina")
	msg = msg.replaceAll("/SD/","South Dakota")
	msg = msg.replaceAll("/TN/","Tennessee")
	msg = msg.replaceAll("/TX/","Texas")
	msg = msg.replaceAll("/UT/","Utah")
	msg = msg.replaceAll("/VT/","Vermont")
	msg = msg.replaceAll("/VA/","Virginia")
	msg = msg.replaceAll("/WA/","Washington")
	msg = msg.replaceAll("/WV/","West Virginia")
	msg = msg.replaceAll("/WI/","Wisconsin")
	msg = msg.replaceAll("/WY/","Wyoming")
	return msg
}

static String alertRemoveTimeZone(String msg) {
	// Remove Timezones
	return msg.replaceAll(/(AST|EST|EDT|CST|CDT|MST|MDT|PST|PDT|AKST|AKDT|HST|HAST|HADT)/,"")
}

static String alertRemoveStates(String msg) {
	return msg.replaceAll(/(AL|AK|AZ|AR|CA|CO|CT|DE|FL|GA|HI|ID|IL|IA|IN|KS|KY|LA|ME|MA|MD|MI|MN|MS|MO|MT|NE|NV|NH|NJ|NM|NY|NC|ND|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI|WY)/, "")
}

static String alertFormatText(String msg) {
	msg = msg.replaceAll(/NWS/,"the National Weather Service of")
	msg = msg.replaceAll(/(WHAT|WHEN|IMPACTS|IMPACT|WHERE|INCLUDES|HAZARDS|INCLUDE|HAZARD|TEMPERATURE|SOURCE)/, "")
	msg = msg.replaceAll(/\.{2,}/, "")
	msg = msg.replaceAll(/\*/, "")
	msg = msg.replaceAll(/MPH/, "miles per hour")
	msg = msg.replaceAll("","")
	msg = msg.replaceAll("\n"," ")
	msg = msg.replaceAll("\\s+", " ")
	msg = msg.replaceAll(/(?:(\d{2})(\d{2}))|(?:(\d(?!\d{3}))(\d{2}))(?=\s?(?i:am|pm))/,'$1$3:$2$4')
	return msg
}

static String alertFormatArea(String msg) {
	msg.replaceAll(/NWS/,"the National Weather Service of")
	msg = msg.replaceAll(", ","")
	msg = msg.replaceAll(",","")
	msg = msg.replaceAll(";",",")
	msg = msg.replaceAll("\n"," ")
	msg = msg.replaceAll("\\s+", " ")
	msg = msg.replaceAll("/","")
	StringBuffer buffer = new StringBuffer(msg)
	msg = buffer.reverse().toString().replaceFirst(",","dna ")
	msg = new StringBuffer(msg).reverse().toString()
	msg = msg + "."
	return msg
}

//Test Alert Section
void runtestAlert() {
	atomicState.testmsg = true
	Integer endTime=30
	if((Boolean)logEnable) log.debug "Initiating a test alert."
	String msg=buildTestAlert()
	state.repeatmsg=msg
	alertNow(null, msg, false)
	if((Boolean)repeatYes){
		if((Boolean)logEnable) log.debug "Initiating a repeat process (ends in 5 minutes) for test alert."
		state.repeat=false
		repeatNow(true)
		endTime=300
	} else runIn(1,callRefreshTile)
	runIn(endTime,endTest)
}

@SuppressWarnings('unused')
void endTest(){
	if((Boolean)logEnable) log.debug "Ending repeat for test alert."
	atomicState.testmsg = false
	if((Boolean)UsealertSwitch && alertSwitch && alertSwitch.currentState("switch").value == "on") alertNow(null, (String)null, false) // maybe Switch.off()
	state.repeat = false
	state.repeatmsg = (String)null
	unschedule(repeatNow)
	runIn(1,callRefreshTile)
}

String buildTestAlert() {
	String alertmsg = alertCustomMsg
	try { alertmsg = alertmsg.replace("{alertarea}","Springfield County.") }
	catch (ignored) {}
	try { alertmsg = alertmsg.replace("{alertseverity}","Severe") }
	catch (ignored) {}
	try { alertmsg = alertmsg.replace("{alertcertainty}","Likely") }
	catch (ignored) {}
	try { alertmsg = alertmsg.replace("{alerturgency}","Immediate") }
	catch (ignored) {}
	try { alertmsg = alertmsg.replace("{alertheadline}","The founder, Jebediah Springfield has spotted a cloud above the nuclear power plant towers.") }
	//try { alertmsg = alertmsg.replace("{alertheadline}","The founder, has spotted a cloud.") }
	catch (ignored) {}
	try { alertmsg = alertmsg.replace("{alertdescription}","The founder, Jebediah Springfield has spotted a cloud above the nuclear power plant towers.  Expect heavy polution, possible fish with three eyes, and a Simpson asleep at the console.  Also a notorius yellow haired boy is terrorizing animals with spit wads.  Be on the look out for suspicious activity.") }
	//try { alertmsg = alertmsg.replace("{alertdescription}","The description.") }
	catch (ignored) {}
	try { alertmsg = alertmsg.replace("{alertinstruction}","Expect heavy polution, possible fish with three eyes, and a Simpson asleep at the console.") }
	catch (ignored) {}
	try { alertmsg = alertmsg.replace("{alertevent}","Nuclear Power Plant Warning") }
	catch (ignored) {}
	return alertmsg
}

// Common Notifcation Routines
void talkNow(String alertmsg, Boolean repeatCheck) {
	if(repeatCheck) {
		if((Boolean)useAlertIntro) alertmsg = "Repeating previous alert,, ${AlertIntro} " + alertmsg
		else alertmsg = "Repeating previous alert,," + alertmsg
	}else if((Boolean)useAlertIntro) alertmsg = "${AlertIntro}, " + alertmsg

	if((Boolean)musicmode) {
		if((Boolean)logEnable) log.debug "Using audioNotification $musicspeaker"
		Boolean okT = false
		if(musicspeaker && ((List)musicspeaker)[0].hasCommand('playTextAndRestore')) {
			try {
				musicspeaker*.playTextAndRestore(alertmsg.toLowerCase(), speakervolume)
				okT = true
				if((Boolean)logEnable) log.debug "Sending alert to audioNotification Speaker(s)."
			}
			catch (ignored) {}
		}
		if(!okT) { log.warn "audioNotificcation device(s) ${musicspeaker} has not been selected or does not support playTextAndRestore command." }
	}

	if((Boolean)echoSpeaks2) {
		if((Boolean)logEnable) log.debug "Using echoSpeaks $echospeaker"
		Boolean supportsSetVolume=false
		List<String> msgs = []
		List svVols = []
		Boolean canPlay = ((List)echospeaker)[0].hasCommand('playAnnouncement')
		if(speakervolume && canPlay && echospeaker && ((List)echospeaker)[0].hasCommand('setVolume')) {
			if(!speakervolRestore) {
				((List)echospeaker).each { dev ->
					def a = dev?.currentState('volume')?.value
					svVols.push(a)
				}
			}
			try {
				echospeaker*.setVolume(speakervolume)
				supportsSetVolume=true
				if((Boolean)logEnable) msgs.push("Setting Echo Speaker to volume level: ${speakervolume}".toString())
			} catch (ignored) { log.warn "unable to set volume" }
		}
		Boolean okT = false
		if(echospeaker && canPlay) {
			try {
				String tt = "serial"
				if(alertmsg.size() < 420) {
					if(echospeaker && ((List)echospeaker).size() > 1 && ((List)echospeaker)[0].hasCommand('parallelPlayAnnouncement')) {
						echospeaker*.parallelPlayAnnouncement(alertmsg.toLowerCase(), 'NOAA Weather Alert')
						tt = "parallel"
						echospeaker[0].noOp()
					} else echospeaker*.playAnnouncement(alertmsg.toLowerCase(), 'NOAA Weather Alert', null, null)
					tt += " announce"
				} else {
					tt += " speak"
					echospeaker*.speak(alertmsg)
				}

				okT = true
				if((Boolean)logEnable) msgs.push("Sending ${tt} alert to EchoSpeaks device(s).".toString())
				if(supportsSetVolume) {
					if(speakervolRestore) {
						echospeaker*.setVolume(speakervolRestore)
						if((Boolean)logEnable) msgs.push("Restoring Speaker to volume level: ${speakervolRestore}".toString())
					} else {
						Integer i = 0
						((List)echospeaker).each { dev ->
							def a = svVols[i]
							try {
								if((Boolean)logEnable) msgs.push("Restoring Speaker $dev to volume level: ${a}".toString())
								dev.setVolume(a)
							} catch (ignored) { if ((Boolean)logEnable) msgs.push("Echo ${i} does not support restore volume command".toString()) }
							i+=1
						}
					}
				}
			}
			catch (ignored) {}
		}
		if (msgs.size() > 0 && (Boolean)logEnable) msgs.each { String msg -> log.debug msg }
		if(!okT) { log.warn "echospeaks device(s) ${echospeaker} has not been selected or does not support playAnnouncement command." }
	}

	if((Boolean)speechmode) {
		List<String> msgs = []
		if((Boolean)logEnable) log.debug "Using speechSynthesis $speechspeaker, delays: ${speechdelay}"
		Boolean okT = false
		Boolean canSpeak = ((List)speechspeaker)[0].hasCommand('speak')
		if(canSpeak && speechspeaker && ((List)speechspeaker)[0].hasCommand('initialize')) {
			try {
				speechspeaker*.initialize()
				if((Boolean)logEnable) msgs.push("Initializing Speech Speaker")
				okT = true
				if((Boolean)speechdelay) pauseExecution(2500)
			}
			catch (ignored) { log.warn "initialize command failed" }
			if(!okT) { if((Boolean)logEnable) msgs.push("Speech device(s) ${speechspeaker} has not been selected or does not support initialize command.".toString()) }
		}

		Boolean supportsSetVolume=false
		List svVols = []
		if(canSpeak && speakervolume && speechspeaker && ((List)speechspeaker)[0].hasCommand('setVolume')) {
			if(!speakervolRestore) {
				((List)speechspeaker).each { dev ->
					def a = dev?.currentState('volume')?.value
					svVols.push(a)
				}
			}
			try {
				if((Boolean)logEnable) msgs.push("Setting Speech Speaker to volume level: ${speakervolume}".toString())
				speechspeaker*.setVolume(speakervolume)
				supportsSetVolume=true
				if((Boolean)speechdelay) pauseExecution(2000)
			}
			catch (ignored) { log.warn "unable to set volume" }

			if(!supportsSetVolume && speakervolume) { if((Boolean)logEnable) msgs.push("Speech device(s) ${speechspeaker} has not been selected or does not support setVolume command.".toString()) }
		}

		if(speechspeaker && canSpeak) {
			okT = false
			alertmsg = alertmsg.toLowerCase()
			try {
				String tt = "serial"
				if(speechspeaker && ((List)speechspeaker).size() > 1 && ((List)speechspeaker)[0].hasCommand('parallelSpeak') && alertmsg.size() < 420) {
					speechspeaker*.parallelSpeak(alertmsg)
					tt = "parallel"
					speechspeaker[0].noOp()
				} else speechspeaker*.speak(alertmsg)

				okT = true
				if((Boolean)logEnable) msgs.push("Sending ${tt} alert to Speech Speaker(s)".toString())

				if(supportsSetVolume) {
					Integer speechDuration = Math.max(Math.round(alertmsg.length()/14).toInteger(),2)+1
					Long speechDuration2 = speechDuration * 1000L
					if((Boolean)speechdelay) pauseExecution(speechDuration2)
					if(speakervolRestore) {
						speechspeaker*.setVolume(speakervolRestore)
						if((Boolean)logEnable) msgs.push("Restoring Speech Speaker to volume level: ${speakervolRestore}".toString())
					} else {
						Integer i = 0
						((List)speechspeaker).each { dev ->
							def a = svVols[i]
							try {
								dev.setVolume(a)
								if((Boolean)logEnable) msgs.push("Restoring Speech Speaker $dev to volume level: ${a}".toString())
							} catch (ignored) { if ((Boolean)logEnable) msgs.push("Speech speaker ${i} doesn't support restore volume command".toString()) }
							i+=1
						}
					}
					//if((Boolean)speechdelay) pauseExecution(1000)
				}
			} catch (ignored) {}
		}
		if (msgs.size() > 0 && (Boolean)logEnable) msgs.each { String msg -> log.debug msg }
		if(!okT) { log.warn "Speech device(s) ${speechspeaker} has not been selected or does not support speak command." }
	}
}

void pushNow(String alertmsg, Boolean repeatCheck) {
	if ((Boolean)pushovertts) {
		if((Boolean)logEnable) log.debug "Sending Pushover message."
		if(repeatCheck) {
			if(repeatTimes>1) alertmsg = "[Alert Repeat ${state.rptCount}/${state.rptNum}] " + alertmsg
			else alertmsg = "[Alert Repeat] " + alertmsg
		}

		List<String> fullalert = []
		//String m1 = alertmsg.replaceAll(/(\r\n|\r|\n|\\r\\n|\\r|\\n)+/, " ")
		String m1 = alertmsg.replaceAll(/\s\s+/, " ")
		List<String> subMsg = m1.tokenize()
		Integer lsiz = subMsg.size()
		Integer a=0
		Integer i=0
		while (i<lsiz){
			String nextpart = ""
			while (nextpart.size() < 1000 && i < lsiz) {
				nextpart += subMsg[i] + ' '
				i+=1
			}
			fullalert[a] = nextpart
			a+=1
		}

/*
		Integer asize=m1.length()
		Integer a=0
		Integer i=0
		while (i<asize){
			Integer end=(Math.min(asize-i, 1015))
			fullalert[a]=m1.substring(i,i+end)
			a=a+1
			i=i+end
		} */
/*		String m1 = alertmsg.replaceAll(/(\r\n|\r|\n|\\r\\n|\\r|\\n)+/, " ")
		def m = m1 =~ /(.|[\r\n]){1,1023}\W/
		while(m.find()) {
			fullalert << m.group()
		}*/

		for(x=0;x<fullalert.size();x++) {
			if(fullalert.size()>1) pushoverdevice.deviceNotification("(${x+1}/${fullalert.size()}) "+fullalert[x])
			else pushoverdevice.deviceNotification(fullalert[x])
			//pauseExecution(1000)
		}
	}
}


List getTile() {
	String myId=app.getId()
	if(!ListofAlertsFLD[myId] && (List)state.ListofAlerts) ListofAlertsFLD[myId] = (List)state.ListofAlerts // on hub restart or code reload
	List<Map> mListofAlertsFLD = ListofAlertsFLD[myId]
	List msg = []
	if(!(Boolean)settings.disableTile){
		if((Boolean)logEnable) log.info "Creating data information for tile display."
		try {
			if((Boolean)state.testmsg) {
				msg << [alertmsg:(String)state.repeatmsg]
			}else{
				if(mListofAlertsFLD) {
					for(x=0;x<mListofAlertsFLD.size();x++) {
						if(msg.toString().length() < 100000) {
							if(!(Boolean)mListofAlertsFLD[x].expired) msg << [alertmsg:mListofAlertsFLD[x].alertmsg]
						}
					}
				}
			}
		}
		catch (ignored) {}
	} else if(!msg && (Boolean)logEnable) log.debug "Tile display is disabled."
	return msg
}

void buildEventsListFrc() {
	buildEventsList(true)
}

@Field static final List<String> eventListFLD = [
"911 Telephone Outage Emergency", "Administrative Message", "Air Quality Alert", "Air Stagnation Advisory", "Arroyo And Small Stream Flood Advisory", "Ashfall Advisory", "Ashfall Warning", "Avalanche Advisory", "Avalanche Warning", "Avalanche Watch", "Beach Hazards Statement", "Blizzard Warning", "Blizzard Watch", "Blowing Dust Advisory", "Blowing Dust Warning", "Brisk Wind Advisory", "Child Abduction Emergency", "Civil Danger Warning", "Civil Emergency Message", "Coastal Flood Advisory", "Coastal Flood Statement", "Coastal Flood Warning", "Coastal Flood Watch", "Dense Fog Advisory", "Dense Smoke Advisory", "Dust Advisory", "Dust Storm Warning", "Earthquake Warning", "Evacuation - Immediate", "Excessive Heat Warning", "Excessive Heat Watch", "Extreme Cold Warning", "Extreme Cold Watch", "Extreme Fire Danger", "Extreme Wind Warning", "Fire Warning", 
"Fire Weather Watch", "Flash Flood Statement", "Flash Flood Warning", "Flash Flood Watch", "Flood Advisory", "Flood Statement", "Flood Warning", "Flood Watch", "Freeze Warning", "Freeze Watch", "Freezing Fog Advisory", "Freezing Rain Advisory", "Freezing Spray Advisory", "Frost Advisory", "Gale Warning", "Gale Watch", "Hard Freeze Warning", "Hard Freeze Watch", "Hazardous Materials Warning", "Hazardous Seas Warning", "Hazardous Seas Watch", "Hazardous Weather Outlook", "Heat Advisory", "Heavy Freezing Spray Warning", "Heavy Freezing Spray Watch", "High Surf Advisory", "High Surf Warning", "High Wind Warning", "High Wind Watch", "Hurricane Force Wind Warning", "Hurricane Force Wind Watch", "Hurricane Local Statement", "Hurricane Warning", "Hurricane Watch", "Hydrologic Advisory", "Hydrologic Outlook", "Ice Storm Warning", "Lake Effect Snow Advisory", "Lake Effect Snow Warning", "Lake Effect Snow Watch", "Lake Wind Advisory", "Lakeshore Flood Advisory", "Lakeshore Flood Statement", "Lakeshore Flood Warning", 
"Lakeshore Flood Watch", "Law Enforcement Warning", "Local Area Emergency", "Low Water Advisory", "Marine Weather Statement", "Nuclear Power Plant Warning", "Radiological Hazard Warning", "Red Flag Warning", "Rip Current Statement", "Severe Thunderstorm Warning", "Severe Thunderstorm Watch", "Severe Weather Statement", "Shelter In Place Warning", "Short Term Forecast", "Small Craft Advisory", "Small Craft Advisory For Hazardous Seas", "Small Craft Advisory For Rough Bar", "Small Craft Advisory For Winds", "Small Stream Flood Advisory", "Snow Squall Warning", "Special Marine Warning", "Special Weather Statement", "Storm Surge Warning", "Storm Surge Watch", "Storm Warning", "Storm Watch", "Test", "Tornado Warning", "Tornado Watch", "Tropical Depression Local Statement", "Tropical Storm Local Statement", "Tropical Storm Warning", 
"Tropical Storm Watch", "Tsunami Advisory", "Tsunami Warning", "Tsunami Watch", "Typhoon Local Statement", "Typhoon Warning", "Typhoon Watch", "Urban And Small Stream Flood Advisory", "Volcano Warning", "Wind Advisory", "Wind Chill Advisory", "Wind Chill Warning", "Wind Chill Watch", "Winter Storm Warning", "Winter Storm Watch", "Winter Weather Advisory"
]

void buildEventsList(Boolean frc=false) {
	if(frc || (List)state.eventTypes==null || (List)state.eventTypes==[]) {
		Map results = getResponseEvents()
		if(results) {
			state.eventTypes = (List)results.eventTypes
			if((Boolean)logEnable) log.debug "Acquired current events list from api.weather.gov"
		}
	}
	if((List)state.eventTypes==null || (List)state.eventTypes==[]) {
		state.eventTypes = eventListFLD
	}
}

// Device creation and status updhandlers
void createChildDevices() {
	try {
		if (!getChildDevice("NOAA")) {
			if ((Boolean)logEnable) log.info "Creating device: NOAA Tile"
			addChildDevice("aaronward", "NOAA Tile", "NOAA", 1234, ["name": "NOAA Tile", isComponent: false])
		}
	}
	catch (e) { log.error "Couldn't create child device. ${e}" }
}

void cleanupChildDevices() {
	try {
		for(device in getChildDevices()) deleteChildDevice(device.deviceNetworkId)
	}
	catch (ignored) { log.error "Couldn't clean up child devices." }
}

// Application Support Routines
Map getResponseURL(Boolean async=false) {
	// Determine if custom coordinates have been selected
	String latitude
	String longitude
	if(useCustomCords) {
		latitude = "${customlatitude}".toString()
		longitude = "${customlongitude}".toString()
	}else{
		latitude = "${location.latitude}".toString()
		longitude = "${location.longitude}".toString()
	}

	String wxURI = "https://api.weather.gov/alerts?point=${latitude}%2C${longitude}&status=actual&message_type=alert,update".toString()
	Map result = null

	// Build out the API options
	if(whatAlertUrgency != null) wxURI = wxURI + "&urgency=${whatAlertUrgency.join(",")}".toString()

	if(whatAlertSeverity != null) wxURI = wxURI + "&severity=${whatAlertSeverity.join(",")}".toString()
	else wxURI = wxURI + "&severity=severe"

	if(whatAlertCertainty !=null) wxURI = wxURI + "&certainty=${whatAlertCertainty.join(",")}".toString()

	state.wxURI = wxURI
	if((Boolean)logEnable) log.debug "URI: <a href='${wxURI}' target=_blank>${wxURI}</a>"


	if((Boolean)logEnable) log.debug "Connecting to weather.gov service."
	Map requestParams =	[
		uri: wxURI,
		requestContentType: "application/json",
		contentType: "application/json",
		timeout: 20
	]

	if(!async){
		try {
			httpGet(requestParams)	{ response ->
				result = response.data
				Integer responseCode=response.status
				if(responseCode>=200 && responseCode<300 && resp.data){
				} else { log.warn "The API Weather.gov did not return a response for ${wxURI}." }
			}
		}
		catch (e) { if((Boolean)logEnable) log.warn "The API Weather.gov did not return a response for ${wxURI}. $e" }
		return result
	} else {
		try {
			asynchttpGet('ahttpreq', requestParams, [command: 'a'])
			return [async:true]
		}
		catch (e) { log.warn "Async http failed for ${wxURI}. $e" }
		return result
	}
}

@SuppressWarnings('unused')
void ahttpreq(resp, Map cbD){
	Boolean ok=false
	def data
	try {
		//def t0=resp.getHeaders()
		Integer responseCode=resp.status
		if(responseCode>=200 && responseCode<300 && resp.data){
			data=resp.data
			if(data!=null && !(data instanceof Map)){
				try{
					data=(LinkedHashMap) new JsonSlurper().parseText((String)data)
				}catch (ignored){
					data=resp.data
				}
			}
            ok=true

		} else log.warn "The API Weather.gov did not return a response."
	} catch(e) {
		log.warn "The API Weather.gov did not return a response. (exception), $e"
	}
	if(ok) finishAlertMsg(data)
	walertCheck()
}

Map getResponseEvents() {
	String wxURI = "https://api.weather.gov/alerts/types"
	Map result = null
	Map requestParams =	[
		uri: wxURI,
		requestContentType: "application/json",
		contentType: "application/json",
		timeout: 20
	]

	try {
		httpGet(requestParams) { response ->
			result = response.data
			Integer responseCode=response.status
			if(responseCode>=200 && responseCode<300 && result){
			} else { log.warn "The API Weather.gov get types did not return a response for ${wxURI}." }
		}
	}
	catch (e) {
		log.warn "The API Weather.gov get types did not return a response for ${wxURI}. $e"
	}
	return result
}

void checkState() {
	atomicState.testmsg = false
	if(whatPoll==null) app.updateSetting("whatPoll",[value:"5",type:"enum"])
	if(logEnable==null) app.updateSetting("logEnable",[value:"false",type:"bool"])
	if(logMinutes==null) app.updateSetting("logMinutes",[value:15,type:"number"])
	if(whatAlertSeverity==null) app.updateSetting("whatAlertSeverity",[value:"severe",type:"enum"])
	if(alertCustomMsg==null) app.updateSetting("whatCustomMsg",[value:"{alertseverity} Weather Alert for the following counties: {alertarea} {alertdescription} This is the end of this Weather Announcement.",type:"text"])

	Integer t0
	if(repeatTimes==null){
		app.updateSetting("repeatTimes",[value:1,type:"number"])
		t0=1
	}else t0=repeatTimes.toInteger()
	state.rptNum = t0

	if(repeatMinutes==null){
		app.updateSetting("repeatMinutes",[value:15,type:"number"])
	}

	state.rptCount = 0
	if(!(Boolean)state.repeat) state.repeatmsg = (String)null
}

void installCheck(){
	state.appInstalled = app.getInstallationState()
	if((String)state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install ${app.label} "}
	}
}

void initialize() {
	buildEventsListFrc()
	checkState()
	unschedule()
	createChildDevices()
	state.repeat = false
	state.repeatmsg = (String)null
	String myId=app.getId()
	ListofAlertsFLD[myId] = []
	ListofAlertsFLD = ListofAlertsFLD
	state.ListofAlerts = []
	if((Boolean)UsealertSwitch && alertSwitch && alertSwitch.currentState("switch").value == "on") alertNow(null, (String)null, false) // maybe Switch.off()
	log.warn "NOAA Weather Alerts application state is reset."

	Integer myPoll=5
	if(whatPoll!=null)myPoll=whatPoll.toInteger()
	switch(myPoll) {
		case 1:
			runEvery1Minute(main)
			break
		case 10:
			runEvery10Minutes(main)
			break
		case 15:
			runEvery15Minutes(main)
			break
		case 60:
			runEvery1Hour(main)
			break
		case 5:
		//	runEvery5Minutes(main)
		//	break
		default:
			runEvery5Minutes(main)
			break
	}
	def random=new Random()
	Integer random_int=random.nextInt(60)
	Integer random_dint=random.nextInt(9)
	schedule("${random_int} ${random_dint} 01 ? * *", buildEventsListFrc) // once a day 1:00 AM

	main()

	runIn(1,callRefreshTile)
	if((Boolean)logEnable){
		Integer myLog=15
		if(logMinutes!=null)myLog=logMinutes.toInteger()
		if(myLog!=0){
			log.info "Debug messages set to automatically disable in ${myLog} minute(s)."
			runIn((myLog*60),logsOff)
		}else log.info "Debug logs set to not automatically disable."
	}else log.info "Debug logs disabled."
}

@SuppressWarnings('unused')
void installed() {
	if((Boolean)logEnable) log.debug "Installed with settings: ${settings}"
	initialize()
}

@SuppressWarnings('unused')
void updated() {
	if((Boolean)logEnable) log.debug "Updated with settings: ${settings}"
	state.remove('num')
	state.remove('speechDuration2')
	state.remove('frequency')
	state.remove('count')
	initialize()
}

@SuppressWarnings('unused')
void uninstalled() {
	cleanupChildDevices()
}


static String UIsupport(String type, String txt) {
	switch(type) {
		case "logo":
			return "<table border=0><thead><tr><th><img border=0 style='max-width:100px' src='https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/NOAA/Support/NOAA.png'></th><th style='padding:10px' align=left><font style='font-size:34px;color:#1A77C9;font-weight: bold'>NOAA Weather Alerts</font><br><font style='font-size:14px;font-weight: none'>This application provides customized Weather Alert announcements.</font></tr></thead></table><br><hr style='margin-top:-15px;background-color:#1A77C9; height: 1px; border: 0;'></hr>"
			break
		case "line":
			return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
			break
		case "header":
			return "<div style='color:#ffffff;font-weight: bold;background-color:#1A7BC7;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${txt}</div>"
			break
		case "footer":
			return "<div style='color:#1A77C9;text-align:center'>App/Driver v${version()}<br>Originally Developed by: Aaron Ward<br></div>"
			break
		case "configured":
			return "<img border=0 style='max-width:15px' src='https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/support/images/Checked.svg'>"
			break
		case "attention":
			return "<img border=0 style='max-width:15px' src='https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/support/images/Attention.svg'>"
			break
	}
	return (String)null
}

@SuppressWarnings('unused')
void logsOff(){
	log.info "Debug logging disabled."
	app.updateSetting("logEnable",[value:"false",type:"bool"])
}
