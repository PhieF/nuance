package com.spipi.spipimediaplayer.library.local;

import android.net.Uri;

import com.spipi.spipimediaplayer.library.FileEditor;
import com.spipi.spipimediaplayer.library.NetworkCredentialsDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbRandomAccessFile;

public class LocalFileEditor extends FileEditor {

    public LocalFileEditor(Uri uri) {
        super(uri);
    }

    @Override
    public boolean mkdir() {
            new File(mUri.toString()).mkdir();
            return true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(mUri.toString());
    }

    @Override
    public InputStream getInputStream(long from) throws Exception {
        InputStream is = new FileInputStream(mUri.toString());
        is.skip(from);
        return is;
    }

    @Override
    public OutputStream getOutputStream() throws SmbException, MalformedURLException, UnknownHostException, FileNotFoundException {
        return  new FileOutputStream(mUri.toString());
    }


    @Override
    public void delete() throws Exception {
    }

    @Override
    public boolean rename(String newName) {

        return false;
    }



    @Override
    public boolean exists() {

        return new File(mUri.toString()).exists();
    }

}
