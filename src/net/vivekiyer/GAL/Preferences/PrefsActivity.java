package net.vivekiyer.GAL.Preferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.vivekiyer.GAL.R;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;
 

/*******************************
 * 
 * @author ryanf@blackmoonit.com
 *
 * Borrowed from http://www.blackmoonit.com/2012/07/all_api_prefsactivity/
 * 
 */
public class PrefsActivity extends PreferenceActivity {
    protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;
 
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
 
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle aSavedState) {
        //onBuildHeaders() will be called during super.onCreate()
        try {
            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class );
            mHasHeaders = getClass().getMethod("hasHeaders");
        } catch (NoSuchMethodException e) {
        }
        super.onCreate(aSavedState);
        if (!isNewV11Prefs()) {
            addPreferencesFromResource(R.xml.pref_server);
        }
    }
 
    @Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this,new Object[]{R.xml.pref_headers, aTarget});
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
}