/**
 *  ****************  NOAA Tile Driver  ****************
 *
 *  importUrl: "https://raw.githubusercontent.com/PrayerfulDrop/Hubitat/master/NOAA/NOAA-Tile-Driver.groovy"
 *
 *  Copyright 2019 Aaron Ward
 *
 *-------------------------------------------------------------------------------------------------------------------
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
 * ------------------------------------------------------------------------------------------------------------------------------
 *              Donations are always appreciated: https://www.paypal.me/aaronmward
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 */

metadata {
	definition (
		name: "NOAA Tile",
		namespace: "aaronward",
		author: "Aaron Ward",
		importUrl: "https://raw.githubusercontent.com/PrayerfulDrop/Hubitat/master/NOAA/NOAA-Tile-Driver.groovy"
		)
		{
		command "sendNoaaTile", ["string"]
        command "initialize"
		capability "Actuator"
		capability "Refresh"
    	attribute "Alerts", "string"
		}

	preferences() {    	
			//input("timeExpire", "text", title: "Minutes to scroll alert?", required: true, defaultValue: "5")
            input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: true)
    }
}

def initialize() {
	log.info "NOAA Tile Driver Initializing."
	refresh()
    sendEvent(name: "Alerts", value: "No weather alerts to report.", displayed: true)
}

def updated() {
	refresh()
}

def installed(){
    log.info "NOAA Tile has been Installed."
	sendEvent(name: "Alerts", value: "No weather alerts to report.", displayed: true)
}

def refresh() {
	if (logEnable) runIn(900,logsOff)
    log.info "NOAA Tile has been updated."
	state.clear()
    state.noaaDataPast=null
    sendEvent(name: "Alerts", value: "No weather alerts to report.", displayed: true)
}

def logsOff(){
    log.warn "Debug logging disabled."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}


def sendNoaaTile(noaaData) {
    if(logEnable) log.info "Received weather alert from NOAA App."
    log.debug noaaData
    if(noaaData == null || noaaData == "") {
        state.noaaDataPast = noaaData
        sendEvent(name: "Alerts", value: "No weather alerts to report.", displayed: true) 
    } else {
        if(state.noaaDataPast==null || state.noaaDataPast=="") state.noaaDataPast=[]
        if(!noaaData.contains(state.noaaDataPast) || !state.looper) {
            state.looper = true
            state.noaaDataPast = noaaData
            messageSize = 300
            fullalert = []
            for(x=0;x<state.noaaDataPast.size();x++) {
	    		m = state.noaaDataPast[x].alertmsg =~ /(.|[\r\n]){1,300}\W/
		    	fullmsg = []
			    while (m.find()) {
    			   fullmsg << m.group()
                }
                for(i=0;i<fullmsg.size();i++) {
                    noaaTile = "<div style='position: absolute;top:2px; left:10px; right:10px;width:100%; height:100px;' ><table style='width:97%'><tr><td valign='bottom' style='text-align:left; border-bottom: medium solid #FFFFFF;width: 100%; height:16px'><font style='font-size:15px;'>"
                    if(i==0) noaaTile += "Alert ${x+1}/${state.noaaDataPast.size()}"
                    else noaaTile += "Alert ${x+1}/${state.noaaDataPast.size()} (continued)"
				    noaaTile += "</font></td></tr><tr><td valign='top' style='width:90%; height:100px; text-align:left; border-top-style:none; border-top-width:medium;'><font style='font-size: 15px'>"
                    noaaTile += "${fullmsg[i]}"
	    			noaaTile += "</font></td></tr></table></center>"
		    		sendEvent(name: "Alerts", value: noaaTile, displayed: true)      
                    pauseExecution(7000)
                }
            } 
            state.looper = false
        }
    }
}
