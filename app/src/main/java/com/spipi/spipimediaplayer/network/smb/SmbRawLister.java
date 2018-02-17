package com.spipi.spipimediaplayer.network.smb;

import android.net.Uri;


import com.spipi.spipimediaplayer.network.FileInfo;
import com.spipi.spipimediaplayer.network.NetworkCredentialsDatabase;
import com.spipi.spipimediaplayer.network.RawLister;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;



public class SmbRawLister extends RawLister {
    public SmbRawLister(Uri uri) {
        super(uri);
        if(!mUri.toString().endsWith("/"))
            mUri = Uri.parse(mUri.toString()+"/");
    }

    public List<FileInfo> getFileList() throws SmbException, MalformedURLException{
        NetworkCredentialsDatabase.Credential cred = NetworkCredentialsDatabase.getInstance().getCredential(mUri.toString());
        SmbFile[] listFiles;
        if(cred==null||cred.getUsername().isEmpty())
            cred = new NetworkCredentialsDatabase.Credential("guest","",mUri.toString(),true);
        if(cred!=null){
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("",cred.getUsername(), cred.getPassword());
            listFiles = new SmbFile(mUri.toString(), auth).listFiles();
        }
        else
            listFiles = new SmbFile(mUri.toString()).listFiles();
        if(listFiles!=null){
            ArrayList<FileInfo> files = new ArrayList<>();
            for(SmbFile f : listFiles){
                files.add(new SmbFileInfo(f));
            }
            return files;
        }
        return null;
    }
}
