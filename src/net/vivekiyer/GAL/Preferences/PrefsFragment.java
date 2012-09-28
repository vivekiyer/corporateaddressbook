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
        super.onCreate(aSavedState);
        Context anAct = getActivity().getApplicationContext();
        int thePrefRes = anAct.getResources().getIdentifier(getArguments().getString("pref-resource"),
                "xml",anAct.getPackageName());
        addPreferencesFromResource(thePrefRes);
    }
}