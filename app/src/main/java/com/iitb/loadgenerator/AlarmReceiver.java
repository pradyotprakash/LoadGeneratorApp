package com.iitb.loadgenerator;


import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

//Triggered when alarm is received. Handles two events : 1) kill timeout(time to stop session) 
//   and 2) normal alarm event to process next download event using DownloaderService 
public class AlarmReceiver extends WakefulBroadcastReceiver
{
    // Prevents instantiation
	public AlarmReceiver() {
    }
	
    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    
    @Override
    public void onReceive(final Context context, Intent intent) {
    	Bundle bundle = intent.getExtras();
    	int killtimeout = bundle.getInt("killtimeout");
    	
    	if(killtimeout == 200){//Need to kill current running session. Send a broadcast to do so
    		Log.d(Constants.LOGTAG, "Alarm Receiver : kill time out");
			bundle.putString(Constants.BROADCAST_MESSAGE,"## Control file received. Setting up alarms\n");
	        
	        Intent local = new Intent(Constants.BROADCAST_ACTION)
	        					.putExtras(bundle);
	        LocalBroadcastManager.getInstance(context).sendBroadcast(local);
    	}
    	else{
	    	if(!MainActivity.running){
	    		Log.d(Constants.LOGTAG, "Alarm Receiver : alarm just received. But experiment not running");
	    		return;
	    	}
	    	
	        int eventid = bundle.getInt("eventid");
	        
	        if(eventid >= 0){
	        	Log.d(Constants.LOGTAG, "Alarm Receiver : alarm just received (eventid=" + eventid + ") Now preparing to handle event");
		    	Intent callingIntent = new Intent(context, DownloaderService.class);
		
		        callingIntent.putExtra("eventid", (int)eventid);
		        
		        startWakefulService(context, callingIntent);
		        Log.d(Constants.LOGTAG, "Alarm Receiver : started the Downloader Service");
	        }
	        else{
	        	Log.d(Constants.LOGTAG, "Alarm Receiver :(eventid=" + eventid + ") Setting up first alarm");
	        }
			
			scheduleNextAlarm(context);
    	}
    }
 
    //Looks at next event from eventlist and schedules next alarm
    void scheduleNextAlarm(Context context){
    	if(!MainActivity.running){
    		Log.d(Constants.LOGTAG, "scheduleNextAlarm : Experiment not 'running'");
    		return;
    	}
    	
    	if(MainActivity.load == null){
    		Log.d(Constants.LOGTAG, "scheduleNextAlarm : load null");
    		return;
    	}
    	
		if(MainActivity.currEvent >= MainActivity.load.events.size()) {
			Log.d(Constants.LOGTAG, "scheduleNextAlarm : All alarms over. Curr Experiment finished");
			return;
		}
		
		RequestEvent e = MainActivity.load.events.get(MainActivity.currEvent);
		
		
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("eventid", (int)MainActivity.currEvent);
		
		PendingIntent sender = PendingIntent.getBroadcast(context, Constants.alarmRequestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		
		//just for now while control file is getting ready
		Calendar cal = Utils.getServerCalendarInstance();
//		MainActivity.am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + 5000, sender);
		Log.d(Constants.LOGTAG, MainActivity.sdf.format(cal.getTime()) + "Scheduling " + MainActivity.currEvent + "@" + MainActivity.sdf.format(e.cal.getTime()) + "\n");
		

		MainActivity.am.set(AlarmManager.RTC_WAKEUP, e.cal.getTimeInMillis() - MainActivity.serverTimeDelta, sender); 
		/*
		 * [event_time_stamp - (server - local)] gives when alarm should be scheduled. 
		 * Why ? Details ahead : 
		 * For e.g if local 2.00, server 2.10. Difference (server - local) = 10
		 * Now server says schedule alarm at 2.15. Alarms follow local time. 
		 * So now according to local time, alarm should be scheduled at 
		 * time 2.05 (because at that moment servertime will be 2.05 + 10 = 2.15)
		 */
		
		MainActivity.currEvent++;
	}
}