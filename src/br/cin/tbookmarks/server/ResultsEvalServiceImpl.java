package br.cin.tbookmarks.server;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.cin.tbookmarks.client.Result;
import br.cin.tbookmarks.client.ResultsEvalService;
import br.cin.tbookmarks.client.TBookmarksRS;
import br.cin.tbookmarks.recommender.EvaluateRecommender;
import br.cin.tbookmarks.recommender.PostFilteringStrategyRecommendation;
import br.cin.tbookmarks.recommender.algorithms.ContextualRecommenderBuilder;
import br.cin.tbookmarks.recommender.algorithms.PostFilteringContextualBuildRecommender;
import br.cin.tbookmarks.recommender.algorithms.PreFilteringContextualBuildRecommender;
import br.cin.tbookmarks.recommender.algorithms.PreFilteringContextualBuildRecommenderByron;
import br.cin.tbookmarks.recommender.algorithms.RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.AmazonCrossDataset;
import br.cin.tbookmarks.recommender.database.contextual.AbstractContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.CompanionContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.contextual.DayTypeContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.PeriodOfDayContextualAttribute;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.evaluation.AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain;
import br.cin.tbookmarks.recommender.evaluation.RMSRecommenderEvaluatorCrossDomain;
import br.cin.tbookmarks.recommender.similarity.ItemDomainRescorer;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
//@SuppressWarnings("serial")
public class ResultsEvalServiceImpl extends RemoteServiceServlet implements
		ResultsEvalService{
	
	private static final Logger log = LoggerFactory.getLogger(ResultsEvalServiceImpl.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1385952445045847365L;
	

	
	public static String recommendByAlgorithm(long userId, int numberOfRecommendations,
			ArrayList<ContextualRecommenderBuilder> rbs, AbstractDataset dataset, IDRescorer idrescorer, ContextualCriteria contextualCriteria) throws TasteException {
		
		StringBuffer returned = new StringBuffer();
		
		for(ContextualRecommenderBuilder rb : rbs){
			List<RecommendedItem> recommendedItems;
			/*if(rb instanceof PreFilteringContextualBuildRecommender){
				
				DataModel filteredDM = ((PreFilteringContextualBuildRecommender) rb).preFilterDataModel(dataset.getModel(),contextualCriteria);
				
				recommendedItems = rb.buildRecommender(filteredDM).recommend(
						userId, numberOfRecommendations,idrescorer);
			}else if(rb instanceof PostFilteringContextualBuildRecommender){
				((PostFilteringContextualBuildRecommender) rb).setContextAndDataset(contextualCriteria, dataset);
				recommendedItems = rb.buildRecommender(dataset.getModel()).recommend(
						userId, numberOfRecommendations,idrescorer);
			}else{*/
			
			
				recommendedItems = rb.buildRecommender(dataset.getModel(), contextualCriteria, idrescorer, dataset).recommend(
					userId, numberOfRecommendations,idrescorer);
			//}
				
				
				
			StringBuffer sbAux = new StringBuffer();
			
			sbAux.append("\nAlgorithm:"+rb+">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			int position = 1;
			for (RecommendedItem recommendation : recommendedItems) {				
				sbAux.append(position + ": " + recommendation.getItemID()
						+ " - "
						+ dataset.getItemInformationByID(recommendation.getItemID()).getName()
						+ " - " + recommendation.getValue()
						+ " - " + dataset.getItemInformationByID(recommendation.getItemID()).getCategories()
						+ " - " + dataset.getItemInformationByID(recommendation.getItemID()).getItemDomain());
				position++;
			}
			log.info(sbAux.toString()+"\n");
			returned.append(sbAux.toString()+"\n");
			
		}

		return returned.toString();
	}

	@Override
	public String getResultsEval(String userID) throws Exception {
		
		 
		/*if(CreateDataModelServiceImpl.dataset == null || CreateDataModelServiceImpl.dataset.getModel() == null) {
			 Queue queue = QueueFactory.getDefaultQueue();
			 queue.add(TaskOptions.Builder.withUrl("/tbookmarksrs/createDataModelService"));
			 
			return "Wait for initializing the dataset model";
		}*/
		
		try {
			CreateDataModelServiceImpl service = new CreateDataModelServiceImpl();
			service.service(null, null);
			
			
			return recommendByAlgorithm(new Long(userID), 5, CreateDataModelServiceImpl.recommenders, CreateDataModelServiceImpl.dataset, CreateDataModelServiceImpl.idrescorer, CreateDataModelServiceImpl.criteria);
		} catch (TasteException e) {
			return e.getMessage();
		}
		
		

	}

}
