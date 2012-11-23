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

package net.vivekiyer.GAL;

import android.provider.Settings.Secure;

import com.android.exchange.adapter.GalParser;
import com.android.exchange.adapter.ProvisionParser;
import com.google.common.collect.HashMultimap;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
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
 * 
 */
public class ActiveSyncManager {
	private String mPolicyKey = "0"; //$NON-NLS-1$
	private String mAuthString;
	private String mUri;
	private String mServerName;
	private String mDomain;
	private String mUsername;
	private String mPassword;
	private String mQuery = ""; //$NON-NLS-1$
	private boolean mUseSSL;
	private boolean mAcceptAllCerts;
	private String mActiveSyncVersion = ""; //$NON-NLS-1$
	private String mDeviceId;
	private HashMultimap<String, Contact> mResults;
	private int requestStatus = Parser.STATUS_NOT_SET;

	public static final int ERROR_UNABLE_TO_REPROVISION		= 449;

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

	public HashMultimap<String, Contact> getResults() {
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
					mServerName, // Authority
					"/Microsoft-Server-ActiveSync", // path //$NON-NLS-1$
					"User=" // query //$NON-NLS-1$
							+ mUsername + "&DeviceId=" //$NON-NLS-1$
							+ mDeviceId + "&DeviceType=Android" //$NON-NLS-1$
							+ "&Cmd=", //$NON-NLS-1$
					null // fragment
			);

			mUri = uri.toString();
		} catch (URISyntaxException e) {
			return false;
		}

		return true;
	}

	public ActiveSyncManager() {
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
	 * @throws Exception
	 * @return Status code returned from the Exchange server
	 * 
	 *         Connects to the Exchange server and obtains the version of
	 *         ActiveSync supported by the server
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
				// version
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
	 * @param query
	 *            The name to search the GAL for
	 * 
	 * @return The status code returned from the Exchange server
	 * @throws Exception
	 * 
	 *             This method searches the GAL on the Exchange server
	 */
	public int searchGAL(String query) throws Exception {
		return searchGAL(query, true);
	}

	/**
	 * @param query
	 *            The name to search the GAL for
	 * @param reprovision
	 *            True if search should be retried after device provisioning is
	 *            needed, false if function should exit when needing
	 *            reprovision. This is to prevent an endless loop of
	 *            reprovisioning fails.
	 * 
	 * @return The status code returned from the Exchange server
	 * @throws Exception
	 * 
	 *             This method searches the GAL on the Exchange server
	 */
	private int searchGAL(String query, boolean reprovision) throws Exception {
		mQuery = query;

		int ret = 0;
		GalRequest request = new GalRequest(mUri, mAuthString, mUseSSL,
				mActiveSyncVersion, mAcceptAllCerts, mPolicyKey, query, 100);

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
						return searchGAL(query, false);
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
	 * @throws SocketTimeoutException 
	 * @returns Server HTTP response code Sends a Provision command to the
	 *          Exchange server. Only needed for Exchange 2007 and above
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
		}
		catch (SocketTimeoutException ex) {
			Debug.Log("Connection timed out during provisioning. Error string:\n" + ex.toString()); //$NON-NLS-1$
			throw ex;
		}
		catch (Exception ex) {
			Debug.Log("Provisioning failed. Error string:\n" + ex.toString()); //$NON-NLS-1$
		}
		return Parser.STATUS_NOT_SET;
	}

	static String getUniqueId() {

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
}
