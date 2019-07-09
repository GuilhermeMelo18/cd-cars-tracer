package br.cin.tbookmarks.recommender.algorithms;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;

import br.cin.tbookmarks.recommender.PostFilteringContextualRecommender;
import br.cin.tbookmarks.recommender.PostFilteringStrategyRecommendation;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

public class PostFilteringContextualBuildRecommender implements
ContextualRecommenderBuilder {

	private ContextualCriteria contextualAttributes;
	private ContextualRecommenderBuilder recommenderBuilder;
	private PostFilteringStrategyRecommendation postFilteringStrategyRecommendation;
	private AbstractDataset dataset;
	private DataModel dataModel;
	
	public PostFilteringContextualBuildRecommender(ContextualRecommenderBuilder recommenderBuilder, 
													PostFilteringStrategyRecommendation postFilteringStrategyRecommendation) {
		//this.contextualAttributes = contexutalAttributes;
		this.recommenderBuilder = recommenderBuilder;
		this.postFilteringStrategyRecommendation = postFilteringStrategyRecommendation;
		//this.dataset = dataset;
	}
	
	private void setContextAndDataset(ContextualCriteria contexutalAttributes, AbstractDataset dataset) {
		this.contextualAttributes = contexutalAttributes;
		this.dataset = dataset;

	}
			
		
	@Override
	public String toString() {
		return "PostF"+"(CF-based="+recommenderBuilder+", strategy: "+postFilteringStrategyRecommendation.getPostFilteringStrategy()+"[onlyGoodRatings: "+postFilteringStrategyRecommendation.isOnlyWithGoodRatings()+", minimal: "+postFilteringStrategyRecommendation.getGoodRatingMin() +"])";
	}

	@Override
	public Recommender buildRecommender(DataModel dataModel,
			ContextualCriteria criteria, IDRescorer rescorer, AbstractDataset dataset)
			throws TasteException {
		
		this.dataModel = dataModel;
		
		setContextAndDataset(criteria,dataset);
		
		if(contextualAttributes == null || dataset == null){
			throw new TasteException("Context and/or dataset unset in PostF");
		}
		
		//if(model instanceof ContextualDataModel){
			return new PostFilteringContextualRecommender(this.recommenderBuilder.buildRecommender(dataModel, criteria, rescorer, dataset),contextualAttributes, this.postFilteringStrategyRecommendation,this.dataset);
		
		//return this.buildRecommender(dataModel);
	}

	@Override
	public DataModel getDataModel() {
		return this.dataModel;
	}
	
}
