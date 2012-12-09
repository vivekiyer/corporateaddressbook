package net.vivekiyer.GAL.Preferences;

import android.accounts.*;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import net.vivekiyer.GAL.ActiveSyncManager;
import net.vivekiyer.GAL.App;
import net.vivekiyer.GAL.R;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-11-29
 * Time: 23:34
 * To change this template use File | Settings | File Templates.
 */
public class AddAccountActivity extends PreferenceActivity implements AccountManagerCallback<Bundle> {


	enum ValidationStatus {
		Undefined,
		FailedValidation,
		PassedValidation
	}
	private ValidationStatus status = ValidationStatus.Undefined;

	private SharedPreferences mPreferences;
	private ProgressDialog progressdialog;
	ActiveSyncManager activeSyncManager;
	private String domain;
	private String username;
	String accountName;

	@Override
	public void run(AccountManagerFuture<Bundle> future) {
		try {
			accountName = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);
			finish();
		} catch (OperationCanceledException e) {
		} catch (IOException e) {
		} catch (AuthenticatorException e) {
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getIntent().addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);
		super.onCreate(savedInstanceState);

		AccountManagerFuture<Bundle> future = AccountManager.get(App.getInstance())
				.addAccount(getString(R.string.ACCOUNT_TYPE), null, null, null, this, this, null);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		int result;
		if(accountName == null) {
			result = RESULT_CANCELED;
		}
		else {
			data.putExtra("accountName", accountName);
			result = RESULT_OK;
		}
		super.onActivityResult(requestCode, result, data);    //To change body of overridden methods use File | Settings | File Templates.
	}
}
