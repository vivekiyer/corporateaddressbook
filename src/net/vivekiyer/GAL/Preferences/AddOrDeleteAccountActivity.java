package net.vivekiyer.GAL.Preferences;

import java.io.IOException;

import net.vivekiyer.GAL.ActiveSyncManager;
import net.vivekiyer.GAL.App;
import net.vivekiyer.GAL.R;
import net.vivekiyer.GAL.Utility;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-11-29
 * Time: 23:34
 * To change this template use File | Settings | File Templates.
 */
@SuppressLint("NewApi")
public class AddOrDeleteAccountActivity extends FragmentActivity implements AccountManagerCallback<Bundle> {


	final static int accountDeleteRequestCode = 0xfab;

	enum ValidationStatus {
		Undefined,
		FailedValidation,
		PassedValidation
	}

	ActiveSyncManager activeSyncManager;
	String accountName;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	public void run(AccountManagerFuture<Bundle> future) {
		try {
			accountName = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);
			finish();
		} catch (OperationCanceledException e) {
		} catch (IOException e) {
		} catch (AuthenticatorException e) {
			AlertDialog.Builder builder;
			if (Utility.isPreHoneycomb())
				builder = new AlertDialog.Builder(this);
			else
				builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);

			builder.setMessage(e.getMessage())
					.setCancelable(false)
					.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.create()
					.show();
		} finally {
			//finish();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getIntent().addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);
		super.onCreate(savedInstanceState);

		final String accountAction = getIntent().getAction();
		if (accountAction.equals(getString(R.string.ACTION_PREFS_ACCOUNT_ADD))) {
			/*AccountManagerFuture<Bundle> future =*/ AccountManager.get(App.getInstance())
					.addAccount(getString(R.string.ACCOUNT_TYPE), null, null, null, this, this, null);
			finish();
		} else if (accountAction.equals(getString(R.string.ACTION_PREFS_ACCOUNT_DELETE))) {
			final String accountKey = getIntent().getStringExtra(getString(R.string.KEY_ACCOUNT_KEY));
			if (accountKey == null)
				throw new RuntimeException("No account supplied for deletion");

			AlertDialog.Builder builder;
			if (Utility.isPreHoneycomb())
				builder = new AlertDialog.Builder(this);
			else
				builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);

			builder.setTitle(getString(R.string.delete_account))
					.setMessage("Are you sure you want to delete the account \'" + accountKey + "\'?")
					.setCancelable(false)
					.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(getBaseContext(), Configure.class);
							intent.setAction(accountAction).addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
							intent.putExtra(getString(R.string.KEY_ACCOUNT_KEY), accountKey);
							startActivity(intent);
							finish();
						}
					})
					.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.create()
					.show();

			return;

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		int result;
//		if(accountName == null) {
//			result = RESULT_CANCELED;
//		}
//		else {
//			data.putExtra("accountName", accountName);
//			result = RESULT_OK;
//		}
		super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
		if (requestCode == R.string.ACTION_PREFS_ACCOUNT_DELETE) {
			finish();
		}
	}
}
