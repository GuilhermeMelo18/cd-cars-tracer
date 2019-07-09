package br.cin.tbookmarks.recommender.algorithms;

import java.lang.reflect.InvocationTargetException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

public class RecommenderBuilderUserBasedNearestNeighbor implements
ContextualRecommenderBuilder {

	private int neiborSize/* = 475*/;
	private Class<? extends UserSimilarity> userSimilarity;
	private DataModel dataModel;
	
	public RecommenderBuilderUserBasedNearestNeighbor(int neiborSize, Class<? extends UserSimilarity> userSim) {
		this.neiborSize = neiborSize;
		this.userSimilarity = userSim;
	}
		
	@Override
	public String toString() {
		return "NearestNeighbor_UserBased"+"(N="+neiborSize+")";
	}

	@Override
	public Recommender buildRecommender(DataModel dataModel,
			ContextualCriteria criteria, IDRescorer rescorer,
			AbstractDataset dataset) throws TasteException {
		
		this.dataModel = dataModel;
		
		UserSimilarity similarity = null;
		try {
			similarity = this.userSimilarity.getDeclaredConstructor(DataModel.class).newInstance(dataModel);
			
			UserNeighborhood neighborhood = new NearestNUserNeighborhood(this.neiborSize,
					similarity, dataModel);
			Recommender recommender = new GenericUserBasedRecommender(dataModel,
					neighborhood, similarity);
			CachingRecommender cr = new CachingRecommender(recommender);
			return cr;
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public DataModel getDataModel() {
		return this.dataModel;
	}
}
