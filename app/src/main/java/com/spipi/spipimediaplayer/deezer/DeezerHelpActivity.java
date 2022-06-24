package com.spipi.spipimediaplayer.deezer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.spipi.spipimediaplayer.R;

import java.util.Locale;

public class DeezerHelpActivity extends AppCompatActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deezer_help);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),this);
		
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public void onClickNext(View v){
		DeezerWrapper.connect(getApplication(), this);
	}
	public void onClickIgnore(View v){
	    this.finish();
    }
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		private Activity mActivity;
		public SectionsPagerAdapter(FragmentManager fm, Activity mActivity) {
			super(fm);
			this.mActivity = mActivity;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
		    if(position==0)
		        return PlaceholderFragment.newInstance(
						mActivity.getResources().getDrawable(R.drawable.favorite_help),
						mActivity.getResources().getString(R.string.pref_deezer),
						mActivity.getResources().getString(R.string.help_deezer));
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
				return "";
			
			}
			return null;
		}
		
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		private Drawable d;
		private String title;
		private String content;
		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(Drawable d, String title, String content) {
		    
			PlaceholderFragment fragment = new PlaceholderFragment(d,title,content);
			
			return fragment;
		}
		public PlaceholderFragment() {

		}
		@SuppressLint("ValidFragment")
        public PlaceholderFragment(Drawable d, String title, String content) {
			this.d=d;
			this.title=title;
			this.content=content;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_deezer_help, container,
					false);
			ImageView iv = (ImageView)rootView.findViewById(R.id.imageView1);
			if(iv!=null)
				iv.setImageDrawable(d);
			TextView tv = (TextView)rootView.findViewById(R.id.textView1);
            if(tv!=null)
                tv.setText(title);
                
                
            tv = (TextView)rootView.findViewById(R.id.textView2);
            if(tv!=null)
                tv.setText(content);
			return rootView;
		}
	}

}
