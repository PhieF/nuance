package com.spipi.spipimediaplayer.ftp;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.spipi.spipimediaplayer.Access;
import com.spipi.spipimediaplayer.R;
import com.spipi.spipimediaplayer.hubic.AccessDatasource;
import com.spipi.spipimediaplayer.hubic.AccountListAdapter;
import com.spipi.spipimediaplayer.hubic.HubicAccess;
import com.spipi.spipimediaplayer.hubic.ItemAccount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AccountListActivity extends ListFragment {


	    private AccountListAdapter adapter;
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	   
	        fill();
	    }
	    @Override
	    public void onResume(){
	    	super.onResume();
	    	fill();
	    }
	    private void fill()
	    {


	       List<ItemAccount> dir = new ArrayList<ItemAccount>();
	       AccessDatasource ads= new AccessDatasource(this.getActivity().getApplicationContext());
			Log.d("accessdebug", "filling");
    		ads.open();
    		List<Access> acc = ads.getAllAccounts();
    		
    		for (int i = 0; i<acc.size(); i++){//
                                //String formated = lastModDate.toString();
								if(acc.get(i) instanceof  HubicAccess)
                               	 dir.add(new ItemAccount(acc.get(i).getName(),acc.get(i)));
    		}
         
    		Collections.sort(dir);

	              
	         adapter = new AccountListAdapter(this.getActivity().getApplicationContext(), R.layout.layout_hubic_account,dir);
	         this.setListAdapter(adapter);
	    }
	    @Override
		public void onListItemClick(ListView l, View v, int position, long id) {
	            Access acc= adapter.getItem(position).getAcc();
	            
	            	            
	        }
	    private void onFileClick(ItemAccount o)
	    {
	        
	    }
	
}
