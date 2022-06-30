package com.spipi.spipimediaplayer;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.spipi.spipimediaplayer.database.MusicDatasource;
import com.spipi.spipimediaplayer.deezer.DeezerWrapper;
import com.spipi.spipimediaplayer.hubic.AccountActivity;
import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerFactory;
import com.spipi.spipimediaplayer.library.ui.NetworkActivity;


public class SettingsActivity extends PreferenceActivity  {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


	}
	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
		private Preference mDeezerButton;
		@Override
		public void onResume() {
			super.onResume();
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		}

		@Override
		public void onPause() {
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
			Preference button = (Preference)findPreference("accountbutton");
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					//code for what you want it to do   
					Intent intent = new Intent(getActivity(), AccountActivity.class);


					startActivity(intent);
					return true;
				}
			});
			button = (Preference)findPreference("remote_indexed_folders");
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					//code for what you want it to do
					Intent intent = new Intent(getActivity(), NetworkActivity.class);


					startActivity(intent);
					return true;
				}
			});
			/*Preference button2 = (Preference)findPreference("folderbutton");
			button2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					//code for what you want it to do   
					DirectoryChooserDialog directoryChooserDialog = 
							new DirectoryChooserDialog(getActivity(), 
									new DirectoryChooserDialog.ChosenDirectoryListener() 
							{
								@Override
								public void onChosenDir(String chosenDir)
								{
									System.out
									.println(chosenDir.substring(1));

									SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
									Editor editor = sharedPref.edit();
									editor.putString("pref_local_storage", chosenDir+"/");
									editor.commit();


								}
							}); 
					// Toggle new folder button enabling
					directoryChooserDialog.setNewFolderEnabled(true);
					// Load directory chooser dialog for initial 'm_chosenDir' directory.
					// The registered callback will be called upon final directory selection.
					directoryChooserDialog.chooseDirectory("/");

					return true;
				}
			});

			*/

			mDeezerButton = (Preference)findPreference("deezerbutton");
			mDeezerButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					SessionStore sessionStore = new SessionStore();
					DeezerConnect deezerConnect = new DeezerConnect(getActivity(), DeezerWrapper.APP_ID);

					if (sessionStore.restore(deezerConnect, getActivity())) {
						sessionStore.clear(getActivity());
						deezerConnect.logout(getActivity());
						MusicDatasource md = new MusicDatasource(getActivity());
						md.open();
						md.deleteAccount(-MediaPlayerFactory.TYPE_DEEZER);
						md.close();
						Intent i = new Intent();
						i.setAction("LibraryUpdated");
						getActivity().sendBroadcast(i);
						getView().postDelayed(new Runnable() {
							@Override
							public void run() {
								resetDeezerButton();
							}
						},500);

					} else
						DeezerWrapper.connect(getActivity().getApplication(), getActivity());
					return true;
				}

			});
			resetDeezerButton();
		}


		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			resetDeezerButton();
			// TODO Auto-generated method stub
			//((MyApplication) getApplication()).getCoverUpdater().startLoop();




		}

		private void resetDeezerButton() {
			if(mDeezerButton!=null){
				SessionStore sessionStore = new SessionStore();
				DeezerConnect deezerConnect = new DeezerConnect(getActivity(), DeezerWrapper.APP_ID);

				if (sessionStore.restore(deezerConnect, getActivity())) {
					mDeezerButton.setTitle(R.string.pref_deezer_remove);

				} else{
					mDeezerButton.setTitle(R.string.pref_deezer);

				}
			}
		}
	}
	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
	}


}
