package net.vivekiyer.GAL;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2013-01-09
 * Time: 00:50
 * To change this template use File | Settings | File Templates.
 */
public class ContactFragmentAdapter extends FragmentStatePagerAdapter {

	final private List<Contact> contacts;

	public ContactFragmentAdapter(FragmentManager fm, List<Contact> contacts) {
		super(fm);
		this.contacts = contacts;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return super.isViewFromObject(view, object);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public Fragment getItem(int i) {
		ContactFragment fragment = new ContactFragment(contacts.get(i));
		return fragment;
	}

	@Override
	public int getCount() {
		return contacts.size();  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return contacts.get(position).getDisplayName();
	}
}