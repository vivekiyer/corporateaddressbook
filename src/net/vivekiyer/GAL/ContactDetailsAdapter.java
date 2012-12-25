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
import java.util.Locale;

import com.devoteam.quickaction.QuickActionWindow;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @author Vivek Iyer
 *
 * This class is responsible for displaying the contact data in a list
 * It beautifies the list by displaying the attribute on the top left 
 * and the actual value below that. It also automatically finds phone numbers
 * and email addresses, and provides action buttons if any of those are found
 */
public class ContactDetailsAdapter extends ArrayAdapter<KeyValuePair> {	
	
	/**
	 * @param context 
	 * @param textViewResourceId 
	 * @param kvps The contact details
	 * 
	 * Adds the contact details to the array adapter
	 */
	public ContactDetailsAdapter(Context context, int textViewResourceId,
			ArrayList<KeyValuePair> kvps) {
		super(context, textViewResourceId);

		ArrayList<KeyValuePair> email = new ArrayList<KeyValuePair>(), others = new ArrayList<KeyValuePair>();
		for (KeyValuePair kvp : kvps) {
			// Not very elegant way to sort, 
			// will think of something better...
			switch (kvp.get_type()) {
			case EMAIL:
				email.add(kvp);
				break;
			case OTHER:
				others.add(kvp);
				break;
			default:
				this.add(kvp);
			}
		}			
		for(KeyValuePair kvp : email)
			this.add(kvp);
		for(KeyValuePair kvp : others)
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
			v = vi.inflate(R.layout.detail_row, null);
		}

		KeyValuePair kvp = this.getItem(position);
		if (kvp != null) {
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);

			// Set the top text
			if (tt != null) {
				tt.setText(kvp.getValue());
			}
			
			ImageButton im = (ImageButton) v.findViewById(R.id.detailsMoreActions);
			// Set the on click listeners
			im.setOnClickListener(mIconListener2);
			View v2 = v.findViewById(R.id.buttonContainer);
			
			// Display the "More actions" button for phone and email
			switch (kvp.get_type()) {
			case MOBILE:
				v2.setVisibility(android.view.View.VISIBLE);
				bt.setText(Utility.getUCString(R.string.mobile));
				v.setOnClickListener(Listeners.getCallListener(kvp.getValue()));
				break;
			case PHONE:
				v2.setVisibility(android.view.View.VISIBLE);
				bt.setText(Utility.getUCString(R.string.office));
				v.setOnClickListener(Listeners.getCallListener(kvp.getValue()));
				break;
			case EMAIL:
				v2.setVisibility(android.view.View.VISIBLE);
				bt.setText(Utility.getUCString(R.string.email));
				v.setOnClickListener(Listeners.getMailListener(kvp.getValue()));
				break;
			default:
				// For others: hide "More actions"
				v2.setVisibility(android.view.View.GONE);
				v.setOnClickListener(null);
				// Try to find a reasonable label
				final String key = kvp.getKey().toLowerCase(Locale.getDefault());
				if(key.contains("office")){ //$NON-NLS-1$
					bt.setText(Utility.getUCString(R.string.location));
				}
				else if(key.contains("alias")){ //$NON-NLS-1$
					bt.setText(Utility.getUCString(R.string.alias));
				}
				else if(key.contains("firstname")){ //$NON-NLS-1$
					bt.setText(Utility.getUCString(R.string.firstname));
				}
				else if(key.contains("lastname")){ //$NON-NLS-1$
					bt.setText(Utility.getUCString(R.string.lastname));
				}
				else if(key.contains("title")){ //$NON-NLS-1$
					bt.setText(Utility.getUCString(R.string.title));
				}
				else if(key.contains("company")){ //$NON-NLS-1$
					bt.setText(Utility.getUCString(R.string.company));
				}
				else{
					bt.setText(key.toUpperCase(Locale.getDefault()));
				}
			}
			im.setTag(kvp);			
		}
		return v;
	}
	
	
	// Create an anonymous implementation of OnItemClickListener
	// Called when the user clicks the sms or the email icon
	private OnClickListener mIconListener2 = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Get the tag, which will provide us the KVP
			ImageButton im = (ImageButton) v.findViewById(R.id.detailsMoreActions);
			final KeyValuePair kvp = (KeyValuePair) im.getTag();
			
			int[] xy = new int[2];
			v.getLocationInWindow(xy);
			Rect rect = new Rect(xy[0], xy[1], xy[0]+v.getWidth(), xy[1]+v.getHeight());
			final QuickActionWindow qa = new QuickActionWindow(v.getContext(), v, rect);
			
			switch (kvp.get_type()){
			case MOBILE:				
				qa.addItem(R.drawable.ic_menu_start_conversation, R.string.sendMessage,
					Listeners.getSmsListener(kvp.getValue(), qa));
	
			case PHONE:
				qa.addItem(R.drawable.ic_menu_call, R.string.call,
					Listeners.getCallListener(kvp.getValue(), qa));
				break;
				
			case EMAIL:
				qa.addItem(R.drawable.ic_menu_compose, R.string.sendEmail, Listeners.getMailListener(kvp.getValue(), qa));
			default:
				break;
			}

			qa.addItem(R.drawable.ic_menu_copy, R.string.copyToClipboard, Listeners.getCopyListener(kvp.getValue(), qa));

			im.getLocationOnScreen(xy);
			qa.show(xy[0] + im.getWidth()/2);
		}

	};
} 
