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

import java.util.ArrayList;
import java.util.Locale;

import net.vivekiyer.GAL.view.ExpandAnimation;
import net.vivekiyer.GAL.view.QuickActionView;
import net.vivekiyer.GAL.view.ViewScaler;

import com.devoteam.quickaction.QuickActionWindow;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.webkit.WebView.FindListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Vivek Iyer
 *
 * This class is responsible for displaying the contact data in a list
 * It beautifies the list by displaying the attribute on the top left 
 * and the actual value below that. It also automatically finds phone numbers
 * and email addresses, and provides action buttons if any of those are found
 */
public class ContactDetailsAdapter extends ArrayAdapter<KeyValuePair> {	
	
	/**
	 * @param context 
	 * @param textViewResourceId 
	 * @param kvps The contact details
	 * 
	 * Adds the contact details to the array adapter
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
		for(KeyValuePair kvp : email)
			this.add(kvp);
		for(KeyValuePair kvp : others)
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
		final ViewGroup parentViewGroup = parent;
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
					if(key.contains("office")){ //$NON-NLS-1$
						bt.setText(Utility.getUCString(R.string.location));
					}
					else if(key.contains("alias")){ //$NON-NLS-1$
						bt.setText(Utility.getUCString(R.string.alias));
					}
					else if(key.contains("firstname")){ //$NON-NLS-1$
						bt.setText(Utility.getUCString(R.string.firstname));
					}
					else if(key.contains("lastname")){ //$NON-NLS-1$
						bt.setText(Utility.getUCString(R.string.lastname));
					}
					else if(key.contains("title")){ //$NON-NLS-1$
						bt.setText(Utility.getUCString(R.string.title));
					}
					else if(key.contains("company")){ //$NON-NLS-1$
						bt.setText(Utility.getUCString(R.string.company));
					}
					else{
						bt.setText(key.toUpperCase(Locale.getDefault()));
					}
				}
				final View qa = v.findViewById(R.id.details_actions);
				im.setTag(R.id.details_actions, qa);		
	            ((RelativeLayout.LayoutParams) qa.getLayoutParams()).bottomMargin = -76;
	            ((RelativeLayout.LayoutParams) qa.getLayoutParams()).height = 76;

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
			final QuickActionView qav = (QuickActionView) v.getTag(R.id.details_actions);
			if(ib.getTag() != null && ((Boolean)ib.getTag()).equals(Boolean.TRUE)) { //qav.getVisibility() != View.GONE) {
				ib.setImageResource(R.drawable.ic_action_expand);
				//qav.setVisibility(View.GONE);
                // Creating the expand animation for the item
				Animation anim;
				anim = new ExpandAnimation(qav, 1500);
//				ScaleAnimation anim = new ViewScaler(1.0f, 1.0f, 1.0f, 0.0f, 1500, qav, true);

				// Start the animation on the toolbar
				qav.startAnimation(anim);
				ib.setTag(Boolean.FALSE);
				return;
			}
			else {
				final KeyValuePair kvp = (KeyValuePair) v.getTag(R.id.detailsMoreActions);
				final ViewGroup qa = ((ViewGroup)qav.findViewById(R.id.quickaction));
				//final ViewGroup parent = ((ViewGroup)v.getTag(R.id.contactsListView));

				ib.setTag(Boolean.TRUE);
				// Remove possible previous QuickAction views (views might be reused)
				// UPDATE: No reuse since the logic has been changed (see getItemViewType())
				//qa.removeViews(1, qa.getChildCount() - 2);
				
				// Only add QuickActions if QuickActionView is not already populated
				if(qa.getChildCount() <= 2) {
					switch (kvp.get_type()){
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
				
				ib.setImageResource(R.drawable.ic_action_collapse);
				//qav.setVisibility(View.VISIBLE);
				//parent.invalidate();
				
                // Creating the expand animation for the item
				Animation anim;
                anim = new ExpandAnimation(qav, 1500);
//				anim = new ViewScaler(1.0f, 1.0f, 0.0f, 1.0f, 1500, qav, false);
				anim.setAnimationListener(new Animation.AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						Toast.makeText(getContext(), "Start", Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
						Toast.makeText(getContext(), "Repeat", Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						Toast.makeText(getContext(), "End", Toast.LENGTH_SHORT).show();
					}
				});

                // Start the animation on the toolbar
                qav.startAnimation(anim);
				
			}
		}

	};
}