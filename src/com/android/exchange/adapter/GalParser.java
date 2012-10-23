/* Copyright (C) 2010 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.exchange.adapter;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.collect.HashMultimap;
import net.vivekiyer.GAL.Contact;
import net.vivekiyer.GAL.Debug;

/**
 * Parse the result of a GAL command.
 */
public class GalParser extends Parser {

	private int numResults = 0;
	
	private HashMultimap<String,Contact> contacts;
	
	public int getNumResults(){
		return numResults;
	}

	public HashMultimap<String,Contact> getResults(){
		return contacts;
	}
	
	public GalParser(InputStream in) throws IOException {
		super(in);
		contacts = HashMultimap.create();
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
				status = getValueInt();
				Debug.Log("GAL search status: " + status);
			} else {
				skipTag();
			}
		}
// Maybe later...
//		if((status != STATUS_NOT_SET) && (status != STATUS_OK))
//			throw new EasNotSuccessfulException("Search not successful", status);
		return numResults > 0;
	}

	public void parseProperties() throws IOException {
		Contact contact = new Contact();
		while (nextTag(Tags.SEARCH_STORE) != END) {
			switch(tag) {
			// Display name and email address use both legacy and new code for galData
			case Tags.GAL_DISPLAY_NAME: 
				contact.setDisplayName(getValue());
				break;
			case Tags.GAL_EMAIL_ADDRESS:
				contact.add("EmailAddress",getValue());
				break;
			case Tags.GAL_PHONE:
				contact.add("Phone",getValue());
				break;
			case Tags.GAL_OFFICE:
				contact.add("Office",getValue());
				break;
			case Tags.GAL_TITLE:
				contact.add("Title",getValue());
				break;
			case Tags.GAL_COMPANY:
				contact.add("Company",getValue());
				break;
			case Tags.GAL_ALIAS:
				contact.add("Alias",getValue());
				break;
			case Tags.GAL_FIRST_NAME:
				contact.add("FirstName",getValue());
				break;
			case Tags.GAL_LAST_NAME:
				contact.add("LastName",getValue());
				break;
			case Tags.GAL_HOME_PHONE:
				contact.add("HomePhone",getValue());
				break;
			case Tags.GAL_MOBILE_PHONE:
				contact.add("MobilePhone",getValue());
				break;
			default:
				skipTag();
			}
		}
		contacts.put(contact.getDisplayName(), contact);
	}

	public void parseResult() throws IOException {
		while (nextTag(Tags.SEARCH_STORE) != END) {
			if (tag == Tags.SEARCH_PROPERTIES) {
				parseProperties();
			} else {
				skipTag();
			}
		}
	}

	public void parseResponse() throws IOException {
		while (nextTag(Tags.SEARCH_RESPONSE) != END) {
			if (tag == Tags.SEARCH_STORE) {
				parseStore();
			} else if (tag == Tags.SEARCH_STATUS){
				String range = getValue();
				Debug.Log("GAL result range: " + range);
			}
			else {
				skipTag();
			}
		}
	}

	public void parseStore() throws IOException {
		while (nextTag(Tags.SEARCH_STORE) != END) {
			if (tag == Tags.SEARCH_RESULT) {
				parseResult();
			} else if (tag == Tags.SEARCH_STATUS){
				Debug.Log("Store status = "+getValue());
			}else if (tag == Tags.SEARCH_RANGE) {
				// Retrieve value, even if we're not using it for debug logging
				String range = getValue();
				Debug.Log("GAL result range: " + range);
			} else if (tag == Tags.SEARCH_TOTAL) {
				numResults = getValueInt();
				Debug.Log("total = " + getValueInt());
			} else {
				skipTag();
			}
		}
	}
}

