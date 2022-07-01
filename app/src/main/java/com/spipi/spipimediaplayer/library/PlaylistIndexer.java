package com.spipi.spipimediaplayer.library;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
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
        visit(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()));
    }

    public void visit(Uri uri){
        RawLister lister = RawListerFactory.getRawListerForUrl(uri);
        try {
            for (FileInfo info : lister.getFileList()){
                if(info.isDirectory()){
                    visit(info.getUri());
                }
                if( info.getExtension().equals("m3u8")) {
                    BufferedReader reader = null;
                    try {
                        boolean isIn = false;
                        PlaylistItem currentPlaylistItem = null;
                        for (PlaylistItem playlistItem: mPlayLists){
                            if(playlistItem.getUri().equals(info.getUri())) {
                                isIn = true;
                                currentPlaylistItem = playlistItem;
                                break;
                            }
                        }

                        MusicDatasource.getInstance(mContext).open();

                        long id = -1;
                        if(currentPlaylistItem != null) {
                            if (currentPlaylistItem.getModificationDate() == info.lastModified())
                                continue;
                            id = currentPlaylistItem.getId();
                            MusicDatasource.getInstance(mContext).emptyPlaylist(id);
                            currentPlaylistItem.setModificationDate(info.lastModified());
                            MusicDatasource.getInstance(mContext).updatePlaylist(currentPlaylistItem);
                        }
                        else {
                            id = MusicDatasource.getInstance(mContext).addPlaylist(info.getName(), PlayList.TYPE_LOCAL, info.getUri(), info.lastModified());
                        }
                        reader = new BufferedReader(new InputStreamReader(FileEditorFactory.getFileEditorForUrl(info.getUri(), null).getInputStream()));

                        while(reader.ready()) {
                            String line = reader.readLine();
                            Uri music = Uri.withAppendedPath(uri, line);

                            if(FileInfoFactory.getFileInfoForUrl(music).exists())
                                MusicDatasource.getInstance(mContext).addToPlaylist(-MediaPlayerFactory.TYPE_LOCAL, music.toString(), id);

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
