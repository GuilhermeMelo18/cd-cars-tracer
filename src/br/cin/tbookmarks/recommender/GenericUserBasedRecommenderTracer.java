package br.cin.tbookmarks.recommender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.common.LongPair;

import com.mathworks.toolbox.javabuilder.MWException;

import Jama.Matrix;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualPreferenceInterface;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.evaluation.PredictionValues;
import br.cin.tbookmarks.util.ItemResource;
import br.cin.tbookmarks.util.UsersPreferencesUtil;
import tracer.TracerRecommender;

public class GenericUserBasedRecommenderTracer extends AbstractRecommender implements UserBasedRecommender {
		
	
		/* Global Variables*/
		private float MAEValidate;
		private float RMSEValidate;
		AbstractDataset dataset;

		private ArrayList<PredictionValues> values;
		private ArrayList<PredictionValues> valuesPosFilter;
		private int NotCounterAvaliateTestRatings;
		private RunningAverage averageMAE;
		private RunningAverage averageRMSE;
		private RunningAverage averageMAEPosFilter;
		private RunningAverage averageRMSEPosFilter;

		
		
		private int totalOfTrainingRatingsFromSource;
		private int totalOfTrainingRatingsFromTarget;
		private int totalOfTestRatings;
		private TracerRecommender tracerRecommender;
		
		
		/* Target and Source Matrix */
		private double[][] TDMusicMatrix; 
		
		private double [][] SDBookMatrix;
		private double [][] SDMovieMatrix;

		private HashMap<Long, Integer> usersTargetMusic;
		private HashMap<Long, Integer> usersSourceBook;
		private HashMap<Long, Integer> usersSourceMovie;
		
		private HashMap<Long, Integer> itensTargetMusic;
		private HashMap<Long, Integer> itensSourceBook;
		private HashMap<Long, Integer> itensSourceMovie;
		
		
		
		/* TRACER PARAMETERS*/
		private double clusterTransferUser;
		private double clusterTransferItem;
		private double trainingIterations;
		private double trainingPercent;
		private double alpha;
		private double beta;
		


		public GenericUserBasedRecommenderTracer(double clusterTransferUser, double clusterTransferItem, double alpha, double beta,  
				double trainingIterations, double trainingPercent, DataModel datamodel, AbstractDataset dataset) {
			
			super(datamodel);
			this.dataset = dataset;
			
			this.clusterTransferUser = clusterTransferUser;
			this.clusterTransferItem = clusterTransferItem;
			this.trainingIterations = trainingIterations;
			this.trainingPercent = trainingPercent;
			this.alpha = alpha;
			this.beta = beta;
			
			this.totalOfTestRatings = 0;
			this.totalOfTrainingRatingsFromSource = 0;
			this.totalOfTrainingRatingsFromTarget = 0;
			this.NotCounterAvaliateTestRatings = 0;

			this.usersTargetMusic = new HashMap<Long, Integer>();
			this.itensTargetMusic = new HashMap<Long, Integer>();
			
			this.usersSourceBook = new HashMap<Long, Integer>();
			this.usersSourceMovie = new HashMap<Long, Integer>();
			
			this.itensSourceBook= new HashMap<Long, Integer>();
			this.itensSourceMovie= new HashMap<Long, Integer>();
			
			averageMAE = new FullRunningAverage();
			averageRMSE = new FullRunningAverage();
			values = new ArrayList<PredictionValues>();
			valuesPosFilter = new ArrayList<PredictionValues>();

			averageMAEPosFilter = new FullRunningAverage();
			averageRMSEPosFilter = new FullRunningAverage();
			
			
			
			
			try {
				this.tracerRecommender = new TracerRecommender();
			} catch (MWException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		public double getMAE() {
			
			return  averageMAE.getAverage();
			
		}
		
		public double getRMSE() {
			
			return Math.sqrt(averageRMSE.getAverage());
			
		}

		public double getAverageMAEPosFilter() {
			return averageMAEPosFilter.getAverage();
		}


		public void setAverageMAEPosFilter(RunningAverage averageMAEPosFilter) {
			this.averageMAEPosFilter = averageMAEPosFilter;
		}


		public double getAverageRMSEPosFilter() {
			return Math.sqrt(averageRMSEPosFilter.getAverage());
		}


		public void setAverageRMSEPosFilter(RunningAverage averageRMSEPosFilter) {
			this.averageRMSEPosFilter = averageRMSEPosFilter;
		}

		
		public int getNotCounterAvaliateTestRatings() {
			return NotCounterAvaliateTestRatings;
		}


		public void setNotCounterAvaliateTestRatings(int notCounterAvaliateTestRatings) {
			NotCounterAvaliateTestRatings = notCounterAvaliateTestRatings;
		}


		public int getTotalOfTrainingRatingsFromSource() {
			return totalOfTrainingRatingsFromSource;
		}



		public int getTotalOfTrainingRatingsFromTarget() {
			return totalOfTrainingRatingsFromTarget;
		}

		

		public int getTotalOfTestRatings() {
			return totalOfTestRatings;
		}
		
		

		public ArrayList<PredictionValues> getValues() {
			return values;
		}


		public void setValues(ArrayList<PredictionValues> values) {
			this.values = values;
		}
		
		

		public ArrayList<PredictionValues> getValuesPosFilter() {
			return valuesPosFilter;
		}


		public void setValuesPosFilter(ArrayList<PredictionValues> valuesPosFilter) {
			this.valuesPosFilter = valuesPosFilter;
		}


		public float getRMSEValidate() {
			return RMSEValidate;
		}


		public void setRMSEValidate(float rMSEValidate) {
			RMSEValidate = rMSEValidate;
		}
		

		public float getMAEValidate() {
			return MAEValidate;
		}


		public void setMAEValidate(float mAEValidate) {
			MAEValidate = mAEValidate;
		}


		public void buildTracer(DataModel dataModel, AbstractDataset dataset) {
		
			try {
	
				LongPrimitiveIterator userIdsIterator = dataModel.getUserIDs();
				
				while(userIdsIterator.hasNext()){
					
					Long userId = userIdsIterator.next();
						
					PreferenceArray prefsForUser = dataModel.getPreferencesFromUser(userId);
					
					HashMap<ItemDomain, Integer> userDomains = UsersPreferencesUtil.verifyUserOverlapping(prefsForUser, dataset);
					
					for (ItemDomain userDomain : userDomains.keySet()) {
						
						addUsersItens(dataModel, dataset, userId, userDomain);
						
					}
				
				}
				
			
				/* Build Matrix to TRACER */
				this.buildMatrix(dataModel, dataset);
				
			} catch (TasteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		private void addUsersItens(DataModel dataModel, AbstractDataset dataset, long userId, ItemDomain domain) throws TasteException {
			
			
			if(domain == ItemDomain.MUSIC) {
				
				this.usersTargetMusic.put(userId, this.usersTargetMusic.size());
			
			}else if(domain == ItemDomain.MOVIE) {
				
				this.usersSourceMovie.put(userId, this.usersSourceMovie.size());
				
			}else if(domain == ItemDomain.BOOK) {
				
				this.usersSourceBook.put(userId, this.usersSourceBook.size());
			}
			
			
			PreferenceArray prefsForUser = dataModel.getPreferencesFromUser(userId);
			
			int maxItens = prefsForUser.getIDs().length;
			
			for (int i = 0; i < maxItens; i++) {
				
				long itemId  = prefsForUser.getIDs()[i];
				ItemDomain itemDomain = dataset.getItemInformationByID(itemId).getItemDomain();
				
				if(!this.itensTargetMusic.containsKey(itemId) && itemDomain == ItemDomain.MUSIC) {

					this.itensTargetMusic.put(itemId, this.itensTargetMusic.size());
				
				}else if(!this.itensSourceMovie.containsKey(itemId) && itemDomain == ItemDomain.MOVIE) {

					this.itensSourceMovie.put(itemId, this.itensSourceMovie.size());
				
				}else if(!this.itensSourceBook.containsKey(itemId) && itemDomain == ItemDomain.BOOK) {

					this.itensSourceBook.put(itemId, this.itensSourceBook.size());
				
				}
				
				
			}
	
		}

		
	
		
		private  double[][] fillMatrix(HashMap<Long,Integer> users, HashMap<Long,Integer> itens, 
				DataModel dataModel, AbstractDataset dataset, ItemDomain domain) throws TasteException {
			

			double [][] model = new Matrix(users.size(), itens.size()).getArray();
			
			
			for (Long userId : users.keySet()) {

				PreferenceArray prefsForUser = dataModel.getPreferencesFromUser(userId);
				
				for (int m = 0; m < prefsForUser.getIDs().length; m++) {
					
					long itemId  = prefsForUser.getIDs()[m];
					
					if(itens.containsKey(itemId)) {
						
						model[users.get(userId)][itens.get(itemId)] = prefsForUser.getValue(m);
						
						if(domain == ItemDomain.BOOK || domain == ItemDomain.MOVIE) {
							this.totalOfTrainingRatingsFromSource++;
						}else {
							this.totalOfTrainingRatingsFromTarget++;
						}
					}
				}
			
			}
			
			return model;
		}
		


		/** Build Matrix to TRACER 
		 * @throws TasteException */
		private void buildMatrix(DataModel dataModel, AbstractDataset dataset) throws TasteException {
			
			
			/* Locate Tracer Target Matrix*/
			this.TDMusicMatrix = this.fillMatrix(this.usersTargetMusic, this.itensTargetMusic , dataModel, dataset, ItemDomain.MUSIC);
		
			
			this.SDBookMatrix = this.fillMatrix(this.usersSourceBook, this.itensSourceBook, dataModel, dataset, ItemDomain.BOOK);
			this.SDMovieMatrix = this.fillMatrix(this.usersSourceMovie, this.itensSourceMovie, dataModel, dataset, ItemDomain.MOVIE);
			
			
			// Desalocar Memória
			desalocarMemoria();
			
	}
	
	
	private HashMap<Long, Integer> convertHashMap(ArrayList<ItemResource> sourceItens, int limitItens){
		
		HashMap<Long, Integer>  convertMap = new HashMap<Long, Integer>();
		
		for (int i = 0; i < sourceItens.size(); i++) {
			
			ItemResource is = sourceItens.get(i);
			
			if(convertMap.size()>=limitItens) {
				return convertMap;
			}
				
			convertMap.put(is.getIdItem(), i);
			
		}
		
		return convertMap;
		
	}
	
	
	@Override
	public float estimatePreference(long userID, long itemID) throws TasteException {
		
		for (int i = 0; i < getValues().size(); i++) {
			
			PredictionValues pv = getValues().get(i);
			
			if(pv.getUserID() == userID && pv.getItemID() == itemID ) {
				
				valuesPosFilter.add(pv);
				double diff = pv.getRealPref() - pv.getEstimatedPref();
			    averageMAEPosFilter.addDatum(Math.abs(diff));
			    averageRMSEPosFilter.addDatum(diff * diff);
			    break;
			}
		}
		
		return 0;
	}
	
	
	/* Método para rodar o tracer*/
	public void runTracerRecommender() throws TasteException {
		
		this.buildTracer(this.getDataModel(), this.dataset);
		
		try {

			Object[] result = tracerRecommender.tracer(6, this.SDBookMatrix, this.SDMovieMatrix, this.TDMusicMatrix ,this.clusterTransferUser,this.clusterTransferItem, this.trainingIterations, this.alpha, this.beta, this.trainingPercent);
			
			
			//this.MAEResult =  Float.parseFloat(result[0].toString());
			//this.RMSEResult =  Float.parseFloat(result[1].toString());	
			this.MAEValidate = Float.parseFloat(result[2].toString());
			this.RMSEValidate = Float.parseFloat(result[3].toString());
			
			String predictValues = result[4].toString();
			String testData =  result[5].toString();

			/* Desalocar Matrizes Tracer*/
			desalocarMatrizesTracer();
			
			builderPredictionsValues(testData, predictValues);
			
			
		} catch (MWException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	private void builderPredictionsValues(String testData, String predictValues) throws TasteException {
		
		HashMap<Integer, Long> users = revertHashMap(this.usersTargetMusic);
		HashMap<Integer, Long> itens = revertHashMap(this.itensTargetMusic);
		
		
		String [] testRatings  = testData.split("\n");
		
		String [] predicts = predictValues.split("\n");

		if(!predictValues.equals("NaN")) {
			
			for (int i = 0; i < testRatings.length; i++) {
				
				String[] stringTest = testRatings[i].trim().replaceAll(" +", " ").split(" ");
				
				float predictRating = (float)Double.parseDouble(predicts[i].trim());
				
				long userId = users.get(Integer.parseInt(stringTest[0])-1) ;
				long itemId = itens.get(Integer.parseInt(stringTest[1])-1);
				long realRating = Long.parseLong(stringTest[2]); 
				long[] contextualPreference = null;
				
				PreferenceArray pArray = this.getDataModel().getPreferencesFromUser(userId);
				for (Preference preference : pArray) {
					ContextualPreferenceInterface cpRealPref = (ContextualPreferenceInterface) preference;
					if(itemId == preference.getItemID()) {
						contextualPreference = cpRealPref.getContextualPreferences();
						break;
					}
				}
				
				double diff = realRating - predictRating;
				this.values.add(new PredictionValues(realRating, predictRating, userId ,itemId, contextualPreference));
				this.averageMAE.addDatum(Math.abs(diff));
			    this.averageRMSE.addDatum(diff * diff);
			
				   
			}
			
			this.totalOfTestRatings = testRatings.length;
			this.totalOfTrainingRatingsFromTarget = (int) (this.totalOfTrainingRatingsFromTarget - this.totalOfTestRatings);
			
		}

		this.usersTargetMusic = null;
		this.itensTargetMusic = null;
	}
	
	private void desalocarMatrizesTracer() {
		
		this.TDMusicMatrix = null;
		this.SDBookMatrix = null;
		this.SDMovieMatrix = null;

	} 
	

	//Desalocando Memória

	private void desalocarMemoria() {

		this.usersSourceBook = null;
		this.usersSourceMovie = null;
		
		this.itensSourceBook = null;
		this.itensSourceMovie = null;
	}
	 
	private HashMap<Integer, Long> revertHashMap(HashMap<Long, Integer> hashmap){
		
		HashMap<Integer, Long> revert = new HashMap<Integer, Long>();
	
		for (Long ds : hashmap.keySet()) {
			
			int value  = hashmap.get(ds);
			
			revert.put(value, ds);
		}
		
		return revert;
	}
	
	
	@Override
	public List<RecommendedItem> recommend(long arg0, int arg1, IDRescorer arg2) throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(Collection<Refreshable> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public long[] mostSimilarUserIDs(long arg0, int arg1) throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long[] mostSimilarUserIDs(long arg0, int arg1, Rescorer<LongPair> arg2) throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
