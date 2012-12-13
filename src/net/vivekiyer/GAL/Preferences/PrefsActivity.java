package net.vivekiyer.GAL.Preferences;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.ListAdapter;
import android.widget.Toast;
import net.vivekiyer.GAL.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * ****************************
 *
 * @author ryanf@blackmoonit.com
 *         <p/>
 *         Borrowed from http://www.blackmoonit.com/2012/07/all_api_prefsactivity/
 */
public class PrefsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener, OnAccountsUpdateListener {
	protected Method mLoadHeaders = null;
	protected Method mHasHeaders = null;

	// extras that allow any preference activity to be launched as part of a wizard

	// show Back and Next buttons? takes boolean parameter
	// Back will then return RESULT_CANCELED and Next RESULT_OK
	static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";

	// add a Skip button?
	static final String EXTRA_PREFS_SHOW_SKIP = "extra_prefs_show_skip";

	// specify custom text for the Back or Next buttons, or cause a button to not appear
	// at all by setting it to null
	static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text";
	static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";
	private ArrayList<Header> mHeaders;
	private boolean mListeningToAccountUpdates = false;

	/**
	 * Checks to see if using new v11+ way of handling PrefsFragments.
	 *
	 * @return Returns false pre-v11, else checks to see if using headers.
	 */
	public boolean isNewV11Prefs() {
		if (mHasHeaders != null && mLoadHeaders != null) {
			try {
				return (Boolean) mHasHeaders.invoke(this);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		return false;
	}

	@Override
	public void finish() {
		super.finish();    //To change body of overridden methods use File | Settings | File Templates.
		//finishActivity(CorporateAddressBook.DISPLAY_CONFIGURATION_REQUEST);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle aSavedState) {
		//onBuildHeaders() will be called during super.onCreate()
		try {
			mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class);
			mHasHeaders = getClass().getMethod("hasHeaders");
		} catch (NoSuchMethodException e) {
		}

//		if(getIntent().hasExtra(EXTRA_SHOW_FRAGMENT)) {
//			String fragment = getIntent().getExtras().getString(EXTRA_SHOW_FRAGMENT);
//			if(fragment.equals(ServerPrefsFragment.class.getName())) {
//				Bundle extras = getIntent().getExtras();
////			    getIntent().putExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, true);
////			    getIntent().putExtra(EXTRA_PREFS_SET_NEXT_TEXT, "Validate");
////			    getIntent().putExtra(EXTRA_PREFS_SET_BACK_TEXT, getString(android.R.string.cancel));
//				Bundle args = extras.getBundle(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
//				if(args == null) {
//					args = new Bundle();
//					extras.putBundle(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
//				}
//				args.putString("pref-resource", "pref_server");
//			}
//		}

		super.onCreate(aSavedState);

//	    if(getIntent().hasExtra(EXTRA_SHOW_FRAGMENT)) {
//		    String fragment = getIntent().getExtras().getString(EXTRA_SHOW_FRAGMENT);
//		    if(fragment.equals(ServerPrefsFragment.class.getName())) {
//			    android.app.FragmentManager fmgr = getFragmentManager();
//			    Debug.Log(fmgr.toString());
//		    }
//	    }

		String action = getIntent().getAction();
		if (action != null && action.equals(getString(R.string.ACTION_PREFS_ACCOUNTS))) {
			addPreferencesFromResource(R.xml.pref_server);
//			addPreferencesFromResource(R.xml.pref_server_footer);
			PreferenceManager prefs = getPreferenceManager();
			Preference validator = prefs.findPreference("PREFS_VALIDATE");
			validator.setOnPreferenceClickListener(this);
		} else if (action != null && action.equals(getString(R.string.ACTION_PREFS_ABOUT))) {
			addPreferencesFromResource(R.xml.pref_about);
		} else if (!isNewV11Prefs()) {
//		    setListAdapter(new HeaderAdapter(this, ));
			addPreferencesFromResource(R.xml.pref_headers_legacy);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mListeningToAccountUpdates) {
			AccountManager.get(this).removeOnAccountsUpdatedListener(this);
			mListeningToAccountUpdates = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
		if (!mListeningToAccountUpdates) {
			AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true);
			mListeningToAccountUpdates = true;
		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();    //To change body of overridden methods use File | Settings | File Templates.
//		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(
//				new SharedPreferences.OnSharedPreferenceChangeListener() {
//			@Override
//			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//				SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).edit();
//				e.putBoolean(getString(R.string.PREFS_KEY_SUCCESSFULLY_CONNECTED), false);
//				e.commit();
//			}
//		});
	}

	@Override
	public void switchToHeader(Header header) {
		if (header.fragment == null && header.intent == null) {
			if (mHeaders == null)
				return;
			for (Header h : mHeaders) {
				if (header.fragment != null || header.intent != null)
					super.switchToHeader(header);
			}
		} else
			super.switchToHeader(header);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public void onBuildHeaders(List<Header> aTarget) {
		try {
			mLoadHeaders.invoke(this, new Object[]{R.xml.pref_headers, aTarget});
			addServerHeaders(aTarget);
			if (mHeaders == null) {
				mHeaders = new ArrayList<Header>();
				// When the saved state provides the list of headers, onBuildHeaders is not called
				// Copy the list of Headers from the adapter, preserving their order
			} else {
				mHeaders.clear();
			}
			for (Header h : aTarget) {
				mHeaders.add(h);
			}

		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
	}

	int addServerHeaders(List<Header> aTarget) {
		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));

		List<Header> newHeaders = new ArrayList<Header>();

		for (Account account : accounts) {
			Header h = new Header();
			h.title = am.getUserData(account, getString(R.string.KEY_ACCOUNT_KEY));
			h.fragment = ServerPrefsFragment.class.getName();
			Bundle b = new Bundle(2);
			b.putString("pref-resource", "pref_server");
			b.putString(getString(R.string.KEY_ACCOUNT_KEY), am.getUserData(account, getString(R.string.KEY_ACCOUNT_KEY)));
			SharedPreferences pref = getSharedPreferences(account.name, MODE_PRIVATE);
			String accountServer = pref.getString(getString(R.string.PREFS_KEY_SERVER_PREFERENCE), null);
			if (accountServer != null) {
				b.putString(getString(R.string.KEY_ACCOUNT_SERVER), accountServer);
			}
			h.fragmentArguments = b;

			newHeaders.add(h);
		}
		int serverHeaderIndex = 0;
		for (int i = 0; i < aTarget.size(); i++) {
			if (aTarget.get(i).id == R.id.server_heading) {
				serverHeaderIndex = i;
				break;
			}
		}
		aTarget.addAll(serverHeaderIndex + 1, newHeaders);

		if (!mListeningToAccountUpdates) {
			AccountManager.get(this).addOnAccountsUpdatedListener(this, null, false);
			mListeningToAccountUpdates = true;
		}

		return newHeaders.size();
	}

	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (adapter == null) {
			super.setListAdapter(null);
			return;
		}

		// Ignore the adapter provided by PreferenceActivity and substitute ours instead
		super.setListAdapter(new HeaderAdapter(this, mHeaders));
	}

	//	void refreshHeaders() {
//		List<Header> aTarget = new ArrayList<Header>();
//		try {
//			mLoadHeaders.invoke(this, new Object[]{R.xml.pref_headers, aTarget});
//			addServerHeaders(aTarget);
//			super.setListAdapter(new HeaderAdapter(this, aTarget));
//		} catch (IllegalArgumentException e) {
//		} catch (IllegalAccessException e) {
//		} catch (InvocationTargetException e) {
//		}
//	}
	@Override
	public void startActivity(android.content.Intent intents) {
		try {
			if (intents.getAction().equals(getString(R.string.ACTION_PREFS_ACCOUNT_DELETE)))
				startActivityForResult(intents, R.string.ACTION_PREFS_ACCOUNT_DELETE);
			else
				super.startActivity(intents);    //To change body of overridden methods use File | Settings | File Templates.
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "Can't find a suitable app", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
		if (requestCode == R.string.ACTION_PREFS_ACCOUNT_DELETE && resultCode == RESULT_OK) {
			if (!isNewV11Prefs())
				finish();
		}
	}

	@Override
	public void onAccountsUpdated(Account[] accounts) {
		invalidateHeaders();
		getListView().requestLayout();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {

		if (preference.getKey().equals("PREFS_VALIDATE")) {
			String prefkey = getString(R.string.PREFS_KEY_SERVER_PREFERENCE);
			PreferenceManager mgr = preference.getPreferenceManager();
			Preference servpref = mgr.findPreference(prefkey);
			EditTextPreference servtextpref = (EditTextPreference) servpref;
			String server = servtextpref.getText();


			Toast.makeText(this, "Yes...\n" + server, Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}
}