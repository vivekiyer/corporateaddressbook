package net.vivekiyer.GAL.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import net.vivekiyer.GAL.ContactWriterSdk5;
import net.vivekiyer.GAL.R;

import java.util.ArrayList;

/**
 * Custom adapter used to display account icons and descriptions in the
 * account spinner.
 */
public class AccountAdapter extends ArrayAdapter<AccountData> implements OnAccountsUpdateListener, SpinnerAdapter {

	public final static int FIRST_ACCOUNT = 0x300;
	private LayoutInflater layoutInflater;
	private Context context;
	private ArrayList<AccountData> mAccounts;

	public AccountAdapter(Context context,
	                      ArrayList<AccountData> accountData) {
		super(context, android.R.layout.simple_list_item_1, accountData);
		mAccounts = accountData;
		this.context = context;
		AccountManager.get(context).addOnAccountsUpdatedListener(this, null,
				true);

		layoutInflater = (LayoutInflater) context.getSystemService
				(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Inflate a view template
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.account_entry,
					parent, false);
		}
		TextView firstAccountLine = (TextView) convertView
				.findViewById(R.id.firstAccountLine);
		TextView secondAccountLine = (TextView) convertView
				.findViewById(R.id.secondAccountLine);
		ImageView accountIcon = (ImageView) convertView
				.findViewById(R.id.accountIcon);

		// Populate template
		AccountData data = getItem(position);
		firstAccountLine.setText(data.getName());
		secondAccountLine.setText(data.getTypeLabel());
		Drawable icon = data.getIcon();
		if (icon == null) {
			icon = context.getResources().getDrawable(
					android.R.drawable.ic_menu_search);
		}
		accountIcon.setImageDrawable(icon);
		return convertView;
	}

	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

	public View getDropDownView() {
		LinearLayout container = new LinearLayout(context);
		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		container.setOrientation(LinearLayout.VERTICAL);
		for (int i = 0; i < mAccounts.size(); i++) {
			AccountData ad = mAccounts.get(i);
			if (!ad.getType().equals(context.getString(R.string.ACCOUNT_TYPE))) {
				View v = getView(i, null, container);
				container.addView(v, layout);
			}
		}
		return container;
	}

	public SubMenu getMenuItems(SubMenu subMenu, final ContactWriterSdk5 contactWriter) {
		ArrayList<MenuItem> list = new ArrayList<MenuItem>();
		for (int i = 0; i < mAccounts.size(); i++) {
			final AccountData ad = mAccounts.get(i);
			if (!ad.getType().equals(context.getString(R.string.ACCOUNT_TYPE))) {
				MenuItem mi = subMenu.add(0, FIRST_ACCOUNT + i, i, ad.getName());
				mi.setIcon(ad.getIcon());
				mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						contactWriter.createContactEntry(ad);
						return true;  //To change body of implemented methods use File | Settings | File Templates.
					}
				});
			}
		}
		return subMenu;
	}

	@Override
	public void onAccountsUpdated(Account[] accounts) {
		//Log.i(TAG, "Account list update detected");
		updateAccountData(context, accounts, mAccounts);

		// Update the account spinner
		this.notifyDataSetChanged();
	}

	static void updateAccountData(Context context, Account[] accounts, ArrayList<AccountData> accountData) {

		// Clear out any old data to prevent duplicates
		accountData.clear();

		// Get account data from system
		AuthenticatorDescription[] accountTypes = AccountManager.get(context)
				.getAuthenticatorTypes();

		// Populate tables
		for (int i = 0; i < accounts.length; i++) {
			// The user may have multiple accounts with the same name, so we
			// need to construct a
			// meaningful display name for each.
			String systemAccountType = accounts[i].type;
			if (!systemAccountType.equals(context.getString(R.string.ACCOUNT_TYPE))) {
				AuthenticatorDescription ad = getAuthenticatorDescription(
						systemAccountType, accountTypes);
				AccountData data = new AccountData(accounts[i].name, ad);
				accountData.add(data);
			}
		}
	}

	/**
	 * Obtain the AuthenticatorDescription for a given account type.
	 *
	 * @param type       The account type to locate.
	 * @param dictionary An array of AuthenticatorDescriptions, as returned by
	 *                   AccountManager.
	 * @return The description for the specified account type.
	 */
	private static AuthenticatorDescription getAuthenticatorDescription(
			String type, AuthenticatorDescription[] dictionary) {
		for (int i = 0; i < dictionary.length; i++) {
			if (dictionary[i].type.equals(type)) {
				return dictionary[i];
			}
		}
		// No match found
		throw new RuntimeException("Unable to find matching authenticator"); //$NON-NLS-1$
	}

	@Override
	protected void finalize() throws Throwable {
		// Make sure listener is un-registered when this object is destructed, will otherwise cause leak
		AccountManager.get(context).removeOnAccountsUpdatedListener(this);
		super.finalize();
	}
}