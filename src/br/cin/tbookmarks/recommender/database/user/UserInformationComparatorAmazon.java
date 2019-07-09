package br.cin.tbookmarks.recommender.database.user;

import java.util.Comparator;

public class UserInformationComparatorAmazon implements Comparator<UserInformation> {

	@Override
	public int compare(UserInformation o1, UserInformation o2) {
		return o1.getAmazonID().compareTo(o2.getAmazonID());
	}

}
