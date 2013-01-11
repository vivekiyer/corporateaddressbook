package net.vivekiyer.GAL;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2013-01-09
 * Time: 00:50
 * To change this template use File | Settings | File Templates.
 */
public class ContactFragmentAdapter extends FragmentStatePagerAdapter {

	private final List<Contact> contacts;

	public ContactFragmentAdapter(FragmentManager fm, List<Contact> contacts) {
		super(fm);
		this.contacts = contacts;
	}

	@Override
	public Fragment getItem(int i) {
		return new ContactFragment(contacts.get(i));	}

	@Override
	public int getCount() {
		return contacts.size();  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return contacts.get(position).getDisplayName();
	}
}
