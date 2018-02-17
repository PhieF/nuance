package com.spipi.spipimediaplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.mediaplayer.FloatingService;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alexandre on 10/06/15.
 */
public class MediaView extends LinearLayout implements  MyApplication.Mp3ServiceBindListener, MediaPlayerService.OnChangeListener {
    private final View mMusicsContainer;
    private final HashMap<MusicItem, View> mMusicViewMap;
    private  SeekBar seekbar;
    private  Timer mTimer;
    private MediaPlayerService mMediaPlayerService;
    private View mRoot;
    private ImageView mPlayButton;
    private View.OnClickListener mOnPlayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mMediaPlayerService.play();
        }
    };
    private View.OnClickListener mOnPauseClickListener= new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mMediaPlayerService.pause();
        }
    };
    private TextView mTitle;
    private ImageView mNextButton;
    private ImageView mPreviousButton;
    private ImageView mMenuButton;
    private boolean isChanging;

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        mMediaPlayerService.removeOnChangeListener(this);
    }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        if(mMediaPlayerService!=null)
            mMediaPlayerService.addOnChangeListener(this);
    }





    private List<MusicItem> mMusics;

    public MediaView(Context context, AttributeSet attrs, int defStyle)
    {

        super(context, attrs, defStyle);
        mMusicsContainer = findViewById(R.id.music_container);
        ((MyApplication)context.getApplicationContext()).bindToMp3Service(this);
        mMusicViewMap = new HashMap<>();

    }


    public MediaView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(Color.parseColor("#CCffffff"));
        seekbar = new android.widget.SeekBar(context);
        setOrientation(LinearLayout.VERTICAL);

        addView(seekbar);




        seekbar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                mMediaPlayerService.seekTo(seekBar.getProgress());
                isChanging = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                isChanging = true;

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

            }
        });
        mTimer = new Timer();
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {

                if(isChanging==true) {
                    return;
                }
                if(mMediaPlayerService!=null){
                    seekbar.setMax(mMediaPlayerService.getDuration());
                    seekbar.setProgress(mMediaPlayerService.getCurrentPosition());
                    seekbar.setSecondaryProgress(mMediaPlayerService.getBuffer());
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 100);
        LayoutInflater li = LayoutInflater.from(context);
        li.inflate(R.layout.fragment_media, this);

        mRoot = this;
        mMusicsContainer = findViewById(R.id.music_container);

        mMusicViewMap = new HashMap<>();
        mPlayButton = (ImageView)mRoot.findViewById(R.id.playButton);
        mNextButton = (ImageView)mRoot.findViewById(R.id.nextButton);
        mPreviousButton = (ImageView)mRoot.findViewById(R.id.previousButton);
        mMenuButton = (ImageView)mRoot.findViewById(R.id.menuButton);
        mMenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingService.instance.removeFloatingView();
            }
        });
        mRoot.findViewById(R.id.launchPlayerButton).setVisibility(View.GONE);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaPlayerService.next();
            }
        });
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaPlayerService.previous();
            }
        });
        mTitle = (TextView)mRoot.findViewById(R.id.title);
        ((MyApplication)context.getApplicationContext()).bindToMp3Service(this);

    }
    public MediaView(Context context){
        this(context, null);



    }


















    @Override
    public void onBind(MediaPlayerService lu) {
        mMediaPlayerService = lu;
        mMediaPlayerService.addOnChangeListener(this);
        onServiceChange();
    }

    @Override
    public void onServiceChange() {
        refreshPlayPause();
    }

    private void refreshPlayPause() {
        if(mMediaPlayerService.isPlaying()) {
            mPlayButton.setImageResource(R.drawable.pause);
            mPlayButton.setOnClickListener(mOnPauseClickListener);
            seekbar.setVisibility(View.VISIBLE);
        }
        else {
            seekbar.setVisibility(View.INVISIBLE);
            if(mPlayButton!=null) {
                mPlayButton.setImageResource(R.drawable.play);
                mPlayButton.setOnClickListener(mOnPlayClickListener);
            }

        }
        if(mTitle!=null)
        mTitle.setText(mMediaPlayerService.getCurrentMusicItem() != null ? mMediaPlayerService.getCurrentMusicItem().getTitle() : "no music");
    }
}
