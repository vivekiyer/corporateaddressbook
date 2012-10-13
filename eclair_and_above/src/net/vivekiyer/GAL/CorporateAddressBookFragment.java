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

import java.util.Arrays;
import com.google.common.collect.HashMultimap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Vivek Iyer
 * 
 *         This class is the main entry point to the application
 */
/**
 * @author danm
 *
 */
/**
 * @author danm
 *
 */
/**
 * @author danm
 *
 */
public class CorporateAddressBookFragment extends android.support.v4.app.Fragment {
 
	public interface ContactListListener {
		public void onContactSelected(Contact contact);
		public void onSearchCleared();
	}
	
	// TAG used for logging
	// private static String TAG = "CorporateAddressBook";

	// Stores the list of contacts returned
	private HashMultimap<String, Contact> mContacts;

	// List of names in the list view control
	private Contact[] contactList;

	// Last search term
	private String latestSearchTerm;

	protected ContactListListener contactListListener;
	
	private Boolean isSelectable = false;
	
	private Boolean isDualFragment = false;

	public Boolean getIsSelectable() {
		return isSelectable;
	}

	public void setIsSelectable(Boolean isSelectable) {
		this.isSelectable = isSelectable;
	    setSelectionMode(getView(), isSelectable);
	}

	public Boolean getIsDualFragment() {
		return isDualFragment;
	}

	public void setIsDualFragment(Boolean isDualFragment) {
		this.isDualFragment = isDualFragment;
	}

	private void setSelectionMode(View view, Boolean isSelectable) {
		ListView lv = (ListView) view.findViewById(R.id.contactsListView);
	    lv.setChoiceMode(isSelectable ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * 
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container,
	        Bundle savedInstanceState) {
	 
	    View view = inflater.inflate(R.layout.main, container, false);
	    setSelectionMode(view, isSelectable);
		return view;
	}
	
	
    /* (non-Javadoc)
     * Overridden so that any Activity this Fragment is attached to is hooked up
     * to the OnContactSelectedListener
     * 
     * @see android.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.contactListListener = (ContactListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnContactSelectedListener");
        }
    }
	
	// Create an anonymous implementation of OnItemClickListener
	// that is used by the listview that displays the results
	private final OnItemClickListener mListViewListener = new OnItemClickListener() {

		/*
		 * (non-Javadoc)
		 * When the user clicks a particular entry in the list view launch the
		 * CorporateContactRecord activity
		 * 
		 * @see
		 * android.widget.AdapterView.OnItemClickListener#onItemClick(android
		 * .widget.AdapterView, android.view.View, int, long)
		 * 
		 */
		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id) {

			// Get the selected display name from the list view
			final Contact selectedItem = (Contact) ((ListView)getView().findViewById(R.id.contactsListView))
					.getItemAtPosition(position);

			// Trigger callback so that the Activity can decide how to handle the click
			assert(contactListListener != null);
			contactListListener.onContactSelected(selectedItem);		
		}
	};
	
	protected void setViewBackground(Boolean shaded){
		if(shaded){
			getView().findViewById(R.id.resultheader).setBackgroundDrawable(getResources().getDrawable(R.drawable.header_border_shading));
			getView().findViewById(R.id.contactsListView).setBackgroundDrawable(getResources().getDrawable(R.drawable.border_shading));
		}
		else{
			getView().findViewById(R.id.resultheader).setBackgroundColor(getResources().getColor(R.color.header_background));
			getView().findViewById(R.id.contactsListView).setBackgroundColor(getResources().getColor(R.color.contact_list_background));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Displays the menu when the user clicks the options button. In our case
	 * our menu only contains one button - Settings
	 */
	@TargetApi(11)
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_fragment_menu, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * 
	 * Launches the preferences pane when the user clicks the settings option
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.clear:
			clearResult();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 * 
	 * Called when the application is closed
	 */
	@Override
	public void onStop() {
		super.onStop();
	}

	public void displayResult(HashMultimap<String, Contact> contacts, String searchTerm) {
		this.mContacts = contacts;
		this.latestSearchTerm = searchTerm;
		this.displayResult();
	}

	/**
	 * Displays the search results in the Listview
	 */
	private void displayResult() {
		if(mContacts == null)
		{
			Toast.makeText(getActivity(), R.string.undefined_result_please_try_again, Toast.LENGTH_LONG).show();
			return;
		}
		TextView tv = (TextView) this.getView().findViewById(R.id.resultheader);
		if(this.latestSearchTerm == null || this.latestSearchTerm.length() == 0)
			tv.setText(String.format(getString(R.string.last_search_produced_x_results), mContacts.size()));
		else
			tv.setText(String.format(getString(R.string.found_x_results_for_y), mContacts.size(), this.latestSearchTerm));
		
		// Get the result and sort the alphabetically
		contactList = new Contact[mContacts.size()];
		
		int i = 0;
		for (Contact contact : mContacts.values()) {
			contactList[i++] = contact;
		}

		Arrays.sort(contactList);

		// Create a new array adapter and add the result to this
		final ContactListAdapter listadapter = new ContactListAdapter(
				this.getActivity(), R.layout.contact_row,
				contactList);

		ListView lv = (ListView) getView().findViewById(R.id.contactsListView);
		lv.setAdapter(listadapter);
		lv.setOnItemClickListener(mListViewListener);
	}

	/**
	 * Clear the results from the listview
	 */
	protected void clearResult() {
		contactListListener.onSearchCleared();
		contactList = new Contact[0];

		// Create a new array adapter and add the result to this
		final ContactListAdapter listadapter = new ContactListAdapter(
				this.getActivity(), R.layout.contact_row,
				contactList);

		ListView lv = (ListView) getView().findViewById(R.id.contactsListView);
		lv.setAdapter(listadapter);
		TextView v = (TextView) getView().findViewById(R.id.resultheader);
		v.setText(R.string.EnterSearchTerm);
		
		assert(contactListListener != null);
	}
}