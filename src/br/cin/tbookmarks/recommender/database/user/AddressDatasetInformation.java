package br.cin.tbookmarks.recommender.database.user;

import java.util.ArrayList;
import java.util.List;


public class AddressDatasetInformation {
	List<AddressInformation> addresses;
	
	public AddressDatasetInformation() {
		addresses = new ArrayList<AddressInformation>();
	}

	public List<AddressInformation> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<AddressInformation> addresses) {
		this.addresses = addresses;
	}
}
