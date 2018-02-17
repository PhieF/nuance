package com.spipi.spipimediaplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Locale;

/**
 * Created by alexandre on 29/05/15.
 */
public class AlbumListActivity extends TabbedFragment {

    public static final String ARG_ARTIST = "artist";

    @Override
    protected FragmentPagerAdapter getFragmentAdapter() {
        return new SectionsPagerAdapter(getChildFragmentManager());
    }
@Override
protected boolean needTabs(){
    return false;
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

            if(position==0)
                return AlbumsFragment.newInstance((ArtistItem) getArguments().getSerializable(ARG_ARTIST));
            return null;

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
                    return getString(R.string.title_albums).toUpperCase(l);
                case 1:
                    return getString(R.string.title_musics).toUpperCase(l);

            }
            return null;
        }
    }
}
