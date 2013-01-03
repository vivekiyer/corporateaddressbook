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

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import net.vivekiyer.GAL.ChoiceDialogFragment.OnChoiceDialogOptionClickListener;
import net.vivekiyer.GAL.CorporateAddressBookFragment.ContactListListener;
import net.vivekiyer.GAL.Preferences.ConnectionChecker;
import net.vivekiyer.GAL.Preferences.PrefsActivity;

import java.util.HashMap;
import java.util.Set;

/**
 * @author Vivek Iyer
 *         <p/>
 *         This class is the main entry point to the application
 */
public class CorporateAddressBook extends SherlockFragmentActivity
		implements ContactListListener, GALSearch.OnSearchCompletedListener, OnChoiceDialogOptionClickListener,
		ActionBar.OnNavigationListener, AccountManager.OnAccountsChangedListener {

	private boolean listeningToAccountChanges = false;

	class AccountResult {

		public static final int OpCanceled = 1;

		public static final int IOEx = 3;

		public int error = 0;
		public String accountName;
		public String errorDescription;

	}

	// Object thatboolean performs all the ActiveSync magic
	private ActiveSyncManager activeSyncManager;

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
	static final String ACCOUNT_KEY = "accountKey";  //$NON-NLS-1$
	static final String START_WITH = "startWith";  //$NON-NLS-1$
	static final String REQUERY = "requery_previous";

	// Progress bar
//	private ProgressDialog progressdialog;
	// Last search term
	private String latestSearchTerm;
	private View searchView;

	// TAG used for logging

	// Stores the list of contacts returned
	private HashMap<String, Contact> mContacts;
	private Contact selectedContact;
	private GALSearch search;

	public static final int AuthEx = 2;

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

		// Turn keystrokes into search
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		initializeActionBar();

		// Get the intent, verify the action and get the query
		// but not if the activity is being recreated (would cause a new search)
		if (savedInstanceState == null || !savedInstanceState.containsKey("mContacts")) { //$NON-NLS-1$
			final Intent intent = getIntent();
			onNewIntent(intent);
		}
	}

	private void initializeActionBar() {
		if (!listeningToAccountChanges) {
			App.getAccounts().addChangeListener(this);
			listeningToAccountChanges = true;
		}

		if(!App.getAccounts().Initialize(this))
			return;
		final ActionBar actionBar = getSupportActionBar();

		if (App.getAccounts().size() >= 2) {
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

			SpinnerAdapter adapter = new ArrayAdapter<ActiveSyncManager>(getBaseContext(), R.layout.sherlock_spinner_dropdown_item, App.getAccounts());
			actionBar.setListNavigationCallbacks(adapter, this);

			if (activeSyncManager == null || !App.getAccounts().contains(activeSyncManager)) {
				if ((activeSyncManager = App.getAccounts().getDefaultAccount()) == null) {
					activeSyncManager = App.getAccounts().get(actionBar.getSelectedNavigationIndex());
				} else
					actionBar.setSelectedNavigationItem(App.getAccounts().indexOf(activeSyncManager));
			} else
				actionBar.setSelectedNavigationItem(App.getAccounts().indexOf(activeSyncManager));
		} else {
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setTitle(getString(R.string.app_name));
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			if (App.getAccounts().isEmpty()) {
				showConfiguration(this);
			} else {
				activeSyncManager = App.getAccounts().get(0);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		super.onNewIntent(intent);
		setIntent(intent);
		// Did we get a SEARCH intent?
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// First: is this to continue search
			if (intent.getBooleanExtra(REQUERY, false)) {
				getNextSearchResult();
				return;
			}

			final String query = intent.getStringExtra(SearchManager.QUERY);
//			// Get first result to get (if not 0)
//			int firstResult = intent.getIntExtra(START_WITH, 0);
//
//			ActiveSyncManager syncManager = activeSyncManager;
//
//			// Did we get an account key? If so, use that ActiveSyncManager
//			String accountKey = intent.getStringExtra(ACCOUNT_KEY);
//			if(accountKey != null && App.getAccounts().hasKey(accountKey)) {
//				syncManager = App.getAccounts().get(accountKey);
//			}
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
			mContacts = (HashMap<String, Contact>) savedInstanceState.get(CONTACTS);
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
					CorporateAddressBookFragment frag = (CorporateAddressBookFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
					frag.setHeader(getString(R.string.retrievingResults), true);
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
		performSearch(name, this.activeSyncManager, 0, true);
	}

	void getNextSearchResult() {
		performSearch(latestSearchTerm, activeSyncManager, mContacts.size(), false);
	}

	private void performSearch(String name, ActiveSyncManager syncManager, int startWith, boolean clearResults) {

//		if (progressdialog != null) {
//			progressdialog.setMessage(getString(R.string.retrievingResults));
//			progressdialog.show();
//		}
		if (clearResults) {
			CorporateAddressBookFragment frag = (CorporateAddressBookFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
			frag.setHeader(getString(R.string.retrievingResults), true);
		}

		// Save search in recent list
		final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
				this, RecentGALSearchTermsProvider.AUTHORITY,
				RecentGALSearchTermsProvider.MODE);
		suggestions.saveRecentQuery(name, null);
		latestSearchTerm = name;

		search = new GALSearch(syncManager);
		search.onSearchCompletedListener = this;
		search.setClearResults(clearResults);
		search.setStartWith(startWith);
		search.execute(name);
	}

	// Cancels the current search if there is one and return true if an actual, ongoing search was canselled
	boolean cancelSearch() {
		if (this.search == null || !search.getStatus().equals(AsyncTask.Status.RUNNING))
			return false;
		CorporateAddressBookFragment fragment = (CorporateAddressBookFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
		fragment.resetHeader();
		search.cancel(false);
		search = null;
		return true;
	}

	@Override
	public Boolean onAccountsChanged(AccountManager accountManager) {
		initializeActionBar();
		return true;
	}


	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		activeSyncManager = App.getAccounts().get(itemPosition);
		App.getAccounts().setDefaultAccount(activeSyncManager);
		return true;
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
		final MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		if (!Utility.isPreFroYo()) {
			// Get the SearchView and set the searchable configuration
			final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			final ComponentName component = getComponentName();
			final SearchableInfo searchableInfo = searchManager
					.getSearchableInfo(component);
			MenuItem item = menu.findItem(
					R.id.menu_search);
			searchView = item.getActionView();
			if (searchView instanceof com.actionbarsherlock.widget.SearchView) {
				((com.actionbarsherlock.widget.SearchView) searchView).setSearchableInfo(searchableInfo);
				((com.actionbarsherlock.widget.SearchView) searchView).setIconifiedByDefault(false);
			} else if (searchView instanceof SearchView) {
				((android.widget.SearchView) searchView).setSearchableInfo(searchableInfo);
				((android.widget.SearchView) searchView).setIconifiedByDefault(false);
			}
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
	public static void showConfiguration(SherlockFragmentActivity parentActivity) {
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
				//loadPreferences();
				break;
		}
	}

	private void addSearchResults(HashMap<String, Contact> contacts) {
		mContacts.putAll(contacts);

		final FragmentManager fragmentManager = getSupportFragmentManager();

		CorporateAddressBookFragment listFragment = (CorporateAddressBookFragment) fragmentManager
				.findFragmentById(R.id.main_fragment);
		if (listFragment == null) {
			Debug.Log("List fragment missing from main activity, discarding search result"); //$NON-NLS-1$
			return;
		}
		listFragment.addResult(contacts, activeSyncManager);
	}

	@TargetApi(11)
	private void displaySearchResult(HashMap<String, Contact> contacts, String searchTerm) {

		this.mContacts = contacts;
		this.latestSearchTerm = searchTerm;

		final FragmentManager fragmentManager = getSupportFragmentManager();

		CorporateAddressBookFragment list = (CorporateAddressBookFragment) fragmentManager
				.findFragmentById(R.id.main_fragment);
		if (list == null) {
			Debug.Log("List fragment missing from main activity, discarding search result"); //$NON-NLS-1$
			return;
		}
		list.displayResult(mContacts, latestSearchTerm, this.activeSyncManager);

		resetAndHideDetails(fragmentManager);
		if (searchView != null) {
			if (searchView instanceof com.actionbarsherlock.widget.SearchView) {
				((com.actionbarsherlock.widget.SearchView) searchView).setQuery("", false); //$NON-NLS-1$
			} else if (searchView instanceof SearchView) {
				((SearchView) searchView).setQuery("", false); //$NON-NLS-1$
			} else {
				throw new RuntimeException("Unknown SearchView type"); //$NON-NLS-1$
			}
		}
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
	}

	@Override
	protected void onDestroy() {
		if (search != null)
			search.setOnSearchCompletedListener(null);
		if (listeningToAccountChanges)
			App.getAccounts().removeChangeListener(this);
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
			// Below does not work since it resizes the accountKey fragment before anim starts,
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
		if (!Utility.isPreFroYo()) {
			if (searchView != null) {
				if (searchView instanceof com.actionbarsherlock.widget.SearchView) {
					com.actionbarsherlock.widget.SearchView v = (com.actionbarsherlock.widget.SearchView) searchView;
					v.setFocusable(true);
					v.setIconified(false);
					v.requestFocusFromTouch();
				} else if (searchView.getClass().toString().equals(SearchView.class.toString())) {
					SearchView v = (SearchView) searchView;
					v.setFocusable(true);
					v.setIconified(false);
					v.requestFocusFromTouch();
				} else {
					throw new RuntimeException("Unknown SearchView type"); //$NON-NLS-1$
				}
				return true;
			} else {
				Debug.Log("Running HC+ without SearchView"); //$NON-NLS-1$
				return false;
			}
		}
		return super.onSearchRequested();
	}

	@Override
	public void onSearchCompleted(int result,
	                              GALSearch search) {
		if (result == RESULT_OK) {
			if (search.getClearResults())
				displaySearchResult(search.getContacts(), search.getSearchTerm());
			else
				addSearchResults(search.getContacts());
			return;
		}

		CorporateAddressBookFragment fragment = (CorporateAddressBookFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
		fragment.resetHeader();

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

	@Override
	public void onSearchCanceled() {
		cancelSearch();
	}
}