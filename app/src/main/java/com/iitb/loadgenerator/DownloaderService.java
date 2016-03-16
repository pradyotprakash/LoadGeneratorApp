package com.iitb.loadgenerator;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.graphics.Bitmap;

//called by alarm receiver to start serving next download event
public class DownloaderService extends IntentService{
	public DownloaderService() {
		super("DownloaderService");
		// TODO Auto-generated constructor stub
	}

	@Override
    protected void onHandleIntent(Intent intent) {
		if(!MainActivity.running){
			Log.d(Constants.LOGTAG, "DownloaderService : entered. But experiment not running");
			return;
		}
		
		if(MainActivity.load == null){
    		Log.d(Constants.LOGTAG, "DownloaderService : load null");
    		return;
    	}
		
		Log.d(Constants.LOGTAG, "DownloaderService : just entered");
		Bundle bundle = intent.getExtras();
        final int eventid = bundle.getInt("eventid");
        final RequestEvent event = MainActivity.load.events.get(eventid);
        
        //final RequestEvent e = MainActivity.load.events.get(eventid);
        
		Log.d(Constants.LOGTAG, "DownloaderService : Handling event " + eventid + "in a thread ... ");
		
		boolean webviewon = true;
		if(event.mode == DownloadMode.SOCKET){
	        
			Runnable r = new Runnable() {
				public void run() {
					Threads.HandleEvent(eventid, getApplicationContext());
				}
			};
			
			Thread t = new Thread(r);
			
	        t.start();
		}
		else if(event.mode == DownloadMode.WEBVIEW){


			//final String url = event.url;
			MainActivity.logfilename = "" + MainActivity.load.loadid;
			
			Log.d(Constants.LOGTAG, "HandleEvent : just entered thread");
			
			MainActivity.textbox.post(new Runnable() {
				
				@Override
				public void run() {

				//	MyWebview webview = new MyWebview(getApplicationContext());

					WebView webview = new WebView(getApplicationContext()) {

								@Override
										public Bitmap  getFavicon () {

											String _url = this.getUrl();
											MyBrowser.writeToFile(_url);
											Bundle bundle = new Bundle();
											bundle.putString(Constants.BROADCAST_MESSAGE,"FAVICON : "+_url);
											if(_url.contains("favicon")){
											//	webview.stopLoading();
												this.stopLoading();
											}
										return null;
								}
					};
					
					webview.setWebViewClient(new MyBrowser(eventid, event.url));
					WebSettings settings = webview.getSettings();

					//settings.setJavaScriptEnabled(true);
					//settings.setJavaScriptEnabled(false);
					//settings.setPluginsEnabled(false);
					
					MainActivity.webViewMap.put(eventid, webview);
					webview.loadUrl(event.url+"##"+event.postDataSize+"##"+event.type);	

					/*
					//webview.clearAnimation();					
					String _html = "<head><link rel=\"shortcut icon\" href=\"data:image/x-icon;,\" type=\"image/x-icon\"></head>";
					webview.loadDataWithBaseURL(event.url+"##"+event.postDataSize+"##"+event.type,
												_html,
												"text/html",
												"UTF-8",
												"about:blank"
												);

//					<head><link rel=shortcut icon" href="data:image/x-icon;," type="image/x-icon"></head> 
//					webview.loadUrl(event.url+"##"+event.postDataSize+"##"+event.type);
//					MyBrowser.getResource(event.url+"##"+event.postDataSize+"##"+event.type);
//					webview.getResource(event.url+"##"+event.postDataSize+"##"+event.type);
*/
				}
			});
		}
		else{
			Log.d(Constants.LOGTAG, "Incorrect Download mode specified");
		}
		
		AlarmReceiver.completeWakefulIntent(intent);
	}
}