package net.vivekiyer.GAL;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2013-01-09
 * Time: 01:57
 * To change this template use File | Settings | File Templates.
 */
public class ContactPagerFragment extends SherlockFragment implements ViewPager.OnPageChangeListener {

	private ArrayList<Contact> contacts;
	private ContactFragmentAdapter fragmentAdapter = null;
	private ViewPager pager = null;
	private Bundle args = null;
	private PageIndicator indicator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null)
			args = savedInstanceState;

		View v = inflater.inflate(R.layout.contact_pager, container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
		if (args == null)
			args = getArguments();

		if (args == null) {
			getFragmentManager().beginTransaction().hide(this).commit();
		} else {
			initialize(args);
		}
		setHasOptionsMenu(true);
	}

	public void initialize(Bundle arguments) {
		args = arguments;
		int initialContactIndex = args.getInt(getString(R.string.KEY_CONTACT_INDEX), 0);
		ArrayList<Contact> contacts = args.getParcelableArrayList(getString(R.string.KEY_CONTACT_LIST));
		initialize(initialContactIndex, contacts);
	}

	private void initialize(int contactIndex, ArrayList<Contact> contacts) {
		indicator = (PageIndicator) getView().findViewById(R.id.indicator);
		if (contacts != null && !contacts.equals(this.contacts)) {
			this.contacts = contacts;
			fragmentAdapter = new ContactFragmentAdapter(getChildFragmentManager(), contacts);
			if (pager == null) {
				pager = (ViewPager) getView().findViewById(R.id.pager);
				indicator.setOnPageChangeListener(this);
			}
			pager.setAdapter(fragmentAdapter);
			indicator.setViewPager(pager, contactIndex);
		} else {
			indicator.setCurrentItem(contactIndex);
		}
	}

	public void update(int contactIndex, ArrayList<Contact> contacts) {
		initialize(pager == null || contactIndex != -1 ? contactIndex : pager.getCurrentItem(), contacts);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);    //To change body of overridden methods use File | Settings | File Templates.
		if (pager != null && fragmentAdapter != null) {
			ContactView f = fragmentAdapter.getItem(pager.getCurrentItem());
			if(f != null)
				f.onCreateOptionsMenu(menu, inflater);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.saveContact:
				ContactWriter writer = new ContactWriterSdk5(getActivity(), contacts.get(pager.getCurrentItem()));
				writer.saveContact();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (pager != null) {
			int contactIndex = pager.getCurrentItem();
			outState.putInt(getString(R.string.KEY_CONTACT_INDEX), contactIndex);
			outState.putParcelableArrayList(getString(R.string.KEY_CONTACT_LIST), contacts);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void onPageSelected(int position) {
		if (getActivity() instanceof ViewPager.OnPageChangeListener) {
			((ViewPager.OnPageChangeListener) getActivity()).onPageSelected(position);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
