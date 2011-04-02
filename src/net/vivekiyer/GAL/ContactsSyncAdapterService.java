package net.vivekiyer.GAL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

public class ContactsSyncAdapterService extends Service {
	private static final String TAG = "ContactsSyncAdapterService";
	private static SyncAdapterImpl sSyncAdapter = null;
	private static ContentResolver mContentResolver = null;

	public ContactsSyncAdapterService() {
		super();
	}

	private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
		private Context mContext;

		public SyncAdapterImpl(Context context) {
			super(context, true);
			mContext = context;
		}

		@Override
		public void onPerformSync(Account account, Bundle extras,
				String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			try {
				ContactsSyncAdapterService.performSync(mContext, account,
						extras, authority, provider, syncResult);
			} catch (OperationCanceledException e) {
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		ret = getSyncAdapter().getSyncAdapterBinder();
		return ret;
	}

	private SyncAdapterImpl getSyncAdapter() {
		if (sSyncAdapter == null)
			sSyncAdapter = new SyncAdapterImpl(this);
		return sSyncAdapter;
	}

	private static void performSync(Context context, Account account,
			Bundle extras, String authority, ContentProviderClient provider,
			SyncResult syncResult) throws OperationCanceledException {

		mContentResolver = context.getContentResolver();

		android.os.Debug.waitForDebugger();

		HashMap<String, Long> localContacts = new HashMap<String, Long>();

		Uri rawContactUri = RawContacts.CONTENT_URI;

		// Lets get everything but Google accounts
		Cursor c1 = mContentResolver.query(rawContactUri, new String[] {
				BaseColumns._ID, RawContacts.ACCOUNT_TYPE,
				RawContacts.ACCOUNT_NAME }, null, null, null);

		while (c1.moveToNext()) {
			Log.i(TAG,
					c1.getLong(0) + ":" + c1.getString(1) + ":"
							+ c1.getString(2));
			localContacts.put(c1.getString(1), c1.getLong(0));
		}

		int numElements = localContacts.size();

		/*
		 * get Collection of values contained in HashMap using Collection
		 * values() method of HashMap class
		 */
		Iterator it = localContacts.entrySet().iterator();

		// iterate through HashMap values iterator
		while (it.hasNext()) {
			Entry<String, Long> pairs = (Entry<String, Long>) it.next();
			Log.i(TAG, pairs.getKey() + " " + pairs.getValue());

		}

		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		try {
			if (operationList.size() > 0)
				mContentResolver.applyBatch(ContactsContract.AUTHORITY,
						operationList);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void addContact(Account account, String name, String username) {
		Log.i(TAG, "Adding contact: " + name);
		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(RawContacts.CONTENT_URI);
		builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
		builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
		builder.withValue(RawContacts.SYNC1, username);
		operationList.add(builder.build());

		builder = ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(
				ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
				0);
		builder.withValue(
				ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		builder.withValue(
				ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
				name);
		operationList.add(builder.build());

		builder = ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
		builder.withValue(ContactsContract.Data.MIMETYPE,
				"vnd.android.cursor.item/vnd.net.vivekiyer.GAL.profile");
		builder.withValue(ContactsContract.Data.DATA1, username);
		builder.withValue(ContactsContract.Data.DATA2, "Exchange Profile");
		builder.withValue(ContactsContract.Data.DATA3, "View profile");
		operationList.add(builder.build());

		try {
			mContentResolver.applyBatch(ContactsContract.AUTHORITY,
					operationList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}