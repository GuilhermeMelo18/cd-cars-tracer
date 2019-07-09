package br.cin.tbookmarks.recommender.algorithms;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;

import br.cin.tbookmarks.recommender.GenericUserBasedRecommenderTracer;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

public class RecommenderBuilderUserBasedTracer implements ContextualRecommenderBuilder {
	
	private DataModel dataModel;
	
	private double clusterTransferUser;
	private double clusterTransferItem;
	private double trainingIterations;
	private double trainingPercent;
	private double alpha;
	private double beta;
	
	
	
	
	public RecommenderBuilderUserBasedTracer(double clusterTransferUser, double clusterTransferItem, double alpha, double beta, double trainingIterations, double trainingPercent) {
		
		this.clusterTransferUser = clusterTransferUser;
		this.clusterTransferItem = clusterTransferItem;
		this.trainingIterations = trainingIterations;
		this.trainingPercent = trainingPercent;
		this.alpha = alpha;
		this.beta = beta;
		
	}
	

	public Recommender buildRecommender(DataModel dataModel, ContextualCriteria criteria, IDRescorer rescorer,
			AbstractDataset dataset) throws TasteException {
		
		Recommender recommender = new GenericUserBasedRecommenderTracer(this.clusterTransferUser, this.clusterTransferItem,
				this.alpha, this.beta, this.trainingIterations, this.trainingPercent, dataModel,  dataset);
			

		return recommender;
	}


	@Override
	public DataModel getDataModel() {
		return this.dataModel;
	}
	
}
