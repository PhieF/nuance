package com.spipi.spipimediaplayer.mediaplayer;

import android.net.Uri;

/**
 * Created by alexandre on 22/05/15.
 */
public class MediaPlayerFactory {
    public final static int TYPE_LOCAL = 0;
    public final static int TYPE_HUBIC =1;
    public final static int TYPE_DEEZER =2;
    public final static int TYPE_NEW =3;
    public static MediaPlayerInterface getMediaPlayer(String path){
        Uri uri = Uri.parse(path);
        if("deezer".equals(uri.getScheme())){
            return  new DeezerMediaPlayer();
        }
        else if ("hubic".equals(uri.getScheme())){
            return new DistantMediaPlayer();
        }
        else if(uri.getScheme()==null||"file".equals(uri.getScheme())){
            return new LocalMediaPlayer();
        }
        else{
            return  new NetworkMediaPlayer();

        }

    }
}
