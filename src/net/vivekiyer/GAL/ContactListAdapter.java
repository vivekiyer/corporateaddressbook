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
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

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
	private ActiveSyncManager syncManager;
	private String searchTerm;

	@Override
	public int getCount() {
		// If syncManager != null then it means that there are more results available
		// and we add another item to the list labeled "Get more..."
		return super.getCount() + (syncManager != null ? 1 : 0);    //To change body of overridden methods use File | Settings | File Templates.
	}

	/**
	 * Adds the contact details to the array adapter
	 *
	 * @param context
	 * @param textViewResourceId
	 * @param cs The contact details
	 *
	 */
	public ContactListAdapter(Context context, int textViewResourceId,
			List<Contact> cs, ActiveSyncManager syncManager) {
		super(context, textViewResourceId, cs);

		this.syncManager = syncManager;
	}

	/* (non-Javadoc)
	 * Displays the contact details in the UI
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 * 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(position == super.getCount())
			return getNextSearchView(convertView, parent);

		View v = convertView;
		if (v == null || !(v instanceof ContactRowView)) {
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
					s = s + ", "; //$NON-NLS-1$
				{
					bt.setText(s + c.getCompany());
				}
			}

			QuickContactBadge qcb = (QuickContactBadge) v.findViewById(R.id.contactPicture);
			qcb.assignContactFromEmail(c.getEmail(), true); 
			qcb.setMode(ContactsContract.QuickContact.MODE_LARGE);
			byte[] pic;
			if((pic = c.getPicture()) != null) {
				Bitmap bm = BitmapFactory.decodeByteArray(pic, 0, pic.length);
				qcb.setImageBitmap(bm);
			} else {
				if(Utility.isPreHoneycomb())
					qcb.setImageResource(R.drawable.ic_quick);
				else
					qcb.setImageToDefault();
			}
			v.setTag(c);			
			qcb.bringToFront();
		}
		return v;
	}

	private View getNextSearchView(View convertView, ViewGroup parent) {
		View v = convertView;
//		if (v == null || !(v instanceof ContactRowView)) {
		LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		v = vi.inflate(R.layout.next_search, null);
//		}
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getContext(), CorporateAddressBook.class);
				i.setAction(Intent.ACTION_SEARCH);
				i.putExtra(CorporateAddressBook.ACCOUNT_KEY, syncManager.getAccountKey());
				i.putExtra(CorporateAddressBook.START_WITH, getCount());
				i.putExtra(CorporateAddressBook.REQUERY, true);
				getContext().startActivity(i);
			}
		});
		return v;
	}

	@Override
	public void addAll(Collection<? extends Contact> collection) {
		super.addAll(collection);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
}
