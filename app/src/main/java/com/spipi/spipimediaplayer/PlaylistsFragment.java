package com.spipi.spipimediaplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;
import com.spipi.spipimediaplayer.playlists.PlayList;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * interface.
 */
public class PlaylistsFragment extends GenericFragment implements MyApplication.Mp3ServiceBindListener, AlbumView.OnMusicClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private ArtistItem mArtist;
    private ArrayList<Item> mAlbums;
    private ArrayList<Item> mItems;
    private ArrayList<ArrayList<MusicItem>> mMusicsToSave;
    private MediaPlayerService mMediaPlayerService;
    private Drawable mDrawable;
    private Bitmap myBitmap;
    private MenuItem mNewPlaylist;


    // TODO: Rename and change types of parameters
    public static PlaylistsFragment newInstance() {
        PlaylistsFragment fragment = new PlaylistsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle save){
        mMusicsToSave = new ArrayList<>();
        mAlbums = new ArrayList<>();
        mDrawable = getResources().getDrawable(R.drawable.white);
        setHasOptionsMenu(true);

        super.onCreate(save);



    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mNewPlaylist = menu.add(R.string.add_playlist);

    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("reloaddebug", "onResume");
        getActivity().sendBroadcast(new Intent("ReloadAllPlaylists"));
    }
    private void newPlaylist(){
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        final EditText tv = new EditText(getActivity());
        builder.setTitle(R.string.add_playlist)
                .setView(tv)
                .setPositiveButton("create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MusicDatasource md = new MusicDatasource(getActivity());
                        md.open();
                        md.addPlaylist(tv.getText().toString(), PlayList.TYPE_LOCAL, null);
                        md.close();
                        setItemList();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        if (mNewPlaylist == item) {

            return true;
        }
    return false;
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        ((MyApplication)getActivity().getApplication()).bindToMp3Service(this);
    }

    @Override
    public void onDetach(){
        super.onDetach();

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(myBitmap!=null)
        myBitmap.recycle();
        myBitmap = null;
        mAlbums = null;
    }
    @Override
    public void onSaveInstanceState(Bundle save){

    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlaylistsFragment() {
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
        // System.out.println(alpha+" "+screenHeight +" "+locationImageInitialLocation+" "+position+" "+diff);
        return alpha;
    }
@Override
public void  postOnCreate(View view){
    super.postOnCreate(view);
    if(mArtist!=null){
        getActivity().setTitle(mArtist.getDisplayName());
        View header = getActivity().getLayoutInflater().inflate(R.layout.recycler_header, null);
        if(mArtist.getThumbnail()!=null&&!mArtist.getPicture().isEmpty()) {
            if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("low_ram",false)){
                BitmapFactory.Options optionsDec = new BitmapFactory.Options();
                optionsDec.inSampleSize = 4;
                myBitmap = BitmapFactory.decodeFile(mArtist.getPicture(), optionsDec);
            }
            else
             myBitmap = BitmapFactory.decodeFile(mArtist.getPicture());
            ((ImageView) header.findViewById(R.id.image)).setImageBitmap(myBitmap);
        }else{
            ((ImageView) header.findViewById(R.id.image)).setImageResource(R.drawable.unknown_artist);
        }
        mAdapter.setHeader(header);
    }

}


    @Override
    protected LinearLayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }
    @Override
public void setItemList(){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                mAlbums =  new ArrayList<Item>(mMusicDatasource.getAllPlaylists(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("only_local_pref", false)));
                mAlbums.add(new ButtonItem(getString(R.string.add_playlist), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newPlaylist();
                    }
                }));
                mItems = new ArrayList<Item>();

                return null;

            }
            @Override
        protected void onPostExecute(Void result){
                mAdapter.setItemList(mAlbums);
                mAdapter.setOnMusicClickListener(PlaylistsFragment.this);
                        mAdapter.notifyDataSetChanged();
            }
        }.execute();


    }


    @Override
    public void onClick(Item item) {
        if(item instanceof PlaylistItem) {
            ((MainActivity)getActivity()).setFragment(PlaylistFragment.newInstance((PlaylistItem) item));
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
}
