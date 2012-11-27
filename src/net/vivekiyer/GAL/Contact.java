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

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Vivek Iyer
 * 
 *         This class parcels up the Contact object so that it can be passed
 *         between two activities without loss of data. It does this by writing
 *         the display name followed by all the contacts details into the parcel
 */
public class Contact implements Parcelable, Comparable<Contact>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1824471215889445550L;

	private ArrayList<KeyValuePair> Details;

	private String DisplayName;

	private String workPhone = ""; //$NON-NLS-1$

	private String officeLocation = ""; //$NON-NLS-1$

	private String title;

	private String company;

	private String alias = ""; //$NON-NLS-1$

	private String firstName;

	private String lastName;

	private String homePhone = ""; //$NON-NLS-1$

	private String mobilePhone = ""; //$NON-NLS-1$

	private String email;
	
	// Field that stores the first non empty field
	private String firstNonEmptyField;
	
	private boolean convertedToFields = false;

	public String getDisplayName() {
		// If the XML did not contain a display name
		// Lets check for the first name and last name
		if(DisplayName == null)
		{			
			DisplayName = ""; //$NON-NLS-1$
	
			generateFieldsFromXML();

			if(firstName != null)
			{
				DisplayName += firstName;
				DisplayName += " "; //$NON-NLS-1$
			}
			
			if(lastName != null)
				DisplayName += lastName;
			
			// If both the first name and last name are empty
			// Use the email address
			if(DisplayName.equalsIgnoreCase("") && email != null) //$NON-NLS-1$
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
		if(title == null)
			for(KeyValuePair kvp : Details)
			{
				if(kvp.getKey().equalsIgnoreCase("title")) //$NON-NLS-1$
				{
					title = kvp.getValue();
					break;
				}
				title = ""; //$NON-NLS-1$
			}
		return title;
	}

	public String getCompany() {
		if(company == null)
			for(KeyValuePair kvp : Details)
			{
				if(kvp.getKey().equalsIgnoreCase("Company")) //$NON-NLS-1$
				{
					company = kvp.getValue();
					break;
				}
				company = ""; //$NON-NLS-1$
			}
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
		if(email == null)
			for(KeyValuePair kvp : Details)
			{
				if(kvp.getKey().equalsIgnoreCase("email")) //$NON-NLS-1$
				{
					email = kvp.getValue();
					break;
				}
				email = ""; //$NON-NLS-1$
			}
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

	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
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

			if (key.equalsIgnoreCase("Phone")) { //$NON-NLS-1$
				workPhone = value;
			} else if (key.equalsIgnoreCase("Office")) { //$NON-NLS-1$
				officeLocation = value;
			} else if (key.equalsIgnoreCase("Title")) { //$NON-NLS-1$
				title = value;
			} else if (key.equalsIgnoreCase("Company")) { //$NON-NLS-1$
				company = value;
			} else if (key.equalsIgnoreCase("Alias")) { //$NON-NLS-1$
				alias = value;
			} else if (key.equalsIgnoreCase("FirstName")) { //$NON-NLS-1$
				firstName = value;
			} else if (key.equalsIgnoreCase("LastName")) { //$NON-NLS-1$
				lastName = value;
			} else if (key.equalsIgnoreCase("HomePhone")) { //$NON-NLS-1$
				homePhone = value;
			} else if (key.equalsIgnoreCase("MobilePhone")) { //$NON-NLS-1$
				mobilePhone = value;
			} else if (key.equalsIgnoreCase("EmailAddress")) { //$NON-NLS-1$
				email = value;
			}
		}
		
		convertedToFields = true;
	}

	@Override
	public int compareTo(Contact another) {
		return(this.getDisplayName().compareTo(another.getDisplayName()));
	}
}
