package com.spipi.spipimediaplayer.network.ftp;

import android.net.Uri;
import android.util.Log;

import com.spipi.spipimediaplayer.network.NetworkCredentialsDatabase;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;


public class Session {
    private static final String TAG = "ftp.Session";
    private static Session sSession = null;
    private final HashMap<NetworkCredentialsDatabase.Credential, FTPClient> ftpsClients;

    private HashMap <NetworkCredentialsDatabase.Credential, FTPClient> ftpClients;
    public Session(){
        ftpClients = new HashMap<NetworkCredentialsDatabase.Credential, FTPClient>();
        ftpsClients = new HashMap<NetworkCredentialsDatabase.Credential, FTPClient>();
    }


    public void removeFTPClient(Uri cred){
        for(Map.Entry<NetworkCredentialsDatabase.Credential,FTPClient> e : ftpClients.entrySet()){
            if((e.getKey()).getUriString().equals(cred.toString())){
                ftpClients.remove(e.getKey()); 
            }
        }
    }

    public FTPClient getNewFTPClient(Uri path, int mode) throws SocketException, IOException, AuthenticationException{

        // Use default port if not set
        int port = path.getPort();
        if (port<0) {
            port = 21; // default port
        }

        String username="anonymous"; // default user
        String password = ""; // default password

        NetworkCredentialsDatabase database = NetworkCredentialsDatabase.getInstance();
        NetworkCredentialsDatabase.Credential cred = database.getCredential(path.toString());
        if(cred!=null){
            password= cred.getPassword();
            username = cred.getUsername();
        }
        FTPClient ftp= new FTPClient();

        //try to connect
        ftp.connect(path.getHost(), port);
        //login to 	server
        if(!ftp.login(username, password))
        {
            ftp.logout();
            throw new AuthenticationException();
        }
        if(mode>=0){
            ftp.setFileType(mode);

        }
        int reply = ftp.getReplyCode();
        //FTPReply stores a set of constants for FTP reply codes. 
        if (!FTPReply.isPositiveCompletion(reply))
        {
            try {
                ftp.disconnect();
            } catch (IOException e) {
                throw e;
            }
            return null;
        }
        //enter passive mode
        ftp.enterLocalPassiveMode();

        return ftp;
    }

    public FTPClient getFTPClient(Uri uri) throws SocketException, IOException, AuthenticationException{
        NetworkCredentialsDatabase database = NetworkCredentialsDatabase.getInstance();
        NetworkCredentialsDatabase.Credential cred = database.getCredential(uri.toString());
        if(cred==null){
            cred = new NetworkCredentialsDatabase.Credential("anonymous","", buildKeyFromUri(uri).toString(), true);
        }
        FTPClient ftpclient = ftpClients.get(cred);
        if (ftpclient!=null && ftpclient.isConnected()){
            return ftpclient;
        }
        // Not previous session found, open a new one
        Log.d(TAG, "create new ftp session for "+uri);
        FTPClient ftp = getNewFTPClient(uri,FTP.BINARY_FILE_TYPE);
        if(ftp==null)
            return null;
        Uri key = buildKeyFromUri(uri);
        Log.d(TAG, "new ftp session created with key "+key);
        ftpClients.put(cred, ftp);
        return ftp;
    }
    public FTPClient getFTPSClient(Uri uri) throws SocketException, IOException, AuthenticationException{
        NetworkCredentialsDatabase database = NetworkCredentialsDatabase.getInstance();
        NetworkCredentialsDatabase.Credential cred = database.getCredential(uri.toString());
        if(cred==null){
            cred = new NetworkCredentialsDatabase.Credential("anonymous","", buildKeyFromUri(uri).toString(), true);
        }
        FTPClient ftpclient = ftpsClients.get(cred);
        if (ftpclient!=null && ftpclient.isConnected()){
            return ftpclient;
        }
        // Not previous session found, open a new one
        Log.d(TAG, "create new ftp session for "+uri);
        FTPClient ftp = getNewFTPSClient(uri, FTP.BINARY_FILE_TYPE);
        if(ftp==null)
            return null;
        Uri key = buildKeyFromUri(uri);
        Log.d(TAG, "new ftp session created with key "+key);
        ftpsClients.put(cred, ftp);
        return ftp;
    }
    private Uri buildKeyFromUri(Uri uri) {
        // We use the Uri without the path segment as key: for example, "ftp://blabla.com:21/toto/titi" gives a "ftp://blabla.com:21" key
        return uri.buildUpon().path("").build();
    }

    public static Session getInstance(){
        if(sSession==null)
            sSession= new Session();
        return sSession;
    }

    public FTPClient getNewFTPSClient(Uri path, int mode) throws SocketException, IOException, AuthenticationException{

        // Use default port if not set
        int port = path.getPort();
        if (port<0) {
            port = 21; // default port
        }

        String username="anonymous"; // default user
        String password = ""; // default password

        NetworkCredentialsDatabase database = NetworkCredentialsDatabase.getInstance();
        NetworkCredentialsDatabase.Credential cred = database.getCredential(path.toString());
        if(cred!=null){
            password= cred.getPassword();
            username = cred.getUsername();
        }
        FTPSClient ftp= new FTPSClient("TLS", false);
        //try to connect
        ftp.connect(path.getHost(), port);

        //login to 	server
        if(!ftp.login(username, password))
        {

            ftp.logout();
            throw new AuthenticationException();
        }
        if(mode>=0){
            ftp.setFileType(mode);

        }
        int reply = ftp.getReplyCode();
        //FTPReply stores a set of constants for FTP reply codes.
        if (!FTPReply.isPositiveCompletion(reply))
        {
            try {
                ftp.disconnect();
            } catch (IOException e) {
                throw e;
            }
            return null;
        }
        //enter passive mode
        ftp.enterLocalPassiveMode();
        // Set protection buffer size
        ftp.execPBSZ(0);
        // Set data channel protection to private
        ftp.execPROT("P");

        return ftp;
    }
}
