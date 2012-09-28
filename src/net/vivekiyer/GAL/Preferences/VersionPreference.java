package net.vivekiyer.GAL.Preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VersionPreference extends Preference {
	
	public VersionPreference(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		try {
		    String version = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		    setSummary(version);
		} catch (NameNotFoundException e) {
		    Log.e("PrefsActivity", e.getMessage());
		}
	}
}