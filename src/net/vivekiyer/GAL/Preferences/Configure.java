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

package net.vivekiyer.GAL.Preferences;

import android.*;
import android.accounts.*;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.text.AndroidCharacter;
import android.widget.*;
import net.vivekiyer.GAL.*;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import net.vivekiyer.GAL.R;

import java.io.IOException;

/**
 * @author Vivek Iyer
 * 
 *         This class handles the configuration pane for the application.
 */
public class Configure extends FragmentActivity implements OnClickListener, TaskCompleteCallback, ChoiceDialogFragment.OnChoiceDialogOptionClickListener {
	
	protected SharedPreferences mPreferences;
	protected ProgressDialog progressdialog;
	protected ActiveSyncManager activeSyncManager;
	protected String domain;
	protected String username;
	protected String accountKey;
	protected Boolean accountRemoved;

	/*
		public static final String PREFS_KEY_USERNAME_PREFERENCE = "username";
		public static final String PREFS_KEY_PASSWORD_PREFERENCE = "password";
		public static final String PREFS_KEY_DOMAIN_PREFERENCE = "domain";
		public static final String PREFS_KEY_SERVER_PREFERENCE = "server";
		public static final String PREFS_KEY_ACTIVESYNCVERSION_PREFERENCE = "activesyncversion";
		public static final String PREFS_KEY_DEVICE_ID = "deviceid";
		public static final String PREFS_KEY_POLICY_PREFS_KEY_PREFERENCE = "policykey";
		public static final String PREFS_KEY_USE_SSL = "usessl";
		public static final String PREFS_KEY_ACCEPT_ALL_CERTS = "acceptallcerts";
		public static final String PREFS_KEY_RESULTS_PREFERENCE = "results";
		public static final String PREFS_KEY_SEARCH_TERM_PREFERENCE = "searchTerm";
		public static final String PREFS_KEY_SUCCESSFULLY_CONNECTED = "successfullyConnected";
	*/
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

		// Edit account
		if(getIntent().getAction().equals(getString(R.string.ACTION_PREFS_ACCOUNT_EDIT))) {
			this.setTitle("Edit account");
			String accountKey = getIntent().getStringExtra(getString(R.string.KEY_ACCOUNT_KEY));
			if(accountKey == null)
				throw new RuntimeException("No account key was supplied for editing");

			// Get the preferences that were entered by the user and display those to the user
			mPreferences = getSharedPreferences(accountKey, MODE_PRIVATE);

			String domain = mPreferences.getString(getString(R.string.PREFS_KEY_DOMAIN_PREFERENCE) , ""),
					user = mPreferences.getString(getString(R.string.PREFS_KEY_USERNAME_PREFERENCE), "");
			user = domain + (domain.length() > 0 ? "\\" : "") + user;
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
		}
		// Add account
		else if(getIntent().getAction().equals(getString(R.string.ACTION_PREFS_ACCOUNT_ADD))) {
			AccountManager am = AccountManager.get(App.getInstance());
			Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));

			if(accounts.length > 0) {
				Bundle extras = getIntent().getExtras();
				AccountAuthenticatorResponse response = extras
						.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
				showAlert("Sorry, Corporate Addressbook currently only supports one server");
				Toast.makeText(this, "Sorry, Corporate Addressbook currently only supports one server", Toast.LENGTH_SHORT).show();
				response.onError(0, "Sorry, Corporate Addressbook currently only supports one server");
				finish();
				return;
			}
		}
		// Delete account
		else if(getIntent().getAction().equals(getString(R.string.ACTION_PREFS_ACCOUNT_DELETE))) {
			accountKey = getIntent().getStringExtra(getString(R.string.KEY_ACCOUNT_KEY));
			if (accountKey == null)
					throw new RuntimeException("No account supplied for deletion");
			DialogFragment cd = ChoiceDialogFragment.newInstance(getString(R.string.delete_account),
					"Are you sure you want to delete the account \'" + accountKey + "\'?",
					getString(android.R.string.ok),
					getString(android.R.string.cancel),
					R.id.account_delete,
					android.R.id.empty)
					.setListener(this);
//					.setParent(this)
//					.create();
			cd.show(getSupportFragmentManager(), "deleteConfirmation");
			return;
		}
		EditText text = (EditText) findViewById(R.id.txtServerName);
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
		
		if(!Utility.isPreHoneycomb())
		{
			ActionBar actionBar = getActionBar();
			if(actionBar != null)
				actionBar.setDisplayHomeAsUpEnabled(true);
//			actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
		}
	}	
	
	/**
	 * @param id
	 *            The id for the UI element
	 * @param s
	 *            The value to set the text to
	 * 
	 *            Sets the text for the EditText UI element to the provided
	 *            value
	 */
	private void setTextForId(int id, String s) {
		EditText text = (EditText) findViewById(id);
		text.setText(s);
	}

	/**
	 * @param id
	 *            The id for the UI element
	 * @return The value the text is set to
	 * 
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
		
		if(splits.length == 1) {
			domain = ""; //$NON-NLS-1$
			username = splits[0];
		}
		else if (splits.length == 2) {
			domain = splits[0];
			username = splits[1];
		}
		else {
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
				"", //$NON-NLS-1$
				"", //$NON-NLS-1$
				null);

		// If we get an error from Initialize
		// That means the URL is just bad
		// display an error
		if (!activeSyncManager.Initialize()) {
			showAlert(getString(R.string.please_check_settings));
			return;
		}

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
		connect();
	}

	/**
	 * @param s
	 *            The alert message Displays an alert dialog with the messaged
	 *            provided
	 */
	private void showAlert(String s) {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(s).setPositiveButton(getResources().getString(android.R.string.ok), null);
		AlertDialog alert = alt_bld.create();
		alert.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.vivekiyer.GAL.TaskCompleteCallback#taskComplete(boolean)
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
		
		if((progressdialog != null) && progressdialog.isShowing()) {
			try {
		progressdialog.dismiss();
			} catch (java.lang.IllegalArgumentException e) { }
		}

		// Looks like there was an error in the settings
		if (!taskStatus) {
			if (!Debug.Enabled) {
				// Send the error message via email
				Debug.sendDebugEmail(this);
			} else {
				try {
				// Handle all errors we're capable of,
				// inform user of others
				switch (statusCode) {
				case 200: // Successful, but obviously something went wrong
					switch(requestStatus){
					case Parser.STATUS_TOO_MANY_DEVICES:
						ChoiceDialogFragment.newInstance(getString(R.string.too_many_device_partnerships_title), getString(R.string.too_many_device_partnerships_detail)).show(getSupportFragmentManager(), "tooManyDevices"); //$NON-NLS-1$
						break;
					default:
						ChoiceDialogFragment.newInstance(getString(R.string.unhandled_error, requestStatus), getString(R.string.unhandled_error_occured)).show(getSupportFragmentManager(), "tooManyDevices");
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
						//.create()
						.show(getSupportFragmentManager(), "forbidden");
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
					ChoiceDialogFragment.newInstance(getString(R.string.connection_failed_title), getString(R.string.connection_failed_detail, statusCode))
						.show(getSupportFragmentManager(), "connError");
					break;
				}
				} catch (java.lang.IllegalStateException e) {
					Debug.Log("Server configuration window was dismissed before Connection check was finished:\n" + e.toString());
			}
		}
		}
		// All went well. Store the settings and return to the main page
		else {
			mPreferences = getSharedPreferences(username, MODE_PRIVATE);
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
			Account account = new Account(username,
					getString(R.string.ACCOUNT_TYPE));
			AccountManager am = AccountManager.get(this);
			boolean accountCreated = am.addAccountExplicitly(account,
					getTextFromId(R.id.txtPassword), null);

			Bundle extras = getIntent().getExtras();
			if (extras != null && accountCreated) {
				AccountAuthenticatorResponse response = extras
						.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

				Bundle result = new Bundle();
				result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
				result.putString(AccountManager.KEY_ACCOUNT_TYPE,
						getString(R.string.ACCOUNT_TYPE));
				response.onResult(result);
			}

			// Close the activity
			finish();
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(11)
	@Override
	public void onChoiceDialogOptionPressed(int action) {
		switch(action) {
			case android.R.id.copy:
				if(Utility.isPreHoneycomb()) {
					final android.text.ClipboardManager clipboard;
					clipboard = (android.text.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
					clipboard.setText(activeSyncManager.getDeviceId());
				}
				else {
					ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					clip.setPrimaryClip(ClipData.newPlainText("Android Device ID", activeSyncManager.getDeviceId()));
				}
				break;
			case R.id.account_delete:
				getSharedPreferences(accountKey, MODE_PRIVATE).edit().clear().commit(); //remove all prefs
				AccountManager am = AccountManager.get(this);
				for (Account acc : am.getAccountsByType(getString(R.string.ACCOUNT_TYPE)))
				{
					if (acc.name.equals(accountKey))
						am.removeAccount(acc, new AccountManagerCallback<Boolean>() {
							@Override
							public void run(AccountManagerFuture<Boolean> future) {
								try {
									setResult(future.getResult() ? 1 : 0);
								} catch (OperationCanceledException e) {
								} catch (IOException e) {
								} catch (AuthenticatorException e) {
								}
								finish();
							}
						}, null);
					break;
				}
				break;
			case android.R.id.empty:
				setResult(0);
				finish();
			default:
				break;
		}
	}

}
