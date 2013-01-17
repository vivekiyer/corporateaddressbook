package net.vivekiyer.GAL;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2013-01-09
 * Time: 01:57
 * To change this template use File | Settings | File Templates.
 */
public class ContactPagerFragment extends SherlockFragment {

	private ArrayList<Contact> contacts;
	private ContactFragmentAdapter fragmentAdapter = null;
	private ViewPager pager = null;
	private Bundle args = null;
	private PageIndicator indicator;

	// TODO: Extract stuff from ContactActivity so that same swipe can be used in single and dual pane.
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
				if (getActivity() instanceof ViewPager.OnPageChangeListener) {
					indicator.setOnPageChangeListener((ViewPager.OnPageChangeListener) getActivity());
				}
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
	public void onSaveInstanceState(Bundle outState) {
		if (pager != null) {
			int contactIndex = pager.getCurrentItem();
			outState.putInt(getString(R.string.KEY_CONTACT_INDEX), contactIndex);
			outState.putParcelableArrayList(getString(R.string.KEY_CONTACT_LIST), contacts);
		}
		super.onSaveInstanceState(outState);
	}
}
