/**
 *
 */
package net.vivekiyer.GAL;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.util.Hashtable;

/**
 * @author Dan
 */
public class App extends Application {

	private static App instance = null;
	private static AccountManager accounts = null;
	// Version String
	public static String VERSION_STRING;

	public static final Hashtable<Integer, GALSearch> taskManager = new Hashtable<Integer, GALSearch>();

	public static AccountManager getAccounts() {
		return accounts;
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
		if (Debug.Enabled && Debug.Verbose) {
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

		accounts = new AccountManager(this);

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


}
