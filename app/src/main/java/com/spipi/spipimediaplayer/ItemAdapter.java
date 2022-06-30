package com.spipi.spipimediaplayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alexandre on 28/05/15.
 */
public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int HEADER = 4;
    private final int ALBUM = 0;
    private final  int ARTIST = 1;
    private final int MUSIC = 2;
    private final int TEXT =5;
    private final int BUTTON=7;
    private final int PLAYLIST = 6;
    private List<Item> mList;
    private Activity mContext;
    private int mLayout;
    private OnItemClickListener mOnItemClickListener;
    private HashMap<ExpandableItem, List<MusicItem>> mMusics;
    private AlbumView.OnMusicClickListener mOnMusicClick;
    private View mHeader;
    private MusicItem playingMusic;
    private OnItemLongClickListener mOnItemLongClickListener;

    public ItemAdapter(Activity context) {
        super();
        mContext = context;
        mMusics = new HashMap<>();
        mLayout = R.layout.grid_item_layout;
        mList = new ArrayList<Item>();
    }
    public void setItemList(List<? extends Item> list){
        for(Item item : list){
            if(item instanceof ExpandableItem){
                Item it = null;
                if(mList.contains(item)&&(it=mList.get(mList.indexOf(item))) instanceof ExpandableItem)
                    ((ExpandableItem) item).setDeployed(((ExpandableItem)it).isDeployed());
            }
        }
        mList.clear();
        mList.addAll(list);
    }

    public void setOnItemClickListener(OnItemClickListener genericFragment) {
        mOnItemClickListener = genericFragment;
    }
    public void setItemLongClickListener(OnItemLongClickListener genericFragment) {
        mOnItemLongClickListener = genericFragment;
    }
    public void setMusicList(HashMap<ExpandableItem, List<MusicItem>> musics) {
        mMusics = musics;
    }
    public void setMusicList(ExpandableItem album, List<MusicItem> musics) {
        mMusics.put(album, musics);
    }
    public void setOnMusicClickListener(AlbumView.OnMusicClickListener onMusicClick) {
        mOnMusicClick = onMusicClick;
    }

    public void setPlayingMusic(MusicItem playingMusic) {
        this.playingMusic = playingMusic;

    }

    public interface OnItemClickListener{
        public void onClick(Item item);
    }
    public interface OnItemLongClickListener{
        public void onLongClick(Item item, AlbumView albumView);
    }
    public class MyViewHolder extends RecyclerView.ViewHolder{

        private final TextView mTextView;
        private final ImageView mThumbnailView;
        private final AlbumView mAlbumView;
        private final View mPlayButton;
        private Item mItem;

        public MyViewHolder(View itemView, ViewGroup viewGroup) {
            super(itemView);
            View click  = itemView.findViewById(R.id.clickable);
            if(!(itemView instanceof AlbumView)) {
                if (click == null)
                    click = itemView;

                click.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemClickListener.onClick(mItem);
                    }
                });
            }
            if(itemView instanceof AlbumView) {
                mAlbumView = (AlbumView) itemView;
                mAlbumView.setItemAdapter(ItemAdapter.this);
                mAlbumView.setOnMusicClickListener(mOnMusicClick);
                mAlbumView.setOnMusicLongClickListener(mOnItemLongClickListener);
            }

            else
                mAlbumView = null;

            if(itemView.findViewById(R.id.plus)!=null&&mOnItemLongClickListener!=null){
                itemView.findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemLongClickListener.onLongClick(mItem,null);
                    }
                });
            }
            if(itemView.findViewById(R.id.imageView)!=null){
                mPlayButton = itemView.findViewById(R.id.imageView);
            }
            else
                mPlayButton= null;
            mTextView = (TextView)itemView.findViewById(R.id.title);
            mThumbnailView = (ImageView) itemView.findViewById(R.id.picture);
            if(mThumbnailView!=null&&mThumbnailView.getLayoutParams().height<=0) {
                mThumbnailView.getLayoutParams().height = viewGroup.getWidth() / 3;
                if(click!=null)
                    click.getLayoutParams().height = viewGroup.getWidth() / 3;
            }

        }



        public void setThumbnail(Bitmap map){

            if(mThumbnailView!=null)
                mThumbnailView.setImageBitmap(map);
        }
        public void setThumbnail(int res){
            if(mThumbnailView!=null)
                mThumbnailView.setImageResource(res);
        }

        public void setText(String displayName) {
            mTextView.setText(displayName);
        }

        public void setMusics(List<MusicItem> musicItems) {
            if(mAlbumView!=null)
                mAlbumView.setMusicList(musicItems);
        }

        public void setItem(Item item) {
            mItem = item;
        }

        public void setAlbum(Item item,List<MusicItem> musicItem) {
            setItem(item);
            if(mAlbumView!=null)
                mAlbumView.setAlbum((ExpandableItem) item,musicItem);
        }

        public void setIsPlaying(boolean equals) {
            if(mPlayButton!=null)
                mPlayButton.setVisibility(equals ? View.VISIBLE : View.INVISIBLE);
        }


        public void setThumbnailVisibility(int thumbnailVisibility) {
            if (mThumbnailView != null)
            mThumbnailView.setVisibility(thumbnailVisibility);
        }
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder{


        public SimpleViewHolder(View itemView) {
            super(itemView);

        }
    }

    public class TextViewHolder extends RecyclerView.ViewHolder{
        private final View mItemView;
        TextView tv ;
        public TextViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            tv = (TextView)itemView.findViewById(R.id.text);

        }
        public void setOnClickListener(View.OnClickListener onClickListener){
            mItemView.setOnClickListener(onClickListener);
        }
        public void setText(String text){
            tv.setText(text);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = null;
        if(viewType == TEXT ||viewType == BUTTON){
            v= mContext.getLayoutInflater().inflate(R.layout.text_item_layout,viewGroup, false);
            return new TextViewHolder(v);
        }
        else if (viewType==HEADER){
            return new SimpleViewHolder(mHeader);
        }
        else if(viewType==ARTIST)
            v = mContext.getLayoutInflater().inflate(R.layout.grid_item_layout,viewGroup, false);
        else if(viewType==ALBUM)
            v= mContext.getLayoutInflater().inflate(R.layout.layout_album_item,viewGroup, false);
        else if(viewType == PLAYLIST)
            v= mContext.getLayoutInflater().inflate(R.layout.layout_playlist_item,viewGroup, false);
        else
            v= mContext.getLayoutInflater().inflate(R.layout.music_item_layout,viewGroup, false);
        return new MyViewHolder(v, viewGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        /*if(mBitmaps.size()>i&&mBitmaps.get(i)!=null)
            mBitmaps.get(i).recycle();*/
        if(viewHolder.getItemViewType() == HEADER)
            return;

        if(mHeader!=null)
            i = i-1;
        if(viewHolder.getItemViewType()==TEXT||viewHolder.getItemViewType() == BUTTON){
            ((TextViewHolder)viewHolder).setText(mList.get(i).getDisplayName());
            if(viewHolder.getItemViewType() == BUTTON)
                ((TextViewHolder)viewHolder).setOnClickListener(((ButtonItem)mList.get(i)).getOnClickListener());
            return;
        }
        ((MyViewHolder)viewHolder).setText(mList.get(i).getDisplayName());
        if (mList.get(i).getThumbnail() != null && mList.get(i).getThumbnail().length() != 0) {

            Bitmap myBitmap;
            if(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("low_ram",false)){
                BitmapFactory.Options optionsDec = new BitmapFactory.Options();
                optionsDec.inSampleSize = 4;
                myBitmap = BitmapFactory.decodeFile(mList.get(i).getThumbnail(), optionsDec);
            }
            else
                myBitmap = BitmapFactory.decodeFile(mList.get(i).getThumbnail());
            if(myBitmap!=null)
                ((MyViewHolder)viewHolder).setThumbnail(myBitmap);
            else
                ((MyViewHolder)viewHolder).setThumbnail(getDefaultResource());
            //mBitmaps.add(myBitmap);
        } else
            ((MyViewHolder)viewHolder).setThumbnail(getDefaultResource());
        if(viewHolder.getItemViewType()== ALBUM ) {
            ((MyViewHolder) viewHolder).setAlbum(mList.get(i), mMusics.get(mList.get(i)));


        }
        else
            ((MyViewHolder) viewHolder).setItem(mList.get(i));
        if(viewHolder.getItemViewType()==MUSIC){
            ((MyViewHolder)viewHolder).setIsPlaying(mList.get(i).equals(playingMusic));
        }


    }

    @Override
    public long getItemId(int i) {

        return 0;
    }

    @Override
    public int getItemCount() {
        return mList.size()+(mHeader!=null?1:0);
    }
    @Override
    public int getItemViewType(int position){
        if(position == 0 &&mHeader!=null)
            return HEADER;
        if(mHeader!=null)
            position--;
        if(mList.get(position) instanceof ArtistItem){
            return ARTIST;
        }
        else if (mList.get(position) instanceof AlbumItem){
            return ALBUM;
        }
        else if (mList.get(position) instanceof PlaylistItem)
            return PLAYLIST;
        else if (mList.get(position) instanceof TextItem){
            return TEXT;
        }
        else if (mList.get(position) instanceof ButtonItem){
            return BUTTON;
        }
        else
            return MUSIC;
    }
    public void setHeader(View header){
        mHeader = header;
    }

    private int getDefaultResource() {
        return R.drawable.unknown_artist;
    }

    public void setLayout(int music_item_layout) {
        mLayout = music_item_layout;
    }
}
