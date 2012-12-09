package net.vivekiyer.GAL.Preferences;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.ListView;
import net.vivekiyer.GAL.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-11-12
 * Time: 23:56
 * To change this template use File | Settings | File Templates.
 */
public class ServerPrefsFragment extends PrefsFragment {
	private String accountKey;
	private String accountServer;

	@Override
	protected void onBeforeAddPrefs() {
		super.onBeforeAddPrefs();    //To change body of overridden methods use File | Settings | File Templates.
		getPreferenceManager().setSharedPreferencesName(accountKey);
	}

	@Override
	public void onCreate(Bundle aSavedState) {
		accountKey = getArguments().getString(getString(R.string.KEY_ACCOUNT_KEY));
		if(accountKey == null)
			throw new RuntimeException("Unable to get accountKey for account");
		accountServer = getArguments().getString(getString(R.string.KEY_ACCOUNT_SERVER));
		super.onCreate(aSavedState, "pref_server");    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public void addPreferencesFromResource(int preferencesResId) {
		super.addPreferencesFromResource(preferencesResId);    //To change body of overridden methods use File | Settings | File Templates.
		addServerPreference(this.getPreferenceScreen());
	}

	void addServerPreference(PreferenceScreen screen) {
		Preference preference = screen.findPreference(getString(R.string.ACCOUNT_TYPE));
		preference.setTitle(accountKey);
		preference.setSummary(accountServer);
		Intent intent = preference.getIntent();
		intent.putExtra(getString(R.string.KEY_ACCOUNT_KEY), accountKey);
		preference = screen.findPreference(getString(R.string.ACTION_PREFS_ACCOUNT_DELETE));
		intent = preference.getIntent();
		intent.putExtra(getString(R.string.KEY_ACCOUNT_KEY), accountKey);
	}

}
