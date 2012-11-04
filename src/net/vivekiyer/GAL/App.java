/**
 * 
 */
package net.vivekiyer.GAL;

import java.util.Hashtable;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * @author Dan
 *
 */
public class App extends Application {

	private static App instance = null;
	// Version String
	public static String VERSION_STRING;
	
	public static final Hashtable<Integer, GALSearch> taskManager = new Hashtable<Integer, GALSearch>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		// Get the version string
		App.VERSION_STRING = "CorporateAddressbook/"+getAppVersion();
	}

	public static App getInstance() {
		return instance;
	}	
	
	/**
	 * Returns the version of the application
	 * @return Version number of the application
	 */
	public String getAppVersion(){
		PackageManager manager = getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(
					getApplicationContext().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			return "";
		}
		return info.versionName;
	}
	
	
}
