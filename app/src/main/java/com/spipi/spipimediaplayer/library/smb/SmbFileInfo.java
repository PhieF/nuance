package com.spipi.spipimediaplayer.library.smb;

import android.content.Context;
import android.net.Uri;


import com.spipi.spipimediaplayer.library.FileEditor;
import com.spipi.spipimediaplayer.library.FileInfo;
import com.spipi.spipimediaplayer.library.RawLister;

import java.net.MalformedURLException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SmbFileInfo extends FileInfo {

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
    public SmbFileInfo(SmbFile file) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        // Only use methods doing no network access here
        mUriString = file.getCanonicalPath();
        String name  = file.getName();
        mIsDirectory = file.isDirectory_noquery();
        mIsFile = file.isFile_noquery();
        mLastModified = file.lastModified_noquery();
        mCanRead = file.canRead_nonetwork();
        mCanWrite = file.canWrite_nonetwork();
        mLength = file.length_noNetwork();

        // remove the '/' at the end of directory name (Jcifs adds it)
        if (mIsDirectory && name.endsWith("/")) {
            mName = name.substring(0, name.length()-1);
        } else {
            mName = name;
        }
    }


    private SmbFileInfo(Uri uri) throws MalformedURLException, SmbException {

       SmbFile file = SmbFileEditor.getSmbFile(uri);
        // Using the methods doing network access to get the actual data
        mUriString = file.getCanonicalPath();
        String name  = file.getName();
        mIsDirectory = file.isDirectory();
        mIsFile = file.isFile();
        mLastModified = file.lastModified();
        mCanRead = file.canRead();
        mCanWrite = file.canWrite();
        mLength = file.length();
        // remove the '/' at the end of directory name (Jcifs adds it)
        if (mIsDirectory && name.endsWith("/")) {
            mName = name.substring(0, name.length()-1);
        } else {
            mName = name;
        }
    }

    /**
     * This method performs network access to get data about the file. Handle with care!
     * @param uri
     * @return
     * @throws MalformedURLException
     * @throws SmbException
     */
    public static FileInfo fromUri(Uri uri) throws MalformedURLException, SmbException {
        return new SmbFileInfo(uri);
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
        return other instanceof SmbFileInfo && getUri().equals(((SmbFileInfo) other).getUri());
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
