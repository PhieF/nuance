package com.spipi.spipimediaplayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.spipi.spipimediaplayer.database.SQLiteHelper;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * interface.
 */
public class PlaylistFragment extends GenericFragment implements MyApplication.Mp3ServiceBindListener, AlbumView.OnMusicClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PLAYLIST = "param1";

    // TODO: Rename and change types of parameters
    private PlaylistItem mPlaylist;
    private ArrayList<MusicItem> mMusics;
    private ArrayList<Item> mItems;
    private ArrayList<ArrayList<MusicItem>> mMusicsToSave;
    private MediaPlayerService mMediaPlayerService;
    private Drawable mDrawable;
    private Bitmap myBitmap;
    private AsyncTask<Void, Void, Void> mTask;
    private ArrayList<MusicItem> mMusicsToDisplay;
    private String mLastQuery = "";


    // TODO: Rename and change types of parameters
    public static PlaylistFragment newInstance(PlaylistItem playlist) {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLAYLIST, playlist);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle save){
        mPlaylist = (PlaylistItem)getArguments().getSerializable(ARG_PLAYLIST);
        mMusicsToSave = new ArrayList<>();
        mMusics = new ArrayList<>();
        mDrawable = getResources().getDrawable(R.drawable.white);


        super.onCreate(save);



    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        ((MyApplication)getActivity().getApplication()).bindToMp3Service(this);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        if(mTask!=null)
            mTask.cancel(true);

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(myBitmap!=null)
        myBitmap.recycle();
        myBitmap = null;
        mMusics = null;
    }
    @Override
    public void onSaveInstanceState(Bundle save){

    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlaylistFragment() {
    }
    public void OnScrollChanged(int l, int t, int oldl, int oldt) {
        // Code ...
        mDrawable.setAlpha((int) getAlphaForView(0 - mListView.getScrollY()));
    }


    private float getAlphaForView(int position) {
        int diff = 0;
        float minAlpha = 0, maxAlpha = 1.f;
        float alpha = minAlpha; // min alpha
        if (position > 500)
            alpha = minAlpha;
        else if (position + 40 < 500)
            alpha = maxAlpha;
        else {
            diff = 500 - position;
            alpha += ((diff * 1f) / 40)* (maxAlpha - minAlpha); // 1f and 0.4f are maximum and min
            // alpha
            // this will return a number betn 0f and 0.6f
        }
        return alpha;
    }
@Override
public void  postOnCreate(View view){
    super.postOnCreate(view);
    if(mPlaylist !=null){
        getActivity().setTitle(mPlaylist.getDisplayName());
        mAdapter.setHeader(null);
    }

}


    @Override
    protected LinearLayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }
    @Override
public void setItemList(){
        if(mTask!=null)
            mTask.cancel(true);
       mTask =  new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                if(getActivity()==null)
                    return null;
                mMusics = new ArrayList<>();
                mMusicsToDisplay = new ArrayList<>();
                mItems = new ArrayList<Item>();

                int page = 0;
                while (true){
                    List<MusicItem> newMusics = mMusicDatasource.getAllMusicsFromPlaylist(mPlaylist, PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("only_local_pref",false), page, SQLiteHelper.COLUMN_ID+" DESC");
                    if (newMusics.size() == 0)
                        break;
                    mMusics.addAll(newMusics);
                    publishProgress();
                    page ++;
                }

                return null;

            }

           @Override
           protected void onProgressUpdate(Void... values) {
               if(mMusics==null)
                   return;
               refreshFilter();
               mAdapter.setOnMusicClickListener(PlaylistFragment.this);


           }

           @Override
        protected void onPostExecute(Void result){

            }
        }.execute();


    }

    private void refreshFilter() {

        mMusicsToDisplay = new ArrayList<>();
        for(MusicItem musicItem: new ArrayList<MusicItem>(mMusics)){
            if(musicItem.getAlbumName().toLowerCase().contains(mLastQuery) || musicItem.getArtistName().toLowerCase().contains(mLastQuery) || musicItem.getTitle().toLowerCase().contains(mLastQuery)){
                mMusicsToDisplay.add(musicItem);
            }
        }
        mAdapter.setItemList(mMusicsToDisplay);
        mAdapter.notifyDataSetChanged();

    }


    @Override
    public void onClick(Item item) {
        if(item instanceof MusicItem) {
            mMediaPlayerService.setMusicList(mMusicsToDisplay);
            mMediaPlayerService.setCurrent(mMusicsToDisplay.indexOf(item) );
            mMediaPlayerService.prepareAndPlay();
        }
        else{

        }
    }

    @Override
    public void onBind(MediaPlayerService lu) {
        mMediaPlayerService = lu;
    }

    @Override
    public void onMusicClick(List<MusicItem> musics, int position) {
        mMediaPlayerService.setMusicList(musics);
        mMediaPlayerService.setCurrent(position);
        mMediaPlayerService.prepareAndPlay();
    }

    public boolean handleSearch(String query) {
        mLastQuery = query.toLowerCase();
        refreshFilter();
        return true;
    }


}
