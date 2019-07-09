package br.cin.tbookmarks.recommender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import com.google.common.base.Preconditions;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.evaluation.PredictionValues;
import tracer.TracerRecommender;

public class PostFilteringContextualRecommenderTracer implements Recommender{
	
	private ContextualCriteria contextualAttributes;
	private GenericUserBasedRecommenderTracer delegated;
	private PostFilteringStrategyRecommendation postFilteringStrategy;
	private AbstractDataset dataset;
	
	

	/* Global Variables*/
	private double MAEValidate;
	private double RMSEValidate;
	private double MAE;
	private double RMSE;
	private double MAEPosFilter;
	private double RMSEPosFilter;
	
	private ArrayList<PredictionValues> values;
	private ArrayList<PredictionValues> valuesPosFilter;
		
	private int totalOfTrainingRatingsFromSource;
	private int totalOfTrainingRatingsFromTarget;
	private int totalOfTestRatings;
	private  int NotCounterAvaliateTestRatings;
	
	

	public double getMAEValidate() {
		return MAEValidate;
	}

	public void setMAEValidate(double mAEValidate) {
		MAEValidate = mAEValidate;
	}
	
	

	public int getNotCounterAvaliateTestRatings() {
		return NotCounterAvaliateTestRatings;
	}

	public void setNotCounterAvaliateTestRatings(int notCounterAvaliateTestRatings) {
		NotCounterAvaliateTestRatings = notCounterAvaliateTestRatings;
	}

	public double getRMSEValidate() {
		return RMSEValidate;
	}

	public void setRMSEValidate(double rMSEValidate) {
		RMSEValidate = rMSEValidate;
	}

	public double getMAE() {
		return MAE;
	}

	public void setMAE(double mAE) {
		MAE = mAE;
	}

	public double getRMSE() {
		return RMSE;
	}

	public void setRMSE(double rMSE) {
		RMSE = rMSE;
	}

	public double getMAEPosFilter() {
		return MAEPosFilter;
	}

	public void setMAEPosFilter(double mAEPosFilter) {
		MAEPosFilter = mAEPosFilter;
	}

	public double getRMSEPosFilter() {
		return RMSEPosFilter;
	}

	public void setRMSEPosFilter(double rMSEPosFilter) {
		RMSEPosFilter = rMSEPosFilter;
	}

	public ArrayList<PredictionValues> getValues() {
		return values;
	}

	public void setValues(ArrayList<PredictionValues> values) {
		this.values = values;
	}

	public ArrayList<PredictionValues> getValuesPosFilter() {
		return valuesPosFilter;
	}

	public void setValuesPosFilter(ArrayList<PredictionValues> valuesPosFilter) {
		this.valuesPosFilter = valuesPosFilter;
	}

	public int getTotalOfTrainingRatingsFromSource() {
		return totalOfTrainingRatingsFromSource;
	}

	public void setTotalOfTrainingRatingsFromSource(int totalOfTrainingRatingsFromSource) {
		this.totalOfTrainingRatingsFromSource = totalOfTrainingRatingsFromSource;
	}

	public int getTotalOfTrainingRatingsFromTarget() {
		return totalOfTrainingRatingsFromTarget;
	}

	public void setTotalOfTrainingRatingsFromTarget(int totalOfTrainingRatingsFromTarget) {
		this.totalOfTrainingRatingsFromTarget = totalOfTrainingRatingsFromTarget;
	}

	public int getTotalOfTestRatings() {
		return totalOfTestRatings;
	}

	public void setTotalOfTestRatings(int totalOfTestRatings) {
		this.totalOfTestRatings = totalOfTestRatings;
	}

	public PostFilteringContextualRecommenderTracer(GenericUserBasedRecommenderTracer recommender,ContextualCriteria contextualAttributes, PostFilteringStrategyRecommendation postFilteringStrategy, AbstractDataset dataset) {
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
		
		this.delegated.runTracerRecommender();
		
		ArrayList<PredictionValues> listPredictions = this.delegated.getValues();
		
		for (PredictionValues predictionValues : listPredictions) {
			
			postFilteringStrategy.filterOrAdjustPreference(predictionValues.getUserID(), predictionValues.getItemID() ,contextualAttributes,this.delegated,this.dataset);
			
		}
		
		// Set Informations After Pós Filter
		setInformationPosFilter();
		
		return 0;
	}
	
	private void setInformationPosFilter() {
		
		this.setMAE(this.delegated.getMAE());
		this.setMAEValidate(this.delegated.getMAEValidate());
		this.setMAEPosFilter(this.delegated.getAverageMAEPosFilter());
		
		this.setRMSE(this.delegated.getRMSE());
		this.setRMSEValidate(this.delegated.getRMSEValidate());
		this.setRMSEPosFilter(this.delegated.getAverageRMSEPosFilter());
		
		this.setValues(this.delegated.getValues());
		this.setValuesPosFilter(this.delegated.getValuesPosFilter());
		
		this.setTotalOfTestRatings(this.delegated.getTotalOfTestRatings());
		this.setTotalOfTrainingRatingsFromSource(this.delegated.getTotalOfTrainingRatingsFromSource());
		this.setTotalOfTrainingRatingsFromTarget(this.delegated.getTotalOfTrainingRatingsFromTarget());
		this.setNotCounterAvaliateTestRatings(this.delegated.getNotCounterAvaliateTestRatings());
		
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
