package br.cin.tbookmarks.recommender.similarity;

import java.util.Collection;
import java.util.Set;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.item.ItemCategory;

public class ItemCategoryItemSimilarity implements ItemSimilarity {

	DataModel dataModel;
	AbstractDataset dataSet;

	public ItemCategoryItemSimilarity(DataModel dataModel,
			AbstractDataset dataSet) {
		this.dataModel = dataModel;
		this.dataSet = dataSet;
	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		// TODO Auto-generated method stub

	}

	private boolean verifyAtleastOneCategoryInside(Set<ItemCategory> item1,
			Set<ItemCategory> item2) {
		for (ItemCategory itemCategory : item1) {
			if (item2.contains(itemCategory)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public double itemSimilarity(long itemID1, long itemID2)
			throws TasteException {
		boolean test = this.verifyAtleastOneCategoryInside(this.dataSet
				.getItemInformationByID(itemID1).getCategories(),
				this.dataSet.getItemInformationByID(itemID2)
						.getCategories());

		return test ? 1.0 : 0.0;
	}

	@Override
	public double[] itemSimilarities(long itemID1, long[] itemID2s)
			throws TasteException {

		double[] result = new double[itemID2s.length];
		for (int i = 0; i < itemID2s.length; i++) {
			result[i] = itemSimilarity(itemID1, itemID2s[i]);
		}
		return result;
	}

	@Override
	public long[] allSimilarItemIDs(long itemID) throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

}
