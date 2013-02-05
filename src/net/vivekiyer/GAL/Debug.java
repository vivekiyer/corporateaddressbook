package net.vivekiyer.GAL;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;


/**
 * Class that is responsible for enabling or disabling
 * DEBUG messages
 *
 * @author vivek
 */

public class Debug {

	// Set this to true to enable DEBUG messages
	private static boolean Enabled = true; //true;
	private static boolean Verbose = false; //true;

	// StringBuffer that stores logs
	private static final StringBuffer logger = new StringBuffer();

	/**
	 * Appends {@code s} to the application log and submits it to the system log
	 *
	 * @param s the {@link String} to log
	 */
	public static void Log(String s) {
		// Do not log unless Debugging is enabled
		if (!Enabled)
			return;

		logger.append(s);
		logger.append("\n"); //$NON-NLS-1$
		android.util.Log.v("CorporateAddressbook", s); //$NON-NLS-1$
	}

	public static void sendDebugEmail(final FragmentActivity context) {
		boolean allowedToSend = PreferenceManager.getDefaultSharedPreferences(App.getInstance())
				.getBoolean(context.getString(R.string.PREFS_KEY_PRIVACY_AGREED), false);
		if (!allowedToSend) {
			ChoiceDialogFragment.newInstance(context.getString(R.string.privacy_title),
					context.getString(R.string.privacy_details),
					context.getString(android.R.string.yes),
					context.getString(android.R.string.no),
					R.string.privacy_title,
					0)
					.setListener(new ChoiceDialogFragment.OnChoiceDialogOptionClickListener() {
						@Override
						public void onChoiceDialogOptionPressed(int action) {
							switch (action) {
								case R.string.privacy_title:
									PreferenceManager.getDefaultSharedPreferences(context)
											.edit()
											.putBoolean(context.getString(R.string.PREFS_KEY_PRIVACY_AGREED), true)
											.apply();
									sendDebugEmail(context);
							}
						}
					})
					.show(context.getSupportFragmentManager(), "privacyNote");
			return;
		}
		// Generate an email with the appropriate data
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain"); //$NON-NLS-1$
		String[] recipients = new String[]{"corporateaddressbook@googlegroups.com", "",}; //$NON-NLS-1$ //$NON-NLS-2$
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Corporate Addressbook log"); //$NON-NLS-1$
		intent.putExtra(
				android.content.Intent.EXTRA_TEXT,
				logger.toString()
		);

		context.startActivity(Intent.createChooser(intent, context.getString(R.string.sendEmail)));
	}

	public static void clear() {
		logger.setLength(0);
	}

	public static boolean isEnabled() {
		return Enabled;
	}

	public static void setEnabled(boolean enabled) {
		if (Enabled != enabled) {
			Enabled = enabled;
			PreferenceManager.getDefaultSharedPreferences(App.getInstance())
					.edit()
					.putBoolean(App.getInstance().getString(R.string.PREFS_KEY_DEBUGGING_ENABLED), enabled)
					.commit();
		}
	}

	public static boolean isVerbose() {
		return Verbose;
	}

	public static void setVerbose(boolean verbose) {
		Verbose = verbose;
	}
}
