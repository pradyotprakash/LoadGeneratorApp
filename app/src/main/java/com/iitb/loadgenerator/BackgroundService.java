package com.iitb.loadgenerator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

//Called when start button is pressed. Registers itself and spawns listen server thread.
public class BackgroundService extends IntentService{
	
	public BackgroundService() {
		super("BackgroundService");
	}
	
	@Override
    protected void onHandleIntent(Intent workIntent) {
		//Trigger thread to send log files pending
		Runnable logSender = new Runnable() {
			public void run() {
				Threads.sendLogFilesBackground(getApplicationContext());
			}
		};
		Thread logSenderThread = new Thread(logSender);
		
	    logSenderThread.start();
	    
	    //Now resume normal flow of sending device registration request
		String msg = "Now Listening ... ";
		int toEnableStart = 0; //whether or not to enable start button

		try
		{
			//ping check 3 times
			for(int i=0; i<3 ;i++){
				boolean success = Utils.ping(MainActivity.serverip);
				Log.d(Constants.LOGTAG, "ping attempt=" + i + " ;result="+ success + "\n");
				if(success) break;
			}
			
			//Create listen socket before sending device info since we need to send listen port also
			MainActivity.listen = new ServerSocket(0);
			MainActivity.listen.setSoTimeout(10000);
			
			int status = sendDeviceInfo();
			
			if(status == 408){
				toEnableStart = 1;
				msg = "Could not contact server. Probably network error. Closing listen socket. Try again";
				MainActivity.listen.close();
			}
			else if(status != 200){
				toEnableStart = 1;
				msg = "Registration Request rejected(maybe window closed). Closing listen socket. Try again";
				MainActivity.listen.close();
			}
			else{
				final Context ctx = getApplicationContext();
				
				Runnable r = new Runnable() {
					public void run() {
						Threads.ListenServer(ctx);
					}
				};
				Thread t = new Thread(r);
				
		        t.start();
			}
		}catch(IOException e)
		{
			toEnableStart = 1;
			msg = "Can create listen socket.";
			try {
				MainActivity.listen.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		Bundle bundle = new Bundle();
		bundle.putInt("enable", toEnableStart);
		bundle.putString(Constants.BROADCAST_MESSAGE,msg);
        //on complete  
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
        					.putExtras(bundle);
        	
        // Broadcasts the Intent to receivers in this application.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
	
	//Registers itself by sending its device info such as ip, port, memory, processor, signal strength, etc
	public static int sendDeviceInfo(){
		HttpClient client = Utils.getClient();
		String url = "http://" + MainActivity.serverip + ":" + MainActivity.serverport + "/" + Constants.SERVLET_NAME + "/registerClient.jsp";
		Log.d(Constants.LOGTAG, url);
		HttpPost httppost = new HttpPost(url);
		List <NameValuePair> params = Utils.getMyDetailsJson(MainActivity.listen);
		int statuscode = 408; //default if no response from server due to some cause(timeout, io error)
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));
			Log.d(Constants.LOGTAG, "Trying to send device info. Params set");
			
			try {
		         HttpResponse response = client.execute(httppost);
		         statuscode = response.getStatusLine().getStatusCode(); //will get 200 only if registration success.
		         
		         String responseBody = EntityUtils.toString(response.getEntity());
		         Log.d(Constants.LOGTAG, statuscode+"");
		         return statuscode; //TODO change this to handle rejection of registration
		    } catch (ClientProtocolException e) {
		         e.printStackTrace();
		    } catch (IOException e) {
		         e.printStackTrace();
		    }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return statuscode;
	}
}
