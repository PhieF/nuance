package com.spipi.spipimediaplayer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.spipi.spipimediaplayer.deezer.DeezerHelpActivity;
import com.spipi.spipimediaplayer.deezer.DeezerLibraryUpdater;
import com.spipi.spipimediaplayer.deezer.DeezerWrapper;
import com.spipi.spipimediaplayer.hubic.hubicLibraryUpdate;
import com.spipi.spipimediaplayer.library.FileInfo;
import com.spipi.spipimediaplayer.library.PlaylistIndexer;
import com.spipi.spipimediaplayer.mediaplayer.FloatingService;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;
import com.spipi.spipimediaplayer.playlists.PlayListConverter;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements MyApplication.CoverServiceBindListener, MyApplication.DeezerUpdateServiceBindListener, MyApplication.Mp3ServiceBindListener, PermissionChecker.PermissionListener {

    private hubicLibraryUpdate mUpdaterService;
    private CoverUpdaterService mCoverService;
    private DeezerLibraryUpdater mDeezerUpdaterService;
    private MediaPlayerService mMediaPlayer;
    private SearchFragment searchFragment;
    private String mLastQuery="";
    private PermissionChecker mPermissionChecker;
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPermissionChecker = new PermissionChecker();
        new Thread(){
            public void run(){
                new PlaylistIndexer(MainActivity.this).visit();
            }
        }.start();

        setContentView(R.layout.activity_main2);
        getSupportActionBar().hide();
        mPermissionChecker.checkAndRequestPermission(this, this);
    }
    public void setFragment(Fragment frag){
        setFragment(frag, true);
    }
    public void setFragment(Fragment frag, boolean add){
        mFragment = frag;
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction()                ;
        ft.setCustomAnimations(R.anim.browser_content_enter,
                R.anim.browser_content_exit, R.anim.browser_content_pop_enter,
                R.anim.browser_content_pop_exit);
        ft.replace(R.id.container, frag, "tag");
        if(add)
            ft.addToBackStack(null);
        searchFragment = null;
        ft.commitAllowingStateLoss();

    }

    @Override
    public void onBackPressed(){
        if(getSupportFragmentManager().getBackStackEntryCount()>0){

            getSupportFragmentManager().popBackStackImmediate();
            searchFragment=null;
        }
        else
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }


        if (id == R.id.action_update) {
           // mUpdaterService.updateDistant();
           LibraryUpdater.sLibraryUpdater.updateDistant();
        }
        if (id == R.id.action_update_deezer) {

            SessionStore sessionStore = new SessionStore();
            DeezerConnect deezerConnect =  new DeezerConnect(this, DeezerWrapper.APP_ID);

            if (sessionStore.restore(deezerConnect, this)) {
                mDeezerUpdaterService.updateDeezerThread();
            }
            else{
                Intent intent = new Intent(this, DeezerHelpActivity.class);

                startActivity(intent);

            }
        }
        if(id == R.id.action_enable_floating_player){
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(FloatingService.ENABLE_FLOATING_PLAYER,!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FloatingService.ENABLE_FLOATING_PLAYER,false)).commit();
            invalidateOptionsMenu();

        }
        if(id == R.id.action_enable_low_ram){
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean("low_ram",!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("low_ram",false)).commit();
            invalidateOptionsMenu();
            sendBroadcast(new Intent("Reload"));

        }
        if (id == R.id.action_get_cover) {
            new Thread(){
                public void run(){
                    mCoverService.updateNoThread();
                }
            }.start();
        }
        if (id == R.id.action_only_local) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("only_local_pref",!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("only_local_pref",false)).commit();
            invalidateOptionsMenu();
            Intent i = new Intent();
            i.setAction("LibraryUpdated");
            this.sendBroadcast(i);
        }
        if (id == R.id.action_exit) {
            if(mMediaPlayer!=null)
                mMediaPlayer.stopForeground(true);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBind(CoverUpdaterService lu) {
        mCoverService = lu;
    }

    @Override
    public void onBind(DeezerLibraryUpdater lu) {
        mDeezerUpdaterService = lu;
    }

    @Override
    public void onBind(MediaPlayerService lu) {
        mMediaPlayer = lu;
    }

    public void setSearchFragment(SearchFragment searchFragment) {
        this.searchFragment = searchFragment;
        searchFragment.doMySearch(mLastQuery);
    }
    public void unsetSearchFragment() {
        this.searchFragment = null;
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            NewMainFragment art = new NewMainFragment();
            setFragment(art, false);
            ((MyApplication)getApplication()).bindToDeezerUpdateService(this);
            ((MyApplication)getApplication()).bindToMp3Service(this);
            ((MyApplication)getApplication()).bindToCoverService(this);
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isfirstLaunch",true)) {
                startActivity(new Intent(this, HelpActivity.class));
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isfirstLaunch", false).commit();
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPermissionChecker.onRequestPermissionsResult(requestCode,permissions, grantResults);
    }
}
