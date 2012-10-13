package net.vivekiyer.GAL;

import com.android.exchange.adapter.GalParser;
import com.android.exchange.adapter.Tags;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 2012-10-06
 * Time: 17:23
 *
 * Extends the std GalParser to also present the search status to discover extended status info
 */
public class Parser extends GalParser {

	// Status according to http://blogs.msdn.com/b/exchangedev/archive/2011/08/19/provisioning-policies-remote-wipe-and-abq-in-exchange-activesync.aspx
	// In Exchange versions 12.1 and earlier, these were all summarized in the server sending a HTTP 449
	// Versions later than 12.1 support a more fine tuned mechanism for provisioning status
	// 142-144 are transient errors that should go away by (re)provisioning. the others are
	// non-transient, they need config change on server to disappear.
	public static final int STATUS_OK                                       = 1;
	public static final int STATUS_NOT_FULLY_PROVISIONABLE                  = 139;
	public static final int STATUS_REMOTE_WIPE_REQUESTED                    = 140;
	public static final int STATUS_LEGACY_DEVICE_ON_STRICT_POLICY           = 141;
	public static final int STATUS_DEVICE_NOT_PROVISIONED                   = 142;
	public static final int STATUS_POLICY_REFRESH                           = 143;
	public static final int STATUS_INVALID_POLICY_KEY                       = 144;
	public static final int STATUS_EXTERNALLY_MANAGED_DEVICES_NOT_ALLOWED   = 145;

	private int searchStatus = STATUS_OK;

	public int getSearchStatus() {
		return searchStatus;
	}

	public Parser(InputStream in) throws IOException {
		super(in);
	}

	@Override
	public boolean parse() throws IOException {
		if (nextTag(START_DOCUMENT) != Tags.SEARCH_SEARCH) {
			throw new IOException();
		}
		while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
			if (tag == Tags.SEARCH_RESPONSE) {
				parseResponse();
			} else if (tag == Tags.SEARCH_STATUS){
				searchStatus = getValueInt();
				Debug.Log("GAL search status: " + searchStatus);
			} else {
				skipTag();
			}
		}
		return getNumResults() > 0;
	}

}
