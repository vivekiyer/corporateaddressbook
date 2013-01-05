package net.vivekiyer.GAL.Preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PrefsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle aSavedState) {
		String fragment = getArguments().getString("pref-resource"); //NON-NLS
		onCreate(aSavedState, fragment);
	}

	public void onCreate(Bundle aSavedState, String prefResource) {
		super.onCreate(aSavedState);
		onBeforeAddPrefs();
		Context anAct = getActivity().getApplicationContext();
		int thePrefRes = anAct.getResources().getIdentifier(prefResource,
				"xml",anAct.getPackageName()); //NON-NLS
		addPreferencesFromResource(thePrefRes);
	}

	protected void onBeforeAddPrefs() {}
}