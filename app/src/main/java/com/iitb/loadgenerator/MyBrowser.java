package com.iitb.loadgenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.Context;


public class MyBrowser extends WebViewClient {
	static long SECONDS_MILLISECONDS = 1000;
	static String LOGTAG = "DEBUG_MY_BROWSER";
	static WifiManager wifimanager;

	public static String js;
	
	int eventid;
	static StringBuilder logwriter  = new StringBuilder(); 
	boolean loggingOn;
	String baseURL;
	int totalResponseTime;
	Calendar pageStartTime = null;
	
	MyBrowser(int id, String tbaseURL){
		eventid = id;



		logwriter = new StringBuilder();
		loggingOn = true;
		baseURL = tbaseURL;
		totalResponseTime = 0;
		if(MainActivity.load == null) return;
		//logwriter.append("\ndetails: " + MainActivity.load.loadid + " " + eventid + " WEBVIEW" + "\n");
		//logwriter.append("url:" + baseURL + " ");
	}

	@Override
	public void  onLoadResource(WebView view, String url)  {
		logwriter.append("Inside onLoadResource : "+url+" \n");
	}

   @Override
   public void onPageStarted(WebView view, String url, Bitmap favicon) {
   	logwriter.append("Inside onPageStarted : "+url+ "\n Favicon : " +favicon+" \n");
    super.onPageStarted(view, url, null);
   }

   @Override
   public boolean shouldOverrideUrlLoading(WebView view, String url) {
 	   logwriter.append("Inside SHOULD OVERRIDE URL LOADING : "+url+" \n");
       view.loadUrl(url);
       return true; //return true means this webview has handled the request. Returning false means host(system browser) takes over control
   }


   public static void writeToFile(String url){
   		
   		logwriter.append("Check URL : "+url+" \n");
   }

   @Override
   public WebResourceResponse  shouldInterceptRequest (WebView view, String url){

       logwriter.append("Inside SHOULD INTERCEPT REQUEST : "+url+" \n");
	   //Log.d(LOGTAG + "-shouldInterceptReques-THREADID", url + " on " + android.os.Process.myTid());
	   if (url.startsWith("http")) {
	   	   //logwriter.append("Inside HTTP \n");
		   //Log.d(LOGTAG + "-shouldInterceptRequest TRUE", "url= " +  url);
	   		WebResourceResponse obj = getResource(url);
	   		logwriter.append("Inside SHOULD INTERCEPT Response: "+obj+" \n");
           return obj;
       }
	   Log.d(LOGTAG + "-shouldInterceptRequest FALSE", "returning NULL " + url);
	   return null; //returning null means webview will load the url as usual.
   }


   
   public static WebResourceResponse postResource(String url){
//   		logwriter.append("Inside POST RESOURCE : "+url+"\n");
   		//return new WebResourceResponse("text/plain", "UTF-8", EMPTY);
   		String  []st = url.split("##");
   		url = st[0];
   		logwriter.append("\n\nPOST "+url+" ");
//   		int sizeOfData = Integer.parseInt(st[1]);
//   		logwriter.append("\nURL : "+url);
//   		logwriter.append("\nSize : "+sizeOfData);

	   HttpClient client = new DefaultHttpClient();
	   HttpGet request = null;
	   String newURL = null;
		try {
			newURL = getURL(url).toString();
		} catch (MalformedURLException e1){

			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.d(LOGTAG + "-MALFORMED", "url malformed " + url);
			return null;
		} catch (URISyntaxException e1){
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.d(LOGTAG + "-MALFORMED", "url malformed " + url);
			return null;
		}
		
		Calendar start = Utils.getServerCalendarInstance();
		long startTime = start.getTimeInMillis();
	   

		try {
			URL urlObj = new URL(newURL);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setReadTimeout(10000); //10 seconds timeout for reading from input stream
			connection.setConnectTimeout(10000); //10 seconds before connection can be established

			//add reuqest header
			connection.setRequestMethod("POST");
			//connection.setRequestProperty("User-Agent", USER_AGENT);
			//connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			/*
			//String urlParameters = "name=ratheesh&rollno=153050057";
			sizeOfData = 800;
    		byte[] b = new byte[sizeOfData];
    		new Random().nextBytes(b);
		    byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(b);
	        String w = decoded.toString();
        	logwriter.append("Random Data : "+w+" [Size : "+w.length()+" ]");

	        String w ="abc#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQD"
                + "abcabc#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234abcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQDabcabcabcabca2736426487234&*&&%@$#&@#*&@#(*(&&*@^#@UQGJFDGEQD";
			*/
                
			int sizeOfData = Integer.parseInt(st[1]);		
			String  _str = "";
			for (int i = 0;i<sizeOfData-5;i++){
				_str += "A";
			}

            //logwriter.append("Data : "+_str+" [POST Date Size : "+_str.length()+" ]");
			String urlParameters = "data=" + _str;
			logwriter.append("Post_Date_Size:"+urlParameters.length()+" ");
			// Send post request
			connection.setDoOutput(true);
			DataOutputStream wr1 = new DataOutputStream(connection.getOutputStream());
			wr1.writeBytes(urlParameters);
			wr1.flush();
			wr1.close();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.d(Constants.LOGTAG, "getResource : " + " connection response code error");
				Calendar end = Utils.getServerCalendarInstance();
				
				String startTimeFormatted =  Utils.sdf.format(start.getTime());
				String endTimeFormatted =  Utils.sdf.format(end.getTime());
				
/*				logwriter.append("ERROR Response_Time:" + (end.getTimeInMillis()-start.getTimeInMillis()) + " [Start_Time:" + startTimeFormatted + " End_Time:" + endTimeFormatted + "] " +
						"Status_Code:" + connection.getResponseCode() + " " ); */


				logwriter.append("ERROR Response_Time:" + (end.getTimeInMillis()-start.getTimeInMillis()) + " " +
						"Status_Code:" + connection.getResponseCode() + " " );



				logwriter.append("RSSI:" + MainActivity.rssi + "dBm " + 
								 "BSSID:" + MainActivity.bssid + " " + 
								 "SSID:" + MainActivity.ssid + " " +
								 "LINK_SPEED:" + MainActivity.linkSpeed + "Mbps ");

				
				Log.d(LOGTAG, "HandleEvent : " + " connection response code error");
			}
			else{
				int fileLength = connection.getContentLength();
				InputStream input = connection.getInputStream();
				
				Log.d(Constants.LOGTAG, "getResource : " + " filelen " + fileLength);
				
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				int nRead;
				byte[] data = new byte[16384];

				while ((nRead = input.read(data, 0, data.length)) != -1) {
				  buffer.write(data, 0, nRead);
				}

				Calendar end = Utils.getServerCalendarInstance();
				long endTime = end.getTimeInMillis();
				
				buffer.flush();

				byte[] responseData = buffer.toByteArray();
				
				String startTimeFormatted =  Utils.sdf.format(start.getTime());
				String endTimeFormatted =  Utils.sdf.format(end.getTime());
				
				logwriter.append("SUCCESS Response_Time:" + (endTime-startTime) + " " +
						 "Received-Content-Length:" + fileLength + " ");

				logwriter.append("RSSI:" + MainActivity.rssi + "dBm " + 
								 "BSSID:" + MainActivity.bssid + " " + 
								 "SSID:" + MainActivity.ssid + " " +
								 "LINK_SPEED:" + MainActivity.linkSpeed + "Mbps ");

				
//				String contex = Context.WIFI_SERVICE ;
		/*				Context context = getApplicationContext();
				wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				WifiInfo info = wifimanager.getConnectionInfo();

		int rssi = info.getRssi();
				String bssid = info.getBSSID();  
				int frequency = info.getFrequency();   // MHz
				String  ssid = info.getSSID();  
				int linkSpeed = info.getLinkSpeed();   // Mbps */


	

				InputStream stream = new ByteArrayInputStream(responseData);
				WebResourceResponse wr = new WebResourceResponse("", "utf-8", stream);
				return wr;
			}



			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Calendar end = Utils.getServerCalendarInstance();
			long endTime = end.getTimeInMillis();
			String startTimeFormatted =  Utils.sdf.format(start.getTime());
			String endTimeFormatted =  Utils.sdf.format(end.getTime());
			logwriter.append("ERROR Response_Time:" + (endTime-startTime) + " Error+msg:" +
					e.getMessage() + " ");

			logwriter.append("RSSI:" + MainActivity.rssi + "dBm " + 
								 "BSSID:" + MainActivity.bssid + " " + 
								 "SSID:" + MainActivity.ssid + " " +
								 "LINK_SPEED:" + MainActivity.linkSpeed + "Mbps ");

			e.printStackTrace();
		}   
	   return null;
   }




   public static WebResourceResponse getResource(String url){
	   //Log.d(LOGTAG, "getJPG for url " + url);
   		

 		if(url.endsWith("##POST")){
 			
			WebResourceResponse obj = postResource(url);
	   		logwriter.append("POST Response: "+obj+" \n");
 			return obj;
        }

//       logwriter.append("Inside GET RESOURCE : "+url+"\n");
      
       String []st = url.split("##");
       url = st[0];
 	   logwriter.append("\n\nGET "+url+" ");

	   HttpClient client = new DefaultHttpClient();
	   HttpGet request = null;
	   String newURL = null;
		try {
			newURL = getURL(url).toString();
	
		} catch (MalformedURLException e1){

			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.d(LOGTAG + "-MALFORMED", "url malformed " + url);
			return null;
		} catch (URISyntaxException e1){
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.d(LOGTAG + "-MALFORMED", "url malformed " + url);
			return null;
		}



		Calendar start = Utils.getServerCalendarInstance();
		long startTime = start.getTimeInMillis();
	   
		try {
			URL urlObj = new URL(newURL);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setReadTimeout(10000); //10 seconds timeout for reading from input stream
			connection.setConnectTimeout(10000); //10 seconds before connection can be established
			
			connection.connect();
			
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.d(Constants.LOGTAG, "getResource : " + " connection response code error");
				Calendar end = Utils.getServerCalendarInstance();
				
				String startTimeFormatted =  Utils.sdf.format(start.getTime());
				String endTimeFormatted =  Utils.sdf.format(end.getTime());
				
				logwriter.append("ERROR Response_Time:" + (end.getTimeInMillis()-start.getTimeInMillis()) + " " +
						"Status_Code:" + connection.getResponseCode() + " ");
				

				logwriter.append("RSSI:" + MainActivity.rssi + "dBm " + 
								 "BSSID:" + MainActivity.bssid + " " + 
								 "SSID:" + MainActivity.ssid + " " +
								 "LINK_SPEED:" + MainActivity.linkSpeed + "Mbps ");


				Log.d(LOGTAG, "HandleEvent : " + " connection response code error");
			}
			else{
				int fileLength = connection.getContentLength();
				InputStream input = connection.getInputStream();
				
				Log.d(Constants.LOGTAG, "getResource : " + " filelen " + fileLength);
				
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				int nRead;
				byte[] data = new byte[16384];

				while ((nRead = input.read(data, 0, data.length)) != -1) {
				  buffer.write(data, 0, nRead);
				}

				Calendar end = Utils.getServerCalendarInstance();
				long endTime = end.getTimeInMillis();
				
				buffer.flush();

				byte[] responseData = buffer.toByteArray();
				
				String startTimeFormatted =  Utils.sdf.format(start.getTime());
				String endTimeFormatted =  Utils.sdf.format(end.getTime());
				
				logwriter.append("SUCCESS Response_Time:" + (endTime-startTime) + " " +
						 "Received-Content-Length:" + fileLength + " " );


				logwriter.append("RSSI:" + MainActivity.rssi + "dBm " + 
								 "BSSID:" + MainActivity.bssid + " " + 
								 "SSID:" + MainActivity.ssid + " " +
								 "LINK_SPEED:" + MainActivity.linkSpeed + "Mbps ");


	/*			wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				WifiInfo info = wifimanager.getConnectionInfo();
				int rssi = info.getRssi();
				String bssid = info.getBSSID();  
				int frequency = info.getFrequency();   // MHz
				String  ssid = info.getSSID();  
				int linkSpeed = info.getLinkSpeed();   // Mbps */

	
				
				InputStream stream = new ByteArrayInputStream(responseData);
				WebResourceResponse wr = new WebResourceResponse("", "utf-8", stream);
				return wr;
			}


			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Calendar end = Utils.getServerCalendarInstance();
			long endTime = end.getTimeInMillis();
			String startTimeFormatted =  Utils.sdf.format(start.getTime());
			String endTimeFormatted =  Utils.sdf.format(end.getTime());
			logwriter.append("ERROR Response_Time:" + (endTime-startTime) + " " +
					" Error_msg:" + e.getMessage() + " ");

			logwriter.append("RSSI:" + MainActivity.rssi + "dBm " + 
								 "BSSID:" + MainActivity.bssid + " " + 
								 "SSID:" + MainActivity.ssid + " " +
								 "LINK_SPEED:" + MainActivity.linkSpeed + "Mbps ");


			e.printStackTrace();
		}   
	   return null;
   }
   
   public static URL getURL(String rawURL) throws MalformedURLException, URISyntaxException{
	   URL url = new URL(rawURL);
	   URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
	   url = uri.toURL();
	   return url;
   }

   /*
   @Override
   public void onPageStarted(WebView view, String url, Bitmap favicon) {
   	//return null;
   }  */
   
   @Override
   public void onPageFinished(WebView view, String url) {
 //  		logwriter.append("Inside On PAGE Finished : "+url+"\n");
	   Log.d(LOGTAG, "########## onPageFinished() called for url " + baseURL);
	   super.onPageFinished(view, url);
       
       if(loggingOn){
	       loggingOn = false; //no more log collection
	       
	       Runnable r = new Runnable() {
				public void run() {
					int num = MainActivity.numDownloadOver++;
					if(MainActivity.load == null){
			    		Log.d(Constants.LOGTAG, "DownloaderService : load null");
			    		return;
			    	}
					int loadSize = MainActivity.load.events.size();
			//		logwriter.append(Constants.SUMMARY_PREFIX + "summary total RT = " +  totalResponseTime + "\n");
			//		logwriter.append("success\n");
			//		logwriter.append(Constants.SUMMARY_PREFIX + Constants.LINEDELIMITER); //this marks the end of this log
					logwriter.append("\n");

					if(num+1 == loadSize){
						logwriter.append(Constants.EOF); //this indicates that all GET requests have been seen without interruption from either user/server
					}
					String logString = logwriter.toString();
					String msg = "";
				    String retmsg = Threads.writeToLogFile(MainActivity.logfilename, logString); //write the log to file. This is a synchronized operation, only one thread can do it at a time
						
					msg += retmsg;
						
						
					if(num+1 == loadSize){
						Log.d(LOGTAG, "Now wrapping up the experiment");
						//Dummy ending of all requests - assuming only one request
						
						msg += "Experiment over : all GET/POST requests completed\n";
						//msg += "Trying to send log file\n";
					
						
					   Log.d(Constants.LOGTAG, "MyBrowser : Sending the log file");
						int ret = Threads.sendLog(MainActivity.logfilename);
						if(ret == 200){
							msg += "log file sent successfully\n";
						}
						else{
							msg += "log file sending failed\n";
						}
					}
					
					Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
					.putExtra(Constants.BROADCAST_MESSAGE, msg);
					
					// Broadcasts the Intent to receivers in this application.
					LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
					MainActivity.removeWebView(eventid); //remove the reference to current this webview so that it gets garbage collected
				}
			};
			
			Thread t = new Thread(r);
			
	        t.start();
       }
       //MainActivity.webview1.setVisibility(View.VISIBLE);
       //MainActivity.progressBar.setVisibility(View.GONE);
       //MainActivity.goButton.setText("GO");
       /*if(!MainActivity.js.isEmpty()){
    	   Log.d(LOGTAG, "onPageFinished() : loading js = " + MainActivity.js);
    	   MainActivity.webview1.loadUrl(MainActivity.js);
    	   MainActivity.js = "";
       }*/
       //MainActivity.textview.setText(MainActivity.js);
   }
}
