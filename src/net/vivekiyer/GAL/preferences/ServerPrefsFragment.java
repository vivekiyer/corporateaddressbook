package net.vivekiyer.GAL.preferences;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import net.vivekiyer.GAL.R;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-11-12
 * Time: 23:56
 * To change this template use File | Settings | File Templates.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ServerPrefsFragment extends PrefsFragment {
	private String accountKey;

	@Override
	protected void onBeforeAddPrefs() {
		super.onBeforeAddPrefs();    //To change body of overridden methods use File | Settings | File Templates.
		getPreferenceManager().setSharedPreferencesName(accountKey);
	}

	@Override
	public void onCreate(Bundle aSavedState) {
		accountKey = getArguments().getString(getString(R.string.KEY_ACCOUNT_KEY));
		if (accountKey == null)
			throw new RuntimeException("Unable to get accountKey for account");
		super.onCreate(aSavedState, "pref_server");    //To change body of overridden methods use File | Settings | File Templates. NON-NLS
	}

	@Override
	public void addPreferencesFromResource(int preferencesResId) {
		super.addPreferencesFromResource(preferencesResId);    //To change body of overridden methods use File | Settings | File Templates.
		PrefsActivity.addServerPreference(this.getPreferenceScreen(), accountKey);
	}

}
