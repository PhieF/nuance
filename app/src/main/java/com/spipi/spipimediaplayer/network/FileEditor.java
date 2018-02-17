package com.spipi.spipimediaplayer.network;

import android.net.Uri;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides methods to edit files, and everything about metafile2 that requires a connection
 * @author alexandre
 * 
 * every action can rise an exception such as
 * FileNotFoundException
 * AuthenticationException
 * UnknownHostException
 * IOException, 
 * and a custom Exception (used in my case for permission exception)
 */
public abstract class FileEditor {
    protected Uri mUri;
    private static final int MAX_COUNT = 32768;
    public FileEditor(Uri uri){
        mUri = uri;
    }
    public abstract boolean mkdir();
    public abstract InputStream getInputStream() throws Exception;
    public abstract InputStream getInputStream(long from) throws Exception;
    public abstract OutputStream getOutputStream() throws Exception;
    public abstract void delete() throws Exception;
    public abstract boolean rename(String newName);
    public abstract boolean exists();

}
