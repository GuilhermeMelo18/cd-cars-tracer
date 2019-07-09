package br.cin.tbookmarks.recommender.algorithms;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

public interface ContextualRecommenderBuilder{
	  
	  /**
	   * <p>
	   * Builds a {@link Recommender} implementation to be evaluated, using the given {@link DataModel}.
	   * </p>
	   * 
	   * @param dataModel
	   *          {@link DataModel} to build the {@link Recommender} on
	   * @return {@link Recommender} based upon the given {@link DataModel}
	   * @throws TasteException
	   *           if an error occurs while accessing the {@link DataModel}
	   */
	  //Recommender buildRecommender(DataModel dataModel) throws TasteException;
	  
	  Recommender buildRecommender(DataModel dataModel, ContextualCriteria criteria, IDRescorer rescorer, AbstractDataset dataset) throws TasteException;
	  
	  DataModel getDataModel();
	  
	}
