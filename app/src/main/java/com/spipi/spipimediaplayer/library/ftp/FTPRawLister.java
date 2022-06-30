package com.spipi.spipimediaplayer.library.ftp;

import android.net.Uri;


import com.spipi.spipimediaplayer.library.FileInfo;
import com.spipi.spipimediaplayer.library.RawLister;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * returns 
 * @author alexandre
 *
 */
public class FTPRawLister extends RawLister {
    public FTPRawLister(Uri uri) {
        super(uri);
    }

    public List<FileInfo> getFileList() throws IOException, AuthenticationException {
        FTPClient ftp = Session.getInstance().getFTPClient(mUri);
        ftp.cwd(mUri.getPath()); 
        org.apache.commons.net.ftp.FTPFile[] listFiles = ftp.listFiles(); 

        if(listFiles==null)
            return null;
        ArrayList<FileInfo> list = new ArrayList<FileInfo>();
        for(org.apache.commons.net.ftp.FTPFile f : listFiles){
            if(!f.getName().equals("..")|| !f.getName().equals(".")){
                FTPFileInfo sf = new FTPFileInfo(f , Uri.withAppendedPath(mUri, f.getName()));
                list.add(sf);   
            }
        }
        return list;
    }
}
