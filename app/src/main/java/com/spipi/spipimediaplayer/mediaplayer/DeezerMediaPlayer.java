package com.spipi.spipimediaplayer.mediaplayer;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.event.OnBufferProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.spipi.spipimediaplayer.MusicItem;
import com.spipi.spipimediaplayer.deezer.DeezerWrapper;

/**
 * Created by alexandre on 25/06/15.
 */
public class DeezerMediaPlayer implements MediaPlayerInterface, PlayerWrapperListener, OnPlayerStateChangeListener, OnBufferProgressListener {
    TrackPlayer trackPlayer=null;
    private boolean mIsTrackPlayerPlaying;
    private Application mApplication;
    private MediaPlayerListener mListener;
    private Long mTrackId=(long)-1;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            mListener.onChange();
             mHandler.sendEmptyMessageDelayed(0,500);
        }


    };
    private Context mContext;
    private double mBuffer = 0;

    @Override
    public void setContext(Context ct) {
        mContext  =ct;
    }
    @Override
    public void setApplication(Application ct) {
        mApplication  =ct;
    }
    @Override
    public int getProgress() {
        if(trackPlayer!=null)
            return (int) trackPlayer.getPosition();
        return 0;
    }

    @Override
    public int getBuffer() {
        return (int) (mBuffer/100*getTotal());
    }

    @Override
    public int getTotal() {
        if(trackPlayer!=null)
            return (int) trackPlayer.getTrackDuration();
        return 0;
    }

    @Override
    public void pause() {
        if(trackPlayer!=null)
            trackPlayer.pause();
    }

    @Override
    public void play() {
        if(mTrackId!=-1&&trackPlayer!=null&&trackPlayer.getPlayerState()==PlayerState.PAUSED) {
            trackPlayer.play();
            mHandler.sendEmptyMessageDelayed(0,500);
        }
    }

    @Override
    public MusicItem getMusic() {
        return null;
    }

    @Override
    public void setMusic(MusicItem music) {
        SessionStore sessionStore = new SessionStore();
        DeezerConnect deezerConnect =  new DeezerConnect(mApplication, DeezerWrapper.APP_ID);

        if (sessionStore.restore(deezerConnect, mApplication)) {
            try {
                trackPlayer = new TrackPlayer(mApplication, deezerConnect, new WifiAndMobileNetworkStateChecker());
                trackPlayer.addPlayerListener(this);
                String path = Uri.parse(music.getPath()).getHost();
                if(path.startsWith("/"))
                    path =  path.substring(1);
                mBuffer = 0;
                mTrackId = new Long(path);
                mIsTrackPlayerPlaying = false;
                trackPlayer.playTrack(mTrackId);
                trackPlayer.addOnBufferProgressListener(this);
                mHandler.sendEmptyMessageDelayed(0,500);
            } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                mListener.onError();
            } catch (DeezerError deezerError) {
                mListener.onError();
            }
        }
    }

    @Override
    public void setListener(MediaPlayerListener listener) {
        mListener= listener;
    }

    @Override
    public boolean isPlaying() {
        if(trackPlayer!=null)return trackPlayer.getPlayerState()==PlayerState.PLAYING;
        return false;
    }

    @Override
    public void release() {
        if(trackPlayer!=null) {
            trackPlayer.stop();
            trackPlayer.release();
        }
        mHandler.removeMessages(0);
        mHandler= null;
        trackPlayer = null;

    }

    @Override
    public void seekTo(int progress) {
        if(trackPlayer!=null)
            trackPlayer.seek(progress);
    }

    @Override
    public void onAllTracksEnded() {
        mIsTrackPlayerPlaying = false;

    }

    @Override
    public void onPlayTrack(Track track) {
        mIsTrackPlayerPlaying = true;
        mListener.onChange();
    }

    @Override
    public void onTrackEnded(Track track) {
        mIsTrackPlayerPlaying = false;
        mListener.onCompletion();
    }

    @Override
    public void onRequestException(Exception e, Object o) {
        mIsTrackPlayerPlaying = false;

    }

    @Override
    public void onPlayerStateChange(PlayerState playerState, long l) {
        switch (playerState){
            case PAUSED:
                mIsTrackPlayerPlaying = false;
                mListener.onChange();
                break;
            case PLAYING:
                mIsTrackPlayerPlaying = true;
                mListener.onChange();
                break;
        }
    }

    @Override
    public void onBufferProgress(double v) {
        mBuffer = v;
    }
}
