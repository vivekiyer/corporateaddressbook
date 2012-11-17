package net.vivekiyer.GAL;

import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;

/**
 * @author Vivek Iyer
 * 
 */
public class RecentGALSearchTermsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "net.vivekiyer.GAL.RecentGALSearchTermsProvider"; //$NON-NLS-1$
    public final static int MODE = DATABASE_MODE_QUERIES;
	
	public RecentGALSearchTermsProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
	
	/**
	 * @return
	 */
	protected boolean clearSearchHistory() {
		final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
				getContext(), RecentGALSearchTermsProvider.AUTHORITY,
				RecentGALSearchTermsProvider.MODE);
		suggestions.clearHistory();
		return true;
	}
}