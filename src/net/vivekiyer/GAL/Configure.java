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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Vivek Iyer 
 *
 * This class handles the configuration pane for the application.
 */
public class Configure extends Activity implements OnClickListener, TaskCompleteCallback{
	
	private SharedPreferences mPreferences;
	private ProgressDialog progressdialog;
	ActiveSyncManager activeSyncManager;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * 
	 * Called when the configuration pane is first launched
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configure);

		// Hook up the on click handler for the button
		Button button = (Button) findViewById(R.id.buttonSignIn);
		button.setOnClickListener(this);

		// Get the preferences that were entered by the user and display those to the user 
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);			
		
		setTextForId(
				R.id.txtUserName, 
				mPreferences.getString(Preferences.KEY_USERNAME_PREFERENCE, ""));
		setTextForId(
				R.id.txtPassword, 
				mPreferences.getString(Preferences.KEY_PASSWORD_PREFERENCE, ""));
		setTextForId(
				R.id.txtDomain, 
				mPreferences.getString(Preferences.KEY_DOMAIN_PREFERENCE, ""));
		setTextForId(R.id.txtServerName, 
				mPreferences.getString(Preferences.KEY_SERVER_PREFERENCE, ""));
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
	
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * 
	 * Called when the user clicks the Log In button
	 */
	@Override
	public void onClick(View v) {		
		// Make sure that the user has entered the username
		// password and the server name
		if (getTextFromId(R.id.txtUserName) == "") {			
			showAlert("Please provide a valid username");
			return;
		}
		
		if (getTextFromId(R.id.txtPassword) == ""){
			showAlert("Please provide a valid password");
			return;			
		}
			
		if (getTextFromId(R.id.txtServerName) == "" ){
			showAlert("Please provide a valid Exchange URL");
			return;
		}
		
		// Now that we have all three
		// Lets validate it
		
		activeSyncManager = new ActiveSyncManager(
				getTextFromId(R.id.txtServerName),
				getTextFromId(R.id.txtDomain),
				getTextFromId(R.id.txtUserName),
				getTextFromId(R.id.txtPassword), 
				"", 
				"");

		activeSyncManager.Initialize();
		
		progressdialog = new ProgressDialog(this);
		progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog.setMessage("Validating settings");
		progressdialog.setCancelable(false);
		progressdialog.show();		
		ConnectionChecker checker = new ConnectionChecker(this);
		checker.execute(activeSyncManager);
	}

	/* (non-Javadoc)
	 * @see net.vivekiyer.GAL.TaskCompleteCallback#taskComplete(boolean)
	 * 
	 * When the connection check is complete, depending upon the outcome
	 * either quit this activity, or ask the user to fix the issue
	 */
	@Override
	public void taskComplete(boolean taskStatus) {		
		progressdialog.dismiss();
		
		// Looks like there was an error in the settings
		if (!taskStatus) {
			AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
			alt_bld.setMessage(
					"Error connecting to server. Please check your settings")
					.setPositiveButton("Ok", null);
			AlertDialog alert = alt_bld.create();
			alert.show();
		}
		// All went well. Store the settings and return to the main page
		else{
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString(Preferences.KEY_SERVER_PREFERENCE,
					getTextFromId(R.id.txtServerName));
			editor.putString(Preferences.KEY_DOMAIN_PREFERENCE,
					getTextFromId(R.id.txtDomain));
			editor.putString(Preferences.KEY_USERNAME_PREFERENCE,
					getTextFromId(R.id.txtUserName));			
			editor.putString(Preferences.KEY_PASSWORD_PREFERENCE,
					getTextFromId(R.id.txtPassword));
			
			editor.putString(Preferences.KEY_ACTIVESYNCVERSION_PREFERENCE,
					activeSyncManager.getActiveSyncVersion());
			editor.putString(Preferences.KEY_POLICY_KEY_PREFERENCE,
					activeSyncManager.getPolicyKey());

			// Commit the edits!
			editor.commit();
			
			// Close the activity
			finish();
		}
			
	}
}
