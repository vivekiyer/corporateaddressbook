package net.vivekiyer.GAL;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2013-01-09
 * Time: 00:50
 * To change this template use File | Settings | File Templates.
 */
public class ContactFragmentAdapter extends PagerAdapter {

	final private List<Contact> contacts;
	private final HashMap<Integer, ContactView> views = null;

	public ContactFragmentAdapter(Context context, List<Contact> contacts) {
		super(fm);
		this.contacts = contacts;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return (view instanceof ContactView && ((ContactView)view).getCon)
	}

	@Override
	public ContactView getItem(int i) {
		ContactView view = ContactView.newInstance(contacts.get(i));
		return view;
	}

	@Override
	public int getCount() {
		return contacts.size();  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		return super.instantiateItem(container, position);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return contacts.get(position).getDisplayName();
	}
}