package net.vivekiyer.GAL.account;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import net.vivekiyer.GAL.Contact;
import net.vivekiyer.GAL.ContactWriterSdk5;
import net.vivekiyer.GAL.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2013-02-09
 * Time: 10:52
 * To change this template use File | Settings | File Templates.
 */
public class SpinnerAccountAdapter extends AccountAdapter implements View.OnClickListener {

	protected View spinnerView;
	protected Class parentClass = null;
	private ContactDataProvider contactDataProvider;

	public void setContactDataProvider(ContactDataProvider contactDataProvider) {
		this.contactDataProvider = contactDataProvider;
	}

	@Override
	public void onClick(View v) {
		if (contactDataProvider != null) {
			ContactWriterSdk5 writer = new ContactWriterSdk5(getContext(), contactDataProvider.getContact());
			writer.createContactEntry((AccountData) v.getTag());
		}

	}

	public interface ContactDataProvider {
		public Contact getContact();
	}

	public SpinnerAccountAdapter(Context context, ArrayList<AccountData> accountData) {
		super(context, accountData);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView != null)
			return convertView;
		if (parentClass == null || !parentClass.equals(parent.getClass())) {

			spinnerView = layoutInflater.inflate(R.layout.spinner_account_icon, parent, false);
			parentClass = parent.getClass();
		}
		return spinnerView;
	}

	public View getEmptyView(ViewGroup parent) {
		return layoutInflater.inflate(R.layout.spinner_account_icon, parent, true);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// Inflate a view template
//		if (super.getCount() < 1)
//			return new View(getContext());
		/*else*/
//		if (convertView == null) {
//			convertView = super.getView(position, convertView, parent);
//		}
//		TextView firstAccountLine = (TextView) convertView
//				.findViewById(R.id.firstAccountLine);
//		TextView secondAccountLine = (TextView) convertView
//				.findViewById(R.id.secondAccountLine);
//		ImageView accountIcon = (ImageView) convertView
//				.findViewById(R.id.accountIcon);
//
//		// Populate template
//		final AccountData account = getItem(position);
//		firstAccountLine.setText(account.getName());
//		secondAccountLine.setText(account.getTypeLabel());
//		Drawable icon = account.getIcon();
//		if (icon == null) {
//			icon = getContext().getResources().getDrawable(
//					android.R.drawable.ic_menu_search);
//		}
//		accountIcon.setImageDrawable(icon);
		View v = super.getView(position, convertView, parent);
		v.setTag(getItem(position));

//		v.setOnClickListener(this);
		return v;
	}

	//	@Override
//	public int getCount() {
//		int size = super.getCount();    //To change body of overridden methods use File | Settings | File Templates.
//		if (size < 1)
//			return 1;
//		else
//			return size;
//	}
//
	public boolean hasAccounts() {
		return super.getCount() > 0;
	}

	public View getDropDownView() {
		LinearLayout container = new LinearLayout(getContext());
		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		container.setOrientation(LinearLayout.VERTICAL);
		for (int i = 0; i < mAccounts.size(); i++) {
			AccountData ad = mAccounts.get(i);
			if (!ad.getType().equals(getContext().getString(R.string.ACCOUNT_TYPE))) {
				View v = getView(i, null, container);
				container.addView(v, layout);
			}
		}
		return container;
	}
}
