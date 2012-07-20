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
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * An implementation of {@link ContactWriter} that uses current Contacts API.
 * This class should be used on Eclair or beyond, but would not work on any
 * earlier release of Android. As a matter of fact, it could not even be loaded.
 */
public class ContactWriterSdk5 extends ContactWriter implements
		OnAccountsUpdateListener {

	ArrayList<AccountData> mAccounts;
	private AccountAdapter mAccountAdapter;
	private Context context;
	private LayoutInflater layoutInflater;
	private Contact mContact;

	// TAG used for logging
	private static String TAG = "ContactWriterSdk5";

	@Override
	public void Initialize(Context ctx, LayoutInflater lf, Contact contact) {
		context = ctx;
		layoutInflater = lf;
		mContact = contact;

		// TODO: Refactor into non-singelton object
		if(mAccounts == null){
			mAccounts = new ArrayList<AccountData>();
			mAccountAdapter = new AccountAdapter(context, mAccounts);
	
			// Prepare the system account manager. On registering the listener
			// below, we also ask for
			// an initial callback to pre-populate the account list.
			AccountManager.get(context).addOnAccountsUpdatedListener(this, null,
					true);
		}		
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

	private void addContactFields(ArrayList<ContentProviderOperation> ops) {
		
		mContact.generateFieldsFromXML();
		
		// Add work phone
		if (!mContact.getWorkPhone().equalsIgnoreCase(""))
			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
							mContact.getWorkPhone())
					.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
							ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
					.build());

		// Add office location
		if (!mContact.getOfficeLocation().equalsIgnoreCase(""))
			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
					.withValue(
							ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
							mContact.getOfficeLocation())
					.withValue(
							ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
							ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
					.build());

		// Add alias
		if (!mContact.getAlias().equalsIgnoreCase(""))
			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Nickname.NAME,
							mContact.getAlias())
					.withValue(
							ContactsContract.CommonDataKinds.Nickname.TYPE,
							ContactsContract.CommonDataKinds.Nickname.TYPE_CUSTOM)
					.withValue(ContactsContract.CommonDataKinds.Nickname.LABEL,
							"Work Alias").build());

		// Add home phone
		if (!mContact.getHomePhone().equalsIgnoreCase(""))
			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
							mContact.getHomePhone())
					.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
							ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
					.build());

		// Add mobile phone
		if (!mContact.getMobilePhone().equalsIgnoreCase(""))
			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
							mContact.getMobilePhone())
					.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
							ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
					.build());

		// Add email address
		if (!mContact.getEmail().equalsIgnoreCase(""))
			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Email.DATA,
							mContact.getEmail())
					.withValue(ContactsContract.CommonDataKinds.Email.TYPE,
							ContactsContract.CommonDataKinds.Email.TYPE_WORK)
					.build());

		// Write the company name and title
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Organization.TITLE,
						mContact.getTitle())
				.withValue(
						ContactsContract.CommonDataKinds.Organization.COMPANY,
						mContact.getCompany())
				.withValue(ContactsContract.CommonDataKinds.Organization.TYPE,
						ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
				.build());

		// Lets map the display name, first name and last name
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
						mContact.getFirstName())
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
						mContact.getLastName())
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
						mContact.getFirstName() + " " + mContact.getLastName())
				.build());

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
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
						selectedAccount.getType())
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
						selectedAccount.getName()).build());

		// Write information for each key value pair in contact
		addContactFields(ops);

		// Ask the Contact provider to create a new contact
		//Log.i(TAG, "Selected account: " + selectedAccount.getName() + " ("
		//		+ selectedAccount.getType() + ")");
		//Log.i(TAG, "Creating contact: " + mContact.getDisplayName());

		try {
			ContentProviderResult[] results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY,
					ops);
			
			for(ContentProviderResult result : results){
				Log.i(TAG, result.uri.toString());
			}
			
		} catch (Exception e) {
			// Log exception
			//Log.e(TAG, "Exceptoin encoutered while inserting contact: " + e);
		}
	}

	/**
	 * Displays an option that allows the user to save the contact being
	 * displayed to the addressbook on the device
	 */
	@Override
	public void saveContact() {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Select account");

		builder.setSingleChoiceItems(mAccountAdapter, -1,
				new OnClickListener() {
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

	@Override
	public void onAccountsUpdated(Account[] accounts) {
		//Log.i(TAG, "Account list update detected");
		// Clear out any old data to prevent duplicates
		mAccounts.clear();

		// Get account data from system
		AuthenticatorDescription[] accountTypes = AccountManager.get(context)
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
	 * A container class used to represent all known information about an
	 * account.
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
		 *            The description for this account. This will be dictated by
		 *            the type of account returned, and can be obtained from the
		 *            system AccountManager.
		 */
		public AccountData(String name, AuthenticatorDescription description) {
			mName = name;
			if (description != null) {
				mType = description.type;

				// The type string is stored in a resource, so we need to
				// convert it into something
				// human readable.
				String packageName = description.packageName;
				PackageManager pm = context.getPackageManager();

				if (description.labelId != 0) {
					mTypeLabel = pm.getText(packageName, description.labelId,
							null);
					if (mTypeLabel == null) {
						throw new IllegalArgumentException(
								"LabelID provided, but label not found");
					}
				} else {
					mTypeLabel = "";
				}

				if (description.iconId != 0) {
					mIcon = pm.getDrawable(packageName, description.iconId,
							null);
					if (mIcon == null) {
						throw new IllegalArgumentException(
								"IconID provided, but drawable not " + "found");
					}
				} else {
					mIcon = context.getResources().getDrawable(
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
		public View getView(int position, View convertView, ViewGroup parent) {
			// Inflate a view template
			if (convertView == null) {
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
				icon = context.getResources().getDrawable(
						android.R.drawable.ic_menu_search);
			}
			accountIcon.setImageDrawable(icon);
			return convertView;
		}
	}

	@Override
	public void cleanUp() {
		// Remove AccountManager callback
		AccountManager.get(context).removeOnAccountsUpdatedListener(this);		
	}
	
	@Override
	protected void finalize() throws Throwable {
		// Make sure listener is un-registered when this object is destructed, will otherwise cause leak
		AccountManager.get(context).removeOnAccountsUpdatedListener(this);
		
		super.finalize();
	}
}
