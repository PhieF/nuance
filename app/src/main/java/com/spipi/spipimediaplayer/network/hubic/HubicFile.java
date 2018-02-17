package com.spipi.spipimediaplayer.network.hubic;

import android.content.Context;
import android.net.Uri;

import com.spipi.spipimediaplayer.network.FileEditor;
import com.spipi.spipimediaplayer.network.FileInfo;
import com.spipi.spipimediaplayer.network.RawLister;

/**
 * Created by alexandre on 03/12/15.
 */
public class HubicFile extends FileInfo {

    private final String mUri;
    private final String mName;

    public HubicFile(String name, String uri){
        mUri = uri;
        mName = Uri.parse(name).getLastPathSegment();
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public RawLister getRawListerInstance() {
        return null;
    }

    @Override
    public FileEditor getFileEditorInstance(Context ct) {
        return null;
    }

    @Override
    public Uri getUri() {
        return Uri.parse(mUri);
    }

    @Override
    public boolean isRemote() {
        return true;
    }
}
