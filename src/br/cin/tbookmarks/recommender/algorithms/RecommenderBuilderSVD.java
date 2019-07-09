package br.cin.tbookmarks.recommender.algorithms;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

public class RecommenderBuilderSVD implements ContextualRecommenderBuilder {
	
	private int numOfFeatures/* = 10*/;
	private double lambda/* = 0.05*/;
	private int numOfIterations/* = 10*/;
	private DataModel dataModel;
	
	
	public RecommenderBuilderSVD(int numOfFeatures, double lambda,
			int numOfIterations) {
		super();
		this.numOfFeatures = numOfFeatures;
		this.lambda = lambda;
		this.numOfIterations = numOfIterations;
	}
	
	@Override
	public String toString() {
		return "SVD"+"(Feat="+numOfFeatures+", lambda="+lambda+", iterat="+numOfIterations+")";
	}

	@Override
	public Recommender buildRecommender(DataModel dataModel,
			ContextualCriteria criteria, IDRescorer rescorer,
			AbstractDataset dataset) throws TasteException {
		this.dataModel =dataModel;
		
		Recommender recommender = new SVDRecommender(dataModel, new ALSWRFactorizer(dataModel, numOfFeatures, lambda, numOfIterations));
		return new CachingRecommender(recommender);
	}

	@Override
	public DataModel getDataModel() {
		return this.dataModel;
	}
}
