/**
 * 
 */
package net.vivekiyer.GAL;

import android.app.Application;

/**
 * @author Dan
 *
 */
public class App extends Application {

	private static App instance = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}

	public static App getInstance() {
		return instance;
	}	
}
