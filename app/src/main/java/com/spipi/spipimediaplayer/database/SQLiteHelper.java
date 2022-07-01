package com.spipi.spipimediaplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    //refresh_token
    //access_token
    //access_token_expiration
    //openstack_access_token
    //openstack_access_token_expiration
    //name
    //type


    /*Browser:
     *
     * access_id
     * path
     * last_modify_server
     * md5_server
     * md5_local
     *
     *
     *
     */
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ACCESS_TOKEN = "access_token";//
    public static final String COLUMN_REFRESH_TOKEN = "refresh_token";//
    public static final String COLUMN_ACCESS_TOKEN_EXPIRATION = "access_token_expiration";//
    public static final String COLUMN_OPENSTACK_ACCESS_TOKEN = "openstack_access_token";//
    public static final String COLUMN_OPENSTACK_URL = "openstack_url";
    public static final String COLUMN_OPENSTACK_ACCESS_TOKEN_EXPIRATION = "openstack_access_token_expiration";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";


    public static final String TABLE_ARTIST = "musics";
    public static final String TABLE_ALBUM = "musics";
    public static final String TABLE_MUSIC = "musics";
    public static final String COLUMN_ACCESS_ID = "access_id";
    public static final String COLUMN_PATH = "path";//
    public static final String COLUMN_ARTIST = "artist";//
    public static final String COLUMN_ALBUM = "album";//
    public static final String COLUMN_AVAILABILITY = "availability";
    public static final String COLUMN_MUSIC = "music";//
    public static final String COLUMN_TRACK = "track";//
    public static final String COLUMN_PRIORITY = "priority";//
    public static final String COLUMN_MUSIC_ID = "music_id";
    public static final String COLUMN_URI = "uri";

    private static final String DATABASE_NAME = "hubicsync.db";
    public static final int DATABASE_VERSION = 4;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_ACCOUNTS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_NAME
            + " text not null, " + COLUMN_TYPE
            + " text not null," + COLUMN_ACCESS_TOKEN
            + " text not null, " + COLUMN_REFRESH_TOKEN
            + " text not null, " + COLUMN_ACCESS_TOKEN_EXPIRATION
            + " text not null, " + COLUMN_OPENSTACK_ACCESS_TOKEN
            + " text not null, " + COLUMN_OPENSTACK_URL
            + " text not null, " + COLUMN_OPENSTACK_ACCESS_TOKEN_EXPIRATION
            + " text not null);";








	/*

			Playlists

	 */


    public static final String TABLE_PLAYLIST = "playlist";
    public static String COLUMN_MODIFICATION_DATE= "modification_date";
    private static final String DATABASE_CREATE_PLAYLIST = "create table "
            + TABLE_PLAYLIST + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_NAME
            + " text not null, " + COLUMN_TYPE
            + " integer not null, " + COLUMN_URI + " text not null, "+COLUMN_MODIFICATION_DATE+" long not null);";


    public static final String TABLE_PLAYLIST_MUSIC = "playlist_music2";
    public static final String COLUMN_PLAYLIST_ID = "playlist_id";
    public static final String COLUMN_PLAYLIST_MUSIC_PATH = "music_id";
    private static final String DATABASE_CREATE_PLAYLIST_MUSIC_ASSOCIATION = "CREATE TABLE " + TABLE_PLAYLIST_MUSIC + " " +
            "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_PLAYLIST_ID + " integer not null," + COLUMN_ACCESS_ID + " INTEGER NOT NULL, " + COLUMN_PLAYLIST_MUSIC_PATH + " TEXT NOT NULL " +
            ", CONSTRAINT fk_column2 FOREIGN KEY (" + COLUMN_PLAYLIST_ID + ")" +
            " REFERENCES " + TABLE_PLAYLIST + "  (" + COLUMN_ID + ")" +
            "	ON DELETE CASCADE);";


    @Override
    public SQLiteDatabase getWritableDatabase() {
        while (true) {
            try {
                return super.getWritableDatabase();
            } catch (Exception e) {
                System.err.println(e);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
    }

    private static final String DATABASE_CREATE_MUSIC = "create table "
            + TABLE_MUSIC + "(" + COLUMN_PATH
            + " text not null, " + COLUMN_ACCESS_ID
            + " integer not null, " + COLUMN_ARTIST
            + " text not null," + COLUMN_ALBUM
            + " text not null, " + COLUMN_MUSIC
            + " text not null, " + COLUMN_TRACK
            + " integer not null, " + COLUMN_PRIORITY
            + " integer not null," + COLUMN_AVAILABILITY + " integer default(1),"
            + "CONSTRAINT " + COLUMN_MUSIC_ID + " PRIMARY KEY (" + COLUMN_PATH + "," + COLUMN_ACCESS_ID + "));";

    private static final String DATABASE_CREATE_ARTIST = "create table "
            + TABLE_ARTIST + "(" + COLUMN_ARTIST
            + " text primary key not null," + COLUMN_AVAILABILITY
            + " integer not null);";
    private static final String DATABASE_CREATE_ALBUM = "create table "
            + TABLE_ALBUM + "(" + COLUMN_ALBUM
            + " text primary key not null, " + COLUMN_AVAILABILITY
            + " integer not null);";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_MUSIC);
        database.execSQL(DATABASE_CREATE);
        database.execSQL(DATABASE_CREATE_PLAYLIST);
        database.execSQL(DATABASE_CREATE_PLAYLIST_MUSIC_ASSOCIATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            database.execSQL(DATABASE_CREATE_PLAYLIST);
        }
        if (oldVersion < 3)
            database.execSQL(DATABASE_CREATE_PLAYLIST_MUSIC_ASSOCIATION);
        if (oldVersion < 4) {
            database.execSQL("ALTER TABLE " + TABLE_MUSIC + " ADD " + COLUMN_AVAILABILITY + " integer default(1)");
        }
    }

}
