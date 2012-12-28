
package net.vivekiyer.GAL.view;

import java.util.zip.Inflater;

import com.devoteam.quickaction.QuickActionItem;

import net.vivekiyer.GAL.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

/**
 * A class that can display, as a popup badge, a collection 
 * of QuickActionItems
 * 
 * Based on the great work done by Mohd Faruq
 *
 */
public class QuickActionView extends FrameLayout implements KeyEvent.Callback {
	
	private final Context mContext;
	private final LayoutInflater mInflater;
	
	View contentView;
	
	private ViewGroup mTrack;
	private Animation mTrackAnim;

	public QuickActionView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	
	public QuickActionView(Context context, AttributeSet attribs) {
		this(context, attribs, 0);
	}
	
	public QuickActionView(Context context, AttributeSet attribs, int defStyle) {
		
		super(context, attribs, defStyle);
		
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		
		setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.quickaction_slider_background));

		contentView = mInflater.inflate(R.layout.quickactionview, null);
		addView(contentView);
		
		mTrack = (ViewGroup) contentView.findViewById(R.id.quickaction);

		setFocusable(true);

		// Prepare track entrance animation
		mTrackAnim = AnimationUtils.loadAnimation(mContext, R.anim.quickaction);
		mTrackAnim.setInterpolator(new Interpolator() {
			public float getInterpolation(float t) {
				// Pushes past the target area, then snaps back into place.
				// Equation for graphing: 1.2-((x*1.6)-1.1)^2
				final float inner = (t * 1.55f) - 1.1f;
				return 1.2f - inner * inner;
			}
		});	
		
//		if(isInEditMode()) {
//			addItem(R.drawable.ic_menu_call, R.string.call, null);
//			addItem(R.drawable.ic_menu_start_conversation, R.string.send_email, null);
//		}
	}
	
	/**
	 * Adds an item to the QuickActionWindow
	 * 
	 * @param drawable Icon to be shown
	 * @param text Label to be shown below the drawable
	 * @param l Definition for the callback to be invoked when the view is cliked
	 */
	public void addItem(Drawable drawable, String text, OnClickListener l) {
		QuickActionItem view = (QuickActionItem) mInflater.inflate(R.layout.quickaction_item, mTrack, false);
		view.setChecked(false);
		view.setImageDrawable(drawable);
		view.setText(text);
		view.setOnClickListener(l);
		
		final int index = mTrack.getChildCount() - 1;
		mTrack.addView(view, index);
	}
	
	/**
	 * Adds an item to the QuickActionWindow
	 * 
	 * @param drawable Icon resource id to be shown
	 * @param text Label to be shown below the drawable
	 * @param l Definition for the callback to be invoked when the view is cliked
	 */
	public void addItem(int drawable, String text, OnClickListener l) {
		addItem(mContext.getResources().getDrawable(drawable), text, l);
	}
	
	/**
	 * Adds an item to the QuickActionWindow
	 * 
	 * @param drawable Icon to be shown
	 * @param text Label resource id to be shown below the drawable
	 * @param l Definition for the callback to be invoked when the view is cliked
	 */
	public void addItem(Drawable drawable, int resid, OnClickListener l) {
		addItem(drawable, mContext.getResources().getString(resid), l);
	}
	
	/**
	 * Adds an item to the QuickActionWindow
	 * 
	 * @param drawable Icon resource id to be shown
	 * @param text Label resource id to be shown below the drawable
	 * @param l Definition for the callback to be invoked when the view is cliked
	 */
	public void addItem(int drawable, int resid, OnClickListener l) {
		addItem(mContext.getResources().getDrawable(drawable), mContext.getResources().getText(resid).toString(), l);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
