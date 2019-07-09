package br.cin.tbookmarks.recommender.algorithms;

import java.lang.reflect.InvocationTargetException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

public class RecommenderBuilderItemBased implements ContextualRecommenderBuilder {

	private Class<? extends ItemSimilarity> itemSimilarity;
	private DataModel dataModel;
	
	public RecommenderBuilderItemBased(Class<? extends ItemSimilarity> itemSimilarity) {
		this.itemSimilarity = itemSimilarity;
	}
	

	@Override
	public String toString() {
		return "CF-ItemBased";
	}

	@Override
	public Recommender buildRecommender(DataModel model,
			ContextualCriteria criteria, IDRescorer rescorer,
			AbstractDataset dataset) throws TasteException {
		
		this.dataModel = model;
		
		ItemSimilarity similarity;
		try {
			similarity = this.itemSimilarity.getDeclaredConstructor(DataModel.class).newInstance(model);
			Recommender recommender = new GenericItemBasedRecommender(model, similarity);
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
