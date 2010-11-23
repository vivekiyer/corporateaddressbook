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
import java.util.Enumeration;
import java.util.Hashtable;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author Vivek Iyer
 *  
 * This class is the main entry point to the application
 */
public class CorporateAddressBook extends Activity implements OnClickListener, TaskCompleteCallback {	
	
	// TAG used for logging
	private static String TAG = "CorporateAddressBook";
	
	// Object that performs all the ActiveSync magic 
	private ActiveSyncManager activeSyncManager;	
	
	// Stores the list of contacts returned 
	private Hashtable<String, Contact> mContacts;
	
	// The listview that displays the contacts returned
	private ListView lv1;
	
	// Preference object that stores the accoutn credentials
	private SharedPreferences mPreferences;
	
	// Used to launch the preference pane
	static final int DISPLAY_PREFERENCES_REQUEST = 0;
	
	// Used to launch the initial configuration pane
	static final int DISPLAY_CONFIGURATION_REQUEST = 1;

	// Progress bar
	private ProgressDialog progressdialog;

	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * 
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Initialize preferences and the activesyncmanager
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		activeSyncManager = new ActiveSyncManager();
		
		// Create the progress bar
		progressdialog = new ProgressDialog(this);
		progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog.setCancelable(false);

		// Set the listener for the button clicks
		Button button = (Button) findViewById(R.id.Button01);
		button.setOnClickListener(this);

		// Check if we have successfully connected to an Exchange
		// server before.
		// If not launch the config pane and query the user for 
		// Exchange server settings
		if (!loadPreferences()) {
			Intent myIntent = new Intent();
			myIntent.setClassName("net.vivekiyer.GAL",
					"net.vivekiyer.GAL.Configure");
			startActivityForResult(myIntent, DISPLAY_CONFIGURATION_REQUEST);
		}
	}

	// Create an anonymous implementation of OnItemClickListener
	// that is used by the listview that displays the results
	private OnItemClickListener mListViewListener = new OnItemClickListener() {
		
		/* (non-Javadoc)
		 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
		 * 
		 * When the user clicks a particular entry in the list view
		 * launch the CorporateContactRecord activity 
		 */
		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id) {
			
			// Get the selected display name from the list view
			String selectedItem = (String) lv1.getItemAtPosition(position);
			Log.v(TAG, selectedItem);

			// Create a parcel with the associated contact object
			// This parcel is used to send data to the activity 
			Bundle b = new Bundle();
			b.putParcelable("net.vivekiyer.GAL", mContacts.get(selectedItem));

			// Launch the activity
			Intent myIntent = new Intent();
			myIntent.setClassName("net.vivekiyer.GAL",
					"net.vivekiyer.GAL.CorporateContactRecord");

			myIntent.putExtras(b);
			startActivity(myIntent);
		}
	};

	
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * 
	 * Implement the OnClickListener callback for the Go button
	 */
	public void onClick(View v) {		

		// Get the text entered by the user
		EditText text = (EditText) findViewById(R.id.Name);
		Editable name = text.getText();

		// Hide the keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
		
		// Launch the progress bar, so the user knows his request is being processed
		progressdialog.setMessage("Retrieving results");
		progressdialog.show();

		// Retrieve the results via an AsyncTask
		new GALSearch().execute(name.toString());
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Displays the menu when the user clicks the options button. 
	 * In our case our menu only contains one button - Settings
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * 
	 * Launches the preferences pane when the user clicks the settings option 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			showPreferences();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Launches the preferences activity
	 */
	public void showPreferences() {
		Intent myIntent = new Intent();
		myIntent.setClassName(
				"net.vivekiyer.GAL",
				"net.vivekiyer.GAL.Preferences");
		startActivityForResult(myIntent, DISPLAY_PREFERENCES_REQUEST);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 * 
	 * This method gets called after the launched activity is closed
	 * This application needs to handle the closing of two activity launches
	 *  - The preference pane
	 *  - The config pane 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		// Called when the user click on the preferences pane
		case DISPLAY_PREFERENCES_REQUEST:

			// The user saved the preference, let's check to make sure the
			// settings are ok
			// Initialize the activesync object
			loadPreferences();

			// Launch a progress bar so the user knows we are processing the change
			progressdialog.setMessage("Validating settings");
			progressdialog.show();

			// Check to make sure that the settings are ok
			ConnectionChecker checker = new ConnectionChecker(this);
			checker.execute(activeSyncManager);

			break;
			
		case DISPLAY_CONFIGURATION_REQUEST:
			
			// Initialize the activesync object with the validated settings
			loadPreferences();

			break;
		}
	}

	/**
	 * Reads the stored preferences and initializes the  
	 * @return True if a valid Exchange server settings was saved during the previous launch. False otherwise * 			   
	 * 
	 */
	public boolean loadPreferences() {
		activeSyncManager.setmUsername(
				mPreferences.getString(Preferences.KEY_USERNAME_PREFERENCE, ""));
		activeSyncManager.setPassword(
				mPreferences.getString(Preferences.KEY_PASSWORD_PREFERENCE, ""));
		activeSyncManager.setDomain(
				mPreferences.getString(Preferences.KEY_DOMAIN_PREFERENCE, ""));
		activeSyncManager.setServerName(
				mPreferences.getString(Preferences.KEY_SERVER_PREFERENCE, ""));
		activeSyncManager.setActiveSyncVersion(
				mPreferences.getString(Preferences.KEY_ACTIVESYNCVERSION_PREFERENCE, ""));
		activeSyncManager.setPolicyKey(
				mPreferences.getString(Preferences.KEY_POLICY_KEY_PREFERENCE, ""));

		activeSyncManager.Initialize();

		// Check to see if we have successfully connected to an Exchange server
		if (activeSyncManager.getActiveSyncVersion().equalsIgnoreCase(""))
			return false;

		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 * 
	 * Called when the application is closed
	 */
	@Override
	protected void onStop() {
		super.onStop();

		// Make sure that the activesync version and policy key get written
		// to the preferences
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(Preferences.KEY_ACTIVESYNCVERSION_PREFERENCE,
				activeSyncManager.getActiveSyncVersion());
		editor.putString(Preferences.KEY_POLICY_KEY_PREFERENCE,
				activeSyncManager.getPolicyKey());

		// Commit the edits!
		editor.commit();
	}

	/* (non-Javadoc)
	 * @see net.vivekiyer.GAL.TaskCompleteCallback#taskComplete(boolean)
	 * 
	 * Callback function that gets called after the 
	 * Connection checker checks the server settings
	 */
	@Override
	public void taskComplete(boolean taskStatus) {
		progressdialog.dismiss();		
		
		// If the server settings are not correct
		// Launch the config pane and ask the user to fix
		// the problem
		if (!taskStatus) {	
			Intent myIntent = new Intent();
			myIntent.setClassName("net.vivekiyer.GAL",
					"net.vivekiyer.GAL.Configure");
			startActivityForResult(myIntent, DISPLAY_CONFIGURATION_REQUEST);
		}
	}	
	
	/**
	 * @author Vivek Iyer
	 * 
	 * This class is responsible for executing the search on the GAL
	 * This derives from AsyncTask since we want to make sure that we 
	 * do not block the main UI thread, thus making the application unresponsive
	 */
	class GALSearch extends AsyncTask <String, Void, Boolean> {
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 * 
		 * The method that searches the GAL
		 */
		@Override
		protected Boolean doInBackground(String... params) {			
			try {
				// Search the GAL
				mContacts = activeSyncManager.searchGAL(params[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.v(TAG,e.toString());
			}
			return null;			
		}
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 * 
		 * This method displays the retrieved results in a list view
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			progressdialog.dismiss();
			
			switch(mContacts.size()){
			case 0:		
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(CorporateAddressBook.this, "No matches found", duration);
				toast.show();
				break;
			case 1:
				// Create a parcel with the associated contact object
				// This parcel is used to send data to the activity 
				Bundle b = new Bundle();
				Contact c = (Contact) mContacts.values().toArray()[0];
				b.putParcelable("net.vivekiyer.GAL", c);		
		        
		        Intent intent = new Intent(CorporateAddressBook.this,CorporateContactRecord.class);
		        intent.putExtras(b);        
		        
		        startActivity(intent);          
		        
				break;
			default:
				// Get the result and sort the alphabetically
				String[] names = new String[mContacts.size()];

				int i = 0;
				for (Enumeration<String> e = mContacts.keys(); e.hasMoreElements();) {
					names[i++] = e.nextElement();
				}
	
				Arrays.sort(names);
				
				lv1 = (ListView) findViewById(R.id.ListView01);
	
				// Create a new array adapter and add the result to this
				ArrayAdapter<String> listadapter 
					= new ArrayAdapter<String>(
							CorporateAddressBook.this,
							android.R.layout.simple_list_item_1, 
							names
							);
	
				lv1.setAdapter(listadapter);
				lv1.setOnItemClickListener(mListViewListener);
				break;
			}
		}
	}
};