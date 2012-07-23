package net.vivekiyer.GAL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import android.os.AsyncTask;

public class GALSearch extends AsyncTask<String, Void, Boolean>
{
	public interface OnSearchCompletedListener{
		void OnSearchCompleted(int result, Hashtable<String, Contact> contacts);
	}

	private ActiveSyncManager activeSyncManager;

	private String errorMesg = "";

	private int errorCode = 0;

	public OnSearchCompletedListener onSearchCompletedListener;
	
	Hashtable<String,Contact> mContacts = null;

	private String searchResultXML = "";

	public Hashtable<String, Contact> getContacts() {
		return mContacts;
	}
	public String getSearchResultXML() {
		return searchResultXML;
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

			final StringBuffer sb = new StringBuffer();
			int statusCode = 0;

				do {
				statusCode = activeSyncManager.searchGAL(params[0], sb);
					switch (statusCode) {
					case 200: // HTTP_OK
						// All went well, lets display the result
						searchResultXML = sb.toString();
						statusCode = parseXML(searchResultXML);
						break;
					case 449: // RETRY AFTER PROVISIONING
					case 142: // RETRY AFTER PROVISIONING
						// Looks like we need to provision again
						activeSyncManager.provisionDevice();
						break;
					case 401: // UNAUTHORIZED
						// Looks like the password expired
						errorCode = 401;
						errorMesg = "Authentication failed. Please check your credentials";
						return false;
					default:
						errorCode = statusCode;
						errorMesg = "Exchange server rejected request with error:"
								+ errorCode;
						return false;
					}
				} while (statusCode != 200);

		} catch (final Exception e) {
			if (Debug.Enabled) {
				Debug.Log(e.toString());
				Debug.Log(searchResultXML);
			} else {
				errorMesg = "Activesync version= "
						+ activeSyncManager.getActiveSyncVersion() + "\n"
						+ e.toString();
				return false;
			}
		}
		return true;
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
			onSearchCompletedListener.OnSearchCompleted(result ? errorCode : -1, mContacts);
	}
	
	public int parseXML(String xml) throws SAXException, IOException{
		// Our parser does not handle ampersands too well. So replace these with
		// &amp;
		xml = xml.replaceAll("&", "&amp;");
	
		// Parse the XML
		final ByteArrayInputStream xmlParseInputStream = new ByteArrayInputStream(
				xml.toString().getBytes());
		final XMLReader xr = XMLReaderFactory.createXMLReader();
	
		XMLParser parser = null;
		parser = new XMLParser();
		xr.setContentHandler(parser);
		xr.parse(new InputSource(xmlParseInputStream));
		mContacts = parser.getContacts();
		return parser.getStatus();
	}

}
