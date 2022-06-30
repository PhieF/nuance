package com.spipi.spipimediaplayer.hubic;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.spipi.spipimediaplayer.Access;
import com.spipi.spipimediaplayer.LibraryUpdater;
import com.spipi.spipimediaplayer.MusicItem;
import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.library.NetworkLibraryUpdate;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class hubicLibraryUpdate  {
	private final LibraryUpdater updater;
	Context ct;
	private static final String TAG = "hubicLibraryUpdate";
	public final IBinder localBinder = new LULocalBinder();
	private boolean isUpdating;
	private boolean trackAlbum;
	private boolean track;
	private boolean trackArtist;
	List<UpdateListener>mListeners;
	public interface UpdateListener{
		public void onUpdate(String artist, String album, String music);
	}
	public hubicLibraryUpdate(LibraryUpdater updater){
this.updater = updater;
		isUpdating=false;
		ct = updater;
		mListeners = new ArrayList<>();
	}

	public void addListener(UpdateListener listener) {
		mListeners.add(listener);
	}
	public void removeListener(UpdateListener listener) {
		mListeners.remove(listener);
	}



	public void updateDistantNoThread(){

		if(!isUpdating){
			updater.startNotification("Updating hubiC library");
			isUpdating=true;
			if(ct!=null){
				AccessDatasource ads= new AccessDatasource(ct);
				ads.open();
				MusicDatasource md = new MusicDatasource(ct);

				List<Access> acc = ads.getAllAccounts();
				ads.close();

				for(int i=0; i< acc.size(); i++){
					Log.d(TAG, "acc " + acc.get(i).getName());
					OpenstackConnector oc = new OpenstackConnector(((HubicAccess)acc.get(i)));
					oc.setRoot("default");
					try {
						ArrayList<String> dir = oc.listFolderAndSubs("");

						if (dir == null){

							((HubicAccess)acc.get(i)).refreshAccessToken();
							((HubicAccess)acc.get(i)).retrieveOpenStackAccessToken();
							ads.open();
							ads.addAccount(acc.get(i));
							ads.close();
							oc = new OpenstackConnector(((HubicAccess)acc.get(i)));
							oc.setRoot("default");
							dir = oc.listFolderAndSubs("");
						}


						md.open();
						/*ArrayList<String> ins = md.getListPath(acc.get(i));
						md.close();
						for(String in:ins){
							if(!dir.contains(in)){
								md.open();
								md.delete(acc.get(i).getId(), in);
								md.close();
							}
						}
						Log.d("updatedebug","has list "+ins.size());
*/
						for (String s : dir){
                            int index = s.lastIndexOf(".");
                            if (index!=-1&&index<s.length()&& NetworkLibraryUpdate.EXTENSION.contains(s.substring(index + 1).toLowerCase())) {
                                md.open();
								updater.startNotification(s);
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
                                        fm.release();
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
										updateListeners(artist, album, title);
										Intent in = new Intent();
										in.putExtra("uri", Uri.parse(s));
										in.putExtra("artist", artist);
										in.putExtra("album", album);
										in.putExtra("title", title);
										in.setAction("LibraryUpdated");
										updater.sendBroadcast(in);

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

				}
			}
			isUpdating=false;
		}
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
				updater.sendBroadcast(i);

			}
		}.start();
	}
	public class LULocalBinder extends Binder {
		public hubicLibraryUpdate getService(){
			return hubicLibraryUpdate.this;
		}
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





