package br.cin.tbookmarks.recommender.database.item;

import java.util.Comparator;

public class ItemInformationComparatorASIN implements Comparator<ItemInformation> {

	@Override
	public int compare(ItemInformation o1, ItemInformation o2) {
		return o1.getAsin().compareTo(o2.getAsin());
	}

}
