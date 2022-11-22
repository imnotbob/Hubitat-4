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
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 * Last Update: 11/22/2022
 */
//file:noinspection GroovySillyAssignment
//file:noinspection unused
//file:noinspection SpellCheckingInspection
//file:noinspection GrDeprecatedAPIUsage
//file:noinspection GroovyUnusedAssignment

import groovy.json.JsonSlurper
import groovy.transform.Field
import groovy.transform.CompileStatic
//import groovy.json.*
//import java.util.regex.*
import java.text.SimpleDateFormat
//import java.text.ParseException
//import java.util.Date
//import groovy.time.*

@Field static final String appVersionFLD ='4.0.020'
@Field static final String sNULL         =(String)null
@Field static final String sBLANK        =''
@Field static final String sSPACE        =' '
@Field static final String sCLRORG       ='orange'
@Field static final String sCLRGRY       ='gray'
@Field static final String sLINEBR       ='<br>'
@Field static final String sCLRRED       ='red'
@Field static final String sFALSE        ='false'
@Field static final String sTRUE         ='true'
@Field static final String sBOOL         ='bool'
@Field static final String sENUM         ='enum'
@Field static final String sNUMBER       ='number'
@Field static final String sTXT          ='text'
@Field static final String sSMALL        ='small'
@Field static final String sDEBUG        ='debug'
@Field static final String sNOAA         ='NOAA'
@Field static final String sON           ='on'
@Field static final String sSW           ='switch'
@Field static final String sNM           ='name'
@Field static final String sVL           ='value'
@Field static final String sTY           ='type'
@Field static final String sTIT          ='title'
@Field static final String sREQ          ='required'
@Field static final String sDEFVAL       ='defaultValue'
@Field static final String sMULTPL       ='multiple'
@Field static final String sSUBONCHG     ='submitOnChange'
@Field static final String sDESC         ='description'

@Field static final String sMAINPG       ='mainPage'
@Field static final String sLOGINFO      ='logInfo'
@Field static final String sLOGDEBUG     ='logDebug'
@Field static final String sLOGTRC       ='logTrace'
@Field static final String sLOGWRN       ='logWarn'
@Field static final String sLOGERR       ='logError'

@Field static final String sPSHTTS       ='pushovertts'
@Field static final String sMUSICMD      ='musicmode'
@Field static final String sSPEECHMD     ='speechmode'
@Field static final String sECHOSPS2     ='echoSpeaks2'
@Field static final String sMODESYES     ='modesYes'
@Field static final String sMODESEVYES   ='modeSeverityYes'
@Field static final String sSWITCHYES    ='switchYes'
@Field static final String sUSEALRTSW    ='UsealertSwitch'
@Field static final String sALRTSW       ='alertSwitch'
@Field static final String sWHATPOLL     ='whatPoll'
@Field static final String sRPTYES       ='repeatYes'

@Field static final String sRPT          ='repeat'
@Field static final String sRPTMSG       ='repeatmsg'
@Field static final String sEVTTYPS      ='eventTypes'
@Field static final String sLISTOFALRTS  ='ListofAlerts'

definition(
	(sNM):'NOAA Weather Alerts',
	namespace: 'aaronward',
	author: 'Aaron Ward',
	(sDESC): 'NOAA Weather Alerts Application ',
	category: 'Weather',
	iconUrl: 'https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png',
	iconX2Url: 'https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png',
	iconX3Url: 'https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png',
	documentationLink: 'https://github.com/imnotbob/Hubitat-4/blob/master/NOAA/README.md',
	singleInstance: true,
	oauth: false,
	pausable: true)

preferences{
	page ((sNM): sMAINPG, (sTIT): sBLANK, install: true, uninstall: false)
	page ((sNM): 'NotificationPage', (sTIT): sBLANK, install: false, uninstall: false, nextPage: sMAINPG)
	page ((sNM): 'ConfigPage', (sTIT): sBLANK, install: false, uninstall: false, nextPage: sMAINPG)
	page ((sNM): 'AdvConfigPage', (sTIT): sBLANK, install: false, uninstall: false, nextPage: sMAINPG)
	page ((sNM): 'RestrictionsPage', (sTIT): sBLANK, install: false, uninstall: false, nextPage: sMAINPG)
	page ((sNM): 'SettingsPage', (sTIT): sBLANK, install: false, uninstall: true, nextPage: sMAINPG)
}

@CompileStatic
private static Boolean bIs(Map m,String v){ (Boolean)m[v] }

/** m.string  */
@CompileStatic
private static String sMs(Map m,String v){ (String)m[v] }

/** m[v]  */
@CompileStatic
private static oMs(Map m,String v){ m[v] }

private Map gtSettings() { return (Map)settings }
private Map gtSt() { return (Map)state }

def mainPage(){
	Map set= gtSettings()
	Map st= gtSt()
	if(oMs(set,sLOGINFO)==null) app.updateSetting(sLOGINFO, [(sVL):sTRUE, (sTY):sBOOL])
	if(oMs(set,sLOGWRN)==null) app.updateSetting(sLOGWRN, [(sVL):sTRUE, (sTY):sBOOL])
	if(oMs(set,sLOGERR)==null) app.updateSetting(sLOGERR, [(sVL):sTRUE, (sTY):sBOOL])

	dynamicPage((sNM): sMAINPG){
		installCheck()
		if(sMs(gtSt(),'appInstalled')=='COMPLETE'){
			section(UIsupport('logo')){
				String s='configured'
				String s1='attention'
				String tit
				Integer typ
				typ= bIs(set,sPSHTTS) || bIs(set,sMUSICMD) || bIs(set,sSPEECHMD) || bIs(set,sECHOSPS2) ? 1:0
				tit= "${UIsupport(typ ? s : s1)} Setup Notification Device(s)"
				href((sNM): 'NotificationPage', (sTIT): tit, (sREQ): false, page: 'NotificationPage', (sDESC): 'Select TTS and PushOver Devices')

				typ = oMs(set,'whatAlertSeverity') || oMs(set,sWHATPOLL) || sMs(set,'alertCustomMsg') ? 1:0
				tit= "${UIsupport(typ ? s : s1)} Weather Alert Settings"
				href((sNM): 'ConfigPage', (sTIT): tit, (sREQ): false, page: 'ConfigPage', (sDESC): 'Change default settings for weather alerts to monitor')

				typ= (List)settings['myWeatherAlert'] || (List)settings['whatAlertUrgency'] || (List)settings['whatAlertCertainty'] ? 1:0
				tit= (typ ? "${UIsupport(s)} " : sBLANK) + "Advanced Alert Settings"
				href((sNM): 'AdvConfigPage', (sTIT): tit, (sREQ): false, page: 'AdvConfigPage', (sDESC): 'Add additional detailed weather settings to monitor')

				typ= bIs(set,sMODESYES) || bIs(set,sSWITCHYES) || bIs(set,sMODESEVYES) || bIs(set,'pushoverttsalways') ? 1:0
				tit= (typ ? "${UIsupport(s)} " : sBLANK) + "Restrictions"
				href((sNM): 'RestrictionsPage', (sTIT): tit, (sREQ): false, page: 'RestrictionsPage', (sDESC): 'Setup restriction options')

				typ= bIs(set,'debug') || bIs(set,sLOGDEBUG) || bIs(set,sLOGTRC) || bIs(set,sLOGINFO) ? 1:0
				tit= (typ ? "${UIsupport(s)} " : sBLANK) + "Settings"
				href((sNM): 'SettingsPage', (sTIT): tit, (sREQ): false, page: 'SettingsPage', (sDESC): 'Modify NOAA Weather Alerts Application Settings, Logging, Test')
				paragraph UIsupport('line')
				paragraph UIsupport('footer')
			}
		}
	}
}

def NotificationPage(){
	buildEventsList()
	dynamicPage((sNM): 'NotificationPage'){
		Map set= gtSettings()
		section(UIsupport('logo')){
			paragraph UIsupport('header', ' Setup Notification Device(s)')
			paragraph 'Select a communication(s) method, notification and restore volume levels, use a switch with advanced settings.'
			String t, t1
			// PushOver Devices
			t=sPSHTTS
			t1='pushoverdevice'
			input t, sBOOL, (sTIT): "Use 'Pushover' device(s)?", (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true
			if(bIs(set,t)){ input t1, 'capability.notification', (sTIT): 'PushOver Device', (sREQ): true, (sMULTPL): true}
			else app.removeSetting(t1)

			// audioNotification Music Speakers (Sonos, etc)
			t=sMUSICMD
			t1='musicspeaker'
			input((sNM): t, (sTY): sBOOL, (sDEFVAL): false, (sTIT): 'Use audioNotification Speaker(s) for TTS?', (sDESC): 'audioNotification Speaker(s)?', (sSUBONCHG): true)
			if(bIs(set,t)) input t1, 'capability.audioNotification', (sTIT): 'Choose audioNotification speaker(s)', (sREQ): false, (sMULTPL): true, (sSUBONCHG): true
			else app.removeSetting(t1)

			// Speech Speakers
			t=sSPEECHMD
			t1='speechspeaker'
			input((sNM): t, (sTY): sBOOL, (sDEFVAL): false, (sTIT): 'Use speechSynthesis Speaker(s) for TTS? (Google, Alexa TTS, etc)', (sDESC): 'Speech Speaker(s)?', (sSUBONCHG): true)
			if(bIs(set,t)){
				input t1, 'capability.speechSynthesis', (sTIT): 'Choose speechSynthesis speaker(s)', (sREQ): false, (sMULTPL): true, (sSUBONCHG): true
				input((sNM): 'speechdelay', (sTY): sBOOL, (sDEFVAL): false, (sTIT): 'Place delays between commands to speechSynthesis Speaker(s)', (sDESC): 'Speech Speaker(s)?', (sSUBONCHG): true)
			} else{
				app.removeSetting(t1)
				app.removeSetting('speechdelay')
			}

			// Echo Speaks devices
			t=sECHOSPS2
			t1='echospeaker'
			input ((sNM): t, (sTY): sBOOL, (sDEFVAL): false, (sTIT): 'Use Echo Speaks device(s) for Announcement TTS?', (sDESC): 'Echo Speaks device?', (sSUBONCHG): true)
			if(bIs(set,t))
				input t1, 'capability.musicPlayer', (sTIT): 'Choose Echo Speaks Device(s)', (sREQ): false, (sMULTPL): true, (sSUBONCHG): true
			else app.removeSetting(t1)

			// Master Volume settings
			Boolean es=bIs(set,sECHOSPS2)
			Boolean sm= bIs(set,sSPEECHMD)
			Boolean mm= bIs(set,sMUSICMD)
			if(es || sm || mm)
				input 'speakervolume', sNUMBER, (sTIT): 'Notification Volume Level: (Leave blank to use current volume setting)', (sDESC): '0-100%', (sREQ): false, (sSUBONCHG): true
			if(es || sm)
				input 'speakervolRestore', sNUMBER, (sTIT): 'Restore Volume Level: (Leave blank to restore previous value)', (sDESC): '0-100', (sREQ): false, (sSUBONCHG): true
			else app.removeSetting('speakervolRestore')

			if(!es && !sm && !mm){
				app.removeSetting('speakervolRestore'); app.removeSetting('speakervolume')
			}

			// Switch to set when alert active
			t=sUSEALRTSW
			t1='alertSwitchWeatherType'
			input ((sNM): t, (sTY): sBOOL, (sTIT): 'Use a switch to turn ON with Alert?', (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true)
			if(bIs(set,t)){
				input ((sNM): sALRTSW, (sTY): 'capability.switch', (sTIT): 'Select a switch to turn ON with Alert?', (sMULTPL): false, (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true)
				input ((sNM):'alertSwitchOff', (sTY): sBOOL, (sTIT): 'Turn off switch when all Alerts expire?', (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true)
				input ((sNM):t1, (sTY): sBOOL, (sTIT): 'Turn off switch if certain weather alert types expire?', (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true)
				if(bIs(set,t1))
					input 'alertSwitchWeatherTypeWatch', sENUM, (sTIT): 'Watch for a specific Weather event(s)?', (sREQ): false, (sMULTPL): true, (sSUBONCHG): true, options: (List)state[sEVTTYPS]
			} else{
				app.removeSetting(sALRTSW)
				app.removeSetting('alertSwitchOff')
				app.removeSetting(t1)
				app.removeSetting('alertSwitchWeatherTypeWatch')
			}
			// Disable Tile updates
			input ((sNM): 'disableTile', (sTY): sBOOL, (sDEFVAL): false, (sTIT): 'Disable updates of Tile Device to display alerts?', (sDESC): 'Disable tile device?', (sSUBONCHG): true)
		}
	}
}

@Field static String custDefFLD= '{alertseverity} Weather Alert for the following counties:{alertarea}{alertdescription} This is the end of this Weather Announcement.'

def ConfigPage(){
	dynamicPage((sNM): 'ConfigPage'){
		Map set= gtSettings()
		section(UIsupport('logo')){
			paragraph UIsupport('header', ' Alert Settings')
			paragraph 'Configure NOAA to look for specific alert severities, how often to poll for weather information, repeat alerts, use custom coordinates and customize the alert message sent to notification device(s).'
			input ((sNM): 'whatAlertSeverity', (sTY): sENUM, (sTIT): 'Weather Severity(s) to gather in poll: ',
				options: [
					'unknown': 'Unknown',
					'minor': 'Minor',
					'moderate': 'Moderate',
					'severe': 'Severe',
					'extreme': 'Extreme'], (sREQ): true, (sMULTPL): true, (sDEFVAL): 'severe')
			input ((sNM):'capitalizeAlertSeverity', (sTY): sBOOL, (sTIT): 'Capitalize Severity in API calls (NOAA bug)?', require: false, (sDEFVAL): false, (sSUBONCHG): true)
			input ((sNM): sWHATPOLL, (sTY): sENUM, (sTIT): 'Poll Frequency: ', options: ['1': '1 Minute', '5': '5 Minutes', '10': '10 Minutes', '15': '15 Minutes', '60': '60 Minutes'], (sREQ): true, (sMULTPL): false, (sDEFVAL): '5')
			input ((sNM): sRPTYES, (sTY): sBOOL, (sTIT): 'Repeat Alert?', require: false, (sDEFVAL): false, (sSUBONCHG): true)
			if(bIs(set,sRPTYES)){
				input ((sNM):'repeatTimes', (sTY): sNUMBER, (sTIT): 'Number of times to repeat the alert?', require: false, (sDEFVAL): 1, (sSUBONCHG):true)
				input ((sNM):'repeatMinutes', (sTY): sNUMBER, (sTIT): 'Number of minutes between each repeating alert?', require: false, (sDEFVAL): 15, (sSUBONCHG):true)
			}
			input ((sNM): 'useCustomCords', (sTY): sBOOL, (sTIT): 'Use Custom Coordinates?', require: false, (sDEFVAL): false, (sSUBONCHG): true)
			if(bIs(set,'useCustomCords')){
				paragraph 'Below coordinates are acquired from your Hubitat Hub. Enter your custom coordinates:'
				input ((sNM):'customlatitude', (sTY):sTXT, (sTIT): 'Latitude coordinate:', require: false, (sDEFVAL): "${location.latitude}", (sSUBONCHG): true)
				input ((sNM):'customlongitude', (sTY):sTXT, (sTIT): 'Longitude coordinate:', require: false, (sDEFVAL): "${location.longitude}", (sSUBONCHG): true)
			}
			input ((sNM):'useAlertIntro', (sTY): sBOOL, (sTIT): 'Use a pre-notification message for TTS device(s)?', require: false, (sDEFVAL): false, (sSUBONCHG): true)
			if(bIs(set,'useAlertIntro'))
				input ((sNM):'AlertIntro', (sTY): sTXT, (sTIT): 'Alert pre-notification message:', require: false, (sDEFVAL):'Attention, Attention')
			input ((sNM): 'alertCustomMsg', (sTY): sTXT, (sTIT): 'Custom Alert Message (use customization instructions):', require: false, (sDEFVAL): custDefFLD, (sSUBONCHG): true)
		}
		section('Alert Message Customization Instructions:', hideable: true, hidden: true){
			paragraph '<b>Alert message variables:</b>'
			paragraph '{alertseverity}=alertseverity'
			paragraph '{alertcertainty}=alert certainty of occurring'
			paragraph '{alerturgency}=alert urgency'
			paragraph '{alertevent}=alert event type'
			paragraph '{alertheadline}=alert headline'
			paragraph '{alertdescription}=alert description'
			paragraph '{alertinstruction}=alert instructions'
			paragraph '{alertarea}=counties, cities or area'
			paragraph ' '
			paragraph '<b>Example:</b>{alertseverity} weather alert. Certainty is{alertcertainty}. Urgency is{alerturgency}.{alertheadline}.{alertinstruction}. This is the end of the weather announcement.'
		}
	}
}

def AdvConfigPage(){
	buildEventsList()
	dynamicPage((sNM): 'AdvConfigPage'){
		section(UIsupport('logo')){
			paragraph UIsupport('header', ' Advanced Alert Settings')
			paragraph "Use with caution as below settings may cause undesired results. Only select what you would like to refine in your alerts. Reference <a href='https://www.weather.gov/documentation/services-web-api?prevfmt=application%2Fcap%2Bxml/default/get_alerts#/default/get_alerts' target='_blank'>Weather.gov API</a> and use the API response test button below to determine your desired results."
			input ((sNM):'myWeatherAlert', (sTY):sENUM, (sTIT): 'Filter results for specific Weather event(s)?', (sREQ): false, (sMULTPL): true, (sSUBONCHG): true, options: (List)state[sEVTTYPS])
			input ((sNM): 'whatAlertUrgency', (sTY): sENUM, (sTIT): 'Poll only for a specific Alert Urgency: ', (sMULTPL): true, (sSUBONCHG): true,
				options: [
					'unknown': 'Unknown',
					//'past': 'Past',
					'immediate': 'Immediate',
					'expected': 'Expected',
					'future': 'Future'
				])

			input ((sNM): 'whatAlertCertainty', (sTY): sENUM, (sTIT): 'Poll only for specific Alert Certainty: ', (sREQ): false, (sMULTPL): true, (sSUBONCHG): true,
				options: [
					'unknown': 'Unknown',
					//'unlikely': 'Unlikely',
					'possible': 'Possible',
					'likely': 'Likely',
					'observed': 'Observed'
				])
		}
	}
}

def RestrictionsPage(){
	buildEventsList()
	dynamicPage((sNM): 'RestrictionsPage'){
		Map set; set= gtSettings()
		section(UIsupport('logo')){
			paragraph UIsupport('header', ' Restrictions')
			paragraph 'Restrict notifications based on modes or a switch. Override restrictions if the alert is a certain severity or weather type. For notifications that are restricted, if a PushOver device is enabled alerts can still be sent but not over TTS.'
			input ((sNM):sMODESYES, (sTY):sBOOL, (sTIT): 'Enable restriction of notifications by current mode(s)?', (sREQ): true, (sDEFVAL): false, (sSUBONCHG): true)
			if(bIs(set,sMODESYES))
				input((sNM):'modes', (sTY): 'mode', (sTIT): 'Restrict notifications when current mode is:', (sMULTPL): true, (sREQ): false, (sSUBONCHG): true)
			input ((sNM):sSWITCHYES, (sTY):sBOOL, (sTIT): 'Restrict notifications using a switch?', (sREQ): true, (sDEFVAL): false, (sSUBONCHG): true)
			if(bIs(set,sSWITCHYES))
				input ((sNM):'restrictbySwitch', (sTY):'capability.switch', (sTIT): 'Use a switch to restrict notifications?', (sREQ): false, (sMULTPL): false, (sDEFVAL): null, (sSUBONCHG): true)
			paragraph '<br>'
			paragraph UIsupport('header', ' Overrides')
			paragraph '<br>Below settings will ignore restrictions above based on either weather severity type or weather type.'
			input ((sNM):sMODESEVYES, (sTY):sBOOL, (sTIT): 'Ignore restrictions for certain severity types?', (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true)
			if(bIs(set,sMODESEVYES))
				input ((sNM): 'modeSeverity', (sTY): sENUM, (sTIT): 'Severity option(s) that will ignore restrictions: ',
					options: [
						'Minor': 'Minor',
						'Moderate': 'Moderate',
						'Severe': 'Severe',
						'Extreme': 'Extreme'], (sREQ): true, (sMULTPL): true, (sDEFVAL): 'Severe')

			input ((sNM):'modeWeatherType', (sTY):sBOOL, (sTIT): "Ignore restrictions for certain weather types?", (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true)

			if(bIs(set,'modeWeatherType'))
				input ((sNM): 'WeatherType', (sTY): sENUM, (sTIT): "Select weather type to ignore restrictions: ", (sREQ): true, (sMULTPL):true, (sSUBONCHG): true, options: (List)state[sEVTTYPS])
			paragraph "<hr>"
			if(bIs(set,sPSHTTS))
				input ((sNM):'pushoverttsalways', (sTY):sBOOL, (sTIT): "Enable Pushover notifications even when restricted?", (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true)
			else app.removeSetting('pushoverttsalways')
		}
	}
}

def SettingsPage(){
	dynamicPage((sNM): "SettingsPage"){
		section(UIsupport("logo")){
			paragraph UIsupport("header", " Settings")
			paragraph 'Enable logging, run a test alert, if errors reset the applications state settings and test your weather alert configurations.'
			input sLOGINFO, sBOOL, (sTIT): inTS1('Show Info Logs?', sDEBUG), (sREQ): false, (sDEFVAL): true, (sSUBONCHG): true
			input sLOGWRN, sBOOL, (sTIT): inTS1('Show Warning Logs?', sDEBUG), (sREQ): false, (sDEFVAL): true, (sSUBONCHG): true
			input sLOGERR, sBOOL, (sTIT): inTS1('Show Error Logs?', sDEBUG), (sREQ): false, (sDEFVAL): true, (sSUBONCHG): true
			input sLOGDEBUG, sBOOL, (sTIT): inTS1('Show Debug Logs?', sDEBUG), (sDESC): 'Auto disables after 6 hours', (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true
			input sLOGTRC, sBOOL, (sTIT): inTS1('Show Detailed Logs?', sDEBUG), (sDESC): 'Only enabled when asked to.\n(Auto disables after 6 hours)', (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true

			Map set; set= gtSettings()

//			input 'logEnable', sBOOL, (sTIT): "Enable Debug Logging?", (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true
//			if(bIs(set,'logEnable')) input 'logMinutes', sNUMBER, (sTIT): "Log for the following number of minutes (0=logs always on):", range: "0..300", (sREQ): false, (sDEFVAL):15, (sSUBONCHG): true
			if(bIs(set,sLOGDEBUG) || bIs(set,sLOGTRC) || bIs(set,sLOGINFO))
				input 'logMinutes', sNUMBER, (sTIT): 'Log for the following number of minutes (0=logs always on):', range: '0..300', (sREQ): false, (sDEFVAL):15, (sSUBONCHG): true

			String t
			t='runTest'
			input t, sBOOL, (sTIT): 'Run a test Alert?', (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true
			if(bIs(set,t)){
				app.updateSetting(t,[(sVL):sFALSE,(sTY):sBOOL])
				app.removeSetting(t)
				runtestAlert()
			}

			t='init'
			input t, sBOOL, (sTIT): 'Reset current application state?', (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true
			if(bIs(set,t)){
				app.updateSetting(t,[(sVL):sFALSE,(sTY):sBOOL])
				app.removeSetting(t)
				unschedule()
				logWarn 'NOAA Weather Alerts application state is being reset.'
				initialize()
			}

			t='debug'
			String t1='getAPI'
			input t, sBOOL, (sTIT): 'Debug alert configuration - if expired alerts are available, use those alerts? (only enable this with the test config option below)', (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true
			input t1, sBOOL, (sTIT): 'Test alert configuration and display weather.gov API response?', (sREQ): false, (sDEFVAL): false, (sSUBONCHG): true
			if(bIs(set,t1)){
				getAlertMsgSync()
				app.updateSetting(t1,[(sVL):sFALSE,(sTY):sBOOL])
				app.removeSetting(t1)
				app.updateSetting(t,[(sVL):sFALSE,(sTY):sBOOL])
				app.removeSetting(t)

				List<Map> mListofAlertsFLD= initListofAlerts()
				if(mListofAlertsFLD){
					def rdev= settings['restrictbySwitch']
					Boolean restrictionSwitch=(bIs(set,sSWITCHYES) && rdev!=null && rdev.currentState(sSW).value==sON)
					List<String> modes= (List<String>)settings['modes']
					Boolean restrictionMode=(bIs(set,sMODESYES) && modes!=null && modes.contains(location.mode))
					Boolean overrideRestSeverity
					overrideRestSeverity=(bIs(set,sMODESEVYES) && settings['modeSeverity'] != null)
					Boolean overrideRestWeather
					overrideRestWeather=(bIs(set,'modeWeatherType') && settings['WeatherType'] != null)
					//Boolean alertSwitchReset=((Boolean)settings.alertSwitchWeatherType && (List)settings.alertSwitchWeatherTypeWatch && ((List)settings.alertSwitchWeatherTypeWatch).contains(mListofAlertsFLD[0].alertevent))
					//def testresult=(!(result || result2) || result3 || result4) ? true : false
					Date date=new Date()
					SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy h:mm a")
					String testConfig; testConfig=sBLANK
					String customMsg; customMsg=sMs(set,'alertCustomMsg')
					if(bIs(set,'useAlertIntro')) customMsg=sMs(set,'AlertIntro')+', '+customMsg

					String temp
					temp="<hr><br>Current poll of Weather API: ${sdf.format(date)}<br/><br/>URI: <a href='${state['wxURI']}' target=_blank>${state['wxURI']}</a><br><br>AlertMSG Built based on configuration:<br><br>${customMsg}<br><br>"
					temp += "<table border=0><tr colspan=2><td>Current Restriction Settings:</td></tr>"
					temp += "<tr><td>Switch:</td><td>${restrictionSwitch ? "Active for ${rdev}" : "Inactive"}</td></tr>"
					temp += "<tr><td>Mode:</td><td>${restrictionMode ? "Active for ${modes}" : "Inactive"}</td></tr>"
					temp += "<tr><td>Severity Overrides Restrictions:</td><td>${overrideRestSeverity ? "Enabled for ${settings['modeSeverity']}" : "Disabled"}</td></tr>"
					temp += "<tr><td>Weather Type Overrides Restrictions:</td><td>${overrideRestWeather ? "Enabled for ${settings['WeatherType']}" : "Disabled"}</td></tr></table></br>"
					paragraph temp
					Integer y
					for(y=0;y<mListofAlertsFLD.size();y++){
						String testalertmsg
						Map item= mListofAlertsFLD[y]
						overrideRestSeverity=(bIs(set,sMODESEVYES) && settings['modeSeverity'] != null && ((List<String>)settings['modeSeverity']).contains(sMs(item,'alertseverity')))
						overrideRestWeather=(bIs(set,'modeWeatherType') && settings['WeatherType'] != null && ((List<String>)settings['WeatherType']).contains(sMs(item,'alertevent')))
						Boolean potts= bIs(set,sPSHTTS)
						if(!restrictionSwitch && !restrictionMode && !overrideRestSeverity && !overrideRestWeather){
							if(potts) testalertmsg="alert would be announced on TTS and PushOver device(s)."
							else testalertmsg="alert would be announced on TTS device(s)."
							testalertmsg += " No restrictions active."
						}else{
							if(overrideRestSeverity || overrideRestWeather){
								if(potts) testalertmsg="alert would be announced on TTS and PushOver device(s)"
								else testalertmsg="alert would be announced only on TTS device(s)"
								testalertmsg += " - Restrictions override active."
							}else{
								if(potts && bIs(set,'pushoverttsalways')) testalertmsg="alert would be announced only on PushOver device(s). Alert restricted with pushover always override."
								else testalertmsg="alert would not be announced. Alert restricted."
							}
						}
						testConfig +="<table border=1px><tr><td colspan='2'>Alert ${y+1}/${mListofAlertsFLD.size()} - ${testalertmsg}</td></tr>"
						testConfig += "<tr><td>Field Name</td><td>Value</td></tr><tr><td>Severity</td><td>${item.alertseverity}</td></tr>"
						testConfig += "<tr><td>Area</td><td>${item.alertarea}</td></tr>"
						testConfig += "<tr><td>Sent</td><td>${item.alertsent}</td></tr>"
						testConfig += "<tr><td>Effective</td><td>${item.alerteffective}</td></tr>"
						testConfig += "<tr><td>Expires</td><td>${item.alertexpires}</td></tr>"
						testConfig += "<tr><td>Status</td><td>${item.alertstatus}</td></tr>"
						testConfig += "<tr><td>Message Type</td><td>${item.alertmessagetype}</td></tr>"
						testConfig += "<tr><td>Category</td><td>${item.alertcategory}</td></tr>"
						testConfig += "<tr><td>Certainty</td><td>${item.alertcertainty}</td></tr>"
						testConfig += "<tr><td>Urgency</td><td>${item.alerturgency}</td></tr>"
						testConfig += "<tr><td>Sender Name</td><td>${item.alertsendername}</td></tr>"
						testConfig += "<tr><td>Event Type</td><td>${item.alertevent}</td></tr>"
						testConfig += "<tr><td>Headline</td><td>${item.alertheadline}</td></tr>"
						testConfig += "<tr><td>Description</td><td>${item.alertdescription}</td></tr>"
						testConfig += "<tr><td>Instruction</td><td>${item.alertinstruction}</td></tr></table>"
					}
					paragraph testConfig
				}
				else paragraph "There are no reported weather alerts in your area, the weather alerts available have expired, the api.weather.gov api is not available, or you need to change NOAA Weather Alert options to acquire desired results.<br><br>Current URI: <a href='${state['wxURI']}' target=_blank>${state['wxURI']}</a>"
			}
		}
		section(){
			paragraph "Change applicaiton label?"
			label ((sNM): sNM, (sTIT): "Name", state: "complete", (sDEFVAL): app.name, (sREQ): false)
		}
	}
}

// Main Application Routines
def main(){
	// Get the alert message
	issueGetAlertMsg()
}

void callRefreshTile(){
	String t=app.id.toString()
	def noaaTile
	noaaTile=getChildDevice(sNOAA+t)
	if(!noaaTile) noaaTile= getChildDevice(sNOAA)
	if(noaaTile) noaaTile.refreshTile()
}

List<Map> initListofAlerts(){
	String myId=app.getId()
	if(!ListofAlertsFLD[myId] && (List)state[sLISTOFALRTS]) ListofAlertsFLD[myId]=(List<Map>)state[sLISTOFALRTS] // on hub restart or code reload
	return ListofAlertsFLD[myId] ?: []
}

void alertNow(Integer y, String alertmsg, Boolean repeatCheck, Map msgMap=null){
	List<Map> mListofAlertsFLD= initListofAlerts()
	Map item= y!=null && mListofAlertsFLD ? mListofAlertsFLD[y] : null
	Map set= gtSettings()

	// check restrictions based on Modes and Switches
	def rdev= settings['restrictbySwitch']
	Boolean restrictionSwitch=(bIs(set,sSWITCHYES) && rdev!=null && rdev.currentState(sSW).value==sON)
	List<String> modes= (List<String>)settings['modes']
	Boolean restrictionMode=(bIs(set,sMODESYES) && modes!=null && modes.contains(location.mode))
	Boolean overrideRestSeverity=(item && bIs(set,sMODESEVYES) && settings['modeSeverity'] != null && ((List<String>)settings['modeSeverity']).contains(sMs(item,'alertseverity')))
	Boolean overrideRestWeather=(item && bIs(set,'modeWeatherType') && settings['WeatherType'] != null && ((List<String>)settings['WeatherType']).contains(sMs(item,'alertevent')))
	logDebug "Restrictions on?  Modes: ${restrictionMode}, Switch: ${restrictionSwitch}, Severity Override: ${overrideRestSeverity}, Weather Type Override: ${overrideRestWeather}"

	Boolean alertWmatch; alertWmatch=false
	if(alertmsg!=sNULL){
		// no restrictions
		def dev=set[sALRTSW]
		Boolean usealsw= bIs(set,sUSEALRTSW) && dev
		if(usealsw && bIs(set,'alertSwitchWeatherType') && item && (List)settings['alertSwitchWeatherTypeWatch'] &&
				((List<String>)settings['alertSwitchWeatherTypeWatch']).contains(sMs(item,'alertevent')) ) alertWmatch=true

		Boolean override= (overrideRestSeverity || overrideRestWeather)
		Boolean needPush= !msgMap || (msgMap && !bIs(msgMap,'alertPushed'))
		if( (!restrictionSwitch && !restrictionMode && !override) || override ){
			logInfo "Sending alert ${ override ? '(override active' : sBLANK}" + ': '+alertmsg
			if(needPush) pushNow(alertmsg, repeatCheck)
			if(msgMap){ msgMap['alertPushed']=true; msgMap['alertAnnounced']=true}
			if(usealsw) dev.on()
			if(alertWmatch) state['alertWeatherMatch']=sMs(item,'alertexpires')
			talkNow(alertmsg, repeatCheck)
		}else{
			if(bIs(set,'pushoverttsalways')){
				logInfo "Sending alert to pushover, Restrictions are enabled but PushoverTTS always override enabled: ${alertmsg}"
				if(needPush) pushNow(alertmsg, repeatCheck)
				if(msgMap) msgMap['alertPushed']=true
			}else logDebug "Not sending alert, Restrictions are enabled."
		}
	}
	walertCheck(alertmsg)
	String awm= sMs(gtSt(),'alertWeatherMatch')
	if(alertWmatch && awm){
		Date dt=parseDt(awm)
		Long sec= Math.round((dt.getTime()-wnow()) / 1000L)
		if(sec > 0L){
			runIn(sec, "walertCheck")
			logDebug "Scheduling check in $sec seconds:"
		}
	}
}

Long wnow(){ return (Long)now() }

static Date parseDt(String s){
	Date dt=new Date().parse("yyyy-MM-dd'T'HH:mm:ssXXX", s)
	return dt
}

void walertCheck(String alertmsg="a"){
	List<Map> mListofAlertsFLD= initListofAlerts()
	Map set= gtSettings()

	Boolean alertSwitchReset; alertSwitchReset=false
	def dev=set[sALRTSW]
	Boolean usealsw= bIs(set,sUSEALRTSW) && dev
	List<String> weaTypWat= (List)settings['alertSwitchWeatherTypeWatch']
	Boolean alswWeatTyp= bIs(set,'alertSwitchWeatherType') && weaTypWat && mListofAlertsFLD
	if(sMs(gtSt(),'alertWeatherMatch')){
		Boolean alertReset; alertReset=true
		Integer y
		for(y=0;y<mListofAlertsFLD.size();y++){
			if(usealsw && alswWeatTyp && weaTypWat.contains(sMs(mListofAlertsFLD[y],'alertevent'))){
				alertReset=false; break
			}
		}
		if(alertReset){ state['alertWeatherMatch']=sNULL; alertSwitchReset=true }
	}
	if((alertSwitchReset || alertmsg==sNULL) && usealsw && (bIs(set,'alertSwitchOff ') || alertSwitchReset)){
		if(dev.currentState(sSW).value==sON){
			String amsg
			amsg="turning off switch due to: "
			if(alertSwitchReset) amsg += "weather alert ended"
			else amsg+= "alerts ended"
			logInfo amsg
			dev.off()
			runIn(1,callRefreshTile)
		}
	}
	if((alertSwitchReset || alertmsg==sNULL)){
		state['alertWeatherMatch']=sNULL
		unschedule(walertCheck)
	}
}

void repeatNow(Boolean newmsg=false){
	Boolean doRefresh; doRefresh=true
	Map set= gtSettings()
	Map st= gtSt()
	String msg; msg= sMs(st,sRPTMSG)
	Boolean repYes= bIs(set,sRPTYES)
	Integer rptm= (Integer)set['repeatMinutes']
	if(repYes && msg && rptm > 0){
		Integer rptc; rptc= (Integer)state['rptCount']
		Integer rptn; rptn= (Integer)state['rptNum']
		if(!newmsg && rptc >= rptn){
			state[sRPT]=false
			state[sRPTMSG]=sNULL; msg=sNULL
			logTrace "Finished repeating alerts."
		}else{
			if(newmsg){
				rptc=0
				state['rptCount']=rptc
				rptn=settings['repeatTimes'] ? (Integer)settings['repeatTimes'] : 1
				state['rptNum']=rptn
				state[sRPT]=true
				doRefresh=false
				logTrace "Starting repeating alerts."
			}else{
				if(bIs(st,sRPT)){
					logTrace "Sending repeat message"
					alertNow(0, msg, true)
				}
			}
			if(bIs(st,sRPT) && rptc < rptn){
				rptc++
				state['rptCount']=rptc
				logTrace "Scheduling repeating alert in ${rptm} minute(s). This is ${rptc}/${rptn} repeated alert(s). Repeat State: ${st[sRPT]}"
				runIn(rptm*60,repeatNow)
			}
		}
	}else{
		logTrace "Repeat not enabled or no message Enabled: ${repYes} msg: $msg State: ${st[sRPT]} Minutes: ${rptm}"
		//match state
		state[sRPT]=false
		state[sRPTMSG]=sNULL
	}
	if(doRefresh) runIn(1,callRefreshTile)
}

@Field volatile static Map<String,List<Map>> ListofAlertsFLD=[:]

void issueGetAlertMsg(){
	Map result=getResponseURL(true)
}

void getAlertMsgSync(){
	Map result=getResponseURL(false)
	finishAlertMsg(result)
	walertCheck()
}

static Map calculateEnd(Map featProp){
	String alertexpires,t
	alertexpires= sNULL
	Boolean replacedAt, useEnds
	replacedAt=false
	useEnds=false
	Date enddt; enddt=null
	t=sMs(featProp,'ends')
	if(t){
		alertexpires=t
		enddt=parseDt(t)
		useEnds=true
	}
	t=sMs(featProp,'replacedAt')
	if(t){
		Date tdt= parseDt(t)
		if(enddt){
			if(tdt.getTime() < enddt.getTime()){ enddt= tdt; alertexpires=t; replacedAt=true; useEnds=false }
		}else{
			enddt= tdt; alertexpires=t; replacedAt=true; useEnds=false
		}
	}
	t=sMs(featProp,'expires')
	if(t){
		Date tdt= parseDt(t)
		if(enddt){
			if(tdt.getTime() < enddt.getTime()){ enddt= tdt; alertexpires=t; useEnds=false; replacedAt=false }
		}else{
			enddt= tdt; alertexpires=t; useEnds=false; replacedAt=false
		}
	}
	return [ enddt: enddt, alertexpires: alertexpires, useEnds: useEnds, replacedAt: replacedAt ]
}

void finishAlertMsg(Map result){
	List<Map> ListofAlerts; ListofAlerts=[]
	List<Map> expireList; expireList=[]
	Map set= gtSettings()

	List<Map> mListofAlertsFLD
	mListofAlertsFLD= initListofAlerts()

	Boolean hadAlerts= mListofAlertsFLD.size()>0

	if(result){
		String myId=app.getId()
		Boolean IsnewList; IsnewList=false
		Date dt1=new Date()
		String timestamp=dt1.format("yyyy-MM-dd'T'HH:mm:ssXXX")

		Integer i
		for(i=0; i<((List)result.features).size();i++){
			Map msgMap; msgMap=null
//			debug=true
//			alertmsg=[]

			Map<String,Map> feat=(Map<String,Map>)((List)result.features)[i]
			Map featProp= feat.properties

			//alert expiration
			Map res= calculateEnd(featProp)
			String alertexpires=sMs(res,'alertexpires')
			Date enddt; enddt=(Date)res.enddt
			Boolean replacedAt= bIs(res,'replacedAt')
			Boolean useEnds= bIs(res,'useEnds')

			Boolean expired; expired=false

			//if specific weatheralerts is chosen
			List<String> t0=(List<String>)settings['myWeatherAlert']
			if(!t0) msgMap=buildAlertMap(feat)
			else if(t0.contains(sMs(featProp,'event'))) msgMap=buildAlertMap(feat)

			//if alert has expired ignore alert
			if(dt1.getTime() > enddt.getTime()){ expired=true }
			logTrace "filtered: ${msgMap==null ? sTRUE : sFALSE} expired: ${expired}  alertexpires ${alertexpires}  replacedAt: $replacedAt useEnds: $useEnds    now: ${timestamp}  ${featProp['severity']}  ${featProp['event']}"

			if(msgMap!=null){
				Boolean useexpired= bIs(set,'debug')
				String alertid= 'alertid'
				if(!expired || useexpired){
					Boolean isNewNotice; isNewNotice=false
					if(mListofAlertsFLD.size() > 0){
						Map fndMsg= mListofAlertsFLD.find{ Map it -> sMs(it,alertid).contains(sMs(msgMap,alertid)) }
						if(fndMsg){ msgMap=fndMsg}
						else isNewNotice=true
					} else{
						isNewNotice=true
						IsnewList=true
					}

					String mmalertid= sMs(msgMap,alertid)
					if(expired && useexpired) logDebug "Using expired alerts"
					if(isNewNotice) logDebug "Valid ${mmalertid} is new in ListofAlerts: ${IsnewList}"
					else logDebug "Valid ${mmalertid} exists in ListofAlerts"

					if(isNewNotice){ msgMap['alertPushed']=false; msgMap['alertAnnounced']=false}
					msgMap['expired']=expired
					ListofAlerts << msgMap
				} else{
					msgMap['expired']=expired
					msgMap['alertPushed']=true
					msgMap['alertAnnounced']=true
					expireList << msgMap
				}
			} // skipped the message due to filter
		} //end of for statement

//ERS
		mListofAlertsFLD=ListofAlerts
		state[sLISTOFALRTS]=ListofAlerts
		ListofAlertsFLD[myId]=mListofAlertsFLD
		ListofAlertsFLD=ListofAlertsFLD

		if(ListofAlerts){
			logDebug "ListofAlerts is (${ListofAlerts.size()}) ${ListofAlerts}"
		} else{ state.remove(sLISTOFALRTS); state.remove('alertAnnounced')}

		if(mListofAlertsFLD){
			Boolean fixedRepeat; fixedRepeat=false
			Boolean schedTile; schedTile=false
			Integer y
			for(y=0;y<mListofAlertsFLD.size();y++){
				Map msgMap= mListofAlertsFLD[y]
				if(msgMap && !bIs(msgMap,'expired')){
					String almsg= sMs(msgMap,'alertmsg')
					if(!bIs(msgMap,'alertAnnounced') || !bIs(msgMap,'alertPushed')){
						Boolean everDid= bIs(msgMap,'alertAnnounced') || bIs(msgMap,'alertPushed')
						alertNow(y, almsg, false, msgMap)
						//msgMap['alertPushed']=true
						//msgMap['alertAnnounced']=true
						mListofAlertsFLD[y]=msgMap
						ListofAlertsFLD[myId]=mListofAlertsFLD
						ListofAlertsFLD=ListofAlertsFLD
						state[sLISTOFALRTS]=mListofAlertsFLD
						if(!everDid && bIs(set,sRPTYES) && !fixedRepeat){
							fixedRepeat=true
							state[sRPTMSG]=almsg
							repeatNow(true)
						}
						schedTile=true
					}
					if(bIs(set,sRPTYES) && !fixedRepeat){
						fixedRepeat=true
						if(sMs(gtSt(),sRPTMSG) != almsg) schedTile=true
						state[sRPTMSG]=almsg // in case messages moved around in order
					}
				}
			}
			if(schedTile){
				runIn(1,callRefreshTile)
			}
		} else logDebug "No new alerts.  Waiting ${sMs(set,sWHATPOLL).toInteger()} minute(s) before next poll..."
	} else logDebug "null result..."

	if(result!=null){ // deal with network outage; don't drop alerts.
		if(!ListofAlerts){
			if(hadAlerts) logDebug "ending alerts"
			def dev=set[sALRTSW]
			Boolean usealsw= bIs(set,sUSEALRTSW) && dev
			if(usealsw && dev.currentState(sSW).value==sON) alertNow(null, sNULL, false) // maybe Switch.off()
			if(bIs(gtSt(),sRPT))
				unschedule(repeatNow)
			runIn(1,callRefreshTile)
			state[sRPT]=false
			state[sRPTMSG]=sNULL
		}
	}
}

@CompileStatic
Map buildAlertMap(Map<String,Map> result){
	//build new entry for map

	Map resultProp= result.properties
	Map res= calculateEnd(resultProp)
	String alertexpires=sMs(res,'alertexpires')
	//Boolean replacedAt= bIs(res,'replacedAt')
	//Boolean useEnds= bIs(res,'useEnds')
	Date enddt; enddt=(Date)res.enddt

/*	if(resultProp.replacedAt)alertexpires=(String)resultProp.replacedAt
	else if(resultProp.ends) alertexpires=(String)resultProp.ends
	else alertexpires=(String)resultProp.expires */
	String alertarea
	alertarea=sMs(resultProp,'areaDesc')
	alertarea=alertRemoveStates(alertarea)
	alertarea=alertFormatArea(alertarea)
	String alertheadline
	alertheadline=sMs(resultProp,'headline')
	alertheadline=alertFormatStates(alertheadline)
	alertheadline=alertRemoveTimeZone(alertheadline)
	alertheadline=alertFormatText(alertheadline)
	String alertdescription
	alertdescription=sMs(resultProp,sDESC)
	alertdescription=alertFormatStates(alertdescription)
	alertdescription=alertRemoveTimeZone(alertdescription)
	alertdescription=alertFormatText(alertdescription)
	String alertinstruction
	String t
	t=sMs(resultProp,'instruction')
	if(t==sNULL) alertinstruction=alertdescription
	else{
		alertinstruction=t
		alertinstruction=alertFormatStates(alertinstruction)
		alertinstruction=alertRemoveTimeZone(alertinstruction)
		alertinstruction=alertFormatText(alertinstruction)
	}
	String alertmsg
	Map set= gtSettings()
	alertmsg= sMs(set,'alertCustomMsg')
	try{alertmsg=alertmsg.replace('{alertarea}',"${alertarea}")}
		catch (ignored){}
	try{alertmsg=alertmsg.replace('{alertseverity}',"${resultProp['severity']}")}
		catch (ignored){}
	try{alertmsg=alertmsg.replace('{alertcertainty}',"${resultProp['certainty']}")}
		catch (ignored){}
	try{alertmsg=alertmsg.replace('{alerturgency}',"${resultProp['urgency']}")}
		catch (ignored){}
	try{alertmsg=alertmsg.replace('{alertheadline}',"${alertheadline}")}
		catch (ignored){}
	try{alertmsg=alertmsg.replace('{alertdescription}',"${alertdescription}")}
		catch (ignored){}
	try{alertmsg=alertmsg.replace('{alertinstruction}',"${alertinstruction}")}
		catch (ignored){}
	try{alertmsg=alertmsg.replace('{alertevent}',"${resultProp['event']}")}
		catch (ignored){}
	try{alertmsg=alertmsg.replaceAll("\n"," ")}
		catch (ignored){}
	try{alertmsg=alertmsg.trim().replaceAll("[ ]{2,}", ", ")}
		catch (ignored){}
	alertmsg=alertmsg.replaceAll("\\s+", " ")

	return [
			alertid:resultProp.id, alertseverity:resultProp['severity'], alertarea:alertarea,
			alertsent:resultProp.sent, alerteffective:resultProp.effective, alertonset:resultProp.onset,
			alertexpires:alertexpires, alertstatus:resultProp.status, alertmessagetype:resultProp.messageType,
			alertcategory:resultProp['category'], alertcertainty:resultProp['certainty'], alerturgency:resultProp['urgency'],
			alertsendername:resultProp.senderName, alertheadline:alertheadline, alertdescription:alertdescription,
			alertinstruction:alertinstruction, alertevent:resultProp['event'], 'alertmsg':alertmsg
	]
}

static String alertFormatStates(String imsg){
	String msg; msg=imsg
	if(msg){
		msg=msg.replaceAll("/AL/", "Alabama")
		msg=msg.replaceAll("/AK/", "Alaska")
		msg=msg.replaceAll("/AZ/", "Arizona")
		msg=msg.replaceAll("/AR/", "Arkansas")
		msg=msg.replaceAll("/CA/", "California")
		msg=msg.replaceAll("/CO/", "Colorado")
		msg=msg.replaceAll("/CT/", "Connecticut")
		msg=msg.replaceAll("/DE/", "Delaware")
		msg=msg.replaceAll("/FL/", "Florida")
		msg=msg.replaceAll("/GA/", "Georgia")
		msg=msg.replaceAll("/HI/", "Hawaii")
		msg=msg.replaceAll("/ID/", "Idaho")
		msg=msg.replaceAll("/IL/", "Illinois")
		msg=msg.replaceAll("/IN/", "Indiana")
		msg=msg.replaceAll("/IA/", "Iowa")
		msg=msg.replaceAll("/KS/", "Kansas")
		msg=msg.replaceAll("/KY/", "Kentucky")
		msg=msg.replaceAll("/LA/", "Louisiana")
		msg=msg.replaceAll("/ME/", "Maine")
		msg=msg.replaceAll("/MA/", "Massachusetts")
		msg=msg.replaceAll("/MD/", "Maryland")
		msg=msg.replaceAll("/MI/", "Michigan")
		msg=msg.replaceAll("/MN/", "Minnesota")
		msg=msg.replaceAll("/MS/", "Mississippi")
		msg=msg.replaceAll("/MO/", "Missouri")
		msg=msg.replaceAll("/MT/", "Montana")
		msg=msg.replaceAll("/NE/", "Nebraska")
		msg=msg.replaceAll("/NV/", "Nevada")
		msg=msg.replaceAll("/NH/", "New Hampshire")
		msg=msg.replaceAll("/NJ/", "New Jersey")
		msg=msg.replaceAll("/NM/", "New Mexico")
		msg=msg.replaceAll("/NY/", "New York")
		msg=msg.replaceAll("/NC/", "North Carolina")
		msg=msg.replaceAll("/ND/", "North Dakota")
		msg=msg.replaceAll("/OH/", "Ohio")
		msg=msg.replaceAll("/OK/", "Oklahoma")
		msg=msg.replaceAll("/OR/", "Oregon")
		msg=msg.replaceAll("/PA/", "Pennsylvania")
		msg=msg.replaceAll("/RI/", "Rhode Island")
		msg=msg.replaceAll("/SC/", "South Carolina")
		msg=msg.replaceAll("/SD/", "South Dakota")
		msg=msg.replaceAll("/TN/", "Tennessee")
		msg=msg.replaceAll("/TX/", "Texas")
		msg=msg.replaceAll("/UT/", "Utah")
		msg=msg.replaceAll("/VT/", "Vermont")
		msg=msg.replaceAll("/VA/", "Virginia")
		msg=msg.replaceAll("/WA/", "Washington")
		msg=msg.replaceAll("/WV/", "West Virginia")
		msg=msg.replaceAll("/WI/", "Wisconsin")
		msg=msg.replaceAll("/WY/", "Wyoming")
	}
	return msg
}

@CompileStatic
static String alertRemoveTimeZone(String imsg){
	// Remove Timezones
	String msg=imsg
	if(msg){
		return msg.replaceAll(/(AST|EST|EDT|CST|CDT|MST|MDT|PST|PDT|AKST|AKDT|HST|HAST|HADT)/,sBLANK)
	}
	return sBLANK
}

@CompileStatic
static String alertRemoveStates(String imsg){
	String msg=imsg
	if(msg){
		return msg.replaceAll(/(AL|AK|AZ|AR|CA|CO|CT|DE|FL|GA|HI|ID|IL|IA|IN|KS|KY|LA|ME|MA|MD|MI|MN|MS|MO|MT|NE|NV|NH|NJ|NM|NY|NC|ND|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI|WY)/, sBLANK)
	}
	return sBLANK
}

@CompileStatic
static String alertFormatText(String imsg){
	String msg; msg=imsg
	if(msg){
		msg=msg.replaceAll(/NWS/, "the National Weather Service of")
		msg=msg.replaceAll(/(WHAT|WHEN|IMPACTS|IMPACT|WHERE|INCLUDES|HAZARDS|INCLUDE|HAZARD|TEMPERATURE|SOURCE)/, sBLANK)
		msg=msg.replaceAll(/\.{2,}/, sBLANK)
		msg=msg.replaceAll(/\*/, sBLANK)
		msg=msg.replaceAll(/MPH/, "miles per hour")
		msg=msg.replaceAll("", "")
		msg=msg.replaceAll("\n", sSPACE)
		msg=msg.replaceAll("\\s+", sSPACE)
		msg=msg.replaceAll(/(?:(\d{2})(\d{2}))|(?:(\d(?!\d{3}))(\d{2}))(?=\s?(?i:am|pm))/, '$1$3:$2$4')
	}
	return msg
}

@CompileStatic
static String alertFormatArea(String imsg){
	String msg; msg=imsg
	if(msg){
		msg.replaceAll(/NWS/, "the National Weather Service of")
		msg=msg.replaceAll(", ", sBLANK)
		msg=msg.replaceAll(",", sBLANK)
		msg=msg.replaceAll(";", ",")
		msg=msg.replaceAll("\n", " ")
		msg=msg.replaceAll("\\s+", sSPACE)
		msg=msg.replaceAll("/", sBLANK)
		StringBuffer buffer=new StringBuffer(msg)
		msg=buffer.reverse().toString().replaceFirst(",", "dna ")
		msg=new StringBuffer(msg).reverse().toString()
		msg=msg+"."
	}
	return msg
}

//Test Alert Section
void runtestAlert(){
	atomicState.testmsg=true
	Integer endTime; endTime=30
	logInfo "Initiating a test alert."
	String msg=buildTestAlert()
	state[sRPTMSG]=msg
	alertNow(null, msg, false)
	if((Boolean)settings[sRPTYES]){
		logInfo "Initiating a repeat process (ends in 5 minutes) for test alert."
		state[sRPT]=false
		repeatNow(true)
		endTime=300
	} else runIn(1,callRefreshTile)
	runIn(endTime,endTest)
}

void endTest(){
	logInfo "Ending repeat for test alert."
	atomicState.testmsg=false
	Map set= gtSettings()
	def dev=set[sALRTSW]
	Boolean usealsw= bIs(set,sUSEALRTSW) && dev
	if(usealsw && dev.currentState(sSW).value==sON) alertNow(null, sNULL, false) // maybe Switch.off()
	state[sRPT]=false
	state[sRPTMSG]=sNULL
	unschedule(repeatNow)
	runIn(1,callRefreshTile)
}

String buildTestAlert(){
	Date dt1=new Date()
	String timestamp=dt1.format("yyyy-MM-dd'T'HH:mm:ssXXX")
	Map<String,Map> res =[
		properties:[
			id: 'test01',
			'sent': timestamp,
			'end': timestamp,
			'status': 'test',
			'areaDesc': 'Springfield County.',
			'headline': 'The founder, Jedidiah Springfield has spotted a cloud above the nuclear power plant towers.',
			(sDESC):'The founder, Jedidiah Springfield has spotted a cloud above the nuclear power plant towers.  Expect heavy pollution, possible fish with three eyes, and a Simpson asleep at the console.  Also a notorious yellow haired boy is terrorizing animals with spit wads.  Be on the look out for suspicious activity.',
			'instruction': 'Expect heavy pollution, possible fish with three eyes, and a Simpson asleep at the console.',
			'severity': 'Severe',
			'certainty': 'Likely',
			'urgency': 'Immediate',
			'event': 'Nuclear Power Plant Warning'
		]
	]
	Map r= buildAlertMap(res)
	String alertmsg = r['alertmsg']
	return alertmsg
}

// Common Notification Routines
void talkNow(String ialertmsg, Boolean repeatCheck){
	Map set= gtSettings()
	String alertmsg; alertmsg=ialertmsg
	Boolean useintro= bIs(set,'useAlertIntro')
	String ms= sMs(set,'AlertIntro')
	Integer speakervolume= (Integer)settings['speakervolume']
	Integer speakervolRestore= (Integer)settings['speakervolRestore']
	if(repeatCheck){
		if(useintro) alertmsg="Repeating previous alert,, ${ms} "+alertmsg
		else alertmsg="Repeating previous alert,,"+alertmsg
	}else if(useintro) alertmsg="${ms}, "+alertmsg

	if(bIs(set,sMUSICMD)){
		List mspks=(List)settings['musicspeaker']
		logDebug "Using audioNotification $mspks"
		Boolean okT; okT=false
		if(mspks && mspks[0].hasCommand('playTextAndRestore')){
			try{
				mspks*.playTextAndRestore(alertmsg.toLowerCase(), speakervolume)
				okT=true
				logInfo "Sending alert to audioNotification Speaker(s)."
			}
			catch (ignored){}
		}
		if(!okT){ logWarn "audioNotificcation device(s) ${mspks} has not been selected or does not support playTextAndRestore command."}
	}

	if(bIs(set,sECHOSPS2)){
		List spks=(List)settings['echospeaker']
		logDebug "Using echoSpeaks ${spks}"
		Boolean supportsSetVolume; supportsSetVolume=false
		List<String> msgs=[]
		List svVols=[]
		Boolean canPlay=spks[0].hasCommand('playAnnouncement')
		if(speakervolume && canPlay && spks && spks[0].hasCommand('setVolume')){
			if(!speakervolRestore){
				spks.each{ dev ->
					def a=dev.currentState('volume')?.value
					svVols.push(a)
				}
			}
			try{
				spks*.setVolume(speakervolume)
				supportsSetVolume=true
				msgs.push("Setting Echo Speaker to volume level: ${speakervolume}".toString())
			} catch (e){ logError "unable to set volume", e}
		}
		Boolean okT; okT=false
		if(spks && canPlay){
			try{
				String tt; tt="serial"
				if(alertmsg.size() < 420){
					if(spks.size() > 1 && spks[0].hasCommand('parallelPlayAnnouncement')){
						spks*.parallelPlayAnnouncement(alertmsg.toLowerCase(), 'NOAA Weather Alert')
						tt="parallel"
						spks[0].noOp()
					} else spks*.playAnnouncement(alertmsg.toLowerCase(), 'NOAA Weather Alert', null, null)
					tt += " announce"
				} else{
					tt += " speak"
					spks*.speak(alertmsg)
				}

				okT=true
				msgs.push("Sending ${tt} alert to EchoSpeaks device(s).".toString())
				if(supportsSetVolume){
					if(speakervolRestore){
						spks*.setVolume(speakervolRestore)
						msgs.push("Restoring Speaker to volume level: ${speakervolRestore}".toString())
					} else{
						Integer i; i=0
						spks.each{ dev ->
							def a=svVols[i]
							try{
								msgs.push("Restoring Speaker $dev to volume level: ${a}".toString())
								dev.setVolume(a)
							} catch (ignored){ msgs.push("Echo ${i} does not support restore volume command".toString())}
							i+=1
						}
					}
				}
			}
			catch (ignored){}
		}
		if(msgs.size() > 0) msgs.each{ String msg -> logDebug msg}
		if(!okT){ logWarn "echospeaks device(s) ${spks} has not been selected or does not support playAnnouncement command."}
	}

	if(bIs(set,sSPEECHMD)){
		List spks=(List)settings['speechspeaker']
		List<String> msgs=[]
		Boolean addDel=bIs(set,'speechdelay')
		logDebug "Using speechSynthesis $spks, add delays: ${addDel}"
		Boolean okT; okT=false
		Boolean canSpeak=spks[0].hasCommand('speak')
		if(canSpeak && spks && spks[0].hasCommand('initialize')){
			try{
				spks*.initialize()
				msgs.push("Initializing Speech Speaker")
				okT=true
				if(addDel) pauseExecution(2500)
			}
			catch (e){ logError "initialize command failed", e}
			if(!okT){ msgs.push("Speech device(s) ${spks} has not been selected or does not support initialize command.".toString())}
		}

		Boolean supportsSetVolume; supportsSetVolume=false
		List svVols=[]
		if(canSpeak && speakervolume && spks && spks[0].hasCommand('setVolume')){
			if(!speakervolRestore){
				spks.each{ dev ->
					def a=dev.currentState('volume')?.value
					svVols.push(a)
				}
			}
			try{
				msgs.push("Setting Speech Speaker to volume level: ${speakervolume}".toString())
				spks*.setVolume(speakervolume)
				supportsSetVolume=true
				if(addDel) pauseExecution(2000)
			}
			catch (e){ logError "unable to set volume", e}

			if(!supportsSetVolume && speakervolume){ msgs.push("Speech device(s) ${spks} has not been selected or does not support setVolume command.".toString())}
		}

		if(spks && canSpeak){
			okT=false
			alertmsg=alertmsg.toLowerCase()
			try{
				String tt; tt="serial"
				if(spks && spks.size() > 1 && spks[0].hasCommand('parallelSpeak') && alertmsg.size() < 420){
					spks*.parallelSpeak(alertmsg)
					tt="parallel"
					spks[0].noOp()
				} else spks*.speak(alertmsg)

				okT=true
				msgs.push("Sending ${tt} alert to Speech Speaker(s)".toString())

				if(supportsSetVolume){
					Integer speechDuration=Math.max(Math.round(alertmsg.length()/14).toInteger(),2)+1
					Long speechDuration2=speechDuration * 1000L
					if(addDel) pauseExecution(speechDuration2)
					if(speakervolRestore){
						spks*.setVolume(speakervolRestore)
						msgs.push("Restoring Speech Speaker to volume level: ${speakervolRestore}".toString())
					} else{
						Integer i; i=0
						spks.each{ dev ->
							def a=svVols[i]
							try{
								dev.setVolume(a)
								msgs.push("Restoring Speech Speaker $dev to volume level: ${a}".toString())
							} catch (ignored){ msgs.push("Speech speaker ${i} doesn't support restore volume command".toString())}
							i+=1
						}
					}
					//if(addDel) pauseExecution(1000)
				}
			} catch (ignored){}
		}
		if(msgs.size() > 0) msgs.each{ String msg -> logDebug msg}
		if(!okT){ logWarn "Speech device(s) ${spks} has not been selected or does not support speak command."}
	}
}

void pushNow(String ialertmsg, Boolean repeatCheck){
	Map set= gtSettings()
	String alertmsg; alertmsg=ialertmsg
	List pdev= (List)set['pushoverdevice']
	if(bIs(set,sPSHTTS) && pdev){
		logInfo "Sending Pushover message."
		if(repeatCheck){
			if((Integer)settings['repeatTimes']>1) alertmsg="[Alert Repeat ${state['rptCount']}/${state['rptNum']}] "+alertmsg
			else alertmsg="[Alert Repeat] "+alertmsg
		}

		List<String> fullalert=[]
		//String m1=alertmsg.replaceAll(/(\r\n|\r|\n|\\r\\n|\\r|\\n)+/, " ")
		String m1=alertmsg.replaceAll(/\s\s+/, " ")
		List<String> subMsg=m1.tokenize()
		Integer lsiz=subMsg.size()
		Integer a,i
		a=0
		i=0
		while (i<lsiz){
			String nextpart; nextpart=sBLANK
			while (nextpart.size() < 1000 && i < lsiz){
				nextpart += subMsg[i]+sSPACE
				i+=1
			}
			fullalert[a]=nextpart
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
/*		String m1=alertmsg.replaceAll(/(\r\n|\r|\n|\\r\\n|\\r|\\n)+/, " ")
		def m=m1 =~ /(.|[\r\n]){1,1023}\W/
		while(m.find()){
			fullalert << m.group()
		}*/

		Integer x
		for(x=0;x<fullalert.size();x++){
			if(fullalert.size()>1) pdev*.deviceNotification("(${x+1}/${fullalert.size()}) "+fullalert[x])
			else pdev*.deviceNotification(fullalert[x])
			//pauseExecution(1000)
		}
	}
}

List<Map> getTile(){
	Map set= gtSettings()
	Map st= gtSt()
	List<Map> mListofAlertsFLD= initListofAlerts()
	List<Map> msg; msg=[]
	if(!bIs(set,'disableTile')){
		try{
			if(bIs(st,'testmsg')){
				msg << ['alertmsg':sMs(st,sRPTMSG)]
			}else{
				if(mListofAlertsFLD){
					Integer x
					for(x=0;x<mListofAlertsFLD.size();x++){
						if(msg.toString().length() < 100000){
							Map m= mListofAlertsFLD[x]
							if(!bIs(m,'expired')) msg << ['alertmsg':m['alertmsg']]
						}
					}
				}
			}
			if(!msg) logDebug "No data for tile display."
		}
		catch (ignored){}
	} else logDebug "Tile display is disabled."
	if(msg) logDebug "Creating data information for tile display."
	return msg
}

void buildEventsListFrc(){
	buildEventsList(true)
}

@Field static final List<String> eventListFLD=[
"911 Telephone Outage Emergency", "Administrative Message", "Air Quality Alert", "Air Stagnation Advisory", "Arroyo And Small Stream Flood Advisory", "Ashfall Advisory", "Ashfall Warning", "Avalanche Advisory", "Avalanche Warning", "Avalanche Watch", "Beach Hazards Statement", "Blizzard Warning", "Blizzard Watch", "Blowing Dust Advisory", "Blowing Dust Warning", "Brisk Wind Advisory", "Child Abduction Emergency", "Civil Danger Warning", "Civil Emergency Message", "Coastal Flood Advisory", "Coastal Flood Statement", "Coastal Flood Warning", "Coastal Flood Watch", "Dense Fog Advisory", "Dense Smoke Advisory", "Dust Advisory", "Dust Storm Warning", "Earthquake Warning", "Evacuation - Immediate", "Excessive Heat Warning", "Excessive Heat Watch", "Extreme Cold Warning", "Extreme Cold Watch", "Extreme Fire Danger", "Extreme Wind Warning", "Fire Warning",
"Fire Weather Watch", "Flash Flood Statement", "Flash Flood Warning", "Flash Flood Watch", "Flood Advisory", "Flood Statement", "Flood Warning", "Flood Watch", "Freeze Warning", "Freeze Watch", "Freezing Fog Advisory", "Freezing Rain Advisory", "Freezing Spray Advisory", "Frost Advisory", "Gale Warning", "Gale Watch", "Hard Freeze Warning", "Hard Freeze Watch", "Hazardous Materials Warning", "Hazardous Seas Warning", "Hazardous Seas Watch", "Hazardous Weather Outlook", "Heat Advisory", "Heavy Freezing Spray Warning", "Heavy Freezing Spray Watch", "High Surf Advisory", "High Surf Warning", "High Wind Warning", "High Wind Watch", "Hurricane Force Wind Warning", "Hurricane Force Wind Watch", "Hurricane Local Statement", "Hurricane Warning", "Hurricane Watch", "Hydrologic Advisory", "Hydrologic Outlook", "Ice Storm Warning", "Lake Effect Snow Advisory", "Lake Effect Snow Warning", "Lake Effect Snow Watch", "Lake Wind Advisory", "Lakeshore Flood Advisory", "Lakeshore Flood Statement", "Lakeshore Flood Warning",
"Lakeshore Flood Watch", "Law Enforcement Warning", "Local Area Emergency", "Low Water Advisory", "Marine Weather Statement", "Nuclear Power Plant Warning", "Radiological Hazard Warning", "Red Flag Warning", "Rip Current Statement", "Severe Thunderstorm Warning", "Severe Thunderstorm Watch", "Severe Weather Statement", "Shelter In Place Warning", "Short Term Forecast", "Small Craft Advisory", "Small Craft Advisory For Hazardous Seas", "Small Craft Advisory For Rough Bar", "Small Craft Advisory For Winds", "Small Stream Flood Advisory", "Snow Squall Warning", "Special Marine Warning", "Special Weather Statement", "Storm Surge Warning", "Storm Surge Watch", "Storm Warning", "Storm Watch", "Test", "Tornado Warning", "Tornado Watch", "Tropical Depression Local Statement", "Tropical Storm Local Statement", "Tropical Storm Warning",
"Tropical Storm Watch", "Tsunami Advisory", "Tsunami Warning", "Tsunami Watch", "Typhoon Local Statement", "Typhoon Warning", "Typhoon Watch", "Urban And Small Stream Flood Advisory", "Volcano Warning", "Wind Advisory", "Wind Chill Advisory", "Wind Chill Warning", "Wind Chill Watch", "Winter Storm Warning", "Winter Storm Watch", "Winter Weather Advisory"
]

void buildEventsList(Boolean frc=false){
	List et; et=(List)state[sEVTTYPS]
	if(frc || et==null || et==[]){
		Map results=getResponseEvents()
		if(results){
			et= (List)results[sEVTTYPS]
			state[sEVTTYPS]=et
			logDebug "Acquired current events list from api.weather.gov"
		}
	}
	if(et==null || et==[])
		state[sEVTTYPS]=eventListFLD
}

// Device creation and status updhandlers
void createChildDevices(){
	try{
		String t=app.id.toString()
		if(!getChildDevice(sNOAA) && !(getChildDevice(sNOAA+t))){
			logInfo "Creating device: NOAA Tile"
			addChildDevice("aaronward", "NOAA Tile", sNOAA+t, [(sNM): "NOAA Tile "+t, isComponent: false])
		}
	}
	catch(e){ logError("Couldn't create child device.", e)}
}

void cleanupChildDevices(){
	try{
		for(device in getChildDevices()) deleteChildDevice(device.deviceNetworkId)
	}
	catch (e){ logError("Couldn't clean up child devices.", e)}
}

// Application Support Routines
Map getResponseURL(Boolean async=false){
	Map set= gtSettings()
	// Determine if custom coordinates have been selected
	String latitude
	String longitude
	if(bIs(set,'useCustomCords')){
		latitude="${set['customlatitude']}".toString()
		longitude="${set['customlongitude']}".toString()
	}else{
		latitude="${location.latitude}".toString()
		longitude="${location.longitude}".toString()
	}

	String wxURI
	wxURI="https://api.weather.gov/alerts?point=${latitude}%2C${longitude}&status=actual&message_type=alert,update".toString()
	Map result; result=null

	// Build out the API options
	List<String> ulst=checkCap((List<String>)set['whatAlertUrgency'])
	if(ulst) wxURI=wxURI+"&urgency=${ulst.join(",")}".toString()

	List<String> sevlst
	def was= set['whatAlertSeverity']
	sevlst= was instanceof String ? [ (String)was ] : (List<String>)was
	if(!sevlst) sevlst=['severe']
	sevlst=checkCap(sevlst)
	wxURI=wxURI+"&severity=${sevlst.join(",")}".toString()

	List<String> alst=checkCap((List<String>)set['whatAlertCertainty'])
	if(alst) wxURI=wxURI+"&certainty=${alst.join(",")}".toString()

	state['wxURI']=wxURI
	logDebug "URI: <a href='${wxURI}' target=_blank>${wxURI}</a>"

	logInfo "Connecting to weather.gov service."
	Map requestParams =	[
		uri: wxURI,
		requestContentType: "application/json",
		contentType: "application/json",
		timeout: 20
	]

	if(!async){
		try{
			httpGet(requestParams)	{ response ->
				result=response.data
				Integer responseCode=response.status
				if( !(responseCode>=200 && responseCode<300 && result) ){
					logWarn "The API Weather.gov did not return a valid response for ${wxURI} $responseCode."
				}
			}
		}
		catch (e){ logError "The API Weather.gov did not return a response for ${wxURI}.",e}
		return result
	} else{
		try{
			asynchttpGet('ahttpreq', requestParams, [command: 'a'])
			return [async:true]
		}
		catch (e){ logError "Async http failed for ${wxURI}.", e}
		return result
	}
}

List checkCap(List<String> sevlst){
	if(sevlst && (Boolean)settings['capitalizeAlertSeverity']){
		List<String> nlst=[]
		sevlst.each{ String ss ->
			nlst.push(ss.capitalize())
		}
		return nlst
	}
	return sevlst
}

void ahttpreq(resp, Map cbD){
	Boolean ok; ok=false
	Map data; data=null
	Integer responseCode
	try{
		//def t0=resp.getHeaders()
		responseCode=resp.status
		if(responseCode>=200 && responseCode<300){
			String rdata=resp.data
			if(rdata){
				//logInfo "http result: data is not a map."
				try{
					data= (Map)new JsonSlurper().parseText(rdata)
				}catch (ignored){
					logInfo "http result: parsing json failed."
					data=resp.data
				}
				ok=true
			}

		} else logWarn "The API Weather.gov did not return a successful response $responseCode message ${resp.getErrorMessage()}."
	} catch(e){ logError "The API Weather.gov did not return a response. (exception)", e}
	if(ok) finishAlertMsg(data)
	walertCheck()
}

Map getResponseEvents(){
	String wxURI="https://api.weather.gov/alerts/types"
	Map result; result=null
	Map requestParams =	[
		uri: wxURI,
		requestContentType: "application/json",
		contentType: "application/json",
		timeout: 20
	]

	try{
		httpGet(requestParams){ response ->
			result=response.data
			Integer responseCode=response.status
			if( !(responseCode>=200 && responseCode<300 && result) ){
				logWarn "The API Weather.gov get types did not return a response for ${wxURI} ${responseCode}."
			}
		}
	}
	catch (e){ logError "The API Weather.gov get types did not return a response for ${wxURI}.", e}
	return result
}

void checkState(){
	Map set= gtSettings()
	atomicState.testmsg=false
	if(!oMs(set,sWHATPOLL)) app.updateSetting(sWHATPOLL,[(sVL):'5',(sTY):sENUM])
	if(oMs(set,sLOGINFO)==null) app.updateSetting(sLOGINFO, [(sVL):sTRUE, (sTY):sBOOL])
	if(oMs(set,sLOGWRN)==null) app.updateSetting(sLOGWRN, [(sVL):sTRUE, (sTY):sBOOL])
	if(oMs(set,sLOGERR)==null) app.updateSetting(sLOGERR, [(sVL):sTRUE, (sTY):sBOOL])
	if(oMs(set,sLOGDEBUG)==null) app.updateSetting(sLOGDEBUG, [(sVL):sFALSE, (sTY):sBOOL])
	if(oMs(set,sLOGTRC)==null) app.updateSetting(sLOGTRC, [(sVL):sFALSE, (sTY):sBOOL])
//	if(settings.logEnable==null) app.updateSetting('logEnable',[(sVL):sFALSE,(sTY):sBOOL])
	String t
	t='logMinutes'
	if(set[t]==null) app.updateSetting(t,[(sVL):15,(sTY):sNUMBER])
	t='whatAlertSeverity'
	if(!(List)set[t]) app.updateSetting(t,[(sVL):['severe'],(sTY):sENUM])
	t='alertCustomMsg'
	if(sMs(set,t)==sNULL) app.updateSetting(t,[(sVL):custDefFLD,(sTY):sTXT])
	app.removeSetting('whatCustomMsg')

	Integer t0
	t='repeatTimes'
	if(set[t]){
		t0=(Integer)set[t]
	}else{
		app.updateSetting(t,[(sVL):1,(sTY):sNUMBER])
		t0=1
	}
	state['rptNum']=t0

	t='repeatMinutes'
	if(set[t]==null || set[t] < 0){
		app.updateSetting(t,[(sVL):15,(sTY):sNUMBER])
	}

	state['rptCount']=0
	if(!(Boolean)state[sRPT]) state[sRPTMSG]=sNULL
}

void installCheck(){
	state.appInstalled=app.getInstallationState()
	if(sMs((Map)state,'appInstalled')!='COMPLETE'){
		section{paragraph "Please hit 'Done' to install ${app.label} "}
	}
}

@SuppressWarnings('GroovyFallthrough')
void initialize(){
	buildEventsListFrc()
	checkState()
	unschedule()
	createChildDevices()
	state[sRPT]=false
	state[sRPTMSG]=sNULL
	String myId=app.getId()
	ListofAlertsFLD[myId]=[]
	ListofAlertsFLD=ListofAlertsFLD
	state[sLISTOFALRTS]=[]
	Map set= gtSettings()
	def dev=set[sALRTSW]
	Boolean usealsw= bIs(set,sUSEALRTSW) && dev
	if(usealsw && dev.currentState(sSW).value==sON) alertNow(null, sNULL, false) // maybe Switch.off()
	logWarn "NOAA Weather Alerts application state is reset."

	Integer myPoll; myPoll=5
	if(oMs(set,sWHATPOLL))myPoll=sMs(set,sWHATPOLL).toInteger()
	switch(myPoll){
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
	if(bIs(set,sLOGDEBUG) || bIs(set,sLOGTRC) || bIs(set,sLOGINFO)){
	//if(bIs(set,'logEnable')){
		Integer myLog; myLog=15
		if(settings['logMinutes']!=null)myLog=(Integer)settings['logMinutes']
		if(myLog>0){
			logTrace "Debug messages set to automatically disable in ${myLog} minute(s)."
			runIn((myLog*60),logsOff)
		}else logTrace "Debug logs set to not automatically disable."
	}else logTrace "Debug logs disabled."
}

void installed(){
	logDebug "Installed with settings: ${settings}"
	initialize()
}

void updated(){
	logDebug "Updated with settings: ${settings}"
	state.remove('num')
	state.remove('speechDuration2')
	state.remove('frequency')
	state.remove('count')
	initialize()
}

void uninstalled(){
	cleanupChildDevices()
}

static String UIsupport(String type, String txt=sBLANK){
	switch(type){
		case 'logo':
			return "<table border=0><thead><tr><th><img border=0 style='max-width:100px' src='https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/NOAA/Support/NOAA.png'></th><th style='padding:10px' align=left><font style='font-size:34px;color:#1A77C9;font-weight: bold'>NOAA Weather Alerts</font><br><font style='font-size:14px;font-weight: none'>This application provides customized Weather Alert announcements.</font></tr></thead></table><br><hr style='margin-top:-15px;background-color:#1A77C9; height: 1px; border: 0;'></hr>"
			break
		case 'line':
			return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
			break
		case 'header':
			return "<div style='color:#ffffff;font-weight: bold;background-color:#1A7BC7;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${txt}</div>"
			break
		case 'footer':
			return "<div style='color:#1A77C9;text-align:center'>App/Driver v${appVersionFLD}<br>Originally Developed by: Aaron Ward<br></div>"
			break
		case 'configured':
			return "<img border=0 style='max-width:15px' src='https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/support/images/Checked.svg'>"
			break
		case 'attention':
			return "<img border=0 style='max-width:15px' src='https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/support/images/Attention.svg'>"
			break
	}
	return sNULL
}

void logsOff(){
	logInfo "Debug logging disabled."
//	app.updateSetting('logEnable',[(sVL):sFALSE,(sTY):sBOOL])
	app.removeSetting('logEnable')
	app.updateSetting(sLOGDEBUG, [(sVL):sFALSE, (sTY):sBOOL])
	app.updateSetting(sLOGTRC, [(sVL):sFALSE, (sTY):sBOOL])
	app.updateSetting(sLOGINFO, [(sVL):sFALSE, (sTY):sBOOL])
}

private void logDebug(String msg){ if((Boolean)settings[sLOGDEBUG]){ log.debug logPrefix(msg, "purple")}}
private void logInfo(String msg){ if((Boolean)settings[sLOGINFO]){ log.info sSPACE+logPrefix(msg, "#0299b1")}}
private void logTrace(String msg){ if((Boolean)settings[sLOGTRC]){ log.trace logPrefix(msg, sCLRGRY)}}
private void logWarn(String msg){ if((Boolean)settings[sLOGWRN]){ log.warn sSPACE+logPrefix(msg, sCLRORG)}}

void logError(String msg, ex=null){
	if((Boolean)settings[sLOGERR]){
		log.error logPrefix(msg, sCLRRED)
		String a
		try{
			if(ex) a=getExceptionMessageWithLine(ex)
		} catch (ignored){
		}
		if(a) log.error logPrefix(a, sCLRRED)
	}
}

static String logPrefix(String msg, String color=sNULL){
	return span("NOAAApp (v"+appVersionFLD+") | ", sCLRGRY)+span(msg, color)
}

static String span(String str, String clr=sNULL, String sz=sNULL, Boolean bld=false, Boolean br=false){ return str ? "<span ${(clr || sz || bld) ? "style='${clr ? "color: ${clr};" : sBLANK}${sz ? "font-size: ${sz};" : sBLANK}${bld ? "font-weight: bold;" : sBLANK}'" : sBLANK}>${str}</span>${br ? sLINEBR : sBLANK}" : sBLANK}
static String inTS1(String str, String img=sNULL, String clr=sNULL, Boolean und=true){ return spanSmBldUnd(str, clr, img)}
static String spanSmBldUnd(String str, String clr=sNULL, String img=sNULL){ return str ? spanImgStr(img)+span(strUnder(str), clr, sSMALL, true) : sBLANK}
static String spanImgStr(String img=sNULL){ return img ? span("<img src='${(!img.startsWith("http://") && !img.startsWith("https://")) ? getAppImg(img) : img}' width='42'> ") : sBLANK}
static String strUnder(String str, Boolean showUnd=true){ return str ? (showUnd ? "<u>${str}</u>" : str) : sBLANK}

static String getAppImg(String imgName){ return "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/${imgName}.png"}