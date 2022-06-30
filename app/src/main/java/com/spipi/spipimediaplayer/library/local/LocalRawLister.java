package com.spipi.spipimediaplayer.library.local;

import android.net.Uri;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.spipi.spipimediaplayer.library.FileInfo;
import com.spipi.spipimediaplayer.library.FileInfoFactory;
import com.spipi.spipimediaplayer.library.RawLister;
import com.spipi.spipimediaplayer.library.ftp.AuthenticationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalRawLister extends RawLister {
    public LocalRawLister(Uri uri) {
        super(uri);
    }

    @Override
    public List<FileInfo> getFileList() throws IOException, AuthenticationException, SftpException, JSchException {
        ArrayList<FileInfo> list = new ArrayList();
        File [] files = new File(mUri.getPath()).listFiles();
        if(files == null)
            return list;
        for (File file: files){
            try {
                list.add(new LocalFileInfo(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
