package net.vivekiyer.GAL.view;

import android.content.Context;
import android.view.View;
import com.actionbarsherlock.view.ActionProvider;
import com.actionbarsherlock.view.SubMenu;
import net.vivekiyer.GAL.Contact;
import net.vivekiyer.GAL.ContactWriterSdk5;
import net.vivekiyer.GAL.account.AccountAdapter;
import net.vivekiyer.GAL.account.AccountData;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2013-02-03
 * Time: 15:07
 * To change this template use File | Settings | File Templates.
 */
public class SaveContactActionProvider extends ActionProvider {

	Context context;
	AccountAdapter adapter;
	ArrayList<AccountData> accountData;
	private Contact contact;

	public SaveContactActionProvider(Context context) {
		super(context);    //To change body of overridden methods use File | Settings | File Templates.
		this.context = context;
		adapter = new AccountAdapter(context, accountData = new ArrayList<AccountData>());
	}

	@Override
	public View onCreateActionView() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void onPrepareSubMenu(SubMenu subMenu) {
		super.onPrepareSubMenu(subMenu);    //To change body of overridden methods use File | Settings | File Templates.
		adapter.getMenuItems(subMenu, new ContactWriterSdk5(context, contact));
	}

	@Override
	public boolean hasSubMenu() {
		return true;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}
}
