package net.vivekiyer.GAL;

import android.os.AsyncTask;
import com.google.common.collect.HashMultimap;

public class GALSearch extends AsyncTask<String, Void, Boolean>
{
	public interface OnSearchCompletedListener{
		void OnSearchCompleted(int result, GALSearch search);
	}

	private ActiveSyncManager activeSyncManager;

	private int errorCode = 0;
	private String errorMesg = "";
	private String errorDetail = "";

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
							switch(activeSyncManager.getSearchStatus()) {
								case Parser.STATUS_TOO_MANY_DEVICES:
									errorCode = Parser.STATUS_TOO_MANY_DEVICES;
									errorMesg = "Too many device partnerships";
									errorDetail = "Delete partnerships on the server before proceeding. " +
											"This is normally done via Outlook Web Access or by contacting your administrator.";
									return false;
								case Parser.STATUS_OK:
									break;
								default:
									errorCode = activeSyncManager.getSearchStatus();
									errorMesg = String.format("%d: Unhandled error", activeSyncManager.getSearchStatus());
									errorDetail = "An error occured that Corporate Addressbook currently cannot handle. " +
											"Please contact the authors to have this addressed.";
									return false;
							}
							mContacts = activeSyncManager.getResults();
							break;
						case 449: // RETRY AFTER PROVISIONING
							// Looks like we need to provision again
							activeSyncManager.provisionDevice();
							break;
						case 401: // UNAUTHORIZED
							// Looks like the password expired
							errorCode = 401;
							errorMesg = "Authentication failed";
							errorDetail = "Please check your credentials";
							return false;
						case 403: // FORBIDDEN
							// Looks like the password expired
							errorCode = 403;
							errorMesg = "Forbidden by server";
							errorDetail = "Either your Exchange server is not configured for connecting with ActiveSync or your user is not enabled for ActiveSync. Please contact your server administrator.";
							return false;
						default:
							errorCode = statusCode;
							errorMesg = String.format("%d: Connection error", statusCode);
							errorDetail = "Your server rejected the search request with the following error:"
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

	public int getErrorCode() {
		return errorCode;
	}
	public String getErrorMesg() {
		return errorMesg;
	}
	public String getErrorDetail() {
		return errorDetail;
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
			onSearchCompletedListener.OnSearchCompleted(result ? 0 : errorCode, this);
	}

}
