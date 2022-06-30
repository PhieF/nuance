package com.spipi.spipimediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerFactory;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alexandre on 10/06/15.
 */
public class PlaylistView extends LinearLayout{

    private ItemAdapter.OnItemLongClickListener mOnMusicLongClick;

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow(); }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
    }



    public void setOnMusicLongClickListener(ItemAdapter.OnItemLongClickListener mOnItemLongClickListener) {
        mOnMusicLongClick = mOnItemLongClickListener;
    }

    public interface OnMusicClickListener{
        public void onMusicClick(List<MusicItem> musics, int position);
    }
    public List<MusicItem> getMusicList() {
        return mMusics;
    }

    private List<MusicItem> mMusics = null;

    public PlaylistView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

    }
    public PlaylistView(Context context, AttributeSet attrs) {
        super(context, attrs);


    }
    public PlaylistView(Context context){
        super(context);
    }




    public void setMusicList(List<MusicItem> musics){


    }
}
