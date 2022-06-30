package com.spipi.spipimediaplayer.library.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spipi.spipimediaplayer.R;
import com.spipi.spipimediaplayer.library.ShortcutDbAdapter;


public class SmbWorkgroupShortcutAndServerAdapter extends WorkgroupShortcutAndServerAdapter {
    private boolean mDisplayWorkgroupSeparator;
    protected static final int TYPE_WORKGROUP_SEPARATOR = 0;

    public SmbWorkgroupShortcutAndServerAdapter(Context ct) {
        super(ct);
    }

    public void updateWorkgroups(/*List<Workgroup> workgroups*/) {
        // No need to display workgroup if there is only one
       /* mDisplayWorkgroupSeparator = workgroups.size() > 1;
        mShares.clear();
        mAvailableShares.clear();

        for (Workgroup w : workgroups) {
            // Add the actual shares
            for (Share share : w.getShares()) {
                GenericShare s = new GenericShare(share.getDisplayName(), share.getWorkgroup(), share.toUri().toString());
                mShares.add(s);
                mAvailableShares.add(s.getName().toLowerCase());
                mAvailableShares.add(Uri.parse(share.getAddress()).getHost()); // retrieve ip address from smb://ip/
            }
        }*/

        resetData();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mDisplayWorkgroupSeparator", mDisplayWorkgroupSeparator);
    }

    /**
     * Restore the state that has been saved in a bundle
     * @param inState
     */
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceState(Bundle inState) {
        mDisplayWorkgroupSeparator = inState.getBoolean("mDisplayWorkgroupSeparator");
        super.onRestoreInstanceState(inState);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if(viewHolder.getItemViewType() == TYPE_WORKGROUP_SEPARATOR) {
            SeparatorViewHolder wsViewHolder = (SeparatorViewHolder)viewHolder;
            wsViewHolder.setProgressVisible(false);
            wsViewHolder.getNameTextView().setText((String)mData.get(position));
        }
        else
          super.onBindViewHolder(viewHolder, position);

    }
    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_WORKGROUP_SEPARATOR) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_workgroup_separator, viewGroup, false);
            return new SeparatorViewHolder(v);
        }
        else return super.onCreateViewHolder(viewGroup,viewType);
    }
    public void resetData() {
        mData.clear();
        mTypes.clear();
        String lastWorgroup = null;
       // mData.add(Integer.valueOf(R.string.discovered));

       // mTypes.add(TYPE_TITLE);
        for (GenericShare s : mShares) {
            if (mDisplayWorkgroupSeparator && s.getWorkgroup()!=null && !s.getWorkgroup().equals(lastWorgroup)) {
                mData.add(s.getWorkgroup());
                mTypes.add(TYPE_WORKGROUP_SEPARATOR);
            }
            mData.add(s);
            mTypes.add(TYPE_SHARE);
            lastWorgroup = s.getWorkgroup();
        }
        mData.add(Integer.valueOf(R.string.indexed_folders));
        mTypes.add(TYPE_TITLE);
        if (mIndexedShortcuts != null&&mIndexedShortcuts.size()>0) {
            for (ShortcutDbAdapter.Shortcut uri : mIndexedShortcuts) {
                mData.add(uri);
                mTypes.add(TYPE_INDEXED_SHORTCUT);
            }
        }else{
            mData.add(mContext.getString(R.string.empty));
            mTypes.add(TYPE_TEXT);
        }
    }
}
