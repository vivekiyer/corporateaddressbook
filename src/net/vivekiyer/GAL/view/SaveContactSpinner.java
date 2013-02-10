package net.vivekiyer.GAL.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SpinnerAdapter;
import com.actionbarsherlock.internal.widget.IcsSpinner;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2013-02-08
 * Time: 23:29
 * To change this template use File | Settings | File Templates.
 */
public class SaveContactSpinner extends IcsSpinner {

	public SaveContactSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SaveContactSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setAdapter(SpinnerAdapter adapter) {
		super.setAdapter(adapter);    //To change body of overridden methods use File | Settings | File Templates.
//		if(adapter instanceof AccountAdapter)
//			setEnabled(((SpinnerAccountAdapter) adapter).hasAccounts());
//		else
//			throw new IllegalArgumentException(this.getClass().getSimpleName() + " requires adapter of type " +
//				AccountAdapter.class.getSimpleName());
	}
}
