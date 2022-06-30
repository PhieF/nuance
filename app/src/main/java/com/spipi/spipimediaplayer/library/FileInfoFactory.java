package com.spipi.spipimediaplayer.library;

import android.net.Uri;

import com.spipi.spipimediaplayer.library.ftp.FTPFileInfo;
import com.spipi.spipimediaplayer.library.local.LocalFileInfo;
import com.spipi.spipimediaplayer.library.smb.SmbFileInfo;
import com.spipi.spipimediaplayer.library.sftp.SFTPFileInfo;


/**
 * Created by alexandre on 22/04/15.
 */
public class FileInfoFactory {
    public static FileInfo getFileInfoForUrl(Uri uri) throws Exception {


        if ("smb".equalsIgnoreCase(uri.getScheme())) {
            return SmbFileInfo.fromUri(uri);
        } else if ("file".equalsIgnoreCase(uri.getScheme()) || uri.getScheme() == null) {
            return LocalFileInfo.fromUri(uri);
        }
        else if ("ftp".equalsIgnoreCase(uri.getScheme())||"ftps".equalsIgnoreCase(uri.getScheme())) {
            return FTPFileInfo.fromUri(uri);
        }
        else if ("sftp".equalsIgnoreCase(uri.getScheme())) {
            return SFTPFileInfo.fromUri(uri);
        }
        else {
            return null;
        }
    }
}
