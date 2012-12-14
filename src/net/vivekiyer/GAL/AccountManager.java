package net.vivekiyer.GAL;

import android.accounts.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-12-12
 * Time: 18:12
 * <p/>
 * This class handles accounts for Exchange server. As a centralized location for change notification as well as
 * reading and storing changes, it is the source for configuration for the app.
 */
public class AccountManager extends ArrayList<ActiveSyncManager> {

	public interface OnAccountsChangedListener {
		Boolean onAccountsChanged(AccountManager accountManager);
	}

	private ArrayList<OnAccountsChangedListener> changeListeners = new ArrayList<OnAccountsChangedListener>();
	protected Context context = null;
	protected Boolean isInitialized = false;

	private AccountManager() {
	}

	public AccountManager(Context context) {
		this();
		this.context = context;
	}

	public void Initialize(ProgressDialog progressdialog, Activity activity) {
		if (!isInitialized) {
			loadPreferences(progressdialog, activity);
		}
	}

	public void addChangeListener(OnAccountsChangedListener listener) {
		changeListeners.add(listener);
	}

	public void removeChangeListener(OnAccountsChangedListener listener) {
		changeListeners.remove(listener);
	}

	protected void notifyChange() {
		for (OnAccountsChangedListener l : changeListeners) {
			if (l != null)
				l.onAccountsChanged(this);
		}
	}

	/**
	 * Reads the stored preferences, identifies the individual accounts and initializes an ActiveSyncManager for
	 * each of them. If no accounts are available, it tries to migrate old style settings (pre-2.1) to the new account
	 * model.
	 * <p/>
	 * If no prefs exist it launches the "Add account" dialog.
	 */
	public void loadPreferences(final ProgressDialog progressdialog, final Activity parentActivity) {

		// Initialize preferences and the corresponding ActiveSyncManager(s)
		final android.accounts.AccountManager am = android.accounts.AccountManager.get(context);
		Account[] accounts = am.getAccountsByType(context.getString(R.string.ACCOUNT_TYPE));
		if (accounts == null || accounts.length == 0) {
			final SharedPreferences existingPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			final String userName = existingPrefs.getString(context.getString(R.string.PREFS_KEY_USERNAME_PREFERENCE), null);
			final String serverName = existingPrefs.getString(context.getString(R.string.PREFS_KEY_SERVER_PREFERENCE), null);
			if (serverName == null) {
				addAccount(am, parentActivity);
			} else {
				progressdialog.setMessage("Migrating settings...");
				progressdialog.show();
				final String accountKey = userName.contains("@") ?
						userName :
						String.format("%1$s@%2$s", userName, serverName);
				final ActiveSyncManager syncManager = new ActiveSyncManager();
				AsyncTask migrateTask = new AsyncTask() {
					@Override
					protected Object doInBackground(Object... params) {
						return syncManager.loadPreferences(existingPrefs);
					}

					@Override
					protected void onPostExecute(Object o) {
						if (o instanceof Boolean && ((Boolean) o)) {
							migrateConfiguration(am, existingPrefs, accountKey, syncManager, progressdialog, null);
						} else {
							if (progressdialog != null && progressdialog.isShowing()) {
								try {
									progressdialog.dismiss();
								} catch (IllegalArgumentException e) {
								}
							}
							addAccount(am, parentActivity);
						}
					}
				};
				migrateTask.execute(existingPrefs, syncManager);
			}
		} else {
			for (Account account : accounts) {
				String accountKey = am.getUserData(account, context.getString(R.string.KEY_ACCOUNT_KEY));

				if (accountKey == null || accountKey.isEmpty()) {
					throw new RuntimeException("Unknown account key");
				}

				ActiveSyncManager activeSyncManager = new ActiveSyncManager();
				if (activeSyncManager.loadPreferences(accountKey)) {
					add(activeSyncManager);
				}
			}
		}
	}

	private void migrateConfiguration(android.accounts.AccountManager am, SharedPreferences existingPrefs, String accountKey, ActiveSyncManager syncManager, ProgressDialog progressdialog, Activity parentActivity) {
		if (syncManager.loadPreferences(existingPrefs)) {
			try {

				SharedPreferences newPrefs = context.getSharedPreferences(accountKey, Context.MODE_PRIVATE);
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

				if (!Debug.Enabled) {
					//existingPrefs.edit().clear().apply();
				}

				// Pass the values to the account manager
				Account account = new Account(accountKey,
						getString(R.string.ACCOUNT_TYPE));
				am.addAccountExplicitly(account,
						existingPrefs.getString(getString(R.string.PREFS_KEY_PASSWORD_PREFERENCE), ""), null);
				am.setUserData(account, getString(R.string.KEY_ACCOUNT_KEY), accountKey);

				loadPreferences(accountKey);
			} catch (Exception e) {
				addAccount(am, parentActivity);
			}
		} else {
			addAccount(am, parentActivity);
		}
		if (progressdialog != null && progressdialog.isShowing()) {
			try {
				progressdialog.dismiss();
			} catch (IllegalArgumentException e) {
			}
		}
	}

	private void addAccount(android.accounts.AccountManager acc, Activity activity) {

		AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				String accountName;
				try {
					accountName = future.getResult().getString(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
					loadPreferences(accountName);
				} catch (OperationCanceledException e) {
				} catch (IOException e) {
				} catch (AuthenticatorException e) {
				}
			}
		};

		AccountManagerFuture<Bundle> future = acc.addAccount(getString(R.string.ACCOUNT_TYPE), null, null, null, activity, callback, null);

	}

	/**
	 * Reads the stored preferences and initializes the
	 *
	 * @param accountKey
	 * @return True if a valid Exchange server settings was saved during the
	 *         previous launch. False otherwise *
	 */
	public boolean loadPreferences(String accountKey) {
		SharedPreferences thesePrefs = context.getSharedPreferences(accountKey, Context.MODE_PRIVATE);

		ActiveSyncManager activeSyncManager = new ActiveSyncManager();

		if (thesePrefs.getAll().size() < 6)
			throw new RuntimeException("Server settings incomplete");

		thesePrefs.registerOnSharedPreferenceChangeListener(activeSyncManager);

		if (activeSyncManager.loadPreferences(thesePrefs)) {
			add(activeSyncManager);
			return true;
		}
		return false;
	}

	private String getString(int id) {
		return context.getString(id);
	}
}
