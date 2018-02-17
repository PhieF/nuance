package com.spipi.spipimediaplayer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;

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
public class SearchFragment extends GenericFragment implements AlbumView.OnMusicClickListener, MyApplication.Mp3ServiceBindListener, MediaPlayerService.OnChangeListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ARTIST = "param1";
    private static final String ARG_ALBUM = "param2";
    private List<Item> mResult;
    private MediaPlayerService mMediaPlayerService;
    private String mLastQuery="";

    @Override
    public void onCreate(Bundle save){

        super.onCreate(save);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mResult = new ArrayList<>();
        ((MainActivity)getActivity()).setSearchFragment(this);
        postOnCreate(view);
        return view;
    }
    // TODO: Rename and change types of parameters
    public static SearchFragment newInstance(String artist, String album) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST, artist);
        args.putString(ARG_ALBUM, album);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SearchFragment() {
    }
    protected LinearLayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(mResult!=null)
        ((MainActivity)getActivity()).setSearchFragment(this);

        ((MyApplication)getActivity().getApplication()).bindToMp3Service(this);
    }


    @Override
    public void onResume(){
        super.onResume();
        if(mResult!=null)
            ((MainActivity)getActivity()).setSearchFragment(this);

    }

    @Override
    public void onPause( ){
        super.onPause();
        ((MainActivity)getActivity()).unsetSearchFragment();
    }
    @Override
    public void setItemList(){
        mAdapter.setLayout(R.layout.music_item_layout);
        mAdapter.setOnMusicClickListener(this);
          }
    @Override
    public void onBind(MediaPlayerService lu) {
        if(!isDetached()) {
            mMediaPlayerService = lu;
            mMediaPlayerService.addOnChangeListener(this);
        }
    }
    @Override
    public void onDetach(){
        super.onDetach();
        mMediaPlayerService.removeOnChangeListener(this);
    }
    @Override
    public void onClick(final Item item) {
        if(item instanceof MusicItem){
            ArrayList<MusicItem> items = new ArrayList<>();
            items.add((MusicItem) item);
            mMediaPlayerService.setMusicList(items);
            mMediaPlayerService.setCurrent(0);
            mMediaPlayerService.prepareAndPlay();
        }
        else if (item instanceof ArtistItem){
            getActivity().onBackPressed();
            Fragment frag = new AlbumsFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(AlbumsFragment.ARG_ARTIST,(ArtistItem)item);

            frag.setArguments(bundle);
            ((MainActivity)getActivity()).setFragment(frag);
        }

    }

    public void doMySearch(final String query) {
        mResult.clear();
        if(mLastQuery.equals(query))
            return;
        mLastQuery = query;
        new AsyncTask<Void, Void, List<? extends Item>>() {


            @Override
            protected List<? extends Item> doInBackground(Void... voids) {
                MusicDatasource md = new MusicDatasource(getActivity());
                md.open();
                List<? extends Item> musics = md.searchMusics(query, PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("only_local_pref",false));
                if(musics!=null){
                    mResult.add(new TextItem(getString(R.string.title_musics)));
                    mResult.addAll(musics);
                }

                md.close();
                return mResult;
            }

            @Override
            protected void onPostExecute(List<? extends Item> result){
                if(result!=null)
                mAdapter.setItemList(result);
                mAdapter.notifyDataSetChanged();
                startAlbumRetrieval(query);
            }
        }.execute();
    }

    private void startAlbumRetrieval(final String query) {
        new AsyncTask<Void, Void, List<? extends Item>>() {


            @Override
            protected List<? extends Item> doInBackground(Void... voids) {
                MusicDatasource md = new MusicDatasource(getActivity());
                List<? extends Item> result = md.searchAlbums(query, PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("only_local_pref",false));

                if(result!=null) {
                    mResult.add(new TextItem(getString(R.string.title_albums)));
                    mResult.addAll(result);
                }
                md.close();
                return mResult;
            }

            @Override
            protected void onPostExecute(List<? extends Item> result){

                if(result!=null) {
                    mAdapter.setItemList(result);
                    mAdapter.notifyDataSetChanged();
                }
                startArtistRetrieval(query);
            }
        }.execute();
    }

    private void startArtistRetrieval(final String query) {
        new AsyncTask<Void, Void, List<? extends Item>>() {


            @Override
            protected List<? extends Item> doInBackground(Void... voids) {
                MusicDatasource md = new MusicDatasource(getActivity());
                md.open();
                List<? extends Item> result = md.searchArtists(query, PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("only_local_pref",false));
                if(result!=null) {
                    mResult.add(new TextItem(getString(R.string.title_artist)));
                    mResult.addAll(result);
                }
                md.close();
                return mResult;
            }

            @Override
            protected void onPostExecute(List<? extends Item> result){
                if(result!=null) {
                    mAdapter.setItemList(result);
                }
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onMusicClick(List<MusicItem> musics, int position) {
        onClick(musics.get(position));
    }

    @Override
    public void onServiceChange() {
        if(mMediaPlayerService!=null&&mAdapter!=null) {
            mAdapter.setPlayingMusic(mMediaPlayerService.getCurrentMusicItem());
            mAdapter.notifyDataSetChanged();
        }
    }
}
