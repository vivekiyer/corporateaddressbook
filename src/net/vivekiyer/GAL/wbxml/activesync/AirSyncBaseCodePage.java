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
 * This class is the specific code page for AirSyncBase in the ActiveSync
 * protocol. The code page number is 17.
 * 
 * @version 1.0
 * @author Matthew Brace
 */
public class AirSyncBaseCodePage extends CodePage {
	/**
	 * Constructor for AirSyncBaseCodePage. Initializes all of the code page
	 * values.
	 */
	public AirSyncBaseCodePage() {
		/* Maps String to Token for the code page */
		codepageTokens.put("BodyPreference", 0x05);
		codepageTokens.put("Type", 0x06);
		codepageTokens.put("TruncationSize", 0x07);
		codepageTokens.put("AllOrNone", 0x08);
		codepageTokens.put("Body", 0x0a);
		codepageTokens.put("Data", 0x0b);
		codepageTokens.put("EstimatedDataSize", 0x0c);
		codepageTokens.put("Truncated", 0x0d);
		codepageTokens.put("Attachments", 0x0e);
		codepageTokens.put("Attachment", 0x0f);
		codepageTokens.put("DisplayName", 0x10);
		codepageTokens.put("FileReference", 0x11);
		codepageTokens.put("Method", 0x12);
		codepageTokens.put("ContentId", 0x13);
		codepageTokens.put("ContentLocation", 0x14);
		codepageTokens.put("IsInline", 0x15);
		codepageTokens.put("NativeBodyType", 0x16);
		codepageTokens.put("ContentType", 0x17);

		/* Maps token to string for the code page */
		for (String s : codepageTokens.keySet()) {
			codepageStrings.put(codepageTokens.get(s), s);
		}

		codePageIndex = 0x11;
		codePageName = "AirSyncBase";
	}
}
