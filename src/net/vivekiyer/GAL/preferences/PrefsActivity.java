package net.vivekiyer.GAL.preferences;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.widget.ListAdapter;
import android.widget.Toast;
import net.vivekiyer.GAL.App;
import net.vivekiyer.GAL.R;
import net.vivekiyer.GAL.Utility;

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
	static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar"; //NON-NLS

	// add a Skip button?
	static final String EXTRA_PREFS_SHOW_SKIP = "extra_prefs_show_skip"; //NON-NLS

	// specify custom text for the Back or Next buttons, or cause a button to not appear
	// at all by setting it to null
	static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text"; //NON-NLS
	static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text"; //NON-NLS
	private ArrayList<Header> mHeaders = new ArrayList<Header>();
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

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle aSavedState) {
		//onBuildHeaders() will be called during super.onCreate()
		try {
			mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class); //NON-NLS
			mHasHeaders = getClass().getMethod("hasHeaders"); //NON-NLS
		} catch (NoSuchMethodException e) {
		}

		super.onCreate(aSavedState);
		Bundle b = getIntent().getExtras();

		String action = getIntent().getAction();
		if (action != null && action.equals(getString(R.string.ACTION_PREFS_ACCOUNTS))) {
			String accountKey = getIntent().getStringExtra(getString(R.string.KEY_ACCOUNT_KEY));
			if(accountKey == null)
				throw new IllegalArgumentException("No Account Key supplied for pref-server");
			getPreferenceManager().setSharedPreferencesName(accountKey);
			addPreferencesFromResource(R.xml.pref_server);
			addServerPreference(getPreferenceScreen(), accountKey);

		} else if (action != null && action.equals(getString(R.string.ACTION_PREFS_ABOUT))) {
			addPreferencesFromResource(R.xml.pref_about);
		} else if (!isNewV11Prefs()) {
			addNonHeaderPrefs();
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
		super.onPostResume();
	}

	@Override
	public void finish() {
		super.finish();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void switchToHeader(Header header) {
		if (header.fragment == null && header.intent == null) {
			if (mHeaders.size() == 0)
				return;
			for (Header h : mHeaders) {
				if (h.fragment != null /*|| h.intent != null*/) {
					super.switchToHeader(h);
				}
			}
		} else
			super.switchToHeader(header);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onBuildHeaders(List<Header> aTarget) {
		try {
			mLoadHeaders.invoke(this, new Object[]{R.xml.pref_headers, aTarget});
			addServerHeaders(aTarget);
				// When the saved state provides the list of headers, onBuildHeaders is not called
				// Copy the list of Headers from the adapter, preserving their order
			mHeaders.clear();
			for (Header h : aTarget) {
				mHeaders.add(h);
			}

		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	int addServerHeaders(List<Header> aTarget) {
		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));

		List<Header> newHeaders = new ArrayList<Header>();

		for (Account account : accounts) {
			Header h = new Header();
			h.title = am.getUserData(account, getString(R.string.KEY_ACCOUNT_KEY));
			h.fragment = ServerPrefsFragment.class.getName();
			Bundle b = new Bundle(2);
			b.putString("pref-resource", "pref_server"); //NON-NLS NON-NLS
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

	@SuppressWarnings("deprecation")
	private void addNonHeaderPrefs() {
		PreferenceCategory preferenceCategory = null;
		PreferenceScreen screen = getPreferenceScreen();

		if(screen != null)
			screen.removeAll();
		addPreferencesFromResource(R.xml.pref_headers_legacy);
		screen = getPreferenceScreen();

		for(int i = 0; i < screen.getPreferenceCount();i++) {
			Preference p = screen.getPreference(i);
			if(p != null && p instanceof PreferenceCategory && getString(R.id.server_heading).equals(p.getKey())) {
				preferenceCategory = (PreferenceCategory) screen.getPreference(i);
			}
		}

		//List<Header> newHeaders = new ArrayList<Header>();
		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));

		for (Account account : accounts) {
			Preference accountPrefs = new Preference(this);
			accountPrefs.setTitle(am.getUserData(account, getString(R.string.KEY_ACCOUNT_KEY)));
			Intent i = new Intent(getString(R.string.ACTION_PREFS_ACCOUNTS));
			i.setClass(this, this.getClass());
			i.putExtra(getString(R.string.KEY_ACCOUNT_KEY), am.getUserData(account, getString(R.string.KEY_ACCOUNT_KEY)));
			accountPrefs.setIntent(i);
			assert preferenceCategory != null;
			preferenceCategory.addPreference(accountPrefs);
		}
	}

	static void addServerPreference(PreferenceScreen screen, String accountKey) {
		final Context ctx = App.getInstance();
		SharedPreferences accountPrefs = ctx.getSharedPreferences(accountKey, MODE_PRIVATE);
		String domain = accountPrefs.getString(ctx.getString(R.string.PREFS_KEY_DOMAIN_PREFERENCE), "");
		String user = accountPrefs.getString(ctx.getString(R.string.PREFS_KEY_USERNAME_PREFERENCE), "");
		String accountServer = accountPrefs.getString(ctx.getString(R.string.PREFS_KEY_SERVER_PREFERENCE), "");
		Preference preference = screen.findPreference(ctx.getString(R.string.ACCOUNT_TYPE));
		if (domain.length() == 0) {
			preference.setTitle(user);
		} else {
			preference.setTitle(domain + "\\" + user);
		}
		preference.setSummary(accountServer);
		Intent intent = preference.getIntent();
		intent.putExtra(ctx.getString(R.string.KEY_ACCOUNT_KEY), accountKey);
		preference = screen.findPreference(ctx.getString(R.string.ACTION_PREFS_ACCOUNT_DELETE));
		intent = preference.getIntent();
		intent.putExtra(ctx.getString(R.string.KEY_ACCOUNT_KEY), accountKey);
	}

	@Override
	public void startActivity(android.content.Intent intents) {
		try {
			if (intents.getAction().equals(getString(R.string.ACTION_PREFS_ACCOUNT_DELETE))) {
				// So that we get to know if an account is deleted - if this is a ServerPrefs activity
				// related to that account it should be finished.
				startActivityForResult(intents, R.string.ACTION_PREFS_ACCOUNT_DELETE);
			}
			else {
				super.startActivity(intents);
			}
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, getString(R.string.cantFindApp), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == R.string.ACTION_PREFS_ACCOUNT_DELETE && resultCode == RESULT_OK) {
			if (!isNewV11Prefs() || mHeaders.size() == 0)
				finish();
			else {
				// Make sure we switch header so that the fragment for the deleted
				// account is discarded
				// getFragmentManager().popBackStack() didn't work
				for(Header h : mHeaders) {
					if(PrefsFragment.class.getName().equals(h.fragment))
						switchToHeader(h);
				}
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onAccountsUpdated(Account[] accounts) {
		if(!Utility.isPreHoneycomb()) {
			if(mHeaders.size() > 0) {
				invalidateHeaders();
				getListView().requestLayout();
			}
		}
		else {
			if(getIntent().getAction() == null)
				addNonHeaderPrefs();
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}
}