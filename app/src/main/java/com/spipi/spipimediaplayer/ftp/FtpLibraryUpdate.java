package com.spipi.spipimediaplayer.ftp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Binder;
import android.os.IBinder;

import com.spipi.spipimediaplayer.MainActivity;
import com.spipi.spipimediaplayer.MusicItem;
import com.spipi.spipimediaplayer.R;
import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.hubic.HubicAccess;
import com.spipi.spipimediaplayer.hubic.AccessDatasource;
import com.spipi.spipimediaplayer.hubic.OpenstackConnector;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FtpLibraryUpdate extends Service {
	Context ct;
	public final IBinder localBinder = new LULocalBinder();
	private boolean isUpdating;
	private boolean trackAlbum;
	private boolean track;
	private boolean trackArtist;
	List<UpdateListener>mListeners;
	public interface UpdateListener{
		public void onUpdate(String artist, String album, String music);
	}
	public FtpLibraryUpdate(){

		this.ct = this;
		isUpdating=false;
	}
	@Override
	public void onCreate() {
		mListeners = new ArrayList<>();
		isUpdating=false;
		//startLoop();

	}
	public void addListener(UpdateListener listener) {
		mListeners.add(listener);
	}
	public void removeListener(UpdateListener listener) {
		mListeners.remove(listener);
	}
	@Override
	public void onStart(Intent intent, int startId) {
		//startLoop();

	}
	/*public void startLoop(){
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		new Thread(){
			public void run(){
				Boolean syncConnPref = sharedPref.getBoolean("pref_sync_over_mobile", false);
				while(true){

					if( syncConnPref || !connect.isConnectedMobile()){
						updateDistantNoThread();
					}
					//updateLocal();
					try {
						Thread.sleep(2 * 60 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					syncConnPref = sharedPref.getBoolean("pref_sync_over_mobile", false);
				}


			}
		}.start();


	}*/


	public void updateDistantNoThread(){

		if(!isUpdating){
			startNotification();
			isUpdating=true;
			if(ct!=null){
				AccessDatasource ads= new AccessDatasource(ct);
				ads.open();
				MusicDatasource md = new MusicDatasource(ct);

				/*List<Access> acc = ads.getAllAccounts();
				ads.close();

				for(int i=0; i< acc.size(); i++){

					FtpConnector oc = new FtpConnector(acc.get(i));
					oc.setRoot("default");
					try {
						ArrayList<String> dir = oc.listFolderAndSubs("");
						if (dir == null){
							acc.get(i).getType()
							acc.get(i).refreshAccessToken();
							acc.get(i).retrieveOpenStackAccessToken();
							ads.open();
							ads.addAccount(acc.get(i));
							ads.close();
							oc = new OpenstackConnector(acc.get(i));
							oc.setRoot("default");
							dir = oc.listFolderAndSubs("");
						}


						md.open();

						for (String s : dir){

							//if(!ins.contains(s)){
							if(s.toLowerCase().endsWith("mp3") ||s.toLowerCase().endsWith("flac")||s.toLowerCase().endsWith("ogg")) {
								md.open();

								MusicItem mm = md.getMusicItem(acc.get(i).getId(),s);
								md.close();
								if(mm==null){
									File f = new File(s);
									MediaMetadataRetriever fm = new MediaMetadataRetriever();
									Map<String, String> header = new HashMap<String, String>();
									header.put(oc.getAuthentificationHeaders().get(0).getName(),oc.getAuthentificationHeaders().get(0).getValue());
									String s2=s;
									try {

										s2= URLEncoder.encode(s, "UTF-8").replace("+", "%20");

									} catch (UnsupportedEncodingException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									try
									{
										fm.setDataSource(oc.getFullURL()+s2, header);

										String artist= fm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
										String album =fm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
										String title = fm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
										String track = fm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER);
										if(track == null)
											track = ""+0;
										Integer t = new Integer(track);

										if(title==null){
											title = f.getName();
										}
										if(artist==null)
											artist = "<unknown>";
										if(album==null)
											album = "<unknown>";

										md.open();
										md.addMusic(acc.get(i).getId(), s, artist, album, title, track);
										md.close();
										updateListeners( artist, album, title);

									}
									catch (Exception e){

									}
								}



							}
						}
						//}



					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}*/
			}
			stopForeground(true);
			isUpdating=false;
		}
	}
	private void startNotification(){


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
		notification.setSubText("Please wait...");
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
				updateDistantNoThread();
				Intent i = new Intent();
				i.setAction("LibraryUpdated");
				sendBroadcast(i);

			}
		}.start();
	}
	public class LULocalBinder extends Binder {
		public FtpLibraryUpdate getService(){
			return FtpLibraryUpdate.this;
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return localBinder;
	}
}/*
	public void updateLocal(){
		ContentResolver contentResolver = ct.getContentResolver();
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		MusicDatasource md = new MusicDatasource(ct);
		System.out.println("bla");
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		if (cursor == null) {
		    // query failed, handle error.
		} else if (!cursor.moveToFirst()) {
		    // no media on the device
		} else {
		    int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
		    int artistColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
		    int albumColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM);
		    int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
		    do {
		       long thisId = cursor.getLong(idColumn);
		       String thisTitle = cursor.getString(titleColumn);
		       String thisArtist = cursor.getString(artistColumn);
		       String thisAlbum = cursor.getString(albumColumn);

		       MusicItem mi = new MusicItem("-1",thisId+"",thisArtist,thisAlbum,thisTitle,false);
		       md.open();
		       md.addAccount(mi);//
		       md.close();
		      // getArtistPic(thisArtist);
		      // getAlbumPic(thisArtist,  thisAlbum);
		       updateUpdatables(mi);
		    } while (cursor.moveToNext());
		}
	}
	 */





