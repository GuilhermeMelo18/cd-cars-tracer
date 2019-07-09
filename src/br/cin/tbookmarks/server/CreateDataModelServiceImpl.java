package br.cin.tbookmarks.server;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.cin.tbookmarks.client.CreateDataModelService;
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
import br.cin.tbookmarks.recommender.database.contextual.DayContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.DayTypeContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.LocationCityContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.LocationCountryContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.LocationStateContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.PeriodOfDayContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.TaskContextualAttribute;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.evaluation.AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain;
import br.cin.tbookmarks.recommender.evaluation.RMSRecommenderEvaluatorCrossDomain;
import br.cin.tbookmarks.recommender.similarity.ItemDomainRescorer;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
//@SuppressWarnings("serial")
public class CreateDataModelServiceImpl extends HttpServlet{
	
	private static final Logger log = LoggerFactory.getLogger(CreateDataModelServiceImpl.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1385952445045847365L;
	
	public static  IDRescorer idrescorer;
	public static  ContextualCriteria criteria;
	public static  ArrayList<ContextualRecommenderBuilder> recommenders;
	public static  AbstractDataset dataset;
	

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if(dataset == null || dataset.getModel() == null) {
		
			recommenders = new ArrayList<ContextualRecommenderBuilder>();
		//    recommenders.add(new RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(475, EuclideanDistanceSimilarity.class));
		//	recommenders.add(new PreFilteringContextualBuildRecommenderByron(new RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(475, EuclideanDistanceSimilarity.class)));
			
			PostFilteringStrategyRecommendation pfStrategy4 = new PostFilteringStrategyRecommendation(PostFilteringStrategyRecommendation.PossibleFilteringStrategies.AT_LEAST_MEDIA_OF_OCCURRENCIES,true,4.0f,(float)2/3,0.5,0.01);
			recommenders.add(new PostFilteringContextualBuildRecommender(new RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(475, EuclideanDistanceSimilarity.class),pfStrategy4));
			
			HashSet<Class<? extends AbstractContextualAttribute>> testedContextualAttributes = new HashSet<Class<? extends AbstractContextualAttribute>>();
	//		testedContextualAttributes.add(LocationCityContextualAttribute.class);
			
	//		testedContextualAttributes.add(DayContextualAttribute.class);
			testedContextualAttributes.add(CompanionContextualAttribute.class);
			
			dataset = AmazonCrossDataset.getInstance();
			
			ItemDomain sourceDomain = ItemDomain.MOVIE;
			
			HashSet<ItemDomain> domainsFilter = new HashSet<ItemDomain>();
			domainsFilter.add(sourceDomain);
			//domainsFilter.add(ItemDomain.MOVIE);
		
			idrescorer = new ItemDomainRescorer(null,domainsFilter, dataset);
			
			criteria = new ContextualCriteria(DayTypeContextualAttribute.WEEKDAY,null,null,null,null,null,null,null);
		
		}
	
	}

}
