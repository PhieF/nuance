package com.spipi.spipimediaplayer;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.spipi.spipimediaplayer.deezer.DeezerLibraryUpdater;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {

    private String someVariable;
	private MediaPlayerService mp3Service;

	private List<Mp3ServiceBindListener> mMp3ServiceBindListener = new ArrayList<>();
	private static AppState.OnForeGroundListener sListener;


	public void enumerate(){
		Log.d("MyApplication", "mDeezerUpdateServiceBindListener "+mDeezerUpdateServiceBindListener.size());
		Log.d("MyApplication", "mCoverServiceBindListener "+mCoverServiceBindListener.size());
		Log.d("MyApplication", "mMp3ServiceBindListener "+mMp3ServiceBindListener.size());

	}
	public interface Mp3ServiceBindListener{
		public void onBind(MediaPlayerService lu);
		
	}
	private ServiceConnection mp3PlayerServiceConnection = new ServiceConnection() {
	        @Override
	        public void onServiceConnected(ComponentName arg0, IBinder binder) {
	            mp3Service =  ((MediaPlayerService.LocalBinder) binder).getService();
				Log.d("servicedebug", "created");
	            if(mMp3ServiceBindListener!=null) {
					for (Mp3ServiceBindListener listener : mMp3ServiceBindListener)
						listener.onBind(mp3Service);
					mMp3ServiceBindListener.clear();
				}
	        }

	        @Override
	        public void onServiceDisconnected(ComponentName arg0) {
	
	        }
	};


    
    public void bindToMp3Service(Mp3ServiceBindListener listener){
		Log.d("servicedebug", "starting1");

		if(mp3Service==null){
			mMp3ServiceBindListener.add(listener);

			//bind to our service by first creating a new connectionIntent
			Log.d("servicedebug", "starting");

			Intent connectionIntent = new Intent(this, MediaPlayerService.class);
	        bindService(connectionIntent, mp3PlayerServiceConnection,
	                Context.BIND_AUTO_CREATE);
    	}
    	else if(listener!=null)
			listener.onBind(mp3Service);
      }














	private List<CoverServiceBindListener> mCoverServiceBindListener = new ArrayList<>();
	private CoverUpdaterService mCoverService;

	public interface CoverServiceBindListener{
		public void onBind(CoverUpdaterService lu);

	}
	private ServiceConnection coverServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			mCoverService =  ((CoverUpdaterService.CoverLocalBinder) binder).getService();
			Log.d("servicedebug", "created");
			if(mCoverServiceBindListener!=null) {
				for (CoverServiceBindListener listener : mCoverServiceBindListener)
					listener.onBind(mCoverService);
				mCoverServiceBindListener.clear();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};



	public void bindToCoverService(CoverServiceBindListener listener){
		Log.d("servicedebug", "starting1");

		if(mCoverService==null){
			mCoverServiceBindListener.add(listener);

			//bind to our service by first creating a new connectionIntent
			Log.d("servicedebug", "starting");

			Intent connectionIntent = new Intent(this, CoverUpdaterService.class);
			bindService(connectionIntent, coverServiceConnection,
					Context.BIND_AUTO_CREATE);
		}
		else if(listener!=null)
			listener.onBind(mCoverService);
	}






















	public DeezerLibraryUpdater mDeezerUpdaterService;

	private List<DeezerUpdateServiceBindListener> mDeezerUpdateServiceBindListener = new ArrayList<>();
	public interface DeezerUpdateServiceBindListener{
		public void onBind(DeezerLibraryUpdater lu);

	}
	private ServiceConnection deezerUpdateServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			mDeezerUpdaterService =  ((DeezerLibraryUpdater.LULocalBinder) binder).getService();
			Log.d("servicedebug", "created");
			if(mDeezerUpdateServiceBindListener!=null) {
				for (DeezerUpdateServiceBindListener listener : mDeezerUpdateServiceBindListener)
					listener.onBind(mDeezerUpdaterService);
				mDeezerUpdateServiceBindListener.clear();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};



	public void bindToDeezerUpdateService(DeezerUpdateServiceBindListener listener){
		Log.d("servicedebug", "starting1");

		if(mDeezerUpdaterService==null){
			mDeezerUpdateServiceBindListener.add(listener);

			//bind to our service by first creating a new connectionIntent
			Log.d("servicedebug", "starting");

			Intent connectionIntent = new Intent(this, DeezerLibraryUpdater.class);
			bindService(connectionIntent, deezerUpdateServiceConnection,
					Context.BIND_AUTO_CREATE);
		}
		else if(listener!=null)
			listener.onBind(mDeezerUpdaterService);
	}


	@Override
	public void onCreate() {
		super.onCreate();
		/*registerActivityLifecycleCallbacks(AppState.sCallbackHandler);
		startService(new Intent(this,LibraryUpdater.class));
		StateService.register(this);
		NetworkCredentialsDatabase.getInstance().loadCredentials(this);
		sListener = new AppState.OnForeGroundListener() {
			@Override
			public void onForeGroundState(Context applicationContext, boolean foreground) {
				enumerate();
			}
		};
		AppState.addOnForeGroundListener(sListener);
		AppState.addOnForeGroundListener(FloatingService.listener);*/

	}
}