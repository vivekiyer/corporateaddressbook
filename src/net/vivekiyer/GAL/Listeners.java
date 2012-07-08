package net.vivekiyer.GAL;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.devoteam.quickaction.QuickActionWindow;

public class Listeners {
	
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

	public static OnClickListener getSmsListener(final String telNo,
			final QuickActionWindow qa) {
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent  intent = new Intent(
						Intent.ACTION_SENDTO, 
						Uri.parse("smsto:"+telNo));
				v.getContext().startActivity(intent);
				if(qa != null)
					qa.dismiss();
			}
			
		};
	};

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
