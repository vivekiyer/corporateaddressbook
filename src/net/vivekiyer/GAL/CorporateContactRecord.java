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

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;


/**
 * @author Vivek Iyer 
 * This class is used to display a Contact object in a list.
 * The class takes a parceled Contact object and displays the
 * DisplayName. It also allows the user to save the contact
 */
public class CorporateContactRecord extends ListActivity implements OnAccountsUpdateListener {

	private Contact mContact;
	private ContactListAdapter m_adapter;

	// TAG used for logging
	private static String TAG = "CorporateContactRecord";
	
	ArrayList <AccountData> mAccounts;
	private AccountAdapter mAccountAdapter;
	

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

		getListView().setOnItemClickListener(mListViewListener);
		
		mAccounts = new ArrayList<AccountData>();
		mAccountAdapter = new AccountAdapter(this, mAccounts);
		
		// Prepare the system account manager. On registering the listener
		// below, we also ask for
		// an initial callback to pre-populate the account list.
		AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true);

	}

	// Create an anonymous implementation of OnItemClickListener
	private OnItemClickListener mListViewListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id) {

		}
	};

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
			saveContact();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Displays an option that allows the user to save the contact being
	 * displayed to the addressbook on the device
	 */
	private void saveContact() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select account");	
		
		builder.setSingleChoiceItems(mAccountAdapter, -1, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// a choice has been made!
				// Write the contact to the account
				createContactEntry(mAccounts.get(which));
				dialog.dismiss();				
			}
		});
		builder.show();
	}
	
	/**
	 * Creates a contact entry from the current UI values in the account named
	 * by mSelectedAccount.
	 */
	protected void createContactEntry(AccountData selectedAccount) {
		
		// Prepare contact creation request
		//
		// Note: We use RawContacts because this data must be associated with a
		// particular account.
		// The system will aggregate this with any other data for this contact
		// and create a
		// corresponding entry in the ContactsContract.Contacts provider for us.
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				.withValue(
						ContactsContract.RawContacts.ACCOUNT_TYPE,
						selectedAccount.getType())
				.withValue(
						ContactsContract.RawContacts.ACCOUNT_NAME,
						selectedAccount.getName())
				.build());

		// Write information for each key value pair in contact		
		mapKVPToContactData(ops);		
		
		// Ask the Contact provider to create a new contact
		Log.i(TAG, "Selected account: " + selectedAccount.getName() + " ("
				+ selectedAccount.getType() + ")");
		Log.i(TAG, "Creating contact: " + mContact.getDisplayName());
		
		try {
			getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (Exception e) {
			// Log exception
			Log.e(TAG, "Exceptoin encoutered while inserting contact: " + e);
		}
	}
	
	private void mapKVPToContactData(ArrayList<ContentProviderOperation> ops){
		
		String firstName = "";
		String lastName = "";
		String title = "";
		String company = "";	
		
		// Get the key value pairs from the contact
		// and loop over each one
		
		for(KeyValuePair kvp : mContact.getDetails()){			
			
			String key = kvp.getKey();
			String value = kvp.getValue();
			
			
			if(key.equalsIgnoreCase("Phone")){
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID, 
								0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.Phone.NUMBER, 
								value)
						.withValue(
								ContactsContract.CommonDataKinds.Phone.TYPE,
								ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
						.build());
				Log.v(TAG, "Adding work phone");
			}
			else if(key.equalsIgnoreCase("Office")){
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID, 
								0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
								value)
						.withValue(
								ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
								ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)								
						.build());
				Log.v(TAG, "Adding office location");
			}
			else if(key.equalsIgnoreCase("Title")){				
				title = value;				
			}
			else if(key.equalsIgnoreCase("Company")){
				company = value;
			}
			else if(key.equalsIgnoreCase("Alias")){
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.Nickname.NAME, 
								value)
						.withValue(
								ContactsContract.CommonDataKinds.Nickname.TYPE,
								ContactsContract.CommonDataKinds.Nickname.TYPE_CUSTOM)
						.withValue(
								ContactsContract.CommonDataKinds.Nickname.LABEL,
								"Work Alias")
						.build());
			}
			else if(key.equalsIgnoreCase("FirstName")){
				firstName = value;
			}
			else if(key.equalsIgnoreCase("LastName")){
				lastName = value;
			}
			else if(key.equalsIgnoreCase("HomePhone")){
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.Phone.NUMBER, 
								value)
						.withValue(
								ContactsContract.CommonDataKinds.Phone.TYPE,
								ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
						.build());		
			}
			else if(key.equalsIgnoreCase("MobilePhone")){
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.Phone.NUMBER, 
								value)
						.withValue(
								ContactsContract.CommonDataKinds.Phone.TYPE,
								ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
						.build());
			}
			else if(key.equalsIgnoreCase("EmailAddress")){
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.Email.DATA, 
								value)
						.withValue(
								ContactsContract.CommonDataKinds.Email.TYPE,
								ContactsContract.CommonDataKinds.Email.TYPE_WORK)
						.build());
			}
		}
		
		// Write the company name and title
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(
						ContactsContract.Data.RAW_CONTACT_ID, 
						0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
				.withValue(
						ContactsContract.CommonDataKinds.Organization.TITLE,
						title)
				.withValue(
						ContactsContract.CommonDataKinds.Organization.COMPANY,
						company)				
				.withValue(
						ContactsContract.CommonDataKinds.Organization.TYPE,
						ContactsContract.CommonDataKinds.Organization.TYPE_WORK)				
				.build());	
		
		// Lets map the display name, first name and last name
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(
						ContactsContract.Data.RAW_CONTACT_ID, 
						0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
						firstName)
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
						lastName)
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
						firstName + " " + lastName)
				.build());		

	}	
	
	/**
	 * A container class used to represent all known information about an account.
	 */
	private class AccountData {
		private String mName;
		private String mType;
		private CharSequence mTypeLabel;
		private Drawable mIcon;

		/**
		 * @param name
		 *            The name of the account. This is usually the user's email
		 *            address or username.
		 * @param description
		 *            The description for this account. This will be dictated by the
		 *            type of account returned, and can be obtained from the system
		 *            AccountManager.
		 */
		public AccountData(String name, AuthenticatorDescription description) {
			mName = name;
			if (description != null) {
				mType = description.type;

				// The type string is stored in a resource, so we need to
				// convert it into something
				// human readable.
				String packageName = description.packageName;
				PackageManager pm =  getPackageManager();

				if (description.labelId != 0) {
					mTypeLabel = pm.getText(packageName, description.labelId, null);
					if (mTypeLabel == null) {
						throw new IllegalArgumentException(
								"LabelID provided, but label not found");
					}
				} else {
					mTypeLabel = "";
				}

				if (description.iconId != 0) {
					mIcon = pm.getDrawable(packageName, description.iconId, null);
					if (mIcon == null) {
						throw new IllegalArgumentException(
								"IconID provided, but drawable not " + "found");
					}
				} else {
					mIcon = getResources().getDrawable(
							android.R.drawable.sym_def_app_icon);
				}
			}
		}

		public String getName() {
			return mName;
		}

		public String getType() {
			return mType;
		}

		public CharSequence getTypeLabel() {
			return mTypeLabel;
		}

		public Drawable getIcon() {
			return mIcon;
		}

		public String toString() {
			return mName;
		}
	}
	
	/**
	 * Custom adapter used to display account icons and descriptions in the
	 * account spinner.
	 */
	private class AccountAdapter extends ArrayAdapter<AccountData> {
		public AccountAdapter(Context context,
				ArrayList<AccountData> accountData) {
			super(context, android.R.layout.simple_list_item_1, accountData);
		}

		@Override
		public View getView(int position, View convertView,
				ViewGroup parent) {
			// Inflate a view template
			if (convertView == null) {
				LayoutInflater layoutInflater = getLayoutInflater();
				convertView = layoutInflater.inflate(R.layout.account_entry,
						parent, false);
			}
			TextView firstAccountLine = (TextView) convertView
					.findViewById(R.id.firstAccountLine);
			TextView secondAccountLine = (TextView) convertView
					.findViewById(R.id.secondAccountLine);
			ImageView accountIcon = (ImageView) convertView
					.findViewById(R.id.accountIcon);

			// Populate template
			AccountData data = getItem(position);
			firstAccountLine.setText(data.getName());
			secondAccountLine.setText(data.getTypeLabel());
			Drawable icon = data.getIcon();
			if (icon == null) {
				icon = getResources().getDrawable(
						android.R.drawable.ic_menu_search);
			}
			accountIcon.setImageDrawable(icon);
			return convertView;
		}
	}

	@Override
	public void onAccountsUpdated(Account[] accounts) {
		Log.i(TAG, "Account list update detected");
		// Clear out any old data to prevent duplicates
		mAccounts.clear();

		// Get account data from system
		AuthenticatorDescription[] accountTypes = AccountManager.get(this)
				.getAuthenticatorTypes();

		// Populate tables
		for (int i = 0; i < accounts.length; i++) {
			// The user may have multiple accounts with the same name, so we
			// need to construct a
			// meaningful display name for each.
			String systemAccountType = accounts[i].type;			
			
			AuthenticatorDescription ad = getAuthenticatorDescription(
					systemAccountType, accountTypes);
			AccountData data = new AccountData(accounts[i].name, ad);
			mAccounts.add(data);
		}

		// Update the account spinner
		mAccountAdapter.notifyDataSetChanged();
		
	}
	
	/**
	 * Called when this activity is about to be destroyed by the system.
	 */
	@Override
	public void onDestroy() {
		// Remove AccountManager callback
		AccountManager.get(this).removeOnAccountsUpdatedListener(this);
		super.onDestroy();
	}
	
	/**
	 * Obtain the AuthenticatorDescription for a given account type.
	 * 
	 * @param type
	 *            The account type to locate.
	 * @param dictionary
	 *            An array of AuthenticatorDescriptions, as returned by
	 *            AccountManager.
	 * @return The description for the specified account type.
	 */
	private static AuthenticatorDescription getAuthenticatorDescription(
			String type, AuthenticatorDescription[] dictionary) {
		for (int i = 0; i < dictionary.length; i++) {
			if (dictionary[i].type.equals(type)) {
				return dictionary[i];
			}
		}
		// No match found
		throw new RuntimeException("Unable to find matching authenticator");
	}

};
