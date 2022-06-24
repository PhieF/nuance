package com.spipi.spipimediaplayer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.hubic.Connection;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class CoverUpdaterService extends Service {
    public final IBinder localBinder = new CoverLocalBinder();
    private boolean isUpdating;
    private boolean isLooping;
    public CoverUpdaterService() {
    }

    @Override
    public void onCreate() {

        isLooping=false;
        isUpdating=false;

    }
    @Override
    public void onStart(Intent intent, int startId) {
        //startLoop();

    }
    public void update(){
        new Thread(){
            public void run(){
                updateNoThread();
            }
        }.start();
    }
    public void updateNoThread(){

        if(!isUpdating){
            startNotification();
            isUpdating=true;
            updateArtists();
            updateAlbums();
            isUpdating=false;
            Intent i = new Intent();
            i.setAction("LibraryUpdated");
            this.sendBroadcast(i);
            stopForeground(true);
        }

    }

    private void startNotification(){
            Intent it =  new Intent(getApplicationContext(), PlayerActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                    it,
                    0);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext());
            notification.setContentTitle("Spipi Music Player");
            notification.setSmallIcon(R.drawable.ic_icon);
            //notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.setContentText("Downloading album arts");
            notification.setSubText("Please wait...");
            notification.setContentIntent(pi);

            startForeground(5, notification.getNotification());

    }
    List<UpdateListener>mListeners = new ArrayList<>();

    public static String getArtistPath(String artist) {
        return Environment.getExternalStorageDirectory()+"/hubicmusic/"+artist+".jpg";
    }

    public interface UpdateListener{
        public void onUpdate(String artist, String album);
    }

    public void addListener(UpdateListener listener) {
        mListeners.add(listener);
    }
    public void removeListener(UpdateListener listener) {
        mListeners.remove(listener);
    }

    public void updateUpdatables(String artist, String album){
        for(UpdateListener up : mListeners){
            up.onUpdate(artist,album);
        }
    }

    public static String getAlbumPath(String artist, String album){

        return Environment.getExternalStorageDirectory()+"/hubicmusic/"+artist+"/"+album;
    }
    private void getArtistPic(String artist){
        File f = new File(Environment.getExternalStorageDirectory()+"/hubicmusic/"+artist+".jpg");
        if(!f.exists()){
            Connection conn = new Connection();

            String query = artist;

            try {


                try {

                    query= URLEncoder.encode(query, "UTF-8").replace("+", "%20");

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String res= conn.sendGet("http://api.deezer.com/search/artist/?q="+query+"&index=0&limit=1&output=json", new ArrayList<NameValuePair>());
                JSONObject object = new JSONObject(res);
                if(!object.getJSONArray("data").isNull(0)){
                    String urlPicture= object.getJSONArray("data").getJSONObject(0).get("picture").toString();
                    conn.downloadFile(urlPicture+"?size=big", new ArrayList<NameValuePair>(), Environment.getExternalStorageDirectory()+"/hubicmusic/"+artist+".jpg");
                    //resize imge
                    if(f.exists()){

                        Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                        Bitmap resized = Bitmap.createScaledBitmap(myBitmap, (int) (myBitmap.getWidth() * 0.5), (int) (myBitmap.getHeight() * 0.5), true);
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        // compress to the format you want, JPEG, PNG...
                        // 70 is the 0-100 quality percentage
                        resized.compress(Bitmap.CompressFormat.JPEG,70 , outStream);
                        // we save the file, at least until we have made use of it
                        File f2 = new File(Environment.getExternalStorageDirectory()+"/hubicmusic/"+artist+"_small.jpg");
                        f2.createNewFile();
                        //write the bytes in file
                        FileOutputStream fo = new FileOutputStream(f2);
                        fo.write(outStream.toByteArray());
                        // remember close de FileOutput
                        fo.close();
                    }
                }

                updateUpdatables(artist,"");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void getAlbumPic(String artist, String album){
        File f = new File(Environment.getExternalStorageDirectory()+"/hubicmusic/"+artist+"/"+album+".jpg");
        if(!f.exists()){
            Connection conn = new Connection();
            String query = artist+" "+album;

            try {


                try {

                    query= URLEncoder.encode(query, "UTF-8").replace("+", "%20");

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String res= conn.sendGet("http://api.deezer.com/search/album/?q="+query+"&index=0&limit=1&output=json", new ArrayList<NameValuePair>());
                JSONObject object = new JSONObject(res);
                if(!object.getJSONArray("data").isNull(0)){
                    String urlPicture= object.getJSONArray("data").getJSONObject(0).get("cover").toString();
                    conn.downloadFile(urlPicture+"?size=big", new ArrayList<NameValuePair>(), Environment.getExternalStorageDirectory()+"/hubicmusic/"+artist+"/"+album+".jpg");
                    //resize imge
                    if(f.exists()){

                        Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                        Bitmap resized = Bitmap.createScaledBitmap(myBitmap, (int) (myBitmap.getWidth() * 0.5), (int) (myBitmap.getHeight() * 0.5), true);
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        // compress to the format you want, JPEG, PNG... 
                        // 70 is the 0-100 quality percentage
                        resized.compress(Bitmap.CompressFormat.JPEG,70 , outStream);
                        // we save the file, at least until we have made use of it
                        File f2 = new File(Environment.getExternalStorageDirectory()+"/hubicmusic/"+artist+"/"+album+"_small.jpg");
                        f2.createNewFile();
                        //write the bytes in file
                        FileOutputStream fo = new FileOutputStream(f2);
                        fo.write(outStream.toByteArray());
                        // remember close de FileOutput
                        fo.close();
                    }
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public void updateArtists(){
        MusicDatasource mds = new MusicDatasource(this);
        mds.open();
        List<ArtistItem> mis = mds.getAllArtists(false);
        mds.close();
        for(ArtistItem mi : mis)
            getArtistPic(mi.getName());

    }
    public void updateAlbums(){
        MusicDatasource mds = new MusicDatasource(this);
        mds.open();
        List<AlbumItem> mis = mds.getAllAlbums(null,false);
        mds.close();
        for(AlbumItem mi : mis){
            getAlbumPic(mi.getArtistName(),mi.getName());

        }

    }


    public class CoverLocalBinder extends Binder {
        public CoverUpdaterService getService(){
            return CoverUpdaterService.this;
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return localBinder;
    }


}
