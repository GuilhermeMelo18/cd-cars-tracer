package br.cin.tbookmarks.recommender.database.item;

import java.util.Comparator;

public class ItemInformationComparatorID implements Comparator<ItemInformation> {

	@Override
	public int compare(ItemInformation o1, ItemInformation o2) {
		int res = 0;
		if (o1.getId() < o2.getId())
			res = -1;
		if (o1.getId() > o2.getId())
			res = 1;
		return res;
	}

}
