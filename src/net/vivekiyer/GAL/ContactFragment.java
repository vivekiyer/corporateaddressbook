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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.internal.widget.IcsSpinner;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author Vivek Iyer 
 * This class is used to display a Contact object in a list.
 * The class takes a parceled Contact object and displays the
 * DisplayName. It also allows the user to save the contact
 */

/**
 * @author vivek
 */
@SuppressWarnings("deprecation")
public class ContactFragment extends SherlockListFragment implements View.OnClickListener {

	private Menu fragmentMenu = null;
	private Contact mContact = null;
	private ContactDetailsAdapter m_adapter = null;
	private ContactWriter contactWriter = null;
	private Boolean isDualFrame = true;

	// Menu ids
	private static final int MENU_ID_COPY_TO_CLIPBOARD = 0;
	private static final int MENU_ID_EMAIL = 1;
	private static final int MENU_ID_CALL = 2;
	private static final int MENU_ID_EDIT_BEFORE_CALL = 3;
	private static final int MENU_ID_SMS = 4;
	private static final BitmapFactory.Options options = new BitmapFactory.Options();

	public ContactFragment(Contact contact) {
		this();
		mContact = contact;
	}

	public ContactFragment() {
		options.inScaled = false;
		options.inDensity = 0;
		options.inTargetDensity = 0;
	}

	public Boolean getIsDualFrame() {
		return isDualFrame;
	}

	public void setIsDualFrame(Boolean isDualFrame) {
		this.isDualFrame = isDualFrame;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(android.view.LayoutInflater inflater,
	                         android.view.ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.contact, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		registerForContextMenu(getListView());

		if (savedInstanceState != null) {
			mContact = savedInstanceState.getParcelable(getString(R.string.KEY_CONTACT));
		}

		if (this.mContact == null)
			getView().findViewById(R.id.contactHeader).setVisibility(View.GONE);
		else
			setContact();

		// Disabling; handled via fragment menu
//		ImageButton contactActions = (ImageButton) getView().findViewById(R.id.contact_actions);
//		assert(contactActions != null);
//		contactActions.setOnClickListener(this);
//		contactActions.setVisibility(View.GONE);
//
		ImageButton saveContacts = (ImageButton) getView().findViewById(R.id.saveContact);
		saveContacts.setOnClickListener(this);
		// Seems to be some logic gone wrong, no way to 
		// save contacts exists in phone layout. Commenting
		// this out for now.
		//saveContacts.setVisibility(View.GONE);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(getString(R.string.KEY_CONTACT), mContact);
		super.onSaveInstanceState(outState);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public void setContact(Contact contact) {
		mContact = contact;
		setContact();
	}

	private void setContact() {
		final View view = getView();

		m_adapter = new ContactDetailsAdapter(this.getActivity(), R.layout.detail_row,
				mContact.getDetails());
		setListAdapter(m_adapter);

		final TextView tv1 = (TextView) view.findViewById(R.id.toptext);
		tv1.setText(mContact.getDisplayName());

		final TextView tv2 = (TextView) view.findViewById(R.id.bottomtext);
		// Set the bottom text
		if (tv2 != null) {
			String s;
			if ((s = mContact.getTitle()).length() != 0)
				s = s + ", "; //$NON-NLS-1$
			{
				tv2.setText(s + mContact.getCompany());
			}
		}

		ImageView imageView = (ImageView) view.findViewById(R.id.contactPicture);
		byte[] pic;
		if ((pic = mContact.getPicture()) != null) {
			Bitmap bm = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
			imageView.setImageBitmap(bm);
		} else {
			imageView.setImageResource(R.drawable.ic_contact_picture);
		}

		// getListView().setOnItemLongClickListener(mListViewLongClickListener);
		contactWriter = new ContactWriterSdk5(getActivity(), mContact);

		view.findViewById(R.id.contactHeader).setVisibility(View.VISIBLE);
		setSaveMenuEnabled(true);
	}

	public void clear() {
		mContact = null;
		m_adapter = null;
		setListAdapter(m_adapter);
		getView().findViewById(R.id.contactHeader).setVisibility(View.GONE);
		setSaveMenuEnabled(false);
	}

	private void setSaveMenuEnabled(boolean enabled) {
		if (fragmentMenu != null) {
			MenuItem item = fragmentMenu.findItem(R.id.saveContact);
			if (item != null)
				item.setEnabled(enabled);
		}
//		else if(getView() != null){
//			View saveContact = getView().findViewById(R.id.save_contact);
//			if(saveContact != null)
//				saveContact.setVisibility(enabled ? View.VISIBLE : View.GONE);
//		}
	}

	/**
	 * @param menu
	 * @param v
	 * @param menuInfo Create a context menu for the list view Depending upon the
	 *                 item selected, shows the user different options
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		// Get the selected item from the listview adapter
		final KeyValuePair kvp = m_adapter.getItem(info.position);

		// Set the header to the selected text
		menu.setHeaderTitle(kvp.getValue());

		// Add the default options (copy to clipboard)
		menu.add(Menu.NONE, MENU_ID_COPY_TO_CLIPBOARD, Menu.NONE,
				R.string.copyToClipboard).setIcon(android.R.drawable.ic_menu_view);

		// Handle the special cases
		switch (kvp.get_type()) {
			case EMAIL:
				menu.add(Menu.NONE, MENU_ID_EMAIL, Menu.NONE, R.string.send_email)
						.setIcon(android.R.drawable.sym_action_email);
				break;
			case MOBILE:
				menu.add(Menu.NONE, MENU_ID_SMS, Menu.NONE,
						getString(R.string.send_sms_to) + kvp.getValue()).setIcon(
						android.R.drawable.ic_menu_send);
			case PHONE:
				menu.add(Menu.NONE, MENU_ID_CALL, Menu.NONE,
						getString(R.string.call_) + kvp.getValue()).setIcon(
						android.R.drawable.ic_menu_call);
				menu.add(Menu.NONE, MENU_ID_EDIT_BEFORE_CALL, Menu.NONE,
						R.string.edit_number_before_call).setIcon(
						android.R.drawable.ic_menu_edit);
			case OTHER:
			case UNDEFINED:
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.saveContact:
				saveContact(v);
			default:
				break;
		}
	}

//	@Override
//	public boolean (MenuItem item) {
//		switch(item.getItemId()){
//		case R.id.saveContact:
//			this.contactWriter.saveContact(getView().getContext());
//			return true;
//		default:
//			break;
//		}
//		return false;
//	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		// Get the selected item from the listview adapter
		final KeyValuePair kvp = m_adapter.getItem(info.position);

		switch (item.getItemId()) {
			case MENU_ID_CALL:
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" //$NON-NLS-1$
						+ kvp.getValue()));
				startActivity(intent);
				break;
			case MENU_ID_SMS:
				intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" //$NON-NLS-1$
						+ kvp.getValue()));
				startActivity(intent);
				break;
			case MENU_ID_COPY_TO_CLIPBOARD:
				final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(kvp.getValue());
				Toast.makeText(this.getActivity(), getString(R.string.text_copied_to_clipboard), Toast.LENGTH_SHORT)
						.show();
				break;
			case MENU_ID_EDIT_BEFORE_CALL:
				intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" //$NON-NLS-1$
						+ kvp.getValue()));
				startActivity(intent);
				break;
			case MENU_ID_EMAIL:
				intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("text/plain"); //$NON-NLS-1$
				intent.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[]{kvp.getValue()});
				startActivity(Intent.createChooser(intent, getString(R.string.send_mail)));
				break;
			default:
				return super.onContextItemSelected(item);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Displays the menu when the user clicks the Options button In our case the
	 * menu contains only one button - save
	 */
//	@TargetApi(11)
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		if(!isDualFrame) {
		inflater.inflate(R.menu.contact_actions_menu, menu);
		this.fragmentMenu = menu;
//		}
		setSaveMenuEnabled(this.mContact != null);

		MenuItem search = fragmentMenu.findItem(R.id.saveContact);

		IcsSpinner spinner = new IcsSpinner(getActivity(), null,
				com.actionbarsherlock.R.attr.actionDropDownStyle);
		spinner.setAdapter(App.getSystemAccounts());

		search.setActionView(spinner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * 
	 * Shows the save contact option when the user clicks the save option
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go home
				final Intent intent = new Intent(this.getActivity(),
						net.vivekiyer.GAL.CorporateAddressBook.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			case R.id.saveContact:
				saveContact(null);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void saveContact(View v) {
		contactWriter.saveContact();
	}

	/**
	 * Called when this activity is about to be destroyed by the system.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (contactWriter != null)
			contactWriter.cleanUp();
	}
}
