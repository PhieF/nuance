package com.spipi.spipimediaplayer.library.ui;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.spipi.spipimediaplayer.library.ShortcutDbAdapter;

/**
 * Created by alexandre on 28/05/15.
 */
public class RootFragmentImplementation extends RootFragment /*implements SambaDiscovery.Listener*/ {
    //private SambaDiscovery mSambaDiscovery;
    private static final String TAG = "RootFragmentImplementation";
   // private AsyncTask<Void, Void, Void> mCheckShortcutAvailabilityTask;

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

        // Instantiate the SMB discovery as soon as we get the activity context
        /*mSambaDiscovery = new SambaDiscovery(activity);
        mSambaDiscovery.setMinimumUpdatePeriodInMs(100);
        */

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
       // mSambaDiscovery.addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        //mSambaDiscovery.removeListener(this);
        /*if(mCheckShortcutAvailabilityTask!=null)
            mCheckShortcutAvailabilityTask.cancel(true);*/
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        //mSambaDiscovery.abort();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
        //mSambaDiscovery.abort();
    }
    /**
     * Start or restart the discovery.
     * Not needed at initialization since the fragment will start it by itself (if there is connectivity)
     */
    public void startDiscovery() {
        //mSambaDiscovery.start();
    }

    // SambaDiscovery.Listener implementation
   // @Override
    public void onDiscoveryStart() {
        ((WorkgroupShortcutAndServerAdapter)mAdapter).setIsLoadingWorkgroups(true);
    }

    // SambaDiscovery.Listener implementation
   // @Override
    public void onDiscoveryEnd() {
        ((WorkgroupShortcutAndServerAdapter)mAdapter).setIsLoadingWorkgroups(false);

    }

    // SambaDiscovery.Listener implementation
   // @Override
    public void onDiscoveryUpdate(/*List<Workgroup> workgroups*/) {
       // ((SmbWorkgroupShortcutAndServerAdapter)mAdapter).updateWorkgroups(workgroups);
        mAdapter.notifyDataSetChanged();
    }

    // SambaDiscovery.Listener implementation
   // @Override
    public void onDiscoveryFatalError() {
        Log.d(TAG, "onDiscoveryFatalError");
        ((WorkgroupShortcutAndServerAdapter)mAdapter).setIsLoadingWorkgroups(false);
    }

/*
    private void checkShortcutAvailability(){

        if(mCheckShortcutAvailabilityTask!=null)
            mCheckShortcutAvailabilityTask.cancel(true);
        mCheckShortcutAvailabilityTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                List<ShortcutDbAdapter.Shortcut> shortcuts = mAdapter.getShortcuts();
                List<String> shares = mAdapter.getAvailableShares();
                List<String> forcedShortcuts = mAdapter.getForcedEnabledShortcuts();
                if(shortcuts==null)
                    return null;
                for (ShortcutDbAdapter.Shortcut shortcut : shortcuts) {
                    Uri uri = Uri.parse(shortcut.getUri());

                        mAdapter.forceShortcutDisplay(shortcut.getUri());


                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();

    }*/

    @Override
    protected void loadIndexedShortcuts() {
        Cursor cursor = ShortcutDbAdapter.getInstance().getAllShortcuts(getActivity(), null,null);
        mAdapter.updateIndexedShortcuts(cursor);
        mAdapter.notifyDataSetChanged();
        //checkShortcutAvailability();
    }
}
