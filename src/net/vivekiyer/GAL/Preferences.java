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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

/**
 * @author Vivek Iyer
 *
 * Class that handles the Preference pane. The summary value gets updated
 * every time the user updates a preference value. 
 */
public class Preferences extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	public static final String KEY_USERNAME_PREFERENCE = "username";
	public static final String KEY_PASSWORD_PREFERENCE = "password";
	public static final String KEY_DOMAIN_PREFERENCE = "domain";
	public static final String KEY_SERVER_PREFERENCE = "server";
	public static final String KEY_ACTIVESYNCVERSION_PREFERENCE = "activesyncversion";
	public static final String KEY_POLICY_KEY_PREFERENCE = "policykey";	

	private EditTextPreference mUsernamePreference;
	private EditTextPreference mDomainPreference;
	private EditTextPreference mServerPreference;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);		

		// Get a reference to the preferences
		mUsernamePreference = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_USERNAME_PREFERENCE);
		mDomainPreference = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_DOMAIN_PREFERENCE);
		mServerPreference = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_SERVER_PREFERENCE);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// Let's do something a preference value changes
		// We want to write the preference value into the summary
		if (key.equals(KEY_USERNAME_PREFERENCE)) {
			mUsernamePreference
					.setSummary(sharedPreferences.getString(key, ""));
		} else if (key.equals(KEY_DOMAIN_PREFERENCE)) {
			mDomainPreference.setSummary(sharedPreferences.getString(key, ""));
		} else if (key.equals(KEY_SERVER_PREFERENCE)) {
			mServerPreference.setSummary(sharedPreferences.getString(key, ""));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Setup the initial values
		SharedPreferences sharedPreferences = getPreferenceScreen()
				.getSharedPreferences();
		mUsernamePreference.setSummary(sharedPreferences.getString(
				KEY_USERNAME_PREFERENCE, ""));
		mDomainPreference.setSummary(sharedPreferences.getString(
				KEY_DOMAIN_PREFERENCE, ""));
		mServerPreference.setSummary(sharedPreferences.getString(
				KEY_SERVER_PREFERENCE, ""));

		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}
}
