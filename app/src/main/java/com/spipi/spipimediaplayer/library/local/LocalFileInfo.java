package com.spipi.spipimediaplayer.library.local;

import android.content.Context;
import android.net.Uri;

import com.spipi.spipimediaplayer.library.FileEditor;
import com.spipi.spipimediaplayer.library.FileInfo;
import com.spipi.spipimediaplayer.library.RawLister;
import com.spipi.spipimediaplayer.library.smb.SmbFileEditor;
import com.spipi.spipimediaplayer.library.smb.SmbRawLister;

import java.io.File;
import java.net.MalformedURLException;


public class LocalFileInfo extends FileInfo {

    private static final long serialVersionUID = 2L;

    private String mName;
    private boolean mIsDirectory;
    private boolean mIsFile;
    private long mLastModified;
    private boolean mCanRead;
    private boolean mCanWrite;
    private long mLength;
    private String mUriString;

    /**
     * SmbFile argument must contain already valid data (name, size, etc.)
     * because this method won't make any network call
     */
    public LocalFileInfo(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        buildFromFile(file);
    }

    private void buildFromFile(File file){
        mUriString = file.getAbsolutePath();
        mName  = file.getName();
        mIsDirectory = file.isDirectory();
        mIsFile = file.isFile();
        mLastModified = file.lastModified();
        mCanRead = true;
        mCanWrite = true;
    }


    private LocalFileInfo(Uri uri)  {
        buildFromFile(new File(uri.getPath()));
    }


    public static FileInfo fromUri(Uri uri) {
        return new LocalFileInfo(uri);
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public boolean isDirectory() {
        return mIsDirectory;
    }

    @Override
    public boolean isFile() {
        return mIsFile;
    }

    @Override
    public long lastModified() {
        return mLastModified;
    }

    @Override
    public long length() {
        return mLength;
    }

    @Override
    public boolean canRead() {
        return mCanRead;
    }

    @Override
    public boolean canWrite() {
        return mCanWrite;
    }

    @Override
    public boolean isRemote() {
        return true;

    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LocalFileInfo && getUri().equals(((LocalFileInfo) other).getUri());
    }

    @Override
    public Uri getUri() {
        return Uri.parse(this.mUriString);
    }

    @Override
    public RawLister getRawListerInstance() {
        return new SmbRawLister(getUri());
    }

    @Override
    public FileEditor getFileEditorInstance(Context ct) {
        return new SmbFileEditor(getUri());
    }
}
