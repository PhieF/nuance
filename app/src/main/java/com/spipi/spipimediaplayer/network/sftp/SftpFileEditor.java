package com.spipi.spipimediaplayer.network.sftp;

import android.net.Uri;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.spipi.spipimediaplayer.network.FileEditor;
import com.spipi.spipimediaplayer.network.ftp.AuthenticationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;

public class SftpFileEditor  extends FileEditor {

    public SftpFileEditor(Uri uri) {
        super(uri);
    }



    @Override
    public boolean mkdir() {
        Session session;
        try {
            session = SFTPSession.getInstance().getSession(mUri);
            Channel channel = SFTPSession.getInstance().getSFTPChannel(mUri);
            ChannelSftp channelSftp = (ChannelSftp)channel;
            channelSftp.mkdir(mUri.getPath());
            return true;
        }
        catch (SftpException e) {

        } catch (JSchException e) {
            e.printStackTrace();
        } 
        return false;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException, JSchException, SftpException {
        Session session = SFTPSession.getInstance().getSession(mUri);
        Channel channel = SFTPSession.getInstance().getSFTPChannel(mUri);
        return ((ChannelSftp)channel).get(mUri.getPath());
    }

    @Override
    public InputStream getInputStream(long from) throws Exception {
        Session session = SFTPSession.getInstance().getSession(mUri);
        Channel channel = SFTPSession.getInstance().getSFTPChannel(mUri);
        return ((ChannelSftp)channel).get(mUri.getPath(), null, from);
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException, JSchException, SftpException {
        Session session = SFTPSession.getInstance().getSession(mUri);
        Channel channel = SFTPSession.getInstance().getSFTPChannel(mUri);
        return ((ChannelSftp)channel).put(mUri.getPath());
    }

    @Override
    public void delete() throws Exception {
        try {
            Session session = SFTPSession.getInstance().getSession(mUri);
            Channel channel = SFTPSession.getInstance().getSFTPChannel(mUri);
            ((ChannelSftp)channel).rm(mUri.getPath());
        } catch (JSchException e) {
            if(e.getCause() instanceof UnknownHostException)
                throw new UnknownHostException();
            else
                throw new AuthenticationException();
        } catch (SftpException e) {
            throw new Exception("permission");
        } 


    }
    @Override
    public boolean rename(String newName){
        try {
            Session session = SFTPSession.getInstance().getSession(mUri);
            Channel channel = SFTPSession.getInstance().getSFTPChannel(mUri);
            ((ChannelSftp)channel).rename(mUri.getPath(), new File(new File(mUri.getPath()).getParentFile(), newName).getAbsolutePath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean exists() {
        try {
            Session session = SFTPSession.getInstance().getSession(mUri);
            Channel channel = SFTPSession.getInstance().getSFTPChannel(mUri);
            SftpATTRS attrs =  ((ChannelSftp)channel).stat(mUri.getPath());
            return attrs !=null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
