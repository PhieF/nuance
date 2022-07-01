package com.spipi.spipimediaplayer.deezer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.deezer.sdk.model.Artist;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.spipi.spipimediaplayer.MainActivity;
import com.spipi.spipimediaplayer.R;
import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerFactory;
import com.spipi.spipimediaplayer.playlists.PlayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class DeezerLibraryUpdater extends Service {

	Context ct;
	public final IBinder localBinder = new LULocalBinder();
	//private ArrayList<Updatable> updateList = new ArrayList<Updatable>();
	private boolean isUpdating;
	private boolean trackAlbum;
	private boolean track;
	private boolean trackArtist;
	private DeezerConnect deezerConnect;
	public DeezerLibraryUpdater(){

		this.ct = this;
		isUpdating=false;
	}
	@Override
	public void onCreate() {


		isUpdating=false;



	}
	@Override
	public void onStart(Intent intent, int startId) {
		//startLoop();

	}


	public void updateDeezerAlbums(long idArtist, String artistName){
		final ArrayList< Boolean> ended= new ArrayList< Boolean>();



		DeezerRequest request = DeezerRequestFactory.requestArtistAlbums(idArtist);

		try {
			String res = deezerConnect.requestSync(request);

			JSONObject obj = null;
			try {
				obj = new JSONObject(res);

				JSONArray arr = obj.getJSONArray("data");
				for (int i = 0; i < arr.length(); i++)
				{
					long albumID = arr.getJSONObject(i).getLong("id");
					String albumName = arr.getJSONObject(i).getString("title");
					updateDeezerTracks(artistName,albumID, albumName);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeezerError deezerError) {
			deezerError.printStackTrace();
		}


	}

	public void updateDeezerTracks(String artist,long albumId, String albumName){
		final ArrayList< Boolean> ended= new ArrayList< Boolean>();
		trackAlbum=true;

		DeezerRequest request = DeezerRequestFactory.requestAlbumTracks(albumId);

		try {
			String res = deezerConnect.requestSync(request);
			JSONObject obj = null;
			try {
				obj = new JSONObject(res);

				JSONArray arr = obj.getJSONArray("data");
				MusicDatasource md = new MusicDatasource(this);
				md.open();
				for (int i = 0; i < arr.length(); i++)
				{
					long trackID = arr.getJSONObject(i).getLong("id");
					int track = arr.getJSONObject(i).getInt("disk_number");
					String title = arr.getJSONObject(i).getString("title");
					md.addMusic(-(long)MediaPlayerFactory.TYPE_DEEZER,trackID+"",artist, albumName, title,track+"");
				}
				md.close();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeezerError deezerError) {
			deezerError.printStackTrace();
		}

	}

	public void updateDeezerTracks(final Long playlist, long playlistDatabaseID){
		final ArrayList< Boolean> ended= new ArrayList< Boolean>();
		trackAlbum=true;


		DeezerRequest request = DeezerRequestFactory.requestPlaylistTracks(playlist);

		try {
			String res = deezerConnect.requestSync(request);
			Log.d("deezerdebug",res);
			JSONObject obj = null;
			try {
				obj = new JSONObject(res);

				JSONArray arr = obj.getJSONArray("data");
				MusicDatasource md = new MusicDatasource(this);
				md.open();
				for (int i = 0; i < arr.length(); i++)
				{
					long trackID = arr.getJSONObject(i).getLong("id");
					int track = 1;
					String title = arr.getJSONObject(i).getString("title");
					String artist = arr.getJSONObject(i).getJSONObject("artist").getString("name");
					String albumName = arr.getJSONObject(i).getJSONObject("album").getString("title");
					Log.d("deezerdebug", "adding " + artist + albumName + title);

					md.addMusic(-(long) MediaPlayerFactory.TYPE_DEEZER, trackID + "", artist, albumName, title, track + "");
					if(playlistDatabaseID>=0){
						md.addToPlaylist(-(long)MediaPlayerFactory.TYPE_DEEZER,trackID + "",playlistDatabaseID);
					}
				}
				md.close();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeezerError deezerError) {
			deezerError.printStackTrace();
		}



	}
	public void updateDeezerTracks(){
		final ArrayList< Boolean> ended= new ArrayList< Boolean>();



		DeezerRequest request = DeezerRequestFactory.requestCurrentUserTracks();


		try {
			String res = deezerConnect.requestSync(request);
			JSONObject obj = null;
			try {
				obj = new JSONObject(res);

				JSONArray arr = obj.getJSONArray("data");
				Log.d("deezerdebug2",arr.toString());
				MusicDatasource md = new MusicDatasource(this);
				md.open();
				for (int i = 0; i < arr.length(); i++) {
					long trackID = arr.getJSONObject(i).getLong("id");
					int track = 1;
					String title = arr.getJSONObject(i).getString("title");
					String artist = arr.getJSONObject(i).getJSONObject("artist").getString("name");
					String albumName = arr.getJSONObject(i).getJSONObject("album").getString("title");

					md.addMusic(-(long) MediaPlayerFactory.TYPE_DEEZER, trackID + "", artist, albumName, title, track + "");
				}
				md.close();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeezerError deezerError) {
			deezerError.printStackTrace();
		}




	}
	public void updateDeezerAlbums(){
		final ArrayList< Boolean> ended= new ArrayList< Boolean>();



		DeezerRequest request = DeezerRequestFactory.requestCurrentUserAlbums();

		try {
			String res = deezerConnect.requestSync(request);
			JSONObject obj = null;
			try {
				obj = new JSONObject(res);

				JSONArray arr = obj.getJSONArray("data");
				for (int i = 0; i < arr.length(); i++)
				{
					long albumID = arr.getJSONObject(i).getLong("id");
					String albumName = arr.getJSONObject(i).getString("title");
					String artistName = arr.getJSONObject(i).getJSONObject("artist").getString("name");
					updateDeezerTracks(artistName, albumID, albumName);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeezerError deezerError) {
			deezerError.printStackTrace();
		}



	}
	public void updateDeezerPlaylists(){
		final ArrayList< Boolean> ended= new ArrayList< Boolean>();


		DeezerRequest request = DeezerRequestFactory.requestCurrentUserPlaylists();

		try {
			String res = deezerConnect.requestSync(request);
			Log.d("deezerdebug3",res);
			JSONObject obj = null;
			try {
				obj = new JSONObject(res);

				JSONArray arr = obj.getJSONArray("data");
				for (int i = 0; i < arr.length(); i++)
				{
					long albumID = arr.getJSONObject(i).getLong("id");
					String playlistName = arr.getJSONObject(i).getString("title");
					MusicDatasource md = new MusicDatasource(this);
					md.open();
					long id = md.addPlaylist(playlistName, PlayList.TYPE_DEEZER, null, System.currentTimeMillis());
					md.close();
					updateDeezerTracks(albumID, id);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeezerError deezerError) {
			deezerError.printStackTrace();
		}






	}
	public void updateDeezerPersonal() {
		final ArrayList<Boolean> ended = new ArrayList<Boolean>();


		DeezerRequest request = DeezerRequestFactory.requestCurrentUserPersonalSongs();

		try {
			String res = deezerConnect.requestSync(request);
			JSONObject obj = null;
			try {
				obj = new JSONObject(res);

				JSONArray arr = obj.getJSONArray("data");
				MusicDatasource md = new MusicDatasource(this);
				Log.d("deezerdebug",arr.toString());
				md.open();
				for (int i = 0; i < arr.length(); i++) {
					long trackID = arr.getJSONObject(i).getLong("id");
					int track = 1;
					String title = arr.getJSONObject(i).getString("title");
					String artist = arr.getJSONObject(i).getJSONObject("artist").getString("name");
					String albumName = arr.getJSONObject(i).getJSONObject("album").getString("title");

					md.addMusic(-(long) MediaPlayerFactory.TYPE_DEEZER, trackID + "", artist, albumName, title, track + "");
				}
				md.close();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeezerError deezerError) {
			deezerError.printStackTrace();
		}


	}
	public void updateDeezerArtists(){
		final ArrayList< Boolean> ended= new ArrayList< Boolean>();
		ended.add(new Boolean(false));

		RequestListener listener = new JsonRequestListener() {

			public void onResult(Object result, Object requestId) {
				List<Artist> artists = (List<Artist>) result;
				for(Artist art : artists){
					//updateDeezerAlbums(art.getId());
				}
				ended.add(0, new Boolean(false));

			}
			public void onUnparsedResult(String requestResponse, Object requestId) {}
			public void onException(Exception e, Object requestId) {}
		};
		DeezerRequest request = DeezerRequestFactory.requestCurrentUserArtists();

		try {
			String res = deezerConnect.requestSync(request);
			if(res!=null&&!res.isEmpty()){
				JSONObject obj = null;
				try {
					obj = new JSONObject(res);

					JSONArray arr = obj.getJSONArray("data");
					for (int i = 0; i < arr.length(); i++)
					{
						long artistId = arr.getJSONObject(i).getLong("id");
						String artistName = arr.getJSONObject(i).getString("name");
						updateDeezerAlbums(artistId, artistName);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeezerError deezerError) {
			deezerError.printStackTrace();
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
		notification.setContentText("Updating Deezer library");
		notification.setSubText("Please wait...");
		notification.setContentIntent(pi);

		startForeground(2, notification.getNotification());

	}
	public void updateDeezer(){
		startNotification();
		SessionStore sessionStore = new SessionStore();
		deezerConnect =  new DeezerConnect(this,DeezerWrapper.APP_ID);

		if (sessionStore.restore(deezerConnect, ct)) {
			Intent i = new Intent();
			/*updateDeezerArtists();

			i.setAction("LibraryUpdated");
			this.sendBroadcast(i);
			updateDeezerAlbums();
			i = new Intent();
			i.setAction("LibraryUpdated");
			this.sendBroadcast(i);
			updateDeezerTracks();
			i = new Intent();
			i.setAction("LibraryUpdated");
			this.sendBroadcast(i);*/
			updateDeezerPlaylists();
			updateDeezerPersonal();

			i = new Intent();
			i.setAction("LibraryUpdated");
			this.sendBroadcast(i);
		}
		stopForeground(true);
	}


	public void updateDeezerThread(){

		new Thread(){
			public void run(){
				updateDeezer();

			}
		}.start();

	}

	public class LULocalBinder extends Binder {
		public DeezerLibraryUpdater getService(){
			return DeezerLibraryUpdater.this;
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return localBinder;
	}


}
