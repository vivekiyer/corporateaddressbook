package net.vivekiyer.GAL;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.viewpagerindicator.TitlePageIndicator;

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

	// TODO: Extract stuff from ContactActivity so that same swipe can be used in single and dual pane.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.

//		if(savedInstanceState != null) {
//			setArguments(savedInstanceState);
//		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(savedInstanceState != null)
			args = savedInstanceState;

		View v = inflater.inflate(R.layout.contact_pager, container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
		if(args == null)
			args = getArguments();

		if(args == null) {
			getFragmentManager().beginTransaction().hide(this).commit();
		}
		else
			initialize(getView());
	}

	private void initialize(View fragmentView) {
		int initialContactIndex = args.getInt(getString(R.string.KEY_CONTACT_INDEX), 0);
		contacts = args.getParcelableArrayList(getString(R.string.KEY_CONTACT_LIST));

		fragmentAdapter = new ContactFragmentAdapter(getChildFragmentManager(), contacts);
		pager = (ViewPager) fragmentView.findViewById(R.id.pager);
		pager.setAdapter(fragmentAdapter);
		TitlePageIndicator titleIndicator = (TitlePageIndicator) fragmentView.findViewById(R.id.indicator);
		titleIndicator.setViewPager(pager, initialContactIndex);
		titleIndicator.setTextColor(getResources().getColor(R.color.abs__primary_text_disable_only_holo_light));
		titleIndicator.setSelectedColor(getResources().getColor(R.color.abs__primary_text_holo_light));
		return;
	}

	public void update(Bundle arguments) {
		args = arguments;
		initialize(getView());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(pager != null) {
			int contactIndex = pager.getCurrentItem();
			outState.putInt(getString(R.string.KEY_CONTACT_INDEX), contactIndex);
			outState.putParcelableArrayList(getString(R.string.KEY_CONTACT_LIST), contacts);
		}
		super.onSaveInstanceState(outState);    //To change body of overridden methods use File | Settings | File Templates.
	}


}
