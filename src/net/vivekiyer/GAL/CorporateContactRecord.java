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

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * @author Vivek Iyer
 * This class is used to display a Contact object in a list. 
 * The class takes a parceled Contact object
 * and displays the DisplayName
 */
public class CorporateContactRecord extends ListActivity {

	private Contact mContact;
	private ContactListAdapter m_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b = getIntent().getExtras();
		
		mContact = b.getParcelable("net.vivekiyer.GAL");

		setContentView(R.layout.contact);
		this.m_adapter = new ContactListAdapter(this, R.layout.row,
				mContact.getDetails());
		setListAdapter(this.m_adapter);

		TextView tv1 = (TextView) findViewById(R.id.displayName);
		tv1.setText(mContact.getDisplayName());

		getListView().setOnItemClickListener(mListViewListener);
	}

	// Create an anonymous implementation of OnItemClickListener
	private OnItemClickListener mListViewListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id) {
			
		}
	};

};
