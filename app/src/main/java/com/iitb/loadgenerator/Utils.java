package com.iitb.loadgenerator;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;


//Utility class defining utility functions. function names are self explanatory in what they do.
public class Utils {
	
	//send the exit signal to server
	static void sendExitSignal(){
		HttpClient client = Utils.getClient();
		String url = "http://" + MainActivity.serverip + ":" + MainActivity.serverport + "/" + Constants.SERVLET_NAME + "/clientExit.jsp";
		Log.d(Constants.LOGTAG, url);
		HttpPost httppost = new HttpPost(url);
		List <NameValuePair> params = Utils.getExitDetails();
		int statuscode = 404; //default if something went wrong
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));
			Log.d(Constants.LOGTAG, "Trying to send device info. Params set");
			
			try {
		         HttpResponse response = client.execute(httppost);
		         statuscode = response.getStatusLine().getStatusCode(); //will get 200 only if registration success.
		         
		         String responseBody = EntityUtils.toString(response.getEntity());
		         Log.d(Constants.LOGTAG, "Exit with statuscode " +  statuscode);
		    } catch (ClientProtocolException e) {
		         e.printStackTrace();
		    } catch (IOException e) {
		         e.printStackTrace();
		    }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	//pings the given network
	public static boolean ping(String net){
        Log.d(Constants.LOGTAG, "ping() : entered.");
        Runtime runtime = Runtime.getRuntime();
        try
        {
        	String pingcommand = "/system/bin/ping -c 1 " + net;
            Log.d(Constants.LOGTAG, "ping() command : " + pingcommand);

            Process  mIpAddrProcess = runtime.exec(pingcommand);
            int exitValue = mIpAddrProcess.waitFor();
            Log.d(Constants.LOGTAG, "ping() mExitValue " + exitValue);
            if(exitValue==0){ //exit value 0 means normal termination
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            System.out.println(" Exception:"+ignore);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            System.out.println(" Exception:"+e);
        }
        return false;
    }
	
	//returns key,value pairs to be sent while sending log file
	static List <NameValuePair> getLogFileJson(String expID){
		List < NameValuePair > nameValuePairs = new ArrayList <NameValuePair> ();
		
		nameValuePairs.add(new BasicNameValuePair("expID", expID));
		nameValuePairs.add(new BasicNameValuePair(Constants.macAddress, Utils.getMACAddress()));
		
		return nameValuePairs;
	}
	
	
	//returns httpclient object setting the default timeout params
	static HttpClient getClient(){
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.timeoutConnection);
		HttpConnectionParams.setSoTimeout(httpParameters, Constants.timeoutSocket);
		
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		
		return httpClient;
	}

	//time formatter
	static SimpleDateFormat sdf = new SimpleDateFormat("ZZZZ HH:mm:s.S", Locale.US);
	
	//returns current time in proper format as defined above
	static String getTimeInFormat(){
		Calendar cal = Utils.getServerCalendarInstance();
		return sdf.format(cal.getTime());
	}
	
	//returns name,value pairs to send while exit signal sent to server
	public static List <NameValuePair> getExitDetails(){
		List < NameValuePair > nameValuePairs = new ArrayList <NameValuePair> ();
		nameValuePairs.add(new BasicNameValuePair(Constants.sessionID, Integer.toString(MainActivity.sessionid)));
		nameValuePairs.add(new BasicNameValuePair(Constants.macAddress, getMACAddress()));
		return nameValuePairs;
	}

	//returns name, values pairs representing device information to be sent to server during registration
	public static List <NameValuePair> getMyDetailsJson(ServerSocket listen){
		
		List < NameValuePair > nameValuePairs = new ArrayList <NameValuePair> ();
		String osVersion = Integer.toString(android.os.Build.VERSION.SDK_INT);
		
		nameValuePairs.add(new BasicNameValuePair(Constants.sessionID, Integer.toString(MainActivity.sessionid)));
		nameValuePairs.add(new BasicNameValuePair(Constants.ip, getIP()));
		nameValuePairs.add(new BasicNameValuePair(Constants.port, Integer.toString(listen.getLocalPort())));
		nameValuePairs.add(new BasicNameValuePair(Constants.osVersion, osVersion));
		nameValuePairs.add(new BasicNameValuePair(Constants.wifiVersion, "802.11n"));

		nameValuePairs.add(new BasicNameValuePair(Constants.rssi,Integer.toString(MainActivity.rssi)));
		nameValuePairs.add(new BasicNameValuePair(Constants.bssid, MainActivity.bssid));
		nameValuePairs.add(new BasicNameValuePair(Constants.ssid, MainActivity.ssid));
		nameValuePairs.add(new BasicNameValuePair(Constants.linkSpeed, Integer.toString(MainActivity.linkSpeed)));

		nameValuePairs.add(new BasicNameValuePair(Constants.macAddress, getMACAddress()));
		nameValuePairs.add(new BasicNameValuePair(Constants.processorSpeed, getProcessorSpeed()));
		nameValuePairs.add(new BasicNameValuePair(Constants.numberOfCores, Integer.toString(getNumCores())));
		nameValuePairs.add(new BasicNameValuePair(Constants.wifiSignalStrength, getWifiStrength()));

		nameValuePairs.add(new BasicNameValuePair(Constants.storageSpace, getAvailableStorage()));
		nameValuePairs.add(new BasicNameValuePair(Constants.memory, getTotalRAM()));
		nameValuePairs.add(new BasicNameValuePair(Constants.packetCaptureAppUsed, (new Boolean(false)).toString()));

		return nameValuePairs;
	}
	
	public static String getIP(){
		WifiInfo info = MainActivity.wifimanager.getConnectionInfo();
		int ip = info.getIpAddress();
		@SuppressWarnings("deprecation")
		String ipString = Formatter.formatIpAddress(ip);
		return ipString;
	}

	public static String getMACAddress(){
		WifiInfo info = MainActivity.wifimanager.getConnectionInfo();
		String address = info.getMacAddress();
		return address;
	}
	
	public static String getWifiStrength(){
		WifiInfo info = MainActivity.wifimanager.getConnectionInfo();
		int level = WifiManager.calculateSignalLevel(info.getRssi(), 10);
		return Integer.toString(level);
	}
	
	public static String getAvailableStorage(){
		File path = Environment.getDataDirectory(); //internal storage
		StatFs sf = new StatFs(path.getPath());
		int blocks = sf.getAvailableBlocks();
		int blocksize = sf.getBlockSize();
		long availStorage = blocks * blocksize/(1024 * 1024); //Mega bytes
		return Long.toString(availStorage);
	}
	
	public static String getTotalRAM() {
	    RandomAccessFile reader = null;
	    String load = "0";
	    try {
	        reader = new RandomAccessFile("/proc/meminfo", "r");
	        load = reader.readLine();
	        String[] tokens = load.split(" +");
	        load = tokens[1].trim(); //here is the memory
	        int ram = Integer.parseInt(load); //KB
	        ram = ram/1024;
	        load = Integer.toString(ram);
	        reader.close();
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	    return load;
	}
	
	/**
	 * Gets the number of cores available in this device, across all processors.
	 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
	 * @return The number of cores, or 1 if failed to get result
	 */
	public static int getNumCores() {
	    //Private Class to display only CPU devices in the directory listing
	    class CpuFilter implements FileFilter {
	        @Override
	        public boolean accept(File pathname) {
	            //Check if filename is "cpu", followed by a single digit number
	            if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
	                return true;
	            }
	            return false;
	        }      
	    }

	    try {
	        //Get directory containing CPU info
	        File dir = new File("/sys/devices/system/cpu/");
	        //Filter to only list the devices we care about
	        File[] files = dir.listFiles(new CpuFilter());
	        //Return the number of cores (virtual CPU devices)
	        return files.length;
	    } catch(Exception e) {
	        //Default to return 1 core
	        return 1;
	    }
	}
	
	public static String getProcessorSpeed() {
	    RandomAccessFile reader = null;
	    String load = "0";
	    try {
	        reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
	        load = reader.readLine();
	        int speed = Integer.parseInt(load); //Khz
	        speed = speed / 1000; //Mhz
	        load = Integer.toString(speed);
	        reader.close();
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	    return load;
	}
	
	//parse the json string sent by server in form of map of key value pairs
	@SuppressWarnings("unchecked")
	static Map<String, String> ParseJson(String json){
		Map<String, String> jsonMap = null;
		JSONParser parser = new JSONParser();
		ContainerFactory containerFactory = new ContainerFactory(){
			@SuppressWarnings("rawtypes")
			public List creatArrayContainer() {
		      return new LinkedList();
		    }
			@SuppressWarnings("rawtypes")
		    public Map createObjectContainer() {
		      return new LinkedHashMap();
		    }
		                        
		};
		
		try {
			jsonMap = (Map<String, String>) parser.parse(json, containerFactory);
		} catch (ParseException e) {
			System.out.println();
			e.printStackTrace();
		}
		return jsonMap;
	}
	
	//sends the file by writing file length, and then data to output stream
	static void SendFile(DataOutputStream out, String fileName){
		File file = new File(fileName);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		int fileLength;
		int count=0;
		try {
			fis = new FileInputStream(file);
			fileLength = (int) file.length();
			bis = new BufferedInputStream(fis);
			if (fileLength > Integer.MAX_VALUE) {
		        System.out.println("File is too large.");
		    }
			
			out.writeInt(fileLength);
		    byte[] bytes = new byte[(int) fileLength];
		    
		    while ((count = bis.read(bytes)) > 0) {
		        out.write(bytes, 0, count);
		    }
		    
		    fis.close();
		    bis.close();
		    Log.d(Constants.LOGTAG, "Deleting log file(not yet)" + fileName);
		    //!TODO
		    //file.delete(); //since this file sending was succesful we can delete it from the log directory
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	static Calendar getServerCalendarInstance(){
		Calendar cal = Calendar.getInstance();
		Log.d("UTILS", "getServerCalendarInstance local " + MainActivity.sdf.format(cal.getTime()));
		cal.add(Calendar.MILLISECOND, (int)MainActivity.serverTimeDelta);
		Log.d("UTILS", "getServerCalendarInstance offset = " + MainActivity.serverTimeDelta + " | server " + MainActivity.sdf.format(cal.getTime()));
		return cal;
	}
	
	
}
