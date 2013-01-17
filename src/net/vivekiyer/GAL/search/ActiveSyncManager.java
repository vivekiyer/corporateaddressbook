/* Copyright 2010 Vivek Iyer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.vivekiyer.GAL.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import com.android.exchange.adapter.GalParser;
import com.android.exchange.adapter.ProvisionParser;
import net.vivekiyer.GAL.*;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

/**
 * @author Vivek Iyer
 *
 * This class is responsible for implementing the ActiveSync commands that
 * are used to connect to the Exchange server and  query the GAL
 */

/**
 * @author vivek
 */
public class ActiveSyncManager implements SharedPreferences.OnSharedPreferenceChangeListener {
	private String mPolicyKey = "0"; //$NON-NLS-1$
	private String mAuthString;
	private String mUri;
	private String mServerName;
	private String mDomain;
	private String mUsername;
	private String mPassword;
	private String mQuery = ""; //$NON-NLS-1$
	private boolean mUseSSL;
	private int mMaxResults;
	private boolean mShowPictures;
	private boolean mAcceptAllCerts;
	private String mActiveSyncVersion = ""; //$NON-NLS-1$
	private String mDeviceId;
	private String accountKey = null;
	private ArrayList<Contact> mResults;
	private int requestStatus = Parser.STATUS_NOT_SET;

	public static final int ERROR_UNABLE_TO_REPROVISION = 449;

	public String getSearchTerm() {
		return mQuery;
	}

	public boolean isUseSSLSet() {
		return mUseSSL;
	}

	public void setUseSSL(boolean mUseSSL) {
		this.mUseSSL = mUseSSL;
	}

	public boolean isAcceptAllCertsSet() {
		return mAcceptAllCerts;
	}

	public void setAcceptAllCerts(boolean mAcceptAllCerts) {
		this.mAcceptAllCerts = mAcceptAllCerts;
	}

	public String getActiveSyncVersion() {
		return mActiveSyncVersion;
	}

	public void setActiveSyncVersion(String version) {
		mActiveSyncVersion = version;
	}

	public String getDomain() {
		return mDomain;
	}

	public void setDomain(String domain) {
		mDomain = domain;
	}

	@Override
	protected void finalize() throws Throwable {
		Context ctx = App.getInstance();
		// Make sure that the activesync policy key get written
		// to the preferences
		final SharedPreferences.Editor editor = App.getInstance().getSharedPreferences(getAccountKey(), Context.MODE_PRIVATE).edit();
		editor.putString(ctx.getString(R.string.PREFS_KEY_ACTIVESYNCVERSION_PREFERENCE),
				getActiveSyncVersion());
		editor.putString(ctx.getString(R.string.PREFS_KEY_DEVICE_ID_STRING), getDeviceId());
		editor.putString(ctx.getString(R.string.PREFS_KEY_POLICY_KEY_PREFERENCE),
				getPolicyKey());

		// Commit the edits!
		editor.commit();
		super.finalize();    //To change body of overridden methods use File | Settings | File Templates.
	}

	public String getPolicyKey() {
		return mPolicyKey;
	}

	public void setPolicyKey(String policyKey) {
		this.mPolicyKey = policyKey;
	}

	public String getServerName() {
		return mServerName;
	}

	public void setServerName(String serverName) {
		this.mServerName = serverName;
	}

	public void setUsername(String username) {
		this.mUsername = username;
	}

	public void setPassword(String password) {
		this.mPassword = password;
	}

	public String getDeviceId() {
		return mDeviceId;
	}

	public void setDeviceId(String deviceId) {
		mDeviceId = deviceId;
	}

	public int getMaxResults() {
		return mMaxResults;
	}

	public void setMaxResults(int mMaxResults) {
		this.mMaxResults = mMaxResults;
	}

	public boolean isShowPictures() {
		return mShowPictures;
	}

	public void setShowPictures(boolean mShowPictures) {
		this.mShowPictures = mShowPictures;
	}

	public ArrayList<Contact> getResults() {
		return mResults;
	}

	/**
	 * Generates the auth string from the username, password and domain
	 */
	private void generateAuthString() {
		// For BPOS the DOMAIN is not required, so remove the backslash
		if (mDomain.equalsIgnoreCase("")) //$NON-NLS-1$
			mAuthString = "Basic " //$NON-NLS-1$
					+ Utility.base64Encode(mUsername + ":" + mPassword); //$NON-NLS-1$
		else
			mAuthString = "Basic " //$NON-NLS-1$
					+ Utility.base64Encode(mDomain + "\\" + mUsername + ":" //$NON-NLS-1$ //$NON-NLS-2$
					+ mPassword);
	}

	/**
	 * Initializes the class by assigning the Exchange URL and the AuthString
	 *
	 * @throws URISyntaxException
	 */
	public boolean Initialize() {

		generateAuthString();

		if (mDeviceId == null)
			mDeviceId = getUniqueId();

		// If we don't have a server name, 
		// there is no way we can proceed
		if (mServerName.compareToIgnoreCase("") == 0) //$NON-NLS-1$
			return false;

		// this is where we will send it
		try {
			URI uri = new URI((mUseSSL) ? "https" : "http", // Scheme					 //$NON-NLS-1$ //$NON-NLS-2$
					mServerName,                    // Authority
					"/Microsoft-Server-ActiveSync", // path //$NON-NLS-1$
					"User=" // query //$NON-NLS-1$
							+ mUsername + "&DeviceId=" //$NON-NLS-1$
							+ mDeviceId + "&DeviceType=Android" //$NON-NLS-1$
							+ "&Cmd=", //$NON-NLS-1$
					null                            // fragment
			);

			mUri = uri.toString();
		} catch (URISyntaxException e) {
			return false;
		}

		return true;
	}

	public ActiveSyncManager() {
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ActiveSyncManager) {
			return getAccountKey() == null ? false : getAccountKey().equals(((ActiveSyncManager) o).getAccountKey());
		}
		return false;
	}

	@Override
	public String toString() {
		return getAccountKey();
	}

	public ActiveSyncManager(String accountKey) {
		loadPreferences(accountKey);
	}

	public ActiveSyncManager(String serverName, String domain, String username,
	                         String password, boolean useSSL, boolean acceptAllCerts,
	                         String policyKey, String activeSyncVersion, String deviceId) {

		mServerName = serverName;
		mDomain = domain;
		mUsername = username;
		mPassword = password;
		mPolicyKey = policyKey;
		mActiveSyncVersion = activeSyncVersion;
		mUseSSL = useSSL;
		mAcceptAllCerts = acceptAllCerts;
		mDeviceId = deviceId;
	}

	/**
	 * @return Status code returned from the Exchange server
	 *         <p/>
	 *         Connects to the Exchange server and obtains the version of
	 *         ActiveSync supported by the server
	 * @throws Exception
	 */
	public int getExchangeServerVersion() throws Exception {

		// First get the options from the server
		CommandRequest request = new CommandRequest(mUri, mAuthString, mUseSSL,
				mActiveSyncVersion, mAcceptAllCerts, mPolicyKey);

		HttpResponse response = request.getOptions();

		// 200 indicates a success
		int statusCode = response.getStatusLine().getStatusCode();

		Header[] headers;
		switch (statusCode) {
			case 200:
				headers = response.getHeaders("MS-ASProtocolVersions"); //$NON-NLS-1$

				if (headers.length != 0) {

					Header header = headers[0];

					// Parse out the ActiveSync Protocol version
					String versions = header.getValue();

					// Look for the last comma, and parse out the highest
					// displayText
					mActiveSyncVersion = versions.substring(versions
							.lastIndexOf(",") + 1); //$NON-NLS-1$

					// Provision the device if necessary
					statusCode = provisionDevice();
				}
				break;
			// Deal with redirect messages stemming from Exchange CAS mismatch
			// (see
			// http://technet.microsoft.com/en-us/library/dd439372(v=exchg.80).aspx)
			// Not tested due to lack of access to CAS enabled servers
			case 451:
				headers = response.getHeaders("X-MS-Location"); //$NON-NLS-1$
				if (headers.length != 0) {
					String url = headers[0].getValue();
					if (!mUri.equals(url)) {
						mUri = url;
						return getExchangeServerVersion();
					}
				}
		}
		return statusCode;
	}

	/**
	 * @param query     The name to search the GAL for
	 * @param startWith
	 * @return The status code returned from the Exchange server
	 * @throws Exception This method searches the GAL on the Exchange server
	 */
	public int searchGAL(String query, int startWith) throws Exception {
		return searchGAL(query, startWith, true);
	}

	/**
	 * @param query       The name to search the GAL for
	 * @param reprovision True if search should be retried after device provisioning is
	 *                    needed, false if function should exit when needing
	 *                    reprovision. This is to prevent an endless loop of
	 *                    reprovisioning fails.
	 * @return The status code returned from the Exchange server
	 * @throws Exception This method searches the GAL on the Exchange server
	 */
	private int searchGAL(String query, int startWith, boolean reprovision) throws Exception {
		mQuery = query;

		int ret;
		GalRequest request = new GalRequest(mUri, mAuthString, mUseSSL,
				mActiveSyncVersion, mAcceptAllCerts, mPolicyKey, query, mMaxResults, mShowPictures);
		request.setStartWith(startWith);

		// Get the WBXML input stream from the response
		CommandResponse resp = new CommandResponse(request.getResponse(true));

		// Make sure there were no errors
		ret = resp.getStatusCode();

		if (ret == 200) {
			GalParser gp = new GalParser(resp.getWBXMLInputStream());
			gp.parse();
			requestStatus = gp.getStatus();
			if (requestStatus != Parser.STATUS_OK) {
				switch (requestStatus) {
					case Parser.STATUS_DEVICE_NOT_PROVISIONED:
					case Parser.STATUS_POLICY_REFRESH:
					case Parser.STATUS_INVALID_POLICY_KEY:
						if (reprovision) {
							provisionDevice();
							return searchGAL(query, startWith, false);
						} else {
							Debug.Log("Unable to reprovision device while searching: request status=" + requestStatus); //$NON-NLS-1$
							return ERROR_UNABLE_TO_REPROVISION;
						}
					default:
						Debug.Log(String.format(
								Locale.getDefault(),
								"Unknown request status returned: %d", gp.getStatus())); //$NON-NLS-1$
				}
			}
			mResults = gp.getResults();
		}
		return ret;
	}

	public int getRequestStatus() {
		return requestStatus;
	}

	/**
	 * Sends a Provision command to the
	 * Exchange server. Only needed for Exchange 2007 and above
	 *
	 * @throws SocketTimeoutException
	 * @returns Server HTTP response code
	 */
	public int provisionDevice() throws SocketTimeoutException {

		try {
			ProvisionRequest provRequest = new ProvisionRequest(mUri,
					mAuthString, mUseSSL, mActiveSyncVersion, mAcceptAllCerts,
					mPolicyKey);

			Debug.Log("Sending provision request"); //$NON-NLS-1$
			// Get the WBXML input stream from the response
			CommandResponse resp = new CommandResponse(
					provRequest.getResponse(true));

			Debug.Log("Received provision response"); //$NON-NLS-1$

			// Make sure there were no errors
			if (resp.getStatusCode() != 200) {
				Debug.Log(resp.getErrorString());
				return resp.getStatusCode();
			}

			ProvisionParser pp = new ProvisionParser(resp.getWBXMLInputStream());
			if (!pp.parse()) {
				requestStatus = pp.getStatus();
				Debug.Log("Failed to parse policy key, status " + requestStatus); //$NON-NLS-1$
				return resp.getStatusCode();
			}

			Debug.Log("Key = " + pp.getSecuritySyncKey()); //$NON-NLS-1$
			Debug.Log("Policy status = " + pp.getPolicyStatus()); //$NON-NLS-1$

			if (pp.getPolicyStatus() == Parser.STATUS_NO_POLICY_NEEDED) {
				this.requestStatus = Parser.STATUS_OK;
				return resp.getStatusCode();
			}

			// Now that we have the temp policy key
			// Tell the server we accept everything it says
			AckProvisionRequest ackProvRequest = new AckProvisionRequest(mUri,
					mAuthString, mUseSSL, mActiveSyncVersion, mAcceptAllCerts,
					mPolicyKey, pp.getSecuritySyncKey(),
					pp.isRemoteWipeRequested());

			Debug.Log("Sending provision ack"); //$NON-NLS-1$

			// Get the WBXML input stream from the response
			resp = new CommandResponse(ackProvRequest.getResponse(false));
			Debug.Log("Received ack response"); //$NON-NLS-1$

			// Make sure there were no errors
			if (resp.getStatusCode() != 200) {
				requestStatus = pp.getStatus();
				Debug.Log(resp.getErrorString() + ", status " + requestStatus); //$NON-NLS-1$
				return resp.getStatusCode();
			}

			pp = new ProvisionParser(resp.getWBXMLInputStream());
			if (!pp.parse()) {
				requestStatus = pp.getStatus();
				Debug.Log("Error in acknowledging Provision request, status " + requestStatus); //$NON-NLS-1$
				return resp.getStatusCode();
			}

			mPolicyKey = pp.getSecuritySyncKey();
			Debug.Log("Final policy Key = " + mPolicyKey); //$NON-NLS-1$
			requestStatus = pp.getStatus();
			Debug.Log("Final request status = " + requestStatus); //$NON-NLS-1$
			return resp.getStatusCode();
		} catch (SocketTimeoutException ex) {
			Debug.Log("Connection timed out during provisioning. Error string:\n" + ex.toString()); //$NON-NLS-1$
			throw ex;
		} catch (Exception ex) {
			Debug.Log("Provisioning failed. Error string:\n" + ex.toString()); //$NON-NLS-1$
		}
		return Parser.STATUS_NOT_SET;
	}

	/**
	 * Provides a unique ID for this instance of the app
	 *
	 * @return A UUID-like string based on either the device ID or, if that is unavailable, a random UUID
	 */
	public static String getUniqueId() {

		final String androidId = Secure.getString(App.getInstance()
				.getContentResolver(), Secure.ANDROID_ID);

		// Use the Android ID unless it's broken, in which case fallback on a
		// random number
		try {
			UUID uuid;
			if ((androidId == null) || "9774d56d682e549c".equals(androidId)) { //$NON-NLS-1$
				uuid = UUID.randomUUID();
			} else {
				uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8")); //$NON-NLS-1$
			}
			return uuid.toString().replace("-", ""); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean loadPreferences(String accountKey) {
		this.accountKey = accountKey;
		return reloadPreferences();
	}

	public boolean reloadPreferences() {
		if (getAccountKey() != null) {
			SharedPreferences prefs = App.getInstance().getSharedPreferences(getAccountKey(), Context.MODE_PRIVATE);
			return loadPreferences(prefs);
		}
		return false;
	}

	public boolean loadPreferences(SharedPreferences thesePrefs) {
		Context context = App.getInstance();

		thesePrefs.registerOnSharedPreferenceChangeListener(this);

		setUsername(thesePrefs.getString(
				context.getString(R.string.PREFS_KEY_USERNAME_PREFERENCE), "")); //$NON-NLS-1$
		setPassword(thesePrefs.getString(
				context.getString(R.string.PREFS_KEY_PASSWORD_PREFERENCE), "")); //$NON-NLS-1$
		setDomain(thesePrefs.getString(
				context.getString(R.string.PREFS_KEY_DOMAIN_PREFERENCE), "")); //$NON-NLS-1$

		// Clean up server name from previous displayText of the app
		setServerName(cleanUpServerName(thesePrefs));

		setUseSSL(thesePrefs.getBoolean(
				context.getString(R.string.PREFS_KEY_USE_SSL), true)); //$NON-NLS-1$
		setAcceptAllCerts(thesePrefs.getBoolean(
				context.getString(R.string.PREFS_KEY_ACCEPT_ALL_CERTS), true));
		int maxResults = Integer.parseInt(thesePrefs.getString(
				context.getString(R.string.PREFS_KEY_MAX_HITS),
				App.getInstance().getString(R.integer.PREFS_KEY_MAX_RESULT)));
		setMaxResults(maxResults);
		setShowPictures(thesePrefs.getBoolean(
				context.getString(R.string.PREFS_KEY_CONTACT_PICS), true));
		setActiveSyncVersion(thesePrefs.getString(
				context.getString(R.string.PREFS_KEY_ACTIVESYNCVERSION_PREFERENCE), "")); //$NON-NLS-1$
		setPolicyKey(thesePrefs.getString(
				context.getString(R.string.PREFS_KEY_POLICY_KEY_PREFERENCE), "")); //$NON-NLS-1$

		// Fix for null device_id
		String device_id_string = thesePrefs.getString(context.getString(R.string.PREFS_KEY_DEVICE_ID_STRING), null);
		if (device_id_string == null) {
			int device_id = thesePrefs.getInt(
					context.getString(R.string.PREFS_KEY_DEVICE_ID), 0);
			if (device_id > 0)
				device_id_string = String.valueOf(device_id);
			else
				device_id_string = getUniqueId();
		}

		setDeviceId(device_id_string);

		if (!Initialize())
			return false;

		// Check to see if we have successfully connected to an Exchange server
		// Do we have a previous successful connect with these settings?
		if (!thesePrefs.getBoolean(context.getString(R.string.PREFS_KEY_SUCCESSFULLY_CONNECTED), false)) {
			// If not, let's try
			if (getActiveSyncVersion().equalsIgnoreCase("")) { //$NON-NLS-1$
				// If we fail, let's return
				return false;
			} else {
				// In case of success, let's make a record of this so that
				// we don't have to check the settings every time we launch.
				// This record will be reset when any change is made to the
				// settings
				SharedPreferences.Editor editor = thesePrefs.edit();
				editor.putBoolean(context.getString(R.string.PREFS_KEY_SUCCESSFULLY_CONNECTED), true);
				editor.commit();
			}
		}

		return true;
	}

	/**
	 * The older displayText of the application included http and https in the
	 * server name. The newer displayText no longer has this. Hence clean up is
	 * required
	 *
	 * @param prefs SharedPreferences object to be used to read server name
	 */
	private String cleanUpServerName(SharedPreferences prefs) {
		String serverName = prefs.getString(
				net.vivekiyer.GAL.App.getInstance().getString(R.string.PREFS_KEY_SERVER_PREFERENCE), ""); //$NON-NLS-1$
		serverName = serverName.toLowerCase(Locale.getDefault());

		if (serverName.startsWith("https://")) { //$NON-NLS-1$
			final SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(net.vivekiyer.GAL.App.getInstance().getString(R.string.PREFS_KEY_USE_SSL), true);
			serverName = serverName.substring(8);
			editor.putString(net.vivekiyer.GAL.App.getInstance().getString(R.string.PREFS_KEY_SERVER_PREFERENCE), serverName);
			editor.commit();
		} else if (serverName.startsWith("http://")) { //$NON-NLS-1$
			final SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(net.vivekiyer.GAL.App.getInstance().getString(R.string.PREFS_KEY_USE_SSL), false);
			serverName = serverName.substring(7);
			editor.putString(net.vivekiyer.GAL.App.getInstance().getString(R.string.PREFS_KEY_SERVER_PREFERENCE), serverName);
			editor.commit();
		}

		return serverName;
	}

	public String getAccountKey() {
		if (accountKey == null) {
			accountKey = mUsername.contains("@") ?
					mUsername :
					String.format("%1$s@%2$s", mUsername, mServerName);
		} //NON-NLS
		return accountKey;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		reloadPreferences();
	}

}
