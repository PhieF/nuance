package com.spipi.spipimediaplayer.library.hubic;

import android.net.Uri;

import com.spipi.spipimediaplayer.Access;
import com.spipi.spipimediaplayer.hubic.AccessDatasource;
import com.spipi.spipimediaplayer.library.FileEditor;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 03/12/15.
 */
public class HubicFileEditor extends FileEditor {
    public HubicFileEditor(Uri uri) {
        super(uri);
    }


    @Override
    public boolean mkdir() {
        return false;
    }

    @Override
    public InputStream getInputStream() throws Exception {
        Access acc;
        AccessDatasource ad =  AccessDatasource.sAccessDatasource;
        ad.open();
        acc = ad.getAccess(mUri.getHost());
        ad.close();
        String distantPath = mUri.getPath();

        try {
            distantPath= URLEncoder.encode(distantPath, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        List<NameValuePair> header = new ArrayList<>(1);
        header.add(new BasicNameValuePair("X-Auth-Token", acc.getOpenStackAccessToken()));
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet hg = new HttpGet(acc.getOpenStackUrl()+"/default/"+distantPath);
        for(int i=0; i<header.size(); i++){
            hg.setHeader(header.get(i).getName().toString(), header.get(i).getValue().toString());
        }

        try {

            HttpResponse response = httpclient.execute(hg);
            InputStream inputStream = response.getEntity().getContent();
            return  inputStream;

        } catch (ClientProtocolException e) {

        } catch (IOException e) {

        }

        return null;
    }

    @Override
    public InputStream getInputStream(long from) throws Exception {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws Exception {
        return null;
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
        return false;
    }
}
