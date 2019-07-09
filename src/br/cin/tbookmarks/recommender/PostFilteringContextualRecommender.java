package br.cin.tbookmarks.recommender;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import com.google.common.base.Preconditions;


public class PostFilteringContextualRecommender implements Recommender{
	
	private ContextualCriteria contextualAttributes;
	private Recommender delegated;
	private PostFilteringStrategyRecommendation postFilteringStrategy;
	private AbstractDataset dataset;
	
	public PostFilteringContextualRecommender(Recommender recommender,ContextualCriteria contextualAttributes, PostFilteringStrategyRecommendation postFilteringStrategy, AbstractDataset dataset) {
		this.delegated = recommender;
		this.contextualAttributes = contextualAttributes;
		this.postFilteringStrategy = postFilteringStrategy;
		this.dataset = dataset;
	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		this.delegated.refresh(alreadyRefreshed);
		
	}

	@Override
	public List<RecommendedItem> recommend(long userID, int howMany)
			throws TasteException {

		    Preconditions.checkArgument(howMany >= 1, "howMany must be at least 1");
		    
		    int doubleHowMany = 100;
		    List<RecommendedItem> postFRecommendedItems = new ArrayList<RecommendedItem>();
		    
		    List<RecommendedItem> recommendedItems;

		    do{
		    	
		    	doubleHowMany = doubleHowMany*2;
		    	
		    	recommendedItems = this.delegated.recommend(userID, doubleHowMany);
		    	
		    	int index = 0;
		    	
		    	while(postFRecommendedItems.size() < howMany && index < recommendedItems.size()){
		    		if(!Float.isNaN(this.estimatePreference(userID, recommendedItems.get(index).getItemID())) &&
		    				!postFRecommendedItems.contains(recommendedItems.get(index))){
		    			postFRecommendedItems.add(recommendedItems.get(index));
		    		}
		    		index++;
		    	}
		    	
		    }while(recommendedItems.size() == doubleHowMany && postFRecommendedItems.size() < howMany);
		    
		    return postFRecommendedItems;
		  
	}

	@Override
	public List<RecommendedItem> recommend(long userID, int howMany,
			IDRescorer rescorer) throws TasteException {
	    Preconditions.checkArgument(howMany >= 1, "howMany must be at least 1");
	    
	    int doubleHowMany = 100;
	    List<RecommendedItem> postFRecommendedItems = new ArrayList<RecommendedItem>();
	    
	    List<RecommendedItem> recommendedItems;

	    do{
	    	
	    	doubleHowMany = doubleHowMany*2;
	    	
	    	recommendedItems = this.delegated.recommend(userID, doubleHowMany, rescorer);
	    	
	    	int index = 0;
	    	
	    	while(postFRecommendedItems.size() < howMany && index < recommendedItems.size()){
	    		if(!Float.isNaN(this.estimatePreference(userID, recommendedItems.get(index).getItemID())) &&
	    				!postFRecommendedItems.contains(recommendedItems.get(index))){
	    			postFRecommendedItems.add(recommendedItems.get(index));
	    		}
	    		index++;
	    	}
	    	
	    }while(recommendedItems.size() == doubleHowMany && postFRecommendedItems.size() < howMany);
	    
	    return postFRecommendedItems;
	  }

	@Override
	public float estimatePreference(long userID, long itemID)
			throws TasteException {
		
		return postFilteringStrategy.filterOrAdjustPreference(userID,itemID,contextualAttributes,this.delegated,this.dataset);
		
	}

	@Override
	public void setPreference(long userID, long itemID, float value)
			throws TasteException {
		this.delegated.setPreference(userID, itemID, value);
		
	}

	@Override
	public void removePreference(long userID, long itemID)
			throws TasteException {
		this.delegated.removePreference(userID, itemID);
		
	}

	@Override
	public DataModel getDataModel() {
		return this.delegated.getDataModel();
	}
	
}
