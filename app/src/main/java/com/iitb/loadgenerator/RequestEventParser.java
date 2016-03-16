package com.iitb.loadgenerator;


import java.util.Calendar;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;

enum RequestType{
	GET,
	POST,
	NONE
};

enum DownloadMode{
	SOCKET,
	WEBVIEW,
	NONE
};


//Event class. stores time(as Calendar object), url, request type
class RequestEvent{
	Calendar cal;
	String url;
	RequestType type;
	DownloadMode mode;
	String postDataSize;
	RequestEvent(Calendar tcal, String turl, RequestType ttype, DownloadMode tmode){
		cal = tcal;
		url = turl;
		type = ttype;
		mode = tmode;
	}

	RequestEvent(Calendar tcal, String turl, RequestType ttype, DownloadMode tmode, String size){
		cal = tcal;
		url = turl;
		type = ttype;
		mode = tmode;
		postDataSize = size;
	}


}

//Load contains all information about an experiment i.e event id and list of all events
class Load{
	long loadid;
	Vector<RequestEvent> events;
	Load(long tloadid, Vector<RequestEvent> tevents){
		loadid = tloadid;
		events = tevents;
	}
}

//Parser class which parses lines from control file and creates event objects
public class RequestEventParser {
	
	private static final Map<String, RequestType> typeMap;
	private static final Map<String, DownloadMode> modeMap;
	static
	{
	    typeMap = new HashMap<String, RequestType>();
	    typeMap.put("GET", RequestType.GET);
	    typeMap.put("POST", RequestType.POST);
	    
	    modeMap = new HashMap<String, DownloadMode>();
	    modeMap.put("SOCKET", DownloadMode.SOCKET);
	    modeMap.put("WEBVIEW", DownloadMode.WEBVIEW);
	}
	
	public static RequestType getRequestEnum(String key){
		RequestType type = typeMap.get(key);
		if(type == null){
			return RequestType.NONE;
		}
		return type;
	}
	
	public static DownloadMode getDownloadModeEnum(String key){
		DownloadMode type = modeMap.get(key);
		if(type == null){
			return DownloadMode.NONE;
		}
		return type;
	}
	
	//Just for testing parseLine
	public static String generateLine(int seconds){
		String line = "GET ";
		
		Calendar cal = Utils.getServerCalendarInstance();
		cal.add(Calendar.SECOND, seconds);
		line += cal.get(Calendar.YEAR) + " ";
		line += cal.get(Calendar.MONTH) + " ";
		line += cal.get(Calendar.DAY_OF_MONTH) + " ";
		line += cal.get(Calendar.HOUR_OF_DAY) + " ";
		line += cal.get(Calendar.MINUTE) + " ";
		line += cal.get(Calendar.SECOND) + " ";
		line += cal.get(Calendar.MILLISECOND) + " ";
		
		line += "http://www.cse.iitb.ac.in/~ashishsonone/serve.php?user=ashish@" + seconds;
		return line;
	}
	
	public static RequestEvent parseLine(String line){
		String[] fields = line.split(" ");
		Log.d("RequestEventParser ", fields[0] + fields.length);
		
		if(fields.length < 10) {
			Log.d("RequestEventParser : parseString", "No of fields less than expected");
			return null; //No valid event could be found 
		}
		// atleast there are 10 fields(DOWNLOADMODE CAN BE "SOCKET/WEBVIEW"
		// TYPE year month dom hod min sec millisec DOWNLOADMODE URL
		// 0     1   2      3    4   5   6   7          8		  9	
		RequestType type = getRequestEnum(fields[0]);
		Log.d("Request type ...... ", type.toString() + " " + fields[8] + " " + fields[9]);
		
		Calendar cal = Utils.getServerCalendarInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(fields[1]));
		cal.set(Calendar.MONTH, Integer.parseInt(fields[2]));
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(fields[3]));
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(fields[4]));
		cal.set(Calendar.MINUTE, Integer.parseInt(fields[5]));
		cal.set(Calendar.SECOND, Integer.parseInt(fields[6]));
		cal.set(Calendar.MILLISECOND, Integer.parseInt(fields[7]));
		
		DownloadMode mode = getDownloadModeEnum(fields[8]);
		String url = fields[9];
		
		if(fields.length == 10){
//		  String retmsg = Threads.writeToLogFile(MainActivity.logfilename,"LINE5 : "+line+"-"+fields[10]+"\n"); 
//		  Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.BROADCAST_MESSAGE, "LINE6 : "+line+"-"+fields[10]);

			return new RequestEvent(cal, url, type, mode,"0");
		} else if (fields.length == 11){

//		  int size = Integer.parseInt(fields[10].trim());

//			return new RequestEvent(cal, url, type, mode, Integer.parseInt(fields[10]));
//		  String retmsg = Threads.writeToLogFile(MainActivity.logfilename,"LINE3 : "+line+"-"+fields[10]+"\n"); 
//		  Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.BROADCAST_MESSAGE, "LINE4 : "+line+"-"+fields[10]);

			return new RequestEvent(cal, url, type, mode, fields[10]);
		}
		return null;	
	}
	
	public static Load parseEvents(String s){
		Log.d("Return event entering", "HERE");
		Vector<RequestEvent> events = new Vector<RequestEvent>();
		Scanner scanner = new Scanner(s);
		String line = scanner.nextLine();
		long id = Long.parseLong(line.split(" ")[0]);
//		Bundle bundle = new Bundle();
		while (scanner.hasNextLine()) {
		  line = scanner.nextLine();
//		  bundle.putString(Constants.BROADCAST_MESSAGE,"LINE : '"+line+"'\n");
//		  String retmsg = Threads.writeToLogFile(MainActivity.logfilename,"LINE1 : "+line+"\n"); 
//		  Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.BROADCAST_MESSAGE, "LINE2 : "+line);

 

		  RequestEvent event = parseLine(line);
		  if(event != null) events.add(event);
		  // process the line
		}
		scanner.close();
		Log.d("Return event", events.size() + "");
		
		return new Load(id, events);
	}
}
