package com.spipi.spipimediaplayer.network.sftp;

import android.content.Context;
import android.net.Uri;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.spipi.spipimediaplayer.network.FileEditor;
import com.spipi.spipimediaplayer.network.FileInfo;
import com.spipi.spipimediaplayer.network.RawLister;
import com.spipi.spipimediaplayer.network.ftp.AuthenticationException;

import java.net.UnknownHostException;

public class SFTPFileInfo extends FileInfo {

    private static final long serialVersionUID = 2L;

    private final String mName;
    private final boolean mIsDirectory;
    private final boolean mIsFile;
    private final long mLastModified;
    private final boolean mCanRead;
    private final boolean mCanWrite;
    private final long mLength;
    private final String mUriString;
    
    /*
     * if this file is a symbolic link, filename isn't the same as file path
     * 
     */   
    public SFTPFileInfo(SftpATTRS stat, String filename, Uri uri) {
        if (filename == null){
            throw new IllegalArgumentException("filename cannot be null");
        }
        if (uri == null) {
            throw new IllegalArgumentException("uri cannot be null");
        }

        mUriString = uri.toString();
        mName = filename;
        mIsDirectory = stat.isDir();
        mIsFile = !stat.isDir();
        mLastModified = stat.getMTime();
        //TODO : permissions
        mCanRead = true;
        mCanWrite = true;
        mLength = stat.getSize();
    }

    @SuppressWarnings("unused")
    private SFTPFileInfo() {
        throw new IllegalArgumentException("Unauthorized to create a SFTPFile2 from nothing! Can only be created from a com.jcraft.jsch.SftpATTRS and an android.net.Uri");
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
        if (other instanceof SFTPFileInfo) {
            return getUri().equals( ((SFTPFileInfo)other).getUri() );
        } else {
            return false;
        }
    }

    @Override
    public Uri getUri() {
        return Uri.parse(this.mUriString);
    }

    @Override
    public RawLister getRawListerInstance() {
        return new SFTPRawLister(getUri());
    }

    @Override
    public FileEditor getFileEditorInstance(Context ct) {
        return new SftpFileEditor(getUri());
    }

    /**
     * get metafile2 object from a uri (please use this only if absolutely necessary
     *
     */
    public static FileInfo fromUri(Uri uri) throws Exception {
        try {

            Session session = SFTPSession.getInstance().getSession(uri);
            Channel channel = SFTPSession.getInstance().getSFTPChannel(uri);
            SftpATTRS attrs = ((ChannelSftp)channel).stat(uri.getPath());
            return new SFTPFileInfo(attrs,uri.getLastPathSegment(), uri);
        } catch (JSchException e) {
            if(e.getCause() instanceof UnknownHostException)
                throw new UnknownHostException();
            else
                throw new AuthenticationException();
        } catch (SftpException e) {
            throw new Exception("permission");
        }
    }


}
