/**
 *
 */
package net.vivekiyer.GAL;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import net.vivekiyer.GAL.account.AccountAdapter;
import net.vivekiyer.GAL.account.AccountData;
import net.vivekiyer.GAL.account.AccountManager;
import net.vivekiyer.GAL.search.GALSearch;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author Dan
 */
public class App extends Application {

	private static App instance = null;
	private static AccountManager accounts = null;
	private static AccountAdapter systemAccounts = null;
	// Version String
	public static String VERSION_STRING;

	public static final Hashtable<Integer, GALSearch> taskManager = new Hashtable<Integer, GALSearch>();

	public static AccountManager getAccounts() {
		if (accounts == null)
			accounts = new AccountManager(App.getInstance());
		return accounts;
	}

	public static AccountAdapter getSystemAccounts() {
		if (systemAccounts == null)
			systemAccounts = new AccountAdapter(App.getInstance(), new ArrayList<AccountData>());
		return systemAccounts;
	}

	public App() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		// Get the displayText string
		App.VERSION_STRING = "CorporateAddressbook/" + getAppVersion(); //$NON-NLS-1$

		boolean debugEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.PREFS_KEY_DEBUGGING_ENABLED), isDebuggable());
		Debug.setEnabled(debugEnabled);

		if (Debug.isEnabled() && Debug.isVerbose()) {
			java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST); //$NON-NLS-1$
			java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST); //$NON-NLS-1$

			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug"); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug"); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug"); //$NON-NLS-1$ //$NON-NLS-2$

			System.setProperty("log.tag.org.apache.http.wire", "debug"); //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("log.tag.org.apache.http.headers", "debug"); //$NON-NLS-1$ //$NON-NLS-2$

			java.util.logging.Logger.getLogger("httpclient.wire.content").log(java.util.logging.Level.CONFIG, "hola"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static App getInstance() {
		return instance;
	}

	/**
	 * Returns the displayText of the application
	 *
	 * @return Version number of the application
	 */
	public String getAppVersion() {
		PackageManager manager = getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(
					getApplicationContext().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			return ""; //$NON-NLS-1$
		}
		return info.versionName;
	}

	public static boolean isDebuggable() {
		return (0 != (getInstance().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
	}
}
