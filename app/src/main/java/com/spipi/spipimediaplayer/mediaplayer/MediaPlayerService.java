package com.spipi.spipimediaplayer.mediaplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.spipi.spipimediaplayer.MusicItem;
import com.spipi.spipimediaplayer.PlayerActivity;
import com.spipi.spipimediaplayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 22/05/15.
 */
public class MediaPlayerService extends Service implements MediaPlayerListener {


    private List<MusicItem> mMusicList;
    private MusicItem musicItem;
    private MediaPlayerInterface mediaPlayer;
    int current = 0;
    public final IBinder localBinder = new LocalBinder();
    private List<OnChangeListener> mOnChangeListeners;
    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private boolean mChangeMusic;


    private class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if(isPlaying())
                    pause();
            }
        }
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                pause();
            } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                //Not in call: Play music
            } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                pause();
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };

    public boolean isPlaying() {
        if(mediaPlayer!=null)
            return mediaPlayer.isPlaying();
        return false;
    }

    public MusicItem getCurrentMusicItem() {
        return musicItem;
    }

    public void setCurrent(int position) {
        current = position;
    }

    public void removeOnChangeListener(OnChangeListener listener) {
        mOnChangeListeners.remove(listener);
    }

    public int getDuration() {

        if(mediaPlayer!=null)
           return mediaPlayer.getTotal();
        return -1;
    }

    public int getCurrentPosition() {

        if(mediaPlayer!=null)
            return mediaPlayer.getProgress();
        return -1;
    }
    public int getBuffer(){
        if(mediaPlayer!=null)
            return mediaPlayer.getBuffer();
        return -1;
    }

    public void seekTo(int progress) {
        if(mediaPlayer!=null)
            mediaPlayer.seekTo(progress);
    }
    private void startNotification(){
        if(isPlaying()&&mChangeMusic){
            mChangeMusic = false;
            Intent it =  new Intent(getApplicationContext(), PlayerActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                    it,
                    0);
            Notification.Builder notification = new Notification.Builder(getApplicationContext());
            notification.setContentTitle("Spipi Music Player");
            notification.setSmallIcon(R.drawable.ic_icon);
           // notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.setContentText(getCurrentMusicItem().getTitle());
            notification.setSubText(getCurrentMusicItem().getAlbum()!=null?"Album: " + getCurrentMusicItem().getAlbum().getName():"");
            notification.setContentIntent(pi);

            startForeground(3, notification.getNotification());
        }
    }
    public void onServiceChange() {
        if(isPlaying())
            startNotification();
        else {

            mChangeMusic = true;
        }
    }

    public interface OnChangeListener{
        public void onServiceChange();
    }
    public void onCreate(){
        mOnChangeListeners = new ArrayList<>();
        registerReceiver(new NoisyAudioStreamReceiver(), intentFilter);

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

    }
    public void addOnChangeListener(OnChangeListener listener){
        if(!mOnChangeListeners.contains(listener))
            mOnChangeListeners.add(listener);
    }
    public void setMusicList(List<MusicItem> musicList){
        current = 0;
        mMusicList = musicList;
    }
    public void pause(){
        if(isPlaying())
            mediaPlayer.pause();
    }
    public void next(){
        current++;
        prepareAndPlay();
    }
    public void previous(){
        current--;
        prepareAndPlay();
    }
    public void prepareAndPlay(){
        if(mMusicList!=null&&mMusicList.size()>0){
            if(current>=mMusicList.size()|| current<0)
                current = 0;
            mChangeMusic=true;
            musicItem = mMusicList.get(current);
            if(mediaPlayer!=null)
                mediaPlayer.release();
            mediaPlayer = MediaPlayerFactory.getMediaPlayer(musicItem.getPath());
            mediaPlayer.setContext(this);
            mediaPlayer.setApplication(getApplication());
            mediaPlayer.setListener(this);
            mediaPlayer.setMusic(musicItem);


            notifyListeners();
        }

    }
    public void play(){
        if(mediaPlayer!=null)
            mediaPlayer.play();
    }
    public class LocalBinder extends Binder {
        public MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    @Override
    public void onReady() {

        mediaPlayer.play();
        notifyListeners();
    }

    private void notifyListeners() {
        for(OnChangeListener listener : mOnChangeListeners){
            listener.onServiceChange();
        }
        onServiceChange();
    }

    @Override
    public void onEnd() {
        notifyListeners();
    }

    @Override
    public void onStart() {
        notifyListeners();
    }

    @Override
    public void onChange() {
        notifyListeners();
    }

    @Override
    public void onCompletion() {
        next();
    }

    @Override
    public void onError() {
        notifyListeners();
    }
}
