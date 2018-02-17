package com.spipi.spipimediaplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Locale;

/**
 * Created by alexandre on 29/05/15.
 */
public class MusicListActivity extends TabbedFragment {

    public static final String ARG_ARTIST = "artist";
    public static final String ARG_ALBUM = "album";

    @Override
    protected FragmentPagerAdapter getFragmentAdapter() {
        return new SectionsPagerAdapter(getChildFragmentManager());
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

           return MusicFragment.newInstance(getArguments().getString(ARG_ARTIST), getArguments().getString(ARG_ALBUM));

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_musics).toUpperCase(l);

            }
            return null;
        }
    }
}
