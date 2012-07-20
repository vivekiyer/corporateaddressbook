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

import com.devoteam.quickaction.QuickActionWindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Vivek Iyer 
 * This class is used to display a Contact object in a list.
 * The class takes a parceled Contact object and displays the
 * DisplayName. It also allows the user to save the contact
 */
/**
 * @author vivek
 * 
 */
@SuppressWarnings("deprecation")
public class CorporateContactRecordFragment extends android.app.ListFragment
	implements OnClickListener, PopupMenu.OnMenuItemClickListener {

	private Contact mContact;
	private ContactDetailsAdapter m_adapter;
	private ContactWriter contactWriter;

	// Menu ids
	private static final int MENU_ID_COPY_TO_CLIPBOARD = 0;
	private static final int MENU_ID_EMAIL = 1;
	private static final int MENU_ID_CALL = 2;
	private static final int MENU_ID_EDIT_BEFORE_CALL = 3;
	private static final int MENU_ID_SMS = 4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.contact, container, false);

		ImageButton contactActions = (ImageButton) view.findViewById(R.id.contact_actions);
		assert(contactActions != null);
		contactActions.setOnClickListener(this);

		return view;		
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		registerForContextMenu(getListView());
	    getView().findViewById(R.id.contactHeader).setVisibility(View.GONE);
	};
	
	public void setContact(Contact contact) {
		mContact = contact;
		setContact();
	}
	private void setContact()
	{
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
			if((s = mContact.getTitle()).length() != 0)
				s = s + ", ";
			{
				tv2.setText(s + mContact.getCompany());
			}
		}

		// getListView().setOnItemLongClickListener(mListViewLongClickListener);
		contactWriter = ContactWriter.getInstance();
		contactWriter.Initialize(this.getActivity(), this.getActivity().getLayoutInflater(), mContact);

	    view.findViewById(R.id.contactHeader).setVisibility(View.VISIBLE);
	}
	
	public void clear() {
		mContact = null;
		m_adapter = null;
		setListAdapter(m_adapter);
	    getView().findViewById(R.id.contactHeader).setVisibility(View.GONE);
	}

	/**
	 * @param menu
	 * @param v
	 * @param menuInfo
	 * 
	 *            Create a context menu for the list view Depending upon the
	 *            item selected, shows the user different options
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
		switch(v.getId()) {
		case R.id.contact_actions:
			if(Utility.isPreHoneycomb()) {
				showContactQuickActions(v);
				return;
			}
			else {
				PopupMenu popup = new PopupMenu(getActivity(), v);
				popup.setOnMenuItemClickListener(this);
				MenuInflater inflater = popup.getMenuInflater();
			
				inflater.inflate(R.menu.contact_actions_menu, popup.getMenu());			
				popup.show();
			}
			break;
		default:
			break;	
		}
	}

	private void showContactQuickActions(View v) {
		// Get the tag, which will provide us the KVP
		
		int[] xy = new int[2];
		v.getLocationInWindow(xy);
		Rect rect = new Rect(xy[0], xy[1], xy[0]+v.getWidth(), xy[1]+v.getHeight());
		final QuickActionWindow qa = new QuickActionWindow(v.getContext(), v, rect);

		qa.addItem(R.drawable.social_add_person, R.id.saveContact, this);
		v.getLocationOnScreen(xy);
		qa.show(xy[0] + v.getWidth()/2);
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch(item.getItemId()){
		case R.id.saveContact:
			this.contactWriter.saveContact();
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		// Get the selected item from the listview adapter
		final KeyValuePair kvp = m_adapter.getItem(info.position);

		switch (item.getItemId()) {
		case MENU_ID_CALL:
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ kvp.getValue()));
			startActivity(intent);
			break;
		case MENU_ID_SMS:
			intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"
					+ kvp.getValue()));
			startActivity(intent);
			break;
		case MENU_ID_COPY_TO_CLIPBOARD:
			final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(kvp.getValue());
			Toast.makeText(this.getActivity(), "Text copied to clipboard", Toast.LENGTH_SHORT)
					.show();
			break;
		case MENU_ID_EDIT_BEFORE_CALL:
			intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ kvp.getValue()));
			startActivity(intent);
			break;
		case MENU_ID_EMAIL:
			intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { kvp.getValue() });
			startActivity(Intent.createChooser(intent, "Send mail..."));
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
		//inflater.inflate(R.menu.contacts_menu, menu);
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
			contactWriter.saveContact();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Called when this activity is about to be destroyed by the system.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(contactWriter != null)
			contactWriter.cleanUp();
	}
};
