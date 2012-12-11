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

import android.accounts.*;
import android.annotation.TargetApi;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;
import com.google.common.collect.HashMultimap;
import net.vivekiyer.GAL.ChoiceDialogFragment.OnChoiceDialogOptionClickListener;
import net.vivekiyer.GAL.CorporateAddressBookFragment.ContactListListener;
import net.vivekiyer.GAL.Preferences.ConnectionChecker;
import net.vivekiyer.GAL.Preferences.PrefsActivity;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * @author Vivek Iyer
 *         <p/>
 *         This class is the main entry point to the application
 */
public class CorporateAddressBook extends FragmentActivity
		implements ContactListListener, GALSearch.OnSearchCompletedListener, OnChoiceDialogOptionClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

	private String accountName;

	class AccountResult {

		public static final int OpCanceled = 1;
		public static final int AuthEx = 2;
		public static final int IOEx = 3;
		public int error = 0;

		public String accountName;
		public String errorDescription;
	}
	// TAG used for logging

	// Object that performs all the ActiveSync magic
	private ActiveSyncManager activeSyncManager;

	// Preference object that stores the account credentials
	private SharedPreferences mPreferences;

	// Used to launch the preference pane
	static final int DISPLAY_PREFERENCES_REQUEST = 0;

	// Used to launch the initial configuration pane
	public static final int DISPLAY_CONFIGURATION_REQUEST = 2;

	// Tags for finding and retrieving fragments
	static final String mainTag = "R.id.main_fragment"; //$NON-NLS-1$

	static final String contactTag = "R.id.contact_fragment"; //$NON-NLS-1$
	static final String CONTACTS = "mContacts"; //$NON-NLS-1$
	static final String SEARCH_TERM = "latestSearchTerm"; //$NON-NLS-1$
	static final String SELECTED_CONTACT = "selectedContact"; //$NON-NLS-1$
	static final String ONGOING_SEARCH = "search";  //$NON-NLS-1$
	// Progress bar
	private ProgressDialog progressdialog;

	// Last search term
	private String latestSearchTerm;

	private SearchView searchView;

	// TAG used for logging

	// Stores the list of contacts returned
	private HashMultimap<String, Contact> mContacts;

	private Contact selectedContact;

	private GALSearch search;

	private boolean isPaused = false;

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
		setContentView(R.layout.main_fragment);

		// Create the progress bar
		progressdialog = new ProgressDialog(this);
		progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog.setCancelable(false);

		// Turn keystrokes into search
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		loadPreferences();

		// Get the intent, verify the action and get the query
		// but not if the activity is being recreated (would cause a new search)
		if (savedInstanceState == null || !savedInstanceState.containsKey("mContacts")) { //$NON-NLS-1$
			final Intent intent = getIntent();
			onNewIntent(intent);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		super.onNewIntent(intent);
		setIntent(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			performSearch(query);
		}
	}

	// Assist user by showing search box whenever returning
	@Override
	protected void onStart() {
		super.onStart();

		FragmentManager fm = getSupportFragmentManager();
		CorporateContactRecordFragment details = (CorporateContactRecordFragment) fm
				.findFragmentById(R.id.contact_fragment);

		if (details != null && details.isInLayout()) {
			CorporateAddressBookFragment contacts = (CorporateAddressBookFragment) fm
					.findFragmentById(R.id.main_fragment);
			contacts.setIsSelectable(true);
			contacts.setViewBackground(false);
			FragmentTransaction ft = fm.beginTransaction();
			ft.hide(details);
			ft.commit();
		}

		final Intent intent = getIntent();
		if (intent != null) {
			final Set<String> categories = intent.getCategories();
			if ((categories != null)
					&& categories.contains(Intent.CATEGORY_LAUNCHER)) {
				this.onSearchRequested();
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (savedInstanceState != null && savedInstanceState.containsKey(CONTACTS)) {
			mContacts = (HashMultimap<String, Contact>) savedInstanceState.get(CONTACTS);
			String latestSearchTerm = savedInstanceState.getString(SEARCH_TERM);
			selectedContact = (Contact) savedInstanceState.get(SELECTED_CONTACT);
			displaySearchResult(mContacts, latestSearchTerm);
			if (selectedContact != null) {
				selectContact(selectedContact);
			}
			Integer searchHash = savedInstanceState.getInt(ONGOING_SEARCH);
			if (searchHash != 0) {
				search = App.taskManager.get(searchHash);
				if (search != null) {
					search.setOnSearchCompletedListener(this);
					App.taskManager.remove(searchHash);
					if (progressdialog != null) {
						progressdialog.setMessage(getString(R.string.retrievingResults));
						progressdialog.show();
					}
				}
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isPaused = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		isPaused = false;
	}

	private void selectContact(Contact selectedContact) {
		CorporateAddressBookFragment contacts = (CorporateAddressBookFragment) getSupportFragmentManager()
				.findFragmentById(R.id.main_fragment);
		contacts.setSelectedContact(selectedContact);
		onContactSelected(selectedContact);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(CONTACTS, this.mContacts);
		outState.putString(SEARCH_TERM, this.latestSearchTerm);
		outState.putSerializable(SELECTED_CONTACT, this.selectedContact);
		if (search != null && search.getStatus().equals(AsyncTask.Status.RUNNING)) {
			outState.putInt(ONGOING_SEARCH, search.hashCode());
			App.taskManager.put(search.hashCode(), search);
		}
	}


	private void performSearch(String name) {

		if (progressdialog != null) {
			progressdialog.setMessage(getString(R.string.retrievingResults));
			progressdialog.show();
		}

		// Save search in recent list
		final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
				this, RecentGALSearchTermsProvider.AUTHORITY,
				RecentGALSearchTermsProvider.MODE);
		suggestions.saveRecentQuery(name, null);

		search = new GALSearch(activeSyncManager);
		search.onSearchCompletedListener = this;
		search.execute(name);
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
			MenuItem item = menu.findItem(
					R.id.menu_search);
			searchView = (SearchView) item.getActionView();
			searchView.setSearchableInfo(searchableInfo);

			//this.onSearchRequested();
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
				CorporateAddressBook.showConfiguration(this);
				return true;
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
	public static void showConfiguration(FragmentActivity parentActivity) {
//		final Intent myIntent = new Intent();
//		myIntent.setClassName("net.vivekiyer.GAL",
//				"net.vivekiyer.GAL.Configure");
//		parentActivity.startActivityForResult(myIntent, DISPLAY_CONFIGURATION_REQUEST);
		parentActivity.startActivityForResult(new Intent(parentActivity, PrefsActivity.class), DISPLAY_CONFIGURATION_REQUEST);
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
				loadPreferences();
				break;
		}
	}

	/**
	 * The older displayText of the application included http and https in the
	 * server name. The newer displayText no longer has this. Hence clean up is
	 * required
	 */
	private String cleanUpServerName(SharedPreferences prefs) {
		String serverName = prefs.getString(
				getString(R.string.PREFS_KEY_SERVER_PREFERENCE), ""); //$NON-NLS-1$
		serverName = serverName.toLowerCase(Locale.getDefault());

		if (serverName.startsWith("https://")) { //$NON-NLS-1$
			final SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(getString(R.string.PREFS_KEY_USE_SSL), true);
			serverName = serverName.substring(8);
			editor.putString(getString(R.string.PREFS_KEY_SERVER_PREFERENCE), serverName);
			editor.commit();
		} else if (serverName.startsWith("http://")) { //$NON-NLS-1$
			final SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(getString(R.string.PREFS_KEY_USE_SSL), false);
			serverName = serverName.substring(7);
			editor.putString(getString(R.string.PREFS_KEY_SERVER_PREFERENCE), serverName);
			editor.commit();
		}

		return serverName;
	}

	protected boolean migrateServerSettings() {
		// TODO: Make sure old settings are migrated to new account structure
		return false;
	}

	// private static String TAG = "CorporateAddressBook";
	// private static String TAG = "CorporateAddressBook";
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		//To change body of implemented methods use File | Settings | File Templates.
		if(!isPaused){
			if(accountName == null || sharedPreferences.getAll().size() < 6)
				loadPreferences();
			else
				loadPreferences(accountName);
		}
	}
	/**
	 * Reads the stored preferences and initializes the
	 *
	 * @return True if a valid Exchange server settings was saved during the
	 *         previous launch. False otherwise *
	 */
	public void loadPreferences() {

		// Initialize preferences and the activesyncmanager
		final AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));
		if (accounts == null || accounts.length == 0) {
			final SharedPreferences existingPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			final String userName = existingPrefs.getString(getString(R.string.PREFS_KEY_USERNAME_PREFERENCE), null);
			if(userName == null) {
				addAccount(progressdialog, am);
			}
			else {
				progressdialog.setMessage("Migrating settings...");
				progressdialog.show();
				final ActiveSyncManager syncManager = new ActiveSyncManager();
				AsyncTask migrateTask =new AsyncTask() {
					@Override
					protected Object doInBackground(Object... params) {
						SharedPreferences thesePrefs = (SharedPreferences) params[0];
						ActiveSyncManager syncManager = (ActiveSyncManager) params[1];
						return loadPreferences(thesePrefs, syncManager);
					}

					@Override
					protected void onPostExecute(Object o) {
						if(o instanceof Boolean && ((Boolean) o)) {
							migrateConfiguration(am, existingPrefs, userName, syncManager);
						}
						else {
							if(progressdialog != null && progressdialog.isShowing()) {
								try {
									progressdialog.dismiss();
								}
								catch(IllegalArgumentException e) {}
							}
							addAccount(progressdialog, am);
						}
					}
				};
				migrateTask.execute(existingPrefs, syncManager);
			}
		} else {
			accountName = accounts[0].name;

			if (accountName == null || accountName.isEmpty()) {
				throw new RuntimeException("No account available");
			}
			if (!loadPreferences(accountName)) {
				showConfiguration(this);
			}
		}
	}

	private void migrateConfiguration(AccountManager am, SharedPreferences existingPrefs, String userName, ActiveSyncManager syncManager) {
		if(loadPreferences(existingPrefs, syncManager)) {
			try {

				SharedPreferences newPrefs = getSharedPreferences(userName, MODE_PRIVATE);
				SharedPreferences.Editor editor = newPrefs.edit();
				editor.putString(getString(R.string.PREFS_KEY_SERVER_PREFERENCE),
						existingPrefs.getString(getString(R.string.PREFS_KEY_SERVER_PREFERENCE), null));
				editor.putString(getString(R.string.PREFS_KEY_DOMAIN_PREFERENCE),
						existingPrefs.getString(getString(R.string.PREFS_KEY_DOMAIN_PREFERENCE), null));
				editor.putString(getString(R.string.PREFS_KEY_USERNAME_PREFERENCE),
						existingPrefs.getString(getString(R.string.PREFS_KEY_USERNAME_PREFERENCE), null));
				editor.putString(getString(R.string.PREFS_KEY_PASSWORD_PREFERENCE),
						existingPrefs.getString(getString(R.string.PREFS_KEY_PASSWORD_PREFERENCE), null));
				editor.putBoolean(getString(R.string.PREFS_KEY_USE_SSL),
						existingPrefs.getBoolean(getString(R.string.PREFS_KEY_USE_SSL), true));
				editor.putBoolean(getString(R.string.PREFS_KEY_ACCEPT_ALL_CERTS),
						existingPrefs.getBoolean(getString(R.string.PREFS_KEY_ACCEPT_ALL_CERTS), true));
				editor.putString(getString(R.string.PREFS_KEY_ACTIVESYNCVERSION_PREFERENCE),
						syncManager.getActiveSyncVersion());
				editor.putString(getString(R.string.PREFS_KEY_DEVICE_ID_STRING),
						syncManager.getDeviceId());
				editor.putString(getString(R.string.PREFS_KEY_POLICY_KEY_PREFERENCE),
						syncManager.getPolicyKey());
				editor.putBoolean(getString(R.string.PREFS_KEY_SUCCESSFULLY_CONNECTED),
						true);

				// Commit the edits!
				editor.commit();

				// Pass the values to the account manager
				Account account = new Account(userName,
						getString(R.string.ACCOUNT_TYPE));
				boolean accountCreated = am.addAccountExplicitly(account,
						existingPrefs.getString(getString(R.string.PREFS_KEY_PASSWORD_PREFERENCE), ""), null);

				if(accountCreated) {
					loadPreferences(userName);
				}
				else {
					addAccount(progressdialog, am);
				}
			}
			catch(Exception e)
			{
				addAccount(progressdialog, am);
			}
		}
		else {
			addAccount(progressdialog, am);
		}
		if(progressdialog != null && progressdialog.isShowing()) {
			try {
				progressdialog.dismiss();
			}
			catch(IllegalArgumentException e) {}
		}
	}

	private void addAccount(final ProgressDialog prog, AccountManager acc) {
//		final AccountManager am = acc == null ? AccountManager.get(this) : acc;
//		final ProgressDialog dialog = prog == null ? new ProgressDialog(this) : prog;

		AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				String accountName;
				try {
					accountName = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);
					loadPreferences(accountName);
				} catch (OperationCanceledException e) {
				} catch (IOException e) {
				} catch (AuthenticatorException e) {
				}
			}
		};

		AccountManagerFuture<Bundle> future = acc.addAccount(getString(R.string.ACCOUNT_TYPE), null, null, null, this, callback, null);

	}

	/**
	 * Reads the stored preferences and initializes the
	 *
	 * @param accountKey
	 * @return True if a valid Exchange server settings was saved during the
	 *         previous launch. False otherwise *
	 */
	public boolean loadPreferences(String accountKey) {
		this.accountName = accountKey;
		SharedPreferences thesePrefs = getSharedPreferences(accountKey, MODE_PRIVATE);
		activeSyncManager = new ActiveSyncManager();

		mPreferences = thesePrefs;
		if(thesePrefs.getAll().size() < 6)
			throw new RuntimeException("Server settings incomplete");

		thesePrefs.registerOnSharedPreferenceChangeListener(this);

		return loadPreferences(thesePrefs, activeSyncManager);
	}

	private boolean loadPreferences(SharedPreferences thesePrefs, ActiveSyncManager syncManager) {
		syncManager.setUsername(thesePrefs.getString(
				getString(R.string.PREFS_KEY_USERNAME_PREFERENCE), "")); //$NON-NLS-1$
		syncManager.setPassword(thesePrefs.getString(
				getString(R.string.PREFS_KEY_PASSWORD_PREFERENCE), "")); //$NON-NLS-1$
		syncManager.setDomain(thesePrefs.getString(
				getString(R.string.PREFS_KEY_DOMAIN_PREFERENCE), "")); //$NON-NLS-1$

		// Clean up server name from previous displayText of the app
		syncManager.setServerName(cleanUpServerName(thesePrefs));

		syncManager.setActiveSyncVersion(thesePrefs.getString(
				getString(R.string.PREFS_KEY_ACTIVESYNCVERSION_PREFERENCE), "")); //$NON-NLS-1$
		syncManager.setPolicyKey(thesePrefs.getString(
				getString(R.string.PREFS_KEY_POLICY_KEY_PREFERENCE), "")); //$NON-NLS-1$
		syncManager.setAcceptAllCerts(thesePrefs.getBoolean(
				getString(R.string.PREFS_KEY_ACCEPT_ALL_CERTS), true));
		syncManager.setUseSSL(thesePrefs.getBoolean(
				getString(R.string.PREFS_KEY_USE_SSL), true));

		// Fix for null device_id
		String device_id_string = thesePrefs.getString(getString(R.string.PREFS_KEY_DEVICE_ID_STRING), null);
		if (device_id_string == null) {
			int device_id = thesePrefs.getInt(
					getString(R.string.PREFS_KEY_DEVICE_ID), 0);
			if (device_id > 0)
				device_id_string = String.valueOf(device_id);
			else
				device_id_string = ActiveSyncManager.getUniqueId();
		}

		syncManager.setDeviceId(device_id_string);

		if (!syncManager.Initialize())
			return false;

//		// Fix for null device_id
//		if(device_id == 0)
//			return false;

		// Check to see if we have successfully connected to an Exchange server
		// Do we have a previous successful connect with these settings?
		if (!thesePrefs.getBoolean(getString(R.string.PREFS_KEY_SUCCESSFULLY_CONNECTED), false)) {
			// If not, let's try
			if (activeSyncManager.getActiveSyncVersion().equalsIgnoreCase("")) { //$NON-NLS-1$
				// If we fail, let's return
				return false;
			} else {
				// In case of success, let's make a record of this so that
				// we don't have to check the settings every time we launch.
				// This record will be reset when any change is made to the
				// settings
				SharedPreferences.Editor editor = thesePrefs.edit();
				editor.putBoolean(getString(R.string.PREFS_KEY_SUCCESSFULLY_CONNECTED), true);
				editor.commit();
			}
		}

		return true;
	}

	@TargetApi(11)
	private void displaySearchResult(HashMultimap<String, Contact> contacts, String searchTerm) {

		this.mContacts = contacts;
		this.latestSearchTerm = searchTerm;

		final FragmentManager fragmentManager = getSupportFragmentManager();

		CorporateAddressBookFragment list = (CorporateAddressBookFragment) fragmentManager
				.findFragmentById(R.id.main_fragment);
		if (list == null) {
			Debug.Log("List fragment missing from main activity, discarding search result"); //$NON-NLS-1$
			return;
		}
		list.displayResult(mContacts, latestSearchTerm);

		resetAndHideDetails(fragmentManager);
		if (!Utility.isPreHoneycomb() && (searchView != null))
			searchView.setQuery("", false); //$NON-NLS-1$
		list.getView().requestFocus();
	}

	private void resetAndHideDetails(final FragmentManager fragmentManager) {

		CorporateAddressBookFragment list = (CorporateAddressBookFragment) fragmentManager
				.findFragmentById(R.id.main_fragment);

		CorporateContactRecordFragment details = (CorporateContactRecordFragment) fragmentManager
				.findFragmentById(R.id.contact_fragment);

		if (details != null && details.isInLayout() && !this.isPaused) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			//ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

			// Below does not work since it clears the detail fragment before anim starts,
			// making it look rather weird. Better off w/o anims, unfortunately.
			//ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
			ft.hide(details);
			ft.commit();
			fragmentManager.executePendingTransactions();
			details.clear();
		}

		list.setViewBackground(false);
		selectedContact = null;
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

		// Make sure that the activesync displayText and policy key get written
		// to the preferences
		if(mPreferences != null) {
			final SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString(getString(R.string.PREFS_KEY_ACTIVESYNCVERSION_PREFERENCE),
					activeSyncManager.getActiveSyncVersion());
			editor.putString(getString(R.string.PREFS_KEY_DEVICE_ID_STRING), activeSyncManager.getDeviceId());
			editor.putString(getString(R.string.PREFS_KEY_POLICY_KEY_PREFERENCE),
					activeSyncManager.getPolicyKey());

			// Commit the edits!
			editor.commit();
		}
	}

	@Override
	protected void onDestroy() {
		if ((progressdialog != null) && progressdialog.isShowing()) {
			try {
				progressdialog.dismiss();
			} catch (java.lang.IllegalArgumentException e) {
			}
		}
		if (search != null)
			search.setOnSearchCompletedListener(null);
		super.onDestroy();
	}

	;

	@Override
	public void onContactSelected(Contact contact) {
		// Create a parcel with the associated contact object
		// This parcel is used to send data to the activity

		this.selectedContact = contact;

		final FragmentManager fragmentManager = getSupportFragmentManager();

		CorporateContactRecordFragment details = (CorporateContactRecordFragment) fragmentManager
				.findFragmentById(R.id.contact_fragment);

		if (details == null || !details.isInLayout()) {
			final Bundle b = new Bundle();
			b.putParcelable("net.vivekiyer.GAL", selectedContact); //$NON-NLS-1$

			// Launch the activity
			final Intent myIntent = new Intent();
			myIntent.setClassName("net.vivekiyer.GAL", //$NON-NLS-1$
					"net.vivekiyer.GAL.CorporateContactRecord"); //$NON-NLS-1$

			myIntent.putExtras(b);
			startActivity(myIntent);
		} else {
			CorporateAddressBookFragment list = (CorporateAddressBookFragment) fragmentManager
					.findFragmentById(R.id.main_fragment);
			list.setViewBackground(true);

			details.setContact(selectedContact);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			//ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			// Below does not work since it resizes the accountName fragment before anim starts,
			// making it look rather weird. Better off w/o anims, unfortunately.
			//ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
			ft.show(details);
			ft.commit();
		}
	}

	@Override
	public void onSearchCleared() {
		resetAndHideDetails(getSupportFragmentManager());
	}

	@TargetApi(11)
	@Override
	public boolean onSearchRequested() {
		if (!Utility.isPreHoneycomb()) {
			if (searchView != null) {
				searchView.setFocusable(true);
				searchView.setIconified(false);
				searchView.requestFocusFromTouch();
				return true;
			} else {
				Debug.Log("Running HC+ without SearchView"); //$NON-NLS-1$
				return false;
			}
		}
		return super.onSearchRequested();
	}

	@Override
	public void OnSearchCompleted(int result,
	                              GALSearch search) {
		if ((progressdialog != null) && progressdialog.isShowing()) {
			try {
				progressdialog.dismiss();
			} catch (java.lang.IllegalArgumentException e) {
			}
		}
		if (result == 0) {
			displaySearchResult(search.getContacts(), search.getSearchTerm());
			return;
		}
		ChoiceDialogFragment dialogFragment;
		String title = search.getErrorMesg();
		String message = search.getErrorDetail();
		String positiveButtonText;
		String negativeButtonText;
		switch (result) {
			// Errors that might be remedied by updating server settings
			case 401:
			case ActiveSyncManager.ERROR_UNABLE_TO_REPROVISION:
			case ConnectionChecker.TIMEOUT:
				positiveButtonText = getString(R.string.show_settings);
				negativeButtonText = getString(android.R.string.cancel);
				dialogFragment = ChoiceDialogFragment.newInstance(title, message, positiveButtonText, negativeButtonText, DISPLAY_CONFIGURATION_REQUEST, android.R.id.closeButton);
				dialogFragment.setListener(this);
				try {
					dialogFragment.show(getSupportFragmentManager(), "reprovision"); //$NON-NLS-1$
				} catch (java.lang.IllegalStateException e) {
					Debug.Log(e.getMessage());
				}
				break;
			// Errors that depend on external (non app-related) circumstances
			case 403:
				positiveButtonText = getString(android.R.string.ok);
				negativeButtonText = getString(android.R.string.copy);
				dialogFragment = ChoiceDialogFragment.newInstance(title, message, positiveButtonText, negativeButtonText, android.R.id.closeButton, android.R.id.copy);
				dialogFragment.setListener(this);
				try {
					dialogFragment.show(getSupportFragmentManager(), "unauthorized"); //$NON-NLS-1$
				} catch (java.lang.IllegalStateException e) {
					Debug.Log(e.getMessage());
				}
				break;
			default:
				dialogFragment = ChoiceDialogFragment.newInstance(title, message);
				try {
					dialogFragment.show(getSupportFragmentManager(), "ContinueFragTag"); //$NON-NLS-1$
				} catch (java.lang.IllegalStateException e) {
					Debug.Log(e.getMessage());
				}
				break;
		}
	}

	public void onChoiceDialogOptionPressed(int action) {
		if (action == DISPLAY_CONFIGURATION_REQUEST)
			showConfiguration(this);
	}
}