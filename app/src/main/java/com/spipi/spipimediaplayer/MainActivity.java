package com.spipi.spipimediaplayer;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.spipi.spipimediaplayer.deezer.DeezerHelpActivity;
import com.spipi.spipimediaplayer.deezer.DeezerLibraryUpdater;
import com.spipi.spipimediaplayer.deezer.DeezerWrapper;
import com.spipi.spipimediaplayer.hubic.hubicLibraryUpdate;
import com.spipi.spipimediaplayer.mediaplayer.FloatingService;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;


public class MainActivity extends AppCompatActivity implements MyApplication.CoverServiceBindListener, MyApplication.DeezerUpdateServiceBindListener, MyApplication.Mp3ServiceBindListener, PermissionChecker.PermissionListener {

    private hubicLibraryUpdate mUpdaterService;
    private CoverUpdaterService mCoverService;
    private DeezerLibraryUpdater mDeezerUpdaterService;
    private MediaPlayerService mMediaPlayer;
    private SearchFragment searchFragment;
    private String mLastQuery="";
    private PermissionChecker mPermissionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPermissionChecker = new PermissionChecker();
        setContentView(R.layout.activity_main2);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000ff")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000ff")));
        getSupportActionBar().setElevation(0);
        mPermissionChecker.checkAndRequestPermission(this, this);

    }
    public void setFragment(Fragment frag){
        setFragment(frag, true);
    }
    public void setFragment(Fragment frag, boolean add){
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_enable_floating_player).setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FloatingService.ENABLE_FLOATING_PLAYER, false));
        menu.findItem(R.id.action_enable_low_ram).setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("low_ram",false));
        menu.findItem(R.id.action_only_local).setCheckable(true).setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("only_local_pref", false));
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        /*final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        int searchImgId = android.support.v7.appcompat.R.id.search_button; // I used the explicit layout ID of searchview's ImageView
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.ic_menu_search);
        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(info);
        searchView.setQuery("", false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (searchFragment == null) {
                    SearchFragment fragm = new SearchFragment();
                    setFragment(fragm);
                }
                else if(searchFragment!=null)
                    searchFragment.doMySearch(searchView.getQuery().toString());
                mLastQuery = searchView.getQuery().toString();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });*/


        return true;
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
            MainFragment art = new MainFragment();
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
