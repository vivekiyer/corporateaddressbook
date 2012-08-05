package net.vivekiyer.GAL;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;

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
	};
	
	@Override
	public boolean isChecked() {
		// TODO Auto-generated method stub
		return isChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		if(isChecked != checked)
			toggle();
	}

	@Override
	public void toggle() {
		isChecked = !isChecked;
		if(isChecked){
			this.setBackgroundDrawable(getResources().getDrawable(R.drawable.selected_border_shading));
			findViewById(R.id.selectedMark).setVisibility(View.VISIBLE);
		}
		else {
			this.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			findViewById(R.id.selectedMark).setVisibility(View.GONE);
		}
	}

}
