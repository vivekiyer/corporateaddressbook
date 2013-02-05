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

package net.vivekiyer.GAL.preferences;

import android.accounts.*;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.*;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import net.vivekiyer.GAL.ChoiceDialogFragment;
import net.vivekiyer.GAL.Debug;
import net.vivekiyer.GAL.R;
import net.vivekiyer.GAL.Utility;
import net.vivekiyer.GAL.account.AccountAdapter;
import net.vivekiyer.GAL.account.AccountData;
import net.vivekiyer.GAL.search.ActiveSyncManager;
import net.vivekiyer.GAL.search.Parser;
import net.vivekiyer.GAL.search.TaskCompleteCallback;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Vivek Iyer
 *         <p/>
 *         This class handles the configuration pane for the application.
 */
public class Configure extends SherlockFragmentActivity implements OnClickListener, TaskCompleteCallback, ChoiceDialogFragment.OnChoiceDialogOptionClickListener {

	private static final int SEND_DEBUG_EMAIL = 0x200;
	protected SharedPreferences mPreferences;
	protected ProgressDialog progressdialog;
	protected ActiveSyncManager activeSyncManager;
	protected String domain;
	protected String username;
	protected String accountKey;
	protected Boolean accountRemoved;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * 
	 * Called when the configuration pane is first launched
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configure);

		// Hook up the on click handler for the button
		Button button = (Button) findViewById(R.id.buttonSignIn);
		button.setOnClickListener(this);

		String action = getIntent().getAction();
		// Edit account
		if (action.equals(getString(R.string.ACTION_PREFS_ACCOUNT_EDIT))) {
			this.setTitle(R.string.edit_account);
			String accountKey = getIntent().getStringExtra(getString(R.string.KEY_ACCOUNT_KEY));
			if (accountKey == null)
				throw new RuntimeException("No account key was supplied for editing"); //$NON-NLS-1$

			// Get the preferences that were entered by the user and display those to the user
			mPreferences = getSharedPreferences(accountKey, MODE_PRIVATE);

			String domain = mPreferences.getString(getString(R.string.PREFS_KEY_DOMAIN_PREFERENCE), ""),
					user = mPreferences.getString(getString(R.string.PREFS_KEY_USERNAME_PREFERENCE), "");
			user = domain + (domain.length() > 0 ? "\\" : "") + user; //$NON-NLS-1$  //$NON-NLS-2$
			if (user.length() > 0)
				setTextForId(R.id.txtDomainUserName, user);
			setTextForId(
					R.id.txtPassword,
					mPreferences.getString(getString(R.string.PREFS_KEY_PASSWORD_PREFERENCE), "")); //$NON-NLS-1$
			setTextForId(R.id.txtServerName,
					mPreferences.getString(getString(R.string.PREFS_KEY_SERVER_PREFERENCE), "")); //$NON-NLS-1$
			setValueForCheckbox(R.id.chkUseSSL,
					mPreferences.getBoolean(getString(R.string.PREFS_KEY_USE_SSL), true));
			setValueForCheckbox(R.id.chkAcceptAllSSLCert,
					mPreferences.getBoolean(getString(R.string.PREFS_KEY_ACCEPT_ALL_CERTS), true));

			// Disable editing of username and server; this would mess up our settings
			findViewById(R.id.txtDomainUserName).setEnabled(false);
			findViewById(R.id.lblDomainUserName).setEnabled(false);
			findViewById(R.id.search_username_button).setEnabled(false);
			findViewById(R.id.txtServerName).setEnabled(false);
			findViewById(R.id.lblServerName).setEnabled(false);
			findViewById(R.id.editingWarning).setVisibility(View.VISIBLE);
		}
		// Add account
		else if (action.equals(getString(R.string.ACTION_PREFS_ACCOUNT_ADD))) {
			// Enable user name lookup
			ImageButton ib = (ImageButton) findViewById(R.id.search_username_button);
			if (ib != null)
				ib.setOnClickListener(this);
		}
		// Delete account
		else if (action.equals(getString(R.string.ACTION_PREFS_ACCOUNT_DELETE))) {
			accountKey = getIntent().getStringExtra(getString(R.string.KEY_ACCOUNT_KEY));
			onChoiceDialogOptionPressed(R.id.account_delete);
			return;
		}
		EditText text = (EditText) findViewById(R.id.txtPassword);
		text.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int actionId,
			                              KeyEvent arg2) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					connect();
					return true;
				}
				return false;
			}
		});

		if (!Utility.isPreHoneycomb()) {
			ActionBar actionBar = getActionBar();
			if (actionBar != null)
				actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * @param id The id for the UI element
	 * @param s  The value to set the text to
	 *           <p/>
	 *           Sets the text for the EditText UI element to the provided
	 *           value
	 */
	private void setTextForId(int id, String s) {
		EditText text = (EditText) findViewById(id);
		text.setText(s);
	}

	/**
	 * @param id The id for the UI element
	 * @return The value the text is set to
	 *         <p/>
	 *         Gets the text that the EditText UI element is set to
	 */
	private String getTextFromId(int id) {
		EditText text = (EditText) findViewById(id);
		return text.getText().toString();
	}

	private boolean getValueFromCheckbox(int id) {
		final CheckBox checkBox = (CheckBox) findViewById(id);
		return checkBox.isChecked();
	}

	private void setValueForCheckbox(int id, boolean value) {
		final CheckBox checkBox = (CheckBox) findViewById(id);
		checkBox.setChecked(value);
	}

	/**
	 * Validates the user entries and connects to the Exchange server
	 */
	private void connect() {
		// Make sure that the user has entered the username
		// password and the server name
		if (getTextFromId(R.id.txtDomainUserName).equals("")) { //$NON-NLS-1$
			showAlert(getString(R.string.valid_domain_and_username_error));
			return;
		}

		String[] splits = getTextFromId(R.id.txtDomainUserName).split("\\\\");

		if (splits.length == 1) {
			domain = ""; //$NON-NLS-1$
			username = splits[0];
		} else if (splits.length == 2) {
			domain = splits[0];
			username = splits[1];
		} else {
			showAlert(getString(R.string.domain_and_username_format_error));
			return;
		}

		if (username.equalsIgnoreCase("")) { //$NON-NLS-1$
			showAlert(getString(R.string.invalid_username_error));
			return;
		}

		if (getTextFromId(R.id.txtPassword).equalsIgnoreCase("")) { //$NON-NLS-1$
			showAlert(getString(R.string.invalid_password_error));
			return;
		}

		if (getTextFromId(R.id.txtServerName).equalsIgnoreCase("")) { //$NON-NLS-1$
			showAlert(getString(R.string.invalid_exchange_url_error));
			return;
		}

		// Now that we have all three
		// Lets validate it

		activeSyncManager = new ActiveSyncManager(
				getTextFromId(R.id.txtServerName).trim(),
				domain,
				username,
				getTextFromId(R.id.txtPassword),
				getValueFromCheckbox(R.id.chkUseSSL),
				getValueFromCheckbox(R.id.chkAcceptAllSSLCert),
				"0", //$NON-NLS-1$
				"", //$NON-NLS-1$
				null);

		// If we get an error from Initialize
		// That means the URL is just bad
		// display an error
		if (!activeSyncManager.Initialize()) {
			showAlert(getString(R.string.please_check_settings));
			return;
		}

		Debug.clear();

		progressdialog = new ProgressDialog(this);
		progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog.setMessage(getString(R.string.validating_settings));
		progressdialog.setCancelable(false);
		progressdialog.show();
		ConnectionChecker checker = new ConnectionChecker(this);
		checker.execute(activeSyncManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * 
	 * Called when the user clicks the Log In button
	 */
	@Override
	public void onClick(View v) {
		if (R.id.buttonSignIn == v.getId())
			connect();
		else if (R.id.search_username_button == v.getId()) {
			populateUsername(this);
		}
	}

	@SuppressLint("NewApi")
	public void populateUsername(Context ctx) {

		final ArrayList<AccountData> accountData = new ArrayList<AccountData>();
		final AccountAdapter accAdapter = new AccountAdapter(this, accountData);

		Builder builder;
		if (Utility.isPreHoneycomb())
			builder = new AlertDialog.Builder(ctx);
		else
			builder = new AlertDialog.Builder(ctx, AlertDialog.THEME_HOLO_LIGHT);
		builder.setTitle(ctx.getString(R.string.select_account));

		builder.setSingleChoiceItems(accAdapter, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText v = ((EditText) findViewById(R.id.txtDomainUserName));
						v.setText(accountData.get(which).getName());
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * @param s The alert message Displays an alert dialog with the messaged
	 *          provided
	 */
	@SuppressLint("NewApi")
	private void showAlert(String s) {
		Builder builder;
		if (Utility.isPreHoneycomb())
			builder = new AlertDialog.Builder(this);
		else
			builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
		builder.setMessage(s).setPositiveButton(getResources().getString(android.R.string.ok), null);
		AlertDialog alert = builder.create();
		alert.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.vivekiyer.GAL.search.TaskCompleteCallback#taskComplete(boolean)
	 * 
	 * When the connection check is complete, depending upon the outcome either
	 * quit this activity, or ask the user to fix the issue
	 */
	@Override
	public void taskComplete(
			boolean taskStatus,
			int statusCode,
			int requestStatus,
			String errorString) {

		if ((progressdialog != null) && progressdialog.isShowing()) {
			try {
				progressdialog.dismiss();
			} catch (java.lang.IllegalArgumentException e) {
			}
		}

		// Looks like there was an error in the settings
		if (!taskStatus) {
//			statusCode = -987698; // To test debug emails
//			if (Debug.Enabled) {
//				// Send the error message via email
//				Debug.sendDebugEmail(this);
//			} else {
			try {
				// Handle all errors we're capable of,
				// inform user of others
				switch (statusCode) {
					case 200: // Successful, but obviously something went wrong
						switch (requestStatus) {
							case Parser.STATUS_TOO_MANY_DEVICES:
								ChoiceDialogFragment.newInstance(getString(R.string.too_many_device_partnerships_title), getString(R.string.too_many_device_partnerships_detail))
										.show(getSupportFragmentManager(), "tooManyDevices"); //$NON-NLS-1$
								break;
							default:
								if (Debug.isEnabled()) {
									ChoiceDialogFragment.newInstance(getString(R.string.unhandled_error, requestStatus), getString(R.string.unhandled_error_occured, ""), getString(R.string.send_debug_email), getString(R.string.close), SEND_DEBUG_EMAIL, 0)
											.setListener(this)
											.show(getSupportFragmentManager(), "unknownError"); //NON-NLS
								} else {
									ChoiceDialogFragment.newInstance(getString(R.string.connection_failed_title),
											getString(R.string.connection_failed_detail, statusCode) + "\n" + getString(R.string.enable_debugging),
											getString(android.R.string.yes),
											getString(android.R.string.no), R.string.enable_debugging, 0)
											.setListener(this)
											.show(getSupportFragmentManager(), "connError"); //NON-NLS
								}
								break;
						}
						break;
					case 401: // UNAUTHORIZED
						showAlert(getString(R.string.authentication_failed_detail));
						break;
					case 403: // FORBIDDEN, typically means that the DeviceID is not accepted and needs to be set in Exchange
						String title = getString(R.string.forbidden_by_server_title);
						String details = getString(R.string.forbidden_by_server_detail, activeSyncManager.getDeviceId());
						ChoiceDialogFragment.newInstance(title, details, getString(android.R.string.ok), getString(android.R.string.copy), android.R.id.button2, android.R.id.copy)
								.setListener(this)
								.show(getSupportFragmentManager(), "forbidden"); //NON-NLS
						break;
					case ConnectionChecker.SSL_PEER_UNVERIFIED:
						ChoiceDialogFragment.newInstance(getString(R.string.authentication_failed_title), getString(R.string.unable_to_find_matching_certificate, "\n", getString(R.string.acceptAllSllText))) //$NON-NLS-1$
								.show(getSupportFragmentManager(), "SslUnverified"); //$NON-NLS-1$
						break;
					case ConnectionChecker.UNKNOWN_HOST:
						ChoiceDialogFragment.newInstance(getString(R.string.invalid_server_title), getString(R.string.invalid_server_detail)).show(getSupportFragmentManager(), "SslUnverified"); //$NON-NLS-1$
						break;
					case ConnectionChecker.TIMEOUT:
						ChoiceDialogFragment.newInstance(getString(R.string.timeout_title), String.format(getString(R.string.timeout_detail), getString(R.string.useSecureSslText))).show(getSupportFragmentManager(), "Timeout"); //$NON-NLS-1$
						break;
					default:
						if (Debug.isEnabled()) {
							ChoiceDialogFragment.newInstance(getString(R.string.connection_failed_title), getString(R.string.connection_failed_detail, statusCode), getString(R.string.send_debug_email), getString(R.string.close), SEND_DEBUG_EMAIL, 0)
									.setListener(this)
									.show(getSupportFragmentManager(), "connError"); //NON-NLS
						} else {
							ChoiceDialogFragment.newInstance(getString(R.string.connection_failed_title),
									getString(R.string.connection_failed_detail, statusCode) + "\n" + getString(R.string.enable_debugging),
									getString(android.R.string.yes),
									getString(android.R.string.no), R.string.enable_debugging, 0)
									.setListener(this)
									.show(getSupportFragmentManager(), "connError"); //NON-NLS
						}
						break;
				}
			} catch (java.lang.IllegalStateException e) {
				Debug.Log("Server configuration window was dismissed before Connection check was finished:\n" + e.toString()); //NON-NLS
			}
//			}
		}
		// All went well. Store the settings and return to the main page
		else {
			AccountManager am = AccountManager.get(this);
			String accountKey = username.contains("@") ?
					username :
					String.format("%1$s@%2$s", username, getTextFromId(R.id.txtServerName).trim()); //NON-NLS

			mPreferences = getSharedPreferences(accountKey, MODE_PRIVATE);
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString(getString(R.string.PREFS_KEY_SERVER_PREFERENCE),
					getTextFromId(R.id.txtServerName).trim());
			editor.putString(getString(R.string.PREFS_KEY_DOMAIN_PREFERENCE), domain);
			editor.putString(getString(R.string.PREFS_KEY_USERNAME_PREFERENCE), username);
			editor.putString(getString(R.string.PREFS_KEY_PASSWORD_PREFERENCE),
					getTextFromId(R.id.txtPassword));
			editor.putBoolean(getString(R.string.PREFS_KEY_USE_SSL), getValueFromCheckbox(R.id.chkUseSSL));
			editor.putBoolean(getString(R.string.PREFS_KEY_ACCEPT_ALL_CERTS),
					getValueFromCheckbox(R.id.chkAcceptAllSSLCert));
			editor.putString(getString(R.string.PREFS_KEY_ACTIVESYNCVERSION_PREFERENCE),
					activeSyncManager.getActiveSyncVersion());
			editor.putString(getString(R.string.PREFS_KEY_DEVICE_ID_STRING), activeSyncManager.getDeviceId());
			editor.putString(getString(R.string.PREFS_KEY_POLICY_KEY_PREFERENCE),
					activeSyncManager.getPolicyKey());
			editor.putBoolean(getString(R.string.PREFS_KEY_SUCCESSFULLY_CONNECTED),
					true);

			// Commit the edits!
			editor.commit();

			// Pass the values to the account manager
			Account account = null;

			account = new Account(accountKey,
					getString(R.string.ACCOUNT_TYPE));
			if (getIntent().getAction().equals(getString(R.string.ACTION_PREFS_ACCOUNT_ADD))) {
				// Disabling sync for all accounts, otherwise a constant wakelock is created...
				ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 0);
			}
			am.addAccountExplicitly(account,
					getTextFromId(R.id.txtPassword), null);
			am.setUserData(account, getString(R.string.KEY_ACCOUNT_KEY), accountKey);

			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				AccountAuthenticatorResponse response = extras
						.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

				if (response != null) {
					Bundle result = new Bundle();
					result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
					result.putString(AccountManager.KEY_ACCOUNT_TYPE,
							getString(R.string.ACCOUNT_TYPE));
					response.onResult(result);
				}
			}

			// Close the activity
			finish();
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(11)
	@Override
	public void onChoiceDialogOptionPressed(int action) {
		switch (action) {
			case android.R.id.copy:
				if (Utility.isPreHoneycomb()) {
					final android.text.ClipboardManager clipboard;
					clipboard = (android.text.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
					clipboard.setText(activeSyncManager.getDeviceId());
				} else {
					ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					clip.setPrimaryClip(ClipData.newPlainText("Android Device ID", activeSyncManager.getDeviceId())); //NON-NLS
				}
				break;
			case R.id.account_delete:
				getSharedPreferences(accountKey, MODE_PRIVATE).edit().clear().commit(); //remove all prefs
				android.accounts.AccountManager am = android.accounts.AccountManager.get(this);
				for (Account acc : am.getAccountsByType(getString(R.string.ACCOUNT_TYPE))) {
					if (acc.name.equals(accountKey)) {
						am.removeAccount(acc, new AccountManagerCallback<Boolean>() {
							@Override
							public void run(AccountManagerFuture<Boolean> future) {
								try {
									setResult(future.getResult() ? RESULT_OK : RESULT_CANCELED);
								} catch (OperationCanceledException e) {
								} catch (IOException e) {
								} catch (AuthenticatorException e) {
								}
								finish();
							}
						}, null);
						break;
					}
				}
				break;
			case android.R.id.empty:
				setResult(RESULT_CANCELED);
				finish();
				break;
			case SEND_DEBUG_EMAIL:
				Debug.sendDebugEmail(this);
				break;
			case R.string.enable_debugging:
				Debug.setEnabled(true);
			default:
				break;
		}
	}

}
