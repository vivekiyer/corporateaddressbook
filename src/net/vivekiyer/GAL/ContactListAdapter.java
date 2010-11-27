/* Copyright 2010 Vivek Iyer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.vivekiyer.GAL;

import java.util.ArrayList;

import net.vivekiyer.GAL.KeyValuePair.Type;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Vivek Iyer
 *
 * This class is responsible for displaying the contact data in a list
 * It beautifies the list by displaying the attribute on the top left 
 * and the actual value below that. It also automatically finds phone numbers
 * and email addresses, and provides action buttons if any of those are found
 */
public class ContactListAdapter extends ArrayAdapter<KeyValuePair> {	
	
	//private static String TAG = "ContactListAdapter";
	
	/**
	 * @param context 
	 * @param textViewResourceId 
	 * @param kvps The contact details
	 * 
	 * Adds the contact details to the array adapter
	 */
	public ContactListAdapter(Context context, int textViewResourceId,
			ArrayList<KeyValuePair> kvps) {
		super(context, textViewResourceId);

		for (KeyValuePair kvp : kvps)
			this.add(kvp);
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 * 
	 * Displays the contact details in the UI
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row, null);
		}

		KeyValuePair kvp = this.getItem(position);
		if (kvp != null) {
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);

			// Set the top text
			if (tt != null) {
				tt.setText(kvp.getKey());
			}
			
			// Set the bottom text
			if (bt != null) {
				bt.setText(kvp.getValue());
			}

			// If the toptext contains a phone
			// Set the icon to phone and message
			String topText = kvp.getKey().toLowerCase();
			ImageView iv1 = (ImageView) v.findViewById(R.id.icon1);
			ImageView iv2 = (ImageView) v.findViewById(R.id.icon2);
			
			// Set the on click listeners
			iv1.setOnClickListener(mIconListener1);
			iv2.setOnClickListener(mIconListener2);
			
			if (topText.contains("phone")) {
				kvp.set_type(Type.PHONE);
				iv1.setImageResource(R.drawable.call_contact);
				iv2.setImageResource(R.drawable.sms);
				iv2.setVisibility(android.view.View.VISIBLE);
				iv1.setVisibility(android.view.View.VISIBLE);				
			}
			else if(topText.contains("email")){
				kvp.set_type(Type.EMAIL);
				iv2.setImageResource(R.drawable.ic_dialog_email);
				iv2.setVisibility(android.view.View.VISIBLE);
				iv1.setVisibility(android.view.View.INVISIBLE);
			}
			else{
				kvp.set_type(Type.OTHER);
				iv1.setVisibility(android.view.View.INVISIBLE);
				iv2.setVisibility(android.view.View.INVISIBLE);
			}
			iv1.setTag(kvp);
			iv2.setTag(kvp);
			
		}
		return v;
	}
	
	// Create an anonymous implementation of OnItemClickListener
	private OnClickListener mIconListener1 = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Get the tag, which will provide us the KVP
			ImageView iv1 = (ImageView) v.findViewById(R.id.icon1);
			KeyValuePair kvp = (KeyValuePair) iv1.getTag();
			
			// This can only be a phone
			if(kvp.get_type() == Type.PHONE){
				//Log.d(TAG, "Call "+kvp.getValue());	
				Intent  intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+kvp.getValue()));
				getContext().startActivity(intent);
			}				
		}		
	};
	
	// Create an anonymous implementation of OnItemClickListener
	private OnClickListener mIconListener2 = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Get the tag, which will provide us the KVP
			ImageView iv2 = (ImageView) v.findViewById(R.id.icon2);
			KeyValuePair kvp = (KeyValuePair) iv2.getTag();
			
			// This can be an email or SMS
			switch(kvp.get_type()){
			case PHONE:
				//Log.d(TAG, "SMS "+kvp.getValue());
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.putExtra("address", kvp.getValue());
				intent.setType("vnd.android-dir/mms-sms");
				getContext().startActivity(intent);
				break;
			case EMAIL:
				//Log.d(TAG, "Email "+kvp.getValue());				
				intent = new Intent(android.content.Intent.ACTION_SEND);				
				intent.setType("text/plain");
				intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{kvp.getValue()});
				getContext().startActivity(Intent.createChooser(intent, "Send mail..."));
				break;
			}
		}		
	};
}
