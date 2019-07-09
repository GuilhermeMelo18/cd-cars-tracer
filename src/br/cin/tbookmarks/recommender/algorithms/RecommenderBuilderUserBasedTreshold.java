package br.cin.tbookmarks.recommender.algorithms;

import java.lang.reflect.InvocationTargetException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

public class RecommenderBuilderUserBasedTreshold implements ContextualRecommenderBuilder {

	private double threshold/* = 0.5*/;
	private Class<? extends UserSimilarity> userSimilarity;
	private DataModel dataModel;
	
	public RecommenderBuilderUserBasedTreshold(double threshold, Class<? extends UserSimilarity> userSim) {
		this.threshold = threshold;
		this.userSimilarity = userSim;
	}
	
		
	@Override
	public String toString() {
		return "Threshold_UserBased"+"(Thrs="+threshold+")";
	}

	@Override
	public Recommender buildRecommender(DataModel dataModel,
			ContextualCriteria criteria, IDRescorer rescorer,
			AbstractDataset dataset) throws TasteException {
		
		this.dataModel = dataModel;
		
		UserSimilarity similarity;
		try {
			similarity = this.userSimilarity.getDeclaredConstructor(DataModel.class).newInstance(dataModel);
			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(this.threshold,
					similarity, dataModel);
			Recommender recommender =  new GenericUserBasedRecommender(
					dataModel, neighborhood, similarity);
			return new CachingRecommender(recommender); 
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public DataModel getDataModel() {
		return this.dataModel;
	}
}
