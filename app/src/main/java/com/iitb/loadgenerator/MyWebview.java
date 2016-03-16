package com.iitb.loadgenerator;


import android.webkit.WebView;
import android.content.Context;
import android.graphics.Bitmap;


/**
 * Created by RATHEESH on 3/11/2016.
 */
public class MyWebview  extends WebView{

			static StringBuilder logwriter  = new StringBuilder(); 


			MyWebview(Context ctx){
				super(ctx);
			}


			@Override		
				public Bitmap  getFavicon () {
	        		String _url = this.getUrl();
                    logwriter.append("Inside getFavicon : "+_url+" \n");
					if(_url.contains("favicon")){
						//	webview.stopLoading();
						this.stopLoading();
						}
					return null;
					}
}

