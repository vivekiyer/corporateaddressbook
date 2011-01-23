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

import android.os.AsyncTask;

/**
 * @author Vivek Iyer
 *
 * This class is a helper class that checks if the settings provided by the user are valid
 * and correspond to a valid Exchange server. It does this by querying the Exchange server for
 * OPTIONS (via the OPTIONS command) and gets the Active Sync protocol version supported by the server
 * If the server is running 2007 or above, it also provisions the device on the server. 
 */
class ConnectionChecker extends AsyncTask<ActiveSyncManager, Void, Boolean> {

	// Callback to call once the check is complete
	private TaskCompleteCallback callback;	
	
	// variable that stores the status of the connect
	private int statusCode = 0;
	
	/**
	 * @param callback Callback to call once the task is complete
	 */
	public void setCallback(TaskCompleteCallback callback) {
		this.callback = callback;
	}
	
	public ConnectionChecker(			
			TaskCompleteCallback _callback
			){
		callback = _callback;
	}
	

	@Override
	protected Boolean doInBackground(ActiveSyncManager... params) {
		try {
			// Let's try to connect to the server
			statusCode = params[0].getExchangeServerVersion();
			
			if(statusCode != 200)
				return false;
			
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {		
		// now that the task is complete
		// call the callback with the status
		callback.taskComplete(result, statusCode);
	}
}
