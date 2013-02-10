/* Copyright 2010 Vivek Iyer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.vivekiyer.GAL;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewStub;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import net.vivekiyer.GAL.view.ExpandAnimation;
import net.vivekiyer.GAL.view.QuickActionView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author Vivek Iyer
 *         <p/>
 *         This class is responsible for displaying the contact data in a list
 *         It beautifies the list by displaying the attribute on the top left
 *         and the actual value below that. It also automatically finds phone numbers
 *         and email addresses, and provides action buttons if any of those are found
 */
public class ContactDetailsAdapter extends ArrayAdapter<KeyValuePair> {

	final private static int ANIM_DURATION = 300;
	final private static boolean ANIM_FROM_TOP = false;

	/**
	 * @param context
	 * @param textViewResourceId
	 * @param kvps               The contact details
	 *                           <p/>
	 *                           Adds the contact details to the array adapter
	 */
	public ContactDetailsAdapter(Context context, int textViewResourceId,
	                             ArrayList<KeyValuePair> kvps) {
		super(context, textViewResourceId);

		ArrayList<KeyValuePair> email = new ArrayList<KeyValuePair>(), others = new ArrayList<KeyValuePair>();
		for (KeyValuePair kvp : kvps) {
			// Not very elegant way to sort, 
			// will think of something better...
			switch (kvp.get_type()) {
				case EMAIL:
					email.add(kvp);
					break;
				case OTHER:
					others.add(kvp);
					break;
				default:
					this.add(kvp);
			}
		}
		for (KeyValuePair kvp : email)
			this.add(kvp);
		for (KeyValuePair kvp : others)
			this.add(kvp);
	}

	@Override
	public int getViewTypeCount() {
		return getCount();
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 * 
	 * Displays the contact details in the UI
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.detail_row, null);

			KeyValuePair kvp = this.getItem(position);
			if (kvp != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);

				// Set the top text
				if (tt != null) {
					tt.setText(kvp.getValue());
				}

				ImageButton im = (ImageButton) v.findViewById(R.id.detailsMoreActions);
				// Set the on click listeners
				im.setOnClickListener(mIconListener2);
				View v2 = v.findViewById(R.id.detailsMoreActions);
				View v3 = v.findViewById(R.id.detailsDivider);

				// Display the "More actions" button for phone and email
				switch (kvp.get_type()) {
					case MOBILE:
						v2.setVisibility(android.view.View.VISIBLE);
						v3.setVisibility(android.view.View.VISIBLE);
						bt.setText(Utility.getUCString(R.string.mobile));
						v.setOnClickListener(Listeners.getCallListener(kvp.getValue()));
						break;
					case PHONE:
						v2.setVisibility(android.view.View.VISIBLE);
						v3.setVisibility(android.view.View.VISIBLE);
						bt.setText(Utility.getUCString(R.string.office));
						v.setOnClickListener(Listeners.getCallListener(kvp.getValue()));
						break;
					case EMAIL:
						v2.setVisibility(android.view.View.VISIBLE);
						v3.setVisibility(android.view.View.VISIBLE);
						bt.setText(Utility.getUCString(R.string.email));
						v.setOnClickListener(Listeners.getMailListener(kvp.getValue()));
						break;
					default:
						// For others: hide "More actions"
						v2.setVisibility(android.view.View.GONE);
						v3.setVisibility(android.view.View.GONE);
						v.setOnClickListener(null);
						// Try to find a reasonable label
						final String key = kvp.getKey().toLowerCase(Locale.getDefault());
						if (key.contains("office")) { //$NON-NLS-1$
							bt.setText(Utility.getUCString(R.string.location));
						} else if (key.contains("alias")) { //$NON-NLS-1$
							bt.setText(Utility.getUCString(R.string.alias));
						} else if (key.contains("firstname")) { //$NON-NLS-1$
							bt.setText(Utility.getUCString(R.string.firstname));
						} else if (key.contains("lastname")) { //$NON-NLS-1$
							bt.setText(Utility.getUCString(R.string.lastname));
						} else if (key.contains("title")) { //$NON-NLS-1$
							bt.setText(Utility.getUCString(R.string.title));
						} else if (key.contains("company")) { //$NON-NLS-1$
							bt.setText(Utility.getUCString(R.string.company));
						} else {
							bt.setText(key.toUpperCase(Locale.getDefault()));
						}
				}
				final View qa = v.findViewById(R.id.details_actions);
				im.setTag(R.id.details_actions, qa);
//	            ((LinearLayout.LayoutParams) qa.getLayoutParams()).bottomMargin = -76;
//	            ((LinearLayout.LayoutParams) qa.getLayoutParams()).height = 76;

				im.setTag(R.id.detailsMoreActions, kvp);
				//im.setTag(R.id.contactsListView, parentViewGroup);
			}

		}
		return v;
	}


	// Create an anonymous implementation of OnItemClickListener
	// Called when the user clicks the sms or the email icon
	private OnClickListener mIconListener2 = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Get the tag, which will provide us the KVP and the QuickActionView
			final ImageButton ib = (ImageButton) v;
			final View vs = (View) v.getTag(R.id.details_actions);
			if (vs instanceof ViewStub) {
				v.setTag(R.id.details_actions, ((ViewStub) vs).inflate());
			}

			final QuickActionView qav = (QuickActionView) v.getTag(R.id.details_actions);
			final Boolean isCollapsing = ib.getTag() == null ? false : (Boolean) ib.getTag();
			if (isCollapsing) { //qav.getVisibility() != View.GONE) {

				// Creating the expand animation for the item
				Animation anim;
				anim = new ExpandAnimation(qav, ANIM_DURATION, ANIM_FROM_TOP);
//				anim = new ViewScaler(1.0f, 1.0f, 1.0f, 0.0f, ANIM_DURATION, qav, true);
				anim.setInterpolator(new AccelerateDecelerateInterpolator());

				// Start the animation on the toolbar
				qav.startAnimation(anim);
				ib.setTag(Boolean.FALSE);
			} else {
				final KeyValuePair kvp = (KeyValuePair) v.getTag(R.id.detailsMoreActions);
				final ViewGroup qa = ((ViewGroup) qav.findViewById(R.id.quickaction));

				ib.setTag(Boolean.TRUE);

				// Only add QuickActions if QuickActionView is not already populated
				if (qa.getChildCount() <= 2) {
					switch (kvp.get_type()) {
						case MOBILE:
							qav.addItem(R.drawable.ic_menu_start_conversation, R.string.sendMessage,
									Listeners.getSmsListener(kvp.getValue(), qav));

						case PHONE:
							qav.addItem(R.drawable.ic_menu_call, R.string.call,
									Listeners.getCallListener(kvp.getValue(), null));
							break;

						case EMAIL:
							qav.addItem(R.drawable.ic_menu_compose, R.string.sendEmail, Listeners.getMailListener(kvp.getValue(), qav));
						default:
							break;
					}

					qav.addItem(R.drawable.ic_menu_copy, R.string.copyToClipboard, Listeners.getCopyListener(kvp.getValue(), null));
				}

				// Creating the expand animation for the item
				MarginLayoutParams params = (MarginLayoutParams) qav.getLayoutParams();
				params.bottomMargin = -(params.height);

				Animation anim;
				anim = new ExpandAnimation(qav, ANIM_DURATION, ANIM_FROM_TOP);
//				anim = new ViewScaler(1.0f, 1.0f, 0.0f, 1.0f, ANIM_DURATION, qav, false);
				anim.setInterpolator(new AccelerateDecelerateInterpolator());

				// Start the animation on the toolbar
				qav.startAnimation(anim);
			}
			RotateAnimation buttonAnim = new RotateAnimation(
					isCollapsing ? -180.0f : 0.0f,
					isCollapsing ? 0.0f : -180.0f,
					Animation.RELATIVE_TO_SELF,
					0.5f,
					Animation.RELATIVE_TO_SELF,
					0.5f);
			buttonAnim.setDuration(ANIM_DURATION);
			buttonAnim.setFillAfter(true);
			buttonAnim.setInterpolator(new AccelerateDecelerateInterpolator());
//			buttonAnim.setAnimationListener(new Animation.AnimationListener() {
//				@Override
//				public void onAnimationStart(Animation animation) { }
//
//				@Override
//				public void onAnimationRepeat(Animation animation) { }
//
//				@Override
//				public void onAnimationEnd(Animation animation) {
//					ib.setImageResource(!isCollapsing ? R.drawable.ic_action_expand : R.drawable.ic_action_collapse);
//				}
//			});

			ib.startAnimation(buttonAnim);
		}

	};
}