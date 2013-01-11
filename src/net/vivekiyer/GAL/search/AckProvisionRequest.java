package net.vivekiyer.GAL.search;

import java.io.IOException;

import com.android.exchange.adapter.Serializer;
import com.android.exchange.adapter.Tags;

public class AckProvisionRequest extends CommandRequest {

	public String getPolicyType(){
		if (getProtocolVersionFloat() >= 12.0)
			return PROVISION_WBXML;		
		else
			return PROVISION_XML;
	}
	
	private String tempKey;
	private boolean remoteWipe;
	
	public AckProvisionRequest(String _uri, String _authString, boolean _useSSL,
			String _protocolVersion, boolean _acceptAllCerts, String _policyKey, String _tempKey, boolean _bRemoteWipe) {
		super(_uri, _authString, _useSSL, _protocolVersion, _acceptAllCerts, _policyKey);

		// Create the request
		setUri(getUri() + "Provision"); //$NON-NLS-1$
		
		tempKey = _tempKey;
		remoteWipe = _bRemoteWipe;

	}

	@Override
	protected void generateWBXMLPayload() throws IOException {
        Serializer s = new Serializer();
        s.start(Tags.PROVISION_PROVISION).start(Tags.PROVISION_POLICIES);
        s.start(Tags.PROVISION_POLICY);

        // Use the proper policy type, depending on EAS displayText
        s.data(Tags.PROVISION_POLICY_TYPE, getPolicyType());

        s.data(Tags.PROVISION_POLICY_KEY, tempKey);
        s.data(Tags.PROVISION_STATUS, "1"); //$NON-NLS-1$
        s.end().end(); // PROVISION_POLICY, PROVISION_POLICIES
        if (remoteWipe) {
            s.start(Tags.PROVISION_REMOTE_WIPE);
            s.data(Tags.PROVISION_STATUS, "1"); //$NON-NLS-1$
            s.end();
        }
        s.end().done(); // PROVISION_PROVISION
        
		setWbxmlBytes(s.toByteArray());
      }
}
