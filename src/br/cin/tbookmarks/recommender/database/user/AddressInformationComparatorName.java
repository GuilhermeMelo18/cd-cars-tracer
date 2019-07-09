package br.cin.tbookmarks.recommender.database.user;

import java.util.Comparator;

public class AddressInformationComparatorName implements Comparator<AddressInformation> {

	@Override
	public int compare(AddressInformation o1, AddressInformation o2) {
		return o1.getUserAddress().compareTo(o2.getUserAddress());
	}

}
