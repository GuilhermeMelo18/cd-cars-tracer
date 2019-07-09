package br.cin.tbookmarks.recommender;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.recommender.AbstractCandidateItemsStrategy;
import org.apache.mahout.cf.taste.model.DataModel;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.item.ItemInformation;

public class PreferredItemsContentFilteringCandidateItemsStrategy extends
		AbstractCandidateItemsStrategy {

	private AbstractDataset dataset;
	
	public PreferredItemsContentFilteringCandidateItemsStrategy(AbstractDataset dataset) {
		this.dataset = dataset;
	}
	
	private FastIDSet getIDsFromAllItensDataset() {
		
	    FastIDSet result = new FastIDSet();

		for (ItemInformation itemInfo : this.dataset
				.getItemDatasetInformation().getItens()) {
			result.add(itemInfo.getId());
		}
		
		return result;
	}
	
	@Override
	protected FastIDSet doGetCandidateItems(long[] preferredItemIDs,
			DataModel dataModel) throws TasteException {
		 FastIDSet possibleItemsIDs = new FastIDSet();
		    /*for (long itemID : preferredItemIDs) {
		      PreferenceArray itemPreferences = dataModel.getPreferencesForItem(itemID);
		      int numUsersPreferringItem = itemPreferences.length();
		      for (int index = 0; index < numUsersPreferringItem; index++) {
		        possibleItemsIDs.addAll(dataModel.getItemIDsFromUser(itemPreferences.getUserID(index)));
		      }
		    }*/
		 	possibleItemsIDs.addAll(getIDsFromAllItensDataset());
		    possibleItemsIDs.removeAll(preferredItemIDs);
		    return possibleItemsIDs;
	}

}
