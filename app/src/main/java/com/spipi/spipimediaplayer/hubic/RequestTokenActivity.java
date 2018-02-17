package com.spipi.spipimediaplayer.hubic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.spipi.spipimediaplayer.LibraryUpdater;
import com.spipi.spipimediaplayer.R;


public class RequestTokenActivity extends Activity  {
	private WebView webView;
	private HubicAccess acc;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request_hubic_token);
		// Get the message from the intent
		Bundle bundle = getIntent().getExtras();

        if(bundle.getString("NAME")!= null)
        {
        	acc = new HubicAccess(bundle.getString("NAME"));
    		webView = (WebView) findViewById(R.id.webView1);
    		
    		//new Thread(rtt).start();
    		webView.setWebViewClient(new WebViewClient(){
    			
    			
    			
    			
    			public boolean shouldOverrideUrlLoading(WebView view, String url)
    			   {
   
    			         // This line we let me load only pages inside Firstdroid Webpage
    				  if(url!=null){
    						Uri uri= Uri.parse(url);
    						if(uri.getHost().contains("localhost")){

    							setAccessCode(uri.getQueryParameter("code"));
    						}
    					}
    			    return false;    
    			   // Return true to override url loading (In this case do nothing).
    		
    			    }
    		}
    		
    				
    				
    				);
    		webView.getSettings().setJavaScriptEnabled(true);
    		webView.loadUrl(acc.getRequestTokenURL());
        	
        }
		
	}

	public void setAccessCode(final String code){
		ProgressDialog dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
		final AccessDatasource ads = new AccessDatasource(this.getApplicationContext());
		ads.open();

		Thread t = new Thread() {
			@Override
			public void run() {
				acc.retrieveRefreshAndAccessToken(code);
				try {
					acc.retrieveOpenStackAccessToken();
					


					acc.setId(ads.addAccount(acc).getId());
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			};
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        LibraryUpdater.sLibraryUpdater.updateDistant();
			dialog.dismiss();
			this.finish();
		
		
		
	}
	





}
