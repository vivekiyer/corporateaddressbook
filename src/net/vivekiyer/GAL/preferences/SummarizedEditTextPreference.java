package net.vivekiyer.GAL.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;
import net.vivekiyer.GAL.R;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-09-28
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class SummarizedEditTextPreference extends EditTextPreference /*implements OnPreferenceChangeListener*/ {
	public SummarizedEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//setOnPreferenceChangeListener(this);
	}

	public SummarizedEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SummarizedEditTextPreference(Context context) {
		super(context);
	}

	@Override
	protected boolean persistString(String value) {
		return super.persistString(value);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	protected String getPersistedString(String defaultReturnValue) {
		return super.getPersistedString(defaultReturnValue);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		super.onSetInitialValue(restoreValue, defaultValue);    //To change body of overridden methods use File | Settings | File Templates.
		setSummary(getText());
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);    //To change body of overridden methods use File | Settings | File Templates.
		if(positiveResult)
			setSummary(getText());
	}

	@Override
	public void setSummary(CharSequence summary) {
		if(summary.length()==0)
			super.setSummary(getContext().getString(R.string.notSet));
		else
			if((this.getEditText().getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0)
					super.setSummary(new String(new char[summary.length()]).replace("\0", "*"));
			else
				super.setSummary(summary);    //To change body of overridden methods use File | Settings | File Templates.
	}
}