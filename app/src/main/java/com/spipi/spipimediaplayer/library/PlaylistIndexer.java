package com.spipi.spipimediaplayer.library;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.spipi.spipimediaplayer.PlaylistItem;
import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.library.ftp.AuthenticationException;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerFactory;
import com.spipi.spipimediaplayer.playlists.PlayList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PlaylistIndexer {
    private final Context mContext;
    private List<PlaylistItem> mPlayLists;

    public PlaylistIndexer(Context context){
        mContext = context;
    }

    public void visit(){
        MusicDatasource.getInstance(mContext).open();
        mPlayLists = MusicDatasource.getInstance(mContext).getAllPlaylists(false);
        MusicDatasource.getInstance(mContext).close();
        visit(Uri.parse("/sdcard/"));
    }

    public void visit(Uri uri){
        RawLister lister = RawListerFactory.getRawListerForUrl(uri);
        try {
            for (FileInfo info : lister.getFileList()){
                if(info.isDirectory()){
                    visit(info.getUri());
                }
                if( info.getExtension().equals("m3u8")) {
                    Log.d("PlayListDebug", info.getUri().toString());
                    BufferedReader reader = null;
                    try {
                        boolean isIn = false;
                        for (PlaylistItem playlistItem: mPlayLists){
                            if(playlistItem.getUri().equals(info.getUri())) {
                                isIn = true;
                                break;
                            }
                        }
                        if(isIn)
                            continue;
                        MusicDatasource.getInstance(mContext).open();

                        long id = MusicDatasource.getInstance(mContext).addPlaylist(info.getName(), PlayList.TYPE_LOCAL, info.getUri());
                        reader = new BufferedReader(new InputStreamReader(FileEditorFactory.getFileEditorForUrl(info.getUri(), null).getInputStream()));

                        while(reader.ready()) {
                            String line = reader.readLine();
                            Log.d("PlayListDebug", "path "+line);
                            Uri.withAppendedPath(uri, line);
                            MusicDatasource.getInstance(mContext).addToPlaylist(-MediaPlayerFactory.TYPE_LOCAL, info.getUri().toString(), id);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    MusicDatasource.getInstance(mContext).close();
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

}
