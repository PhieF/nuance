package com.spipi.spipimediaplayer;

import android.net.Uri;

/**
 * Created by alexandre on 26/05/15.
 */
public class PlaylistItem extends ExpandableItem implements Comparable<PlaylistItem>{
    private final long mId;
    private final String mPicture;
    private final int mType;
    private String mName;
    private String mThumbnail;
    private String mUri;
    private long mModificationDate;

    public PlaylistItem(String name, String picture, String thumbnail, long id, Uri uri, long modificationDate, int type){
        mName = name;
        mThumbnail=thumbnail;
        mPicture=picture;
        mUri = uri.toString();
        mId = id;
        mModificationDate = modificationDate;
        mType = type;
    }
    public String getName(){
        return mName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaylistItem that = (PlaylistItem) o;

        return !(mName != null ? !mName.equals(that.mName) : that.mName != null);

    }

    @Override
    public int hashCode() {
        return mName != null ? mName.hashCode() : 0;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getThumbnail() {
        return mThumbnail;
    }
    @Override
    public String getPicture() {
        return mPicture;
    }

    public long getId() {
        return mId;
    }


    @Override
    public int compareTo(PlaylistItem albumItem) {
        return getName().compareTo(albumItem.getName());
    }

    public Uri getUri() {
        return Uri.parse(mUri);
    }

    public long getModificationDate(){
        return mModificationDate;
    }

    public void setModificationDate(long lastModified) {
        mModificationDate= lastModified;
    }

    public int getType(){
        return mType;
    }

}
