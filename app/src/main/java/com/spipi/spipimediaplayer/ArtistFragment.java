package com.spipi.spipimediaplayer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.spipi.spipimediaplayer.hubic.hubicLibraryUpdate;

import java.util.Collections;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * interface.
 */
public class ArtistFragment extends GenericFragment implements hubicLibraryUpdate.UpdateListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            mAdapter.setItemList(mArtists);
            mAdapter.notifyDataSetChanged();
        }

    };

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private List<ArtistItem> mArtists;



    // TODO: Rename and change types of parameters
    public static ArtistFragment newInstance(String param1, String param2) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistFragment() {

    }

    @Override
    public void onAttach(Activity act){
        super.onAttach(act);

    }
    @Override
    public void setItemList(){
        Log.d("statedebug", "setItemList");
        mArtists = mMusicDatasource.getAllArtists(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("only_local_pref",false));
        mAdapter.setItemList(mArtists);
    }
    public void onResume(){
        super.onResume();
        getActivity().setTitle(R.string.app_name);
    }

    @Override
    public void onClick(Item item) {
        Fragment frag = new AlbumsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(AlbumsFragment.ARG_ARTIST,(ArtistItem)item);

        frag.setArguments(bundle);
        ((MainActivity)getActivity()).setFragment(frag);

    }

    @Override
    public void onUpdate(String artist, String album, String music) {
        boolean add = true;
        for(ArtistItem art : mArtists){
            if(art.getName().equals(artist)) {
                add = false;
                break;
            }
        }
        if(add){
            mArtists.add(new ArtistItem(artist,"","",-1));
            Collections.sort(mArtists);
            mHandler.sendEmptyMessage(0);
        }
    }
    @Override
    public void onDetach(){
        super.onDetach();


    }

    @Override
    public void onLongClick(Item item, AlbumView albumView) {

    }
}
