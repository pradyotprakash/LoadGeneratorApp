package com.iitb.loadgenerator;


import java.io.File;
import java.net.ServerSocket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.rampo.updatechecker.notice.Notice;
import com.rampo.updatechecker.UpdateChecker;
import android.content.pm.PackageManager;

public class MainActivity extends ActionBarActivity {
	static TextView textbox; //scrollable texview displays important messages
	static EditText ipbox; //input ip
	static EditText portbox; //input port
	static EditText sessionidbox; //input session id
	static Button startbutton; //start button
//	static Button exitbutton; //exit button
	public static HashMap<Integer, WebView> webViewMap = new HashMap<Integer, WebView>();
	
	public static String logfilename;
	public static Context context;
	
	
	static String defaultServerIP = "10.129.5.155";
	static String defaultServerPort = "8080";
	static String defaultSessionId = "1";
	
	//following default values of ip, port and sessionid are not used.
	static String serverip = "10.129.5.155";
	static int serverport = 8080;
	static int sessionid = 1;
	static String myip;
	
	static long serverTimeDelta = 0; //(serverTime - clientTime)
	static boolean experimentOn = true; //whether to listen as server(session is on)
	static boolean running = false; //whether scheduling alarms and downloading is going on
	static int numDownloadOver = 0; //indicates for how many events download in thread is over
	
	static ServerSocket listen = null; //socket which listens to server for to receive control file,
									   //or commands such as stop experiment/ clear registration
	
	//Alarm specific
	static Load load = null; //this stores info about current experiment such as exp id and all events(get requests) with resp scheduled time
	static int currEvent = 0; //which event is currently being processed
	
	static AlarmManager am ;
	static WifiManager wifimanager;
	static SimpleDateFormat sdf = new SimpleDateFormat("ZZZZ HH:mm:s:S", Locale.US);
	
	static File logDir; //directory containing log files
	SharedPreferences sharedPreferences; //shared preferences simply serves as offline storage for last used ip, port	


	static int rssi;
	static String bssid;  
//	static int frequency;   // MHz
	static String  ssid;  
	static int linkSpeed;  // Mbps 




	 /*public void dialog(View view) {
        UpdateChecker checker = new UpdateChecker(this);
        checker.start(); 

        //
        //
        //https://github.com/rampo/UpdateChecker/blob/master/CUSTOMIZATION.md#setsuccessfulchecksrequiredint-checksrequired
    }*/


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//dialog();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	

 		UpdateChecker checker = new UpdateChecker(this);
		checker.setNotice(Notice.DIALOG);
		checker.setSuccessfulChecksRequired(1);
        checker.start();
 


		//init views
		textbox = (TextView) findViewById(R.id.response_id);
		ipbox = (EditText) findViewById(R.id.serverip);
		portbox = (EditText) findViewById(R.id.serverport);
		sessionidbox = (EditText) findViewById(R.id.sessionid);
		
		/*webview1 = (WebView) findViewById(R.id.webview1);
		webview1.setWebViewClient(new MyBrowser());
		WebSettings settings1 = webview1.getSettings();
		settings1.setJavaScriptEnabled(true);*/
		//webview1.setVisibility(View.GONE);
		//webview1.loadUrl("http://www.cse.iitb.ac.in");
		context = getApplicationContext();
		
		/*CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();*/
		
		//fill port and ip from shared prefs
		sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
		if(sharedPreferences.contains(Constants.keyServerAdd)){
			ipbox.setText(sharedPreferences.getString(Constants.keyServerAdd, ""));
		}
		else{
			ipbox.setText(defaultServerIP);
		}
		if(sharedPreferences.contains(Constants.keyServerPort)){
			portbox.setText(sharedPreferences.getString(Constants.keyServerPort, ""));
		}
		else{
			portbox.setText(defaultServerPort);
		}
		if(sharedPreferences.contains(Constants.keyServerSession)){
			sessionidbox.setText(sharedPreferences.getString(Constants.keyServerSession, ""));
		}
		else{
			sessionidbox.setText(defaultSessionId);
		}

		startbutton = (Button) findViewById(R.id.startbutton);
//		exitbutton = (Button) findViewById(R.id.stopbutton);
		
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifimanager.getConnectionInfo();

		rssi = info.getRssi();
		bssid = info.getBSSID();  
//		frequency = info.getFrequency();   // MHz
		ssid = info.getSSID();  
		linkSpeed = info.getLinkSpeed();   // Mbps 
		
		//logDirectory is defined in Constants class. make the directory if not already exists
		logDir = new File(Constants.logDirectory);
		logDir.mkdirs();
		
		//Set the orientation to portrait
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //hide keyboard until actually needed 
		
		//Register Broadcast receiver. To receive messages which needs to be displayed on screen
		IntentFilter broadcastIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION);
        
        ResponseReceiver broadcastReceiver = new ResponseReceiver(new Handler());
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, broadcastIntentFilter);
        
        //Register AlarmReceiver.
        IntentFilter alarmIntentFilter = new IntentFilter(Constants.BROADCAST_ALARM_ACTION);
        AlarmReceiver alarmReceiver = new AlarmReceiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(alarmReceiver, alarmIntentFilter);
        
        //check network is connected or not. if not show networkError dialog
        if(!isNetworkAvailable()){
        	showNetworkErrorDialog();
        }
        
        textbox.append("\nNetwork status checked using ConnectivityManager");
        
	}
	
	//reset all alarms, load, etc. Continue to listen for new experiment again from scratch
	public static void reset(Context ctx){
		if(running){
			load = null;
			currEvent = 0;
			running = false;
			numDownloadOver = 0;
			
			//cancel scheduled alarms
			Intent intent = new Intent(ctx, AlarmReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(ctx, Constants.alarmRequestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			am.cancel(sender);
			
			cancelTimeoutAlarm(ctx);
		}
	}
	
	@Override
	public void onBackPressed() {
	   Log.d(Constants.LOGTAG, "onBackPressed Called");
	   Intent setIntent = new Intent(Intent.ACTION_MAIN);
	   setIntent.addCategory(Intent.CATEGORY_HOME);
	   setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	   startActivity(setIntent);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	boolean isEmpty(EditText et){
		if(et.getText().toString().toString().trim().length() > 0) return false;
		return true;
	}
	
	//function called on pressing start button
	public void startService(View v){
		hideKeyBoard();
		//check if input boxes are empty
		if(isEmpty(ipbox)){
			Toast.makeText(this, "Please enter ip", Toast.LENGTH_SHORT).show();
			return;
		}
		if(isEmpty(portbox)){
			Toast.makeText(this, "Please enter port", Toast.LENGTH_SHORT).show();
			return;
		}
		if(isEmpty(sessionidbox)){
			Toast.makeText(this, "Please enter sessionid", Toast.LENGTH_SHORT).show();
			return;
		}
		serverip = ipbox.getText().toString();
		serverport = Integer.parseInt(portbox.getText().toString());
		sessionid = Integer.parseInt(sessionidbox.getText().toString());
		
		//every time start button is pressed
		experimentOn = true;
		
		//store ip and port in shared prefs
		Editor editor = sharedPreferences.edit();
	    editor.putString(Constants.keyServerAdd, serverip);
	    editor.putString(Constants.keyServerPort, Integer.toString(serverport));
	    editor.commit();
		
		textbox.setText("");
		textbox.append("Server : IP = " + serverip + " | Port = " + serverport + "\n");
		textbox.append("My IP = " + Utils.getIP() + "\n");
		textbox.append("My MAC Address = " + Utils.getMACAddress() + "\n");
		textbox.append("Connected BSSID = " + MainActivity.bssid + "\n");

   try { 
             
             textbox.append("Installed App Version : " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n");
        } catch (PackageManager.NameNotFoundException ignored) { 

        } 
 
		Intent mServiceIntent = new Intent(this, BackgroundService.class);
    	startbutton.setEnabled(false); //disable start button. At a time only one session can run
    //	exitbutton.setEnabled(false); //disable exit button.

    	startService(mServiceIntent); //start BackgroundService which is the main service thread running in background
    	
    	setKillTimeoutAlarm(this); //since session has started,
    						   //start killer alarm timer which will close the session after certain time
		 
	}
	
	public void useDefaultServer(View v){
		hideKeyBoard();
		ipbox.setText(defaultServerIP);
		portbox.setText(defaultServerPort);
		sessionidbox.setText(defaultSessionId);
		Toast.makeText(this, "default ip/port set", Toast.LENGTH_SHORT).show();
	}
	
	//exit app. But before exiting, send the server that you are exiting
	public void exit(View v){
		hideKeyBoard();
		new AlertDialog.Builder(this)
		.setTitle("Exit")
		.setMessage("Do you want to exit?")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		        //Toast.makeText(MainActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
		        Log.d(Constants.LOGTAG, "creating asynctask : ExitTask");
				new ExitTask().execute();
		    }})
		 .setNegativeButton(android.R.string.no, null).show();
	}
	
	//ExitTask sends the exit signal to server telling that it is going to exit so that server can update its info about available devices
	 private class ExitTask extends AsyncTask<URL, Integer, Integer> {
	     protected Integer doInBackground(URL... urls) {
	         Utils.sendExitSignal();
	         return 0;
	     }

	     protected void onProgressUpdate(Integer... progress) {
	     }

	     protected void onPostExecute(Integer result) {
	    	 Log.d(Constants.LOGTAG, "killing self");
	    	 finish();
	         android.os.Process.killProcess(android.os.Process.myPid());
	     }
	 }
	
	//checks if device is connected to wifi
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    
	    NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

	    if (mWifi.isConnected()) {
	    	return true;
	    }
	    else return false;
	}
	
	
	//this shows network error dialog box. On cliking Settings button takes to wifi settings page. On cliking cancel, exits the app
	private void showNetworkErrorDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("You need a wifi network connection to use this application. Please turn on Wi-Fi in Settings.")
	        .setTitle("Unable to connect")
	        .setCancelable(false)
	        .setPositiveButton("Settings",
	        new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
	            }
	        }
	    )
	    .setNegativeButton("Cancel",
	        new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                finish();
	            }
	        }
	    );
	    AlertDialog alert = builder.create();
	    alert.show();
	}
	
	
	//sets the kill timeout once the session has started specified by Constants.killTimeoutDuration. 
	//Once alarms is triggered, it stops the current session closing all sockets and alarms 
	//and returns to default state as if app has newly started.
	static void setKillTimeoutAlarm(Context context){
		Log.d("DEBUG_MAIN_ACTIVITY", "restart timeout alarm " + Constants.killTimeoutDuration);
		Intent timeoutintent = new Intent(context, AlarmReceiver.class);
		timeoutintent.putExtra("killtimeout", 200);
		PendingIntent timeoutsender = PendingIntent.getBroadcast(context, Constants.timeoutAlarmRequestCode, timeoutintent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		Calendar cal = Calendar.getInstance(); //here we want actual calendar instance(local time)
		cal.add(Calendar.MINUTE, Constants.killTimeoutDuration);
		Log.d("DEBUG_MAIN_ACTIVITY", "Scheduling KILLTIMEOUT @" + MainActivity.sdf.format(cal.getTime()) + "\n");
		
		MainActivity.am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), timeoutsender);
	}
	
	static void cancelTimeoutAlarm(Context ctx){
		Log.d("DEBUG_MAIN_ACTIVITY", "cancel timeout alarm ");
		//cancel timeout alarm
		Intent timeoutintent = new Intent(ctx, AlarmReceiver.class);
		PendingIntent timeoutsender = PendingIntent.getBroadcast(ctx, Constants.timeoutAlarmRequestCode, timeoutintent, PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(timeoutsender);
	}
	
	synchronized public static void removeWebView(int eventid){
		webViewMap.remove(eventid);
	}
	
	void hideKeyBoard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(ipbox.getWindowToken(), 0);
	}
}
