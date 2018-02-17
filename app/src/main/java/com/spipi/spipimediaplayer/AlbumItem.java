package com.spipi.spipimediaplayer;

/**
 * Created by alexandre on 26/05/15.
 */
public class AlbumItem extends ExpandableItem implements Comparable<AlbumItem>{
    private final ArtistItem mArtistItem;
    private final long mId;
    private final String mPicture;
    private String mAlbum;
    private String mThumbnail;
    private String artistName;

    public AlbumItem(ArtistItem artistItem,String artistName, String album, String picture, String thumbnail,long id){
        mAlbum = album;
        this.artistName = artistName;
        mArtistItem = artistItem;
        mThumbnail=thumbnail;
        mPicture=picture;

        mId = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlbumItem albumItem = (AlbumItem) o;

        if (mArtistItem != null ? !mArtistItem.equals(albumItem.mArtistItem) : albumItem.mArtistItem != null)
            return false;
        if (mAlbum != null ? !mAlbum.equals(albumItem.mAlbum) : albumItem.mAlbum != null)
            return false;
        return !(artistName != null ? !artistName.equals(albumItem.artistName) : albumItem.artistName != null);

    }

    @Override
    public int hashCode() {
        int result = mArtistItem != null ? mArtistItem.hashCode() : 0;
        result = 31 * result + (mAlbum != null ? mAlbum.hashCode() : 0);
        result = 31 * result + (artistName != null ? artistName.hashCode() : 0);
        return result;
    }

    public String getName(){
        return mAlbum;
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
    public ArtistItem getArtistItem() {
        return mArtistItem;
    }

    public long getId() {
        return mId;
    }



    @Override
    public int compareTo(AlbumItem albumItem) {
        return getName().compareTo(albumItem.getName());
    }

    public String getArtistName() {
        return artistName;
    }
}
