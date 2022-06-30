package com.spipi.spipimediaplayer.library;

import android.net.Uri;

import com.spipi.spipimediaplayer.library.ftp.FTPRawLister;
import com.spipi.spipimediaplayer.library.local.LocalRawLister;
import com.spipi.spipimediaplayer.library.smb.SmbRawLister;
import com.spipi.spipimediaplayer.library.sftp.SFTPRawLister;


public class RawListerFactory {

    public static RawLister getRawListerForUrl(Uri uri) {

        if ("smb".equals(uri.getScheme())) {
            return new SmbRawLister(uri);
        } else if ("file".equals(uri.getScheme()) || uri.getScheme() == null) {
            return new LocalRawLister(uri);
        }
        else if ("ftp".equals(uri.getScheme()) || "ftps".equals(uri.getScheme())) {
            return new FTPRawLister(uri) {
            };
        }
        else if ("sftp".equals(uri.getScheme())) {
            return new SFTPRawLister(uri);
        }

        else {
            throw new IllegalArgumentException("not implemented yet for "+uri);
        }
    }
}
