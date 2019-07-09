package br.cin.tbookmarks.recommender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.model.AbstractDataModel;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import br.cin.tbookmarks.recommender.algorithms.ContextualRecommenderBuilder;
import br.cin.tbookmarks.recommender.algorithms.PostFilteringContextualBuildRecommender;
import br.cin.tbookmarks.recommender.algorithms.PreFilteringContextualBuildRecommender;
import br.cin.tbookmarks.recommender.algorithms.PreFilteringContextualBuildRecommenderByron;
import br.cin.tbookmarks.recommender.algorithms.RecommenderBuilderUserBasedNearestNeighbor;
import br.cin.tbookmarks.recommender.algorithms.RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo;
import br.cin.tbookmarks.recommender.algorithms.Recommenders;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.AmazonCrossDataset;
import br.cin.tbookmarks.recommender.database.contextual.AbstractContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.CompanionContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.contextual.DayContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.DayTypeContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.PeriodOfDayContextualAttribute;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.similarity.ItemDomainRescorer;

public class SampleRecommender {

	public static String recommendByAlgorithm(long userId, int numberOfRecommendations,
			ArrayList<ContextualRecommenderBuilder> rbs, AbstractDataset dataset, IDRescorer idrescorer, ContextualCriteria contextualCriteria) throws TasteException {
		
		StringBuffer sb = new StringBuffer();
		
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
			
			sbAux.append("\nAlgorithm:"+rb+">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
			int position = 1;
			for (RecommendedItem recommendation : recommendedItems) {				
				sbAux.append(position + ": " + recommendation.getItemID()
						+ " - "
						+ dataset.getItemInformationByID(recommendation.getItemID()).getName()
						+ " - " + recommendation.getValue()
						+ " - " + dataset.getItemInformationByID(recommendation.getItemID()).getCategories()
						+ " - " + dataset.getItemInformationByID(recommendation.getItemID()).getItemDomain()+"\n");
				position++;
			}
			System.out.println(sbAux.toString()+"\n");
			sb.append(sbAux);
		}
		return sb.toString();
	}


	public static void main(String[] args) {
		
		ArrayList<ContextualRecommenderBuilder> recommenders = new ArrayList<ContextualRecommenderBuilder>();
//	    recommenders.add(new RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(475, EuclideanDistanceSimilarity.class));
		recommenders.add(new PreFilteringContextualBuildRecommenderByron(new RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(475, EuclideanDistanceSimilarity.class)));
		
		PostFilteringStrategyRecommendation pfStrategy4 = new PostFilteringStrategyRecommendation(PostFilteringStrategyRecommendation.PossibleFilteringStrategies.AT_LEAST_MEDIA_OF_OCCURRENCIES,true,4.0f,(float)2/3,0.5,0.01);
//		recommenders.add(new PostFilteringContextualBuildRecommender(new RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(475, EuclideanDistanceSimilarity.class),pfStrategy4));
		
		HashSet<Class<? extends AbstractContextualAttribute>> testedContextualAttributes = new HashSet<Class<? extends AbstractContextualAttribute>>();
//		testedContextualAttributes.add(LocationCityContextualAttribute.class);
		
//		testedContextualAttributes.add(DayContextualAttribute.class);
		testedContextualAttributes.add(CompanionContextualAttribute.class);
		
		AbstractDataset dataset = AmazonCrossDataset.getInstance();
		
		ItemDomain sourceDomain = ItemDomain.BOOK;
		
		HashSet<ItemDomain> domainsFilter = new HashSet<ItemDomain>();
		domainsFilter.add(sourceDomain);
		//domainsFilter.add(ItemDomain.MOVIE);
	
		IDRescorer idrescorer = new ItemDomainRescorer(null,domainsFilter, dataset);
		
		ContextualCriteria criteria = new ContextualCriteria(null,null,null,null,null,CompanionContextualAttribute.ACCOMPANIED,CompanionContextualAttribute.COUPLE,null);
		
		try {
			recommendByAlgorithm(2017034, 20, recommenders, dataset, idrescorer, criteria);
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*HashSet<ItemDomain> domainsDataset = new HashSet<ItemDomain>();
		domainsDataset.add(ItemDomain.BOOK);
		domainsDataset.add(ItemDomain.MOVIE);
		//domainsFilter.add(ItemDomain.BOOK);
		
		AbstractDataset absDataset = AmazonCrossDataset.getInstance();	
		
		HashSet<ItemDomain> domainsFilter = new HashSet<ItemDomain>();
		domainsFilter.add(ItemDomain.BOOK);
		
		IDRescorer idrescorer = new ItemDomainRescorer(null,domainsFilter, absDataset);
		
		//ContextualCriteria criteria = new ContextualCriteria(DayTypeContextualAttribute.WEEKEND,PeriodOfDayContextualAttribute.DAWN);
		ContextualCriteria criteria = new ContextualCriteria(DayTypeContextualAttribute.WEEKEND,null);
		
		SampleRecommender sr = new SampleRecommender(absDataset, idrescorer,criteria);
		

		ArrayList<RecommenderBuilder> list = sr.recommenders
				.getRecommenderBuilders();

		try {
			int userId =41; //6041

			for (RecommenderBuilder recommenderBuilder : list) {
				System.out.println("\n"+recommenderBuilder.getClass()
						.getSimpleName() + ">>>>>>>>>>>>>>>");
				sr.recommendByAlgorithm(userId, sr.maxOfRecommendedItems,
						recommenderBuilder);
				sr.printInfoRecommendations();
			}
			System.out.println("FINISHED!!");

		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}

}
