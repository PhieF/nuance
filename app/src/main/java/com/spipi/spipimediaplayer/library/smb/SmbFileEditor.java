package com.spipi.spipimediaplayer.library.smb;

import android.net.Uri;


import com.spipi.spipimediaplayer.library.FileEditor;
import com.spipi.spipimediaplayer.library.NetworkCredentialsDatabase;

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

public class SmbFileEditor extends FileEditor {

    public SmbFileEditor(Uri uri) {
        super(uri);
    }

    @Override
    public boolean mkdir() {
        try {
            getSmbFile(mUri).mkdir();
            return true;
        } catch (SmbException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new SmbFileInputStream(getSmbFile(mUri));
    }

    @Override
    public InputStream getInputStream(long from) throws Exception {
        InputStream is = new SmbRandomAccessFile(getSmbFile(mUri), "r");
        ((SmbRandomAccessFile)is).seek(from);
        return is;
    }

    @Override
    public OutputStream getOutputStream() throws SmbException, MalformedURLException, UnknownHostException {
        return  new SmbFileOutputStream(getSmbFile(mUri));
    }


    @Override
    public void delete() throws Exception {
        getSmbFile(mUri).delete();
    }

    @Override
    public boolean rename(String newName) {
        try {
            SmbFile from = getSmbFile(mUri);
            if(from!=null) {
                SmbFile to = getSmbFile(Uri.parse(from.getParent() + "/" + newName));
                if(to!=null){

                    from.renameTo(to);
                    return true;
                }
            }
        }
        catch (SmbException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static SmbFile getSmbFile(Uri uri) throws MalformedURLException {

        NetworkCredentialsDatabase.Credential cred = NetworkCredentialsDatabase.getInstance().getCredential(uri.toString());
        SmbFile smbfile;
        if(cred==null||cred.getUsername().isEmpty())
            cred = new NetworkCredentialsDatabase.Credential("guest","",uri.toString(),true);
        if(cred!=null){
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("",cred.getUsername(), cred.getPassword());
            smbfile= new SmbFile(uri.toString(),auth);
        }
        else {
            smbfile= new SmbFile(uri.toString());
        }
        return smbfile;

    }

    @Override
    public boolean exists() {
        try {
            SmbFile sf = getSmbFile(mUri);
            if(sf!=null)
                return sf.exists();
        } catch (SmbException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
