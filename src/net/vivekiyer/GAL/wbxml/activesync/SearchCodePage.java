/* Copyright 2010 Matthew Brace
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

package net.vivekiyer.GAL.wbxml.activesync;

import net.vivekiyer.GAL.wbxml.CodePage;

/**
 * This class is the specific code page for Search in the ActiveSync protocol.
 * The code page number is 15.
 * 
 * @version 1.0
 * @author Matthew Brace
 */
public class SearchCodePage extends CodePage {
	/**
	 * Constructor for SearchCodePage. Initializes all of the code page values.
	 */
	public SearchCodePage() {
		/* Maps String to Token for the code page */
		codepageTokens.put("Search", 0x05);
		codepageTokens.put("Store", 0x07);
		codepageTokens.put("Name", 0x08);
		codepageTokens.put("Query", 0x09);
		codepageTokens.put("Options", 0x0a);
		codepageTokens.put("Range", 0x0b);
		codepageTokens.put("Status", 0x0c);
		codepageTokens.put("Response", 0x0d);
		codepageTokens.put("Result", 0x0e);
		codepageTokens.put("Properties", 0x0f);
		codepageTokens.put("Total", 0x10);
		codepageTokens.put("EqualTo", 0x11);
		codepageTokens.put("Value", 0x12);
		codepageTokens.put("And", 0x13);
		codepageTokens.put("Or", 0x14);
		codepageTokens.put("FreeText", 0x15);
		codepageTokens.put("DeepTraversal", 0x17);
		codepageTokens.put("LongId", 0x18);
		codepageTokens.put("RebuildResults", 0x19);
		codepageTokens.put("LessThan", 0x1a);
		codepageTokens.put("GreaterThan", 0x1b);
		codepageTokens.put("Schema", 0x1c);
		codepageTokens.put("Supported", 0x1d);
		/*
		 * Tokens 0x06 and 0x16 intentionally ommitted. They are not supported
		 * by ActiveSync
		 */

		/* Maps token to string for the code page */
		for (String s : codepageTokens.keySet()) {
			codepageStrings.put(codepageTokens.get(s), s);
		}

		codePageIndex = 0x0f;
		codePageName = "Search";
	}
}
