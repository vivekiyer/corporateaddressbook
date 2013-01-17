package net.vivekiyer.GAL.account;

import android.accounts.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import net.vivekiyer.GAL.App;
import net.vivekiyer.GAL.Debug;
import net.vivekiyer.GAL.R;
import net.vivekiyer.GAL.search.ActiveSyncManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-12-12
 * Time: 18:12
 * <p/>
 * This class handles accounts for Exchange server. As a centralized location for change notification as well as
 * reading and storing changes, it is the source for configuration for the app.
 */
public class AccountManager extends ArrayList<ActiveSyncManager> implements OnAccountsUpdateListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public interface OnAccountsChangedListener {

		Boolean onAccountsChanged(AccountManager accountManager);

	}

	private ArrayList<OnAccountsChangedListener> changeListeners = new ArrayList<OnAccountsChangedListener>();
	private ActiveSyncManager defaultAccount = null;
	private boolean listeningToAccountUpdates = false;

	protected Context context = null;
	protected Boolean isInitialized = false;

	public ActiveSyncManager getDefaultAccount() {
		return defaultAccount;
	}

	public void setDefaultAccount(ActiveSyncManager defaultAccount) {
		this.defaultAccount = defaultAccount;
		if (defaultAccount != null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
			prefs.edit().putString(App.getInstance().getString(R.string.PREFS_KEY_DEFAULT_ACCOUNT), defaultAccount.getAccountKey()).commit();
		}
	}

	private AccountManager() {
	}

	public AccountManager(Context context) {
		this();
		this.context = context;
	}

	public boolean Initialize(Activity activity) {
		if (!isInitialized) {
			if (loadPreferences(activity)) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
				String defaultAccount = prefs.getString(App.getInstance().getString(R.string.PREFS_KEY_DEFAULT_ACCOUNT), null);
				for (ActiveSyncManager syncManager : this) {
					if (syncManager.getAccountKey().equals(defaultAccount))
						this.defaultAccount = syncManager;
				}
				isInitialized = true;
			}
		}
		return isInitialized;
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
	public boolean loadPreferences(final Activity parentActivity) {

		// Create the progress bar
		final ProgressDialog progressdialog = new ProgressDialog(parentActivity);
		progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog.setCancelable(false);

		// Initialize preferences and the corresponding ActiveSyncManager(s)
		final android.accounts.AccountManager am = android.accounts.AccountManager.get(context);
		if (!listeningToAccountUpdates) {
			am.addOnAccountsUpdatedListener(this, null, false);
			listeningToAccountUpdates = true;
		}

		Account[] accounts = am.getAccountsByType(context.getString(R.string.ACCOUNT_TYPE));
		if (accounts == null || accounts.length == 0) {
			final SharedPreferences existingPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			final String userName = existingPrefs.getString(context.getString(R.string.PREFS_KEY_USERNAME_PREFERENCE), null);
			final String serverName = existingPrefs.getString(context.getString(R.string.PREFS_KEY_SERVER_PREFERENCE), null);
			final boolean isAlreadyMigrated = existingPrefs.getBoolean(getString(R.string.PREFS_KEY_ALREADY_MIGRATED), false);
			if (serverName == null || isAlreadyMigrated) {
				addAccount(am, parentActivity);
				return false;
			} else {
				progressdialog.setMessage(parentActivity.getString(R.string.migratingSettings));
				progressdialog.show();
				final String accountKey = userName.contains("@") ?
						userName :
						String.format("%1$s@%2$s", userName, serverName); //NON-NLS
				final ActiveSyncManager syncManager = new ActiveSyncManager();
				AsyncTask<Object, Void, Boolean> migrateTask = new AsyncTask<Object, Void, Boolean>() {
					@Override
					protected Boolean doInBackground(Object... params) {
						return syncManager.loadPreferences(existingPrefs);
					}

					@Override
					protected void onPostExecute(Boolean success) {
						if (success) {
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
				return false;
			}
		} else {
			reloadAccounts(am, accounts, false);
		}
		return true;
	}

	private void reloadAccounts(android.accounts.AccountManager am, Account[] accounts, boolean triggerListeners) {
		Boolean needsNotification = false;
		for (Account account : accounts) {
			Boolean changeHandled = false;
			if (account.type.equals(getString(R.string.ACCOUNT_TYPE))) {
				String accountKey = am.getUserData(account, context.getString(R.string.KEY_ACCOUNT_KEY));

				if (accountKey == null || accountKey.length() == 0) {
					if (Debug.Enabled)
						accountKey = am.getUserData(account, "net.vivekiyer.GAL.Preferences.ACCOUNT_KEY");
					else
						throw new RuntimeException("Unknown account key");
				}

				for (ActiveSyncManager syncManager : this) {
					if (syncManager.getAccountKey().equals(accountKey)) {
						changeHandled = true;
						if (!syncManager.reloadPreferences()) {
							remove(syncManager);
							break;
						}
					}
				}
				if (!changeHandled) {
					ActiveSyncManager activeSyncManager = new ActiveSyncManager();
					if (activeSyncManager.loadPreferences(accountKey)) {
						changeHandled = true;
						add(activeSyncManager);
					}
				}

			}
			needsNotification |= changeHandled;
		}
		Iterator<ActiveSyncManager> it = this.iterator();
		while (it.hasNext()) {
			ActiveSyncManager syncManager = it.next();
			Boolean found = false;
			for (Account account : accounts) {
				if (account.type.equals(getString(R.string.ACCOUNT_TYPE))) {
					String accountKey = am.getUserData(account, context.getString(R.string.KEY_ACCOUNT_KEY));
					if (syncManager.getAccountKey().equals(accountKey))
						found = true;
				}
			}
			if (!found)
				it.remove();
		}
		if (needsNotification && triggerListeners)
			notifyChange();
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
						false);

				// Commit the edits!
				editor.commit();

				if (!App.isDebuggable()) {
					// TODO: add flag for setting migrated
					//existingPrefs.edit().clear().apply();
				}

				// Pass the values to the account manager
				Account account = new Account(accountKey,
						getString(R.string.ACCOUNT_TYPE));
				am.addAccountExplicitly(account,
						existingPrefs.getString(getString(R.string.PREFS_KEY_PASSWORD_PREFERENCE), ""), null);
				am.setUserData(account, getString(R.string.KEY_ACCOUNT_KEY), accountKey);

				// Make sure we only do this once for non-debug builds
				if (!App.isDebuggable())
					existingPrefs.edit().putBoolean(getString(R.string.PREFS_KEY_ALREADY_MIGRATED), true).commit();

				if (!loadPreferences(accountKey))
//					editAccount(am, parentActivity);
					addAccount(am, parentActivity);
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

	@Override
	public void onAccountsUpdated(Account[] accounts) {
		reloadAccounts(android.accounts.AccountManager.get(context), accounts, true);
		//To change body of implemented methods use File | Settings | File Templates.
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

		/*AccountManagerFuture<Bundle> future =*/
		acc.addAccount(getString(R.string.ACCOUNT_TYPE), null, null, null, activity, callback, null);

	}

	private void editAccount(String accountKey) {
		throw new RuntimeException("editAccount() not implemented");
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

		if (activeSyncManager.loadPreferences(thesePrefs)) {
			add(activeSyncManager);
			notifyChange();
			return true;
		}
		return false;
	}

	private String getString(int id) {
		return context.getString(id);
	}

	public ActiveSyncManager get(String accountKey) {
		for (ActiveSyncManager a : this) {
			if (a.getAccountKey().equals(accountKey))
				return a;
		}
		return null;
	}

	public boolean hasKey(String accountKey) {
		return (get(accountKey) != null);
	}
}
