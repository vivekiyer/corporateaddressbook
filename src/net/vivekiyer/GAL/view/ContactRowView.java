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
		if(isChecked != checked)
			toggle();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressWarnings("deprecation")
	@Override
	public void toggle() {
		isChecked = !isChecked;
		if(isChecked){
			if(Utility.isPreJellyBean())
				this.setBackgroundDrawable(getResources().getDrawable(R.drawable.selected_border_shading));
			else
				this.setBackground(getResources().getDrawable(R.drawable.selected_border_shading));
			findViewById(R.id.selectedMark).setVisibility(View.VISIBLE);
		}
		else {
			this.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			findViewById(R.id.selectedMark).setVisibility(View.GONE);
		}
	}

}
