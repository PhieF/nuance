package com.spipi.spipimediaplayer.library.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;


/**
 * Created by alexandre on 08/06/15.
 */
public abstract class RootFragment extends NetworkRootFragment implements WorkgroupShortcutAndServerAdapter.OnShareOpenListener{
    public RootFragment(){
        super();

    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        ((WorkgroupShortcutAndServerAdapter)mAdapter).setOnShareOpenListener(this);
    }
    @Override
    public void onShareOpen(WorkgroupShortcutAndServerAdapter.GenericShare share) {
        final Uri uri = Uri.parse(share.getUri());
        Bundle args = new Bundle();
        /*args.putParcelable(BrowserByNetwork.CURRENT_DIRECTORY, uri);
        args.putString(BrowserByNetwork.TITLE, share.getName());
        args.putString(BrowserByNetwork.SHARE_NAME, uri.getLastPathSegment());
        Fragment f;
        if (uri.getScheme().equals("smb")) {
            f = Fragment.instantiate(getActivity(), BrowserBySmb.class.getCanonicalName(), args);
        } else {
            f = Fragment.instantiate(getActivity(), BrowserByUpnp.class.getCanonicalName(), args);
        }
        BrowserCategory category = (BrowserCategory) getActivity().getSupportFragmentManager().findFragmentById(R.id.category);
        category.startContent(f);*/
    }

}
