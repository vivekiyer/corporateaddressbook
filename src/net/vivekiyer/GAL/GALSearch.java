package net.vivekiyer.GAL;

import com.google.common.collect.HashMultimap;

import android.os.AsyncTask;

public class GALSearch extends AsyncTask<String, Void, Boolean>
{
	public interface OnSearchCompletedListener{
		void OnSearchCompleted(int result, HashMultimap<String, Contact> mContacts);
	}

	private ActiveSyncManager activeSyncManager;

	private String errorMesg;

	private int errorCode = 0;

	public OnSearchCompletedListener onSearchCompletedListener;
	
	HashMultimap<String,Contact> mContacts = null;

	public HashMultimap<String,Contact> getContacts() {
		return mContacts;
	}
	public GALSearch(ActiveSyncManager activeSyncManager) {
		this.activeSyncManager = activeSyncManager;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 * 
	 * The method that searches the GAL
	 */
	@Override
	protected Boolean doInBackground(String... params) {
		try {
			// Search the GAL
			mContacts = null;

			int statusCode = 0;

				do {
				statusCode = activeSyncManager.searchGAL(params[0]);
					switch (statusCode) {
						case 200:
							// All went ok, get the results
							mContacts = activeSyncManager.getResults();
							break;
						case 449: // RETRY AFTER PROVISIONING
						case 142: // RETRY AFTER PROVISIONING
							// Looks like we need to provision again
							activeSyncManager.provisionDevice();
							break;
						case 401: // UNAUTHORIZED
							// Looks like the password expired
							errorCode = 401;
							errorMesg = "Authentication failed. Please check your credentials";
							return false;
						default:
							errorCode = statusCode;
							errorMesg = "Exchange server rejected request with error:"
									+ errorCode;
							return false;
					}
				} while (statusCode != 200);

		} catch (final Exception e) {
			if (Debug.Enabled) {
				Debug.Log(e.toString());
			} else {
				errorMesg = "Activesync version= "
						+ activeSyncManager.getActiveSyncVersion() + "\n"
						+ e.toString();
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 * 
	 * This method displays the retrieved results in a list view
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if(onSearchCompletedListener != null)
			onSearchCompletedListener.OnSearchCompleted(result ? 0 : errorCode, mContacts);
	}

}
