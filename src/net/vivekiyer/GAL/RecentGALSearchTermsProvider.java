package net.vivekiyer.GAL;

import android.content.SearchRecentSuggestionsProvider;

/**
 * @author Vivek Iyer
 * 
 */
public class RecentGALSearchTermsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "net.vivekiyer.GAL.RecentGALSearchTermsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;
	
	public RecentGALSearchTermsProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}