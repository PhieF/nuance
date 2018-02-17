package com.spipi.spipimediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerFactory;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;

import java.util.HashMap;
import java.util.List;

/**
 * Created by alexandre on 10/06/15.
 */
public class AlbumView extends LinearLayout implements View.OnClickListener, MyApplication.Mp3ServiceBindListener, MediaPlayerService.OnChangeListener {
    private final View mMusicsContainer;
    private final HashMap<MusicItem, View> mMusicViewMap;
    private OnMusicClickListener mOnMusicClick;
    private ExpandableItem mAlbumItem;
    private MediaPlayerService mMediaPlayerService;
    private View mLastView;
    private boolean isLoaded;
    private ItemAdapter mAdapter;
    private ItemAdapter.OnItemLongClickListener mOnMusicLongClick;

    @Override
    public void onClick(View view) {
        if(findViewById(R.id.music_container).getVisibility() == View.GONE)
            displayMusicList();
        else
            hideMusicList();

    }
    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(receiver);
        mMediaPlayerService.removeOnChangeListener(this);
    }
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mAlbumItem!=null)
            Log.d("reloaddebug", "onReceive" +mAlbumItem.getDisplayName());
            if(isLoaded&&mAlbumItem!=null&&mAlbumItem instanceof AlbumItem && "LibraryUpdated".equals(intent.getAction())){

               if(((AlbumItem) mAlbumItem).getName().equalsIgnoreCase(intent.getStringExtra("album")))
                    refreshMusicList();
            }
            if(isLoaded&&mAlbumItem!=null&&mAlbumItem instanceof PlaylistItem &&
                    (intent.getAction().equals("ReloadAllPlaylists")||
                    intent.getLongExtra("playlist",-1)>=0&&((PlaylistItem) mAlbumItem).getId()==intent.getLongExtra("playlist",-1)
                    )
                ){
                Log.d("reloaddebug", "refreshMusicList");
                refreshMusicList();
            }

        }
    };
    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        Log.d("attachdebug", "onAttachedToWindow");
        IntentFilter filter = new IntentFilter();
        filter.addAction("ReloadPlaylist");
        filter.addAction("LibraryUpdated");
        filter.addAction("ReloadAllPlaylists");
        getContext().registerReceiver(receiver, filter);
        if(mMediaPlayerService!=null)
            mMediaPlayerService.addOnChangeListener(this);
    }
    private void hideMusicList() {
        findViewById(R.id.music_container).setVisibility(View.GONE);
        mAlbumItem.setDeployed(false);
    }

    @Override
    public void onBind(MediaPlayerService lu) {
        mMediaPlayerService = lu;
        mMediaPlayerService.addOnChangeListener(this);
    }
    AsyncTask<Void, Void, Void> musicTask ;
    @Override
    public void onServiceChange() {
        if(mLastView!=null)
            mLastView.findViewById(R.id.imageView).setVisibility(View.INVISIBLE);
        if(mMusics!=null&&(mLastView=mMusicViewMap.get(mMediaPlayerService.getCurrentMusicItem()))!=null) {
            mLastView.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
        }
    }

    public void setOnMusicLongClickListener(ItemAdapter.OnItemLongClickListener mOnItemLongClickListener) {
        mOnMusicLongClick = mOnItemLongClickListener;
    }

    public interface OnMusicClickListener{
        public void onMusicClick(List<MusicItem> musics, int position);
    }
    public List<MusicItem> getMusicList() {
        return mMusics;
    }

    private List<MusicItem> mMusics = null;

    public AlbumView(Context context, AttributeSet attrs, int defStyle)
    {

        super(context, attrs, defStyle);
        setOnClickListener(this);
        mMusicsContainer = findViewById(R.id.music_container);
        ((MyApplication)context.getApplicationContext()).bindToMp3Service(this);
        mMusicViewMap = new HashMap<>();

    }
    public void setItemAdapter(ItemAdapter adapter){
        mAdapter = adapter;
    }
    public AlbumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        mMusicsContainer = findViewById(R.id.music_container);
        ((MyApplication)context.getApplicationContext()).bindToMp3Service(this);
        mMusicViewMap = new HashMap<>();


    }
    public  AlbumView(Context context){
        super(context);
        setOnClickListener(this);
        mMusicsContainer = findViewById(R.id.music_container);
        ((MyApplication)context.getApplicationContext()).bindToMp3Service(this);
        mMusicViewMap = new HashMap<>();


    }
    public void setOnMusicClickListener(OnMusicClickListener onMusicClick){
        mOnMusicClick = onMusicClick;
    }
    public void refreshMusicList(){
        musicTask = new AsyncTask<Void, Void, Void>(){

            @SuppressWarnings("ResourceType")
            @Override
            protected Void doInBackground(Void... voids) {
                if(getContext()==null)
                    return null ;
                int i = 0;
                MusicDatasource mMusicDatasource = new MusicDatasource(getContext());
                if(mAlbumItem instanceof AlbumItem) {

                    mMusics = (List<MusicItem>) mMusicDatasource.getAllMusics(((AlbumItem)mAlbumItem).getArtistItem(),(AlbumItem) mAlbumItem, PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("only_local_pref", false));
                    if(mAdapter!=null&&mMusics!=null)
                        mAdapter.setMusicList(mAlbumItem, mMusics);
                }
                else {
                    mMusics = (List<MusicItem>) mMusicDatasource.getAllMusicsFromPlaylist((PlaylistItem) mAlbumItem, PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("only_local_pref", false));

                }

                isLoaded=true;
                return null;

            }
            @Override
            protected void onPostExecute(Void result){
                if(mMusics!=null)
                    displayMusicList();
            }
        };
        musicTask.execute();
    }
    public void displayMusicList(){
        if(mMusics!=null) {
            loadMusicList();
            findViewById(R.id.music_container).setVisibility(View.VISIBLE);
            mAlbumItem.setDeployed(true);
        }
        else{
            refreshMusicList();
        }

    }
    public void setAlbum(ExpandableItem item, List<MusicItem> musics){
        isLoaded = false;
        mMusics=null;

        mMusics=musics;
        if(musics!=null) {
            isLoaded = true;
            mMusicViewMap.clear();
        }
        mAlbumItem   = item;
        if(mAlbumItem.isDeployed())
            displayMusicList();
        else
            findViewById(R.id.music_container).setVisibility(View.GONE);
    }
    public void loadMusicList(){

        LinearLayout musicContainer = (LinearLayout)findViewById(R.id.music_container);
        musicContainer.removeAllViews();
        int pos = 0;
        if(mMusics!=null) {
            for (MusicItem music : mMusics) {
                View musicView = LayoutInflater.from(getContext()).inflate(R.layout.music_item_layout, musicContainer, false);

                mMusicViewMap.put(music, musicView);
                Uri uri = Uri.parse(music.getPath());
                if(!(uri.getScheme()==null||"file".equals(uri.getScheme()))){
                    musicView.setBackgroundColor(Color.parseColor("#e1e1e1"));
                }
                ((TextView) musicView.findViewById(R.id.title)).setText(music.getDisplayName());
                if (mMediaPlayerService != null && mMediaPlayerService.getCurrentMusicItem() == music)
                    musicView.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                else
                    musicView.findViewById(R.id.imageView).setVisibility(View.INVISIBLE);
                final int finalPos = pos;
                if (mAlbumItem instanceof PlaylistItem)
                    ((ImageView) musicView.findViewById(R.id.plus)).setImageResource(R.drawable.minus);
                else
                    ((ImageView) musicView.findViewById(R.id.plus)).setImageResource(R.drawable.plus);
                musicView.findViewById(R.id.plus).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAlbumItem instanceof PlaylistItem) {
                            MusicDatasource md = new MusicDatasource(getContext());
                            md.open();
                            int accessID = 0;
                            String path;
                            if (((MusicItem) mMusics.get(finalPos)).getType() == MediaPlayerFactory.TYPE_HUBIC || ((MusicItem) mMusics.get(finalPos)).getType() == MediaPlayerFactory.TYPE_DEEZER) {
                                accessID = Integer.valueOf(Uri.parse(((MusicItem) mMusics.get(finalPos)).getPath()).getHost());
                                path = Uri.parse(((MusicItem) mMusics.get(finalPos)).getPath()).getPath();
                            } else {
                                accessID = -((MusicItem) mMusics.get(finalPos)).getType();
                                path = ((MusicItem) mMusics.get(finalPos)).getPath();

                            }
                            md.removeFromPlaylist(accessID, path, ((PlaylistItem) mAlbumItem).getId());
                            md.close();
                            Intent intent = new Intent("ReloadPlaylist");
                            intent.putExtra("playlist", ((PlaylistItem) mAlbumItem).getId());
                            getContext().sendBroadcast(intent);
                        } else if (mOnMusicLongClick != null)
                            mOnMusicLongClick.onLongClick(mMusics.get(finalPos), AlbumView.this);
                    }
                });
                musicView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mOnMusicLongClick != null)
                            mOnMusicLongClick.onLongClick(mMusics.get(finalPos), AlbumView.this);
                        return true;
                    }
                });
                musicView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnMusicClick != null)
                            mOnMusicClick.onMusicClick(mMusics, finalPos);
                    }
                });
                musicContainer.addView(musicView);
                pos++;
            }

        }
    }
    public void setMusicList(List<MusicItem> musics){


    }
}
