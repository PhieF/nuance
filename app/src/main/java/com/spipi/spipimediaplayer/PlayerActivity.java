package com.spipi.spipimediaplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;

import java.util.Timer;
import java.util.TimerTask;


public class PlayerActivity extends AppCompatActivity implements MyApplication.Mp3ServiceBindListener, MediaPlayerService.OnChangeListener {

    private MediaPlayerService mMediaPlayerService;
    private ImageView mAlbumArt;
    private TextView mAlbumTV;
    private TextView mArtistTV;
    private boolean isChanging;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mAlbumArt = (ImageView) findViewById(R.id.albumArt);
        mAlbumTV = (TextView) findViewById(R.id.albumTV);
        mArtistTV = (TextView)findViewById(R.id.artistTV);

        final SeekBar seekbar = (android.widget.SeekBar) findViewById(R.id.seekBar1);






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
        ((MyApplication)getApplication()).bindToMp3Service(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_exit) {
            if(mMediaPlayerService!=null)
                mMediaPlayerService.stopForeground(true);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mMediaPlayerService!=null)
            mMediaPlayerService.removeOnChangeListener(this);
        mTimer.purge();
    }
    @Override
    public void onBind(MediaPlayerService lu) {
        mMediaPlayerService = lu;
        mMediaPlayerService.addOnChangeListener(this);
        onServiceChange();
    }

    @Override
    public void onServiceChange() {
        MusicItem mus = mMediaPlayerService.getCurrentMusicItem();
        if(mus!=null){
            String art = mus.getPicture();
            String artist = mus.getArtistName();
            String album = mus.getAlbumName();
            if(artist!=null)
                mArtistTV.setText(artist);
            if (album !=null) {
                mAlbumTV.setText(album);
            }
            if(art!=null){
                Bitmap myBitmap = BitmapFactory.decodeFile(art);
                mAlbumArt.setImageBitmap(myBitmap);
            } else
                mAlbumArt.setImageResource(R.drawable.unknown_artist);


        }
    }
}
