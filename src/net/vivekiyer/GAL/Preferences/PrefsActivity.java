package net.vivekiyer.GAL.Preferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.preference.*;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageButton;
import net.vivekiyer.GAL.Debug;
import net.vivekiyer.GAL.R;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.widget.Toast;
 

/*******************************
 * 
 * @author ryanf@blackmoonit.com
 *
 * Borrowed from http://www.blackmoonit.com/2012/07/all_api_prefsactivity/
 * 
 */
public class PrefsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {
    protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;

	// extras that allow any preference activity to be launched as part of a wizard

	// show Back and Next buttons? takes boolean parameter
	// Back will then return RESULT_CANCELED and Next RESULT_OK
	private static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";

	// add a Skip button?
	private static final String EXTRA_PREFS_SHOW_SKIP = "extra_prefs_show_skip";

	// specify custom text for the Back or Next buttons, or cause a button to not appear
	// at all by setting it to null
	private static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text";
	private static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";

	/**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
        if (mHasHeaders!=null && mLoadHeaders!=null) {
            try {
                return (Boolean)mHasHeaders.invoke(this);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return false;
    }

	@Override
	public void finish() {
		super.finish();    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle aSavedState) {
        //onBuildHeaders() will be called during super.onCreate()
        try {
            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class );
            mHasHeaders = getClass().getMethod("hasHeaders");
        } catch (NoSuchMethodException e) {
        }

	    if(getIntent().hasExtra(EXTRA_SHOW_FRAGMENT)) {
		    String fragment = getIntent().getExtras().getString(EXTRA_SHOW_FRAGMENT);
		    if(fragment.equals(ServerPrefsFragment.class.getName())) {
			    Bundle extras = getIntent().getExtras();
			    getIntent().putExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, true);
			    getIntent().putExtra(EXTRA_PREFS_SET_NEXT_TEXT, "Validate");
			    getIntent().putExtra(EXTRA_PREFS_SET_BACK_TEXT, getString(android.R.string.cancel));
		    }
	    }

        super.onCreate(aSavedState);

//	    if(getIntent().hasExtra(EXTRA_SHOW_FRAGMENT)) {
//		    String fragment = getIntent().getExtras().getString(EXTRA_SHOW_FRAGMENT);
//		    if(fragment.equals(ServerPrefsFragment.class.getName())) {
//			    android.app.FragmentManager fmgr = getFragmentManager();
//			    Debug.Log(fmgr.toString());
//		    }
//	    }

	    String action = getIntent().getAction();
	    if (action != null && action.equals(getString(R.string.ACTION_PREFS_SERVER))) {
		    addPreferencesFromResource(R.xml.pref_server);
		    addPreferencesFromResource(R.xml.pref_server_footer);
		    PreferenceManager prefs = getPreferenceManager();
		    Preference validator = prefs.findPreference("PREFS_VALIDATE");
		    validator.setOnPreferenceClickListener(this);
	    }
	    else if (action != null && action.equals(getString(R.string.ACTION_PREFS_ABOUT))) {
		    addPreferencesFromResource(R.xml.pref_about);
	    }
        else if (!isNewV11Prefs()) {
            addPreferencesFromResource(R.xml.pref_headers_legacy);
        }
    }

	private void setListener(PreferenceFragment fragment) {
		PreferenceFragment servfrag = ((PreferenceFragment)fragment);
		Preference validator = servfrag.findPreference("PREFS_VALIDATE");
		if(validator != null) {
			validator.setOnPreferenceClickListener(this);
		}
	}

	@Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this,new Object[]{R.xml.pref_headers, aTarget});
//	        View panel = getLayoutInflater().inflate(R.layout.server_footer, null);
//	        setListFooter(panel);
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }   
    }
    
	@Override
    public void startActivity(android.content.Intent intents) {
    	try {
			super.startActivity(intents);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "Can't find an app that can send email", Toast.LENGTH_SHORT).show();
		}
    }

	@Override
	public boolean onPreferenceClick(Preference preference) {

		if(preference.getKey().equals("PREFS_VALIDATE"))
		{
			String prefkey = getString(R.string.PREFS_KEY_SERVER_PREFERENCE);
			PreferenceManager mgr = preference.getPreferenceManager();
			Preference servpref = mgr.findPreference(prefkey);
			EditTextPreference servtextpref = (EditTextPreference) servpref;
			String server = servtextpref.getText();


			Toast.makeText(this, "Yes...\n"+server, Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}
}