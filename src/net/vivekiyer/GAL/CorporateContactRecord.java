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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;


/**
 * @author Vivek Iyer 
 * This class is used to display a Contact object in a list.
 * The class takes a parceled Contact object and displays the
 * DisplayName. It also allows the user to save the contact
 */
/**
 * @author vivek
 *
 */
public class CorporateContactRecord extends ListActivity{

	private Contact mContact;
	private ContactListAdapter m_adapter;
	private ContactWriter contactWriter;
	
	// Menu ids
	private static final int MENU_ID_COPY_TO_CLIPBOARD = 0;
	private static final int MENU_ID_EMAIL = 1;
	private static final int MENU_ID_CALL = 2;
	private static final int MENU_ID_EDIT_BEFORE_CALL = 3;
	private static final int MENU_ID_SMS = 4;
	

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

		//getListView().setOnItemLongClickListener(mListViewLongClickListener);		
		contactWriter = ContactWriter.getInstance();
		contactWriter.Initialize(this, getLayoutInflater(), mContact);		
		
		registerForContextMenu(getListView());
	}

	
	/**
	 * @param menu
	 * @param v
	 * @param menuInfo
	 * 
	 * Create a context menu for the list view
	 * Depending upon the item selected, shows the user
	 * different options
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	  super.onCreateContextMenu(menu, v, menuInfo);  
	  
	  AdapterView.AdapterContextMenuInfo info 
	  		= (AdapterView.AdapterContextMenuInfo)menuInfo;
	  
	  // Get the selected item from the listview adapter
	  KeyValuePair kvp = m_adapter.getItem(info.position);
	  
	  // Set the header to the selected text
	  menu.setHeaderTitle(kvp.getValue());

	  // Add the default options (copy to clipboard)
	  menu.add(
			  Menu.NONE, 
			  MENU_ID_COPY_TO_CLIPBOARD, 
			  Menu.NONE, 
			  "Copy to clipboard");
	  
	  // Handle the special cases
	  switch(kvp.get_type()){
	  case EMAIL:
		  menu.add(
				  Menu.NONE, 
				  MENU_ID_EMAIL, 
				  Menu.NONE, 
				  "Send email");
		  break;
	  case MOBILE:
	  case PHONE:
		  menu.add(
				  Menu.NONE, 
				  MENU_ID_CALL, 
				  Menu.NONE, 
				  "Call " + kvp.getValue());
		  menu.add(
				  Menu.NONE, 
				  MENU_ID_EDIT_BEFORE_CALL, 
				  Menu.NONE, 
				  "Edit number before call");
		  menu.add(
				  Menu.NONE, 
				  MENU_ID_SMS, 
				  Menu.NONE, 
				  "Send text message");
		  break;
	  }
	}	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  
	  // Get the selected item from the listview adapter
	  KeyValuePair kvp = m_adapter.getItem(info.position);

		switch (item.getItemId()) {
		case MENU_ID_CALL:
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ kvp.getValue()));
			startActivity(intent);
			break;
		case MENU_ID_COPY_TO_CLIPBOARD:
			ClipboardManager clipboard 
				= (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(kvp.getValue());
			Toast.makeText(
					this, 
					"Text copied to clipboard", 
					Toast.LENGTH_SHORT).show();			
			break;
		case MENU_ID_EDIT_BEFORE_CALL:
			intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ kvp.getValue()));
			startActivity(intent);
			break;
		case MENU_ID_EMAIL:
			intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { kvp.getValue() });
			startActivity(Intent.createChooser(intent, "Send mail..."));
			break;
		case MENU_ID_SMS:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.putExtra("address", kvp.getValue());
			intent.setType("vnd.android-dir/mms-sms");
			startActivity(intent);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}
	
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
