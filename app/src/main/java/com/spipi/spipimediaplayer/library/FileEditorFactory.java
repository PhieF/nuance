package com.spipi.spipimediaplayer.library;

import android.content.Context;
import android.net.Uri;

import com.spipi.spipimediaplayer.library.ftp.FtpFileEditor;
import com.spipi.spipimediaplayer.library.hubic.HubicFileEditor;
import com.spipi.spipimediaplayer.library.local.LocalFileEditor;
import com.spipi.spipimediaplayer.library.smb.SmbFileEditor;
import com.spipi.spipimediaplayer.library.sftp.SftpFileEditor;


/**
 * create a file editor
 * @author alexandre
 *
 */
public class FileEditorFactory {
    public static FileEditor getFileEditorForUrl(Uri uri, Context ct) {
        if ("smb".equalsIgnoreCase(uri.getScheme())) {
            return new SmbFileEditor(uri);
        } else if ("file".equalsIgnoreCase(uri.getScheme()) || uri.getScheme() == null) {
            return new LocalFileEditor(uri);
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
