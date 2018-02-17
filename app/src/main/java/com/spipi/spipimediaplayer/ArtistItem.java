package com.spipi.spipimediaplayer;

/**
 * Created by alexandre on 26/05/15.
 */
public class ArtistItem extends Item implements Comparable<ArtistItem>{
    private final String mPicture;

    public long getId() {
        return mId;
    }

    private final long mId;
    private String mArtist;
    private String mThumbnail;
    public ArtistItem(String artist, String thumbnail, String picture, long ID){
        mArtist = artist;
        mThumbnail=thumbnail;
        mPicture = picture;
        mId = ID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArtistItem that = (ArtistItem) o;

        if (mId != that.mId) return false;
        return !(mArtist != null ? !mArtist.equals(that.mArtist) : that.mArtist != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (mId ^ (mId >>> 32));
        result = 31 * result + (mArtist != null ? mArtist.hashCode() : 0);
        return result;
    }

    public String getName(){
        return mArtist;
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
    @Override
    public int compareTo(ArtistItem albumItem) {
        return getName().compareToIgnoreCase(albumItem.getName());
    }
}
