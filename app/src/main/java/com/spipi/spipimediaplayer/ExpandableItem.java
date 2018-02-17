package com.spipi.spipimediaplayer;

/**
 * Created by alexandre on 07/09/15.
 */
public abstract class ExpandableItem extends Item {

    private boolean mIsDeployed;

    public boolean isDeployed() {
        return mIsDeployed;
    }
    public void setDeployed(boolean deployed) {
        mIsDeployed = deployed;
    }
}
