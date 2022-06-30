package com.spipi.spipimediaplayer.library;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.spipi.spipimediaplayer.LibraryUpdater;
import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerFactory;
import com.spipi.spipimediaplayer.library.ftp.AuthenticationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NetworkLibraryUpdate  {
	LibraryUpdater updater;
	public final IBinder localBinder = new LULocalBinder();
	private boolean isUpdating;
	private final static String TAG = "NetworkLibraryUpdate";
	private boolean trackAlbum;
	private boolean track;
	private boolean trackArtist;
	List<UpdateListener>mListeners;
    private int mLastUpdate;
    public static List<String> EXTENSION = new ArrayList<String>(){
        {
          add("mp3");
            add("flac");
            add("ogg");
            add("aac");
            add("wav");
            }

        };

    public interface UpdateListener{
		public void onUpdate(String artist, String album, String music);
	}
	public NetworkLibraryUpdate(LibraryUpdater updater){

		this.updater = updater;
		isUpdating=false;
		mListeners = new ArrayList<>();
	}

	public void addListener(UpdateListener listener) {
		mListeners.add(listener);
	}
	public void removeListener(UpdateListener listener) {
		mListeners.remove(listener);
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


	private interface NewFileListener{
		public void onNewFile(FileInfo metaFile);
	}
	private void visitFile(Uri uri, NewFileListener listener){
		RawLister lister = RawListerFactory.getRawListerForUrl(uri);
		List<FileInfo> list = null;
		try {
			list = lister.getFileList();
			if(list!=null){
				for(FileInfo file : list){
					if(file.isDirectory()){
						visitFile(file.getUri(), listener);
					}
					else {
						listener.onNewFile(file);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AuthenticationException e) {
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} catch (JSchException e) {
			e.printStackTrace();
		}

	}


	public void updateDistantNoThread(){
        mLastUpdate = 0;
		if(!isUpdating){
			updater.startNotification("please wait...");
			isUpdating=true;
			if(updater!=null){
				updater.startNotification("please wait.....");
				Cursor c = ShortcutDbAdapter.getInstance().getAllShortcuts(updater, null, null);
				if(c!=null&&c.getCount()>0){
					c.moveToFirst();

					do{



						ShortcutDbAdapter.Shortcut shortcut =  ShortcutDbAdapter.getInstance().cursorToShortcut(c);

						Log.d(TAG, "exists ? " + shortcut.getUri());

						FileEditor editor = FileEditorFactory.getFileEditorForUrl(Uri.parse(shortcut.getUri()), updater);
						if(!editor.exists())
							continue;
						final MusicDatasource md = new MusicDatasource(updater);
						final List<String> indexed = md.getAllMusicPathStartingWithPath(shortcut.getUri());
						visitFile(Uri.parse(shortcut.getUri()), new NewFileListener() {
							@Override
							public void onNewFile(FileInfo metaFile) {
								String s = metaFile.getUri().toString();
                                int index = s.lastIndexOf(".");
								if (index!=-1&&index<s.length()&&EXTENSION.contains(s.substring(index + 1).toLowerCase())) {

										if (!indexed.contains(metaFile.getUri().toString())) {
											record(metaFile, md, 0);
										}

										else {
											indexed.remove(metaFile.getUri().toString());
										}

								}
							}
						});
						for(String str : indexed){
							md.removeMusicWithPath(str);
						}

					}while(c.moveToNext());

				}
			}
            Intent i = new Intent();

            i.setAction("LibraryUpdated");
            updater.sendBroadcast(i);
			isUpdating=false;
		}
	}

	private void record(FileInfo metaFile, MusicDatasource md, int trials) {

		try {
			String s = metaFile.getUri().toString();
			updater.startNotification(metaFile.getName());
			HttpProxy proxy = new HttpProxy(metaFile, null);
			File f = new File(s);
			Log.d(TAG, "onNewFile " + proxy.getUri(metaFile.getName()));
			MediaMetadataRetriever fm = new MediaMetadataRetriever();
			fm.setDataSource(proxy.getUri(metaFile.getName()).toString(), new HashMap<String, String>());

			String artist = fm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			String album = fm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
			String title = fm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			String track = fm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER);
            fm.release();
			if (track == null)
				track = "" + 0;
			Integer t = new Integer(track);

			if (title == null) {
				title = f.getName();
			}
			if (artist == null)
				artist = "<unknown>";
			if (album == null)
				album = "<unknown>";

			md.open();
			Log.d(TAG, "addMusic " + metaFile.getUri());

			md.addMusic((long) -MediaPlayerFactory.TYPE_NEW, s, artist, album, title, track);
			md.close();
			updateListeners(artist, album, title);
            if(mLastUpdate%8 == 0){
                Intent i = new Intent();
                i.putExtra("uri", metaFile.getUri());
                i.putExtra("artist", artist);
                i.putExtra("album", album);
                i.putExtra("title", title);
                i.setAction("LibraryUpdated");
                updater.sendBroadcast(i);

            }
            mLastUpdate++;
		}catch(java.lang.RuntimeException ex){
			if(trials==0)
				record(metaFile, md, trials+1);
		}
		catch(Exception ex){
			if(trials==0)
				record(metaFile, md, trials+1);
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
		public NetworkLibraryUpdate getService(){
			return NetworkLibraryUpdate.this;
		}
	}

}


