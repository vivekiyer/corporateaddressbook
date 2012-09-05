package com.android.exchange.adapter;

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

import java.io.IOException;
import java.io.InputStream;

import net.vivekiyer.GAL.Debug;

import com.android.exchange.adapter.Parser;
import com.android.exchange.adapter.Tags;

/**
 * Parse the result of the Provision command
 *
 * Assuming a successful parse, we store the PolicySet and the policy key
 */
public class ProvisionParser extends Parser {
    String mSecuritySyncKey = null;
    boolean mRemoteWipe = false;

    public boolean isRemoteWipeRequested() {
		return mRemoteWipe;
	}

	public ProvisionParser(InputStream in) throws IOException {
        super(in);
    }

    public String getSecuritySyncKey() {
        return mSecuritySyncKey;
    }

    public void setSecuritySyncKey(String securitySyncKey) {
        mSecuritySyncKey = securitySyncKey;
    }

     private void parsePolicy() throws IOException {
        String policyType = null;
        while (nextTag(Tags.PROVISION_POLICY) != END) {
            switch (tag) {
                case Tags.PROVISION_POLICY_TYPE:
                    policyType = getValue();
                    Debug.Log("Policy type: "+ policyType);
                    break;
                case Tags.PROVISION_POLICY_KEY:
                    mSecuritySyncKey = getValue();
                    break;
                case Tags.PROVISION_STATUS:
                	Debug.Log("Policy status: "+ getValue());
                    break;
                default:
                    skipTag();
            }
        }
    }

    private void parsePolicies() throws IOException {
        while (nextTag(Tags.PROVISION_POLICIES) != END) {
            if (tag == Tags.PROVISION_POLICY) {
                parsePolicy();
            } else {
                skipTag();
            }
        }
    }

    @Override
    public boolean parse() throws IOException {
        boolean res = false;
        if (nextTag(START_DOCUMENT) != Tags.PROVISION_PROVISION) {
            throw new IOException();
        }
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
            switch (tag) {
                case Tags.PROVISION_STATUS:
                    int status = getValueInt();
                    Debug.Log("Provision status: " + status);
                    res = (status == 1);
                    break;
                case Tags.PROVISION_POLICIES:
                    parsePolicies();
                    break;
                case Tags.PROVISION_REMOTE_WIPE:
                    // Indicate remote wipe command received
                    mRemoteWipe = true;
                    break;
                default:
                    skipTag();
            }
        }
        return res;
    }
}
