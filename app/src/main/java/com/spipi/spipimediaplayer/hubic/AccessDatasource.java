package com.spipi.spipimediaplayer.hubic;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.spipi.spipimediaplayer.Access;

import java.util.ArrayList;
import java.util.List;

public class AccessDatasource {

	  
	  
	  
		  // Database fields
		  private SQLiteDatabase database;
		  private SQLiteHelper dbHelper;
		public static AccessDatasource sAccessDatasource;
		  private String[] allColumns = { SQLiteHelper.COLUMN_ID,
		      SQLiteHelper.COLUMN_NAME, SQLiteHelper.COLUMN_TYPE,SQLiteHelper.COLUMN_ACCESS_TOKEN, SQLiteHelper.COLUMN_REFRESH_TOKEN, SQLiteHelper.COLUMN_ACCESS_TOKEN_EXPIRATION, SQLiteHelper.COLUMN_OPENSTACK_ACCESS_TOKEN, SQLiteHelper.COLUMN_OPENSTACK_ACCESS_TOKEN_EXPIRATION, SQLiteHelper.COLUMN_OPENSTACK_URL };
		  
		  private Context context;
		  public AccessDatasource(Context context) {
		    dbHelper = new SQLiteHelper(context);
			  sAccessDatasource = this;
		    this.context = context;
		  }

		  public void open() throws SQLException {
		    database = dbHelper.getWritableDatabase();
		  }

		  public void close() {
		    dbHelper.close();
		  }


		  
		  public Access addAccount(Access account) {
		    ContentValues values = new ContentValues();
		    values.put(SQLiteHelper.COLUMN_NAME, account.getName());
		    values.put(SQLiteHelper.COLUMN_TYPE, account.getType());
		    values.put(SQLiteHelper.COLUMN_ACCESS_TOKEN , account.getAccess_token());
		    values.put(SQLiteHelper.COLUMN_REFRESH_TOKEN , account.getRefresh_token());
		    values.put(SQLiteHelper.COLUMN_ACCESS_TOKEN_EXPIRATION  , account.getExpiration());
		    values.put(SQLiteHelper.COLUMN_OPENSTACK_ACCESS_TOKEN , account.getOpenstack_access_token());
		    values.put(SQLiteHelper.COLUMN_OPENSTACK_URL , account.getOpenstack_url());
			System.out.println("addAccount "+account.getOpenstack_url());
		    values.put(SQLiteHelper.COLUMN_OPENSTACK_ACCESS_TOKEN_EXPIRATION, account.getOpenstack_access_token_expiration());
		    Cursor cursor = null;
		    long insertId = 0;
		    if(account.getId()!=null){
		    	cursor = database.query(SQLiteHelper.TABLE_ACCOUNTS,
				        allColumns, SQLiteHelper.COLUMN_ID + " = " + account.getId(), null,
				        null, null, null);
		    	insertId = account.getId();
		    }
		    
		    if(cursor==null || cursor.isAfterLast())
		    	insertId = database.insert(SQLiteHelper.TABLE_ACCOUNTS, null,
				        values);
		   
		    else
		    	database.updateWithOnConflict(SQLiteHelper.TABLE_ACCOUNTS,
				        values,SQLiteHelper.COLUMN_ID + " = " + insertId, null,database.CONFLICT_REPLACE);
		    
		    cursor = database.query(SQLiteHelper.TABLE_ACCOUNTS,
			        allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null,
			        null, null, null);
		    cursor.moveToFirst();
		    Access newAccess = cursorToAccess(cursor);
		    System.out.println("afteraddAccount "+newAccess.getOpenstack_url());
		    cursor.close();
		    return newAccess;
		  }

		  public void deleteComment(HubicAccess acc) {
		    long id = acc.getId();
		    System.out.println("Account deleted with id: " + id);
		    database.delete(SQLiteHelper.TABLE_ACCOUNTS, SQLiteHelper.COLUMN_ID
		        + " = " + id, null);
		  }

		  public List<Access> getAllAccounts() {
			  Log.d("accessdebug", "getting");
		    List<Access> comments = new ArrayList<>();

		    Cursor cursor = database.query(SQLiteHelper.TABLE_ACCOUNTS,
		        allColumns, null, null, null, null, null);

		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		      Access comment = cursorToAccess(cursor);
		      comments.add(comment);
		      cursor.moveToNext();
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return comments;
		  }

			public Access getAccess(String id) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				
				  String where="";
				  String[] tab = null;
				  where = " WHERE "+SQLiteHelper.COLUMN_ID+"= ?";

				  tab = new String[]{id};
					  
				   
				  Cursor cursor  = database.query(SQLiteHelper.TABLE_ACCOUNTS,
					        allColumns, SQLiteHelper.COLUMN_ID + " = " + id, null,
					        null, null, null);
				    cursor.moveToFirst();
				    Access newAccess = cursorToAccess(cursor);
				return newAccess;
				
			}
		  private Access cursorToAccess(Cursor cursor) {
		  SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			String localStorage =sharedPref.getString("pref_local_storage", "/sdcard/hubic/");

			Access acc;
			  if(cursor.getString(2).equals("hubic"))
					  acc = new HubicAccess(cursor.getString(1));
			  else
				  acc = new Access(cursor.getString(1));
		    acc.setLocalStorage(localStorage+acc.getName()+"/");
		    acc.setId(cursor.getLong(0));
		    acc.setType(cursor.getString(2));
		    acc.setAccess_token(cursor.getString(3));
		    acc.setRefresh_token(cursor.getString(4));
		    acc.setExpiration(cursor.getString(5));
		    acc.setOpenstack_access_token(cursor.getString(6));
		    acc.setOpenstack_url(cursor.getString(8));
		    acc.setOpenstack_access_token_expiration(cursor.getString(7));
		    return acc;
		  }
		
}
