package com.iitb.loadgenerator;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;


public class ResponseReceiver extends WakefulBroadcastReceiver
{
	private Handler handler;

	public ResponseReceiver() {
    	;
    }
	
    // Prevents instantiation
    ResponseReceiver(Handler h) {
    	handler = h;
    }
    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    
    @Override
    public void onReceive(final Context context, Intent intent) {
    	Bundle bundle = intent.getExtras();
    	final int killtimeout = bundle.getInt("killtimeout");
    	
    	Log.d(Constants.LOGTAG, "ResponseReceiver called " + Integer.toString(killtimeout));
    	
    	if(killtimeout == 200){//the broadcast was to handle kill self event.
    		handler.post(new Runnable() {
	            @Override
	            public void run() {
	            	MainActivity.reset(context); //clear all alarms and everything related to current activity
	            	
	            	MainActivity.experimentOn = false;
					if(MainActivity.listen != null){ //close the listen server
						try {
							MainActivity.listen.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					MainActivity.startbutton.setEnabled(true);
					MainActivity.textbox.append("\n TIMEOUT closing current experiment running status.\n Start Again!\n");
	            }
	        });
    	}
    	else{//broadcast was just to display a message on screen
	    	
	    	final String msg = (String) bundle.getString(Constants.BROADCAST_MESSAGE);
	    	final int enable = bundle.getInt("enable"); //returns 0 if no suck key exists
	    	Log.d("On Receive", msg);
	    	handler.post(new Runnable() {
	            @Override
	            public void run() {
	                //MainActivity.button.setEnabled(true);
	            	//Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	            	MainActivity.textbox.append("\n" + msg + "\n");
	            	if(enable == 1) {
	            		MainActivity.reset(context);
	            		MainActivity.startbutton.setEnabled(true);
	            	}
	            }
	        });
    	}
    }
}
