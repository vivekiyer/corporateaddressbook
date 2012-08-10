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

import net.vivekiyer.GAL.Debug;

/**
 * Parse the result of a GAL command.
 */
public class GalParser extends Parser {

	private int numResults = 0;
	
	public int getNumResults(){
		return numResults;
	}

	public GalParser(InputStream in) throws IOException {
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
			} else {
				skipTag();
			}
		}
		return numResults > 0;
	}

	public void parseProperties() throws IOException {
		while (nextTag(Tags.SEARCH_STORE) != END) {
			switch(tag) {
			// Display name and email address use both legacy and new code for galData
			case Tags.GAL_DISPLAY_NAME: 
				System.out.println("DISPLAY NAME:" + getValue());
				break;
			case Tags.GAL_EMAIL_ADDRESS:
				System.out.println("Email: "+ getValue());
				break;
			case Tags.GAL_PHONE:
				System.out.println("Phone: "+ getValue());
				break;
			case Tags.GAL_OFFICE:
				System.out.println("Office: "+ getValue());
				break;
			case Tags.GAL_TITLE:
				System.out.println("Title: "+ getValue());
				break;
			case Tags.GAL_COMPANY:
				System.out.println("Company: "+ getValue());
				break;
			case Tags.GAL_ALIAS:
				System.out.println("Alias: "+ getValue());
				break;
			case Tags.GAL_FIRST_NAME:
				System.out.println("First Nam: "+ getValue());
				break;
			case Tags.GAL_LAST_NAME:
				System.out.println("Last Name: "+ getValue());
				break;
			case Tags.GAL_HOME_PHONE:
				System.out.println("Home Phone: "+ getValue());
				break;
			case Tags.GAL_MOBILE_PHONE:
				System.out.println("Mobile Phone: "+ getValue());
				break;
			default:
				skipTag();
			}
		}
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
				System.out.println("Status = "+getValue());
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

