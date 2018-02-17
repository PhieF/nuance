package com.spipi.spipimediaplayer.network;

import android.net.Uri;

import com.spipi.spipimediaplayer.network.ftp.FTPRawLister;
import com.spipi.spipimediaplayer.network.smb.SmbRawLister;
import com.spipi.spipimediaplayer.network.sftp.SFTPRawLister;


public class RawListerFactory {

    public static RawLister getRawListerForUrl(Uri uri) {

        if ("smb".equals(uri.getScheme())) {
            return new SmbRawLister(uri);
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
