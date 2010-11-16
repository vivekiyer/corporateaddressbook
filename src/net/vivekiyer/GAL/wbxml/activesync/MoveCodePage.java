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
 * This class is the specific code page for Move in the ActiveSync protocol.
 * The code page number is 5.
 * 
 * @version 1.0
 * @author  Matthew Brace
 */
public class MoveCodePage extends CodePage {
    /**
     * Constructor for MoveCodePage.  Initializes all of the code page values.
     */
    public MoveCodePage() {
        /* Maps String to Token for the code page */
        codepageTokens.put("MoveItems", 0x05);
        codepageTokens.put("Move", 0x06);
        codepageTokens.put("SrcMsgId", 0x07);
        codepageTokens.put("SrcFldId", 0x08);
        codepageTokens.put("DstFldId", 0x09);
        codepageTokens.put("Response", 0x0a);
        codepageTokens.put("Status", 0x0b);
        codepageTokens.put("DstMsgId", 0x0c);
        
        /* Maps token to string for the code page */
        for (String s : codepageTokens.keySet()) {
            codepageStrings.put(codepageTokens.get(s), s);
        }

        codePageIndex = 0x05;
        codePageName = "Move";
    }
}
