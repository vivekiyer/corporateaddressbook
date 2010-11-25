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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.LayoutInflater;

/**
 * An implementation of {@link ContactWriter} that uses legacy Contacts API.
 * These APIs are deprecated and should not be used unless we are running on a
 * pre-Eclair SDK.
 */
@SuppressWarnings("deprecation")
public class ContactWriterSdk3_4 extends ContactWriter {
	private Context context;
	private Contact mContact;
	private static String TAG = "ContactWriterSdk3_4";
	private ContentResolver contentResolver;

	@Override
	public void saveContact() {
		ContentValues values = new ContentValues();
		mContact.generateFieldsFromXML();
		
		values.put(
				People.NAME, 
					mContact.getFirstName() + 
					" " + 
					mContact.getLastName()
				);

		// add it to the database
		Uri newPerson = Contacts.People.createPersonInMyContactsGroup(
				contentResolver, values);

		if (newPerson == null) {
			Log.v(TAG, "Error creating contact occured");
			return;
		}
		addContactFields(values, newPerson);
	}

	@Override
	public void Initialize(Context ctx, LayoutInflater lf, Contact contact) {
		context = ctx;
		mContact = contact;
		contentResolver = context.getContentResolver();
	}

	private void addContactFields(ContentValues values, Uri newPerson) {

		if (!mContact.getWorkPhone().equalsIgnoreCase("")) {
			ContentValues workPhoneValues = new ContentValues();
			Uri workPhoneUri = Uri.withAppendedPath(newPerson,
					Contacts.People.Phones.CONTENT_DIRECTORY);
			workPhoneValues.put(Contacts.Phones.NUMBER, mContact.getWorkPhone());
			workPhoneValues
					.put(Contacts.Phones.TYPE, Contacts.Phones.TYPE_WORK);
			Uri phoneUpdate = contentResolver.insert(workPhoneUri,
					workPhoneValues);
			if (phoneUpdate == null) {
				Log.v(TAG, "Failed to insert mobile phone number");
			}
		}
		
		if (!mContact.getOfficeLocation().equalsIgnoreCase("")) {
			ContentValues addressValues = new ContentValues();
			Uri addressUri = Uri.withAppendedPath(newPerson,
					Contacts.People.ContactMethods.CONTENT_DIRECTORY);
			addressValues.put(Contacts.ContactMethods.KIND,
					Contacts.KIND_POSTAL);
			addressValues.put(Contacts.ContactMethods.TYPE,
					Contacts.ContactMethods.TYPE_WORK);
			addressValues.put(Contacts.ContactMethods.DATA, mContact.getOfficeLocation());
			Uri addressUpdate = contentResolver.insert(addressUri,
					addressValues);
			if (addressUpdate == null) {
				Log.v(TAG, "Failed to insert office location");
			}
		} 
		
//		if (!mContact.getAlias().equalsIgnoreCase("")) {
//			values.put(People.PHONETIC_NAME, mContact.getAlias());
//		}
//		
		
		if (!mContact.getHomePhone().equalsIgnoreCase("")) {
			ContentValues homePhoneValues = new ContentValues();
			Uri homePhoneUri = Uri.withAppendedPath(newPerson,
					Contacts.People.Phones.CONTENT_DIRECTORY);
			homePhoneValues.put(Contacts.Phones.NUMBER, mContact.getHomePhone());
			homePhoneValues
					.put(Contacts.Phones.TYPE, Contacts.Phones.TYPE_HOME);
			Uri phoneUpdate = contentResolver.insert(homePhoneUri,
					homePhoneValues);
			if (phoneUpdate == null) {
				Log.v(TAG, "Failed to insert mobile phone number");
			}
		} 
		
		if (!mContact.getMobilePhone().equalsIgnoreCase("")) {
			ContentValues mobileValues = new ContentValues();
			Uri mobileUri = Uri.withAppendedPath(newPerson,
					Contacts.People.Phones.CONTENT_DIRECTORY);
			mobileValues.put(Contacts.Phones.NUMBER, mContact.getMobilePhone());
			mobileValues.put(Contacts.Phones.TYPE, Contacts.Phones.TYPE_MOBILE);
			Uri phoneUpdate = contentResolver.insert(mobileUri, mobileValues);
			if (phoneUpdate == null) {
				Log.v(TAG, "Failed to insert mobile phone number");
			}
		} 
		
		if (!mContact.getEmail().equalsIgnoreCase("")) {
			ContentValues emailValues = new ContentValues();
			Uri emailUri = Uri.withAppendedPath(newPerson,
					Contacts.People.ContactMethods.CONTENT_DIRECTORY);
			emailValues.put(Contacts.ContactMethods.KIND, Contacts.KIND_EMAIL);
			emailValues.put(Contacts.ContactMethods.TYPE,
					Contacts.ContactMethods.TYPE_WORK);
			emailValues.put(Contacts.ContactMethods.DATA, mContact.getEmail());
			Uri emailUpdate = contentResolver.insert(emailUri, emailValues);
			if (emailUpdate == null) {
				Log.v(TAG, "Failed to insert email");
			}
		}

		ContentValues organisationValues = new ContentValues();
		Uri orgUri = Uri.withAppendedPath(newPerson,
				Contacts.Organizations.CONTENT_DIRECTORY);
		organisationValues.put(Contacts.Organizations.TITLE, mContact.getTitle());
		organisationValues.put(Contacts.Organizations.COMPANY, mContact.getCompany());
		organisationValues.put(Contacts.Organizations.TYPE,
				Contacts.Organizations.TYPE_WORK);
		Uri orgUpdate = contentResolver.insert(orgUri, organisationValues);
		if (orgUpdate == null) {
			Log.v(TAG, "Could not add organization");
		}
	}

}
