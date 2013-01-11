package net.vivekiyer.GAL.preferences;

import android.content.Context;
import android.preference.DialogPreference;
import android.text.ClipboardManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import net.vivekiyer.GAL.R;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-11-12
 * Time: 23:40
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("deprecation")
public class StaticDisplayPreference extends DialogPreference {
	protected String displayText = "";

	public StaticDisplayPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View onCreateView(ViewGroup viewGroup) {
		setSummary(displayText);
		return super.onCreateView(viewGroup);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	protected void onClick() {
		super.onClick();
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);    //To change body of overridden methods use File | Settings | File Templates.
		if(positiveResult) {
			final ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(displayText);
			Toast.makeText(getContext(), getContext().getString(R.string.deviceIdCopied), Toast.LENGTH_SHORT)
					.show();
		}
	}
}
