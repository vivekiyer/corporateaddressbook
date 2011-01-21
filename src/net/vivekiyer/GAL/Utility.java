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

import android.app.AlertDialog;
import android.content.Context;

/**
 * @author Vivek Iyer
 * Utility methods
 */
public class Utility {    
    
    /**
     * @param encoded base64 data
     * @return decoded data
     * Decodes base64 data
     */
    public static byte[] base64Decode(byte[] encoded) {
        if (encoded == null) {
            return null;
        }
        byte[] decoded = new Base64().decode(encoded);
        return decoded;
    }

    /**
     * @param s String to encode
     * @return base64 encoded string
     * Encoder the string to base64
     */
    public static String base64Encode(String s) {
        if (s == null) {
            return s;
        }
        byte[] encoded = new Base64().encode(s.getBytes());
        return new String(encoded);
    }
     
	/**
	 * @param s The alert message
	 * Displays an alert dialog with the messaged provided
	 */
	public static void showAlert(Context context, String mesg){
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);
		alt_bld.setMessage(mesg)
				.setPositiveButton("Ok", null);
		AlertDialog alert = alt_bld.create();
		alert.show();	
	}
}
