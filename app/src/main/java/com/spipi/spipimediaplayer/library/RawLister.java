package com.spipi.spipimediaplayer.library;

import android.net.Uri;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.spipi.spipimediaplayer.library.ftp.AuthenticationException;

import java.io.IOException;
import java.util.List;


/**
 * returns 
 * @author alexandre
 *
 */
public abstract class RawLister {
    protected Uri mUri;
    public RawLister(Uri uri){
        mUri = uri;
    }
    public abstract List<FileInfo> getFileList() throws IOException, AuthenticationException, SftpException, JSchException;
}
