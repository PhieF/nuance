package com.spipi.spipimediaplayer.library.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.spipi.spipimediaplayer.LibraryUpdater;
import com.spipi.spipimediaplayer.R;
import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.library.NetworkCredentialsDatabase;
import com.spipi.spipimediaplayer.library.ShortcutDbAdapter;


public abstract class NetworkRootFragment extends Fragment implements  WorkgroupShortcutAndServerAdapter.OnShortcutTapListener,  WorkgroupShortcutAndServerAdapter.OnRefreshClickListener {

    private static final String TAG = "NetworkRootFragment";

    private RecyclerView mDiscoveryList;
    private RecyclerView.LayoutManager mLayoutManager;
    protected RootFragmentAdapter mAdapter;
    private Toast mToast;
    private ShortcutDbAdapter.Shortcut mSelectedShortcut;
    // protected QuickAction mQuickAction;
   // private ShortcutDbAdapter.Shortcut mSelectedShortcut;


    @Override
    public void onShortcutTap(Uri uri) {
        // Build root Uri from shortcut Uri
        String rootUriString = uri.getScheme() + "://" + uri.getHost();
        if (uri.getPort() != -1) {
            rootUriString += ":" + uri.getPort();
        }
        rootUriString += "/";// important to end with "/"
        Uri rootUri = Uri.parse(rootUriString);
        Bundle args = new Bundle();
        /*args.putParcelable(BrowserByNetwork.CURRENT_DIRECTORY, uri);
        args.putString(BrowserByNetwork.TITLE
                , uri.getLastPathSegment());
        args.putString(BrowserByNetwork.SHARE_NAME, uri.getLastPathSegment());

        Fragment f;
        if (uri.getScheme().equals("smb")) {
            f = Fragment.instantiate(getActivity(), BrowserBySmb.class.getCanonicalName(), args);
        } else if (uri.getScheme().equals("upnp")) {
            f = Fragment.instantiate(getActivity(), BrowserByUpnp.class.getCanonicalName(), args);
        } else {
            f = Fragment.instantiate(getActivity(), BrowserBySFTP.class.getCanonicalName(), args);
        }
        BrowserCategory category = (BrowserCategory) getActivity().getSupportFragmentManager().findFragmentById(R.id.category);
        category.startContent(f);*/
    }

    @Override
    public void onUnavailableShortcutTap(Uri uri) {
        if (mToast != null) {
            mToast.cancel(); // if we don't do that we have a very long toast in case user press on several shortcuts in row
        }
    }

    @Override
    public void onRefreshClickListener(View v, final Uri uri) {
        // Network shortcut
/*
        mQuickAction = new QuickAction(v);

        ActionItem rescanAction = new ActionItem();
        rescanAction.setTitle(getString(R.string.network_reindex));
        rescanAction.setIcon(getResources().getDrawable(R.drawable.ic_menu_refresh));
        rescanAction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Rescan the contents of the folder
                NetworkScanner.scanVideos(getActivity(), uri);
                if(ShortcutDbAdapter.VIDEO.isShortcut(getActivity(), uri.toString())<0){
                    //if not a shortcut, add as shortcut
                    ShortcutDbAdapter.VIDEO.addShortcut(getActivity(), new ShortcutDbAdapter.Shortcut(uri.getLastPathSegment(), uri.toString()));
                    loadIndexedShortcuts();
                }
                // Close the popup
                mQuickAction.dismiss();
            }
        });
        mQuickAction.addActionItem(rescanAction);
        mQuickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
        mQuickAction.show();
        final View fv = v;
        mQuickAction.setOnDismissListener(new PopupWindow.OnDismissListener() {
            public void onDismiss() {
                fv.invalidate();
                mQuickAction.onClose();
            }
        });*/
    }

    public NetworkRootFragment() {
        Log.d(TAG, "SambaDiscoveryFragment() constructor " + this);
        setRetainInstance(false);

    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        // Adapter need to be instantiate ASAP because setOnShareOpenListener() may be called before onCreateView()
        mAdapter = getAdapter();
        mAdapter.setOnRefreshClickListener(this);
        mAdapter.setOnCreateContextMenuListener(this);
        mAdapter.setOnShortcutTapListener(this);

        //refresh when scan state changes to show or hide "refresh indexing" arrow
        //NetworkScannerServiceVideo.addListener(this);
    }



    protected abstract RootFragmentAdapter getAdapter();


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);


    }

    private void askForCredentials() {
        String tag = ServerCredentialsDialog.class.getCanonicalName();
        ServerCredentialsDialog dialog = (ServerCredentialsDialog)getFragmentManager().findFragmentByTag(tag);
        if (dialog == null) {
            dialog = new ServerCredentialsDialog();

            dialog.show(getFragmentManager(), tag);
        }

        dialog.setOnConnectClickListener(new ServerCredentialsDialog.OnConnectClickListener() {
            @Override
            public void onConnectClick(String username, Uri path, String password) {
                NetworkCredentialsDatabase.getInstance().saveCredential(new NetworkCredentialsDatabase.Credential(username, password, path.toString(),true));
                String shortcutName = path.getLastPathSegment(); //to avoid name like "33" in upnp
                boolean result = ShortcutDbAdapter.getInstance().addShortcut(getActivity(), new ShortcutDbAdapter.Shortcut(shortcutName, path.toString()));

                if (result) {
                    Toast.makeText(getActivity(), getString(R.string.indexed_folder_added, shortcutName), Toast.LENGTH_SHORT).show();
                    loadIndexedShortcuts();
                    LibraryUpdater.sLibraryUpdater.updateDistant();
                }
                else {
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
       if(item.getItemId() == R.string.add_folder){
            askForCredentials();
         return true;
        }
        return false;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, R.string.remove_from_indexed_folders, 0, R.string.remove_from_indexed_folders);

        mSelectedShortcut = ((WorkgroupShortcutAndServerAdapter.ShortcutViewHolder) v.getTag()).getShortcut();
    }
    protected abstract void rescanAvailableShortcuts();
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.string.remove_from_indexed_folders:
                removeShortcut(mSelectedShortcut);
                return true;

        }

        return super.onContextItemSelected(item);
    }
    private void removeShortcut(ShortcutDbAdapter.Shortcut shortcut) {
        // Remove the shortcut from the list
        ShortcutDbAdapter.getInstance().deleteShortcut(getActivity(), shortcut.getUri().toString());
        loadIndexedShortcuts();
        String text = getString(R.string.indexed_folder_removed, shortcut.getName());
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        MusicDatasource mMusicDataSource = new MusicDatasource(getActivity());
        mMusicDataSource.removeAllMusicPathStartingWithPath(shortcut.getUri().toString());
        // Update the menu items
        getActivity().invalidateOptionsMenu();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");

    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mLayoutManager!=null)
            outState.putParcelable("mLayoutManager", mLayoutManager.onSaveInstanceState()); // Save the layout manager state (that's cool we don't even know what it is doing inside!)
        mAdapter.onSaveInstanceState(outState);        // Save the adapter "saved instance" parameters
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.samba_discovery_fragment, container, false);
        v.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForCredentials();
            }
        });
        mDiscoveryList = (RecyclerView)v.findViewById(R.id.discovery_list);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mDiscoveryList.setLayoutManager(mLayoutManager);
        mDiscoveryList.setHasFixedSize(false); // there are separators
        mDiscoveryList.setAdapter(mAdapter);
        mDiscoveryList.setFocusable(false);
        if (savedInstanceState!=null) {
            mAdapter.onRestoreInstanceState(savedInstanceState); // Restore the adapter "saved instance" parameters
            mLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable("mLayoutManager")); // Restore the layout manager state
        }

        loadIndexedShortcuts();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        loadIndexedShortcuts();
    }

    protected abstract void loadIndexedShortcuts();

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

}
