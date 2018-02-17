package com.spipi.spipimediaplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.spipi.spipimediaplayer.hubic.hubicLibraryUpdate;
import com.spipi.spipimediaplayer.network.NetworkLibraryUpdate;

import java.util.ArrayList;
import java.util.List;


public class LibraryUpdater extends Service {
	public  static LibraryUpdater sLibraryUpdater;
	Context ct;
	public final IBinder localBinder = new LULocalBinder();
	private boolean isUpdating;
	private final static String TAG = "NetworkLibraryUpdate";
	private boolean trackAlbum;
	private boolean track;
	private boolean trackArtist;
	List<UpdateListener>mListeners;
    private boolean mIsUpdating = false;

    public interface UpdateListener{
		public void onUpdate(String artist, String album, String music);
	}
	public LibraryUpdater(){
		sLibraryUpdater = this;
		this.ct = this;
		isUpdating=false;
	}
	@Override
	public void onCreate() {
		mListeners = new ArrayList<>();
		isUpdating=false;
		//startLoop();

	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void addListener(UpdateListener listener) {
		mListeners.add(listener);
	}
	public void removeListener(UpdateListener listener) {
		mListeners.remove(listener);
	}





	public void startNotification(String text){


		Intent it =  new Intent(getApplicationContext(), MainActivity.class);
		it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
				Intent.FLAG_ACTIVITY_SINGLE_TOP |
				Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
				it,
				0);
		Notification.Builder notification = new Notification.Builder(getApplicationContext());
		notification.setContentTitle("Spipi Music Player");
		notification.setSmallIcon(R.drawable.ic_icon);
		//notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.setContentText("Updating library");
		notification.setSubText(text);
		notification.setContentIntent(pi);

		startForeground(2, notification.getNotification());

	}
	private void updateListeners(String artist, String album, String title) {
		for(UpdateListener listener : mListeners){
			listener.onUpdate(artist, album, title);
		}
	}

	public void updateDistant(){
		new Thread(){
			public void run(){
                if(!mIsUpdating) {
                    mIsUpdating=true;
                    startNotification("Updating Library");
                    NetworkLibraryUpdate networkLibraryUpdate = new NetworkLibraryUpdate(LibraryUpdater.this);
                    networkLibraryUpdate.updateDistantNoThread();
                    hubicLibraryUpdate hubicLibraryUpdate = new hubicLibraryUpdate(LibraryUpdater.this);
                    hubicLibraryUpdate.updateDistantNoThread();
                    stopForeground(true);
                    mIsUpdating = false;
                }


			}
		}.start();
	}
	public class LULocalBinder extends Binder {
		public LibraryUpdater getService(){
			return LibraryUpdater.this;
		}
	}

}





