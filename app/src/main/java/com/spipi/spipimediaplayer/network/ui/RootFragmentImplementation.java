package com.spipi.spipimediaplayer.network.ui;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.spipi.spipimediaplayer.network.ShortcutDbAdapter;

/**
 * Created by alexandre on 28/05/15.
 */
public class RootFragmentImplementation extends RootFragment /*implements SambaDiscovery.Listener*/ {
    private static final String TAG = "RootFragmentImplementation";

    public RootFragmentImplementation(){
        super();
    }

    @Override
    protected WorkgroupShortcutAndServerAdapter getAdapter() {
        return new SmbWorkgroupShortcutAndServerAdapter(getActivity());
    }

    @Override
    protected void rescanAvailableShortcuts() {
        Cursor cursor = ShortcutDbAdapter.getInstance().getAllShortcuts(getActivity(), ShortcutDbAdapter.KEY_PATH+" LIKE ?",new String[]{"smb%"});
        int uriIndex = cursor.getColumnIndex(ShortcutDbAdapter.KEY_PATH);
        if (cursor == null) return;
        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            do {
                String path = cursor.getString(uriIndex);
                if (((WorkgroupShortcutAndServerAdapter) mAdapter).getShares().contains(Uri.parse(path).getHost().toLowerCase())) {
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    public void onViewCreated (View v, Bundle saved){
        if (saved!=null) {
            // Restart the discovery if it was running when saving the instance
            if (saved.getBoolean("isRunning")) {
                //mSambaDiscovery.start();
            }
        }
        else {
            // First initialization, start the discovery (if there is connectivity)
                //mSambaDiscovery.start();

        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
         // Remember if the discovery is still running in order to restart it when restoring the fragment
        //outState.putBoolean("isRunning", mSambaDiscovery.isRunning());
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }
    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    @Override
    protected void loadIndexedShortcuts() {
        Cursor cursor = ShortcutDbAdapter.getInstance().getAllShortcuts(getActivity(), null,null);
        mAdapter.updateIndexedShortcuts(cursor);
        mAdapter.notifyDataSetChanged();
    }
}
