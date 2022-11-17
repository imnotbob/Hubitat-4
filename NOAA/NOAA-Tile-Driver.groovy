/**
 *  ****************  NOAA Tile Driver  ****************
 *
 *  importUrl: https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/NOAA/NOAA-Tile-Driver.groovy
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
 * Last Update: 11/17/2022
 */
//file:noinspection SpellCheckingInspection
//file:noinspection GroovyUnusedAssignment
//file:noinspection unused

import groovy.transform.Field

@Field volatile static Map<String,List<Map>> tileMapFLD = [:]

metadata {
	definition (
		name: "NOAA Tile",
		namespace: "aaronward",
		author: "Aaron Ward",
		importUrl: "https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/NOAA/NOAA-Tile-Driver.groovy") {
		command "sendNoaaTile", ["string"]
		command "initialize"
		command "refreshTile"
		capability "Actuator"
		capability "Refresh"
		attribute "Alerts", "string"
		}

	preferences() {		
		input("logEnable", "bool", title: "Enable logging", required: true, defaultValue: false)
	}
}

def initialize() {
	logInfo("NOAA Tile Driver Initializing.")
	refresh()
	refreshTile()
}

def updated() {
	if(logEnable) logInfo("NOAA Tile updated run.")
	refresh()
}

def installed(){
	logInfo("NOAA Tile has been Installed.")
	sendEvent(name: "Alerts", value: "No weather alerts to report.", displayed: true)
}

def refresh() {
	if(logEnable) runIn(900,logsOff)
}

void logsOff(){
	logWarn("Debug logging disabled.")
	device.updateSetting("logEnable",[value:"false",type:"bool"])
}


void refreshTile() {
	if(logEnable) logInfo("Requesting current weather alert from NOAA App.")
	unschedule(continuePlay)
	unschedule(playLoop)
	List<Map> noaaData; noaaData = []
	try {
		noaaData = (List<Map>)parent.getTile()
		if(logEnable) logInfo ("Received alert data from NOAA Alerts: ${noaaData}")
	}
	catch(ignored){}

	String myId=device.getId()
		tileMapFLD[myId] = noaaData
		tileMapFLD = tileMapFLD

	playLoop()
}

void playLoop(Integer startx=0, Integer starti=0){
	String myId=device.getId()
	List<Map> noaaData = tileMapFLD[myId]

	if(logEnable) logInfo("Displaying ${noaaData?.size()} alerts on the dashboard.")
	if(!noaaData) {
		unschedule(continuePlay)
		sendEvent(name: "Alerts", value: "No weather alerts to report.", displayed: true)
		//runIn(60, refreshTile)
	} else {
		continuePlay([startx, starti])
	}
}

void continuePlay(List<Integer> dd){
	String myId=device.getId()
	List<Map> noaaData = tileMapFLD[myId]
	Integer startx=dd[0]
	Integer starti=dd[1]
	if(!noaaData) {
		if(logEnable) logInfo("continue play no noaaData $startx $starti")
		return
	} else
		if(logEnable) logInfo("playing $startx of ${noaaData?.size()}  $starti alert on the dashboard.")
	List fullmsg
	Boolean didIt; didIt=false
	Integer x,i
	for(x=0;x<noaaData.size();x++) {
		if(didIt) { runIn(12, continuePlay, [data: [x, 0]]); return }

		String t= (String)noaaData[x].alertmsg
		def m = t =~ /(.|[\r\n]){1,378}\W/
		fullmsg = []
		while (m.find()) {
			fullmsg << m.group()
		}
		for(i=0;i<fullmsg.size();i++) {
			if(didIt) { runIn(12, continuePlay, [data: [x, i]]); return }

			String noaaTile
			noaaTile = "<table style='position:relative;top:-10px;left:10px;border-collapse:collapse;width:97%'><tr style='border-bottom:medium solid #FFFFFF;'><td valign='bottom' style='text-align:left;width:50%;border-bottom:medium solid #FFFFFF;height:13px'><font style='font-size:12px;'>"
			noaaTile += "Alert: ${x+1}/${noaaData.size()}"
			noaaTile += "</font></td><td valign='bottom' style='text-align:right;border-bottom:medium solid #FFFFFF;width:50%;height:13px'><font style='font-size:12px;'>Page: ${i+1}/${fullmsg.size()}</font></td></tr>"
			noaaTile += "<tr><td colspan=2 valign='top' style='line-height:normal;width:90%;height:100px;text-align:left;border-top-style:none;border-top-width:medium;'><font style='font-size:13px'>${fullmsg[i]}"
			noaaTile += "</font></td></tr></table>"
			if(startx==x && starti==i) { sendEvent(name: "Alerts", value: noaaTile, displayed: true); didIt=true }
		}
	}
	if(noaaData.size())runIn(20, playLoop)
}

@Field static final String appVersionFLD ='4.0.020'
@Field static final String sNULL         =(String)null
@Field static final String sBLANK        =''
@Field static final String sSPACE        =' '
@Field static final String sCLRGRY       ='gray'
@Field static final String sCLRORG       ='orange'
@Field static final String sCLRRED       ='red'
@Field static final String sSMALL        ='small'
@Field static final String sLINEBR       ='<br>'

private void logDebug(String msg){ log.debug logPrefix(msg, "purple")}
private void logInfo(String msg){ log.info sSPACE+logPrefix(msg, "#0299b1")}
private void logTrace(String msg){ log.trace logPrefix(msg, sCLRGRY)}
private void logWarn(String msg){ log.warn sSPACE+logPrefix(msg, sCLRORG)}

void logError(String msg, ex=null){
	log.error logPrefix(msg, sCLRRED)
	String a
	try{
		if(ex) a=getExceptionMessageWithLine(ex)
	} catch (ignored){
	}
	if(a) log.error logPrefix(a, sCLRRED)
}

static String logPrefix(String msg, String color=sNULL){
	return span("NOAATile (v"+appVersionFLD+") | ", sCLRGRY)+span(msg, color)
}

static String span(String str, String clr=sNULL, String sz=sNULL, Boolean bld=false, Boolean br=false){ return str ? "<span ${(clr || sz || bld) ? "style='${clr ? "color: ${clr};" : sBLANK}${sz ? "font-size: ${sz};" : sBLANK}${bld ? "font-weight: bold;" : sBLANK}'" : sBLANK}>${str}</span>${br ? sLINEBR : sBLANK}" : sBLANK}
static String spanSmBldUnd(String str, String clr=sNULL, String img=sNULL){ return str ? spanImgStr(img)+span(strUnder(str), clr, sSMALL, true) : sBLANK}
static String spanImgStr(String img=sNULL){ return img ? span("<img src='${(!img.startsWith("http://") && !img.startsWith("https://")) ? getAppImg(img) : img}' width='42'> ") : sBLANK}
static String strUnder(String str, Boolean showUnd=true){ return str ? (showUnd ? "<u>${str}</u>" : str) : sBLANK}

static String getAppImg(String imgName){ return "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/${imgName}.png"}
