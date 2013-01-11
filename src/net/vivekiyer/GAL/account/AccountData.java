package net.vivekiyer.GAL.account;

import android.accounts.AuthenticatorDescription;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import net.vivekiyer.GAL.App;

/**
 * A container class used to represent all known information about an
 * account.
 */
public class AccountData {
	private String mName;
	private String mType;
	private CharSequence mTypeLabel;
	private Drawable mIcon;

	/**
	 * @param name
	 *            The name of the account. This is usually the user's email
	 *            address or username.
	 * @param description
	 *            The description for this account. This will be dictated by
	 *            the type of account returned, and can be obtained from the
	 *            system AccountManager.
	 */
	public AccountData(String name, AuthenticatorDescription description) {
		mName = name;
		if (description != null) {
			mType = description.type;

			// The type string is stored in a resource, so we need to
			// convert it into something
			// human readable.
			String packageName = description.packageName;
			PackageManager pm = App.getInstance().getPackageManager();

			if (description.labelId != 0) {
				mTypeLabel = pm.getText(packageName, description.labelId,
						null);
				if (mTypeLabel == null) {
					throw new IllegalArgumentException(
							"LabelID provided, but label not found"); //$NON-NLS-1$
				}
			} else {
				mTypeLabel = ""; //$NON-NLS-1$
			}

			if (description.iconId != 0) {
				mIcon = pm.getDrawable(packageName, description.iconId,
						null);
				if (mIcon == null) {
					throw new IllegalArgumentException(
							"IconID provided, but drawable not " + "found"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				mIcon = App.getInstance().getResources().getDrawable(
						android.R.drawable.sym_def_app_icon);
			}
		}
	}

	public String getName() {
		return mName;
	}

	public String getType() {
		return mType;
	}

	public CharSequence getTypeLabel() {
		return mTypeLabel;
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public String toString() {
		return mName;
	}
}