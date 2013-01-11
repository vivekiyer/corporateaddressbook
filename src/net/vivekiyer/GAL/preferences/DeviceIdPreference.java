package net.vivekiyer.GAL.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import net.vivekiyer.GAL.search.ActiveSyncManager;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DeviceIdPreference extends StaticDisplayPreference {

	public DeviceIdPreference(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
	    displayText = ActiveSyncManager.getUniqueId();
	}

}