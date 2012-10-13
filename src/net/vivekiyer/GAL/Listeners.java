package net.vivekiyer.GAL;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.devoteam.quickaction.QuickActionWindow;

public class Listeners {
	
	public static OnClickListener getCallListener(final String telNo) {
		return getCallListener(telNo, null);
	}

	public static OnClickListener getCallListener(final String telNo,
		final QuickActionWindow qa) {
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent  intent = new Intent(
						Intent.ACTION_DIAL, 
						Uri.parse("tel:"+telNo));
				v.getContext().startActivity(intent);
				if(qa != null)
					qa.dismiss();
			}
			
		};
	}

	public static OnClickListener getSmsListener(final String telNo) {
		return getSmsListener(telNo, null);
	}

	public static OnClickListener getSmsListener(final String telNo,
		final QuickActionWindow qa) {
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent  intent = new Intent(
						Intent.ACTION_SENDTO, 
						Uri.parse("smsto:"+telNo));
				try {
					v.getContext().startActivity(intent);
					if(qa != null)
						qa.dismiss();					
				} catch (android.content.ActivityNotFoundException e) {
					Toast.makeText(v.getContext(), R.string.could_not_find_sms_application , Toast.LENGTH_SHORT).show();
				}
			}
			
		};
	}

	public static OnClickListener getMailListener(final String mailAddress) {
		return getMailListener(mailAddress, null);
	}
	public static OnClickListener getMailListener(final String mailAddress,
		final QuickActionWindow qa) {
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent  intent = new Intent(
						Intent.ACTION_SENDTO, 
						Uri.parse("mailto:"+mailAddress));
				v.getContext().startActivity(intent);
				if(qa != null)
					qa.dismiss();
			}
			
		};
	}

	public static OnClickListener getCopyListener(final String text) {
		return getCopyListener(text, null);
	}

	public static OnClickListener getCopyListener(final String text,
		final QuickActionWindow qa) {
		return new OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				final android.text.ClipboardManager clipboard = (android.text.ClipboardManager) v.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
				clipboard.setText(text);
				Toast.makeText(v.getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT)
						.show();
				if(qa != null)
					qa.dismiss();
			}
		};
	}
}