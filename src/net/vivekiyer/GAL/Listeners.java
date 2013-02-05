package net.vivekiyer.GAL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.devoteam.quickaction.QuickActionWindow;
import net.vivekiyer.GAL.view.QuickActionView;

public class Listeners {
	
	public static OnClickListener getCallListener(final String telNo) {
		return getCallListener(telNo, null);
	}

	public static OnClickListener getCallListener(final String telNo,
		final Object qa) {
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent  intent = new Intent(
						Intent.ACTION_DIAL, 
						Uri.parse("tel:"+telNo)); //$NON-NLS-1$
				v.getContext().startActivity(intent);
				if(qa != null)
					if(qa instanceof QuickActionView)
						((QuickActionView)qa).setVisibility(View.GONE);
					else if(qa instanceof QuickActionWindow)
						((QuickActionWindow)qa).dismiss();
					else
						throw new IllegalArgumentException("getSmsListener must have QuickActionWindow or QuickActionView target");
			}
			
		};
	}

	public static MenuItem.OnMenuItemClickListener getCallMenuListener(final Context context, final String phone) {
		return new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem mi) {
				Intent  intent = new Intent(
						Intent.ACTION_DIAL,
						Uri.parse("tel:"+phone)); //$NON-NLS-1$
				try {
					context.startActivity(intent);
					return true;
				} catch (android.content.ActivityNotFoundException e) {
					Toast.makeText(context, R.string.could_not_find_email_application, Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		};
	}

	public static OnClickListener getSmsListener(final String telNo) {
		return getSmsListener(telNo, null);
	}

	public static OnClickListener getSmsListener(final String telNo,
		final Object qa) {
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			Intent  intent = new Intent(
					Intent.ACTION_SENDTO,
					Uri.parse("smsto:"+telNo)); //$NON-NLS-1$
			try {
				v.getContext().startActivity(intent);
				if(qa != null) {
					if(qa instanceof QuickActionView)
						((QuickActionView)qa).setVisibility(View.GONE);
					else if(qa instanceof QuickActionWindow)
						((QuickActionWindow)qa).dismiss();
					else
						throw new IllegalArgumentException("getSmsListener must have QuickActionWindow or QuickActionView target");
				}
			} catch (android.content.ActivityNotFoundException e) {
				Toast.makeText(v.getContext(), R.string.could_not_find_sms_application , Toast.LENGTH_SHORT).show();
			}
			}
			
		};
	}

	public static MenuItem.OnMenuItemClickListener getSmsMenuListener(final Context context, final String phone) {
		return new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem mi) {
				Intent  intent = new Intent(
						Intent.ACTION_SENDTO,
						Uri.parse("smsto:"+phone)); //$NON-NLS-1$
				try {
					context.startActivity(intent);
					return true;
				} catch (android.content.ActivityNotFoundException e) {
					Toast.makeText(context, R.string.could_not_find_sms_application, Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		};
	}
	public static OnClickListener getMailListener(final String mailAddress) {
		return getMailListener(mailAddress, null);
	}
	public static OnClickListener getMailListener(final String mailAddress,
	                                              final Object qa) {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent  intent = new Intent(
						Intent.ACTION_SENDTO,
						Uri.parse("mailto:"+mailAddress)); //$NON-NLS-1$

				try {
					v.getContext().startActivity(intent);
					if(qa != null)
						if(qa instanceof QuickActionView)
							((QuickActionView)qa).setVisibility(View.GONE);
						else if(qa instanceof QuickActionWindow)
							((QuickActionWindow)qa).dismiss();
						else
							throw new IllegalArgumentException("getSmsListener must have QuickActionWindow or QuickActionView target");
				} catch (android.content.ActivityNotFoundException e) {
					Toast.makeText(v.getContext(), R.string.could_not_find_email_application, Toast.LENGTH_SHORT).show();
				}
			}

		};
	}
	public static MenuItem.OnMenuItemClickListener getMailMenuListener(final Context context, final String mailAddress) {
		return new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem mi) {
				Intent  intent = new Intent(
						Intent.ACTION_SENDTO,
						Uri.parse("mailto:"+mailAddress)); //$NON-NLS-1$
				try {
					context.startActivity(intent);
					return true;
				} catch (android.content.ActivityNotFoundException e) {
					Toast.makeText(context, R.string.could_not_find_email_application, Toast.LENGTH_SHORT).show();
				}
				return false;
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
				Toast.makeText(v.getContext(), v.getContext().getString(R.string.text_copied_to_clipboard), Toast.LENGTH_SHORT)
						.show();
				if(qa != null)
					qa.dismiss();
			}
		};
	}

	public static MenuItem.OnMenuItemClickListener getCopyMenuListener(final Context context, final String text) {
		return new MenuItem.OnMenuItemClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public boolean onMenuItemClick(MenuItem mi) {
				final android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
				clipboard.setText(text);
				Toast.makeText(context, context.getString(R.string.text_copied_to_clipboard), Toast.LENGTH_SHORT)
						.show();
				return true;
			}
		};
	}
}