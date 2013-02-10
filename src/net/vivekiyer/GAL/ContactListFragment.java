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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshStickyHeadersListView;
import net.vivekiyer.GAL.search.ActiveSyncManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 */
public class ContactListFragment extends SherlockFragment {

	public interface ContactListListener {
		public void onContactSelected(Contact contact);

		public void onSearchCleared();

		public void onSearchCanceled();
	}

	// TAG used for logging
	// private static String TAG = "CorporateAddressBook";
	private static final String FIRST_VISIBLE_ITEM = "firstVisibleItem";

	// List of names in the list view control
	private List<Contact> contactList;

	private ContactListAdapter listadapter;
	private PullToRefreshStickyHeadersListView ptrListView;

	protected ContactListListener contactListListener;

	private String searchTerm;
	private String previousHeaderText;
	private int totalNumberOfResults;

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
		ptrListView.getRefreshableView().setChoiceMode(isSelectable ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}


	// Create an anonymous implementation of OnItemClickListener
	// that is used by the listview that displays the results
	private final OnItemClickListener mListViewListener = new OnItemClickListener() {

		/*
		 * (non-Javadoc)
		 * When the user clicks a particular entry in the list view launch the
		 * ContactActivity activity
		 * 
		 * @see
		 * android.widget.AdapterView.OnItemClickListener#onItemClick(android
		 * .widget.AdapterView, android.view.View, int, long)
		 * 
		 */
		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id) {

			// Get the selected display name from the list view
			StickyListHeadersListView listView = ptrListView.getRefreshableView();
			int headerCount = listView.getHeaderViewsCount();
			final Contact selectedItem = (Contact) listView.getAdapter()
					.getItem(position - headerCount);

			// Trigger callback so that the Activity can decide how to handle the click
			assert (contactListListener != null);
			contactListListener.onContactSelected(selectedItem);
		}
	};

	public ContactListFragment() {
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
		//setRetainInstance(true);
	}

	@Override
	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container,
	                         Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.main, container, false);
		ptrListView = (PullToRefreshStickyHeadersListView) view.findViewById(R.id.contactsListView);

		if (!PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getBoolean(getString(R.string.PREFS_KEY_PTR_DEMO_SHOWN), false)) {
			enablePtrDemo(ptrListView);
		}
		setSelectionMode(view, isSelectable);
		if (savedInstanceState != null) {
			int firstVisiblePosition = savedInstanceState.getInt(FIRST_VISIBLE_ITEM);
			ptrListView.getRefreshableView().setSelectionFromTop(firstVisiblePosition, 0);
		}
		return view;
	}

	private void enablePtrDemo(final PullToRefreshStickyHeadersListView ptrListView) {
		ptrListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//To change body of implemented methods use File | Settings | File Templates.
			}

			// Show demo for user when he/she scrolls to the end. If shown, record into prefs so that it
			// only happens once.
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount) {
					if (PreferenceManager.getDefaultSharedPreferences(getActivity())
							.getBoolean(getString(R.string.PREFS_KEY_PTR_DEMO_SHOWN), false)) {
						if (ptrListView.demo()) {
							PreferenceManager.getDefaultSharedPreferences(getActivity())
									.edit().putBoolean(getString(R.string.PREFS_KEY_PTR_DEMO_SHOWN), true);
						}
					}
				}
			}
		});
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		View v = getFragmentManager().findFragmentById(R.id.main_fragment).getView().findViewById(R.id.result_header_cancel);
		v.setVisibility(View.GONE);
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (contactListListener != null) contactListListener.onSearchCanceled();
			}
		});

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
			throw new ClassCastException(activity.toString() + " must implement OnContactSelectedListener"); //$NON-NLS-1$
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);    //To change body of overridden methods use File | Settings | File Templates.
		outState.putInt(FIRST_VISIBLE_ITEM, ptrListView.getRefreshableView().getFirstVisiblePosition());
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	protected void setViewBackground(Boolean shaded) {
		if (shaded) {
			if (Utility.isPreJellyBean()) {
				getView().setBackgroundDrawable(getResources().getDrawable(R.drawable.shaded_background_right));
			} else {
				getView().setBackground(getResources().getDrawable(R.drawable.shaded_background_right));
			}
		} else {
			getView().setBackgroundDrawable(null);
		}
		if (listadapter != null)
			listadapter.setHeaderBackground(getResources().getDrawable(shaded ? R.drawable.shaded_fading_header_background : R.drawable.fading_header_background));
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

	public void displayResult(ArrayList<Contact> mContacts, String latestSearchTerm, final ActiveSyncManager syncManager, int totalNumberOfResults) {
		if (mContacts == null || syncManager == null) {
			//Toast.makeText(getActivity(), R.string.undefined_result_please_try_again, Toast.LENGTH_LONG).show();
			return;
		}
		searchTerm = latestSearchTerm;
		this.totalNumberOfResults = totalNumberOfResults;
		final int numberOfHits = mContacts.size();
		setHeaderText(numberOfHits, totalNumberOfResults);

		// Get the accountName and sort the alphabetically
		contactList = mContacts;
		Collections.sort(contactList);

		boolean refreshable = totalNumberOfResults > numberOfHits;

		// Create a new array adapter and add the accountName to this
		listadapter = new ContactListAdapter(
				this.getActivity(),
				R.layout.contact_row,
				contactList,
				/* "Get more" functionailty disabled in favor of PullToRefresh: refreshable ? syncManager : */ null);

		listadapter.setHasHeaders(
				PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(getString(R.string.PREFS_KEY_CONTACT_LIST_HEADERS), true)
		);

		StickyListHeadersListView sticky = ptrListView.getRefreshableView();
		sticky.setDivider(getResources().getDrawable(R.color.contact_list_divider));
		sticky.setDividerHeight(Utility.dip2Pixels(getActivity(), 1));
		sticky.setOnItemClickListener(mListViewListener);
		ptrListView.setAdapter(listadapter);
		ptrListView.setMode(refreshable ? PullToRefreshBase.Mode.PULL_FROM_END : PullToRefreshBase.Mode.DISABLED);
		ptrListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<StickyListHeadersListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<StickyListHeadersListView> refreshView) {
				//To change body of implemented methods use File | Settings | File Templates.
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<StickyListHeadersListView> refreshView) {
				ptrListView.setRefreshing();

				Intent i = new Intent(getActivity(), CorporateAddressBook.class);
				i.setAction(Intent.ACTION_SEARCH);
				i.putExtra(CorporateAddressBook.ACCOUNT_KEY, syncManager.getAccountKey());
				i.putExtra(CorporateAddressBook.START_WITH, listadapter.getCount());
				i.putExtra(CorporateAddressBook.REQUERY, true);
				getActivity().startActivity(i);
			}
		});
		// Set labels for PullToRefresh
		setPtrLabels(ptrListView);

	}

	private void setPtrLabels(PullToRefreshStickyHeadersListView ListView) {
		final ILoadingLayout loadLayout = ListView.getLoadingLayoutProxy();
		loadLayout.setPullLabel(getString(R.string.pull_to_get_more).toUpperCase());
		loadLayout.setReleaseLabel(getString(R.string.release_to_get_more).toUpperCase());
		loadLayout.setRefreshingLabel(getString(R.string.retrievingResults).toUpperCase());
		loadLayout.setBackground(getResources().getDrawable(R.drawable.refresh_background));
	}

	private void setHeaderText(final int numberOfHits, int totalNumberOfResults) {
		if (searchTerm == null || searchTerm.length() == 0)
			setHeader(String.format(getString(R.string.last_search_produced_x_results), numberOfHits), false);
		else
			setHeader(String.format(getString(R.string.showing_x_of_y_results_for_z), numberOfHits, totalNumberOfResults, searchTerm), false);
	}

	void setHeader(String message, boolean isInProgress) {
		if (message != null) {
			TextView tv = (TextView) this.getView().findViewById(R.id.result_header_text);
			previousHeaderText = tv.getText().toString();
			tv.setText(message);
		}
		View v = getView().findViewById(R.id.result_header_progress);
		v.setVisibility(isInProgress ? View.VISIBLE : View.INVISIBLE);
		v = getView().findViewById(R.id.result_header_cancel);
		v.setVisibility(isInProgress ? View.VISIBLE : View.INVISIBLE);
	}

	void resetHeader() {
		setHeader(previousHeaderText, false);
		if (listadapter != null) {
			listadapter.resetSearchNextView();
		}
	}

	public void addResult(ArrayList<Contact> contacts, ActiveSyncManager syncManager) {

		boolean refreshable = contacts.size() == syncManager.getMaxResults();
		listadapter.addAll(contacts, false /*refreshable*/);

		ptrListView.onRefreshComplete();
		ptrListView.setMode(refreshable ? PullToRefreshBase.Mode.PULL_FROM_END : PullToRefreshBase.Mode.DISABLED);

		setHeaderText(this.contactList.size(), totalNumberOfResults);
	}

	/**
	 * Clear the results from the listview
	 */
	protected void clearResult() {
		contactListListener.onSearchCleared();
		contactList = new ArrayList<Contact>(0);

		// Create a new array adapter and add the accountName to this
		listadapter = new ContactListAdapter(
				this.getActivity(), R.layout.contact_row,
				contactList, null);

		ptrListView.onRefreshComplete();
		ptrListView.getRefreshableView().setAdapter(listadapter);
		setHeader(getString(R.string.EnterSearchTerm), false);

		assert (contactListListener != null);
	}

	public void setSelectedContact(Contact selectedContact) {
		ListView lv = ptrListView.getRefreshableView();
		lv.requestFocusFromTouch();
		int selection = contactList.indexOf(selectedContact) + lv.getHeaderViewsCount();
		lv.setSelection(selection);
		lv.setItemChecked(selection, true);
	}
}