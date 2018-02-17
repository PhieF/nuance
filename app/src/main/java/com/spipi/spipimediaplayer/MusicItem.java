package com.spipi.spipimediaplayer;

import java.io.File;

/**
 * Created by alexandre on 22/05/15.
 */
public class MusicItem extends Item implements Comparable<MusicItem> {
    private final int mTrack;
    private String mAlbumString;
    private String mArtistString;
    private int mType;
    private String mPath;
    private ArtistItem mArtist;
    private AlbumItem mAlbum;
    private String mTitle;

    public int getmTrack() {
        return mTrack;
    }

    public MusicItem(String path, ArtistItem artist, AlbumItem album, String title, int track, int type) {
        mType = type;
        mPath = path;
        mAlbum = album;
        if (album != null)
            mAlbumString = album.getName();
        mArtist = artist;
        if (artist != null)
            mArtistString = artist.getName();
        mTrack = track;
        mTitle = title;

    }

    public MusicItem(String path, String artist, String album, String title, int track, int type, int nu) {
        mType = type;
        mPath = path;
        mAlbumString = album;
        mArtistString = artist;
        mTrack = track;
        mTitle = title;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MusicItem musicItem = (MusicItem) o;

        if (mPath != null ? !mPath.equals(musicItem.mPath) : musicItem.mPath != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mPath != null ? mPath.hashCode() : 0;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

    public ArtistItem getArtist() {
        return mArtist;
    }

    public void setArtist(ArtistItem mArtist) {
        this.mArtist = mArtist;
    }

    public AlbumItem getAlbum() {
        return mAlbum;
    }

    public void setAlbum(AlbumItem mAlbum) {
        this.mAlbum = mAlbum;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }


    @Override
    public String getDisplayName() {
        return getTitle();
    }

    @Override
    public String getThumbnail() {
        return null;

    }

    @Override
    public String getPicture() {
        String albumPath = CoverUpdaterService.getAlbumPath(mArtistString, mAlbumString);
        String artistPath = CoverUpdaterService.getArtistPath(mArtistString);
        if (mAlbum != null && mAlbum.getPicture() != null && !mAlbum.getPicture().isEmpty())
            return mAlbum.getPicture();
        else if (new File(albumPath).exists())
            return albumPath;
        else if (mArtist != null && mArtist.getPicture() != null && !mArtist.getPicture().isEmpty())
            return mArtist.getPicture();
        else if (new File(artistPath).exists())
            return artistPath;
        return null;
    }

    @Override
    public int compareTo(MusicItem musicItem) {
        if (musicItem.getmTrack() - getmTrack() != 0)
            return getmTrack() - musicItem.getmTrack() < 0 ? -1 : 1;

        return getTitle().compareTo(musicItem.getTitle());
    }

    public String getAlbumName() {
        if (mAlbum != null)
            return mAlbum.getDisplayName();
        return mAlbumString;
    }

    public String getArtistName() {
        if (mArtist != null)
            return mArtist.getDisplayName();
        return mArtistString;
    }
}
