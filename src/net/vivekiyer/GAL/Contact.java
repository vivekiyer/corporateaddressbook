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

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Vivek Iyer
 *
 * This class parcels up the Contact object so that it can be passed between two activities 
 * without loss of data. It does this by writing the display name followed by all the 
 * contacts details into the parcel
 */
public class Contact implements Parcelable{

	private ArrayList<KeyValuePair> Details;

	private String DisplayName;
	
	public String getDisplayName() {
		return DisplayName;
	}

	public void setDisplayName(String displayName) {
		DisplayName = displayName;
	}

	public ArrayList<KeyValuePair> getDetails() {
		return Details;
	}

	public void setDetails(ArrayList<KeyValuePair> details) {
		Details = details;
	}	
	
	public Contact(String displayName){
		DisplayName = displayName;
		Details = new ArrayList<KeyValuePair>();
	}	
	
	public Contact(){
		Details = new ArrayList<KeyValuePair>();
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }
 
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
    
	// Load our class from the parcel
	public Contact(Parcel in){
		
		// The Display name for the contact
		DisplayName = in.readString();
		
		Details = new ArrayList<KeyValuePair>();
		
		// The number of elements in the array list
		int size = in.readInt();		
		
		// Each KVP in the Array List
		for(int i=0;i<size;i++){
			add(in.readString(), in.readString());
		}
	}
	
	public void add(String key, String value){
		Details.add(new KeyValuePair(key, value));
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	// Flatten this object into a parcel
	public void writeToParcel(Parcel dest, int flags) {
		// The Display name for the contact
		dest.writeString(DisplayName);
		
		// The number of elements in the array list
		dest.writeInt(Details.size());
		
		// Each KVP in the Array List
		for(KeyValuePair kvp : Details){
			dest.writeString(kvp.getKey());
			dest.writeString(kvp.getValue());
		}
	}		
	
}
