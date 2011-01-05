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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Vivek Iyer
 *  
 * This class is the main entry point to the application
 */
public class CorporateAddressBook extends Activity implements OnClickListener{	
	
	// TAG used for logging
	private static String TAG = "CorporateAddressBook";
	
	// Object that performs all the ActiveSync magic 
	private ActiveSyncManager activeSyncManager;	
	
	// Stores the list of contacts returned 
	private Hashtable<String, Contact> mContacts;
	
	// Stores the XML returned by Exchange
	private String searchResultXML;
	
	// The listview that displays the contacts returned
	private ListView lv1;
	
	// Preference object that stores the account credentials
	private SharedPreferences mPreferences;
	
	// Used to launch the preference pane
	static final int DISPLAY_PREFERENCES_REQUEST = 0;
	
	// Used to launch the initial configuration pane
	static final int DISPLAY_CONFIGURATION_REQUEST = 1;

	// Progress bar
	private ProgressDialog progressdialog;

	// List of names in the list view control
	private String[] names;
	
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
		ImageButton button = (ImageButton) findViewById(R.id.Button01);
		button.setOnClickListener(this);

		lv1 = (ListView) findViewById(R.id.ListView01);
		
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
		
		EditText text = (EditText) findViewById(R.id.Name);
		text.setOnEditorActionListener(new EditText.OnEditorActionListener() {
		   @Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		            performSearch();
		            return true;
		        }
		        return false;
			}
		});
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

	/**
	 * Searches the GAL
	 */
	private void performSearch(){
		// Clear any results that are being displayed
		clearResult();
		
		// Get the text entered by the user
		EditText text = (EditText) findViewById(R.id.Name);
		Editable name = text.getText();

		// Hide the keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
		
		// Launch the progress bar, so the user knows his request is being processed
		progressdialog.setMessage("Retrieving results");
		progressdialog.show();

		//activeSyncManager.testXMLtoWBXML();
		
		// Retrieve the results via an AsyncTask
		new GALSearch().execute(name.toString());
		
	}
	
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * 
	 * Implement the OnClickListener callback for the Go button
	 */
	public void onClick(View v) {		
		performSearch();
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
			showConfiguration();
			return true;
		case R.id.clear:
			clearResult();
			EditText text = (EditText) findViewById(R.id.Name);
			text.setText("");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Launches the preferences activity
	 */
	public void showConfiguration() {
		Intent myIntent = new Intent();
		myIntent.setClassName("net.vivekiyer.GAL",
				"net.vivekiyer.GAL.Configure");
		startActivityForResult(myIntent, DISPLAY_CONFIGURATION_REQUEST);
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
		case DISPLAY_CONFIGURATION_REQUEST:			
			// Initialize the activesync object with the validated settings
			if (!loadPreferences()) {
				Intent myIntent = new Intent();
				myIntent.setClassName("net.vivekiyer.GAL",
						"net.vivekiyer.GAL.Configure");
				startActivityForResult(myIntent, DISPLAY_CONFIGURATION_REQUEST);
			}
			break;
		}
	}
	
	/**
	 * The older version of the application included http and https in the
	 * server name. The newer version no longer has this. Hence clean up is required
	 */
	private void cleanUpServerName(){
		String serverName = mPreferences.getString(Configure.KEY_SERVER_PREFERENCE, "");		
		serverName = serverName.toLowerCase();		
		
		if(serverName.startsWith("https://")){
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean(Configure.KEY_USE_SSL,true);
			serverName = serverName.substring(8);
			editor.putString(Configure.KEY_SERVER_PREFERENCE, serverName);
			editor.commit();
		}
		else if(serverName.startsWith("http://")){
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean(Configure.KEY_USE_SSL,false);
			serverName = serverName.substring(7);
			editor.putString(Configure.KEY_SERVER_PREFERENCE, serverName);
			editor.commit();
		}
		
		activeSyncManager.setServerName(serverName);		
	}

	/**
	 * Reads the stored preferences and initializes the  
	 * @return True if a valid Exchange server settings was saved during the previous launch. False otherwise * 			   
	 * 
	 */
	public boolean loadPreferences() {
		activeSyncManager.setmUsername(
				mPreferences.getString(Configure.KEY_USERNAME_PREFERENCE, ""));
		activeSyncManager.setPassword(
				mPreferences.getString(Configure.KEY_PASSWORD_PREFERENCE, ""));
		activeSyncManager.setDomain(
				mPreferences.getString(Configure.KEY_DOMAIN_PREFERENCE, ""));

		// Clean up server name from previous version of the app
		cleanUpServerName();
		
	
		activeSyncManager.setActiveSyncVersion(
				mPreferences.getString(Configure.KEY_ACTIVESYNCVERSION_PREFERENCE, ""));
		activeSyncManager.setPolicyKey(
				mPreferences.getString(Configure.KEY_POLICY_KEY_PREFERENCE, ""));
		activeSyncManager.setAcceptAllCerts(
				mPreferences.getBoolean(Configure.KEY_ACCEPT_ALL_CERTS, true));
		activeSyncManager.setUseSSL(
				mPreferences.getBoolean(Configure.KEY_USE_SSL, true));

		activeSyncManager.Initialize();

		// Check to see if we have successfully connected to an Exchange server
		if (activeSyncManager.getActiveSyncVersion().equalsIgnoreCase(""))
			return false;

		// If we connected fine, load the last set of results
		try {
			searchResultXML = mPreferences.getString(Configure.KEY_RESULTS_PREFERENCE, "");
			if(searchResultXML.equalsIgnoreCase(""))
				return true;
			
			mContacts = activeSyncManager.parseXML(searchResultXML);		
			
			EditText text = (EditText) findViewById(R.id.Name);		
			text.setText(mPreferences.getString(Configure.KEY_SEARCH_TERM_PREFERENCE, "" ));

			displayResult();
			
			// Hide the keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(text.getWindowToken(), 0);

		} catch (Exception e) {
			Log.e(TAG,e.toString());
		}
		
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
		editor.putString(Configure.KEY_ACTIVESYNCVERSION_PREFERENCE,
				activeSyncManager.getActiveSyncVersion());
		editor.putString(Configure.KEY_POLICY_KEY_PREFERENCE,
				activeSyncManager.getPolicyKey());
		editor.putString(Configure.KEY_RESULTS_PREFERENCE,
					searchResultXML);
		EditText text = (EditText) findViewById(R.id.Name);		
		editor.putString(Configure.KEY_SEARCH_TERM_PREFERENCE, text.getText().toString() );
		
		// Commit the edits!
		editor.commit();
	}
	
	/**
	 * Displays the search results in the Listview 
	 */
	private void displayResult(){
		// Get the result and sort the alphabetically
		names = new String[mContacts.size()];

		int i = 0;
		for (Enumeration<String> e = mContacts.keys(); e.hasMoreElements();) {
			names[i++] = e.nextElement();
		}

		Arrays.sort(names);	
		
		// Create a new array adapter and add the result to this
		ArrayAdapter<String> listadapter 
			= new ArrayAdapter<String>(
					CorporateAddressBook.this,
					android.R.layout.simple_list_item_1, 
					names
					);

		lv1.setAdapter(listadapter);
		lv1.setOnItemClickListener(mListViewListener);
	}
	
	/**
	 * @param s The alert message
	 * Displays an alert dialog with the messaged provided
	 */
	private void showAlert(String s){
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(s)
				.setPositiveButton("Ok", null);
		AlertDialog alert = alt_bld.create();
		alert.show();
	}
	
	
	/**
	 * Clear the results from the listview
	 */
	private void clearResult(){
		names = new String[0];

		// Create a new array adapter and add the result to this
		ArrayAdapter<String> listadapter 
			= new ArrayAdapter<String>(
					CorporateAddressBook.this,
					android.R.layout.simple_list_item_1, 
					names
					);

		lv1.setAdapter(listadapter);
	}
	
	/**
	 * @author Vivek Iyer
	 * 
	 * This class is responsible for executing the search on the GAL
	 * This derives from AsyncTask since we want to make sure that we 
	 * do not block the main UI thread, thus making the application unresponsive
	 */
	class GALSearch extends AsyncTask <String, Void, Boolean> {
		
		private String errorMesg = "";
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 * 
		 * The method that searches the GAL
		 */
		@Override
		protected Boolean doInBackground(String... params) {			
			try {
				// Search the GAL
				mContacts = null;
				searchResultXML = activeSyncManager.searchGAL(params[0]);
				mContacts = activeSyncManager.parseXML(searchResultXML);
			} catch (Exception e) {
				errorMesg += "ActiveSync version=" +activeSyncManager.getActiveSyncVersion() + "\n";
				errorMesg += e.toString();
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
			
			if(mContacts == null){
				CorporateAddressBook.this.showAlert(errorMesg);
				return;
			}
				
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
				displayResult();
				break;
			}
		}
	}
};