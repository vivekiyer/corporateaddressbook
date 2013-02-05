package net.vivekiyer.GAL.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import net.vivekiyer.GAL.R;
import net.vivekiyer.GAL.Utility;

public class ContactRowView extends RelativeLayout implements Checkable {

	private Boolean isChecked = false;

	public ContactRowView(Context context) {
		super(context);
	}

	public ContactRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ContactRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		findViewById(R.id.selectedMark).setVisibility(View.GONE);
	}

	@Override
	public boolean isChecked() {
		return isChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		if (isChecked == checked)
			return;

		if (isChecked = checked) {
//			((TextView)findViewById(R.id.toptext)).setTextColor(android.R.color.white);
//			((TextView)findViewById(R.id.bottomtext)).setTextColor(android.R.color.white);
			if (Utility.isPreJellyBean()) {
				this.setBackgroundDrawable(getResources().getDrawable(R.drawable.shaded_background_holo_blue));
			} else {
				this.setBackground(getResources().getDrawable(R.drawable.shaded_background_holo_blue));
			}
			findViewById(R.id.selectedMark).setVisibility(View.VISIBLE);
		} else {
//			((TextView)findViewById(R.id.toptext)).setTextColor(android.R.color.black);
//			((TextView)findViewById(R.id.bottomtext)).setTextColor(android.R.color.black);
			this.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			findViewById(R.id.selectedMark).setVisibility(View.GONE);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressWarnings("deprecation")
	@Override
	public void toggle() {
		setChecked(!isChecked);
	}

}
