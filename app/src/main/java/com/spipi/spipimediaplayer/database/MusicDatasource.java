package com.spipi.spipimediaplayer.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Artists;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

import com.spipi.spipimediaplayer.AlbumItem;
import com.spipi.spipimediaplayer.ArtistItem;
import com.spipi.spipimediaplayer.Item;
import com.spipi.spipimediaplayer.MusicItem;
import com.spipi.spipimediaplayer.PlaylistItem;
import com.spipi.spipimediaplayer.hubic.HubicAccess;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MusicDatasource {


    public static final String TAG = "MusicDatasource";
    public static MusicDatasource musicDatasource;


    // Database fields
    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;
    // private Updatable up;


    private String[] allColumns = { SQLiteHelper.COLUMN_PATH,
            SQLiteHelper.COLUMN_ACCESS_ID, SQLiteHelper.COLUMN_ARTIST,SQLiteHelper.COLUMN_ALBUM, SQLiteHelper.COLUMN_MUSIC,SQLiteHelper.COLUMN_TRACK, SQLiteHelper.COLUMN_PRIORITY};

    private String[] allColumnsArtists = { SQLiteHelper.COLUMN_ARTIST, SQLiteHelper.COLUMN_AVAILABILITY};

    private Context context;
    private List<ArtistItem> mArtists;

    public MusicDatasource(Context context) {
        dbHelper = new SQLiteHelper(context);
        this.context = context;
    }

    public static MusicDatasource getInstance(Context context){
        if(musicDatasource==null)
            musicDatasource = new MusicDatasource(context);
        return musicDatasource;
    }


    public List<ArtistItem> getAllArtists(boolean hideRemote) {

        refreshArtists(hideRemote);
        return mArtists;
    }




    public void refreshArtists(boolean hideRemote){
        mArtists = new ArrayList<ArtistItem>();
        // make sure to close the cursor
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = new String[] { Artists._ID, Artists.ARTIST};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = Media.ARTIST + " ASC";
        Cursor cursor = contentResolver.query(Artists.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        cursor.moveToFirst();
        String notLike = "";
        String [] artistsArgs = null;
        if(cursor.getCount()>0) {
            artistsArgs = new String[cursor.getCount()];
            int i = 0;
            while (!cursor.isAfterLast()) {
                String thumbnail = null;
                String picture = null;
                File f2 = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor.getString(1) + ".jpg");
                if (f2.exists())
                    picture = f2.getAbsolutePath();
                File f = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor.getString(1) + "_small.jpg");
                if (f.exists())
                    thumbnail = f.getAbsolutePath();
                else
                    thumbnail = picture;
                ArtistItem artist = new ArtistItem(cursor.getString(1), thumbnail,picture, cursor.getLong(0));
                if (notLike.length() != 0)
                    notLike += ", ";
                else
                    notLike += "LOWER("+SQLiteHelper.COLUMN_ARTIST+ " )NOT IN (";
                notLike += "?";
                artistsArgs[i] = cursor.getString(1).toLowerCase();
                mArtists.add(artist);
                cursor.moveToNext();
                i++;
            }
            if(notLike.length() != 0)
                notLike+=")";
        }
        cursor.close();
        open();
        if(!hideRemote) {
            cursor = database.rawQuery("SELECT DISTINCT(LOWER(" + SQLiteHelper.COLUMN_ARTIST + ")),+" + SQLiteHelper.COLUMN_ARTIST + " FROM " + SQLiteHelper.TABLE_MUSIC + (artistsArgs != null ? " WHERE " + buildWhere(notLike) : ""), artistsArgs);


            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String thumbnail = null;
                String picture = null;
                File f2 = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor.getString(1) + ".jpg");
                if (f2.exists())
                    picture = f2.getAbsolutePath();
                File f = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor.getString(1) + "_small.jpg");
                if (f.exists())
                    thumbnail = f.getAbsolutePath();
                else
                    thumbnail = picture;
                ArtistItem artist = new ArtistItem(cursor.getString(1), thumbnail, picture, -1);
                mArtists.add(artist);
                cursor.moveToNext();
            }
            cursor.close();

        }
        close();
        Collections.sort(mArtists);

    }
    public List<? extends Item> searchAlbums(String query, boolean hideRemote) {

        // TODO Auto-generated method stub
        List<AlbumItem> comments = new ArrayList<AlbumItem>();
        String[] tab = null;
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        boolean grabLocal = true;

        String where = Media.ALBUM + " LIKE ?";
        String[] whereArgs = new String[]{"%" + query + "%"};

        String notLike = SQLiteHelper.COLUMN_ALBUM + " LIKE ?";
        tab = new String[]{"%" + query + "%"};
        ContentResolver contentResolver = context.getContentResolver();

        String [] artistsArgs = null;
        Cursor cursor2;
        boolean hasSet = false;
        Log.d("searchdebug", "entering");
        if(grabLocal) {
            String[] projection = new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_ART, Artists._ID};
            cursor2 = contentResolver.query(uri, projection, where,
                    whereArgs, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
            cursor2.moveToFirst();
            artistsArgs = new String[cursor2.getCount()+(tab!=null?tab.length:0)];
            if(tab!=null)
                artistsArgs[0] = tab[0];

            int i = tab!=null?1:0;
            while (!cursor2.isAfterLast()) {
                String thumbnail = null;
                String picture = null;
                if (cursor2.getString(3) != null && cursor2.getString(3).length() != 0) {
                    thumbnail = cursor2.getString(3);
                    picture = thumbnail;
                }
                else {
                    File f2 = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor2.getString(1) + "/" + cursor2.getString(2) + ".jpg");
                    if (f2.exists())
                        picture = f2.getAbsolutePath();
                    File f = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor2.getString(1) + "/" + cursor2.getString(2) + "_small.jpg");
                    if (f.exists())
                        thumbnail = f.getAbsolutePath();
                    else
                        thumbnail = picture;


                }


                AlbumItem comment = new AlbumItem(null,cursor2.getString(1), cursor2.getString(2), picture,thumbnail, cursor2.getLong(0));
                comments.add(comment);
                if (hasSet) {
                    notLike += ", ";

                }
                else {
                    if(notLike.length() != 0)
                        notLike+=" AND ";
                    notLike += SQLiteHelper.COLUMN_ALBUM + " NOT IN (";
                    hasSet=true;
                }
                notLike += "?";
                artistsArgs[i] = cursor2.getString(2);

                cursor2.moveToNext();
                i++;
            }
            if(hasSet)
                notLike+=")";
            cursor2.close();
        }
        else{
            if(tab!=null) {
                artistsArgs = new String[tab.length];
                artistsArgs[0] = tab[0];
            }
        }
        if(!hideRemote) {
            open();
            cursor2 = database.rawQuery("SELECT DISTINCT(" + SQLiteHelper.COLUMN_ALBUM + "), " + SQLiteHelper.COLUMN_ARTIST + " FROM " + SQLiteHelper.TABLE_MUSIC + (notLike != null && notLike.length() != 0 ? " WHERE "+notLike : ""), artistsArgs);

            cursor2.moveToFirst();
            while (!cursor2.isAfterLast()) {
                String thumbnail = null;
                String picture = null;

                File f = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor2.getString(1) + "/" + cursor2.getString(0) + ".jpg");
                if (f.exists())
                    picture = f.getAbsolutePath();
                File f2 = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor2.getString(1) + "/" + cursor2.getString(0) + "_small.jpg");
                if (f2.exists())
                    thumbnail = f2.getAbsolutePath();
                else
                    thumbnail = picture;
                AlbumItem albumItem = new AlbumItem(null, cursor2.getString(1), cursor2.getString(0), picture, thumbnail, -1);
                comments.add(albumItem);
                cursor2.moveToNext();
            }
            cursor2.close();
            close();
        }

        Collections.sort(comments);

        return comments;

    }
    public List<AlbumItem> getAllAlbums(ArtistItem artist, boolean hideRemote) {
        // TODO Auto-generated method stub
        List<AlbumItem> comments = new ArrayList<AlbumItem>();
        String notLike = "";
        String[] tab = null;
        Uri uri = null;

        boolean grabLocal = false;
        if(artist!=null&&artist.getId()>=0){
            Uri.Builder builder = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI.buildUpon();
            builder.appendPath(""+artist.getId());
            builder.appendPath("albums");
            uri = builder.build();
            grabLocal = true;
            if(artist.getName()!=null&&artist.getName().length()!=0){
                notLike += SQLiteHelper.COLUMN_ARTIST+" = ?";
                tab = new String[]{artist.getName()};
            }

        }else if(artist!=null&&artist.getName()!=null&&artist.getName().length()!=0){
            notLike += SQLiteHelper.COLUMN_ARTIST+" = ?";
            tab = new String[]{artist.getName()};
        }
        else {
            uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            grabLocal = true;
        }
        ContentResolver contentResolver = context.getContentResolver();

        String [] artistsArgs = null;
        Cursor cursor2;
        boolean hasSet = false;
        if(grabLocal) {
            String[] projection = new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_ART, Artists._ID};
            cursor2 = contentResolver.query(uri, projection, null,
                    null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
            if(cursor2!=null) {
                Log.d(TAG, "found "+cursor2.getCount()+" albums");
                cursor2.moveToFirst();

                artistsArgs = new String[cursor2.getCount() + (tab != null ? tab.length : 0)];
                if (tab != null)
                    artistsArgs[0] = tab[0];

                int i = tab != null ? 1 : 0;
                while (!cursor2.isAfterLast()) {
                    String thumbnail = null;
                    String picture = null;
                    if (cursor2.getString(3) != null && cursor2.getString(3).length() != 0) {
                        thumbnail = cursor2.getString(3);
                        picture = thumbnail;
                    } else {
                        File f2 = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor2.getString(1) + "/" + cursor2.getString(2) + ".jpg");
                        if (f2.exists())
                            picture = f2.getAbsolutePath();
                        File f = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor2.getString(1) + "/" + cursor2.getString(2) + "_small.jpg");
                        if (f.exists())
                            thumbnail = f.getAbsolutePath();
                        else
                            thumbnail = picture;


                    }

                    Log.d(TAG, "adding "+cursor2.getString(2));

                    AlbumItem comment = new AlbumItem(artist, cursor2.getString(1), cursor2.getString(2), picture, thumbnail, cursor2.getLong(0));
                    comments.add(comment);
                    if (hasSet) {
                        notLike += ", ";

                    } else {
                        if (notLike.length() != 0)
                            notLike += " AND ";
                        notLike += SQLiteHelper.COLUMN_ALBUM + " NOT IN (";
                        hasSet = true;
                    }
                    notLike += "?";
                    artistsArgs[i] = cursor2.getString(2);

                    cursor2.moveToNext();
                    i++;
                }
                if (notLike.length() != 0)
                    notLike += ")";
                cursor2.close();
            }
            else if(tab!=null) {
                artistsArgs = new String[tab.length];
                artistsArgs[0] = tab[0];
            }
        }
        else{
            if(tab!=null) {
                artistsArgs = new String[tab.length];
                artistsArgs[0] = tab[0];
            }
        }
        if(!hideRemote) {
            open();
            cursor2 = database.rawQuery("SELECT DISTINCT(" + SQLiteHelper.COLUMN_ALBUM + "), " + SQLiteHelper.COLUMN_ARTIST + " FROM " + SQLiteHelper.TABLE_MUSIC + (notLike != null && notLike.length() != 0 ? " WHERE " + buildWhere(notLike) : buildWhere(null)), artistsArgs);

            cursor2.moveToFirst();
            while (!cursor2.isAfterLast()) {
                String thumbnail = null;
                String picture = null;

                File f = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor2.getString(1) + "/" + cursor2.getString(0) + ".jpg");
                if (f.exists())
                    picture = f.getAbsolutePath();
                File f2 = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor2.getString(1) + "/" + cursor2.getString(0) + "_small.jpg");
                if (f2.exists())
                    thumbnail = f2.getAbsolutePath();
                else
                    thumbnail = picture;
                AlbumItem albumItem = new AlbumItem(artist, cursor2.getString(1), cursor2.getString(0), picture, thumbnail, -1);
                comments.add(albumItem);
                cursor2.moveToNext();
            }
            cursor2.close();
            close();
        }

        Collections.sort(comments);
        return comments;

    }

    public List<? extends Item> searchMusics(String query, boolean hideRemote) {
        List<MusicItem> comments = new ArrayList<MusicItem>();
        String[] projection = new String[] {Media.DATA, Media.TITLE, Media.TRACK,Media.ARTIST, Media.ALBUM};
        String where = null;
        String [] whereArgs =null;
        String whereBis = "";
        HashMap<String,ArrayList<String>> whereArgsBisArray = new HashMap<>();
        ArrayList<String> whereArgsBis = new ArrayList<>();
        ArrayList<String> whereBisArray = new ArrayList<>();
        int currentWhere = 0;
        boolean lookLocal = false;
        lookLocal = true;
        where = Media.TITLE + " LIKE ?";
        whereArgs = new String[]{"%"+query+"%" };

        whereBis = " WHERE "+ SQLiteHelper.COLUMN_MUSIC+" LIKE ?";
        whereArgsBis.add("%" + query +"%");
        whereBisArray.add(whereBis);

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor3;
        if(lookLocal){
            cursor3 = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,where ,whereArgs
                    , MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            cursor3.moveToFirst();
            boolean first = true;
            int current = 0;
            if(cursor3.getCount()>0){
                ArrayList<String>currentArrayArg = new ArrayList<>();
                currentArrayArg.addAll(whereArgsBis);
                while (!cursor3.isAfterLast()) {
                    MusicItem comment = new MusicItem(cursor3.getString(0),null, null, cursor3.getString(1), cursor3.getInt(2), 0);
                    comments.add(comment);
                    if(whereBisArray.get(currentWhere).length() == 0){
                        whereBisArray.set(currentWhere, " WHERE ");
                    }
                    else if(first){
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + " AND ");
                    }
                    if(first){
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + " " + SQLiteHelper.COLUMN_MUSIC + " NOT IN (");
                        first=false;
                    }
                    else
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + ", ");
                    whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + "?");
                    currentArrayArg.add(cursor3.getString(1));

                    cursor3.moveToNext();
                    if(current>10&&!cursor3.isAfterLast()){
                        first = true;
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + ")");
                        whereArgsBisArray.put(whereBisArray.get(currentWhere), currentArrayArg);
                        currentWhere ++;
                        currentArrayArg = new ArrayList<>();
                        currentArrayArg.addAll(whereArgsBis);
                        String wheretmp = new String(whereBis);
                        whereBisArray.add(wheretmp);

                        current = 0;
                    }
                    current++;
                }
                whereBisArray.set(currentWhere,whereBisArray.get(currentWhere)+")");
                whereArgsBisArray.put(whereBisArray.get(currentWhere), currentArrayArg);


            }else{
                whereArgsBisArray.put(whereBis,whereArgsBis);
            }
            cursor3.close();
        }
        else{
            whereArgsBisArray.put(whereBis,whereArgsBis);
        }

        if(!hideRemote) {
            open();

            for (String whereBis2 : whereBisArray) {
                cursor3 = database.rawQuery("SELECT DISTINCT(" + SQLiteHelper.COLUMN_MUSIC + "), " + SQLiteHelper.COLUMN_TRACK + ", " + SQLiteHelper.COLUMN_PATH + ", " + SQLiteHelper.COLUMN_ACCESS_ID + " FROM " +
                        SQLiteHelper.TABLE_MUSIC + buildWhere(whereBis2), whereArgsBisArray.get(whereBis2).toArray(new String[0]));

                cursor3.moveToFirst();
                while (!cursor3.isAfterLast()) {

                    MusicItem albumItem = new MusicItem("distant://" + cursor3.getLong(3) + "/" + cursor3.getString(2), null, null, cursor3.getString(0), cursor3.getInt(1), cursor3.getLong(3) > 0 ? MediaPlayerFactory.TYPE_HUBIC : MediaPlayerFactory.TYPE_DEEZER);
                    comments.add(albumItem);
                    cursor3.moveToNext();
                }
                cursor3.close();
            }
            close();
        }
        Collections.sort(comments);
        return comments;


    }

    public List<? extends Item> getAllMusics(ArtistItem artist, AlbumItem album, boolean hideRemote) {
        List<MusicItem> comments = new ArrayList<MusicItem>();
        String[] projection = new String[] {Media.DATA, Media.TITLE, Media.TRACK};
        String where = null;
        String [] whereArgs =null;
        String whereBis = "";
        HashMap<String,ArrayList<String>> whereArgsBisArray = new HashMap<>();
        ArrayList<String> whereArgsBis = new ArrayList<>();
        ArrayList<String> whereBisArray = new ArrayList<>();
        int currentWhere = 0;
        boolean lookLocal = false;
        if(album!=null) {
            if(album.getId()>-1) {
                lookLocal = true;
                where = Media.ALBUM_ID + " = ?";
                whereArgs = new String[]{album.getId() + ""};
            }
            whereBis = " WHERE "+ SQLiteHelper.COLUMN_ALBUM+"= ?";
            whereArgsBis.add(album.getName());
        }
        else if(artist!=null){
            if(artist.getId()>=0) {
                lookLocal = true;
                where = Media.ARTIST_ID + " = ?";
                whereArgs = new String[]{artist.getId() + ""};
            }
            if(whereBis.length() == 0){
                whereBis = " WHERE ";
            }
            else {
                whereBis += " AND ";
            }
            whereBis += SQLiteHelper.COLUMN_ARTIST+"= ?";
            whereArgsBis.add(artist.getName());
        }
        whereBisArray.add(whereBis);

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor3;
        if(lookLocal){
            cursor3 = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,where ,whereArgs
                    , MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            cursor3.moveToFirst();
            boolean first = true;
            int current = 0;
            if(cursor3.getCount()>0){
                ArrayList<String>currentArrayArg = new ArrayList<>();
                currentArrayArg.addAll(whereArgsBis);
                while (!cursor3.isAfterLast()) {
                    MusicItem music = new MusicItem(cursor3.getString(0),artist, album, cursor3.getString(1), cursor3.getInt(2), 0);
                    comments.add(music);
                    Log.d("musicdebug","music "+music.getPath());
                    if(whereBisArray.get(currentWhere).length() == 0){
                        whereBisArray.set(currentWhere, " WHERE ");
                    }
                    else if(first){
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + " AND ");
                    }
                    if(first){
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + " " + SQLiteHelper.COLUMN_MUSIC + " NOT IN (");
                        first=false;
                    }
                    else
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + ", ");
                    whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + "?");
                    currentArrayArg.add(cursor3.getString(1));

                    cursor3.moveToNext();
                    if(current>10&&!cursor3.isAfterLast()){
                        first = true;
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + ")");
                        whereArgsBisArray.put(whereBisArray.get(currentWhere), currentArrayArg);
                        currentWhere ++;
                        currentArrayArg = new ArrayList<>();
                        currentArrayArg.addAll(whereArgsBis);
                        String wheretmp = new String(whereBis);
                        whereBisArray.add(wheretmp);

                        current = 0;
                    }
                    current++;
                }
                whereBisArray.set(currentWhere,whereBisArray.get(currentWhere)+")");
                whereArgsBisArray.put(whereBisArray.get(currentWhere), currentArrayArg);


            }else{
                whereArgsBisArray.put(whereBis,whereArgsBis);
            }
            cursor3.close();
        }
        else{
            whereArgsBisArray.put(whereBis,whereArgsBis);
        }

        if(!hideRemote) {
            open();

            for (String whereBis2 : whereBisArray) {
                cursor3 = database.rawQuery("SELECT DISTINCT(" + SQLiteHelper.COLUMN_MUSIC + "), " + SQLiteHelper.COLUMN_TRACK + ", " + SQLiteHelper.COLUMN_PATH + ", " + SQLiteHelper.COLUMN_ACCESS_ID + " FROM " +
                        SQLiteHelper.TABLE_MUSIC + buildWhere(whereBis2), whereArgsBisArray.get(whereBis2).toArray(new String[0]));

                cursor3.moveToFirst();
                while (!cursor3.isAfterLast()) {

                    MusicItem albumItem = new MusicItem(getPathFromCursor(cursor3), artist, album, cursor3.getString(0), cursor3.getInt(1), cursor3.getLong(3) > 0 ? MediaPlayerFactory.TYPE_HUBIC : MediaPlayerFactory.TYPE_DEEZER);
                    comments.add(albumItem);
                    cursor3.moveToNext();
                }
                cursor3.close();
            }
            close();
        }
        Collections.sort(comments);
        return comments;


    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public ArrayList<String> getListPath(HubicAccess access) {
        return null;
    }

    public void close() {
        if(database!=null)
            database.close();
    }

    public void delete(Long id, String path) {

    }
    public MusicItem getMusicItem(long id, String s) {
        open();
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();
        sb.append(SQLiteHelper.COLUMN_ACCESS_ID + " = ?");
        sb.append(" AND "+SQLiteHelper.COLUMN_PATH + " = ?");
        String where = sb.toString();
        String[] args =  new String[]{
                id+"",
                s
        };
        Cursor cursor3 = database.rawQuery("SELECT * FROM " +
                SQLiteHelper.TABLE_MUSIC +" WHERE "+where,args);
        if(cursor3!=null&&cursor3.getCount()>0){
            cursor3.moveToFirst();
            do{
                return  cursorToMusic(cursor3);

            }while(cursor3.moveToNext());
        }
        return null;
    }
    public MusicItem getMusicItem(String s) {
        open();
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();

        sb.append(SQLiteHelper.COLUMN_PATH + " = ?");
        String where = sb.toString();
        String[] args =  new String[]{
                s
        };
        Cursor cursor3 = database.rawQuery("SELECT * FROM " +
                SQLiteHelper.TABLE_MUSIC +" WHERE "+where,args);
        if(cursor3!=null&&cursor3.getCount()>0){
            cursor3.moveToFirst();
            do{
                return  cursorToMusic(cursor3);

            }while(cursor3.moveToNext());
        }
        return null;
    }
    public void deleteAccount(long id){
        database.delete(SQLiteHelper.TABLE_MUSIC, SQLiteHelper.COLUMN_ACCESS_ID + " = ?", new String[]{id + ""});

    }
    public void addMusic(Long id, String path, String artist, String album, String title, String track) {
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_PATH, path);
        values.put(SQLiteHelper.COLUMN_ACCESS_ID, id + "");
        values.put(SQLiteHelper.COLUMN_ARTIST , artist);
        values.put(SQLiteHelper.COLUMN_ALBUM , album);
        values.put(SQLiteHelper.COLUMN_MUSIC, title);
        values.put(SQLiteHelper.COLUMN_TRACK, track);
        values.put(SQLiteHelper.COLUMN_PRIORITY, 0);
        try{
            long insertId = database.insertWithOnConflict(SQLiteHelper.TABLE_MUSIC, null,
                    values,database.CONFLICT_REPLACE);
        }catch(Exception e){
            try {
                Thread.sleep(200);
                addMusic(id, path, artist, album, title, track);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        }
    }


    public List<? extends Item> searchArtists(String query, boolean hideRemote) {

        List<ArtistItem> mArtists = new ArrayList<ArtistItem>();
        // make sure to close the cursor
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = new String[] { Artists._ID, Artists.ARTIST};
        String selection = Media.ARTIST +" LIKE ?";
        String[] selectionArgs = new String[]{"%"+query+"%"};

        String selection2 = " WHERE "+SQLiteHelper.COLUMN_ARTIST+" LIKE ?";
        String[] selectionArgs1 = new String[]{"%"+query+"%"};
        String sortOrder = Media.ARTIST + " ASC";
        Cursor cursor = contentResolver.query(Artists.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        cursor.moveToFirst();
        String notLike = "";
        String [] artistsArgs = null;
        if(cursor.getCount()>0) {
            artistsArgs = new String[cursor.getCount()+1];
            artistsArgs[0]="%"+query+"%";
            int i = 1;
            while (!cursor.isAfterLast()) {
                String thumbnail = null;
                String picture = null;
                File f2 = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor.getString(1) + ".jpg");
                if (f2.exists())
                    picture = f2.getAbsolutePath();
                File f = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor.getString(1) + "_small.jpg");
                if (f.exists())
                    thumbnail = f.getAbsolutePath();
                else
                    thumbnail = picture;
                ArtistItem artist = new ArtistItem(cursor.getString(1), thumbnail,picture, cursor.getLong(0));
                if (notLike.length() != 0)
                    notLike += ", ";
                else
                    notLike += SQLiteHelper.COLUMN_ARTIST+ " NOT IN (";
                notLike += "?";
                artistsArgs[i] = cursor.getString(1);
                mArtists.add(artist);
                cursor.moveToNext();
                i++;
            }
            if(notLike.length() != 0)
                notLike+=")";
        }
        cursor.close();
        open();
        if(!hideRemote) {
            cursor = database.rawQuery("SELECT DISTINCT(LOWER(" + SQLiteHelper.COLUMN_ARTIST + "))," + SQLiteHelper.COLUMN_ARTIST + " FROM " + SQLiteHelper.TABLE_MUSIC +buildWhere(selection2+(artistsArgs != null ? " AND " + notLike : "")), artistsArgs!=null?artistsArgs:selectionArgs1);


            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String thumbnail = null;
                String picture = null;
                File f2 = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor.getString(1) + ".jpg");
                if (f2.exists())
                    picture = f2.getAbsolutePath();
                File f = new File(Environment.getExternalStorageDirectory() + "/hubicmusic/" + cursor.getString(1) + "_small.jpg");
                if (f.exists())
                    thumbnail = f.getAbsolutePath();
                else
                    thumbnail = picture;
                ArtistItem artist = new ArtistItem(cursor.getString(1), thumbnail, picture, -1);
                mArtists.add(artist);
                cursor.moveToNext();
            }
            cursor.close();

        }
        close();
        Collections.sort(mArtists);
        return mArtists;
    }





















    public long addPlaylist(String name, int type, Uri uri, long modificationDate) {
        if(uri == null)
            uri = Uri.parse("");
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_NAME, name);
        values.put(SQLiteHelper.COLUMN_TYPE, type+"");
        values.put(SQLiteHelper.COLUMN_URI, uri.toString());
        values.put(SQLiteHelper.COLUMN_MODIFICATION_DATE, modificationDate);

        try{
            long insertId = database.insertWithOnConflict(SQLiteHelper.TABLE_PLAYLIST, null,
                    values,database.CONFLICT_REPLACE);
            return insertId;
        }catch(Exception e){
            e.printStackTrace();
            try {
                Thread.sleep(200);
                addPlaylist(name, type, null, System.currentTimeMillis());
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        }
        return -1;
    }
    public void addToPlaylist(long accessID, String path, long playlistID){

        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_ACCESS_ID, accessID+"");
        values.put(SQLiteHelper.COLUMN_PLAYLIST_MUSIC_PATH, path);
        values.put(SQLiteHelper.COLUMN_PLAYLIST_ID, ""+playlistID);
        try{
             database.insertWithOnConflict(SQLiteHelper.TABLE_PLAYLIST_MUSIC, null,
                     values, database.CONFLICT_REPLACE);
        }catch(Exception e){
            try {
                Thread.sleep(200);
                addToPlaylist(accessID, path, playlistID);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        }
    }
    public void removeFromPlaylist(long accessID, String path, long playlistID) {
        String where = SQLiteHelper.COLUMN_ACCESS_ID + "= ? AND "
                + SQLiteHelper.COLUMN_PLAYLIST_MUSIC_PATH + " = ? AND " + SQLiteHelper.COLUMN_PLAYLIST_ID + "= ? ";
        String[] values = new String[]{
                accessID + "",
                path,
                "" + playlistID};
        try {
            database.delete(SQLiteHelper.TABLE_PLAYLIST_MUSIC, where,
                    values);
        } catch (Exception e) {
            try {
                Thread.sleep(200);
                removeFromPlaylist(accessID, path, playlistID);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }


        }
    }

    public List<PlaylistItem> getAllPlaylists(boolean only_local_pref) {
        refreshLocalPlaylists();
        open();
        Cursor cursor = database.rawQuery("SELECT * FROM " + SQLiteHelper.TABLE_PLAYLIST , null);

        List<PlaylistItem> list = new ArrayList<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                PlaylistItem artist = new PlaylistItem(cursor.getString(1), null, null, cursor.getLong(0), Uri.parse(cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_URI))), cursor.getLong(cursor.getColumnIndex(SQLiteHelper.COLUMN_MODIFICATION_DATE)), cursor.getInt(cursor.getColumnIndex(SQLiteHelper.COLUMN_TYPE)));
                list.add(artist);
                cursor.moveToNext();
            }
            cursor.close();
            close();

        Collections.sort(list);
        return list;
    }

    private void refreshLocalPlaylists() {

        String[] projection = new String[]{MediaStore.Audio.PlaylistsColumns.NAME};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        cursor.moveToFirst();
        Log.d("PlaylistDebug", "count " + cursor.getCount());
        if (cursor.getCount() > 0) {
            while (!cursor.isAfterLast()) {


                Log.d("PlaylistDebug","new playlist: "+cursor.getString(cursor.getColumnIndex(MediaStore.Audio.PlaylistsColumns.NAME)));
                cursor.moveToNext();
            }

        }
        cursor.close();
    }

    public List<MusicItem> getAllMusicsFromPlaylist(PlaylistItem playlistItem, boolean only_local_pref, int page, String order) {

        String [] where = new String[]{playlistItem.getId()+""};
        List<MusicItem> mis = new ArrayList<>();
        open();
        Cursor cursor = database.rawQuery("SELECT * FROM " + SQLiteHelper.TABLE_PLAYLIST_MUSIC+" WHERE "+SQLiteHelper.COLUMN_PLAYLIST_ID+"=? "+(order != null?" ORDER BY "+order:"")+(page >=0? " LIMIT "+(page*20)+", "+(20):""), where);
        cursor.moveToFirst();

        int accessColumn = cursor.getColumnIndex(SQLiteHelper.COLUMN_ACCESS_ID);
        int pathColumn = cursor.getColumnIndex(SQLiteHelper.COLUMN_PLAYLIST_MUSIC_PATH);
        ArrayList <String> local = new ArrayList<>();
        String localQuery="";
        ArrayList<String> distant = new ArrayList<>();
        String distantQuery="";
        List<String> ordered = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            int access = cursor.getInt(accessColumn);
            String path = cursor.getString(pathColumn);
            if(access>0){ //hubic
                if(distantQuery.length() > 0)
                    distantQuery+=" OR ";
                distantQuery+=" "+SQLiteHelper.COLUMN_ACCESS_ID+"= ? AND "+SQLiteHelper.COLUMN_PATH+"= ?";
                distant.add(access+"");
                distant.add(path);
                ordered.add(getPathFromParams(access, path));
            }
            else if(access == -MediaPlayerFactory.TYPE_DEEZER||access == -MediaPlayerFactory.TYPE_NEW){

                if(distantQuery.length() != 0)
                    distantQuery+=" OR ";
                distantQuery+=" "+SQLiteHelper.COLUMN_ACCESS_ID+"= ? AND "+SQLiteHelper.COLUMN_PATH+"= ?";
                distant.add(access+"");
                distant.add(path);
                ordered.add(getPathFromParams(access, path));


            }
            else if(access == - MediaPlayerFactory.TYPE_LOCAL){
                if(localQuery.length() != 0)
                    localQuery+=" OR ";
                localQuery+=" "+Media.DATA+"= ?";
                local.add(path);
                ordered.add("local://" + access + "/" + path);

            }

            cursor.moveToNext();

        }
        cursor.close();

        ArrayList<MusicItem> toReturn = new ArrayList<>();
        HashMap<String,MusicItem> tmp = new HashMap<>();
        if(localQuery.length() != 0) {
            String[] projection = new String[]{Media.DATA, Media.TITLE, Media.TRACK, Media.ARTIST, Media.ALBUM};
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor3 = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, localQuery, local.toArray(new String[]{})
                    , MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            cursor3.moveToFirst();

            if (cursor3.getCount() > 0) {
                while (!cursor3.isAfterLast()) {


                    MusicItem comment = new MusicItem(cursor3.getString(0), cursor3.getString(3), cursor3.getString(4), cursor3.getString(1), cursor3.getInt(2), MediaPlayerFactory.TYPE_LOCAL,1);
                    tmp.put("local://" +- MediaPlayerFactory.TYPE_LOCAL + "/" + cursor3.getString(0),comment);
                    cursor3.moveToNext();
                }

            }
            cursor3.close();

        }

        if(distantQuery.length() != 0) {
            Cursor cursor3 = database.rawQuery("SELECT DISTINCT(" + SQLiteHelper.COLUMN_MUSIC + "), " + SQLiteHelper.COLUMN_TRACK + ", "+ SQLiteHelper.COLUMN_ARTIST + ", "+ SQLiteHelper.COLUMN_ALBUM + ", " + SQLiteHelper.COLUMN_PATH + ", " + SQLiteHelper.COLUMN_ACCESS_ID + " FROM " +
                    SQLiteHelper.TABLE_MUSIC + " WHERE "+buildWhere(distantQuery),distant.toArray(new String[]{}));

            cursor3.moveToFirst();
            if(cursor3.getCount()>0) {
                while (!cursor3.isAfterLast()) {
                    MusicItem albumItem = cursorToMusic(cursor3);
                   // MusicItem albumItem = new MusicItem("distant://" + cursor3.getLong(3) + "/" + cursor3.getString(2), null, null, cursor3.getString(0), cursor3.getInt(1), cursor3.getLong(3) > 0 ? MediaPlayerFactory.TYPE_HUBIC : MediaPlayerFactory.TYPE_DEEZER,1);
                    tmp.put(albumItem.getPath(),albumItem);

                    cursor3.moveToNext();
                }
            }
            cursor3.close();


        }
        //ordering
        for (String str : ordered){
            if(!toReturn.contains(tmp.get(str))&&tmp.containsKey(str))
                toReturn.add(tmp.get(str));
        }
        close();
        return toReturn;
    }

    private String buildWhere(String localQuery) {


        if(localQuery==null||localQuery.length()==0){
            return SQLiteHelper.COLUMN_AVAILABILITY +" = 1";
        }
        String whereString = SQLiteHelper.COLUMN_AVAILABILITY +" = 1";
        if(localQuery.startsWith(" WHERE ")) {
            whereString = " WHERE "+whereString;
            localQuery = localQuery.substring(" WHERE ".length());
        }
        Log.d("statedebug","where : "+whereString+" AND ("+localQuery+")");
        return whereString+" AND ("+localQuery+")";
    }

    private String getPathFromCursor(Cursor cursor){
        int accessIDColumn = cursor.getColumnIndex(SQLiteHelper.COLUMN_ACCESS_ID);
        int pathColumn = cursor.getColumnIndex(SQLiteHelper.COLUMN_PATH);
        String path = cursor.getString(pathColumn);
        long accessID = cursor.getLong(accessIDColumn);
        return getPathFromParams(accessID, path);
    }
    private String getPathFromParams(long accessID, String path){

        if(accessID == -MediaPlayerFactory.TYPE_DEEZER)
            path = "deezer://"+path;
        else if(accessID>0)
            path = "hubic://"+accessID+"/"+path;
        return path;
    }
    private MusicItem cursorToMusic(Cursor cursor){

        int artistColumn = cursor.getColumnIndex(SQLiteHelper.COLUMN_ARTIST);
        int albumColumn = cursor.getColumnIndex(SQLiteHelper.COLUMN_ALBUM);
        int titleColumn = cursor.getColumnIndex(SQLiteHelper.COLUMN_MUSIC);
        int trackColumn = cursor.getColumnIndex(SQLiteHelper.COLUMN_TRACK);

        cursor.getString(artistColumn);

        MusicItem musicItem = new MusicItem(getPathFromCursor(cursor), cursor.getString(artistColumn), cursor.getString(albumColumn), cursor.getString(titleColumn), cursor.getInt(trackColumn), MediaPlayerFactory.TYPE_NEW,-1 );
        return musicItem;
    }
    public List<String> getAllMusicPathStartingWithPath(String uri) {
        open();
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();

        sb.append(SQLiteHelper.COLUMN_PATH + " LIKE ?");
        String where = sb.toString();
        String[] args =  new String[]{
                uri + "/%.%" // like
        };
        Cursor cursor3 = database.rawQuery("SELECT * FROM " +
                SQLiteHelper.TABLE_MUSIC +" WHERE "+buildWhere(where),args);
        if(cursor3!=null&&cursor3.getCount()>0){
            cursor3.moveToFirst();
            do{
                Log.d("musicdebug","adding "+getPathFromCursor(cursor3));
                list.add(getPathFromCursor(cursor3));
            }while(cursor3.moveToNext());
        }
        return list;
    }

    public int removeMusicWithPath(String uri) {
        open();
        StringBuilder sb = new StringBuilder();
        sb.append(SQLiteHelper.COLUMN_PATH);
        String where = sb.toString();
        String[] args =  new String[]{
                uri
        };
       return  database.delete(SQLiteHelper.TABLE_MUSIC, where, args);
    }
    public int removeAllMusicPathStartingWithPath(String uri) {
        open();
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();

        sb.append(SQLiteHelper.COLUMN_PATH + " LIKE ?");
        String where = sb.toString();
        String[] args =  new String[]{
                uri + "/%.%" // like
        };

        return database.delete(SQLiteHelper.TABLE_MUSIC, where, args);
    }

    public synchronized void setMusicState(long access, boolean isAvailable) {
        open();
        ContentValues cv = new ContentValues();
        String[] args =null;
        cv.put(SQLiteHelper.COLUMN_AVAILABILITY, isAvailable ? 1 : 0);
        String where = null;
        StringBuilder sb = null;
        Log.d("statedebug","setMusicState "+access);
        if(access!=-1) {
            sb = new StringBuilder();


            sb.append(SQLiteHelper.COLUMN_ACCESS_ID + " = ?");
            where = sb.toString();
            args = new String[]{
                    access +""// like
            };
        }
        int res = database.update(SQLiteHelper.TABLE_MUSIC,cv, where, args);
        Log.d("statedebug","setMusicState "+res);

        close();
    }
    public synchronized void setHubicMusicState(boolean isAvailable) {
        open();
        ContentValues cv = new ContentValues();
        String[] args =null;
        cv.put(SQLiteHelper.COLUMN_AVAILABILITY, isAvailable ? 1 : 0);
        String where = null;
        StringBuilder sb = null;

            sb = new StringBuilder();


            sb.append(SQLiteHelper.COLUMN_ACCESS_ID + " >0 ");
            where = sb.toString();


        int res = database.update(SQLiteHelper.TABLE_MUSIC,cv, where, args);
        Log.d("statedebug","setMusicState "+res);

        close();
    }
    public synchronized void setMusicState(String uri, boolean isAvailable) {
        open();
        ContentValues cv = new ContentValues();
        String[] args =null;
        cv.put(SQLiteHelper.COLUMN_AVAILABILITY, isAvailable ? 1 : 0);
        String where = null;
        StringBuilder sb = null;
        Log.d("statedebug","setMusicState "+uri);
        if(uri!=null) {
            sb = new StringBuilder();


            sb.append(SQLiteHelper.COLUMN_PATH + " LIKE ?");
            where = sb.toString();
            args = new String[]{
                    uri + "/%" // like
            };
        }
        int res = database.update(SQLiteHelper.TABLE_MUSIC,cv, where, args);
        Log.d("statedebug","setMusicState "+res);

        close();
    }

    public void emptyPlaylist(long id) {
        String where = SQLiteHelper.COLUMN_PLAYLIST_ID + "= ? ";
        String[] values = new String[]{

                "" + id};
        try {
            database.delete(SQLiteHelper.TABLE_PLAYLIST_MUSIC, where,
                    values);
        } catch (Exception e) {
            try {
                Thread.sleep(200);
                emptyPlaylist(id);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }


        }
    }

    public void updatePlaylist(PlaylistItem currentPlaylistItem) {

        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_NAME, currentPlaylistItem.getName());
        values.put(SQLiteHelper.COLUMN_TYPE, currentPlaylistItem.getType());
        values.put(SQLiteHelper.COLUMN_URI, currentPlaylistItem.getUri().toString());
        values.put(SQLiteHelper.COLUMN_MODIFICATION_DATE, currentPlaylistItem.getModificationDate());
        String where = SQLiteHelper.COLUMN_ID + " = ?";
        String [] args = new String[]{
                currentPlaylistItem.getId()+""
        };
        try{

            database.update(SQLiteHelper.TABLE_PLAYLIST,values, where, args);
        }catch(Exception e){
            e.printStackTrace();
            try {
                Thread.sleep(200);
                updatePlaylist(currentPlaylistItem);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        }
    }

    public void removePlaylist(long id) {
        Log.d("playlistdebug","removing "+id);

        emptyPlaylist(id);
        String where = SQLiteHelper.COLUMN_ID + " = ?";
        String [] args = new String[]{
                id+""
        };
        try{

            database.delete(SQLiteHelper.TABLE_PLAYLIST, where, args);
        }catch(Exception e){
            e.printStackTrace();
            try {
                Thread.sleep(200);
                removePlaylist(id);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        }
    }
/*
    public List<? extends Item> searchMusics(String query, boolean hideRemote) {
        List<MusicItem> comments = new ArrayList<MusicItem>();
        String[] projection = new String[] {Media.DATA, Media.TITLE, Media.TRACK,Media.ARTIST, Media.ALBUM};
        String where = null;
        String [] whereArgs =null;
        String whereBis = "";
        HashMap<String,ArrayList<String>> whereArgsBisArray = new HashMap<>();
        ArrayList<String> whereArgsBis = new ArrayList<>();
        ArrayList<String> whereBisArray = new ArrayList<>();
        int currentWhere = 0;
        boolean lookLocal = false;
        lookLocal = true;
        where = Media.TITLE + " LIKE ?";
        whereArgs = new String[]{"%"+query+"%" };

        whereBis = " WHERE "+ SQLiteHelper.COLUMN_MUSIC+" LIKE ?";
        whereArgsBis.add("%" + query +"%");
        whereBisArray.add(whereBis);

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor3;
        if(lookLocal){
            cursor3 = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,where ,whereArgs
                    , MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            cursor3.moveToFirst();
            boolean first = true;
            int current = 0;
            if(cursor3.getCount()>0){
                ArrayList<String>currentArrayArg = new ArrayList<>();
                currentArrayArg.addAll(whereArgsBis);
                while (!cursor3.isAfterLast()) {
                    MusicItem comment = new MusicItem(cursor3.getString(0),null, null, cursor3.getString(1), cursor3.getInt(2), 0);
                    comments.add(comment);
                    if(whereBisArray.get(currentWhere).isEmpty()){
                        whereBisArray.set(currentWhere, " WHERE ");
                    }
                    else if(first){
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + " AND ");
                    }
                    if(first){
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + " " + SQLiteHelper.COLUMN_MUSIC + " NOT IN (");
                        first=false;
                    }
                    else
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + ", ");
                    whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + "?");
                    currentArrayArg.add(cursor3.getString(1));

                    cursor3.moveToNext();
                    if(current>10&&!cursor3.isAfterLast()){
                        first = true;
                        whereBisArray.set(currentWhere, whereBisArray.get(currentWhere) + ")");
                        whereArgsBisArray.put(whereBisArray.get(currentWhere), currentArrayArg);
                        currentWhere ++;
                        currentArrayArg = new ArrayList<>();
                        currentArrayArg.addAll(whereArgsBis);
                        String wheretmp = new String(whereBis);
                        whereBisArray.add(wheretmp);

                        current = 0;
                    }
                    current++;
                }
                whereBisArray.set(currentWhere,whereBisArray.get(currentWhere)+")");
                whereArgsBisArray.put(whereBisArray.get(currentWhere), currentArrayArg);


            }else{
                whereArgsBisArray.put(whereBis,whereArgsBis);
            }
            cursor3.close();
        }
        else{
            whereArgsBisArray.put(whereBis,whereArgsBis);
        }

        if(!hideRemote) {
            open();

            for (String whereBis2 : whereBisArray) {
                cursor3 = database.rawQuery("SELECT DISTINCT(" + SQLiteHelper.COLUMN_MUSIC + "), " + SQLiteHelper.COLUMN_TRACK + ", " + SQLiteHelper.COLUMN_PATH + ", " + SQLiteHelper.COLUMN_ACCESS_ID + " FROM " +
                        SQLiteHelper.TABLE_MUSIC + whereBis2, whereArgsBisArray.get(whereBis2).toArray(new String[0]));

                cursor3.moveToFirst();
                while (!cursor3.isAfterLast()) {

                    MusicItem albumItem = new MusicItem("distant://" + cursor3.getLong(3) + "/" + cursor3.getString(2), null, null, cursor3.getString(0), cursor3.getInt(1), cursor3.getLong(3) > 0 ? MediaPlayerFactory.TYPE_HUBIC : MediaPlayerFactory.TYPE_DEEZER);
                    comments.add(albumItem);
                    cursor3.moveToNext();
                }
                cursor3.close();
            }
            close();
        }
        Collections.sort(comments);
        return comments;


    }*/
}
