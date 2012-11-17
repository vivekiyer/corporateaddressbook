package net.vivekiyer.GAL;

import java.io.IOException;

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

//This structure is used to store command-specific
//parameters (MS-ASCMD section 2.2.1.1.1.2.5)
class CommandParameter
{
	public String Parameter;
	public String Value;
}

public class CommandRequest {

	public static final String PROVISION_WBXML = "MS-EAS-Provisioning-WBXML"; //$NON-NLS-1$
	public static final String PROVISION_XML = "MS-WAP-Provisioning-XML"; //$NON-NLS-1$

	// Constants
	private final float APP_PROTOCOL_VERSION = 14.1F;
	private final String APP_PROTOCOL_VERSION_STRING = "14.1"; //$NON-NLS-1$

	// Constructor
	protected CommandRequest(
			String _uri,
			String _authString,
			boolean _useSSL,
			String _protocolVersion,
			boolean _acceptAllCerts,
			String _policyKey
			)
	{
		uri = _uri;
		authString = _authString;
		useSSL = _useSSL;
		protocolVersion = _protocolVersion;
		if(protocolVersion.length() > 0)
			protocolVersionFloat = Float.parseFloat(protocolVersion);
		acceptAllCerts = _acceptAllCerts;
		policyKey = _policyKey;
	}
		
	private String uri = null;
	protected String getUri() {
		return uri;
	}
	protected void setUri(String uri) {
		this.uri = uri;
	}

	private String authString = null;
	protected String getAuthString() {
		return authString;
	}
	protected void setAuthString(String authString) {
		this.authString = authString;
	}

	
	private boolean useSSL = true;
	protected boolean isUseSSL() {
		return useSSL;
	}
	protected void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	private byte[] wbxmlBytes = null;
	protected byte[] getWbxmlBytes() {
		return wbxmlBytes;
	}
	protected void setWbxmlBytes(byte[] wbxmlBytes) {
		this.wbxmlBytes = wbxmlBytes;
	}

	private float protocolVersionFloat = 0.0F;
	protected float getProtocolVersionFloat() {
		return protocolVersionFloat;
	}
	protected void setProtocolVersionFloat(float protocolVersion) {
		this.protocolVersionFloat = protocolVersion;
	}
	
	private String protocolVersion = null;
	protected String getProtocolVersion() {
		return protocolVersion;
	}
	protected void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
	
	private String command = null;
	protected String getCommand() {
		return command;
	}
	protected void setCommand(String command) {
		this.command = command;
	}

	private String policyKey = "0"; //$NON-NLS-1$
	protected String getPolicyKey() {
		return policyKey;
	}
	protected void setPolicyKey(String policyKey) {
		this.policyKey = policyKey;
	}

	private CommandParameter[] parameters = null;
	protected CommandParameter[] getParameters() {
		return parameters;
	}
	protected void setParameters(CommandParameter[] parameters) {
		this.parameters = parameters;
	}
	
	private boolean acceptAllCerts;
	protected boolean isAcceptAllCerts() {
		return acceptAllCerts;
	}
	protected void setAcceptAllCerts(boolean acceptAllCerts) {
		this.acceptAllCerts = acceptAllCerts;
	}

	/**
	 * @return The Response from the server
	 * @throws Exception 
	 * 
	 * Creates a POST request and sends it to the Exchange server. 
	 */
	public HttpResponse getResponse(boolean includePolicyKey) throws Exception {

		if(authString == null || protocolVersion == null || uri == null)
			throw new Exception("Not initialized correctly"); //$NON-NLS-1$
		
		// Generate the WBXML payload
		generateWBXMLPayload();

		// Set the common headers
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setHeader("User-Agent", App.VERSION_STRING); //$NON-NLS-1$
		httpPost.setHeader("DeviceType", "Android");		 //$NON-NLS-1$ //$NON-NLS-2$
		
		httpPost.setHeader("Accept", "*/*"); //$NON-NLS-1$ //$NON-NLS-2$
//		httpPost.setHeader("Accept-Encoding", "gzip");
		httpPost.setHeader("Content-Type", "application/vnd.ms-sync.wbxml"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// We will indicate that we are a 14.1 client
		// unless the server does not support it
		if(protocolVersionFloat >= APP_PROTOCOL_VERSION)
			httpPost.setHeader("MS-ASProtocolVersion", APP_PROTOCOL_VERSION_STRING); //$NON-NLS-1$
		else
			httpPost.setHeader("MS-ASProtocolVersion", protocolVersion); //$NON-NLS-1$
		
		httpPost.setHeader("Authorization", getAuthString()); //$NON-NLS-1$

		// Include policy key if required
		if (includePolicyKey)
			httpPost.setHeader("X-MS-PolicyKey", policyKey); //$NON-NLS-1$

		// Add the XML to the request
		if (wbxmlBytes != null) {
			ByteArrayEntity myEntity = new ByteArrayEntity(wbxmlBytes);
			myEntity.setContentType("application/vnd.ms-sync.wbxml"); //$NON-NLS-1$
			httpPost.setEntity(myEntity);
		}
		
		// POST the request to the server
		HttpClient client = createHttpClient();
		HttpContext localContext = new BasicHttpContext();
		return client.execute(httpPost, localContext);
	}
	
	/**
	 * @return The headers returned by the Exchange server
	 * @throws Exception
	 * 
	 * Get the options that are supported by the Exchange server. 
	 * This is accomplished by sending an OPTIONS request with the Cmd set to SYNC
	 */
	public HttpResponse getOptions() throws Exception {
		HttpOptions httpOptions = new HttpOptions(getUri());
		httpOptions.setHeader("User-Agent", App.VERSION_STRING); //$NON-NLS-1$
		httpOptions.setHeader("Authorization", getAuthString()); //$NON-NLS-1$

		// Send the OPTIONS message
		HttpClient client = createHttpClient();
		HttpContext localContext = new BasicHttpContext();
		return client.execute(httpOptions, localContext);

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
        HttpConnectionParams.setConnectionTimeout(httpParams, 15 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 15 * 1000);
        HttpConnectionParams.setSocketBufferSize(httpParams, 131072);
        
		SchemeRegistry registry = new SchemeRegistry();
	    registry.register(
	    		new Scheme("http", new PlainSocketFactory(), 80)); //$NON-NLS-1$
	    registry.register(
	    		new Scheme(
	    				"https", acceptAllCerts ? new FakeSocketFactory() : SSLSocketFactory.getSocketFactory() ,  //$NON-NLS-1$
	    				443));
	    HttpClient httpclient = new DefaultHttpClient(
	    		new ThreadSafeClientConnManager(httpParams, registry), httpParams);		

		// Set the headers
		httpclient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
				"Android"); //$NON-NLS-1$

		// Make sure we are not validating any hostnames
		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		sslSocketFactory
				.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		return httpclient;
	}
		
	// This function generates an WBXML payload.
	// Classes that extend this class to implement
	// commands override this function to generate
	// the WBXML payload based on the command's request schema
	protected void generateWBXMLPayload() throws IOException {
	}
}
