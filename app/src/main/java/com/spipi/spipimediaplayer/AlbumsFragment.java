package com.spipi.spipimediaplayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * interface.
 */
public class AlbumsFragment extends GenericFragment implements MyApplication.Mp3ServiceBindListener, AlbumView.OnMusicClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_ARTIST = "param1";

    // TODO: Rename and change types of parameters
    private ArtistItem mArtist;
    private ArrayList<AlbumItem> mAlbums;
    private ArrayList<Item> mItems;
    private HashMap<AlbumItem,List<MusicItem>> mMusics;
    private ArrayList<ArrayList<MusicItem>> mMusicsToSave;
    private MediaPlayerService mMediaPlayerService;
    private Drawable mDrawable;
    private Bitmap myBitmap;
    private AsyncTask<Void, Void, Void> mTask;


    // TODO: Rename and change types of parameters
    public static AlbumsFragment newInstance(ArtistItem artist) {
        AlbumsFragment fragment = new AlbumsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ARTIST, artist);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle save){
        mArtist = (ArtistItem)getArguments().getSerializable(ARG_ARTIST);
        mMusics = new HashMap<>();
        mMusicsToSave = new ArrayList<>();
        mAlbums = new ArrayList<>();
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
        mAlbums = null;
    }
    @Override
    public void onSaveInstanceState(Bundle save){

    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlbumsFragment() {
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
        if(mArtist.getThumbnail()!=null&&mArtist.getPicture().length() != 0) {
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
        if(mTask!=null)
            mTask.cancel(true);
       mTask =  new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                if(getActivity()==null)
                    return null;
                mAlbums = (ArrayList<AlbumItem>) mMusicDatasource.getAllAlbums(mArtist, PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("only_local_pref",false));
                mItems = new ArrayList<Item>();

                return null;

            }
            @Override
        protected void onPostExecute(Void result){
                if(mAlbums==null)
                    return;
                mAdapter.setItemList(mAlbums);
                mAdapter.setOnMusicClickListener(AlbumsFragment.this);
                        mAdapter.notifyDataSetChanged();
            }
        }.execute();


    }


    @Override
    public void onClick(Item item) {
        if(item instanceof AlbumItem) {
            Fragment frag = new MusicListActivity();
            Bundle bundle = new Bundle();
            bundle.putSerializable(MusicListActivity.ARG_ARTIST, ((AlbumItem) item).getArtistItem());
            bundle.putSerializable(MusicListActivity.ARG_ALBUM, item);
            frag.setArguments(bundle);
            ((MainActivity) getActivity()).setFragment(frag);
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


}
