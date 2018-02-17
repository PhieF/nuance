package com.spipi.spipimediaplayer;

import java.io.Serializable;

/**
 * Created by alexandre on 28/05/15.
 */
public abstract class Item implements Serializable{
    public abstract String getDisplayName();
    public abstract String getThumbnail();

    public String getPicture() {
        return getThumbnail();
    }
}
