package com.spipi.spipimediaplayer.deezer;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;


import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.spipi.spipimediaplayer.MyApplication;



public class DeezerWrapper {
	public static final String APP_ID="148751";
    private static DeezerLibraryUpdater mDeezer = null;
	public  static void connect(final Application app, final Activity act){

        ((MyApplication) app).bindToDeezerUpdateService(new MyApplication.DeezerUpdateServiceBindListener() {
            @Override
            public void onBind(DeezerLibraryUpdater lu) {
                mDeezer = lu;
            }
        });

        final DeezerConnect deezerConnect = new DeezerConnect(app, APP_ID);
		String[] permissions = new String[] {
				Permissions.BASIC_ACCESS,
				Permissions.MANAGE_LIBRARY,
				Permissions.OFFLINE_ACCESS,
				Permissions.LISTENING_HISTORY };
				 
				// The listener for authentication events
				DialogListener listener = new DialogListener() {
				 
				public void onComplete(Bundle values) {
					SessionStore sessionStore = new SessionStore();
					sessionStore.save(deezerConnect, act);
                    if(mDeezer!=null)
                        mDeezer.updateDeezerThread();
					act.finish();

				}
				 	
				public void onCancel() {		act.finish();
}
				 
				public void onException(Exception e) {		act.finish();
}
				};
				 
				// Launches the authentication process
				System.out.println("authorizing");
				deezerConnect.authorize(act, permissions, listener);
	}
}
