package br.cin.tbookmarks.recommender.algorithms;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorization;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.RandomUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.AmazonCrossDataset;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CodeBookGenerator extends AbstractFactorizerCBT {

  private final DataModel dataModel;

  /** number of features used to compute this factorization */
  private final int k,l;
  /** parameter to control the regularization */
  //private final double lambda;
  /** number of iterations */
  private final int numIterations;

  /*private final boolean usesImplicitFeedback;
  *//** confidence weighting parameter, only necessary when working with implicit feedback *//*
  private final double alpha;*/

  private final int numTrainingThreads;

  //private static final double DEFAULT_ALPHA = 40;

  private static final Logger log = LoggerFactory.getLogger(CodeBookGenerator.class);

  /*public CodeBookGenerator(DataModel dataModel, int numFeatures, double lambda, int numIterations,
      boolean usesImplicitFeedback, double alpha, int numTrainingThreads) throws TasteException {
    super(dataModel);
    this.dataModel = dataModel;
    this.numFeatures = numFeatures;
    this.lambda = lambda;
    this.numIterations = numIterations;
    this.usesImplicitFeedback = usesImplicitFeedback;
    this.alpha = alpha;
    this.numTrainingThreads = numTrainingThreads;
  }

  public CodeBookGenerator(DataModel dataModel, int numFeatures, double lambda, int numIterations,
                         boolean usesImplicitFeedback, double alpha) throws TasteException {
    this(dataModel, numFeatures, lambda, numIterations, usesImplicitFeedback, alpha,
        Runtime.getRuntime().availableProcessors());
  }*/

  public CodeBookGenerator(DataModel dataModel, int k, int l, int numIterations) throws TasteException {
	  super(dataModel);
	   this.dataModel = dataModel;
	   this.k = k;
	    this.l = l;
	    this.numIterations = numIterations;
	    this.numTrainingThreads = Runtime.getRuntime().availableProcessors();
  }

  static class Features {

    private final DataModel dataModel;
    private final int k,l;

    private final double[][] V;
    private final double[][] U;
    private final double[][] S;

    Features(CodeBookGenerator factorizer) throws TasteException {
      dataModel = factorizer.dataModel;
      l = factorizer.l;
      k = factorizer.k;
      
      V = new double[dataModel.getNumItems()][l];
      /*LongPrimitiveIterator itemIDsIterator = dataModel.getItemIDs();
      while (itemIDsIterator.hasNext()) {
        long itemID = itemIDsIterator.nextLong();
        int itemIDIndex = factorizer.itemIndex(itemID);
        V[itemIDIndex][0] = averateRating(itemID);
        for (int feature = 1; feature < l; feature++) {
          V[itemIDIndex][feature] = random.nextDouble() * 0.1;
        }
      }*/
      U = new double[dataModel.getNumUsers()][k];
      S = new double[k][l];
      
      initializeRandomly(V,dataModel.getNumItems(),l);
      initializeRandomly(U,dataModel.getNumUsers(),k);
      initializeRandomly(S,k,l);
    }

    private void initializeRandomly(double[][] v, int r, int c) {
    	Random random = RandomUtils.getRandom();
    	
		for(int i = 0; i< r;i++){
			for(int j = 0; j< c;j++){
				v[i][j] = random.nextDouble();
			}
		}
		
	}

	double[][] getV() {
      return V;
    }

    double[][] getU() {
      return U;
    }
    
    double[][] getS() {
        return S;
      }

    Vector getUserFeatureColumn(int index) {
      return new DenseVector(U[index]);
    }

    Vector getItemFeatureColumn(int index) {
      return new DenseVector(V[index]);
    }

    void setFeatureColumnInU(int idIndex, Vector vector,int numFeatures) {
      setFeatureColumn(U, idIndex, vector, numFeatures);
    }

    void setFeatureColumnInV(int idIndex, Vector vector, int numFeatures) {
      setFeatureColumn(V, idIndex, vector, numFeatures);
    }

    protected void setFeatureColumn(double[][] matrix, int idIndex, Vector vector, int numFeatures) {
      for (int feature = 0; feature < numFeatures; feature++) {
        matrix[idIndex][feature] = vector.get(feature);
      }
    }

    protected double averateRating(long itemID) throws TasteException {
      PreferenceArray prefs = dataModel.getPreferencesForItem(itemID);
      RunningAverage avg = new FullRunningAverage();
      for (Preference pref : prefs) {
        avg.addDatum(pref.getValue());
      }
      return avg.getAverage();
    }
  }

  @Override
  public Factorization factorize() throws TasteException {
    log.info("starting to compute the Code Book (CBT) Building...");
    final Features features = new Features(this);
    
    RealMatrix V = MatrixUtils.createRealMatrix(features.getV());
    
    RealMatrix U = MatrixUtils.createRealMatrix(features.getU());
    RealMatrix S = MatrixUtils.createRealMatrix(features.getS());
    
    RealMatrix X = getDataModelMatrix();

    for (int iteration = 0; iteration < numIterations; iteration++) {
      log.info("iteration {}", iteration);

      
      /* fix U,S - compute V */    
     
     RealMatrix numerator1 = (RealMatrix) X.transpose().multiply(U).multiply(S);
     RealMatrix denominator1 = (RealMatrix) V.multiply(V.transpose()).multiply(X.transpose()).multiply(U).multiply(S);
     
     RealMatrix inverseDen1 = new LUDecomposition(denominator1).getSolver().getInverse();
     
     V = (RealMatrix) V.multiply(numerator1).multiply(inverseDen1);
      
      /* fix V,S - compute U */
    
     RealMatrix numerator2 = (RealMatrix) X.multiply(V).multiply(S.transpose());
     RealMatrix denominator2 = (RealMatrix) U.multiply(U.transpose()).multiply(X).multiply(V).multiply(S.transpose());
     
     RealMatrix inverseDen2 = new LUDecomposition(denominator2).getSolver().getInverse();
     
     U = (RealMatrix) U.multiply(numerator2).multiply(inverseDen2);

     /* fix V,U - compute S */
      
     RealMatrix numerator3 = (RealMatrix) U.transpose().multiply(X).multiply(V);
     RealMatrix denominator3 = (RealMatrix) U.transpose().multiply(U).multiply(S).multiply(V.transpose()).multiply(V);
     
     RealMatrix inverseDen3 = new LUDecomposition(denominator3).getSolver().getInverse();
     
     S = (RealMatrix) S.multiply(numerator3).multiply(inverseDen3);
     
     System.out.println(U.toString());
     
    }

    log.info("finished computation of the factorization...");
    return createFactorization(U.getData(), V.getData());
  }

  protected ExecutorService createQueue() {
    return Executors.newFixedThreadPool(numTrainingThreads);
  }

  protected static Vector ratingVector(PreferenceArray prefs) {
    double[] ratings = new double[prefs.length()];
    for (int n = 0; n < prefs.length(); n++) {
      ratings[n] = prefs.get(n).getValue();
    }
    return new DenseVector(ratings, true);
  }

  //TODO find a way to get rid of the object overhead here
  protected OpenIntObjectHashMap<Vector> itemFeaturesMapping(LongPrimitiveIterator itemIDs, int numItems,
      double[][] featureMatrix) {
    OpenIntObjectHashMap<Vector> mapping = new OpenIntObjectHashMap<Vector>(numItems);
    while (itemIDs.hasNext()) {
      long itemID = itemIDs.next();
      mapping.put((int) itemID, new DenseVector(featureMatrix[itemIndex(itemID)], true));
    }

    return mapping;
  }

  protected OpenIntObjectHashMap<Vector> userFeaturesMapping(LongPrimitiveIterator userIDs, int numUsers,
      double[][] featureMatrix) {
    OpenIntObjectHashMap<Vector> mapping = new OpenIntObjectHashMap<Vector>(numUsers);

    while (userIDs.hasNext()) {
      long userID = userIDs.next();
      mapping.put((int) userID, new DenseVector(featureMatrix[userIndex(userID)], true));
    }

    return mapping;
  }

  protected Vector sparseItemRatingVector(PreferenceArray prefs) {
    SequentialAccessSparseVector ratings = new SequentialAccessSparseVector(Integer.MAX_VALUE, prefs.length());
    for (Preference preference : prefs) {
      ratings.set((int) preference.getUserID(), preference.getValue());
    }
    return ratings;
  }

  protected Vector sparseUserRatingVector(PreferenceArray prefs) {
    SequentialAccessSparseVector ratings = new SequentialAccessSparseVector(Integer.MAX_VALUE, prefs.length());
    for (Preference preference : prefs) {
      ratings.set((int) preference.getItemID(), preference.getValue());
    }
    return ratings;
  }
  
  public static void main(String[] args) {
	AbstractDataset ad = AmazonCrossDataset.getInstance();
	DataModel dm = ad.getModel();
	
	try {
		CodeBookGenerator cb = new CodeBookGenerator(dm, 4, 5, 10);
		Factorization f = cb.factorize();
		
	} catch (TasteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
