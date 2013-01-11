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
import android.app.ActionBar;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author Vivek Iyer 
 * This class is used to display a Contact object in a list.
 * The class takes a parceled Contact object and displays the
 * DisplayName. It also allows the user to save the contact
 */

/**
 * @author vivek
 */
public class ContactActivity extends SherlockFragmentActivity {

	private SearchView searchView;

	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_pager_fragment);

		Bundle b = null;
		if(savedInstanceState != null) {
			b = savedInstanceState;
		}
		else {
			b = getIntent().getExtras();
		}
		ContactPagerFragment pagerFragment = (ContactPagerFragment) getSupportFragmentManager().findFragmentById(R.id.contact_pager_fragment);
		pagerFragment.update(b);

		if (!Utility.isPreHoneycomb()) {
			final ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * @param menu Create a context menu for the list view Depending upon the
	 *             item selected, shows the user different options
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Displays the menu when the user clicks the Options button In our case the
	 * menu contains only one button - save
	 */
	@TargetApi(11)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.contacts_menu, menu);

		// Get the SearchView and set the searchable configuration for FroYo
		// and above (getSearchableInfo() not supported pre-FroYo)
		if (!Utility.isPreFroYo()) {
			final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			final ComponentName component = getComponentName();
			final SearchableInfo searchableInfo = searchManager
					.getSearchableInfo(component);
			MenuItem item = menu.findItem(
					R.id.menu_search);
			if (Utility.isPreHoneycomb()) {
				com.actionbarsherlock.widget.SearchView searchView = new com.actionbarsherlock.widget.SearchView(this);
				searchView.setSearchableInfo(searchableInfo);
				item.setActionView(searchView);
			} else {
				SearchView searchView = new SearchView(this);
				searchView.setSearchableInfo(searchableInfo);
				item.setActionView(searchView);
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
			case android.R.id.home:
				// app icon in action bar clicked; go home
				final Intent intent = new Intent(this,
						net.vivekiyer.GAL.CorporateAddressBook.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			case R.id.menu_search:
				return this.onSearchRequested();
			case R.id.settings:
				CorporateAddressBook.showConfiguration(this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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

}
