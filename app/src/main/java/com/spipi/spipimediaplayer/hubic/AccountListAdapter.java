package com.spipi.spipimediaplayer.hubic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.spipi.spipimediaplayer.R;

import java.util.List;

public class AccountListAdapter extends ArrayAdapter<ItemAccount> {
       private Context c;
	   private int id;
	   private List<ItemAccount> items;
	       
	  public AccountListAdapter(Context context, int textViewResourceId, List<ItemAccount> objects) {
            super(context, textViewResourceId, objects);
            c = context;
            id = textViewResourceId;
            items = objects;
	 }
	 public ItemAccount getItem(int i)
	 {
	    return items.get(i);
	}
	 @Override
	 public View getView(int position, View convertView, ViewGroup parent) {
	               View v = convertView;
	      if (v == null) {
	           LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(id, null);
	       }
	              
	               /* create a new view of my layout and inflate it in the row */
	                //convertView = ( RelativeLayout ) inflater.inflate( resource, null );
	               
           final ItemAccount o = items.get(position);
           if (o != null) {
                   TextView t1 = (TextView) v.findViewById(R.id.TextView01);

                  
                   if(t1!=null)
                   t1.setText(o.getName());
                  
           }
           return v;
   }
	
}
