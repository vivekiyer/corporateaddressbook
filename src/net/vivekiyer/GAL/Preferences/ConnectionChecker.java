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

package net.vivekiyer.GAL.Preferences;

import net.vivekiyer.GAL.ActiveSyncManager;
import net.vivekiyer.GAL.Parser;
import net.vivekiyer.GAL.TaskCompleteCallback;
import android.os.AsyncTask;

import java.net.UnknownHostException;

/**
 * @author Vivek Iyer
 *
 * This class is a helper class that checks if the settings provided by the user are valid
 * and correspond to a valid Exchange server. It does this by querying the Exchange server for
 * OPTIONS (via the OPTIONS command) and gets the Active Sync protocol version supported by the server
 * If the server is running 2007 or above, it also provisions the device on the server. 
 */
class ConnectionChecker extends AsyncTask<ActiveSyncManager, Void, Boolean> {
	
	public static final int SSL_PEER_UNVERIFIED = -1;
	public static final int UNKNOWN_HOST = -2;

	// Callback to call once the check is complete
	private TaskCompleteCallback callback;	
	
	// variable that stores the status of the HTTP connection
	private int statusCode = 0;
	
	// variable that stores the status of the parsed message
	// (if available; if not it is equal to Parser.STATUS_NOT_SET)
	private int requestStatus = Parser.STATUS_NOT_SET;
	
	// variable that stores the error string
	private String errorString = "";
	
	/**
	 * @param callback Callback to call once the task is complete
	 */
	public void setCallback(TaskCompleteCallback callback) {
		this.callback = callback;
	}
	
	public ConnectionChecker(TaskCompleteCallback _callback){
		callback = _callback;
	}
	

	@Override
	protected Boolean doInBackground(ActiveSyncManager... params) {
		try {
			ActiveSyncManager syncMgr = params[0];
			// Let's try to connect to the server
			statusCode = syncMgr.getExchangeServerVersion();
			
			requestStatus = syncMgr.getRequestStatus();
			
			return ((statusCode == 200) &&
					((requestStatus == Parser.STATUS_NOT_SET) || (requestStatus == Parser.STATUS_OK)));
				
		} catch (javax.net.ssl.SSLPeerUnverifiedException spue) {
			statusCode = SSL_PEER_UNVERIFIED;
			errorString = spue.toString();
		} catch (UnknownHostException e) {
			statusCode = UNKNOWN_HOST;
			errorString = e.toString();
		} catch (Exception e) {
			errorString = e.toString();
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {		
		// now that the task is complete
		// call the callback with the status
		callback.taskComplete(result, statusCode, requestStatus, errorString);
	}
}
