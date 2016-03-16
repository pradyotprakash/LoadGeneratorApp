package com.iitb.loadgenerator;

import android.os.Environment;


public class Constants {
	static final String action = "action";
	static final String serverTime = "serverTime";
	static final String sessionID = "sessionID";
	static final String ip = "ip";
	static final String port = "port";
	static final String osVersion = "osVersion";
	static final String wifiVersion = "wifiVersion";
	static final String macAddress = "macAddress";
	static final String numberOfCores = "numberOfCores";
	static final String memory = "memory";				//in MB
	static final String processorSpeed = "processorSpeed";		//in GHz
	static final String wifiSignalStrength = "wifiSignalStrength";
	static final String storageSpace = "storageSpace";		//in MB
	static final String packetCaptureAppUsed = "packetCaptureAppUsed";
	static final String action_controlFile = "controlFile";
	static final String textFileFollow = "textFileFollow";
	static final String action_stopExperiment = "stopExperiment";
	static final String action_clearRegistration = "clearRegistration";
	static final String action_refreshRegistration = "refreshRegistration";

	static final String rssi = "rssi";
	static final String bssid = "bssid";
	static final String ssid = "ssid";
	static final String linkSpeed = "linkSpeed";
	
	static final int alarmRequestCode = 192837;
	static final int timeoutAlarmRequestCode = 123343;
	static final int killTimeoutDuration = 180; //in minutes
	static final String logDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/loadgenerator/logs";
	
	
	//temporary
	static final String LOGTAG = "LOADGENERATOR";
	static final String LINEDELIMITER = "* * * * * *\n";
	static final String SUMMARY_PREFIX = "### ";
	static final String EOF = "\nEOF\n";
	
	// Defines a custom intent for alarm receiver
	public static final String BROADCAST_ALARM_ACTION = "com.iitb.loadgenerator.BROADCAST_ALARM";
	
	// Defines a custom Intent action
    public static final String BROADCAST_ACTION = "com.example.android.threadsample.BROADCAST";
    
    // Defines the key for the status "extra" in an Intent
    public static final String BROADCAST_MESSAGE = "com.example.android.threadsample.STATUS";
    
    public static final int timeoutConnection = 3000; //timeout in milliseconds until a connection is established.
    public static final int timeoutSocket = 5000; //timeout for waiting for data.
    public static final String SERVLET_NAME = "serverplus";
    public static final String SHARED_PREFS = "serverDetails";
    public static final String keyServerAdd = "serverAdd";
    public static final String keyServerPort = "serverPort";
    public static final String keyServerSession = "serverSession";
}
