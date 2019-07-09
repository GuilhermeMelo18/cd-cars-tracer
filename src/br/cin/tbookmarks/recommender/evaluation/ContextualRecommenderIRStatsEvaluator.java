package br.cin.tbookmarks.recommender.evaluation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;

import br.cin.tbookmarks.recommender.algorithms.ContextualRecommenderBuilder;

public interface ContextualRecommenderIRStatsEvaluator {
	  
	  /**
	   * @param recommenderBuilder
	   *          object that can build a {@link org.apache.mahout.cf.taste.recommender.Recommender} to test
	   * @param dataModelBuilder
	   *          {@link DataModelBuilder} to use, or if null, a default {@link DataModel} implementation will be
	   *          used
	   * @param dataModel
	   *          dataset to test on
	   * @param rescorer
	   *          if any, to use when computing recommendations
	   * @param at
	   *          as in, "precision at 5". The number of recommendations to consider when evaluating precision,
	   *          etc.
	   * @param relevanceThreshold
	   *          items whose preference value is at least this value are considered "relevant" for the purposes
	   *          of computations
	   * @return {@link IRStatistics} with resulting precision, recall, etc.
	   * @throws TasteException
	   *           if an error occurs while accessing the {@link DataModel}
	   */
	  IRStatistics evaluate(ContextualRecommenderBuilder recommenderBuilder,
	                        DataModelBuilder dataModelBuilder,
	                        DataModel dataModel,
	                        IDRescorer rescorer,
	                        int at,
	                        double relevanceThreshold,
	                        double evaluationPercentage) throws TasteException;
	  
	}
