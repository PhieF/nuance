package com.spipi.spipimediaplayer.library.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spipi.spipimediaplayer.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class NetworkActivityFragment extends Fragment {

    public NetworkActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_network, container, false);
    }
}
