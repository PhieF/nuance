package com.spipi.spipimediaplayer.library.ftp;

import android.content.Context;
import android.net.Uri;

import com.spipi.spipimediaplayer.library.FileEditor;
import com.spipi.spipimediaplayer.library.FileInfo;
import com.spipi.spipimediaplayer.library.RawLister;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;


public class FTPFileInfo extends FileInfo {

    private static final long serialVersionUID = 2L;

    private final String mUriString;
    private final String mName;
    private final boolean mIsDirectory;
    private final boolean mIsFile;
    private final long mLastModified;
    private final boolean mCanRead;
    private final boolean mCanWrite;
    private final long mLength;

    public FTPFileInfo(org.apache.commons.net.ftp.FTPFile file, Uri uri) {

        if (uri == null) {
            throw new IllegalArgumentException("uri cannot be null");
        }

        mUriString = uri.toString();
        String name = file.getName();
        mIsDirectory = file.isDirectory();
        mIsFile = file.isFile();
        if (file.getTimestamp() != null)
            mLastModified = file.getTimestamp().getTimeInMillis();
        else
            mLastModified = 0;
        mCanRead = file.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION);
        mCanWrite = file.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION);
        mLength = file.getSize();
        // remove the '/' at the end of directory name
        if (mIsDirectory && name.endsWith("/")) {
            mName = name.substring(0, name.length()-1);
        } else {
            mName = name;
        }
    }

    @SuppressWarnings("unused")
    private FTPFileInfo() {
        throw new IllegalArgumentException("Unauthorized to create a FTPFile2 from nothing! Can only be created from a org.apache.commons.net.ftp.FTPFile and an android.net.Uri");
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
        if (other instanceof FTPFileInfo) {
            return getUri().equals( ((FTPFileInfo)other).getUri() );
        } else {
            return false;
        }
    }

    @Override
    public Uri getUri() {
        return Uri.parse(this.mUriString);
    }

    public RawLister getRawListerInstance() {
        return new FTPRawLister(getUri()) ;
    }

    @Override
    public FileEditor getFileEditorInstance(Context ct) {
        return new FtpFileEditor(getUri());
    }
    /**
     * get metafile2 object from a uri (please use this only if absolutely necessary
     *
     */
    public static FileInfo fromUri(Uri uri) throws Exception {
        FTPClient ftp=null;
        if(uri.getScheme().equals("ftps"))
            ftp= Session.getInstance().getNewFTPSClient(uri, FTP.BINARY_FILE_TYPE);
        else
            ftp= Session.getInstance().getNewFTPClient(uri, FTP.BINARY_FILE_TYPE);
        FTPFile ftpFile = ftp.mlistFile(uri.getPath());
        if(ftpFile!=null)
            return new FTPFileInfo(ftpFile,uri);
        else
            return null;
    }
}
