package net.vivekiyer.GAL.preferences;

import android.content.Context;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import net.vivekiyer.GAL.R;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-11-29
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */

class HeaderAdapter extends ArrayAdapter<PreferenceActivity.Header> {
	static final int HEADER_TYPE_CATEGORY = 0;
	static final int HEADER_TYPE_NORMAL = 1;
	static final int HEADER_TYPE_SWITCH = 2;
	private static final int HEADER_TYPE_COUNT = HEADER_TYPE_SWITCH + 1;

//	private AuthenticatorHelper mAuthHelper;

	private static class HeaderViewHolder {
		ImageView icon;
		TextView title;
		TextView summary;
		Switch switch_;
	}

	private LayoutInflater mInflater;

	static int getHeaderType(PreferenceActivity.Header header) {
		if (header.fragment == null && header.intent == null) {
			return HEADER_TYPE_CATEGORY;
//		} else if (header.id == R.id.wifi_settings || header.id == R.id.bluetooth_settings) {
//			return HEADER_TYPE_SWITCH;
		} else {
			return HEADER_TYPE_NORMAL;
		}
	}

	@Override
	public int getItemViewType(int position) {
		PreferenceActivity.Header header = getItem(position);
		return getHeaderType(header);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false; // because of categories
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) != HEADER_TYPE_CATEGORY;
	}

	@Override
	public int getViewTypeCount() {
		return HEADER_TYPE_COUNT;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public HeaderAdapter(Context context, List<PreferenceActivity.Header> objects
	                     ) {
//			,
//	                     AuthenticatorHelper authenticatorHelper) {
		super(context, 0, objects);

//		mAuthHelper = authenticatorHelper;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Temp Switches provided as placeholder until the adapter replaces these with actual
		// Switches inflated from their layouts. Must be done before adapter is set in super
//		mWifiEnabler = new WifiEnabler(context, new Switch(context));
//		mBluetoothEnabler = new BluetoothEnabler(context, new Switch(context));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;
		PreferenceActivity.Header header = getItem(position);
		int headerType = getHeaderType(header);
		View view = null;

		if (convertView == null) {
			holder = new HeaderViewHolder();
			switch (headerType) {
				case HEADER_TYPE_CATEGORY:
					view = new TextView(getContext(), null,
							android.R.attr.listSeparatorTextViewStyle);
					holder.title = (TextView) view;
					break;

				case HEADER_TYPE_SWITCH:
					view = mInflater.inflate(R.layout.preference_header_switch_item, parent,
							false);
					holder.icon = (ImageView) view.findViewById(R.id.icon);
					holder.title = (TextView)
							view.findViewById(R.id.title);
					holder.summary = (TextView)
							view.findViewById(R.id.summary);
					holder.switch_ = (Switch) view.findViewById(R.id.switchWidget);
					break;

				case HEADER_TYPE_NORMAL:
					view = mInflater.inflate(
							R.layout.preference_header_item, parent,
							false);
					holder.icon = (ImageView) view.findViewById(R.id.icon);
					holder.title = (TextView)
							view.findViewById(R.id.title);
					holder.summary = (TextView)
							view.findViewById(R.id.summary);
					break;
			}
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (HeaderViewHolder) view.getTag();
		}

		// All view fields must be updated every time, because the view may be recycled
		switch (headerType) {
			case HEADER_TYPE_CATEGORY:
				holder.title.setText(header.getTitle(getContext().getResources()));
				break;

			case HEADER_TYPE_SWITCH:
				// Would need a different treatment if the main menu had more switches
//				if (header.id == R.id.wifi_settings) {
//					mWifiEnabler.setSwitch(holder.switch_);
//				} else {
//					mBluetoothEnabler.setSwitch(holder.switch_);
//				}
				// No break, fall through on purpose to update common fields

				//$FALL-THROUGH$
			case HEADER_TYPE_NORMAL:
//				if (header.extras != null
//						&& header.extras.containsKey(ManageAccountsSettings.KEY_ACCOUNT_TYPE)) {
//					String accType = header.extras.getString(
//							ManageAccountsSettings.KEY_ACCOUNT_TYPE);
//					ViewGroup.LayoutParams lp = holder.icon.getLayoutParams();
//					lp.width = getContext().getResources().getDimensionPixelSize(
//							R.dimen.header_icon_width);
//					lp.height = lp.width;
//					holder.icon.setLayoutParams(lp);
//					Drawable icon = mAuthHelper.getDrawableForType(getContext(), accType);
//					holder.icon.setImageDrawable(icon);
//				} else
				{
					holder.icon.setImageResource(header.iconRes);
				}
				holder.title.setText(header.getTitle(getContext().getResources()));
				CharSequence summary = header.getSummary(getContext().getResources());
				if (!TextUtils.isEmpty(summary)) {
					holder.summary.setVisibility(View.VISIBLE);
					holder.summary.setText(summary);
				} else {
					holder.summary.setVisibility(View.GONE);
				}
				break;
		}

		return view;
	}

	public void resume() {
//		mWifiEnabler.resume();
//		mBluetoothEnabler.resume();
	}

	public void pause() {
//		mWifiEnabler.pause();
//		mBluetoothEnabler.pause();
	}
}
