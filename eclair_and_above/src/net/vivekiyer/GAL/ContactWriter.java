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

import android.app.Application;
import android.content.Context;
import android.os.Build;

/**
 * This abstract class defines SDK-independent API for communication with
 * Contacts Provider. The actual implementation used by the application depends
 * on the level of API available on the device. If the API level is Cupcake or
 * Donut, we want to use the {@link ContactAccessorSdk3_4} class. If it is
 * Eclair or higher, we want to use {@link ContactAccessorSdk5}.
 */

public abstract class ContactWriter {
	/*
	 * Copyright (C) 2009 The Android Open Source Project
	 * 
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may
	 * not use this file except in compliance with the License. You may obtain a
	 * copy of the License at
	 * 
	 * http://www.apache.org/licenses/LICENSE-2.0
	 * 
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	 * License for the specific language governing permissions and limitations
	 * under the License.
	 */

	/**
	 * Static singleton instance of {@link ContactAccessor} holding the
	 * SDK-specific implementation of the class.
	 */
	private static ContactWriter sInstance;

	// TAG used for logging
	//private static String TAG = "ContactWriterSdk5";

	public static ContactWriter getInstance() {
		if (sInstance == null) {
			String className;

			/*
			 * Check the version of the SDK we are running on. Choose an
			 * implementation class designed for that version of the SDK.
			 * 
			 * Unfortunately we have to use strings to represent the class
			 * names. If we used the conventional
			 * ContactAccessorSdk5.class.getName() syntax, we would get a
			 * ClassNotFoundException at runtime on pre-Eclair SDKs. Using the
			 * above syntax would force Dalvik to load the class and try to
			 * resolve references to all other classes it uses. Since the
			 * pre-Eclair does not have those classes, the loading of
			 * ContactAccessorSdk5 would fail.
			 */
			@SuppressWarnings("deprecation")
			int sdkVersion = Integer.parseInt(Build.VERSION.SDK); // Cupcake
																	// style
			if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
				className = "net.vivekiyer.GAL.ContactWriterSdk3_4";
				//Log.d(TAG,"Detected pre Eclair SDK. SDK Version=" + sdkVersion);
			} else {
				className = "net.vivekiyer.GAL.ContactWriterSdk5";
				//Log.d(TAG,"Detected post Donut SDK. SDK Version=" + sdkVersion);
			}

			/*
			 * Find the required class by name and instantiate it.
			 */
			try {
				Class<? extends ContactWriter> clazz = Class.forName(className)
						.asSubclass(ContactWriter.class);
				sInstance = clazz.newInstance();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return sInstance;
	}

	public abstract void Initialize(Application appCtx, Contact contact);
	public abstract void cleanUp();
	public abstract void saveContact(Context ctx);
}
