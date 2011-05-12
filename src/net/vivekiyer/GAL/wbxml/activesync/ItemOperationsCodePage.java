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
 * This class is the specific code page for ItemOperations in the ActiveSync
 * protocol. The code page number is 20.
 * 
 * @version 1.0
 * @author Matthew Brace
 */
public class ItemOperationsCodePage extends CodePage {
	/**
	 * Constructor for ItemOperationsCodePage. Initializes all of the code page
	 * values.
	 */
	public ItemOperationsCodePage() {
		/* Maps String to Token for the code page */
		codepageTokens.put("ItemOperations", 0x05);
		codepageTokens.put("Fetch", 0x06);
		codepageTokens.put("Store", 0x07);
		codepageTokens.put("Option", 0x08);
		codepageTokens.put("Range", 0x09);
		codepageTokens.put("Total", 0x0a);
		codepageTokens.put("Properties", 0x0b);
		codepageTokens.put("Data", 0x0c);
		codepageTokens.put("Status", 0x0d);
		codepageTokens.put("Response", 0x0e);
		codepageTokens.put("Version", 0x0f);
		codepageTokens.put("Schema", 0x10);
		codepageTokens.put("Part", 0x11);
		codepageTokens.put("EmptyFolderContents", 0x12);
		codepageTokens.put("DeleteSubFolders", 0x13);

		/* Maps token to string for the code page */
		for (String s : codepageTokens.keySet()) {
			codepageStrings.put(codepageTokens.get(s), s);
		}

		codePageIndex = 0x14;
		codePageName = "ItemOperations";
	}
}
