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
 * Last Update: 10/02/2021
 */
//file:noinspection GroovySillyAssignment
//file:noinspection unused


import groovy.json.JsonSlurper
import groovy.transform.Field
//import groovy.json.*
//import java.util.regex.*
import java.text.SimpleDateFormat
//import java.text.ParseException
//import java.util.Date
//import groovy.time.*


@Field static final String appVersionFLD  = '4.0.017'
@Field static final String sNULL          = (String)null
@Field static final String sBLANK         = ''
@Field static final String sSPACE         = ' '
@Field static final String sCLRORG        = 'orange'
@Field static final String sCLRGRY        = 'gray'
@Field static final String sLINEBR        = '<br>'
@Field static final String sCLRRED        = 'red'
@Field static final String sFALSE         = 'false'
@Field static final String sTRUE          = 'true'
@Field static final String sBOOL          = 'bool'
@Field static final String sENUM          = 'enum'
@Field static final String sNUMBER        = 'number'
@Field static final String sSMALL         = 'small'
@Field static final String sDEBUG         = 'debug'

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
	page name: "mainPage", title: sBLANK, install: true, uninstall: false
	page name: "NotificationPage", title: sBLANK, install: false, uninstall: false, nextPage: "mainPage"
	page name: "ConfigPage", title: sBLANK, install: false, uninstall: false, nextPage: "mainPage"
	page name: "AdvConfigPage", title: sBLANK, install: false, uninstall: false, nextPage: "mainPage"
	page name: "RestrictionsPage", title: sBLANK, install: false, uninstall: false, nextPage: "mainPage"
	page name: "SettingsPage", title: sBLANK, install: false, uninstall: true, nextPage: "mainPage"

}

def mainPage() {
	if(settings.logInfo == null) app.updateSetting("logInfo", [value:sTRUE, type:sBOOL])
	if(settings.logWarn == null) app.updateSetting("logWarn", [value:sTRUE, type:sBOOL])
	if(settings.logError == null) app.updateSetting("logError", [value:sTRUE, type:sBOOL])

	dynamicPage(name: "mainPage") {
		installCheck()
		if((String)state.appInstalled == 'COMPLETE') {
			section(UIsupport("logo")) {
				if((Boolean)settings.pushovertts || (Boolean)settings.musicmode || (Boolean)settings.speechmode || (Boolean)settings.echoSpeaks2) href(name: "NotificationPage", title: "${UIsupport("configured")} Setup Notification Device(s)", required: false, page: "NotificationPage", description: "Select TTS and PushOver Devices")
				else href(name: "NotificationPage", title: "${UIsupport("attention")} Setup Notification Device(s)", required: false, page: "NotificationPage", description: "Select TTS and PushOver Devices")

				if(settings.whatAlertSeverity || settings.whatPoll || settings.alertCustomMsg) href(name: "ConfigPage", title: "${UIsupport("configured")} Weather Alert Settings", required: false, page: "ConfigPage", description: "Change default settings for weather alerts to monitor")
				else  href(name: "ConfigPage", title: "${UIsupport("attention")} Weather Alert Settings", required: false, page: "ConfigPage", description: "Change default settings for weather alerts to monitor")

				if(settings.myWeatherAlert || settings.whatAlertUrgency || settings.whatAlertCertainty) href(name: "AdvConfigPage", title: "${UIsupport("configured")} Advanced Alert Settings", required: false, page: "AdvConfigPage", description: "Add additional detailed weather settings to monitor")
				else href(name: "AdvConfigPage", title: "Advanced Alert Settings", required: false, page: "AdvConfigPage", description: "Add additional detailed weather settings to monitor")

				if((Boolean)settings.modesYes || (Boolean)settings.switchYes || (Boolean)settings.modeSeverityYes || (Boolean)settings.pushoverttsalways) href(name: "RestrictionsPage", title: "${UIsupport("configured")} Restrictions", required: false, page: "RestrictionsPage", description: "Setup restriction options")
				else href(name: "RestrictionsPage", title: "Restrictions", required: false, page: "RestrictionsPage", description: "Setup restriction options")
				href(name: "SettingsPage", title: "Settings", required: false, page: "SettingsPage", description: "Modify NOAA Weather Alerts Application Settings, Logging, Test")
				paragraph UIsupport("line")
				paragraph UIsupport("footer")
			}
		}
	}
}

def NotificationPage() {
	buildEventsList()
	dynamicPage(name: "NotificationPage") {
		section(UIsupport("logo")) {
			paragraph UIsupport("header", " Setup Notification Device(s)")
			paragraph "Select a communication(s) method, notification and restore volume levels, use a switch with advanced settings."
			// PushOver Devices
			input "pushovertts", sBOOL, title: "Use 'Pushover' device(s)?", required: false, defaultValue: false, submitOnChange: true
			if((Boolean)settings.pushovertts){ input "pushoverdevice", "capability.notification", title: "PushOver Device", required: true, multiple: true}
			else app.removeSetting('pushoverdevice')

			// audioNotification Music Speakers (Sonos, etc)
			input(name: "musicmode", type: sBOOL, defaultValue: false, title: "Use audioNotification Speaker(s) for TTS?", description: "audioNotification Speaker(s)?", submitOnChange: true)
			if ((Boolean)settings.musicmode) input "musicspeaker", "capability.audioNotification", title: "Choose audioNotification speaker(s)", required: false, multiple: true, submitOnChange: true
			else app.removeSetting('musicspeaker')

			// Speech Speakers
			input(name: "speechmode", type: sBOOL, defaultValue: false, title: "Use speechSynthesis Speaker(s) for TTS? (Google, Alexa TTS, etc)", description: "Speech Speaker(s)?", submitOnChange: true)
			if ((Boolean)settings.speechmode) {
				input "speechspeaker", "capability.speechSynthesis", title: "Choose speechSynthesis speaker(s)", required: false, multiple: true, submitOnChange: true
				input(name: "speechdelay", type: sBOOL, defaultValue: false, title: "Place delays between commands to speechSynthesis Speaker(s)", description: "Speech Speaker(s)?", submitOnChange: true)
			} else {
				app.removeSetting('speechspeaker')
				app.removeSetting('speechdelay')
			}

			// Echo Speaks devices
			input (name: "echoSpeaks2", type: sBOOL, defaultValue: false, title: "Use Echo Speaks device(s) for Announcement TTS?", description: "Echo Speaks device?", submitOnChange: true)
			if((Boolean)settings.echoSpeaks2) input "echospeaker", "capability.musicPlayer", title: "Choose Echo Speaks Device(s)", required: false, multiple: true, submitOnChange: true
			else app.removeSetting('echospeaker')

			// Master Volume settings
			if((Boolean)settings.echoSpeaks2 || (Boolean)settings.speechmode || (Boolean)settings.musicmode) input "speakervolume", sNUMBER, title: "Notification Volume Level: (Leave blank to use current volume setting)", description: "0-100%", required: false, submitOnChange: true
			if((Boolean)settings.echoSpeaks2 || (Boolean)settings.speechmode) input "speakervolRestore", sNUMBER, title: "Restore Volume Level: (Leave blank to restore previous value)", description: "0-100", required: false, submitOnChange: true
			else app.removeSetting('speakervolRestore')

			if(!(Boolean)settings.echoSpeaks2 && !(Boolean)settings.speechmode && !(Boolean)settings.musicmode) { app.removeSetting('speakervolRestore'); app.removeSetting('speakervolume') }

			// Switch to set when alert active
			input (name: "UsealertSwitch", type: sBOOL, title: "Use a switch to turn ON with Alert?", required: false, defaultValue: false, submitOnChange: true)
			if((Boolean)settings.UsealertSwitch) {
				input (name: "alertSwitch", type: "capability.switch", title: "Select a switch to turn ON with Alert?", multiple: false, required: false, defaultValue: false, submitOnChange: true)
				input (name:"alertSwitchOff", type: sBOOL, title: "Turn off switch when all Alerts expire?", required: false, defaultValue: false, submitOnChange: true)
				input (name:"alertSwitchWeatherType", type: sBOOL, title: "Turn off switch if certain weather alert types expire?", required: false, defaultValue: false, submitOnChange: true)
				if((Boolean)settings.alertSwitchWeatherType) input "alertSwitchWeatherTypeWatch", sENUM, title: "Watch for a specific Weather event(s)?", required: false, multiple: true, submitOnChange: true, options: (List)state.eventTypes
			} else {
				app.removeSetting('alertSwitch')
				app.removeSetting('alertSwitchOff')
				app.removeSetting('alertSwitchWeatherType')
				app.removeSetting('alertSwitchWeatherTypeWatch')
			}
			// Disable Tile updates
			input (name: "disableTile", type: sBOOL, defaultValue: false, title: "Disable updates of Tile Device to display alerts?", description: "Disable tile device?", submitOnChange: true)
		}
	}
}

def ConfigPage() {
	dynamicPage(name: "ConfigPage") {
		section(UIsupport("logo")) {
			paragraph UIsupport("header", " Alert Settings")
			paragraph "Configure NOAA to look for specific alert severities, how often to poll for weather information, repeat alerts, use custom coordinates and customize the alert message sent to notification device(s)."
			input name: "whatAlertSeverity", type: sENUM, title: "Weather Severity(s) to gather in poll: ",
				options: [
					"unknown": "Unknown",
					"minor": "Minor",
					"moderate": "Moderate",
					"severe": "Severe",
					"extreme": "Extreme"], required: true, multiple: true, defaultValue: "severe"
			input name:"capitalizeAlertSeverity", type: sBOOL, title: "Capitalize Severity in API calls (NOAA bug)?", require: false, defaultValue: false, submitOnChange: true
			input name: "whatPoll", type: sENUM, title: "Poll Frequency: ", options: ["1": "1 Minute", "5": "5 Minutes", "10": "10 Minutes", "15": "15 Minutes", "60": "60 Minutes"], required: true, multiple: false, defaultValue: "5"
			input "repeatYes", sBOOL, title: "Repeat Alert?", require: false, defaultValue: false, submitOnChange: true
			if((Boolean)settings.repeatYes) {
				input name:"repeatTimes", type: sNUMBER, title: "Number of times to repeat the alert?", require: false, defaultValue: 1, submitOnChange:true
				input name:"repeatMinutes", type: sNUMBER, title: "Number of minutes between each repeating alert?", require: false, defaultValue: 15, submitOnChange:true
			}
			input name: "useCustomCords", type: sBOOL, title: "Use Custom Coordinates?", require: false, defaultValue: false, submitOnChange: true
			if((Boolean)settings.useCustomCords) {
				paragraph "Below coordinates are acquired from your Hubitat Hub. Enter your custom coordinates:"
				input name:"customlatitude", type:"text", title: "Latitude coordinate:", require: false, defaultValue: "${location.latitude}", submitOnChange: true
				input name:"customlongitude", type:"text", title: "Longitude coordinate:", require: false, defaultValue: "${location.longitude}", submitOnChange: true
			}
			input name:"useAlertIntro", type: sBOOL, title: "Use a pre-notification message for TTS device(s)?", require: false, defaultValue: false, submitOnChange: true
			if((Boolean)settings.useAlertIntro) input name:"AlertIntro", type: "text", title: "Alert pre-notification message:", require: false, defaultValue:"Attention, Attention"
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

def AdvConfigPage() {
	buildEventsList()
	dynamicPage(name: "AdvConfigPage") {
		section(UIsupport("logo")) {
			paragraph UIsupport("header", " Advanced Alert Settings")
			paragraph "Use with caution as below settings may cause undesired results. Only select what you would like to refine in your alerts. Reference <a href='https://www.weather.gov/documentation/services-web-api?prevfmt=application%2Fcap%2Bxml/default/get_alerts#/default/get_alerts' target='_blank'>Weather.gov API</a> and use the API response test button below to determine your desired results."
			input "myWeatherAlert", sENUM, title: "Filter results for specific Weather event(s)?", required: false, multiple: true, submitOnChange: true, options: (List)state.eventTypes
			input name: "whatAlertUrgency", type: sENUM, title: "Poll only for a specific Alert Urgency: ", multiple: true, submitOnChange: true,
				options: [
					"unknown": "Unknown",
					//"past": "Past",
					"immediate": "Immediate",
					"expected": "Expected",
					"future": "Future"
				]

			input name: "whatAlertCertainty", type: sENUM, title: "Poll only for specific Alert Certainty: ", required: false, multiple: true, submitOnChange: true,
				options: [
					"unknown": "Unknown",
					//"unlikely": "Unlikely",
					"possible": "Possible",
					"likely": "Likely",
					"observed": "Observed"
				]
		}
	}
}

def RestrictionsPage() {
	buildEventsList()
	dynamicPage(name: "RestrictionsPage") {
		section(UIsupport("logo")) {
			paragraph UIsupport("header", " Restrictions")
			paragraph "Restrict notifications based on modes or a switch. Override restrictions if the alert is a certain severity or weather type. For notifications that are restricted, if a PushOver device is enabled alerts can still be sent but not over TTS."
			input "modesYes", sBOOL, title: "Enable restriction of notifications by current mode(s)?", required: true, defaultValue: false, submitOnChange: true
			if((Boolean)settings.modesYes) input(name:"modes", type: "mode", title: "Restrict notifications when current mode is:", multiple: true, required: false, submitOnChange: true)
			input "switchYes", sBOOL, title: "Restrict notifications using a switch?", required: true, defaultValue: false, submitOnChange: true
			if((Boolean)settings.switchYes) input "restrictbySwitch", "capability.switch", title: "Use a switch to restrict notfications?", required: false, multiple: false, defaultValue: null, submitOnChange: true
			paragraph "<br>"
			paragraph UIsupport("header", " Overrides")
			paragraph "<br>Below settings will ignore restrictions above based on either weather severity type or weather type."
			input "modeSeverityYes", sBOOL, title: "Ignore restrictions for certain severity types?", required: false, defaultValue: false, submitOnChange: true
			if((Boolean)settings.modeSeverityYes) input name: "modeSeverity", type: sENUM, title: "Severity option(s) that will ignore restrictions: ",
				options: [
					"Minor": "Minor",
					"Moderate": "Moderate",
					"Severe": "Severe",
					"Extreme": "Extreme"], required: true, multiple: true, defaultValue: "Severe"

			input "modeWeatherType", sBOOL, title: "Ignore restrictions for certain weather types?", required: false, defaultValue: false, submitOnChange: true

			if((Boolean)settings.modeWeatherType) input name: "WeatherType", type: sENUM, title: "Select weather type to ignore restrictions: ", required: true, multiple:true, submitOnChange: true, options: (List)state.eventTypes
			paragraph "<hr>"
			if((Boolean)settings.pushovertts) input "pushoverttsalways", sBOOL, title: "Enable Pushover notifications even when restricted?", required: false, defaultValue: false, submitOnChange: true
			else app.removeSetting('pushoverttsalways')
		}
	}
}

def SettingsPage() {
	dynamicPage(name: "SettingsPage") {
		section(UIsupport("logo")) {
			paragraph UIsupport("header", " Settings")
			paragraph "Enable logging, run a test alert, if errors reset the applications state settings and test your weather alert configurations."
			input "logInfo", sBOOL, title: inTS1("Show Info Logs?", sDEBUG), required: false, defaultValue: true, submitOnChange: true
			input "logWarn", sBOOL, title: inTS1("Show Warning Logs?", sDEBUG), required: false, defaultValue: true, submitOnChange: true
			input "logError", sBOOL, title: inTS1("Show Error Logs?", sDEBUG), required: false, defaultValue: true, submitOnChange: true
			input "logDebug", sBOOL, title: inTS1("Show Debug Logs?", sDEBUG), description: "Auto disables after 6 hours", required: false, defaultValue: false, submitOnChange: true
			input "logTrace", sBOOL, title: inTS1("Show Detailed Logs?", sDEBUG), description: "Only enabled when asked to.\n(Auto disables after 6 hours)", required: false, defaultValue: false, submitOnChange: true

//			input "logEnable", sBOOL, title: "Enable Debug Logging?", required: false, defaultValue: false, submitOnChange: true
//			if((Boolean)settings.logEnable) input "logMinutes", sNUMBER, title: "Log for the following number of minutes (0=logs always on):", range: "0..300", required: false, defaultValue:15, submitOnChange: true
			if((Boolean)settings.logDebug || (Boolean)settings.logTrace || (Boolean)settings.logInfo) input "logMinutes", sNUMBER, title: "Log for the following number of minutes (0=logs always on):", range: "0..300", required: false, defaultValue:15, submitOnChange: true
			input "runTest", sBOOL, title: "Run a test Alert?", required: false, defaultValue: false, submitOnChange: true
			if((Boolean)settings.runTest) {
				app.updateSetting("runTest",[value:sFALSE,type:sBOOL])
				app.removeSetting('runTest')
				runtestAlert()
			}
			input "init", sBOOL, title: "Reset current application state?", required: false, defaultValue: false, submitOnChange: true
			if((Boolean)settings.init) {
				app.updateSetting("init",[value:sFALSE,type:sBOOL])
				app.removeSetting("init")
				unschedule()
				logWarn "NOAA Weather Alerts application state is being reset."
				initialize()
			}
			input "debug", sBOOL, title: "Debug alert configuration - if expired alerts are available, use those alerts? (only enable this with the test config option below)", required: false, defaultValue: false, submitOnChange: true
			input "getAPI", sBOOL, title: "Test alert configuration and display weather.gov API response?", required: false, defaultValue: false, submitOnChange: true
			if((Boolean)settings.getAPI) {
				getAlertMsgSync()
				app.updateSetting("getAPI",[value:sFALSE,type:sBOOL])
				app.removeSetting("getAPI")
				app.updateSetting("debug",[value:sFALSE,type:sBOOL])
				app.removeSetting("debug")

				String myId=app.getId()
				if(!ListofAlertsFLD[myId] && (List)state.ListofAlerts) ListofAlertsFLD[myId] = (List)state.ListofAlerts // on hub restart or code reload
				List<Map>mListofAlertsFLD = ListofAlertsFLD[myId]
				if(mListofAlertsFLD) {
					Boolean restrictionSwitch = ((Boolean)settings.switchYes && settings.restrictbySwitch != null && settings.restrictbySwitch.currentState("switch").value == "on")
					Boolean restrictionMode = ((Boolean)settings.modesYes && settings.modes != null && settings.modes.contains(location.mode))
					Boolean overrideRestSeverity = ((Boolean)settings.modeSeverityYes && settings.modeSeverity != null)
					Boolean overrideRestWeather = ((Boolean)settings.modeWeatherType && settings.WeatherType != null)
					//Boolean alertSwitchReset = ((Boolean)settings.alertSwitchWeatherType && (List)settings.alertSwitchWeatherTypeWatch && ((List)settings.alertSwitchWeatherTypeWatch).contains(mListofAlertsFLD[0].alertevent))
					//def testresult = (!(result || result2) || result3 || result4) ? true : false
					Date date = new Date()
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a")
					String testConfig = sBLANK
					String customMsg = (String)settings.alertCustomMsg
					if((Boolean)settings.useAlertIntro) customMsg = (String)settings.AlertIntro+', '+(String)settings.alertCustomMsg

					temp = "<hr><br>Current poll of Weather API: ${sdf.format(date)}<br/><br/>URI: <a href='${state.wxURI}' target=_blank>${state.wxURI}</a><br><br>AlertMSG Built based on configuration:<br><br>${customMsg}<br><br>"
					temp += "<table border=0><tr colspan=2><td>Current Restriction Settings:</td></tr>"
					temp += "<tr><td>Switch:</td><td>${restrictionSwitch ? "Active for ${settings.restrictbySwitch}" : "Inactive"}</td></tr>"
					temp += "<tr><td>Mode:</td><td>${restrictionMode ? "Active for ${settings.modes}" : "Inactive"}</td></tr>"
					temp += "<tr><td>Severity Overrides Restrictions:</td><td>${overrideRestSeverity ? "Enabled for ${settings.modeSeverity}" : "Disabled"}</td></tr>"
					temp += "<tr><td>Weather Type Overrides Restrictions:</td><td>${overrideRestWeather ? "Enabled for ${settings.WeatherType}" : "Disabled"}</td></tr></table></br>"
					paragraph temp
					for(y=0;y<mListofAlertsFLD.size();y++) {
						String testalertmsg
						overrideRestSeverity = ((Boolean)settings.modeSeverityYes && settings.modeSeverity != null && ((List)settings.modeSeverity).contains(mListofAlertsFLD[y].alertseverity))
						overrideRestWeather = ((Boolean)settings.modeWeatherType && settings.WeatherType != null && settings.WeatherType.contains(mListofAlertsFLD[y].alertevent))
						if(!restrictionSwitch && !restrictionMode && !overrideRestSeverity && !overrideRestWeather) {
						//if((!restrictionSwitch || !restrictionMode) && (!(Boolean)settings.modeSeverityYes || !(Boolean)settings.modeWeatherType)) {
							if((Boolean)settings.pushovertts) testalertmsg = "alert would be announced on TTS and PushOver device(s)."
							else testalertmsg = "alert would be announced on TTS device(s)."
							testalertmsg += " No restricitons active."
						}else{
							if (overrideRestSeverity || overrideRestWeather) {
								if((Boolean)settings.pushovertts) testalertmsg = "alert would be announced on TTS and PushOver device(s)"
								else testalertmsg = "alert would be announced only on TTS device(s)"
								testalertmsg += " - Restrictions override active."
							}else{
								if((Boolean)settings.pushovertts && (Boolean)settings.pushoverttsalways) testalertmsg = "alert would be announced only on PushOver device(s). Alert restricted with pushover always override."
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

void callRefreshTile(){
	def noaaTile = getChildDevice("NOAA")
	if(noaaTile) noaaTile.refreshTile()
}

void alertNow(Integer y, String alertmsg, Boolean repeatCheck, Map msgMap=null){
	String myId=app.getId()
	if(!ListofAlertsFLD[myId] && (List)state.ListofAlerts) ListofAlertsFLD[myId] = (List)state.ListofAlerts // on hub restart or code reload
	List<Map> mListofAlertsFLD = ListofAlertsFLD[myId]

	// check restrictions based on Modes and Switches
	Boolean restrictionSwitch = ((Boolean)settings.switchYes && settings.restrictbySwitch != null && settings.restrictbySwitch.currentState("switch").value == "on")
	Boolean restrictionMode = ((Boolean)settings.modesYes && settings.modes != null && settings.modes.contains(location.mode))
	Boolean overrideRestSeverity = (y!=null && (Boolean)settings.modeSeverityYes && settings.modeSeverity != null && mListofAlertsFLD && ((List)settings.modeSeverity).contains(mListofAlertsFLD[y]?.alertseverity))
	Boolean overrideRestWeather = (y!=null && (Boolean)settings.modeWeatherType && settings.WeatherType != null && mListofAlertsFLD && settings.WeatherType.contains(mListofAlertsFLD[y]?.alertevent))
	logDebug "Restrictions on?  Modes: ${restrictionMode}, Switch: ${restrictionSwitch}, Severity Override: ${overrideRestSeverity}, Weather Type Override: ${overrideRestWeather}"

	Boolean alertWmatch = false
	if(alertmsg!=sNULL){
		// no restrictions
		if((Boolean)settings.UsealertSwitch && settings.alertSwitch && (Boolean)settings.alertSwitchWeatherType && (List)settings.alertSwitchWeatherTypeWatch && mListofAlertsFLD && y!=null &&
				((List)settings.alertSwitchWeatherTypeWatch).contains(mListofAlertsFLD[y].alertevent) ) alertWmatch=true

		if(!restrictionSwitch && !restrictionMode && !overrideRestSeverity && !overrideRestWeather) {//(!(Boolean)settings.modeSeverityYes && !(Boolean)settings.modeWeatherType)) {
			logInfo "Sending alert: ${alertmsg}"
			if (!msgMap || (msgMap && !(Boolean)msgMap.alertPushed)) pushNow(alertmsg, repeatCheck)
			if(msgMap) { msgMap.alertPushed=true; msgMap.alertAnnounced=true }
			if((Boolean)settings.UsealertSwitch && settings.alertSwitch) settings.alertSwitch.on()
			if(alertWmatch) state.alertWeatherMatch = (String)mListofAlertsFLD[y].alertexpires
			talkNow(alertmsg, repeatCheck)
		}else{
			if(overrideRestSeverity || overrideRestWeather) {
				logInfo "Sending alert (override active): ${alertmsg}"
				if (!msgMap || (msgMap && !(Boolean)msgMap.alertPushed)) pushNow(alertmsg, repeatCheck)
				if(msgMap) { msgMap.alertPushed=true; msgMap.alertAnnounced=true }
				if((Boolean)settings.UsealertSwitch && settings.alertSwitch) settings.alertSwitch.on()
				if(alertWmatch) state.alertWeatherMatch = (String)mListofAlertsFLD[y].alertexpires
				talkNow(alertmsg, repeatCheck)
			}else{
				if((Boolean)settings.pushoverttsalways) {
					logInfo "Sending alert to pushover, Restrictions are enabled but PushoverTTS always override enabled: ${alertmsg}"
					if (!msgMap || (msgMap && !(Boolean)msgMap.alertPushed)) pushNow(alertmsg, repeatCheck)
					if(msgMap) { msgMap.alertPushed=true }
				}else logDebug "Not sending alert, Restrictions are enabled."
			}
		}
	}
	walertCheck(alertmsg)
	if(alertWmatch && (String)state.alertWeatherMatch) {
		Date dt = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", (String)state.alertWeatherMatch)
		Long sec = (dt.getTime() - now()) / 1000
		if(sec > 0L) {
			runIn(sec, "walertCheck")
			logDebug "Scheduling check in $sec seconds:"
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
			if((Boolean)settings.UsealertSwitch && settings.alertSwitch && (Boolean)settings.alertSwitchWeatherType && (List)settings.alertSwitchWeatherTypeWatch && mListofAlertsFLD && y!=null &&
					((List)settings.alertSwitchWeatherTypeWatch).contains(mListofAlertsFLD[y].alertevent) ) alertReset=false
		}
		if(alertReset) { state.alertWeatherMatch=sNULL; alertSwitchReset=true }
	}
	if((alertSwitchReset || alertmsg==sNULL) && (Boolean)settings.UsealertSwitch && settings.alertSwitch && ((Boolean)settings.alertSwitchOff || alertSwitchReset)){
		if((Boolean)settings.UsealertSwitch && settings.alertSwitch && settings.alertSwitch.currentState("switch").value == "on"){
			String amsg="turning off switch due to: "
			if(alertSwitchReset) amsg += "weather alert ended"
			else amsg+= "alerts ended"
			logInfo amsg
			settings.alertSwitch.off()
		}
	}
	if((alertSwitchReset || alertmsg==sNULL)){
		state.alertWeatherMatch=sNULL
		unschedule(walertCheck)
	}
}

void repeatNow(Boolean newmsg=false){
	Boolean doRefresh=true
	if((Boolean)settings.repeatYes && (String)state.repeatmsg && settings.repeatMinutes > 0) {
		if(!newmsg && state.rptCount >= state.rptNum){
			state.repeat = false
			state.repeatmsg = sNULL
			logTrace "Finished repeating alerts."
		}else{
			if(newmsg){
				state.rptCount = 0
				state.rptNum = settings.repeatTimes ? (Integer)settings.repeatTimes : 1
				state.repeat = true
				doRefresh=false
				logTrace "Starting repeating alerts."
			}else{
				if((Boolean)state.repeat) {
					logTrace "Sending repeat message"
					alertNow(0, (String)state.repeatmsg, true)
					//runIn(1,callRefreshTile)
				}
			}
			if((Boolean)state.repeat && (Integer)state.rptCount < (Integer)state.rptNum){
				state.rptCount = (Integer)state.rptCount + 1
				logTrace "Scheduling repeating alert in ${settings.repeatMinutes} minute(s). This is ${state.rptCount}/${state.rptNum} repeated alert(s). Repeat State: ${state.repeat}"
				runIn((Integer)settings.repeatMinutes*60,repeatNow)
			}
		}
	}else{
		logTrace "Repeat not enabled or no message Enabled: ${settings.repeatYes} msg: $state.repeatmsg State: ${state.repeat} Minutes: ${settings.repeatMinutes}"
		//match state
		state.repeat = false
		state.repeatmsg = sNULL
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
			List<String> t0=(List<String>)settings.myWeatherAlert
			if(!t0) msgMap = buildAlertMap(feat)
			else if(t0.contains((String)feat.properties.event)) msgMap = buildAlertMap(feat)

			Boolean expired=false
			//if alert has expired ignore alert
			Date dt = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", alertexpires)
			if(dt1.getTime() > dt.getTime()) { expired=true }
			logTrace "filtered: ${msgMap == null ? sTRUE : sFALSE} expired: ${expired}  alertexpires ${alertexpires}  replacedAt: $replacedAt useEnds: $useEnds    now: ${timestamp}  ${feat.properties.severity}  ${feat.properties.event}"

			if(msgMap!=null){
				if(!expired || (Boolean)settings.debug) {
					Boolean isNewNotice=false
					if(mListofAlertsFLD.size() > 0) {
						Map fndMsg = (Map)mListofAlertsFLD.find { ((String)it.alertid).contains((String)msgMap.alertid) }
						if(fndMsg) { msgMap = fndMsg }
						else isNewNotice = true
					} else {
						isNewNotice = true
						IsnewList = true
					}
					if(isNewNotice) logDebug "Valid ${msgMap.alertid} is new in ListofAlerts: ${IsnewList}"
					else logDebug "Valid ${msgMap.alertid} exists in ListofAlerts"

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
			logDebug "ListofAlerts is (${ListofAlerts.size()}) ${ListofAlerts}"
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
						if(!everDid && (Boolean)settings.repeatYes && !fixedRepeat){
							fixedRepeat=true
							state.repeatmsg = (String)msgMap.alertmsg
							repeatNow(true)
						}
						schedTile=true
					}
					if((Boolean)settings.repeatYes && !fixedRepeat){
						fixedRepeat=true
						if((String)state.repeatmsg != (String)msgMap.alertmsg) schedTile=true
						state.repeatmsg = (String)msgMap.alertmsg // in case messages moved around in order
					}
				}
			}
			if(schedTile) {
				runIn(1,callRefreshTile)
			}
		} else logDebug "No new alerts.  Waiting ${settings.whatPoll.toInteger()} minute(s) before next poll..."
	} else logDebug "null result..."

	if(result!=null){ // deal with network outage; don't drop alerts.
		if(!ListofAlerts){
			if(hadAlerts) logDebug "ending alerts"
			if((Boolean)settings.UsealertSwitch && settings.alertSwitch && settings.alertSwitch.currentState("switch").value == "on") alertNow(null, sNULL, false) // maybe Switch.off()
			if((Boolean)state.repeat){
				unschedule(repeatNow)
				runIn(1,callRefreshTile)
			}
			state.repeat = false
			state.repeatmsg = sNULL
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
	alertmsg = (String)settings.alertCustomMsg
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
	return msg.replaceAll(/(AST|EST|EDT|CST|CDT|MST|MDT|PST|PDT|AKST|AKDT|HST|HAST|HADT)/,sBLANK)
}

static String alertRemoveStates(String msg) {
	return msg.replaceAll(/(AL|AK|AZ|AR|CA|CO|CT|DE|FL|GA|HI|ID|IL|IA|IN|KS|KY|LA|ME|MA|MD|MI|MN|MS|MO|MT|NE|NV|NH|NJ|NM|NY|NC|ND|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI|WY)/, sBLANK)
}

static String alertFormatText(String msg) {
	msg = msg.replaceAll(/NWS/,"the National Weather Service of")
	msg = msg.replaceAll(/(WHAT|WHEN|IMPACTS|IMPACT|WHERE|INCLUDES|HAZARDS|INCLUDE|HAZARD|TEMPERATURE|SOURCE)/, sBLANK)
	msg = msg.replaceAll(/\.{2,}/, sBLANK)
	msg = msg.replaceAll(/\*/, sBLANK)
	msg = msg.replaceAll(/MPH/, "miles per hour")
	msg = msg.replaceAll("","")
	msg = msg.replaceAll("\n",sSPACE)
	msg = msg.replaceAll("\\s+", sSPACE)
	msg = msg.replaceAll(/(?:(\d{2})(\d{2}))|(?:(\d(?!\d{3}))(\d{2}))(?=\s?(?i:am|pm))/,'$1$3:$2$4')
	return msg
}

static String alertFormatArea(String msg) {
	msg.replaceAll(/NWS/,"the National Weather Service of")
	msg = msg.replaceAll(", ",sBLANK)
	msg = msg.replaceAll(",",sBLANK)
	msg = msg.replaceAll(";",",")
	msg = msg.replaceAll("\n"," ")
	msg = msg.replaceAll("\\s+", sSPACE)
	msg = msg.replaceAll("/",sBLANK)
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
	logInfo "Initiating a test alert."
	String msg=buildTestAlert()
	state.repeatmsg=msg
	alertNow(null, msg, false)
	if((Boolean)settings.repeatYes){
		logInfo "Initiating a repeat process (ends in 5 minutes) for test alert."
		state.repeat=false
		repeatNow(true)
		endTime=300
	} else runIn(1,callRefreshTile)
	runIn(endTime,endTest)
}

void endTest(){
	logInfo "Ending repeat for test alert."
	atomicState.testmsg = false
	if((Boolean)settings.UsealertSwitch && settings.alertSwitch && settings.alertSwitch.currentState("switch").value == "on") alertNow(null, sNULL, false) // maybe Switch.off()
	state.repeat = false
	state.repeatmsg = sNULL
	unschedule(repeatNow)
	runIn(1,callRefreshTile)
}

String buildTestAlert() {
	String alertmsg = (String)settings.alertCustomMsg
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
		if((Boolean)settings.useAlertIntro) alertmsg = "Repeating previous alert,, ${settings.AlertIntro} " + alertmsg
		else alertmsg = "Repeating previous alert,," + alertmsg
	}else if((Boolean)settings.useAlertIntro) alertmsg = "${settings.AlertIntro}, " + alertmsg

	if((Boolean)settings.musicmode) {
		List mspks = (List)settings.musicspeaker
		logDebug "Using audioNotification $mspks"
		Boolean okT = false
		if(mspks && mspks[0].hasCommand('playTextAndRestore')) {
			try {
				mspks*.playTextAndRestore(alertmsg.toLowerCase(), settings.speakervolume)
				okT = true
				logInfo "Sending alert to audioNotification Speaker(s)."
			}
			catch (ignored) {}
		}
		if(!okT) { logWarn "audioNotificcation device(s) ${mspks} has not been selected or does not support playTextAndRestore command." }
	}

	if((Boolean)settings.echoSpeaks2) {
		List spks = (List)settings.echospeaker
		logDebug "Using echoSpeaks ${spks}"
		Boolean supportsSetVolume=false
		List<String> msgs = []
		List svVols = []
		Boolean canPlay = spks[0].hasCommand('playAnnouncement')
		if(settings.speakervolume && canPlay && spks && spks[0].hasCommand('setVolume')) {
			if(!settings.speakervolRestore) {
				spks.each { dev ->
					def a = dev?.currentState('volume')?.value
					svVols.push(a)
				}
			}
			try {
				spks*.setVolume(settings.speakervolume)
				supportsSetVolume=true
				msgs.push("Setting Echo Speaker to volume level: ${settings.speakervolume}".toString())
			} catch (e) { logError "unable to set volume", e }
		}
		Boolean okT = false
		if(spks && canPlay) {
			try {
				String tt = "serial"
				if(alertmsg.size() < 420) {
					if(spks.size() > 1 && spks[0].hasCommand('parallelPlayAnnouncement')) {
						spks*.parallelPlayAnnouncement(alertmsg.toLowerCase(), 'NOAA Weather Alert')
						tt = "parallel"
						spks[0].noOp()
					} else spks*.playAnnouncement(alertmsg.toLowerCase(), 'NOAA Weather Alert', null, null)
					tt += " announce"
				} else {
					tt += " speak"
					spks*.speak(alertmsg)
				}

				okT = true
				msgs.push("Sending ${tt} alert to EchoSpeaks device(s).".toString())
				if(supportsSetVolume) {
					if(settings.speakervolRestore) {
						spks*.setVolume(settings.speakervolRestore)
						msgs.push("Restoring Speaker to volume level: ${settings.speakervolRestore}".toString())
					} else {
						Integer i = 0
						spks.each { dev ->
							def a = svVols[i]
							try {
								msgs.push("Restoring Speaker $dev to volume level: ${a}".toString())
								dev.setVolume(a)
							} catch (ignored) { msgs.push("Echo ${i} does not support restore volume command".toString()) }
							i+=1
						}
					}
				}
			}
			catch (ignored) {}
		}
		if (msgs.size() > 0) msgs.each { String msg -> logDebug msg }
		if(!okT) { logWarn "echospeaks device(s) ${spks} has not been selected or does not support playAnnouncement command." }
	}

	if((Boolean)settings.speechmode) {
		List spks = (List)settings.speechspeaker
		List<String> msgs = []
		logDebug "Using speechSynthesis $spks, delays: ${settings.speechdelay}"
		Boolean okT = false
		Boolean canSpeak = spks[0].hasCommand('speak')
		if(canSpeak && spks && spks[0].hasCommand('initialize')) {
			try {
				spks*.initialize()
				msgs.push("Initializing Speech Speaker")
				okT = true
				if((Boolean)settings.speechdelay) pauseExecution(2500)
			}
			catch (e) { logError "initialize command failed", e }
			if(!okT) { msgs.push("Speech device(s) ${spks} has not been selected or does not support initialize command.".toString()) }
		}

		Boolean supportsSetVolume=false
		List svVols = []
		if(canSpeak && settings.speakervolume && spks && spks[0].hasCommand('setVolume')) {
			if(!settings.speakervolRestore) {
				spks.each { dev ->
					def a = dev?.currentState('volume')?.value
					svVols.push(a)
				}
			}
			try {
				msgs.push("Setting Speech Speaker to volume level: ${settings.speakervolume}".toString())
				spks*.setVolume(settings.speakervolume)
				supportsSetVolume=true
				if((Boolean)settings.speechdelay) pauseExecution(2000)
			}
			catch (e) { logError "unable to set volume", e }

			if(!supportsSetVolume && settings.speakervolume) { msgs.push("Speech device(s) ${spks} has not been selected or does not support setVolume command.".toString()) }
		}

		if(spks && canSpeak) {
			okT = false
			alertmsg = alertmsg.toLowerCase()
			try {
				String tt = "serial"
				if(spks && spks.size() > 1 && spks[0].hasCommand('parallelSpeak') && alertmsg.size() < 420) {
					spks*.parallelSpeak(alertmsg)
					tt = "parallel"
					spks[0].noOp()
				} else spks*.speak(alertmsg)

				okT = true
				msgs.push("Sending ${tt} alert to Speech Speaker(s)".toString())

				if(supportsSetVolume) {
					Integer speechDuration = Math.max(Math.round(alertmsg.length()/14).toInteger(),2)+1
					Long speechDuration2 = speechDuration * 1000L
					if((Boolean)settings.speechdelay) pauseExecution(speechDuration2)
					if(settings.speakervolRestore) {
						spks*.setVolume(settings.speakervolRestore)
						msgs.push("Restoring Speech Speaker to volume level: ${settings.speakervolRestore}".toString())
					} else {
						Integer i = 0
						spks.each { dev ->
							def a = svVols[i]
							try {
								dev.setVolume(a)
								msgs.push("Restoring Speech Speaker $dev to volume level: ${a}".toString())
							} catch (ignored) { msgs.push("Speech speaker ${i} doesn't support restore volume command".toString()) }
							i+=1
						}
					}
					//if((Boolean)settings.speechdelay) pauseExecution(1000)
				}
			} catch (ignored) {}
		}
		if (msgs.size() > 0) msgs.each { String msg -> logDebug msg }
		if(!okT) { logWarn "Speech device(s) ${spks} has not been selected or does not support speak command." }
	}
}

void pushNow(String alertmsg, Boolean repeatCheck) {
	if ((Boolean)settings.pushovertts) {
		logInfo "Sending Pushover message."
		if(repeatCheck) {
			if((Integer)settings.repeatTimes>1) alertmsg = "[Alert Repeat ${state.rptCount}/${state.rptNum}] " + alertmsg
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
			String nextpart = sBLANK
			while (nextpart.size() < 1000 && i < lsiz) {
				nextpart += subMsg[i] + sSPACE
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
			if(fullalert.size()>1) ((List)settings.pushoverdevice)*.deviceNotification("(${x+1}/${fullalert.size()}) "+fullalert[x])
			else ((List)settings.pushoverdevice)*.deviceNotification(fullalert[x])
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
		logDebug "Creating data information for tile display."
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
	} else if(!msg) logDebug "Tile display is disabled."
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
			logDebug "Acquired current events list from api.weather.gov"
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
			logInfo "Creating device: NOAA Tile"
			addChildDevice("aaronward", "NOAA Tile", "NOAA", 1234, ["name": "NOAA Tile", isComponent: false])
		}
	}
	catch (e) { logError("Couldn't create child device.", e) }
}

void cleanupChildDevices() {
	try {
		for(device in getChildDevices()) deleteChildDevice(device.deviceNetworkId)
	}
	catch (e) { logError("Couldn't clean up child devices.", e) }
}

// Application Support Routines
Map getResponseURL(Boolean async=false) {
	// Determine if custom coordinates have been selected
	String latitude
	String longitude
	if((Boolean)settings.useCustomCords) {
		latitude = "${settings.customlatitude}".toString()
		longitude = "${settings.customlongitude}".toString()
	}else{
		latitude = "${location.latitude}".toString()
		longitude = "${location.longitude}".toString()
	}

	String wxURI = "https://api.weather.gov/alerts?point=${latitude}%2C${longitude}&status=actual&message_type=alert,update".toString()
	Map result = null

	// Build out the API options
	List<String> ulst = (List<String>)settings.whatAlertUrgency
	if(ulst) wxURI = wxURI + "&urgency=${ulst.join(",")}".toString()

	List<String> sevlst = (List<String>)settings.whatAlertSeverity
	if(!sevlst) sevlst = ["severe"]
	if((Boolean)settings.capitalizeAlertSeverity) {
		List nlst=[]
		sevlst.each { String ss ->
			nlst.push(ss.capitalize())
		}
		sevlst = nlst
	}
	wxURI = wxURI + "&severity=${sevlst.join(",")}".toString()

	List<String> alst = (List)settings.whatAlertCertainty
	if(alst) wxURI = wxURI + "&certainty=${alst.join(",")}".toString()

	state.wxURI = wxURI
	logDebug "URI: <a href='${wxURI}' target=_blank>${wxURI}</a>"


	logInfo "Connecting to weather.gov service."
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
				if( !(responseCode>=200 && responseCode<300 && resp.data) ){
					logWarn "The API Weather.gov did not return a valid response for ${wxURI} $responseCode."
				}
			}
		}
		catch (e) { logError "The API Weather.gov did not return a response for ${wxURI}.",e }
		return result
	} else {
		try {
			asynchttpGet('ahttpreq', requestParams, [command: 'a'])
			return [async:true]
		}
		catch (e) { logError "Async http failed for ${wxURI}.", e }
		return result
	}
}

void ahttpreq(resp, Map cbD){
	Boolean ok=false
	def data
	Integer responseCode
	try {
		//def t0=resp.getHeaders()
		responseCode=resp.status
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

		} else logWarn "The API Weather.gov did not return a response $responseCode."
	} catch(e) { logError "The API Weather.gov did not return a response. (exception)", e }
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
			if( !(responseCode>=200 && responseCode<300 && result) ){
				logWarn "The API Weather.gov get types did not return a response for ${wxURI} ${responseCode}."
			}
		}
	}
	catch (e) { logError "The API Weather.gov get types did not return a response for ${wxURI}.", e }
	return result
}

void checkState() {
	atomicState.testmsg = false
	if(settings.whatPoll==null) app.updateSetting("whatPoll",[value:"5",type:sENUM])
	if(settings.logInfo == null) app.updateSetting("logInfo", [value:sTRUE, type:sBOOL])
	if(settings.logWarn == null) app.updateSetting("logWarn", [value:sTRUE, type:sBOOL])
	if(settings.logError == null) app.updateSetting("logError", [value:sTRUE, type:sBOOL])
	if(settings.logDebug==null) app.updateSetting("logDebug", [value:sFALSE, type:sBOOL])
	if(settings.logTrace==null) app.updateSetting("logTrace", [value:sFALSE, type:sBOOL])
//	if(settings.logEnable==null) app.updateSetting("logEnable",[value:sFALSE,type:sBOOL])
	if(settings.logMinutes==null) app.updateSetting("logMinutes",[value:15,type:sNUMBER])
	if(!(List)settings.whatAlertSeverity) app.updateSetting("whatAlertSeverity",[value:"severe",type:sENUM])
	if((String)settings.alertCustomMsg==sNULL) app.updateSetting("whatCustomMsg",[value:"{alertseverity} Weather Alert for the following counties: {alertarea} {alertdescription} This is the end of this Weather Announcement.",type:"text"])

	Integer t0
	if(settings.repeatTimes){
		t0=(Integer)settings.repeatTimes
	}else{
		app.updateSetting("repeatTimes",[value:1,type:sNUMBER])
		t0=1
	}
	state.rptNum = t0

	if(settings.repeatMinutes==null || settings.repeatMinutes < 0){
		app.updateSetting("repeatMinutes",[value:15,type:sNUMBER])
	}

	state.rptCount = 0
	if(!(Boolean)state.repeat) state.repeatmsg = sNULL
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
	state.repeatmsg = sNULL
	String myId=app.getId()
	ListofAlertsFLD[myId] = []
	ListofAlertsFLD = ListofAlertsFLD
	state.ListofAlerts = []
	if((Boolean)settings.UsealertSwitch && settings.alertSwitch && settings.alertSwitch.currentState("switch").value == "on") alertNow(null, sNULL, false) // maybe Switch.off()
	logWarn "NOAA Weather Alerts application state is reset."

	Integer myPoll=5
	if(settings.whatPoll)myPoll=settings.whatPoll.toInteger()
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
	if((Boolean)settings.logDebug || (Boolean)settings.logTrace || (Boolean)settings.logInfo) {
	//if((Boolean)settings.logEnable){
		Integer myLog=15
		if(settings.logMinutes!=null)myLog=(Integer)settings.logMinutes
		if(myLog>0){
			logTrace "Debug messages set to automatically disable in ${myLog} minute(s)."
			runIn((myLog*60),logsOff)
		}else logTrace "Debug logs set to not automatically disable."
	}else logTrace "Debug logs disabled."
}

void installed() {
	logDebug "Installed with settings: ${settings}"
	initialize()
}

void updated() {
	logDebug "Updated with settings: ${settings}"
	state.remove('num')
	state.remove('speechDuration2')
	state.remove('frequency')
	state.remove('count')
	initialize()
}

void uninstalled() {
	cleanupChildDevices()
}


static String UIsupport(String type, String txt=sBLANK) {
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
			return "<div style='color:#1A77C9;text-align:center'>App/Driver v${appVersionFLD}<br>Originally Developed by: Aaron Ward<br></div>"
			break
		case "configured":
			return "<img border=0 style='max-width:15px' src='https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/support/images/Checked.svg'>"
			break
		case "attention":
			return "<img border=0 style='max-width:15px' src='https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/support/images/Attention.svg'>"
			break
	}
	return sNULL
}

void logsOff(){
	logInfo "Debug logging disabled."
//	app.updateSetting("logEnable",[value:sFALSE,type:sBOOL])
	app.removeSetting("logEnable")
	app.updateSetting("logDebug", [value:sFALSE, type:sBOOL])
	app.updateSetting("logTrace", [value:sFALSE, type:sBOOL])
	app.updateSetting("logInfo", [value:sFALSE, type:sBOOL])
}

private void logDebug(String msg) { if((Boolean)settings.logDebug) { log.debug logPrefix(msg, "purple") } }
private void logInfo(String msg) { if((Boolean)settings.logInfo) { log.info sSPACE + logPrefix(msg, "#0299b1") } }
private void logTrace(String msg) { if((Boolean)settings.logTrace) { log.trace logPrefix(msg, sCLRGRY) } }
private void logWarn(String msg) { if((Boolean)settings.logWarn) { log.warn sSPACE + logPrefix(msg, sCLRORG) } }

void logError(String msg, ex=null) {
	if((Boolean)settings.logError) {
		log.error logPrefix(msg, sCLRRED)
		String a
		try {
			if (ex) a = getExceptionMessageWithLine(ex)
		} catch (ignored) {
		}
		if(a) log.error logPrefix(a, sCLRRED)
	}
}

static String logPrefix(String msg, String color = sNULL) {
	return span("NOAAApp (v" + appVersionFLD + ") | ", sCLRGRY) + span(msg, color)
}

static String span(String str, String clr=sNULL, String sz=sNULL, Boolean bld=false, Boolean br=false) { return str ? "<span ${(clr || sz || bld) ? "style='${clr ? "color: ${clr};" : sBLANK}${sz ? "font-size: ${sz};" : sBLANK}${bld ? "font-weight: bold;" : sBLANK}'" : sBLANK}>${str}</span>${br ? sLINEBR : sBLANK}" : sBLANK }
static String inTS1(String str, String img = sNULL, String clr=sNULL, Boolean und=true) { return spanSmBldUnd(str, clr, img) }
static String spanSmBldUnd(String str, String clr=sNULL, String img=sNULL) { return str ? spanImgStr(img) + span(strUnder(str), clr, sSMALL, true) : sBLANK }
static String spanImgStr(String img=sNULL) { return img ? span("<img src='${(!img.startsWith("http://") && !img.startsWith("https://")) ? getAppImg(img) : img}' width='42'> ") : sBLANK }
static String strUnder(String str, Boolean showUnd=true) { return str ? (showUnd ? "<u>${str}</u>" : str) : sBLANK }

static String getAppImg(String imgName) { return "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/${imgName}.png" }