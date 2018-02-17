package com.spipi.spipimediaplayer.mediaplayer;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.spipi.spipimediaplayer.MusicItem;

import java.io.IOException;

/**
 * Created by alexandre on 22/05/15.
 */
public class LocalMediaPlayer implements  MediaPlayerInterface, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {
    protected MusicItem mMusicItem;
    protected MediaPlayerListener mListener;
    protected MediaPlayer mMediaPlayer;
    protected Context mContext;
    protected boolean mIsReady;
    protected int mBuffer = 0;

    public LocalMediaPlayer(){

    }

    @Override
    public void setContext(Context ct) {
        mContext = ct;
    }



    @Override
    public void setApplication(Application ct) {

    }

    @Override
    public int getProgress() {
        if(mIsReady)
            try {
                return mMediaPlayer.getCurrentPosition();
            }
            catch (Exception e){

            }
        return -1;
    }

    @Override
    public int getBuffer() {

        return getTotal();
    }

    @Override
    public int getTotal() {
        if(mIsReady)
            try {
                return mMediaPlayer.getDuration();
            }
            catch (Exception e){

            }
        return -1;
    }
    @Override
    public void pause(){
        if(mIsReady)
            mMediaPlayer.pause();
        if(mListener!=null)
            mListener.onChange();
    }
    @Override
    public void play(){
        if(mIsReady)
            try {
                mMediaPlayer.start();
            }
            catch (Exception e){
                setMusic(mMusicItem);
            }
        if(mListener!=null)
            mListener.onChange();
    }
    @Override
    public MusicItem getMusic(){
        return mMusicItem;
    }
    @Override
    public void setMusic(MusicItem music) {
        mMusicItem = music;
        if(mMediaPlayer!=null){
            mMediaPlayer.release();
        }
        mBuffer=0;
        mIsReady = false;
        mMediaPlayer = new MediaPlayer();
        try {

            mMediaPlayer.setDataSource(mContext, Uri.parse(mMusicItem.getPath()));
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            mListener.onError();
        }
    }

    @Override
    public void setListener(MediaPlayerListener listener) {
        mListener = listener;
    }

    @Override
    public boolean isPlaying() {
        if(mIsReady)
            try {
                return mMediaPlayer.isPlaying();
            }
            catch (Exception e){

            }

        return false;
    }

    @Override
    public void release() {
        if(mIsReady)
            try {
            mMediaPlayer.release();
        }
        catch (Exception e){

        }
    }

    @Override
    public void seekTo(int progress) {
        if(mIsReady) {
            try {
                mMediaPlayer.seekTo(progress);
            }
            catch (Exception e){

            }

        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mIsReady = true;

        if(mListener!=null)
            mListener.onReady();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mIsReady=false;
        if(mListener!=null)
            mListener.onCompletion();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        mBuffer = i;
    }
}
