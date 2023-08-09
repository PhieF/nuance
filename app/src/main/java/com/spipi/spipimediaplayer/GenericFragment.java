package com.spipi.spipimediaplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.mediaplayer.FloatingService;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerFactory;

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
public abstract class GenericFragment extends Fragment implements  ItemAdapter.OnItemClickListener, ItemAdapter.OnItemLongClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";



    /**
     * The fragment's ListView/GridView.
     */
    protected RecyclerView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    protected ItemAdapter mAdapter;
    protected MusicDatasource mMusicDatasource;
    private LinearLayoutManager mLayoutManager;
    private BroadcastReceiver receiver;
    private boolean mHasBeenSet;
    private View mView;
    protected Toolbar mToolbar;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GenericFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d("leakdebug", "onCreate");
        // TODO: Change Adapter to display your content
        if (mAdapter==null){
            mAdapter = new ItemAdapter(getActivity());
        mMusicDatasource = new MusicDatasource(getActivity());

        setItemList();
        }
        mHasBeenSet = true;
    }
    public void setItemList(){

        mAdapter.setItemList(mMusicDatasource.getAllArtists(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("only_local_pref", false)));

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("leakdebug", "oncreateview " + getClass().getCanonicalName());
        if(mView==null) {
            mView = inflater.inflate(R.layout.fragment_artists_grid, container, false);
            mToolbar = ((Toolbar)mView.findViewById(R.id.myToolbar));
            mToolbar.inflateMenu(R.menu.menu_generic);
            Menu menu = mToolbar.getMenu();
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
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    filter("");
                    return false;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filter(query);
                    searchView.clearFocus();
                    Log.d("querydebug","query "+query);

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            postOnCreate(mView);
        } else {
            ((ViewGroup)mView.getParent()).removeView(mView);
        }
        return mView;
    }

    protected void filter(String query){
        mAdapter.filter(query);
        Log.d("querydebug","query "+query);

    }

    public void postOnCreate(View view){
        // Set the adapter
        mListView = (RecyclerView) view.findViewById(R.id.list);
        mListView.setAdapter(mAdapter);
        mLayoutManager = getLayoutManager();
        mListView.setLayoutManager(mLayoutManager);
        mListView.setHasFixedSize(false);
        // Set OnItemClickListener so we can be notified on item clicks
        mAdapter.setOnItemClickListener(this);
        mAdapter.setItemLongClickListener(this);

    }

    protected LinearLayoutManager getLayoutManager() {
        return new GridLayoutManager(getActivity(),3);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(receiver==null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("LibraryUpdated");
            filter.addAction("Reload");
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (mHasBeenSet) {
                        if(intent.getAction().equals("LibraryUpdated"))
                            setItemList();
                        mAdapter.notifyDataSetChanged();
                    }
                }
            };
            activity.registerReceiver(receiver, filter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("memorydebug", "mAdapter");
       mAdapter = null;
        try {
            getActivity().unregisterReceiver(receiver);
        }
        catch (Exception e){}
        if(mListView!=null)
            unbindDrawables(mListView);
        System.gc();
        mListView = null;
        mLayoutManager = null;
        mMusicDatasource = null;

    }
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            Log.d("memorydebug", "setCallback");
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }


    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {

    }

    public void showAddToPlayListDialog(Item item){
        final MusicDatasource md = new MusicDatasource(getActivity());
        final List<PlaylistItem> playlistItemList = md.getAllPlaylists(false);
        List<CharSequence> list = new ArrayList<>();
        for(PlaylistItem pl : playlistItemList){
            list.add(pl.getName());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_to_playlist);
        builder.setItems(list.toArray(new CharSequence[]{}), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                md.open();
                int accessID = 0;
                String path;
                if(((MusicItem) item).getType()== MediaPlayerFactory.TYPE_HUBIC) {
                    accessID = Integer.valueOf(Uri.parse(((MusicItem) item).getPath()).getHost());
                    path = Uri.parse(((MusicItem) item).getPath()).getPath();
                    if(path.startsWith("/"))
                        path = path.substring(1);
                }
                else if (Uri.parse(((MusicItem) item).getPath()).getScheme()!=null){
                    if("deezer".equalsIgnoreCase(Uri.parse(((MusicItem) item).getPath()).getScheme())){
                        accessID = -MediaPlayerFactory.TYPE_DEEZER;
                        path = Uri.parse(((MusicItem) item).getPath()).getHost();
                        if(path.startsWith("/"))
                            path = path.substring(1);
                    }
                    else{
                        accessID = -MediaPlayerFactory.TYPE_NEW;
                        path = ((MusicItem) item).getPath();

                    }
                }
                else{
                    accessID = - ((MusicItem) item).getType();
                    path = ((MusicItem) item).getPath();

                }
                md.addToPlaylist(accessID, path, playlistItemList.get(which).getId());
                Intent intent = new Intent("ReloadPlaylist");
                intent.putExtra("playlist",playlistItemList.get(which).getId());
                getActivity().sendBroadcast(intent);
                md.close();
                Toast.makeText(getActivity(), playlistItemList.get(which).getDisplayName(), Toast.LENGTH_LONG).show();
            }
        });
        builder.create().show();
    }


    @Override
    public void onLongClick(final Item item, AlbumView albumView) {
        if(item instanceof MusicItem){
            showAddToPlayListDialog(item);
        }
    }

    public boolean handleSearch(String query) {
        return false;
    }
}
