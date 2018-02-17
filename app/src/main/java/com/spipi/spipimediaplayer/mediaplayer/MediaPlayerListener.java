package com.spipi.spipimediaplayer.mediaplayer;

/**
 * Created by alexandre on 22/05/15.
 */
public interface MediaPlayerListener {
    public void onReady();
    public void onEnd();
    public void onStart();
    void onChange();
    void onCompletion();
    void onError();



}
