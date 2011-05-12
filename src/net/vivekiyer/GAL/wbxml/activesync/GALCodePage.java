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
 * This class is the specific code page for GAL in the ActiveSync protocol. The
 * code page number is 16.
 * 
 * @version 1.0
 * @author Matthew Brace
 */
public class GALCodePage extends CodePage {
	/**
	 * Constructor for GALCodePage. Initializes all of the code page values.
	 */
	public GALCodePage() {
		/* Maps String to Token for the code page */
		codepageTokens.put("DisplayName", 0x05);
		codepageTokens.put("Phone", 0x06);
		codepageTokens.put("Office", 0x07);
		codepageTokens.put("Title", 0x08);
		codepageTokens.put("Company", 0x09);
		codepageTokens.put("Alias", 0x0a);
		codepageTokens.put("FirstName", 0x0b);
		codepageTokens.put("LastName", 0x0c);
		codepageTokens.put("HomePhone", 0x0d);
		codepageTokens.put("MobilePhone", 0x0e);
		codepageTokens.put("EmailAddress", 0x0f);

		/* Maps token to string for the code page */
		for (String s : codepageTokens.keySet()) {
			codepageStrings.put(codepageTokens.get(s), s);
		}

		codePageIndex = 0x10;
		codePageName = "GAL";
	}
}
