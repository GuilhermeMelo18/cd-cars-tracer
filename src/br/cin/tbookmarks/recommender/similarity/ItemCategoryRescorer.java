package br.cin.tbookmarks.recommender.similarity;

import org.apache.mahout.cf.taste.recommender.IDRescorer;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.item.ItemCategory;
import br.cin.tbookmarks.recommender.database.item.ItemInformation;

public class ItemCategoryRescorer implements IDRescorer {

	private final ItemCategory itemCategoryCriteria;
	private final ItemCategory itemCategoryCriteriaExclusion;
	private final AbstractDataset dataset;

	public ItemCategoryRescorer(ItemCategory itemCategory, ItemCategory itemCategoryCriteriaExclusion, AbstractDataset dataset) {
		this.itemCategoryCriteria = itemCategory;
		this.dataset = dataset;
		this.itemCategoryCriteriaExclusion = itemCategoryCriteriaExclusion;
	}

	@Override
	public double rescore(long id, double originalScore) {
		ItemInformation itemInfo = this.dataset.getItemInformationByID(id);
		for(ItemCategory itemCategory : itemInfo.getCategories()) {
			if(itemCategory.equals(this.itemCategoryCriteria)){
				return originalScore * 2;
			}
			
		}
		return originalScore;
	}

	@Override
	public boolean isFiltered(long id) {
		ItemInformation itemInfo = this.dataset.getItemInformationByID(id);
		for(ItemCategory itemCategory : itemInfo.getCategories()) {
			if(itemCategory.equals(this.itemCategoryCriteriaExclusion)){
				return true;
			}
			
		}
		return false;
	}

}
