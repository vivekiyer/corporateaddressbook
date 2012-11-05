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

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.TextView;

/**
 * 
 * This class is responsible for displaying the contact data in a list.
 * <p/>
 * It beautifies the list by displaying the attribute on the top left 
 * and the actual value below that. It also automatically finds phone numbers
 * and email addresses, and provides action buttons if any of those are found.
 * 
 * @author Vivek Iyer
 *
 */
public class ContactListAdapter extends ArrayAdapter<Contact> {	
	
	//private static String TAG = "ContactListAdapter";
	
	/**
	 * Adds the contact details to the array adapter
	 * 
	 * @param context 
	 * @param textViewResourceId 
	 * @param kvps The contact details
	 * 
	 */
	public ContactListAdapter(Context context, int textViewResourceId,
			Contact[] cs) {
		super(context, textViewResourceId);

		for (Contact c : cs)
			this.add(c);
	}

	/* (non-Javadoc)
	 * Displays the contact details in the UI
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 * 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.contact_row, null);
		}

		Contact c = this.getItem(position);
		if (c != null) {
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);

			// Set the top text
			if (tt != null) {
				tt.setText(c.getDisplayName());
			}
			
			// Set the bottom text
			if (bt != null) {
				String s = c.getTitle();
				if(s != null && s.length() > 0)
					s = s + ", ";
				{
					bt.setText(s + c.getCompany());
				}
			}

			QuickContactBadge qcb = (QuickContactBadge) v.findViewById(R.id.contactPicture);
			qcb.assignContactFromEmail(c.getEmail(), true); 
			qcb.setMode(ContactsContract.QuickContact.MODE_LARGE);
			v.setTag(c);			
			qcb.bringToFront();
			// If the toptext contains a phone
			// Set the icon to phone and message
//			ImageView iv2 = (ImageView) v.findViewById(R.id.contactPicture);
//			
//			// Set the on click listeners
//			iv2.setOnClickListener(mIconListener2);
//			
//			iv2.setTag(c);		
			
		}
		return v;
	}
	
	
	// Create an anonymous implementation of OnItemClickListener
	// Called when the user clicks the sms or the email icon
//	private OnClickListener mIconListener2 = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			// Get the tag, which will provide us the KVP
////			ImageView iv2 = (ImageView) v.findViewById(R.id.contactPicture);
//			Contact c = (Contact) v.getTag();
//			
//			// Create a parcel with the associated contact object
//			// This parcel is used to send data to the activity
//			final Bundle b = new Bundle();
//			b.putParcelable("net.vivekiyer.GAL", c);
//
//			// Launch the activity
//			final Intent myIntent = new Intent();
//			myIntent.setClassName("net.vivekiyer.GAL",
//					"net.vivekiyer.GAL.CorporateContactRecord");
//
//			myIntent.putExtras(b);
//			getContext().startActivity(myIntent);
//		}		
//	};
}
