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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

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

import android.util.Log;

import net.vivekiyer.GAL.wbxml.WBXML;

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
	private String mPolicyKey = "0";
	private String mAuthString;
	private String mUri;
	private String mServerName;
	private String mDomain;
	private WBXML wbxml;
	private String mUsername;
	private String mPassword;
	private boolean mUseSSL;
	private boolean mAcceptAllCerts;
	private String mActiveSyncVersion = "";	
	private int mDeviceId = 0;
	private float mActiveSyncVersionFloat = 0.0F;
	
	//private static final String TAG = "ActiveSyncManager";
	private float getActiveSyncVersionFloat(){
		if(mActiveSyncVersionFloat == 0.0F)
			mActiveSyncVersionFloat = Float.parseFloat(mActiveSyncVersion);
		return mActiveSyncVersionFloat;
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

	public void setmUsername(String username) {
		this.mUsername = username;
	}

	public void setPassword(String password) {
		this.mPassword = password;
	}

	public WBXML getWbxml() {
		return wbxml;
	}
	
	public int getDeviceId(){
		return mDeviceId;
	}
	
	public void setDeviceId(int deviceId){
		mDeviceId = deviceId;
	}
	
	
	/**
	 * Generates the auth string from the username, password and domain
	 */
	private void generateAuthString(){
		// For BPOS the DOMAIN is not required, so remove the backslash
		if(mDomain.equalsIgnoreCase(""))
			mAuthString = "Basic "
				+ Utility.base64Encode(mUsername + ":" + mPassword);
		else
		mAuthString = "Basic "
				+ Utility.base64Encode(mDomain + "\\" + mUsername + ":"
						+ mPassword);
	}
	
	/**
	 * Initializes the class by assigning the Exchange URL and the AuthString  
	 * @throws URISyntaxException 
	 */
	public boolean Initialize() {
		wbxml = new WBXML();

		generateAuthString();

		Random rand = new Random();
		
		// Generate a random deviceId that is greater than 0
		while(mDeviceId <= 0)
			mDeviceId = rand.nextInt();
		
		// If we don't have a server name, 
		// there is no way we can proceed
		if(mServerName.compareToIgnoreCase("") == 0)
			return false;
		
		// this is where we will send it
		try {
			URI uri = new URI(
					(mUseSSL) ? "https" : "http", 	// Scheme					
					mServerName ,					// Authority
					"/Microsoft-Server-ActiveSync", // path
					"User="							// query
					+ mUsername
					+ "&DeviceId=" 
					+ mDeviceId
					+ "&DeviceType=Android" 
					+ "&Cmd=",
					null							// fragment
			);
			
			mUri = uri.toString();
		} catch (URISyntaxException e) {
			// This really should not occur
			Log.d("ActiveSyncManager",e.toString());
			return false;
		}

		return true;
	}

	public ActiveSyncManager() {		
	}

	public ActiveSyncManager(
			String serverName, 
			String domain, 
			String username,
			String password,
			boolean useSSL,
			boolean acceptAllCerts,
			String policyKey, 
			String activeSyncVersion,
			int deviceId) {

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
	 * Connects to the Exchange server and obtains the version of ActiveSync supported 
	 * by the server 
	 */
	public int getExchangeServerVersion() throws Exception {
		
		// First get the options from the server
		HttpResponse response = getOptions();		

		// 200 indicates a success
		int statusCode = response.getStatusLine().getStatusCode() ; 
		
		if( statusCode == 200){
			
			Header [] headers = response.getHeaders("MS-ASProtocolVersions");
			
			if (headers.length != 0) {

				Header header = headers[0];
				
				// Parse out the ActiveSync Protocol version
				String versions = header.getValue();

				// Look for the last comma, and parse out the highest
				// version
				mActiveSyncVersion = versions.substring(versions
						.lastIndexOf(",") + 1);

				mActiveSyncVersionFloat = Float.parseFloat(mActiveSyncVersion);
				
				// Provision the device if necessary
				provisionDevice();
			}
		}
		return statusCode;
	}

	
	/**
	 * @param entity The entity to decode
	 * @return The decoded WBXML or text/HTML entity
	 * 
	 * Decodes the entity that is returned from the Exchange server
	 * @throws Exception 
	 * @throws  
	 */
	private String decodeContent(HttpEntity entity) throws Exception{
		String result = "";
		
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
		//Log.d(TAG, (result.toString()));
		return result;
		
	}
	
	/**
	 * @param httpPost The request to POST to the Exchange sever
	 * @return The response to the POST message
	 * @throws Exception
	 * 
	 * POSTs a message to the Exchange server. Any WBXML or String entities that are 
	 * returned by the server are parsed and returned to the callee
	 */
	private HttpResponse sendPostRequest(HttpPost httpPost) throws Exception {
		
		// POST the request to the server
		HttpClient client = createHttpClient();
		HttpContext localContext = new BasicHttpContext();
		return client.execute(httpPost, localContext);
	}

	/**
	 * @param httpOptions The OPTIONS message to send to the Exchange server
	 * @return The headers returned by the Exchange server
	 * @throws Exception
	 * 
	 * Sends an OPTIONS request to the Exchange server
	 */
	private HttpResponse sendOptionsRequest(HttpOptions httpOptions)
			throws Exception {

		// Send the OPTIONS message
		HttpClient client = createHttpClient();
		HttpContext localContext = new BasicHttpContext();
		return client.execute(httpOptions, localContext);
	}

	/**
	 * @return The headers returned by the Exchange server
	 * @throws Exception
	 * 
	 * Get the options that are supported by the Exchange server. 
	 * This is accomplished by sending an OPTIONS request with the Cmd set to SYNC
	 */
	public HttpResponse getOptions() throws Exception {
		String uri = mUri; 
		return sendOptionsRequest(createHttpOptions(uri));
	}

	/**
	 * @throws Exception
	 * 
	 * Send a Sync command to the Exchange server
	 */
	public HttpResponse sync() throws Exception {
		String uri = mUri + "Sync";
		return sendPostRequest(createHttpPost(uri, null));
	}

	/**
	 * @throws Exception
	 * Send a FolderSync command to the Exchange server
	 */
	public HttpResponse folderSync() throws Exception {
		// Create the request
		String uri = mUri + "FolderSync";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<FolderSync xmlns=\"FolderHierarchy:\">\n"
				+ "\t<SyncKey>0</SyncKey>\n" + "</FolderSync>";

		// Send it to the server
		return sendPostRequest(createHttpPost(uri, xml, true));
	}

	/**
	 * @param query The name to search the GAL for
	 * @param result The XML contacts returned by the Exchange server
	 * 
	 * @return The status code returned from the Exchange server
	 * @throws Exception
	 * 
	 * This method searches the GAL on the Exchange server
	 */
	public int searchGAL(
			String query, 
			StringBuffer result) throws Exception 
	{
		// Create the request
		String uri = mUri + "Search";

		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<Search xmlns=\"Search:\">\n" + 
				"\t<Store>\n"
				+ "\t\t<Name>GAL</Name>\n" 
				+ "\t\t<Query>" + query + "</Query>\n" 
				+ "\t\t<Options>\n"
				+ "\t\t\t<Range>0-99</Range>\n"
				+ "\t\t</Options>\n"
				+ "\t</Store>\n"
				+ "</Search>";

		// Send it to the server
		HttpResponse response = sendPostRequest(createHttpPost(uri,xml,true));	
		
		// Check the response code to see if the result was 200
		// Only then try to decode the content
		
		int statusCode = response.getStatusLine().getStatusCode();
		
		if(statusCode == 200)
		{
			// Decode the XML content
			result.append(decodeContent(response.getEntity())); 
		}		
		
		// parse and return the results
		return statusCode;
	}

	/**
	 * @throws Exception
	 * 
	 * Sends a Provision command to the Exchange server. Only needed for Exchange 2007 and above 
	 */
	public void provisionDevice() throws Exception {

		// Create the request
		String uri = mUri + "Provision";
		String policyType;
		
		if (getActiveSyncVersionFloat() >= 12.0)
			policyType = "MS-EAS-Provisioning-WBXML";		
		else
			policyType =  "MS-WAP-Provisioning-XML";
		
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<Provision xmlns=\"Provision:\">\n" + "\t<Policies>\n"
				+ "\t\t<Policy>\n"
				+ "\t\t\t<PolicyType>" 
				+ policyType 
				+ "</PolicyType>\n"
				+ "\t\t</Policy>\n" + "\t</Policies>\n" + "</Provision>";
		
		HttpResponse response = sendPostRequest(createHttpPost(uri, xml, true));
		xml = decodeContent(response.getEntity());
		
		// Get the temporary policy key from the server
		String[] result = parseXML(xml, "PolicyKey");		
		
		if (result == null ) {
			//  This implies that there is no policy key
			// So return
			return;
		}

		// Now that we have the temporary policy key,
		// Tell the server that we accept all the provisioning settings by  
		// Setting the status to 1
		xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<Provision xmlns=\"Provision:\">\n" + "\t<Policies>\n"
				+ "\t\t<Policy>\n"
				+ "\t\t\t<PolicyType>" 
				+ policyType 
				+ "</PolicyType>\n"
				+ "\t\t\t<PolicyKey>" + result[0] + "</PolicyKey>\n"
				+ "\t\t\t<Status>1</Status>\n" + "\t\t</Policy>\n"
				+ "\t</Policies>\n" + "</Provision>";
	
		response = sendPostRequest(createHttpPost(uri, xml, false));
		xml = decodeContent(response.getEntity());
		
		// Get the final policy key
		mPolicyKey = parseXML(xml, "PolicyKey")[0];
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

        // Default connection and socket timeout of 120 seconds.  Tweak to taste.
        HttpConnectionParams.setConnectionTimeout(httpParams, 120 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 120 * 1000);
        HttpConnectionParams.setSocketBufferSize(httpParams, 131072);
        
		SchemeRegistry registry = new SchemeRegistry();
	    registry.register(
	    		new Scheme("http", new PlainSocketFactory(), 80));
	    registry.register(
	    		new Scheme(
	    				"https",mAcceptAllCerts ? new FakeSocketFactory() : SSLSocketFactory.getSocketFactory() , 
	    				443));
	    HttpClient httpclient = new DefaultHttpClient(
	    		new ThreadSafeClientConnManager(httpParams, registry), httpParams);		

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
		
		// If we are connecting to Exchange 2010 or above
		// Lets tell the Exchange server that we are a 12.1 client
		// This is so we don't have to support sending of additional
		// information in the provision method
		if(getActiveSyncVersionFloat() >= 14.0)
			httpPost.setHeader("MS-ASProtocolVersion", "12.1");
		// Else set the version to the highest version returned by the
		// Exchange server
		else
			httpPost.setHeader("MS-ASProtocolVersion", getActiveSyncVersion());
			
		
		//Log.d(TAG, mActiveSyncVersion);
		httpPost.setHeader("Accept-Language", "en-us");
		httpPost.setHeader("Authorization", mAuthString);

		// Include policy key if required
		if (includePolicyKey)
			httpPost.setHeader("X-MS-PolicyKey", mPolicyKey);

		// Add the XML to the request
		if (requestXML != null) {
			//Log.d(TAG, requestXML);
			// Set the body
			// Convert the XML to WBXML
			ByteArrayInputStream xmlParseInputStream = new ByteArrayInputStream(
					requestXML.toString().getBytes());

			java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
			wbxml.convertXmlToWbxml(xmlParseInputStream, output);
			byte[] bytes = output.toByteArray();

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
		httpOptions.setHeader("Authorization", mAuthString);

		return httpOptions;
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
		xml = xml.replaceAll("&", "&amp;");
		
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
