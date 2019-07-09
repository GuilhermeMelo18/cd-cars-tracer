package br.cin.tbookmarks.recommender.algorithms;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorization;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorizer;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

/**
 * base class for {@link Factorizer}s, provides ID to index mapping
 */
public abstract class AbstractFactorizerCBT implements Factorizer {

  private final DataModel dataModel;
  private FastByIDMap<Integer> userIDMapping;
  private FastByIDMap<Integer> itemIDMapping;
  private final RefreshHelper refreshHelper;

  protected AbstractFactorizerCBT(DataModel dataModel) throws TasteException {
    this.dataModel = dataModel;
    buildMappings();
    refreshHelper = new RefreshHelper(new Callable<Object>() {
      @Override
      public Object call() throws TasteException {
        buildMappings();
        return null;
      }
    });
    refreshHelper.addDependency(dataModel);
  }
  
  private void buildMappings() throws TasteException {
    userIDMapping = createIDMapping(dataModel.getNumUsers(), dataModel.getUserIDs());
    itemIDMapping = createIDMapping(dataModel.getNumItems(), dataModel.getItemIDs());
  }

  protected Factorization createFactorization(double[][] userFeatures, double[][] itemFeatures) {
    return new Factorization(userIDMapping, itemIDMapping, userFeatures, itemFeatures);
  }

  protected Integer userIndex(long userID) {
    Integer userIndex = userIDMapping.get(userID);
    if (userIndex == null) {
      userIndex = userIDMapping.size();
      userIDMapping.put(userID, userIndex);
    }
    return userIndex;
  }

  protected Integer itemIndex(long itemID) {
    Integer itemIndex = itemIDMapping.get(itemID);
    if (itemIndex == null) {
      itemIndex = itemIDMapping.size();
      itemIDMapping.put(itemID, itemIndex);
    }
    return itemIndex;
  }

  private static FastByIDMap<Integer> createIDMapping(int size, LongPrimitiveIterator idIterator) {
    FastByIDMap<Integer> mapping = new FastByIDMap<Integer>(size);
    int index = 0;
    while (idIterator.hasNext()) {
      mapping.put(idIterator.nextLong(), index++);
    }
    return mapping;
  }
  
  protected RealMatrix getDataModelMatrix(){
	  double X[][];
	  
	  RealMatrix dataModelMatrix = null;
	try {
		dataModelMatrix = MatrixUtils.createRealMatrix(dataModel.getNumUsers(), dataModel.getNumItems());
		
		X = new double[dataModel.getNumUsers()*3/5][dataModel.getNumItems()*3/5];
				
		LongPrimitiveIterator users = dataModel.getUserIDs();
		while (users.hasNext()) {
		      Long user = users.next();
		      PreferenceArray pa = dataModel.getPreferencesFromUser(user);
		      for(int itemIndex = 0; itemIndex < pa.length(); itemIndex++){
		    	  long itemID = pa.getItemID(itemIndex);
		    	  double rating = pa.getValue(itemIndex);
		    	  X[userIndex(user)][itemIndex(itemID)] = rating;
		      }
		 }
		
		dataModelMatrix = MatrixUtils.createRealMatrix(X);
	} catch (TasteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	  
	  
	  return dataModelMatrix;   
  }

  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    refreshHelper.refresh(alreadyRefreshed);
  }
  
  /*public static void main(String[] args) {
	  double X[][] = new double[2][2];
	  System.out.println(X[0][0]);
  }*/
  
}
