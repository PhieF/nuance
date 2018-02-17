package com.spipi.spipimediaplayer.network;

import android.content.Context;
import android.net.Uri;

import com.spipi.spipimediaplayer.network.ftp.FtpFileEditor;
import com.spipi.spipimediaplayer.network.hubic.HubicFileEditor;
import com.spipi.spipimediaplayer.network.smb.SmbFileEditor;
import com.spipi.spipimediaplayer.network.sftp.SftpFileEditor;


/**
 * create a file editor
 * @author alexandre
 *
 */
public class FileEditorFactory {
    public static FileEditor getFileEditorForUrl(Uri uri, Context ct) {
        if ("smb".equalsIgnoreCase(uri.getScheme())) {
            return new SmbFileEditor(uri);
        }
        else if ("ftp".equalsIgnoreCase(uri.getScheme())||"ftps".equalsIgnoreCase(uri.getScheme())) {
            return new FtpFileEditor(uri);
        }
        else if ("sftp".equalsIgnoreCase(uri.getScheme())) {
            return new SftpFileEditor(uri);
        }
        else if ("hubic".equalsIgnoreCase(uri.getScheme())) {
            return new HubicFileEditor(uri);
        }

        else {
            throw new IllegalArgumentException("not implemented yet for "+uri);
        }
    }
}
