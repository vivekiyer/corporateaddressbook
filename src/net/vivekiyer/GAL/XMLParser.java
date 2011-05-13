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

import java.util.ArrayList;
import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Vivek Iyer
 * 
 *         XML parser that goes through an input XML and looks for certain nodes
 */
public class XMLParser extends DefaultHandler {

	// private static String TAG = "XMLParser";
	private String mNodeToFind;
	private boolean foundNode = false;
	private boolean foundStatus = false;
	private boolean createContact = false;
	private ArrayList<String> output;
	private Hashtable<String, Contact> contacts;
	private Contact contact;

	private String key;
	private String value;
	private String status = "1";

	private int startCol = 0;

	private Locator locator;

	/**
	 * @return The matched values
	 * @throws Exception
	 *             Returns the values that are contained in the input nodes
	 */
	public String[] getOutput() throws Exception {
		if (output.isEmpty())
			return null;

		return output.toArray(new String[output.size()]);
	}

	public int getStatus() {
		int statusCode = Integer.parseInt(status);
		// Set this to 200 to indicate a success
		return (statusCode == 1) ? 200 : statusCode;
	}

	/**
	 * @return Contacts
	 * @throws Exception
	 *             Returns all the contacts that were obtained by parsing the
	 *             XML
	 */
	public Hashtable<String, Contact> getContacts() throws Exception {
		return contacts;
	}

	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	/**
	 * @param nodeToFind
	 *            The node to find within the XML
	 */
	public XMLParser(String nodeToFind) {
		mNodeToFind = nodeToFind;
		output = new ArrayList<String>();
	}

	/**
	 * Default constructor
	 */
	public XMLParser() {
		createContact = true;
		contacts = new Hashtable<String, Contact>();
		output = new ArrayList<String>();
		mNodeToFind = "Properties";
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		if (locator != null) {
			startCol = locator.getColumnNumber();
		}

		// Lets see if we got the status element
		if (localName.compareToIgnoreCase("status") == 0)
			foundStatus = true;

		// Grab control when we reach the required node
		if (localName.compareToIgnoreCase(mNodeToFind) == 0) {
			foundNode = true;

			if (createContact)
				contact = new Contact();
		}
		key = localName;
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) {
		// Check for the end of the status element
		if (localName.compareToIgnoreCase("status") == 0)
			foundStatus = false;

		// Grab control when we the required node ends
		if (localName.compareToIgnoreCase(mNodeToFind) == 0) {
			foundNode = false;

			if (createContact)
				contacts.put(contact.getDisplayName(), contact);
		}
	}

	@Override
	public void characters(char ch[], int start, int length) {
		value = new String(ch, start, length);
		// Get the status value
		if (foundStatus) {
			// We only care for erroneous status
			if (value.equalsIgnoreCase("1") == false)
				status = value;
		}

		if (foundNode) {
			if (!createContact) {
				output.add(value);
			} else {
				if (key.compareToIgnoreCase("displayname") == 0) {
					contact.setDisplayName(value);
					// Log.d(TAG,"Display name="+value);
				} else {
					contact.add(key, value);
				}
			}
		}
	}

	public int getStartCol() {
		return startCol;
	}
}