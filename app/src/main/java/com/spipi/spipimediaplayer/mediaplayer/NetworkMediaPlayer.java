package com.spipi.spipimediaplayer.mediaplayer;

import android.media.MediaPlayer;
import android.net.Uri;

import com.spipi.spipimediaplayer.MusicItem;
import com.spipi.spipimediaplayer.network.HttpProxy;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by alexandre on 22/05/15.
 */
public class NetworkMediaPlayer extends LocalMediaPlayer implements MediaPlayer.OnErrorListener{

    @Override
    public int getBuffer() {

        return mBuffer;
    }
    @Override
    public void setMusic(MusicItem music) {
        mMusicItem = music;
        if(mMediaPlayer!=null){
            mMediaPlayer.release();
        }
        mIsReady = false;
        mMediaPlayer = new MediaPlayer();

        try {

            HttpProxy httpProxy = new HttpProxy(Uri.parse(music.getPath()), null);

            mMediaPlayer.setDataSource(mContext, httpProxy.getUri("music"), new HashMap<String, String>() );
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {


            mListener.onError();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }
}
