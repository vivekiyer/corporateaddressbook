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
									errorMesg = App.getInstance().getString(R.string.too_many_device_partnerships_title);
									errorDetail = App.getInstance().getString(R.string.too_many_device_partnerships_detail);
									return false;
								case Parser.STATUS_OK:
									break;
								default:
									errorCode = activeSyncManager.getSearchStatus();
									errorMesg = App.getInstance().getString(R.string.unhandled_error, activeSyncManager.getSearchStatus());
									errorDetail = App.getInstance().getString(R.string.unhandled_error_occured);
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
							errorMesg = App.getInstance().getString(R.string.authentication_failed_title);
							errorDetail = App.getInstance().getString(R.string.authentication_failed_detail);
							return false;
						case 403: // FORBIDDEN
							// Looks like the password expired
							errorCode = 403;
							errorMesg = App.getInstance().getString(R.string.forbidden_by_server_title);
							errorDetail = App.getInstance().getString(R.string.forbidden_by_server_detail);
							return false;
						default:
							errorCode = statusCode;
							errorMesg = App.getInstance().getString(R.string.connection_failed_title);
							errorDetail = App.getInstance().getString(R.string.connection_failed_detail, statusCode);
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
