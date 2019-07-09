package br.cin.tbookmarks.recommender.similarity;

import java.util.HashSet;

import org.apache.mahout.cf.taste.recommender.IDRescorer;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.database.item.ItemInformation;

public class ItemDomainRescorer implements IDRescorer {

	private final HashSet<ItemDomain> itemDomainCriteria;
	private final HashSet<ItemDomain> itemDomainCriteriaExclusion;
	private final AbstractDataset dataset;

	public ItemDomainRescorer(HashSet<ItemDomain> itemDomainCriteria, HashSet<ItemDomain> itemDomainCriteriaExclusion, AbstractDataset dataset) {
		this.itemDomainCriteria = itemDomainCriteria;
		this.dataset = dataset;
		this.itemDomainCriteriaExclusion = itemDomainCriteriaExclusion;
	}

	@Override
	public double rescore(long id, double originalScore) {
		ItemInformation itemInfo = this.dataset.getItemInformationByID(id);
		if(this.itemDomainCriteria != null){
			for (ItemDomain itemDomain : this.itemDomainCriteria) {
				if (itemInfo.getItemDomain().equals(itemDomain)) {
					return originalScore * 2;
				}
	
			}
		}
		return originalScore;
	}

	@Override
	public boolean isFiltered(long id) {
		ItemInformation itemInfo = this.dataset.getItemInformationByID(id);
		if(this.itemDomainCriteriaExclusion != null){
			for (ItemDomain itemDomain : this.itemDomainCriteriaExclusion) {
				if (itemInfo.getItemDomain().equals(itemDomain)) {
					return true;
				}
	
			}
		}
		return false;
	}

}
