package br.cin.tbookmarks.recommender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.EstimatedPreferenceCapper;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.LongPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * <p>
 * A simple {@link org.apache.mahout.cf.taste.recommender.Recommender}
 * which uses a given {@link DataModel} and {@link UserNeighborhood} to produce recommendations.
 * </p>
 */
public class GenericUserBasedRecommenderCremonesiRicardo extends AbstractRecommender implements UserBasedRecommender {
  
  private static final Logger log = LoggerFactory.getLogger(GenericUserBasedRecommenderCremonesiRicardo.class);
  
  private final UserNeighborhood neighborhood;
  private final int nSize;
  private final UserSimilarity similarity;
  private final RefreshHelper refreshHelper;
  private EstimatedPreferenceCapper capper;
  
  private static FastByIDMap<FastByIDMap<Double>> similaritiesUserTransclosureMatrix;
  
  public GenericUserBasedRecommenderCremonesiRicardo(int n,DataModel dataModel,
                                     UserNeighborhood neighborhood,
                                     UserSimilarity similarity) {
    super(dataModel);
    Preconditions.checkArgument(neighborhood != null, "neighborhood is null");
    this.neighborhood = neighborhood;
    this.nSize = n;
    this.similarity = similarity;
    this.refreshHelper = new RefreshHelper(new Callable<Void>() {
      @Override
      public Void call() {
        capper = buildCapper();
        return null;
      }
    });
    refreshHelper.addDependency(dataModel);
    refreshHelper.addDependency(similarity);
    refreshHelper.addDependency(neighborhood);
    capper = buildCapper();

	similaritiesUserTransclosureMatrix = new FastByIDMap<FastByIDMap<Double>>();
  }
  

public UserSimilarity getSimilarity() {
    return similarity;
  }

@Override
public List<RecommendedItem> recommend(long userID, int howMany) throws TasteException {
	return this.recommend(userID, howMany,null);
}
  
  @Override
  public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
    Preconditions.checkArgument(howMany >= 1, "howMany must be at least 1");

    log.debug("Recommending items for user ID '{}'", userID);

    long[] theNeighborhood = neighborhood.getUserNeighborhood(userID);

    if (theNeighborhood.length == 0) {
      return Collections.emptyList();
    }
    
    FastByIDMap<Double> similaritiesTransClosure;
    
    if(!similaritiesUserTransclosureMatrix.containsKey(userID)){
    
	    similaritiesTransClosure = discoverNewLinks(userID, theNeighborhood);
	    
    }else{
    	similaritiesTransClosure = similaritiesUserTransclosureMatrix.get(userID);
    }
    
    LongPrimitiveIterator similaritiesTransClosureIterator = similaritiesTransClosure.keySetIterator();
    
    long[] newNeighborhood = new long[similaritiesTransClosure.size()];
    
    int index = 0;
    
    while (similaritiesTransClosureIterator.hasNext()) {
    	newNeighborhood[index++] = similaritiesTransClosureIterator.next();
    }

    FastIDSet allItemIDs = getAllOtherItems(newNeighborhood, userID);

    TopItems.Estimator<Long> estimator = new Estimator(userID, newNeighborhood);

    List<RecommendedItem> topItems = TopItems
        .getTopItems(howMany, allItemIDs.iterator(), rescorer, estimator);

    log.debug("Recommendations are: {}", topItems);
    return topItems;
  }
  
  @Override
  public float estimatePreference(long userID, long itemID) throws TasteException {
    DataModel model = getDataModel();
    Float actualPref = model.getPreferenceValue(userID, itemID);
    if (actualPref != null) {
      return actualPref;
    }
    long[] theNeighborhood = neighborhood.getUserNeighborhood(userID);
    return doEstimatePreference(userID, theNeighborhood, itemID);
  }
  
  @Override
  public long[] mostSimilarUserIDs(long userID, int howMany) throws TasteException {
    return mostSimilarUserIDs(userID, howMany, null);
  }
  
  @Override
  public long[] mostSimilarUserIDs(long userID, int howMany, Rescorer<LongPair> rescorer) throws TasteException {
    TopItems.Estimator<Long> estimator = new MostSimilarEstimator(userID, similarity, rescorer);
    return doMostSimilarUsers(howMany, estimator);
  }
  
  private long[] doMostSimilarUsers(int howMany, TopItems.Estimator<Long> estimator) throws TasteException {
    DataModel model = getDataModel();
    return TopItems.getTopUsers(howMany, model.getUserIDs(), null, estimator);
  }
  
  protected float doEstimatePreference(long userA, long[] theNeighborhood, long itemID) throws TasteException {
    if (theNeighborhood.length == 0) {
      return Float.NaN;
    }
    DataModel dataModel = getDataModel();
    double preference = 0.0;
    double totalSimilarity = 0.0;
    int count = 0;
    
    FastByIDMap<Double> similaritiesTransClosure;
    
    if(!similaritiesUserTransclosureMatrix.containsKey(userA)){
    
	    similaritiesTransClosure = discoverNewLinks(userA, theNeighborhood);
	    
    }else{
    	similaritiesTransClosure = similaritiesUserTransclosureMatrix.get(userA);
    }
    
    LongPrimitiveIterator similaritiesTransClosureIterator = similaritiesTransClosure.keySetIterator();
    
    while (similaritiesTransClosureIterator.hasNext()) {
    	
    	long userIDTransClosured = similaritiesTransClosureIterator.next();
    	
        if (userIDTransClosured != userA) {
          // See GenericItemBasedRecommender.doEstimatePreference() too
          Float pref = dataModel.getPreferenceValue(userIDTransClosured, itemID);
          if (pref != null) {
            double theSimilarity = similaritiesTransClosure.get(userIDTransClosured);
            if (!Double.isNaN(theSimilarity)) {    	  
          	  
              preference += theSimilarity * pref;
              totalSimilarity += theSimilarity;
              count++;
            }
            
          }
        }
      }
    
    
    // Throw out the estimate if it was based on no data points, of course, but also if based on
    // just one. This is a bit of a band-aid on the 'stock' item-based algorithm for the moment.
    // The reason is that in this case the estimate is, simply, the user's rating for one item
    // that happened to have a defined similarity. The similarity score doesn't matter, and that
    // seems like a bad situation.
    
    if (count <= 1) {
      return Float.NaN;
    }
    float estimate = (float) (preference / totalSimilarity);
    if (capper != null) {
      estimate = capper.capEstimate(estimate);
    }
    return estimate;
  }


private FastByIDMap<Double> discoverNewLinks(long userA, long[] theNeighborhood)
		throws TasteException {
	FastByIDMap<Double> similaritiesTransClosure;
	FastByIDMap<Double> similaritiesMap = new FastByIDMap<Double>();

	for (long userB : theNeighborhood) {
	  if (userB != userA) {
	    // See GenericItemBasedRecommender.doEstimatePreference() too
	    //Float pref = dataModel.getPreferenceValue(userB, itemID);
	    //if (pref != null) {
	      double theSimilarity = similarity.userSimilarity(userA, userB);
	      if (!Double.isNaN(theSimilarity)) {
	    	  similaritiesMap.put(userB, theSimilarity);
	     // }
	    }
	  }
	}
	
	if(nSize > similaritiesMap.size()){
	
	    FastByIDMap<FastByIDMap<Double>> newNeighborhood = new FastByIDMap<FastByIDMap<Double>> ();
	    
	    LongPrimitiveIterator similaritiesMapIterator = similaritiesMap.keySetIterator();
	    
	    while (similaritiesMapIterator.hasNext() /*&& (nSize >= (similaritiesTransClosure.size() + newNeighborhood.size()))*/) {
	    	
	    	Long userB = similaritiesMapIterator.next();
	    
		    long[] theNeighborhoodUserC = neighborhood.getUserNeighborhood(userB);		    
				    
			  for (long userC : theNeighborhoodUserC) {
				  //!similaritiesMap.keySet().contains(userIDY) so considera novos links
			      if (!similaritiesMap.containsKey(userC) 
			    		  && userC != userA && userC != userB) {
			        
			        //Float prefC = dataModel.getPreferenceValue(userC, itemID);
			        //if (prefC != null) {
			          double theSimilarityBC = similarity.userSimilarity(userB, userC);
			          
			          FastByIDMap<Double> current = newNeighborhood.get(userC);
			          
			          if(current == null){
			        	  FastByIDMap<Double> userBSimilarity = new FastByIDMap<Double>();
			        	 			          
				          userBSimilarity.put(userB, theSimilarityBC);
				          
				          newNeighborhood.put(userC, userBSimilarity);

				          /*if(nSize <= (similaritiesTransClosure.size() + newNeighborhood.size())){
				        	  break;
				          }*/
			          }else{
			        	  current.put(userB, theSimilarityBC);
			          }
			          
				  }
		    }
		    
	    }
	
	    similaritiesTransClosure = similaritiesMap.clone();
	    //FastByIDMap<Double> newSimilaritiesTrans = new FastByIDMap<Double>();
	    
	    double infoInit[] = min(similaritiesTransClosure);
	    
	    double minValue = infoInit[0];
	    long minUser = (long) infoInit[1];
	    
	    LongPrimitiveIterator newNeighborhoodIterator = newNeighborhood.keySetIterator();
	    
	    while(newNeighborhoodIterator.hasNext()){
	    	double newSimilarity = 0.0;
	    	int counter = 0;
	    	
	    	long newUserLinkID = newNeighborhoodIterator.next();
	    	
	    	FastByIDMap<Double> newSimilarities =  newNeighborhood.get(newUserLinkID);
	    	
	    	LongPrimitiveIterator newSimilaritiesIterator = newSimilarities.keySetIterator();
	    	
	    	while(newSimilaritiesIterator.hasNext()){
	    		
	    		long knownUserID = newSimilaritiesIterator.next();
	    		
	    		newSimilarity = newSimilarity + (newSimilarities.get(knownUserID) * similaritiesMap.get(knownUserID));
	    		counter++;
	    	}
	    	
	    	double normalizedSimilarity = newSimilarity/counter;
	    	
	    	if(nSize > similaritiesTransClosure.size()/*+newSimilaritiesTrans.size()*/){
	    		//newSimilaritiesTrans.put(newUserLinkID, normalizedSimilarity);
	    		similaritiesTransClosure.put(newUserLinkID, normalizedSimilarity);
	    		if(normalizedSimilarity < minValue){
	    			minValue = normalizedSimilarity;
	    			minUser = newUserLinkID;
	    		}
	    	}else{
	    		if(normalizedSimilarity > minValue){
	    			/*newSimilaritiesTrans.put(newUserLinkID, normalizedSimilarity);
	    			newSimilaritiesTrans.remove(minUser);
	    			double minReturn[] = min(newSimilaritiesTrans);*/
	    			similaritiesTransClosure.put(newUserLinkID, normalizedSimilarity);
	    			similaritiesTransClosure.remove(minUser);
	    			double minReturn[] = min(similaritiesTransClosure);
	    			minValue = minReturn[0];
	    			minUser = (long)minReturn[1];
	    		}
	    	}
	    	
	    	
	    	//System.out.println("New relation discovered for user "+userA+": "+newUserLinkID+" is "+newSimilarity+" similar");
	    }
	    
	    
	    /*LongPrimitiveIterator newSimilaritiesTransIterator = newSimilaritiesTrans.keySetIterator();
	    
	    while(newSimilaritiesTransIterator.hasNext()){
	    	
	    	long newUser = newSimilaritiesTransIterator.next();
	    	
	    	similaritiesTransClosure.put(newUser, newSimilaritiesTrans.get(newUser));
	    }*/
	
	    
	    similaritiesUserTransclosureMatrix.put(userA,similaritiesTransClosure);
	}else{
		
		similaritiesTransClosure = similaritiesMap;
		
		similaritiesUserTransclosureMatrix.put(userA,similaritiesTransClosure);
	}
	return similaritiesTransClosure;
}
  
  private double[] min(FastByIDMap<Double> newSimilaritiesTrans) {
	  
	double min = 1000000;
	double user = -1;
	
	double ret[] = new double[2]; 

	LongPrimitiveIterator newSimilaritiesTransIterator = newSimilaritiesTrans.keySetIterator();
	
	while(newSimilaritiesTransIterator.hasNext()){
		long temp = newSimilaritiesTransIterator.next();
		if(newSimilaritiesTrans.get(temp) < min){
			min = newSimilaritiesTrans.get(temp);
			user = temp;
		}
	}
	
	ret[0] = min;
	ret[1] = user;
	  
	return ret;
  }



protected FastIDSet getAllOtherItems(long[] theNeighborhood, long theUserID) throws TasteException {
    DataModel dataModel = getDataModel();
    FastIDSet possibleItemIDs = new FastIDSet();
    for (long userID : theNeighborhood) {
      possibleItemIDs.addAll(dataModel.getItemIDsFromUser(userID));
    }
    possibleItemIDs.removeAll(dataModel.getItemIDsFromUser(theUserID));
    return possibleItemIDs;
  }
  
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    refreshHelper.refresh(alreadyRefreshed);
  }
  
  @Override
  public String toString() {
    return "GenericUserBasedRecommenderCremonesiRicardo[neighborhood:" + neighborhood + ']';
  }

  private EstimatedPreferenceCapper buildCapper() {
    DataModel dataModel = getDataModel();
    if (Float.isNaN(dataModel.getMinPreference()) && Float.isNaN(dataModel.getMaxPreference())) {
      return null;
    } else {
      return new EstimatedPreferenceCapper(dataModel);
    }
  }
  
  private static final class MostSimilarEstimator implements TopItems.Estimator<Long> {
    
    private final long toUserID;
    private final UserSimilarity similarity;
    private final Rescorer<LongPair> rescorer;
    
    private MostSimilarEstimator(long toUserID, UserSimilarity similarity, Rescorer<LongPair> rescorer) {
      this.toUserID = toUserID;
      this.similarity = similarity;
      this.rescorer = rescorer;
    }
    
    @Override
    public double estimate(Long userID) throws TasteException {
      // Don't consider the user itself as a possible most similar user
      if (userID == toUserID) {
        return Double.NaN;
      }
      if (rescorer == null) {
        return similarity.userSimilarity(toUserID, userID);
      } else {
        LongPair pair = new LongPair(toUserID, userID);
        if (rescorer.isFiltered(pair)) {
          return Double.NaN;
        }
        double originalEstimate = similarity.userSimilarity(toUserID, userID);
        return rescorer.rescore(pair, originalEstimate);
      }
    }
  }
  
  private final class Estimator implements TopItems.Estimator<Long> {
    
    private final long theUserID;
    private final long[] theNeighborhood;
    
    Estimator(long theUserID, long[] theNeighborhood) {
      this.theUserID = theUserID;
      this.theNeighborhood = theNeighborhood;
    }
    
    @Override
    public double estimate(Long itemID) throws TasteException {
      return doEstimatePreference(theUserID, theNeighborhood, itemID);
    }
  }
  
  /*public static void main(String[] args) {
	  
	  		
	  	int numUsers = 5;
	  	
	  	double similarity[][] = new double[numUsers][numUsers];
	  	similarity[0][0] = 1;
	  	similarity[0][1] = 0.5f;
	  	similarity[0][2] = 0.4f;
	  	similarity[0][3] = 0f;
	  	similarity[0][4] = 0f;
	  	
	  	similarity[1][0] = similarity[0][1];
	  	similarity[1][1] = 1;
	  	similarity[1][2] = 0.8f;
	  	similarity[1][3] = 0.3f;
	  	similarity[1][4] = 0.2f;
	  	
		similarity[2][0] = similarity[0][2];
		similarity[2][1] = similarity[1][2];
		similarity[2][2] = 1;
		similarity[2][3] = 0.2f;
		similarity[2][4] = 0.4f;
		
		similarity[3][0] = similarity[0][3];
		similarity[3][1] = similarity[1][3];
		similarity[3][2] = similarity[2][3];
		similarity[3][3] = 1;
		similarity[3][4] = 0.5f;
		
		similarity[4][0] = similarity[0][4];
		similarity[4][1] = similarity[1][4];
		similarity[4][2] = similarity[2][4];
		similarity[4][3] = similarity[3][4];
		similarity[4][4] = 1;
	  	
	  	
	  	
	  	int userA = 0;
	  	
	  	Integer theNeighborhood[] = getUserNeighborhood(userA,similarity);
	  	
	  	//HashMap<Long,>

	    if (theNeighborhood.length == 0) {
	      return Float.NaN;
	    }
	    //DataModel dataModel = getDataModel();
	    double preference = 0.0;
	    double totalSimilarity = 0.0;
	    int count = 0;
	    
	    HashMap<Integer, Double> similaritiesMap = new HashMap<Integer, Double>();

	    for (int userB : theNeighborhood) {
	      if (userB != userA) {
	        // See GenericItemBasedRecommender.doEstimatePreference() too
	        //Float pref = dataModel.getPreferenceValue(userB, itemID);
	        //if (pref != null) {
	          double theSimilarity = similarity[userA][userB];
	          if (!Double.isNaN(theSimilarity)) {
	        	  similaritiesMap.put(new Integer(userB), theSimilarity);
	          }
	        //}
	      }
	    }
		    
	    HashMap<Integer, Double> similaritiesTransClosure = (HashMap<Integer, Double>) similaritiesMap.clone();
	    
	    HashMap<Integer,HashMap<Integer, Double>> newNeighborhood = new HashMap<Integer, HashMap<Integer,Double>>();
	    
	    for (int userB : similaritiesMap.keySet()) {
	    
		    Integer[] theNeighborhoodUserC = getUserNeighborhood(userB,similarity);
				    
			  for (int userC : theNeighborhoodUserC) {
				  //!similaritiesMap.keySet().contains(userIDY) so considera novos links
			      if (!similaritiesMap.keySet().contains(userC) 
			    		  && userC != userA && userC != userB) {
			        
			        //Float prefC = dataModel.getPreferenceValue(userC, itemID);
			        //if (prefC != null) {
			          double theSimilarityBC = similarity[userB][userC];
			          
			          HashMap<Integer, Double> current = newNeighborhood.get(userC);
			          
			          if(current == null){
			        	  HashMap<Integer, Double> userBSimilarity = new HashMap<Integer, Double>();
				          
				          userBSimilarity.put(userB, theSimilarityBC);
				          
				          newNeighborhood.put(userC, userBSimilarity);
				          
			          }else{
			        	  current.put(userB, theSimilarityBC);
			          }
			          
			       // }
				      
				  }
		    }
		    
	    }
	    
	    for(int newUserLinkID : newNeighborhood.keySet()){
	    	double newSimilarity = 0.0;
	    	
	    	HashMap<Integer, Double> newSimilarities =  newNeighborhood.get(newUserLinkID);
	    	
	    	for(int knownUserID : newSimilarities.keySet()){
	    		newSimilarity = newSimilarity + (newSimilarities.get(knownUserID) * similaritiesMap.get(knownUserID));
	    	}
	    	similaritiesTransClosure.put(newUserLinkID, newSimilarity);
	    }
	    
	    for (long userIDTransClosured : similaritiesTransClosure.keySet()) {
	        if (userIDTransClosured != userA) {
	          // See GenericItemBasedRecommender.doEstimatePreference() too
	          Float pref = dataModel.getPreferenceValue(userIDTransClosured, itemID);
	          if (pref != null) {
	            double theSimilarity = similaritiesTransClosure.get(userIDTransClosured);
	            if (!Double.isNaN(theSimilarity)) {    	  
	          	  
	              preference += theSimilarity * pref;
	              totalSimilarity += theSimilarity;
	              count++;
	            }
	            
	          }
	        }
	      }
	    
	    // Throw out the estimate if it was based on no data points, of course, but also if based on
	    // just one. This is a bit of a band-aid on the 'stock' item-based algorithm for the moment.
	    // The reason is that in this case the estimate is, simply, the user's rating for one item
	    // that happened to have a defined similarity. The similarity score doesn't matter, and that
	    // seems like a bad situation.
	    
	    System.out.println(theUserID+" "+itemID);
	    System.out.println(similaritiesTransClosure);
	    
	    if (count <= 1) {
	      return Float.NaN;
	    }
	    float estimate = (float) (preference / totalSimilarity);
	    if (capper != null) {
	      estimate = capper.capEstimate(estimate);
	    }
	    //return estimate;
	  
  }
  private static Integer[] getUserNeighborhood(long userB, double[][] similarity2) {
	ArrayList<Integer> n = new ArrayList<Integer>();
	
	double vector[] = similarity2[(int)userB];
	
	for(int i = 0; i < vector.length; i++){
		if(vector[i] != 0 && userB != i){
			n.add(i);
		}
	}
	
	Integer[] returned = new Integer[n.size()];
	
	for(int i=0; i< n.size();i++){
		returned[i] = n.get(i);
	}
	
	return returned;
  }


private static float[][] copyArray(float[][] arrayUserUser, int num_users) {
		float newArray[][] = new float[num_users][num_users];
		for (int i = 0; i < num_users; i++) {
			for (int j = 0; j < num_users; j++) {
				newArray[i][j] = arrayUserUser[i][j];
			}
		}
		return newArray;
	}

	private static void printArray(float[][] arrayUserUser, int num_users) {
		for (int i = 0; i < num_users; i++) {
			for (int j = 0; j < num_users; j++) {
				System.out.print(arrayUserUser[i][j]+" ");
			}
			System.out.print("\n");
		}
		
	}*/
}

