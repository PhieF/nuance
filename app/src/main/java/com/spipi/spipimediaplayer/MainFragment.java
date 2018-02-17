package com.spipi.spipimediaplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Locale;

/**
 * Created by alexandre on 29/05/15.
 */
public class MainFragment extends TabbedFragment {
    private ArtistFragment mArtistFrag;
    private AlbumsFragment mAlbumFrag;

    @Override
    protected FragmentPagerAdapter getFragmentAdapter() {
        return new SectionsPagerAdapter(getChildFragmentManager());
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {


        private PlaylistFragment mPlaylistFrag;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(position==0) {
                if(mArtistFrag == null)
                    mArtistFrag =  ArtistFragment.newInstance("", "");
                return mArtistFrag;

            }

            else if(position==1)  {
                if(mAlbumFrag == null)
                    mAlbumFrag =  AlbumsFragment.newInstance(null);
                return mAlbumFrag;

            }
            else{
                if(mPlaylistFrag == null)
                    mPlaylistFrag =  PlaylistFragment.newInstance();
                return mPlaylistFrag;
            }


        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_artist).toUpperCase(l);
                case 1:
                    return getString(R.string.title_albums).toUpperCase(l);
                case 2:
                    return getString(R.string.playlists).toUpperCase(l);

            }
            return null;
        }
    }
}
