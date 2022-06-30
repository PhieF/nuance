package com.spipi.spipimediaplayer.library.ftp;

import android.net.Uri;


import com.spipi.spipimediaplayer.library.FileEditor;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

public class FtpFileEditor extends FileEditor {
    public FtpFileEditor(Uri uri) {
        super(uri);
        // TODO Auto-generated constructor stub
    }


    @Override
    public boolean mkdir() {
        FTPClient ftp = null;
        try {
            if (mUri.getScheme().equals("ftps"))
                ftp = Session.getInstance().getNewFTPSClient(mUri, -1);
            else
                ftp = Session.getInstance().getNewFTPClient(mUri, -1);
            return ftp.makeDirectory(mUri.getPath());
        } catch (AuthenticationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public InputStream getInputStream() throws AuthenticationException, SocketException, IOException {
        FTPClient ftp = null;
        if (mUri.getScheme().equals("ftps"))
            ftp = Session.getInstance().getNewFTPSClient(mUri, FTP.BINARY_FILE_TYPE);
        else
            ftp = Session.getInstance().getNewFTPClient(mUri, FTP.BINARY_FILE_TYPE);
        InputStream is = ftp.retrieveFileStream(mUri.getPath());
        return is;

    }

    @Override
    public InputStream getInputStream(long from) throws Exception {

        FTPClient ftp;
        if (mUri.getScheme().equals("ftps"))
            ftp = Session.getInstance().getNewFTPSClient(mUri, FTP.BINARY_FILE_TYPE);
        else
            ftp = Session.getInstance().getNewFTPClient(mUri, FTP.BINARY_FILE_TYPE);
        ftp.setRestartOffset(from); // will refuse in ascii mode
        InputStream is = ftp.retrieveFileStream(mUri.getPath());
        return is;
    }

    @Override
    public OutputStream getOutputStream() throws AuthenticationException, SocketException, IOException {
        FTPClient ftp = null;
        if (mUri.getScheme().equals("ftps"))
            ftp = Session.getInstance().getNewFTPSClient(mUri, FTP.BINARY_FILE_TYPE);
        else
            ftp = Session.getInstance().getNewFTPClient(mUri, FTP.BINARY_FILE_TYPE);
        return ftp.storeFileStream(mUri.getPath());
    }

    @Override
    public void delete() throws SocketException, IOException, AuthenticationException {
        FTPClient ftp = null;
        if (mUri.getScheme().equals("ftps"))
            ftp = Session.getInstance().getNewFTPSClient(mUri, -1);
        else
            ftp = Session.getInstance().getNewFTPClient(mUri, -1);
        ftp.deleteFile(mUri.getPath());
    }

    @Override
    public boolean rename(String newName) {
        try {
            FTPClient ftp = null;
            if (mUri.getScheme().equals("ftps"))
                ftp = Session.getInstance().getFTPSClient(mUri);
            else
                ftp = Session.getInstance().getFTPClient(mUri);
            ftp.rename(mUri.getPath(), new File(new File(mUri.getPath()).getParentFile(), newName).getAbsolutePath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean exists() {
        try {
            FTPClient ftp = null;
            if (mUri.getScheme().equals("ftps"))
                ftp = Session.getInstance().getNewFTPSClient(mUri, FTP.BINARY_FILE_TYPE);
            else
                ftp = Session.getInstance().getNewFTPClient(mUri, FTP.BINARY_FILE_TYPE);
            FTPFile ftpFile = ftp.mlistFile(mUri.getPath());
            return ftpFile != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
