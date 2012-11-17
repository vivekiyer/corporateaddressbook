package net.vivekiyer.GAL;

import java.io.IOException;

import com.android.exchange.adapter.Serializer;
import com.android.exchange.adapter.Tags;

public class GalRequest extends CommandRequest {

	private String query;
	private int limit;
	
	public GalRequest(String _uri, String _authString, boolean _useSSL,
			String _protocolVersion, boolean _acceptAllCerts, String _policyKey, String _query, int _limit) {
		super(_uri, _authString, _useSSL, _protocolVersion, _acceptAllCerts, _policyKey);
		setUri(getUri() + "Search"); //$NON-NLS-1$
		query= _query;
		limit = _limit;
	}

	protected void generateWBXMLPayload() throws IOException {
		Serializer s = new Serializer();
		s.start(Tags.SEARCH_SEARCH).start(Tags.SEARCH_STORE);
		s.data(Tags.SEARCH_NAME, "GAL").data(Tags.SEARCH_QUERY, query); //$NON-NLS-1$
		s.start(Tags.SEARCH_OPTIONS);
		s.data(Tags.SEARCH_RANGE, "0-" + Integer.toString(limit - 1)); //$NON-NLS-1$

//      Pictures disabled for now since we have no way of testing
//		if(getProtocolVersionFloat() >= 14.1) {
//			s.start(Tags.SEARCH_PICTURE);
//			s.data(Tags.SEARCH_MAX_PICTURES, String.valueOf(limit));
//			s.end(); // SEARCH
//		}

		s.end().end().end().done(); // STORE / OPTIONS / PICTURE

		setWbxmlBytes(s.toByteArray());
	}
}
