package com.spipi.spipimediaplayer.network.sftp;

import android.net.Uri;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.spipi.spipimediaplayer.network.FileInfo;
import com.spipi.spipimediaplayer.network.RawLister;
import com.spipi.spipimediaplayer.network.ftp.AuthenticationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;


/**
 * returns 
 * @author alexandre
 *
 */
public class SFTPRawLister extends RawLister {

    public SFTPRawLister(Uri uri) {
        super(uri);
    }

    @Override
    public ArrayList<FileInfo> getFileList() throws IOException, AuthenticationException, SftpException, JSchException {
        Channel channel = SFTPSession.getInstance().getSFTPChannel(mUri);

        if(channel==null){
            
            return null;
        }

        ChannelSftp channelSftp = (ChannelSftp)channel;
        Vector<LsEntry> vec = channelSftp.ls(mUri.getPath().isEmpty()?"/":mUri.getPath());               

        // Check Error in reading the directory.
        if (vec == null) {
           
            return null;
        }

        final ArrayList<FileInfo> files = new ArrayList<FileInfo>();
        for(LsEntry ls : vec){
            if(ls.getFilename().equals(".")||ls.getFilename().equals(".."))
                continue;
            if(ls.getAttrs().isLink()){
                try {
                    String path = channelSftp.readlink(mUri.getPath()+"/"+ls.getFilename());
                    SftpATTRS stat = channelSftp.stat(path);
                    Uri newUri = Uri.withAppendedPath(mUri, ls.getFilename());
                    SFTPFileInfo sf = new SFTPFileInfo( stat, ls.getFilename(),newUri);
                    files.add(sf);
                }catch (SftpException e) {
                    e.printStackTrace();
                }
            }
            else{
                SFTPFileInfo sf =   new SFTPFileInfo( ls.getAttrs(), ls.getFilename(),Uri.withAppendedPath(mUri, ls.getFilename()));
                files.add(sf);
            }
        }
        channel.disconnect();
        return files;
    }
 
}
