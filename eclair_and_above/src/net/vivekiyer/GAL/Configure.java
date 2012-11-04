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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author Vivek Iyer 
 *
 * This class handles the configuration pane for the application.
 */
public class Configure extends FragmentActivity implements OnClickListener, TaskCompleteCallback, ChoiceDialogFragment.OnChoiceDialogOptionClickListener {
	
	private SharedPreferences mPreferences;
	private ProgressDialog progressdialog;
	ActiveSyncManager activeSyncManager;
	private String domain;
	private String username;
	
	public static final String KEY_USERNAME_PREFERENCE = "username";
	public static final String KEY_PASSWORD_PREFERENCE = "password";
	public static final String KEY_DOMAIN_PREFERENCE = "domain";
	public static final String KEY_SERVER_PREFERENCE = "server";
	public static final String KEY_ACTIVESYNCVERSION_PREFERENCE = "activesyncversion";
	public static final String KEY_DEVICE_ID = "deviceid";
	public static final String KEY_DEVICE_ID_STRING = "deviceidstring";
	public static final String KEY_POLICY_KEY_PREFERENCE = "policykey";	
	public static final String KEY_USE_SSL = "usessl";
	public static final String KEY_ACCEPT_ALL_CERTS = "acceptallcerts";
	public static final String KEY_SUCCESSFULLY_CONNECTED = "successfullyConnected";
	
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

		// Get the preferences that were entered by the user and display those to the user 
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);			
		
		String domain = mPreferences.getString(KEY_DOMAIN_PREFERENCE , ""), user = mPreferences.getString(KEY_USERNAME_PREFERENCE, "");
		user = domain + (domain.length() > 0 ? "\\" : "") + user;
		if (user.length() > 0)
			setTextForId(R.id.txtDomainUserName, user);
		setTextForId(
				R.id.txtPassword, 
				mPreferences.getString(KEY_PASSWORD_PREFERENCE, ""));
		setTextForId(R.id.txtServerName, 
				mPreferences.getString(KEY_SERVER_PREFERENCE, ""));
		setValueForCheckbox(
				R.id.chkUseSSL,
				mPreferences.getBoolean(KEY_USE_SSL, true));
		setValueForCheckbox(
				R.id.chkAcceptAllSSLCert,
				mPreferences.getBoolean(KEY_ACCEPT_ALL_CERTS, true));
		
		EditText text = (EditText) findViewById(R.id.txtServerName);
		text.setOnEditorActionListener(new EditText.OnEditorActionListener() {
		   @Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
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
			actionBar.setDisplayHomeAsUpEnabled(true);
//			actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
		}
	}	
	
	/**
	 * @param id The id for the UI element 
	 * @param s The value to set the text to
	 * 
	 * Sets the text for the EditText UI element to the provided value
	 */
	private void setTextForId(int id, String s){
		EditText text = (EditText) findViewById(id);
		text.setText(s);
	}
	
	/**
	 * @param id The id for the UI element
	 * @return The value the text is set to
	 * 
	 * Gets the text that the EditText UI element is set to
	 */
	private String getTextFromId(int id){
		EditText text = (EditText) findViewById(id);
		return text.getText().toString();
	}
	
	private boolean getValueFromCheckbox(int id){
        final CheckBox checkBox = (CheckBox) findViewById(id);
        return checkBox.isChecked();
	}

	private void setValueForCheckbox(int id, boolean value){
		final CheckBox checkBox = (CheckBox) findViewById(id);
		checkBox.setChecked(value);
	}
	/**
	 * @param s The alert message
	 * Displays an alert dialog with the messaged provided
	 */
	private void showAlert(String s){
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(s)
				.setPositiveButton("Ok", null);
		AlertDialog alert = alt_bld.create();
		alert.show();
	}
	
	
	/**
	 * Validates the user entries and connects to the Exchange server 
	 */
	private void connect(){
		// Make sure that the user has entered the username
		// password and the server name
		if (getTextFromId(R.id.txtDomainUserName).equals("")) {
			showAlert(getString(R.string.valid_domain_and_username_error));
			return;
		}
		
		String[] splits = getTextFromId(R.id.txtDomainUserName).split("\\\\");		
		
		if(splits.length == 1) {
			domain = "";
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
		
		if (username.equalsIgnoreCase("")) {
			showAlert(getString(R.string.invalid_username_error));
			return;
		}
		
		if (getTextFromId(R.id.txtPassword).equalsIgnoreCase("")){
			showAlert(getString(R.string.invalid_password_error));
			return;			
		}
			
		if (getTextFromId(R.id.txtServerName).equalsIgnoreCase("") ){
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
				"", 
				"",
				null);

		// If we get an error from Initialize
		// That means the URL is just bad
		// display an error
		if(!activeSyncManager.Initialize()){
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
	
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * 
	 * Called when the user clicks the Log In button
	 */
	@Override
	public void onClick(View v) {		
		connect();
	}

	
	/* (non-Javadoc)
	 * @see net.vivekiyer.GAL.TaskCompleteCallback#taskComplete(boolean)
	 * 
	 * When the connection check is complete, depending upon the outcome
	 * either quit this activity, or ask the user to fix the issue
	 */
	@Override
	public void taskComplete(
			boolean taskStatus, 
			int statusCode,
			int requestStatus,
			String errorString) {		
		progressdialog.dismiss();

		// Looks like there was an error in the settings
		if (!taskStatus) {
			if(Debug.Enabled){
				// Send the error message via email				
				Debug.sendDebugEmail(this);
			}
			else
			{				
				// Handle all errors we're capable of,
				// inform user of others
				switch (statusCode){
				case 200: // Successful, but obviously something went wrong
					switch(requestStatus){
					case Parser.STATUS_TOO_MANY_DEVICES:
						ChoiceDialogFragment.newInstance(getString(R.string.too_many_device_partnerships_title), getString(R.string.too_many_device_partnerships_detail)).show(getSupportFragmentManager(), "tooManyDevices");
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
						.show(getSupportFragmentManager(), "forbidden");
					break;
				case ConnectionChecker.SSL_PEER_UNVERIFIED:
					ChoiceDialogFragment.newInstance(getString(R.string.unable_to_find_matching_certificate), getString(R.string.acceptAllSllText))
						.show(getSupportFragmentManager(), "SslUnverified");
					break;
				case ConnectionChecker.UNKNOWN_HOST:
					ChoiceDialogFragment.newInstance(getString(R.string.invalid_server_title), getString(R.string.invalid_server_detail)).show(getSupportFragmentManager(), "SslUnverified");
					break;
				default:
					ChoiceDialogFragment.newInstance(getString(R.string.connection_failed_title), getString(R.string.connection_failed_detail, statusCode))
						.show(getSupportFragmentManager(), "connError");
					break;				 
				}
			}
		}
		// All went well. Store the settings and return to the main page
		else{
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString(KEY_SERVER_PREFERENCE,
					getTextFromId(R.id.txtServerName).trim());
			editor.putString(KEY_DOMAIN_PREFERENCE,
					domain);
			editor.putString(KEY_USERNAME_PREFERENCE,
					username);			
			editor.putString(KEY_PASSWORD_PREFERENCE,
					getTextFromId(R.id.txtPassword));
			editor.putBoolean(KEY_USE_SSL, 
					getValueFromCheckbox(R.id.chkUseSSL));
			editor.putBoolean(KEY_ACCEPT_ALL_CERTS, 
					getValueFromCheckbox(R.id.chkAcceptAllSSLCert));			
			editor.putString(KEY_ACTIVESYNCVERSION_PREFERENCE,
					activeSyncManager.getActiveSyncVersion());
			editor.putString(KEY_DEVICE_ID_STRING,
					activeSyncManager.getDeviceId());
			editor.putString(KEY_POLICY_KEY_PREFERENCE,
					activeSyncManager.getPolicyKey());
			editor.putBoolean(Configure.KEY_SUCCESSFULLY_CONNECTED,
					true);

			// Commit the edits!
			editor.commit();		
			
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
		}
	}

}
