package com.spipi.spipimediaplayer;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.mediaplayer.FloatingService;

public class NewMainFragment extends Fragment implements  ItemAdapter.OnItemClickListener, ItemAdapter.OnItemLongClickListener, View.OnClickListener {

    private LinearLayout mView;
    private Fragment mFragment;
    private Toolbar mToolbar;

    public static NewMainFragment newInstance() {
        NewMainFragment fragment = new NewMainFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.artists_button:
                setFragment(ArtistFragment.newInstance("",""));
            case R.id.albums_button:
                setFragment(AlbumsFragment.newInstance(null));
            case R.id.playlists_button:
                setFragment(PlaylistsFragment.newInstance());
            case R.id.favorites_button:
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(mView==null) {
            mView = (LinearLayout) inflater.inflate(R.layout.fragment_main_new, container, false);
            mView.findViewById(R.id.artists_button).setOnClickListener(this);
            mView.findViewById(R.id.playlists_button).setOnClickListener(this);
            mView.findViewById(R.id.albums_button).setOnClickListener(this);
            mView.findViewById(R.id.favorites_button).setOnClickListener(this);
            mToolbar = ((Toolbar)mView.findViewById(R.id.myToolbar));
            mToolbar.inflateMenu(R.menu.menu_main);
            mToolbar.setTitle(R.string.app_name);
            Menu menu = mToolbar.getMenu();
            menu.findItem(R.id.action_enable_floating_player).setChecked(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(FloatingService.ENABLE_FLOATING_PLAYER, false));
            menu.findItem(R.id.action_enable_low_ram).setChecked(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("low_ram",false));
            menu.findItem(R.id.action_only_local).setCheckable(true).setChecked(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("only_local_pref", false));
            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
            // Assumes current activity is the searchable activity
            int searchImgId = android.support.v7.appcompat.R.id.search_button; // I used the explicit layout ID of searchview's ImageView
            ImageView v = (ImageView) searchView.findViewById(searchImgId);
            v.setImageResource(R.drawable.ic_menu_search);
            SearchableInfo info = searchManager.getSearchableInfo(getActivity().getComponentName());
            searchView.setSearchableInfo(info);
            Log.d("MainActivity","setQuery");

            searchView.setQuery("", false);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d("MainActivity","onQueryTextSubmit "+query);
                    searchView.clearFocus();
                    if(mFragment != null && mFragment instanceof GenericFragment && ((GenericFragment) mFragment).handleSearch(query)){
                        return true;
                    }
               /*if (searchFragment == null) {
                    SearchFragment fragm = new SearchFragment();
                    setFragment(fragm);
                }
                else if(searchFragment!=null)
                    searchFragment.doMySearch(searchView.getQuery().toString());
                mLastQuery = searchView.getQuery().toString();*/
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });


        } else {
            ((ViewGroup)mView.getParent()).removeView(mView);
        }
        return mView;
    }

    @Override
    public void onClick(Item item) {

    }

    @Override
    public void onLongClick(Item item, AlbumView albumView) {

    }


    public void onAlbumsClick(View view) {
        setFragment(AlbumsFragment.newInstance(null));
    }

    private void setFragment(Fragment fragment) {

        mFragment = fragment;
        ((MainActivity)getActivity()).setFragment(fragment);
    }

    public void onArtistsClick(View view) {
        setFragment(ArtistFragment.newInstance("", ""));
    }

    public void onPlaylistsClick(View view) {
        setFragment(PlaylistsFragment.newInstance());
    }

    public void onFavoritesClick(View view) {
        setFragment(PlaylistsFragment.newInstance());

    }
}
