package com.spipi.spipimediaplayer.network;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerFactory;

/**
 * Created by alexandre on 03/12/15.
 */
public class StateService extends IntentService {

    public static IntentFilter sFilter = new IntentFilter();
    public static BroadcastReceiver sReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, StateService.class));
        }
    };

    public static void register(Context ct){
        sFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        ct.registerReceiver(sReceiver, sFilter);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *
     */
    public StateService() {
        super("StateService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        Cursor cursor = ShortcutDbAdapter.getInstance().getAllShortcuts(this, null, null);
        final MusicDatasource md = new MusicDatasource(this);

        Log.d("statedebug", "onHandleIntent ");

        if(cursor!=null&&cursor.getCount()>0) {
            Log.d("statedebug", "cursor!=null&&cursor.getCount()>0 ");
            cursor.moveToFirst();

            if (wifi.isConnectedOrConnecting() || mobile.isConnectedOrConnecting()) {
                do {
                    final ShortcutDbAdapter.Shortcut shortcut = ShortcutDbAdapter.getInstance().cursorToShortcut(cursor);

                    md.setHubicMusicState(true);
                    md.setMusicState(-MediaPlayerFactory.TYPE_DEEZER, true);
                    Intent i = new Intent();

                    i.setAction("LibraryUpdated");
                    sendBroadcast(i);
                    new Thread() {
                        @Override
                        public void run() {
                            Log.d("statedebug", shortcut.getUri());
                            FileEditor editor = FileEditorFactory.getFileEditorForUrl(Uri.parse(shortcut.getUri()), StateService.this);
                            if (editor.exists()) {
                                Log.d("statedebug", "exists");
                                md.setMusicState(shortcut.getUri(), true);
                            } else {
                                md.setMusicState(shortcut.getUri(), false);
                            }
                            Intent i = new Intent();
                            i.setAction("LibraryUpdated");
                            i.putExtra("uri", Uri.parse(shortcut.getUri()));
                            sendBroadcast(i);
                        }
                    }.start();

                }while (cursor.moveToNext());

            } else {
                md.setMusicState(null, false);
                Intent i = new Intent();
                i.setAction("LibraryUpdated");
                sendBroadcast(i);
            }
        }
    }
}
