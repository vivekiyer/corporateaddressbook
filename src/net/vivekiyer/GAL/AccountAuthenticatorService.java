package net.vivekiyer.GAL;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.util.Log;
import net.vivekiyer.GAL.Preferences.Configure;
import net.vivekiyer.GAL.Preferences.PrefsActivity;
import net.vivekiyer.GAL.Preferences.ServerPrefsFragment;

public class AccountAuthenticatorService extends Service {
	private static final String TAG = "AccountAuthenticatorService";
	private static AccountAuthenticatorImpl sAccountAuthenticator = null;

	public AccountAuthenticatorService() {
		super();
	}

	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		if (intent.getAction().equals(
				android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT))
			ret = getAuthenticator().getIBinder();
		return ret;
	}

	private AccountAuthenticatorImpl getAuthenticator() {
		if (sAccountAuthenticator == null)
			sAccountAuthenticator = new AccountAuthenticatorImpl(this);
		return sAccountAuthenticator;
	}

	private static class AccountAuthenticatorImpl extends
			AbstractAccountAuthenticator {
		private Context mContext;

		public AccountAuthenticatorImpl(Context context) {
			super(context);
			mContext = context;
		}

		/*
		 * The user has requested to add a new account to the system. We return
		 * an intent that will launch our login screen if the user has not
		 * logged in yet, otherwise our activity will just pass the user's
		 * credentials on to the account manager.
		 */
		@Override
		public Bundle addAccount(AccountAuthenticatorResponse response,
				String accountType, String authTokenType,
				String[] requiredFeatures, Bundle options)
				throws NetworkErrorException {

			Bundle result = new Bundle();
			Intent i = new Intent(mContext, Configure.class);
			i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
					response);
//			i.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
//					AddAccountActivity.class.getName());
			i.setAction(App.getInstance().getString(R.string.ACTION_PREFS_ACCOUNT_ADD));
			result.putParcelable(AccountManager.KEY_INTENT, i);
			return result;
		}

		@Override
		public Bundle confirmCredentials(AccountAuthenticatorResponse response,
				Account account, Bundle options) {
			// TODO Auto-generated method stub
			Log.i(TAG, "confirmCredentials");
			return null;
		}

		@Override
		public Bundle editProperties(AccountAuthenticatorResponse response,
				String accountType) {
			// TODO Auto-generated method stub
			Log.i(TAG, "editProperties");
			return null;
		}

		@Override
		public Bundle getAuthToken(AccountAuthenticatorResponse response,
				Account account, String authTokenType, Bundle options)
				throws NetworkErrorException {
			// TODO Auto-generated method stub
			Log.i(TAG, "getAuthToken");
			return null;
		}

		@Override
		public String getAuthTokenLabel(String authTokenType) {
			// TODO Auto-generated method stub
			Log.i(TAG, "getAuthTokenLabel");
			return null;
		}

		@Override
		public Bundle hasFeatures(AccountAuthenticatorResponse response,
				Account account, String[] features)
				throws NetworkErrorException {
			// TODO Auto-generated method stub
			Log.i(TAG, "hasFeatures: " + features);
			return null;
		}

		@Override
		public Bundle updateCredentials(AccountAuthenticatorResponse response,
				Account account, String authTokenType, Bundle options) {
			// TODO Auto-generated method stub
			Log.i(TAG, "updateCredentials");
			return null;
		}
	}
}