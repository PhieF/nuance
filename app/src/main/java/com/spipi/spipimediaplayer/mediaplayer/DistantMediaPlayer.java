package com.spipi.spipimediaplayer.mediaplayer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.spipi.spipimediaplayer.Access;
import com.spipi.spipimediaplayer.MusicItem;
import com.spipi.spipimediaplayer.hubic.HubicAccess;
import com.spipi.spipimediaplayer.hubic.AccessDatasource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by alexandre on 22/05/15.
 */
public class DistantMediaPlayer extends LocalMediaPlayer implements MediaPlayer.OnErrorListener{

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

        Access acc;
        AccessDatasource ad = new AccessDatasource(mContext);
        ad.open();
        HashMap<String,String> header = null;
        acc = ad.getAccess(Uri.parse(mMusicItem.getPath()).getHost());
        ad.close();
        try {

            header = new HashMap<String,String>();
            header.put("X-Auth-Token", acc.getOpenStackAccessToken());
            String path = Uri.parse(mMusicItem.getPath()).getPath();
            Log.d("pathdebug",mMusicItem.getPath());
            Log.d("pathdebug", Uri.parse(mMusicItem.getPath()).getPath());
            try {
                path=  new String(path.getBytes(),"UTF-8");
                path= URLEncoder.encode(path, "UTF-8").replace("+", "%20");

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String url = acc.getOpenStackUrl()+"/default"+path; // your URL here
            mMediaPlayer.setDataSource(mContext,Uri.parse(url), header );
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            ((HubicAccess)acc).refreshAccessToken();
            try {
                ((HubicAccess)acc).retrieveOpenStackAccessToken();
                ad.open();
                ad.addAccount( (acc));
                ad.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            mListener.onError();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }
}
