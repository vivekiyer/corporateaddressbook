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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * An implementation of {@link ContactWriter} that uses current Contacts API.
 * This class should be used on Eclair or beyond, but would not work on any
 * earlier release of Android. As a matter of fact, it could not even be loaded.
 */
public class ContactWriterSdk5 extends ContactWriter {

	private static ArrayList<AccountData> mAccounts = null;
	private static AccountAdapter mAccountAdapter = null;
	private Context context;
	private Contact mContact;

	// TAG used for logging
	private static String TAG = "ContactWriterSdk5"; //$NON-NLS-1$

	public ContactWriterSdk5(Application appCtx, Contact contact) {
		super();
		Initialize(appCtx, contact);
	}
	
	@Override
	public void Initialize(Application appCtx, Contact contact) {
		context = appCtx;
		mContact = contact;

		// TODO: Refactor into non-singelton object
		if(mAccounts == null){
			mAccounts = new ArrayList<AccountData>();
			mAccountAdapter = new AccountAdapter(appCtx, mAccounts);
	
			// Prepare the system account manager. On registering the listener
			// below, we also ask for
			// an initial callback to pre-populate the account list.
		}		
	}

	private void addContactFields(ArrayList<ContentProviderOperation> ops) {
		
		mContact.generateFieldsFromXML();
		
		// Add work phone
		if (!mContact.getWorkPhone().equalsIgnoreCase("")) //$NON-NLS-1$
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
		if (!mContact.getOfficeLocation().equalsIgnoreCase("")) //$NON-NLS-1$
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
		if (!mContact.getAlias().equalsIgnoreCase("")) //$NON-NLS-1$
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
							"Work Alias").build()); //$NON-NLS-1$

		// Add home phone
		if (!mContact.getHomePhone().equalsIgnoreCase("")) //$NON-NLS-1$
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
		if (!mContact.getMobilePhone().equalsIgnoreCase("")) //$NON-NLS-1$
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
		if (!mContact.getEmail().equalsIgnoreCase("")) //$NON-NLS-1$
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
				.withValue(
						ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION,
						mContact.getOfficeLocation())
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
						mContact.getFirstName() + " " + mContact.getLastName()) //$NON-NLS-1$
				.build());

		// Add the alias
		if(mContact.getAlias().length() > 0) {
			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE)
					.withValue(
							ContactsContract.CommonDataKinds.Identity.IDENTITY,
							mContact.getAlias())
					.build());
		}

		// Add picture, if available
		if (mContact.getPicture().length > 0) { //$NON-NLS-1$
			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,
							mContact.getPicture())
					.build());
		}

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
						selectedAccount.getName())
				.build());

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
	@SuppressLint("NewApi")
	@Override
	public void saveContact(Context ctx) {

		Builder builder;
		if(Utility.isPreHoneycomb())
			builder = new AlertDialog.Builder(ctx);
		else
			builder = new AlertDialog.Builder(ctx, AlertDialog.THEME_HOLO_LIGHT);
		builder.setTitle(ctx.getString(R.string.select_account));

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
	public void cleanUp() {
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
