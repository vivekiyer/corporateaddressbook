package net.vivekiyer.GAL;

import android.content.Context;
import android.content.Intent;


/**
 * @author vivek
 *         Class that is responsible for enabling or disabling
 *         DEBUG messages
 */

public class Debug {

	// Set this to true to enable DEBUG messages
	public static boolean Enabled = true; //true;
	public static boolean Verbose = false; //true;

	// StringBuffer that stores logs
	private static final StringBuffer logger = new StringBuffer();

	public static void Log(String s) {
		logger.append(s);
		logger.append("\n"); //$NON-NLS-1$
		android.util.Log.v("CorporateAddressbook", s); //$NON-NLS-1$
	}

	public static void sendDebugEmail(Context context) {
		// Do not send any emails unless Debugging is enabled
		if (!Enabled)
			return;

		// Generate an email with the appropriate data
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain"); //$NON-NLS-1$
		String[] recipients = new String[]{"corporateaddressbook@googlegroups.com", "",}; //$NON-NLS-1$ //$NON-NLS-2$
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GAL log"); //$NON-NLS-1$
		intent.putExtra(
				android.content.Intent.EXTRA_TEXT,
				logger.toString()
		);

		context.startActivity(Intent.createChooser(intent, context.getString(R.string.sendEmail)));
	}
}
