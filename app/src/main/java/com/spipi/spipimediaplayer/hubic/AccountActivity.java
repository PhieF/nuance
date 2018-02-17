package com.spipi.spipimediaplayer.hubic;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.spipi.spipimediaplayer.R;


@SuppressLint("ValidFragment")
public class AccountActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hubic_accounts);

		if (savedInstanceState == null) {
			 getFragmentManager().beginTransaction()
             .add(R.id.container, new  AccountListActivity())
               .commit();
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
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
    public void sendMessage(View view) {
        // Do something in response to button
    	NewAccountDialogFragment f = new NewAccountDialogFragment();
        f.show(getFragmentManager(), null);
    	
    	
    }

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_hubic_accounts,
					container, false);
			return rootView;
		}
	}
	   public class NewAccountDialogFragment extends DialogFragment {
	        @Override
	        public Dialog onCreateDialog(Bundle savedInstanceState) {
	            // Use the Builder class for convenient dialog construction
	            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	            builder.setMessage("Choose a name :");
	            final EditText input = new EditText(getActivity());
	            builder.setView(input)
	                   .setPositiveButton("Create", new DialogInterface.OnClickListener() {
	                       public void onClick(DialogInterface dialog, int id) {
	                    	   String value = input.getText().toString();
	                    	   System.out.println(value);
	                    	   Intent intent = new Intent(getActivity(), RequestTokenActivity.class);
	                       	//EditText editText = (EditText) findViewById(R.id.edit_message);
	                       //	String message = editText.getText().toString();
	                       	intent.putExtra("NAME", value);
	                       	startActivity(intent);
	                       }
	                   })
	                   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                       public void onClick(DialogInterface dialog, int id) {
	                           // User cancelled the dialog
	                       }
	                   });
	            // Create the AlertDialog object and return it
	            return builder.create();
	        }
	    }
}
