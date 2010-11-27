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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;


/**
 * @author Vivek Iyer 
 * This class is used to display a Contact object in a list.
 * The class takes a parceled Contact object and displays the
 * DisplayName. It also allows the user to save the contact
 */
public class CorporateContactRecord extends ListActivity{

	private Contact mContact;
	private ContactListAdapter m_adapter;
	private ContactWriter contactWriter;

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
		contactWriter = ContactWriter.getInstance();
		contactWriter.Initialize(this, getLayoutInflater(), mContact);
		
	}

	// Create an anonymous implementation of OnItemClickListener
	private OnItemClickListener mListViewListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id) {

		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Displays the menu when the user clicks the Options button In our case the
	 * menu contains only one button - save
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contacts_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * 
	 * Shows the save contact option when the user clicks the save option
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.saveContact:
			contactWriter.saveContact();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}	
	
	
	/**
	 * Called when this activity is about to be destroyed by the system.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		contactWriter.cleanUp();		
	}	
};
