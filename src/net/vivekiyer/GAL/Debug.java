package net.vivekiyer.GAL;

import android.content.Context;
import android.content.Intent;


/**
 * @author vivek
 * Class that is responsible for enabling or disabling
 * DEBUG messages
 */

public class Debug {
	
	// Set this to true to enable DEBUG messages
	public static boolean Enabled = true;
	
	public static void sendDebugEmail(
			Context context,
			String text){
		// Generate an email with the appropriate data
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		String[] recipients = new String[]{"vivekiyer@gmail.com", "",};
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT,"GAL log");
		intent.putExtra(
				android.content.Intent.EXTRA_TEXT,
				text
		);

		context.startActivity(Intent.createChooser(intent, "Send mail..."));	
	}
}
