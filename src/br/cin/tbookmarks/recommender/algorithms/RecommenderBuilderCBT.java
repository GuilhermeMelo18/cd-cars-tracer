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

public class RecommenderBuilderCBT implements ContextualRecommenderBuilder {
	
	private int k,l/* = 10*/;
	//private double lambda/* = 0.05*/;
	private int numOfIterations/* = 10*/;
	private DataModel dataModel;
	
	
	
	public RecommenderBuilderCBT(int k, int l, int numOfIterations) {
		super();
		this.k = k;
		this.l = l;
		this.numOfIterations = numOfIterations;
	}

	@Override
	public String toString() {
		return "SVD"+"(K="+k+", L="+l+", iterat="+numOfIterations+")";
	}

	@Override
	public Recommender buildRecommender(DataModel dataModel,
			ContextualCriteria criteria, IDRescorer rescorer,
			AbstractDataset dataset) throws TasteException {
		
		this.dataModel = dataModel;
		
		Recommender recommender = new SVDRecommender(dataModel, new CodeBookGenerator(dataModel, k, l, numOfIterations));
		return new CachingRecommender(recommender);
	}

	@Override
	public DataModel getDataModel() {
		return this.dataModel;
	}
}
