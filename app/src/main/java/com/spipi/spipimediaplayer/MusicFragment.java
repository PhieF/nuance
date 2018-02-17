package com.spipi.spipimediaplayer;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * interface.
 */
public class MusicFragment extends GenericFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ARTIST = "param1";
    private static final String ARG_ALBUM = "param2";

    // TODO: Rename and change types of parameters
    private ArtistItem mArtist;
    private AlbumItem mAlbum;
    @Override
    public void onCreate(Bundle save){
        mArtist = (ArtistItem) getArguments().getSerializable(ARG_ARTIST);
        mAlbum = (AlbumItem) getArguments().getSerializable(ARG_ALBUM);
        super.onCreate(save);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists_grid, container, false);

        postOnCreate(view);
        return view;
    }
    // TODO: Rename and change types of parameters
    public static MusicFragment newInstance(String artist, String album) {
        MusicFragment fragment = new MusicFragment();
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
    public MusicFragment() {
    }


    @Override
    public void setItemList(){
        mAdapter.setLayout(R.layout.music_item_layout);
                mAdapter.setItemList(mMusicDatasource.getAllMusics(mArtist, mAlbum, PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("only_local_pref",false)));
    }


    @Override
    public void onClick(Item item) {

    }

    @Override
    public void onLongClick(Item item, AlbumView albumView) {

    }
}
