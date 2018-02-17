package com.spipi.spipimediaplayer.mediaplayer;

import android.app.Application;
import android.content.Context;

import com.spipi.spipimediaplayer.MusicItem;

/**
 * Created by alexandre on 22/05/15.
 */
public interface MediaPlayerInterface {
    public void setContext(Context ct);
    public void setApplication(Application ct);

    public int getProgress();
    public int getBuffer();
    public int getTotal();
    public void pause();
    public void play();
    public MusicItem getMusic();
    public void setMusic(MusicItem music);
    public void setListener(MediaPlayerListener listener);

    boolean isPlaying();

    void release();

    void seekTo(int progress);
}
