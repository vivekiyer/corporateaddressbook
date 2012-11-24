package net.vivekiyer.GAL.Preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DeviceIdPreference extends StaticDisplayPreference {

	public DeviceIdPreference(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
	    displayText = net.vivekiyer.GAL.ActiveSyncManager.getUniqueId();
	}

}