package net.vivekiyer.GAL;

import android.app.Activity;
import android.os.AsyncTask;
import net.vivekiyer.GAL.Preferences.ConnectionChecker;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.http.conn.ConnectTimeoutException;

import static net.vivekiyer.GAL.Preferences.ConnectionChecker.*;

public class GALSearch extends AsyncTask<String, Void, Boolean>
{
	public interface OnSearchCompletedListener{
		void onSearchCompleted(int result, GALSearch search);
	}

	private int startWith = 0;
	private boolean clearResults = true;
	private ActiveSyncManager activeSyncManager;
	private int errorCode = 0;

	private String errorMesg = ""; //$NON-NLS-1$
	private String errorDetail = ""; //$NON-NLS-1$
	protected volatile OnSearchCompletedListener onSearchCompletedListener;

	HashMap<String,Contact> mContacts = null;

	public void setStartWith(int startWith) {
		this.startWith = startWith;
	}

	public boolean getClearResults() {
		return clearResults;
	}

	public void setClearResults(boolean clearResults) {
		this.clearResults = clearResults;
	}

	public HashMap<String,Contact> getContacts() {
		return mContacts;
	}
	public String getSearchTerm() {
		return activeSyncManager.getSearchTerm();
	}
	public OnSearchCompletedListener getOnSearchCompletedListener() {
		synchronized (this) {
			return onSearchCompletedListener;
		}
	}
	public void setOnSearchCompletedListener(
			OnSearchCompletedListener onSearchCompletedListener) {
		synchronized (this) {
			this.onSearchCompletedListener = onSearchCompletedListener;
		}
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
			if(params.length > 1)
				startWith = Integer.parseInt(params[1]);

			int statusCode = 0;

				do {
				statusCode = activeSyncManager.searchGAL(params[0], startWith);
					switch (statusCode) {
						case 200:
							// All went ok, get the results
							switch(activeSyncManager.getRequestStatus()) {
								case Parser.STATUS_TOO_MANY_DEVICES:
									errorCode = Parser.STATUS_TOO_MANY_DEVICES;
									errorMesg = App.getInstance().getString(R.string.too_many_device_partnerships_title);
									errorDetail = App.getInstance().getString(R.string.too_many_device_partnerships_detail);
									return false;
								case ActiveSyncManager.ERROR_UNABLE_TO_REPROVISION:
									errorCode = ActiveSyncManager.ERROR_UNABLE_TO_REPROVISION;
									errorMesg = App.getInstance().getString(R.string.authentication_failed_title);
									errorDetail = App.getInstance().getString(R.string.please_check_settings);
								case Parser.STATUS_OK:
									break;
								default:
									errorCode = activeSyncManager.getRequestStatus();
									errorMesg = App.getInstance().getString(R.string.unhandled_error, activeSyncManager.getRequestStatus());
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
							// Device ID not accepted by server
							errorCode = 403;
							errorMesg = App.getInstance().getString(R.string.forbidden_by_server_title);
							errorDetail = App.getInstance().getString(R.string.forbidden_by_server_detail, activeSyncManager.getDeviceId());
							return false;
						default:
							errorCode = statusCode;
							errorMesg = App.getInstance().getString(R.string.connection_failed_title);
							errorDetail = App.getInstance().getString(R.string.connection_failed_detail, statusCode);
							return false;
					}
				} while (statusCode != 200);
		} catch (final SocketTimeoutException e) {
			errorCode = TIMEOUT;
			errorMesg = App.getInstance().getString(R.string.timeout_title);
			errorDetail = App.getInstance().getString(R.string.timeout_detail, App.getInstance().getString(R.string.useSecureSslText));
			return false;
		} catch (final ConnectTimeoutException e) {
			errorCode = TIMEOUT;
			errorMesg = App.getInstance().getString(R.string.timeout_title);
			errorDetail = App.getInstance().getString(R.string.timeout_detail, App.getInstance().getString(R.string.useSecureSslText));
			return false;
		} catch (final UnknownHostException e) {
			errorCode = UNKNOWN_HOST;
			errorMesg = App.getInstance().getString(R.string.invalid_server_title);
			errorDetail = App.getInstance().getString(R.string.invalid_server_detail, App.getInstance().getString(R.string.useSecureSslText));
			return false;
		} catch (javax.net.ssl.SSLPeerUnverifiedException spue) {
			errorCode = SSL_PEER_UNVERIFIED;
			errorMesg = App.getInstance().getString(R.string.authentication_failed_title);
			errorDetail = App.getInstance().getString(R.string.unable_to_find_matching_certificate);
			return false;
		} catch (final Exception e) {
			Debug.Log("Exception in GALSearch:\n" + e.toString());
			e.printStackTrace();
			errorCode = ConnectionChecker.UNDEFINED;
			errorDetail = App.getInstance().getString(R.string.unhandled_error_occured) +
					"\n" + e.toString();
			errorMesg = App.getInstance().getString(R.string.unhandled_error, errorCode);
			return false;
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
		if(getOnSearchCompletedListener() != null)
			getOnSearchCompletedListener().onSearchCompleted(result ? Activity.RESULT_OK : errorCode, this);
	}

}
