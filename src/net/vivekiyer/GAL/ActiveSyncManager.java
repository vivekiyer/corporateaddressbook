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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Hashtable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import net.vivekiyer.GAL.wbxml.WBXML;

import android.util.Log;


/**
 * @author Vivek Iyer
 *
 * This class is responsible for implementing the ActiveSync commands that
 * are used to connect to the Exchange server and  query the GAL
 */
public class ActiveSyncManager {
	private String mPolicyKey = "0";
	private String mAuthString;
	private String mUri;
	private String mServerName;
	private String mDomain;
	private WBXML wbxml;
	private String mUsername;
	private String mPassword;
	private String mActiveSyncVersion = "";
	private static final String TAG = "ActiveSyncManager";

	
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

	public void setmUsername(String username) {
		this.mUsername = username;
	}

	public void setPassword(String password) {
		this.mPassword = password;
	}

	public WBXML getWbxml() {
		return wbxml;
	}
	
	/**
	 * Initializes the class by assigning the Exchange URL and the AuthString  
	 */
	public void Initialize() {
		wbxml = new WBXML();

		mAuthString = "Basic "
				+ Utility.base64Encode(mDomain + "\\" + mUsername + ":"
						+ mPassword);

		// this is where we will send it
		mUri = mServerName + "/Microsoft-Server-Activesync?" + "User="
				+ mUsername
				+ "&DeviceId=490154203237518&DeviceType=PocketPC&Cmd=";

	}

	public ActiveSyncManager() {		
	}

	public ActiveSyncManager(String serverName, String domain, String username,
			String password, String policyKey, String activeSyncVersion) {

		mServerName = serverName;
		mDomain = domain;
		mUsername = username;
		mPassword = password;
		mPolicyKey = policyKey;
		mActiveSyncVersion = activeSyncVersion;
	}

	/**
	 * @throws Exception
	 * 
	 * Connects to the Exchange server and obtains the version of ActiveSync supported 
	 * by the server 
	 */
	public void getExchangeServerVersion() throws Exception {
		// First get the options from the server
		Header[] headers = getOptions();

		if (headers != null) {
			for (Header header : headers) {
				Log.v(TAG, (header.toString()));

				// Parse out the ActiveSync Protocol version
				if (header.getName().equalsIgnoreCase("MS-ASProtocolVersions")) {
					String versions = header.getValue();

					// Look for the last comma, and parse out the highest
					// version
					mActiveSyncVersion = versions.substring(versions
							.lastIndexOf(",") + 1);
					Log.v(TAG, "ActiveSync version = " + mActiveSyncVersion);
					break;
				}
			}

			// If the exchange version is 12.0 or above (Exchange 2007 and
			// above), lets try
			if (Float.parseFloat(mActiveSyncVersion) >= 12.0) {
				// Get the policy key
				provisionDevice();
			}
		}
	}

	/**
	 * @param httpPost The request to POST to the Exchange sever
	 * @return The response to the POST message
	 * @throws Exception
	 * 
	 * POSTs a message to the Exchange server. Any WBXML or String entities that are 
	 * returned by the server are parsed and returned to the callee
	 */
	private String sendPostRequest(HttpPost httpPost) throws Exception {
		
		// POST the request to the server
		String result = "";
		HttpClient client = createHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpResponse response = client.execute(httpPost, localContext);

		Log.v(TAG, (response.getStatusLine().toString()));
		
		// Log all the headers
		Header[] headers = response.getAllHeaders();

		for (Header header : headers) {
			Log.v(TAG, (header.toString()));
		}

		// Get the content
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
			
			// Parse all the entities
			String contentType = entity.getContentType().getValue();
			
			// WBXML entities
			if (contentType
					.compareToIgnoreCase("application/vnd.ms-sync.wbxml") == 0) {
				InputStream is = entity.getContent();
				wbxml.convertWbxmlToXml(is, output);
				result = output.toString();

			}
			// Text / HTML entities
			else if (contentType.compareToIgnoreCase("text/html") == 0) {
				result = EntityUtils.toString(entity);
			}
		}
		Log.v(TAG, (result.toString()));
		return result;
	}

	/**
	 * @param httpOptions The OPTIONS message to send to the Exchange server
	 * @return The headers returned by the Exchange server
	 * @throws Exception
	 * 
	 * Sends an OPTIONS request to the Exchange server
	 */
	private Header[] sendOptionsRequest(HttpOptions httpOptions)
			throws Exception {

		// Send the OPTIONS message
		HttpClient client = createHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpResponse response = client.execute(httpOptions, localContext);

		Log.v(TAG, (response.getStatusLine().toString()));
		Header[] headers = response.getAllHeaders();

		return headers;
	}

	/**
	 * @return The headers returned by the Exchange server
	 * @throws Exception
	 * 
	 * Get the options that are supported by the Exchange server. 
	 * This is accomplished by sending an OPTIONS request with the Cmd set to SYNC
	 */
	public Header[] getOptions() throws Exception {
		String uri = mUri + "Sync";
		return sendOptionsRequest(createHttpOptions(uri));

	}

	/**
	 * @throws Exception
	 * 
	 * Send a Sync command to the Exchange server
	 */
	public void sync() throws Exception {
		String uri = mUri + "Sync";
		sendPostRequest(createHttpPost(uri, null));
	}

	/**
	 * @throws Exception
	 * Send a FolderSync command to the Exchange server
	 */
	public void folderSync() throws Exception {
		// Create the request
		String uri = mUri + "FolderSync";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<FolderSync xmlns=\"FolderHierarchy:\">\n"
				+ "\t<SyncKey>0</SyncKey>\n" + "</FolderSync>";

		// Send it to the server
		sendPostRequest(createHttpPost(uri, xml, true));
	}

	/**
	 * @param name The name to search the GAL for
	 * @return The list of contacts that match the query
	 * @throws Exception
	 * 
	 * This method searches the GAL on the Exchange server
	 */
	public Hashtable<String, Contact> searchGAL(String name) throws Exception {
		// Create the request
		String uri = mUri + "Search";

		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<Search xmlns=\"Search:\">\n" + "\t<Store>\n"
				+ "\t\t<Name>GAL</Name>\n" + "\t\t<Query>" + name
				+ "</Query>\n" + "\t</Store>\n" + "</Search>";

		// Send it to the server
		String result = sendPostRequest(createHttpPost(uri,xml,true));
//		String result = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
//				"<Search xmlns=\"Search\"><Status>1</Status><Response><Store>" +
//				"<Status>1</Status><Result><Properties><DisplayName>Duck, Donald</DisplayName>" +
//				"<Phone>1-858-555-1234</Phone><Office>AB-CDEF</Office>" +
//				"<Title>Engineer, Senior</Title><Company>Big Brother Inc</Company>" +
//				"<Alias>dduck</Alias><FirstName>Donald</FirstName>" +
//				"<LastName>Duck</LastName><EmailAddress>dduck@example.com</EmailAddress>" +
//				"</Properties></Result></Store></Response></Search>";
		
		Log.v(TAG,result);
		
		// parse and return the results
		return parseXML(result);
	}

	/**
	 * @throws Exception
	 * 
	 * Sends a Provision comand to the Exchange server. Only needed for Exchange 2007 and above 
	 */
	public void provisionDevice() throws Exception {

		// Create the request
		String uri = mUri + "Provision";

		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<Provision xmlns=\"Provision:\">\n" + "\t<Policies>\n"
				+ "\t\t<Policy>\n"
				+ "\t\t\t<PolicyType>MS-EAS-Provisioning-WBXML</PolicyType>\n"
				+ "\t\t</Policy>\n" + "\t</Policies>\n" + "</Provision>";

		xml = sendPostRequest(createHttpPost(uri, xml, true));

		// Get the temporary policy key from the server
		String[] result = parseXML(xml, "PolicyKey");

		if (result == null) {
			throw new Exception("Error provisioning device");
		}

		// Now that we have the temporary policy key,
		// Tell the server that we accept all the provisioning settings by  
		// Setting the status to 1
		xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<Provision xmlns=\"Provision:\">\n" + "\t<Policies>\n"
				+ "\t\t<Policy>\n"
				+ "\t\t\t<PolicyType>MS-EAS-Provisioning-WBXML</PolicyType>\n"
				+ "\t\t\t<PolicyKey>" + result[0] + "</PolicyKey>\n"
				+ "\t\t\t<Status>1</Status>\n" + "\t\t</Policy>\n"
				+ "\t</Policies>\n" + "</Provision>";

		xml = sendPostRequest(createHttpPost(uri, xml, false));

		// Get the final policy key
		mPolicyKey = parseXML(xml, "PolicyKey")[0];
		Log.v(TAG, "Policy Key: " + mPolicyKey);
	}

	/**
	 * @return the HttpClient object
	 * 
	 * Creates a HttpClient object that is used to POST messages to the Exchange server
	 */
	private HttpClient createHttpClient() {		
		HttpParams httpParams = new BasicHttpParams();

        // Turn off stale checking.  Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);

        // Default connection and socket timeout of 20 seconds.  Tweak to taste.
        HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
        
		SchemeRegistry registry = new SchemeRegistry();
	    registry.register(new Scheme("http", new PlainSocketFactory(), 80));
	    registry.register(new Scheme("https", new FakeSocketFactory() , 443));
	    HttpClient httpclient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, registry), httpParams);		

		// Set the headers
		httpclient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
				"Android");

		// Make sure we are not validating any hostnames
		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		sslSocketFactory
				.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		return httpclient;
	}

	private HttpPost createHttpPost(String uri, String requestXML)
			throws Exception {
		return createHttpPost(uri, requestXML, false);
	}

	/**
	 * @param uri The URI to send the POST message to
	 * @param requestXML The XML to send in the message
	 * @param includePolicyKey Should we include the policyKey in the header
	 * @return The POST request that can be sent to the server
	 * @throws Exception
	 * 
	 * Creates a POST request that can be sent to the Exchange server. This method
	 * sets all the necessary headers in the POST message that are required for
	 * the Exchange server to respond appropriately
	 */
	private HttpPost createHttpPost(String uri, String requestXML,
			boolean includePolicyKey) throws Exception {

		// Set the common headers
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setHeader("User-Agent", "Android");
		httpPost.setHeader("Accept", "*/*");
		httpPost.setHeader("Content-Type", "application/vnd.ms-sync.wbxml");
		httpPost.setHeader("MS-ASProtocolVersion", mActiveSyncVersion);
		Log.v(TAG, mActiveSyncVersion);
		httpPost.setHeader("Accept-Language", "en-us");
		httpPost.setHeader("Authorization", mAuthString);

		// Include policy key if required
		if (includePolicyKey)
			httpPost.setHeader("X-MS-PolicyKey", mPolicyKey);

		// Add the XML to the request
		if (requestXML != null) {
			Log.v(TAG, requestXML);
			// Set the body
			// Convert the XML to WBXML
			ByteArrayInputStream xmlParseInputStream = new ByteArrayInputStream(
					requestXML.toString().getBytes());

			java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
			wbxml.convertXmlToWbxml(xmlParseInputStream, output);
			byte[] bytes = output.toByteArray();

			String s = "";
			for (byte b : bytes) {
				s += String.format("%02x ", b);
			}

			Log.v(TAG, s);

			ByteArrayEntity myEntity = new ByteArrayEntity(bytes);
			myEntity.setContentType("application/vnd.ms-sync.wbxml");
			httpPost.setEntity(myEntity);
		}
		return httpPost;
	}

	/**
	 * @param uri The URI that the request needs to be sent to
	 * @return The OPTIONS request that can be sent to the server
	 * 
	 * This method creates an OPTIONS request that can be sent to the Exchange server
	 * to query for the features that are supported by the server
	 */
	private HttpOptions createHttpOptions(String uri) {
		HttpOptions httpOptions = new HttpOptions(uri);
		httpOptions.setHeader("User-Agent", "Android");
		httpOptions.setHeader("Accept", "*/*");
		httpOptions.setHeader("Content-Type", "application/vnd.ms-sync.wbxml");
		httpOptions.setHeader("MS-ASProtocolVersion", mActiveSyncVersion);
		httpOptions.setHeader("Accept-Language", "en-us");
		httpOptions.setHeader("Authorization", mAuthString);

		return httpOptions;
	}

	/**
	 * @param xml The XML to parse for contacts
	 * @return List of contacts tagged with the Display name
	 * @throws Exception
	 * 
	 * This method parses an XML containing a list of contacts and returns 
	 * a hashtable containing the contacts in the XML
	 * indexed by the DisplayName of the contacts
	 */
	private Hashtable<String, Contact> parseXML(String xml) throws Exception {
		// Our parser does not handle ampersands too well. So replace these with &amp;
		xml = Utility.replaceAmpersandWithEntityString(xml);
		 
		//Parse the XML
		ByteArrayInputStream xmlParseInputStream = new ByteArrayInputStream(xml
				.toString().getBytes());
		XMLReader xr = XMLReaderFactory.createXMLReader();

		XMLParser parser = null;
		parser = new XMLParser();
		xr.setContentHandler(parser);
		xr.parse(new InputSource(xmlParseInputStream));
		return parser.getContacts();
	}

	/**
	 * @param xml The XML to parse 
	 * @param nodeName The Node to search for in the XML 
	 * @return List of strings found in the specified node
	 * @throws Exception
	 * 
	 * This method parses the an XML string and returns all values that were found
	 * in the node specified in the request 
	 */
	private String[] parseXML(String xml, String nodeName) throws Exception {
		// Our parser does not handle ampersands too well. Replace with &amp;
		xml = Utility.replaceAmpersandWithEntityString(xml);
		
		// Parse the XML
		ByteArrayInputStream xmlParseInputStream = new ByteArrayInputStream(xml
				.toString().getBytes());
		XMLReader xr = XMLReaderFactory.createXMLReader();
		XMLParser parser = new XMLParser(nodeName);
		xr.setContentHandler(parser);
		xr.parse(new InputSource(xmlParseInputStream));
		return parser.getOutput();
	}

}
