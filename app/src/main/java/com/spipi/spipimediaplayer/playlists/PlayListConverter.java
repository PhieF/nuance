package com.spipi.spipimediaplayer.playlists;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.spipi.spipimediaplayer.PlaylistItem;
import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.library.FileEditorFactory;
import com.spipi.spipimediaplayer.library.FileInfo;
import com.spipi.spipimediaplayer.library.FileInfoFactory;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by alexandre on 08/07/15.
 */
public class PlayListConverter {

    public static PlayList convert(PlayList pl){
        return null;
    }
    /*

    PLaylist can be null, if not null will import to playlist
     */
    public static boolean importFromFile(Context context, Uri uri, PlaylistItem playList){
        try {
            MusicDatasource.getInstance(context).open();
            FileInfo info = FileInfoFactory.getFileInfoForUrl(uri);
            long id;
            if(playList == null)
                id = MusicDatasource.getInstance(context).addPlaylist(info.getName(), PlayList.TYPE_LOCAL, info.getUri(), info.lastModified());
            else
                id = playList.getId();
            BufferedReader reader = null;
            try {

                reader = new BufferedReader(new InputStreamReader(FileEditorFactory.getFileEditorForUrl(info.getUri(), null).getInputStream()));

                while(reader.ready()) {
                    String line = reader.readLine();
                    Uri music = Uri.withAppendedPath(info.getParent().getUri(), line);
                    Log.d("playlistdebug","music "+music.toString());
                    if(FileInfoFactory.getFileInfoForUrl(music).exists()) {
                        MusicDatasource.getInstance(context).addToPlaylist(-MediaPlayerFactory.TYPE_LOCAL, music.toString(), id);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            MusicDatasource.getInstance(context).close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
