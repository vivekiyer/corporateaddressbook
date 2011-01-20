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
 *         This class parcels up the Contact object so that it can be passed
 *         between two activities without loss of data. It does this by writing
 *         the display name followed by all the contacts details into the parcel
 */
public class Contact implements Parcelable {

	private ArrayList<KeyValuePair> Details;

	private String DisplayName;

	private String workPhone = "";

	private String officeLocation = "";

	private String title = "";

	private String company = "";

	private String alias = "";

	private String firstName;

	private String lastName;

	private String homePhone = "";

	private String mobilePhone = "";

	private String email;
	
	// Field that stores the first non empty field
	private String firstNonEmptyField;
	
	private boolean convertedToFields = false;

	public String getDisplayName() {
		// If the XML did not contain a display name
		// Lets check for the first name and last name
		if(DisplayName == null)
		{			
			DisplayName = "";
	
			generateFieldsFromXML();

			if(firstName != null)
			{
				DisplayName += firstName;
				DisplayName += " ";
			}
			
			if(lastName != null)
				DisplayName += lastName;
			
			// If both the first name and last name are empty
			// Use the email address
			if(DisplayName.equalsIgnoreCase("") && email != null)
				DisplayName = email;			
			else if(firstNonEmptyField!=null)
				DisplayName = firstNonEmptyField;
		}
		return DisplayName;
	}

	public String getWorkPhone() {
		return workPhone;
	}

	public String getOfficeLocation() {
		return officeLocation;
	}

	public String getTitle() {
		return title;
	}

	public String getCompany() {
		return company;
	}

	public String getAlias() {
		return alias;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getHomePhone() {
		return homePhone;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public String getEmail() {
		return email;
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

	public Contact(String displayName) {
		DisplayName = displayName;
		Details = new ArrayList<KeyValuePair>();
	}

	public Contact() {
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
	public Contact(Parcel in) {

		// The Display name for the contact
		DisplayName = in.readString();

		Details = new ArrayList<KeyValuePair>();

		// The number of elements in the array list
		int size = in.readInt();

		// Each KVP in the Array List
		for (int i = 0; i < size; i++) {
			add(in.readString(), in.readString());
		}
	}

	public void add(String key, String value) {
		Details.add(new KeyValuePair(key, value));
	}

	@Override
	public int describeContents() {
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
		for (KeyValuePair kvp : Details) {
			dest.writeString(kvp.getKey());
			dest.writeString(kvp.getValue());
		}
	}

	public void generateFieldsFromXML() {
		if (convertedToFields)
			return;
		
		// Get the key value pairs from the contact
		// and loop over each one		

		for (KeyValuePair kvp : getDetails()) {
			String key = kvp.getKey();
			String value = kvp.getValue();

			if(firstNonEmptyField == null)
				firstNonEmptyField = value;			

			if (key.equalsIgnoreCase("Phone")) {
				workPhone = value;
			} else if (key.equalsIgnoreCase("Office")) {
				officeLocation = value;
			} else if (key.equalsIgnoreCase("Title")) {
				title = value;
			} else if (key.equalsIgnoreCase("Company")) {
				company = value;
			} else if (key.equalsIgnoreCase("Alias")) {
				alias = value;
			} else if (key.equalsIgnoreCase("FirstName")) {
				firstName = value;
			} else if (key.equalsIgnoreCase("LastName")) {
				lastName = value;
			} else if (key.equalsIgnoreCase("HomePhone")) {
				homePhone = value;
			} else if (key.equalsIgnoreCase("MobilePhone")) {
				mobilePhone = value;
			} else if (key.equalsIgnoreCase("EmailAddress")) {
				email = value;
			}
		}
		
		convertedToFields = true;
	}
}
