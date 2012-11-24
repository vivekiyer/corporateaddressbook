package net.vivekiyer.GAL.Preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VersionPreference extends StaticDisplayPreference {
	
	public VersionPreference(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		try {
		    displayText = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		    Log.e("VersionPreference", e.getMessage());
		}
	}
}