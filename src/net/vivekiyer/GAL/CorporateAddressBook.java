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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Vivek Iyer
 * 
 *         This class is the main entry point to the application
 */
public class CorporateAddressBook extends Activity implements OnClickListener {

	// TAG used for logging
	// private static String TAG = "CorporateAddressBook";

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
	private Contact[] contactList;

	// Last search term
	private String latestSearchTerm;

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
		setContentView(R.layout.main);

		// Initialize preferences and the activesyncmanager
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		activeSyncManager = new ActiveSyncManager();

		// Create the progress bar
		progressdialog = new ProgressDialog(this);
		progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog.setCancelable(false);

		// Turn keystrokes into search
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		lv1 = (ListView) findViewById(R.id.ListView01);

		// Check if we have successfully connected to an Exchange
		// server before.
		// If not launch the config pane and query the user for
		// Exchange server settings
		if (!loadPreferences()) {
			final Intent myIntent = new Intent();
			myIntent.setClassName("net.vivekiyer.GAL",
					"net.vivekiyer.GAL.Configure");
			startActivityForResult(myIntent, DISPLAY_CONFIGURATION_REQUEST);
		}


		// Get the intent, verify the action and get the query
		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			performSearch(query);
		}
	}
	
	// Assist user by showing search box whenever returning
	@Override
	public void onStart() {
		super.onStart();
		final Intent intent = getIntent();
		if (intent != null) {
			final Set<String> categories = intent.getCategories();
			if ((categories != null)
					&& categories.contains(Intent.CATEGORY_LAUNCHER)) {
				this.onSearchRequested();
			}
		}
	}

	// Create an anonymous implementation of OnItemClickListener
	// that is used by the listview that displays the results
	private final OnItemClickListener mListViewListener = new OnItemClickListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.widget.AdapterView.OnItemClickListener#onItemClick(android
		 * .widget.AdapterView, android.view.View, int, long)
		 * 
		 * When the user clicks a particular entry in the list view launch the
		 * CorporateContactRecord activity
		 */
		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id) {

			// Get the selected display name from the list view
			final Contact selectedItem = (Contact) lv1
					.getItemAtPosition(position);

			// Create a parcel with the associated contact object
			// This parcel is used to send data to the activity
			final Bundle b = new Bundle();
			b.putParcelable("net.vivekiyer.GAL", selectedItem);

			// Launch the activity
			final Intent myIntent = new Intent();
			myIntent.setClassName("net.vivekiyer.GAL",
					"net.vivekiyer.GAL.CorporateContactRecord");

			myIntent.putExtras(b);
			startActivity(myIntent);
		}
	};

	/**
	 * Searches the GAL
	 */
	private void performSearch() {
		// Clear any results that are being displayed
		clearResult();

		// Get the text entered by the user
		// EditText text = (EditText) findViewById(R.id.Name);
		// Editable name = text.getText();
		//
		// // Hide the keyboard
		// InputMethodManager imm = (InputMethodManager)
		// getSystemService(Context.INPUT_METHOD_SERVICE);
		// imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
		// performSearch(name.toString());
	}

	private void performSearch(String name) {
		latestSearchTerm = name;
		// Save search in recent list
		final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
				this, RecentGALSearchTermsProvider.AUTHORITY,
				RecentGALSearchTermsProvider.MODE);
		suggestions.saveRecentQuery(name, null);

		// Launch the progress bar, so the user knows his request is being
		// processed
		progressdialog.setMessage(getString(R.string.retrievingResults));
		progressdialog.show();

		// Retrieve the results via an AsyncTask
		new GALSearch().execute(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * 
	 * Implement the OnClickListener callback for the Go button
	 */
	@Override
	public void onClick(View v) {
		performSearch();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		// Get the SearchView and set the searchable configuration for Honeycomb
		// and above
		if (!Utility.isPreHoneycomb()) {
			final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			final ComponentName component = getComponentName();
			final SearchableInfo searchableInfo = searchManager
					.getSearchableInfo(component);
			final SearchView searchView = (SearchView) menu.findItem(
					R.id.menu_search).getActionView();
			searchView.setSearchableInfo(searchableInfo);
		}

		return super.onCreateOptionsMenu(menu);
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
		case R.id.menu_search:
			return this.onSearchRequested();
		case R.id.settings:
			showConfiguration();
			return true;
		case R.id.clear:
			clearResult();
			// EditText text = (EditText) findViewById(R.id.Name);
			// text.setText("");
			return true;
			/*
			 * case R.id.debug: Debug.sendDebugEmail(this); return true;
			 */
		case R.id.clearSearchHistory:
			final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
					this, RecentGALSearchTermsProvider.AUTHORITY,
					RecentGALSearchTermsProvider.MODE);
			suggestions.clearHistory();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Launches the preferences activity
	 */
	public void showConfiguration() {
		final Intent myIntent = new Intent();
		myIntent.setClassName("net.vivekiyer.GAL",
				"net.vivekiyer.GAL.Configure");
		startActivityForResult(myIntent, DISPLAY_CONFIGURATION_REQUEST);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 * 
	 * This method gets called after the launched activity is closed This
	 * application needs to handle the closing of two activity launches - The
	 * preference pane - The config pane
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case DISPLAY_CONFIGURATION_REQUEST:
			// Initialize the activesync object with the validated settings
			if (!loadPreferences()) {
				final Intent myIntent = new Intent();
				myIntent.setClassName("net.vivekiyer.GAL",
						"net.vivekiyer.GAL.Configure");
				startActivityForResult(myIntent, DISPLAY_CONFIGURATION_REQUEST);
			}
			break;
		}
	}

	/**
	 * The older version of the application included http and https in the
	 * server name. The newer version no longer has this. Hence clean up is
	 * required
	 */
	private void cleanUpServerName() {
		String serverName = mPreferences.getString(
				Configure.KEY_SERVER_PREFERENCE, "");
		serverName = serverName.toLowerCase();

		if (serverName.startsWith("https://")) {
			final SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean(Configure.KEY_USE_SSL, true);
			serverName = serverName.substring(8);
			editor.putString(Configure.KEY_SERVER_PREFERENCE, serverName);
			editor.commit();
		} else if (serverName.startsWith("http://")) {
			final SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean(Configure.KEY_USE_SSL, false);
			serverName = serverName.substring(7);
			editor.putString(Configure.KEY_SERVER_PREFERENCE, serverName);
			editor.commit();
		}

		activeSyncManager.setServerName(serverName);
	}

	/**
	 * @param xml
	 *            The XML to parse for contacts
	 * @return List of contacts tagged with the Display name
	 * @throws Exception
	 * 
	 *             This method parses an XML containing a list of contacts and
	 *             returns a hashtable containing the contacts in the XML
	 *             indexed by the DisplayName of the contacts
	 */
	public int parseXML(String xml) throws Exception {
		// Our parser does not handle ampersands too well. So replace these with
		// &amp;
		xml = xml.replaceAll("&", "&amp;");

		// Parse the XML
		final ByteArrayInputStream xmlParseInputStream = new ByteArrayInputStream(
				xml.toString().getBytes());
		final XMLReader xr = XMLReaderFactory.createXMLReader();

		XMLParser parser = null;
		parser = new XMLParser();
		xr.setContentHandler(parser);
		xr.parse(new InputSource(xmlParseInputStream));
		mContacts = parser.getContacts();
		return parser.getStatus();
	}

	/**
	 * Reads the stored preferences and initializes the
	 * 
	 * @return True if a valid Exchange server settings was saved during the
	 *         previous launch. False otherwise *
	 * 
	 */
	public boolean loadPreferences() {
		activeSyncManager.setmUsername(mPreferences.getString(
				Configure.KEY_USERNAME_PREFERENCE, ""));
		activeSyncManager.setPassword(mPreferences.getString(
				Configure.KEY_PASSWORD_PREFERENCE, ""));
		activeSyncManager.setDomain(mPreferences.getString(
				Configure.KEY_DOMAIN_PREFERENCE, ""));

		// Clean up server name from previous version of the app
		cleanUpServerName();

		activeSyncManager.setActiveSyncVersion(mPreferences.getString(
				Configure.KEY_ACTIVESYNCVERSION_PREFERENCE, ""));
		activeSyncManager.setPolicyKey(mPreferences.getString(
				Configure.KEY_POLICY_KEY_PREFERENCE, ""));
		activeSyncManager.setAcceptAllCerts(mPreferences.getBoolean(
				Configure.KEY_ACCEPT_ALL_CERTS, true));
		activeSyncManager.setUseSSL(mPreferences.getBoolean(
				Configure.KEY_USE_SSL, true));
		activeSyncManager.setDeviceId(mPreferences.getInt(
				Configure.KEY_DEVICE_ID, 0));

		if (activeSyncManager.Initialize() == false)
			return false;

		// Check to see if we have successfully connected to an Exchange server
		if (activeSyncManager.getActiveSyncVersion().equalsIgnoreCase(""))
			return false;

		// If we connected fine, load the last set of results
		try {
			searchResultXML = mPreferences.getString(
					Configure.KEY_RESULTS_PREFERENCE, "");
			if (searchResultXML.equalsIgnoreCase(""))
				return true;

			parseXML(searchResultXML);

			// EditText text = (EditText) findViewById(R.id.Name);
			// text.setText(mPreferences.getString(Configure.KEY_SEARCH_TERM_PREFERENCE,
			// "" ));

			displayResult();

			// Hide the keyboard
			// InputMethodManager imm = (InputMethodManager)
			// getSystemService(Context.INPUT_METHOD_SERVICE);
			// imm.hideSoftInputFromWindow(text.getWindowToken(), 0);

		} catch (final Exception e) {
			Toast.makeText(CorporateAddressBook.this, e.toString(),
					Toast.LENGTH_LONG).show();
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 * 
	 * Called when the application is closed
	 */
	@Override
	protected void onStop() {
		super.onStop();

		// Make sure that the activesync version and policy key get written
		// to the preferences
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(Configure.KEY_ACTIVESYNCVERSION_PREFERENCE,
				activeSyncManager.getActiveSyncVersion());
		editor.putInt(Configure.KEY_DEVICE_ID, activeSyncManager.getDeviceId());
		editor.putString(Configure.KEY_POLICY_KEY_PREFERENCE,
				activeSyncManager.getPolicyKey());
		editor.putString(Configure.KEY_RESULTS_PREFERENCE, searchResultXML);
		// EditText text = (EditText) findViewById(R.id.Name);
		editor.putString(Configure.KEY_SEARCH_TERM_PREFERENCE, latestSearchTerm);

		// Commit the edits!
		editor.commit();
	}

	/**
	 * Displays the search results in the Listview
	 */
	private void displayResult() {
		
		TextView tv = (TextView) this.findViewById(R.id.resultheader);
		if(this.latestSearchTerm == null)
			tv.setText("Last search produced " + mContacts.size() + " results");
		else
			tv.setText("Found " + mContacts.size() + " results for '" + this.latestSearchTerm + "'");
		
		// Get the result and sort the alphabetically
		contactList = new Contact[mContacts.size()];
		
		int i = 0;
		for (final Enumeration<Contact> e = mContacts.elements(); e
				.hasMoreElements();) {
			contactList[i++] = e.nextElement();
		}

		Arrays.sort(contactList);

		// Create a new array adapter and add the result to this
		final ContactListAdapter listadapter = new ContactListAdapter(
				CorporateAddressBook.this, R.layout.contact_row,
				contactList);

		lv1.setAdapter(listadapter);
		lv1.setOnItemClickListener(mListViewListener);
	}

	/**
	 * Clear the results from the listview
	 */
	private void clearResult() {
		contactList = new Contact[0];

		// Create a new array adapter and add the result to this
		final ContactListAdapter listadapter = new ContactListAdapter(
				CorporateAddressBook.this, R.layout.contact_row,
				contactList);

		lv1.setAdapter(listadapter);
		TextView v = (TextView) findViewById(R.id.resultheader);
		v.setText(R.string.EnterSearchTerm);
	}

	/**
	 * @author Vivek Iyer
	 * 
	 *         This class is responsible for executing the search on the GAL
	 *         This derives from AsyncTask since we want to make sure that we do
	 *         not block the main UI thread, thus making the application
	 *         unresponsive
	 */
	class GALSearch extends AsyncTask<String, Void, Boolean> {

		private String errorMesg = "";

		private int errorCode = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 * 
		 * The method that searches the GAL
		 */
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				// Search the GAL
				mContacts = null;

				final StringBuffer sb = new StringBuffer();
				int statusCode = 0;

				do {
					statusCode = activeSyncManager.searchGAL(params[0], sb);
					switch (statusCode) {
					case 200: // HTTP_OK
						// All went well, lets display the result
						searchResultXML = sb.toString();
						statusCode = parseXML(searchResultXML);
						break;
					case 449: // RETRY AFTER PROVISIONING
					case 142: // RETRY AFTER PROVISIONING
						// Looks like we need to provision again
						activeSyncManager.provisionDevice();
						break;
					case 401: // UNAUTHORIZED
						// Looks like the password expired
						errorCode = 401;
						errorMesg = "Authentication failed. Please check your credentials";
						return false;
					default:
						errorCode = statusCode;
						errorMesg = "Exchange server rejected request with error:"
								+ errorCode;
						return false;
					}
				} while (statusCode != 200);

			} catch (final Exception e) {
				if (Debug.Enabled) {
					Debug.Log(e.toString());
					Debug.Log(searchResultXML);
				} else {
					errorMesg = "Activesync version= "
							+ activeSyncManager.getActiveSyncVersion() + "\n"
							+ e.toString();
					return false;
				}
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 * 
		 * This method displays the retrieved results in a list view
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			progressdialog.dismiss();

			if (mContacts == null) {
				Toast.makeText(CorporateAddressBook.this, errorMesg,
						Toast.LENGTH_LONG).show();

				// Check if the password did not validate
				if (errorCode == 401)
					showConfiguration();

				// If this is not a 401 error, send a debug email
				else if (Debug.Enabled)
					Debug.sendDebugEmail(CorporateAddressBook.this);

				return;
			}

			switch (mContacts.size()) {
			case 0:
				Toast.makeText(CorporateAddressBook.this, "No matches found",
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				// Create a parcel with the associated contact object
				// This parcel is used to send data to the activity
				final Bundle b = new Bundle();
				final Contact c = (Contact) mContacts.values().toArray()[0];
				b.putParcelable("net.vivekiyer.GAL", c);

				final Intent intent = new Intent(CorporateAddressBook.this,
						CorporateContactRecord.class);
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