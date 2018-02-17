package com.spipi.spipimediaplayer.network.sftp;

import android.net.Uri;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.spipi.spipimediaplayer.network.NetworkCredentialsDatabase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SFTPSession {
    private static SFTPSession sshSession = null;
    private ConcurrentHashMap<NetworkCredentialsDatabase.Credential, Session> sessions;
    public SFTPSession(){
        sessions = new ConcurrentHashMap<NetworkCredentialsDatabase.Credential, Session>();
    }
	
	
	
	public static SFTPSession getInstance(){
		if(sshSession==null)
			sshSession= new SFTPSession();
		return sshSession;
	}

	public Channel getSFTPChannel(Uri cred) throws JSchException{
        Session session = getSession(cred);
        if(session !=null){
            try {
                Channel channel = session.openChannel("sftp");
                channel.connect();
                return channel;
            } catch (JSchException e) {
                //channel isn't openable, we have to reset the session !
                removeSession(cred);
                Session session2 = getSession(cred);
                if(session2 !=null){
                    try {
                        Channel channel;
                        channel = session2.openChannel("sftp");             
                        channel.connect();
                        return channel;
                        } catch (JSchException e1) {
                            // TODO Auto-generated catch block
                            throw e1;
                        }
                    
                }
            }
        }
        
        return null;

    }
    public void removeSession(Uri cred){

        for(Map.Entry<NetworkCredentialsDatabase.Credential, Session> e : sessions.entrySet()){
            Uri uri = Uri.parse(e.getKey().getUriString());
            if(uri.getHost().equals(cred.getHost())&&uri.getPort()==cred.getPort()){
                e.getValue().disconnect();
                sessions.remove(e.getKey());
            }
        }


    }


    private Uri buildKeyFromUri(Uri uri) {
        // We use the Uri without the path segment as key: for example, "ftp://blabla.com:21/toto/titi" gives a "ftp://blabla.com:21" key
        return uri.buildUpon().path("").build();
    }


    public Session getSession(Uri path) throws JSchException{
        String username="anonymous";

        String password = "";
        NetworkCredentialsDatabase database = NetworkCredentialsDatabase.getInstance();
        NetworkCredentialsDatabase.Credential cred = database.getCredential(path.toString());
        if(cred==null){
            cred = new NetworkCredentialsDatabase.Credential("anonymous","",buildKeyFromUri(path).toString(), true);

        }
        if(cred!=null){
            password= cred.getPassword();
            username = cred.getUsername();
        }

        Session session = sessions.get(cred);
        if(session!=null){
            if(!session.isConnected())
                try {
                    session.connect();
                } catch (JSchException e1) {
                    removeSession(path);
                    return getSession(path);
                }
            return session;
        }

        JSch jsch=new JSch();
        try {
            session = jsch.getSession(username, path.getHost(), path.getPort());
            session.setPassword(password);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            sessions.put(cred, session);
            return session;
        } catch (JSchException e) {
            // TODO Auto-generated catch block
            throw e;

        }
        

    }
}
