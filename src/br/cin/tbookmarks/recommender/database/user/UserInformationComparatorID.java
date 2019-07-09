package br.cin.tbookmarks.recommender.database.user;

import java.util.Comparator;


public class UserInformationComparatorID implements Comparator<UserInformation> {

	@Override
	public int compare(UserInformation o1, UserInformation o2) {
		int res = 0;
		if (o1.getId() < o2.getId())
			res = -1;
		if (o1.getId() > o2.getId())
			res = 1;
		return res;
	}

}
