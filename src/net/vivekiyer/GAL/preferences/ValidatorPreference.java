package net.vivekiyer.GAL.preferences;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import net.vivekiyer.GAL.R;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-11-18
 * Time: 23:51
 * To change this template use File | Settings | File Templates.
 */
public class ValidatorPreference extends Preference {

	public ValidatorPreference(Context context) {
		super(context);
	}

	public ValidatorPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ValidatorPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		View v = super.onCreateView(parent);    //To change body of overridden methods use File | Settings | File Templates.
		v.setBackgroundColor(getContext().getResources().getColor(R.color.header_background));
		return v;
	}

	@Override
	protected void onClick() {
		super.onClick();    //To change body of overridden methods use File | Settings | File Templates.
//		getPreferenceManager().
//		Context ctx = getContext();
//		ctx.getTheme();
	}
}
