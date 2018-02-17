package com.spipi.spipimediaplayer.playlists;

import com.spipi.spipimediaplayer.MusicItem;

import java.util.List;

/**
 * Created by alexandre on 08/07/15.
 */
public abstract class PlayList {

    public static final int ERROR_ASK_FOR_CONVERSION = -1;
    public static final int SUCCESS = 0;

    public final static int TYPE_LOCAL = 0;
    public final static int TYPE_HUBIC =1;
    public final static int TYPE_DEEZER =2;

    /**
     * Error code
     * @param mi
     * @return
     */
    public abstract int addToPlaylist(MusicItem mi);
    public abstract String getReadableError(int errorCode);
    public abstract String getDisplayName();
    public abstract List<MusicItem> getItemList();
    public abstract int getItemCount();

}
