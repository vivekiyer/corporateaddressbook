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
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ArrayAdapter;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import net.vivekiyer.GAL.ChoiceDialogFragment.OnChoiceDialogOptionClickListener;
import net.vivekiyer.GAL.ContactListFragment.ContactListListener;
import net.vivekiyer.GAL.account.AccountManager;
import net.vivekiyer.GAL.preferences.ConnectionChecker;
import net.vivekiyer.GAL.preferences.PrefsActivity;
import net.vivekiyer.GAL.search.ActiveSyncManager;
import net.vivekiyer.GAL.search.GALSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * @author Vivek Iyer
 *         <p/>
 *         This class is the main entry point to the application
 */
public class CorporateAddressBook extends SherlockFragmentActivity
		implements ContactListListener, GALSearch.OnSearchCompletedListener, OnChoiceDialogOptionClickListener,
		ActionBar.OnNavigationListener, AccountManager.OnAccountsChangedListener, ViewPager.OnPageChangeListener {

	private boolean listeningToAccountChanges = false;

	// Object that performs all the ActiveSync magic
	private ActiveSyncManager activeSyncManager;

	// Used to launch the preference pane
	static final int DISPLAY_PREFERENCES_REQUEST = 0;

	// Used to launch the initial configuration pane
	public static final int DISPLAY_CONFIGURATION_REQUEST = 2;
	public static final int REPROVISION_AND_RETRY = 3;

	static final String CONTACTS = "mContacts"; //$NON-NLS-1$
	static final String SEARCH_TERM = "latestSearchTerm"; //$NON-NLS-1$
	static final String SELECTED_CONTACT = "selectedContact"; //$NON-NLS-1$
	static final String ONGOING_SEARCH = "search";  //$NON-NLS-1$
	static final String ACCOUNT_KEY = "accountKey";  //$NON-NLS-1$
	static final String START_WITH = "startWith";  //$NON-NLS-1$
	static final String REQUERY = "requery_previous"; //NON-NLS
	static final String TOTAL_RESULT_COUNT = "totalResultCount";

	// Progress bar
	//	private ProgressDialog progressdialog;
	// Last search term
	private String latestSearchTerm;
	private com.actionbarsherlock.widget.SearchView searchView;

	// TAG used for logging

	// Stores the list of contacts returned
	private ArrayList<Contact> mContacts;
	private Contact selectedContact;
	private GALSearch search;

	public static final int AuthEx = 2;

	private boolean isPaused = false;
	private boolean mDualPane;
	private ContactListFragment contactsFragment;
	private ContactPagerFragment detailsFragment;

	private boolean isUpdating = false;
	private int totalNumberOfResults;

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

		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View detailsFrame = findViewById(R.id.contact_fragment);
		mDualPane = detailsFrame != null
				&& detailsFrame.getVisibility() == View.VISIBLE;

		initializeActionBar();

		FragmentManager fm = getSupportFragmentManager();
		detailsFragment = (ContactPagerFragment) fm
				.findFragmentById(R.id.contact_fragment);
		contactsFragment = (ContactListFragment) fm
				.findFragmentById(R.id.main_fragment);

	}

	private void initializeActionBar() {
		if (!listeningToAccountChanges) {
			App.getAccounts().addChangeListener(this);
			listeningToAccountChanges = true;
		}

		if (!App.getAccounts().Initialize(this))
			return;
		final ActionBar actionBar = getSupportActionBar();

		if (Utility.isPreHoneycomb()) {
			BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(R.drawable.actionbar_background);
			bg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			actionBar.setBackgroundDrawable(bg);
			actionBar.setSplitBackgroundDrawable(bg);
		}

		if (App.getAccounts().size() >= 2) {
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

			ArrayAdapter<ActiveSyncManager> adapter = new ArrayAdapter<ActiveSyncManager>(actionBar.getThemedContext(), R.layout.sherlock_spinner_item, App.getAccounts());
			adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
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

			performSearch(query);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// A savedInstanceState means that activity is being recreated and not launched
		// from a new Intent
		if (savedInstanceState != null && savedInstanceState.containsKey(CONTACTS)) {
			mContacts = (ArrayList<Contact>) savedInstanceState.get(CONTACTS);
			String latestSearchTerm = savedInstanceState.getString(SEARCH_TERM);
			totalNumberOfResults = savedInstanceState.getInt(TOTAL_RESULT_COUNT);
			selectedContact = (Contact) savedInstanceState.get(SELECTED_CONTACT);
			displaySearchResult(mContacts, latestSearchTerm, totalNumberOfResults);
			if (selectedContact != null) {
				selectContact(selectedContact);
			}
			Integer searchHash = savedInstanceState.getInt(ONGOING_SEARCH);
			if (searchHash != 0) {
				search = App.taskManager.get(searchHash);
				if (search != null) {
					search.setOnSearchCompletedListener(this);
					App.taskManager.remove(searchHash);
					contactsFragment.setHeader(getString(R.string.retrievingResults), true);
				}
			}
		}
		// Not recreated; that means we've got a new Intent to deal with
		// Get the intent, verify the action and get the query
//		if (savedInstanceState == null || !savedInstanceState.containsKey(CONTACTS)) { //$NON-NLS-1$
		else {
			final Intent intent = getIntent();
			onNewIntent(intent);
		}

	}

	// Assist user by showing search box whenever returning
	@Override
	protected void onStart() {
		super.onStart();

		if (mDualPane) { //detailsFragment != null && detailsFragment.isInLayout()) {
			contactsFragment.setIsSelectable(true);
//			contactsFragment.setViewBackground(false);
			getSupportFragmentManager()
					.beginTransaction()
					.hide(detailsFragment)
					.commit();
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
		contactsFragment.setSelectedContact(selectedContact);
		onContactSelected(selectedContact);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(CONTACTS, this.mContacts);
		outState.putString(SEARCH_TERM, this.latestSearchTerm);
		outState.putSerializable(SELECTED_CONTACT, this.selectedContact);
		outState.putInt(TOTAL_RESULT_COUNT, totalNumberOfResults);
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
		if (this.search != null && search.getStatus().equals(AsyncTask.Status.RUNNING))
			return;

		if (clearResults) {
			contactsFragment.setHeader(getString(R.string.retrievingResults), true);
		}

		// Save search in recent list
		final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
				this, RecentGALSearchTermsProvider.AUTHORITY,
				RecentGALSearchTermsProvider.MODE);
		suggestions.saveRecentQuery(name, null);
		latestSearchTerm = name;

		search = new GALSearch(syncManager);
		search.setOnSearchCompletedListener(this);
		search.setClearResults(clearResults);
		search.setStartWith(startWith);
		search.execute(name);
	}


	// Cancels the current search if there is one and return true if an actual, ongoing search was cancelled
	boolean cancelSearch() {
		if (this.search == null || !search.getStatus().equals(AsyncTask.Status.RUNNING))
			return false;
		contactsFragment.resetHeader();
		search.cancel(false);
		search = null;
		supportInvalidateOptionsMenu();
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
			searchView = (com.actionbarsherlock.widget.SearchView) item.getActionView();
			searchView.setSearchableInfo(searchableInfo);
			searchView.setIconifiedByDefault(false);
			searchView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					searchView.requestFocus();
				}
			});
			item.setEnabled(search == null || !search.getStatus().equals(AsyncTask.Status.RUNNING));
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
		MenuItem item = menu.findItem(R.id.menu_search);
		item.setEnabled(search == null || search.getStatus() != AsyncTask.Status.RUNNING);
		return true;
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
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void addSearchResults(ArrayList<Contact> contacts) {
		supportInvalidateOptionsMenu();

		Collections.sort(contacts);

		if (contactsFragment == null) {
			Debug.Log("List fragment missing from main activity, discarding search result"); //$NON-NLS-1$
			return;
		}
		contactsFragment.addResult(contacts, activeSyncManager);
		if (detailsFragment != null && detailsFragment.isVisible()) {
			// To force redraw/update pager to new size of contact list
			detailsFragment.update(-1, null);
		}
	}

	@TargetApi(11)
	private void displaySearchResult(ArrayList<Contact> contacts, String searchTerm, int totalNumberOfResults) {
		supportInvalidateOptionsMenu();

		this.mContacts = contacts;
		this.latestSearchTerm = searchTerm;
		this.totalNumberOfResults = totalNumberOfResults;

		if (contactsFragment == null) {
			Debug.Log("List fragment missing from main activity, discarding search result"); //$NON-NLS-1$
			return;
		}
		contactsFragment.displayResult(mContacts, latestSearchTerm, this.activeSyncManager, totalNumberOfResults);

		resetAndHideDetails();
		if (searchView != null) {
			searchView.setQuery("", false); //$NON-NLS-1$
		}
		contactsFragment.getView().requestFocus();
	}

	private void resetAndHideDetails() {

		if (detailsFragment != null && detailsFragment.isInLayout() && !this.isPaused) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			//ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

			// Below does not work since it clears the detail fragment before anim starts,
			// making it look rather weird. Better off w/o anims, unfortunately.
			//ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
			ft.hide(detailsFragment);
			ft.commit();
			//fragmentManager.executePendingTransactions();
			//details.clear();
		}

		contactsFragment.setViewBackground(false);
		selectedContact = null;
	}

	@Override
	protected void onDestroy() {
		if (search != null)
			search.setOnSearchCompletedListener(null);
		if (listeningToAccountChanges)
			App.getAccounts().removeChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onContactSelected(Contact contact) {
		// Create a parcel with the associated contact object
		// This parcel is used to send data to the activity

		this.selectedContact = contact;

		assert (mContacts != null);
		int contactIndex = mContacts.indexOf(contact);

		if (detailsFragment == null || !detailsFragment.isInLayout()) {
			// Launch the activity
			final Intent myIntent = new Intent(this, ContactActivity.class);
			myIntent.putExtra(getString(R.string.KEY_CONTACT_INDEX), contactIndex);
			myIntent.putParcelableArrayListExtra(getString(R.string.KEY_CONTACT_LIST), mContacts);
			startActivity(myIntent);

		} else {
			isUpdating = true;
			detailsFragment.update(contactIndex, mContacts);
			isUpdating = false;

			if (!detailsFragment.isVisible()) {
				contactsFragment.setViewBackground(true);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				//ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// Below does not work since it resizes the accountKey fragment before anim starts,
				// making it look rather weird. Better off w/o anims, unfortunately.
				//ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
				ft.replace(R.id.contact_fragment, detailsFragment)
						.show(detailsFragment)
						.setTransition(
								FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.commit();
				//			details.getView().setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onSearchCleared() {
		resetAndHideDetails();
	}

	@TargetApi(11)
	@Override
	public boolean onSearchRequested() {
		if (!Utility.isPreFroYo()) {
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
	public void onSearchCompleted(int result,
	                              GALSearch search) {
		if (result == RESULT_OK) {
			supportInvalidateOptionsMenu();
			if (search.getClearResults())
				displaySearchResult(search.getContacts(), search.getSearchTerm(), search.getTotalNumberOfResults());
			else
				addSearchResults(search.getContacts());
			return;
		}

		contactsFragment.resetHeader();

		ChoiceDialogFragment dialogFragment;
		String title = search.getErrorMesg();
		String message = search.getErrorDetail();
		String positiveButtonText;
		String negativeButtonText;
		switch (result) {
			// Errors that might be remedied by updating server settings
			case 401: // UNAUTHORIZED
			case ActiveSyncManager.ERROR_UNABLE_TO_REPROVISION:
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
			case 500:
			case ConnectionChecker.TIMEOUT:
				positiveButtonText = getString(R.string.retry);
				negativeButtonText = getString(android.R.string.cancel);
				dialogFragment = ChoiceDialogFragment.newInstance(
						title,
						message, positiveButtonText, negativeButtonText, REPROVISION_AND_RETRY, android.R.id.closeButton);
				dialogFragment.setListener(this);
				try {
					dialogFragment.show(getSupportFragmentManager(), "reprovision"); //$NON-NLS-1$
				} catch (java.lang.IllegalStateException e) {
					Debug.Log(e.getMessage());
				}
				break;
			// Errors that depend on external (non app-related) circumstances
			case 403: // FORBIDDEN
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
		switch (action) {
			case DISPLAY_CONFIGURATION_REQUEST:
				showConfiguration(this);
				break;
			case REPROVISION_AND_RETRY:
				AsyncTask<ActiveSyncManager, Void, Boolean> getVersionTask =
						new AsyncTask<ActiveSyncManager, Void, Boolean>() {
							@Override
							protected Boolean doInBackground(ActiveSyncManager... params) {
								try {
									return params[0].getExchangeServerVersion() == 200;
								} catch (Exception e) {
									Exception ex = e;
									return false;
								}
							}

							@Override
							protected void onPostExecute(Boolean requery) {
								if (requery)
									CorporateAddressBook.this.performSearch(latestSearchTerm);
								else
									showConfiguration(CorporateAddressBook.this);
							}
						};
				getVersionTask.execute(activeSyncManager);
				break;
			default:
				break;
		}

	}

	@Override
	public void onSearchCanceled() {
		cancelSearch();
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void onPageSelected(int position) {
		if (!isUpdating) {
			contactsFragment.setSelectedContact(mContacts.get(position));
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}