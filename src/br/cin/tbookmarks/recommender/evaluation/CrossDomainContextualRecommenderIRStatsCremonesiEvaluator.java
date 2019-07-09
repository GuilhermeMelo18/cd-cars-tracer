package br.cin.tbookmarks.recommender.evaluation;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.recommender.ByValueRecommendedItemComparator;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.common.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.cin.tbookmarks.recommender.algorithms.ContextualRecommenderBuilder;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.contextual.ContextualDataModel;
import br.cin.tbookmarks.recommender.database.contextual.ContextualPreferenceInterface;
import br.cin.tbookmarks.recommender.database.contextual.ContextualUserPreferenceArray;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.util.Functions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * <p>
 * For each user, these implementation determine the top {@code n} preferences,
 * then evaluate the IR statistics based on a {@link DataModel} that does not
 * have these values. This number {@code n} is the "at" value, as in
 * "precision at 5". For example, this would mean precision evaluated by
 * removing the top 5 preferences for a user and then finding the percentage of
 * those 5 items included in the top 5 recommendations for that user.
 * </p>
 */
public final class CrossDomainContextualRecommenderIRStatsCremonesiEvaluator
		implements ContextualRecommenderIRStatsEvaluator {

	private static final Logger log = LoggerFactory
			.getLogger(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);

	private static final double LOG2 = Math.log(2.0);

	/**
	 * Pass as "relevanceThreshold" argument to
	 * {@link #evaluate(ContextualRecommenderBuilder, DataModelBuilder, DataModel, IDRescorer, int, double, double)}
	 * to have it attempt to compute a reasonable threshold. Note that this will
	 * impact performance.
	 */
	public static final double CHOOSE_THRESHOLD = Double.NaN;

	//private static final boolean considerTestedContext = true;

	private final Random random;

	private final int numOfSortedItens = 100;

	private int numItensTraining;
	private int numItensProbe;

	private int numUsersTraining;
	private int numUsersProbe;
	
	private int numRatingsInProbe;

	private int numOverlapedUsersTraining;

	//private List<ItemDomain> domains;
	//private List<ContextualCriteria> contexts;

	private ArrayList<CremonesiValues> cremonesiValuesList;
	private RunningAverage precision;
	private RunningAverage recall;
	
	private RunningAverage hits;	
	
	private IDRescorer idrescorer;
	private ContextualCriteria contextualCriteria;
	private AbstractDataset dataset;

	public CrossDomainContextualRecommenderIRStatsCremonesiEvaluator(
			IDRescorer rescorer, ContextualCriteria cc, AbstractDataset dataset) {
		this.idrescorer = rescorer;
		this.contextualCriteria = cc;
		this.dataset = dataset;
		reset();
		
		//this.dataset = dataset;
		//this.contexts = Functions.getDatasetContexts(dataset);
		//this.domains = Functions.getDatasetDomains(dataset);
		random = RandomUtils.getRandom();
	}

	// private final RelevantItemsDataSplitter dataSplitter;

	/*
	 * public GenericRecommenderIRStatsEvaluator() { this(new
	 * GenericRelevantItemsDataSplitter()); }
	 * 
	 * public GenericRecommenderIRStatsEvaluator(RelevantItemsDataSplitter
	 * dataSplitter) { Preconditions.checkNotNull(dataSplitter); random =
	 * RandomUtils.getRandom(); this.dataSplitter = dataSplitter; }
	 */

	public int getNumOfSortedItens() {
		return numOfSortedItens;
	}

	public IDRescorer getIdrescorer() {
		return idrescorer;
	}

	public int getNumItensTraining() {
		return numItensTraining;
	}

	public void setNumItensTraining(int numItensTraining) {
		this.numItensTraining = numItensTraining;
	}

	public int getNumItensProbe() {
		return numItensProbe;
	}

	public void setNumItensProbe(int numItensProbe) {
		this.numItensProbe = numItensProbe;
	}

	public int getNumUsersTraining() {
		return numUsersTraining;
	}

	public void setNumUsersTraining(int numUsersTraining) {
		this.numUsersTraining = numUsersTraining;
	}

	public int getNumUsersProbe() {
		return numUsersProbe;
	}

	public void setNumUsersProbe(int numUsersProbe) {
		this.numUsersProbe = numUsersProbe;
	}
	
	public void setNumRatingsInProbe(int numRatingsInProbe) {
		this.numRatingsInProbe = numRatingsInProbe;
	}
	
	public int getNumRatingsInProbe() {
		return numRatingsInProbe;
	}

	public int getNumOverlapedUsersTraining() {
		return numOverlapedUsersTraining;
	}

	public void setNumOverlapedUsersTraining(int numOverlapedUsersTraining) {
		this.numOverlapedUsersTraining = numOverlapedUsersTraining;
	}

	public ArrayList<CremonesiValues> getCremonesiValuesList() {
		return cremonesiValuesList;
	}

	/*private FastIDSet getRelevantItemsIDs(long userID, int at,
			double relevanceThreshold, DataModel dataModel)
			throws TasteException {
		ContextualUserPreferenceArray prefs = (ContextualUserPreferenceArray) dataModel
				.getPreferencesFromUser(userID);
		FastIDSet relevantItemIDs = new FastIDSet(at);
		prefs.sortByValueReversed();
		for (int i = 0; i < prefs.length() && relevantItemIDs.size() < at; i++) {
			if (prefs.getValue(i) >= relevanceThreshold
					&& this.contextualCriteria
							.containsAllContextualAttributes(prefs
									.getContextualPreferences(i))
					&& !this.idrescorer.isFiltered(prefs.getItemID(i))) {
				relevantItemIDs.add(prefs.getItemID(i));
			}
		}
		return relevantItemIDs;
	}

	private void processOtherUser(long userID, FastIDSet relevantItemIDs,
			FastByIDMap<PreferenceArray> trainingUsers, long otherUserID,
			DataModel dataModel) throws TasteException {
		PreferenceArray prefs2Array = dataModel
				.getPreferencesFromUser(otherUserID);
		// If we're dealing with the very user that we're evaluating for
		// precision/recall,
		if (userID == otherUserID) {
			// then must remove all the test IDs, the "relevant" item IDs
			List<Preference> prefs2 = Lists
					.newArrayListWithCapacity(prefs2Array.length());
			for (Preference pref : prefs2Array) {
				prefs2.add(pref);
			}
			for (Iterator<Preference> iterator = prefs2.iterator(); iterator
					.hasNext();) {
				Preference pref = iterator.next();
				if (relevantItemIDs.contains(pref.getItemID())) {
					iterator.remove();
				}
			}
			if (!prefs2.isEmpty()) {
				trainingUsers.put(otherUserID,
						new ContextualUserPreferenceArray(prefs2));
			}
		} else {
			// otherwise just add all those other user's prefs
			trainingUsers.put(otherUserID, prefs2Array);
		}
	}

	private int min(Collection<Integer> values) {
		int min = 0;
		for (Integer value : values) {
			if (min > value) {
				min = value;
			}
		}

		return min;
	}*/

	public static List<RecommendedItem> getTopItems(long userId, int howMany,
			FastIDSet possibleItemIDs, IDRescorer rescorer,
			Recommender estimator) throws TasteException {
		Preconditions.checkArgument(possibleItemIDs != null,
				"possibleItemIDs is null");
		Preconditions.checkArgument(estimator != null, "estimator is null");

		Queue<RecommendedItem> topItems = new PriorityQueue<RecommendedItem>(
				howMany + 1,
				Collections.reverseOrder(ByValueRecommendedItemComparator
						.getInstance()));
		boolean full = false;
		double lowestTopValue = Double.NEGATIVE_INFINITY;
		for (Long itemID : possibleItemIDs) {
			//long itemID = possibleItemIDs.next();
			if (rescorer == null || !rescorer.isFiltered(itemID)) {
				double preference;
				try {
					preference = estimator.estimatePreference(userId, itemID);
					if(Double.isNaN(preference)){
						preference = 0;
					}
				} catch (NoSuchItemException nsie) {
					continue;
				}
				double rescoredPref = rescorer == null ? preference : rescorer
						.rescore(itemID, preference);
				if (!Double.isNaN(rescoredPref)
						&& (!full || rescoredPref > lowestTopValue)) {
					topItems.add(new GenericRecommendedItem(itemID,
							(float) rescoredPref));
					if (full) {
						topItems.poll();
					} else if (topItems.size() > howMany) {
						full = true;
						topItems.poll();
					}
					lowestTopValue = topItems.peek().getValue();
				}
			}
		}
		int size = topItems.size();
		if (size == 0) {
			return Collections.emptyList();
		}
		List<RecommendedItem> result = Lists.newArrayListWithCapacity(size);
		result.addAll(topItems);
		Collections
				.sort(result, ByValueRecommendedItemComparator.getInstance());
		return result;
	}

	public IRStatistics evaluate(ContextualRecommenderBuilder recommenderBuilder,
			DataModelBuilder dataModelBuilder, DataModel trainingDataModel,
			IDRescorer rescorer, int at, double relevanceThreshold,
			double evaluationPercentage, double trainingPercentage,HashSet<ItemDomain> domains, boolean considerTestedContext)
			throws TasteException {

		reset();
		
		Preconditions.checkArgument(recommenderBuilder != null,
				"recommenderBuilder is null");
		Preconditions.checkArgument(trainingDataModel != null,
				"dataModel is null");
		Preconditions.checkArgument(at >= 1, "at must be at least 1");
		Preconditions.checkArgument(evaluationPercentage > 0.0
				&& evaluationPercentage <= 1.0,
				"Invalid evaluationPercentage: " + evaluationPercentage
						+ ". Must be: 0.0 < evaluationPercentage <= 1.0");

		// int numItems = trainingDataModel.getNumItems();
		
		// RunningAverage fallOut = new FullRunningAverage();
		// RunningAverage nDCG = new FullRunningAverage();
		// int numUsersRecommendedFor = 0;
		// int numUsersWithRecommendations = 0;
		
		trainingDataModel = generateDataModelWithEvaluationPercentage(trainingDataModel,evaluationPercentage);

		FastByIDMap<PreferenceArray> trainingRatings = new FastByIDMap<PreferenceArray>(
				trainingDataModel.getNumUsers());
		FastByIDMap<PreferenceArray> probeRatings = new FastByIDMap<PreferenceArray>(
				trainingDataModel.getNumUsers());
		/*
		 * HashMap<ItemDomain,Integer> numOfRatingsInDomainProbe = new
		 * HashMap<ItemDomain, Integer>(); HashMap<ContextualCriteria,Integer>
		 * numOfRatingsInContextProbe = new HashMap<ContextualCriteria,
		 * Integer>();
		 * 
		 * int numOfRatingsForProbe = (int)
		 * (Functions.numOfRatings(trainingDataModel)*(1-trainingPercentage));
		 * 
		 * for(ItemDomain domain : domains){
		 * numOfRatingsInDomainProbe.put(domain, 0); }
		 * 
		 * for(ContextualCriteria context : contexts){
		 * numOfRatingsInContextProbe.put(context, 0); }
		 * 
		 * int maxNumOfRatingsPerDomain = numOfRatingsForProbe/domains.size();
		 * int maxNumOfRatingsPerContext = numOfRatingsForProbe/contexts.size();
		 */

		generateProbeDataset(trainingPercentage, trainingDataModel,
				trainingRatings, probeRatings);

		
		DataModel trainingModel = dataModelBuilder == null ? new ContextualDataModel(
				trainingRatings) : dataModelBuilder
				.buildDataModel(trainingRatings);
				
		this.numUsersTraining = trainingModel.getNumUsers();
		this.numItensTraining = trainingModel.getNumItems();
		
		this.numOverlapedUsersTraining = Functions.getNumOfUsersAndOverlappedUsers(trainingModel, this.dataset,domains)[1];

		Recommender recommender = recommenderBuilder
				.buildRecommender(trainingModel, contextualCriteria, this.idrescorer, dataset);

		LongPrimitiveIterator it = probeRatings.keySetIterator();
		
		//int hits = 0;
		//int numOfTestedRatings = 0;
		
		Collection<Callable<Void>> estimateCallables = Lists.newArrayList();

		numUsersProbe = 0;
		numRatingsInProbe = 0;

		FastIDSet allItemIDsInProbe = new FastIDSet(this.numItensTraining);
		
		FastIDSet allItemIDsInTest = new FastIDSet(this.numItensTraining);
		
		ArrayList<ContextualPreferenceInterface> prefsForTest = new ArrayList<ContextualPreferenceInterface>();
		
		while (it.hasNext()) {
			
			long userID = it.nextLong();
			
			try{
			
				if(recommenderBuilder.getDataModel().getPreferencesFromUser(userID) == null){
					continue;
				}
			}catch(NoSuchUserException e){
				continue;
			}
			
			
			numUsersProbe++;
			
			ContextualUserPreferenceArray prefs = (ContextualUserPreferenceArray) probeRatings
					.get(userID);

			for (Iterator<Preference> iterator = prefs.iterator(); iterator
					.hasNext();) {
				ContextualPreferenceInterface pref = (ContextualPreferenceInterface) iterator
						.next();
				
				numRatingsInProbe++;
				allItemIDsInProbe.add(pref.getItemID());
				
				boolean sameContext = considerTestedContext ? (this.contextualCriteria != null
						&& this.contextualCriteria.containsAllContextualAttributesIgnoringUnkwnown(pref.getContextualPreferences())) : true;
				
				if(pref.getValue() >= relevanceThreshold && this.idrescorer != null
						&& !this.idrescorer.isFiltered(pref.getItemID())){
					allItemIDsInTest.add(pref.getItemID());
					
					//Test target domain and tested context
					if(sameContext){
						prefsForTest.add(pref);
					}
				}else if(pref.getValue() >= relevanceThreshold && this.idrescorer == null){//For single-domain
					allItemIDsInTest.add(pref.getItemID());
					
					//Test target domain and tested context
					if(sameContext){
						prefsForTest.add(pref);
					}	
				}
				
				
				
				/*if (pref.getValue() == relevanceThreshold && this.idrescorer != null
						&& !this.idrescorer.isFiltered(pref.getItemID())
						&& sameContext) {

					FastIDSet possibleItemIDs = new FastIDSet();

					possibleItemIDs.add(pref.getItemID());

					possibleItemIDs.addAll(getOtherUnratedItemsRandomly(userID,pref.getItemID(),recommenderBuilder.getDataModel(),this.idrescorer));

					
					estimateCallables.add(new PreferenceEstimateCallable(recommender,userID,pref.getItemID(),this.idrescorer,possibleItemIDs,at,pref.getContextualPreferences()));
					
				}*/
			}
			
		}
		
		for(ContextualPreferenceInterface pref : prefsForTest){
			FastIDSet possibleItemIDs = new FastIDSet();

			possibleItemIDs.add(pref.getItemID());

			possibleItemIDs.addAll(getOtherUnratedItemsRandomly(pref.getUserID(),pref.getItemID(),allItemIDsInTest,recommenderBuilder.getDataModel()));

			
			estimateCallables.add(new PreferenceEstimateCallable(recommender,pref.getUserID(),pref.getItemID(),this.idrescorer,possibleItemIDs,at,pref.getContextualPreferences()));
		}
		
		numItensProbe = allItemIDsInProbe.size();
		
		AtomicInteger noEstimateCounter = new AtomicInteger();
		log.info("Beginning evaluation of {} ratings", estimateCallables.size());
		if(estimateCallables.size() == 0){
			return null;
		}
		RunningAverageAndStdDev timing = new FullRunningAverageAndStdDev();
		execute(estimateCallables, noEstimateCounter, timing);
		
		if(this.hits.getCount() >0 && at>0){
			double recallValue = this.hits.getAverage();
			
			this.recall.addDatum(recallValue);
			
			double precisionValue = recallValue/(double) at;
			
			 this.precision.addDatum(precisionValue);
		}		
	 	

		/*
		 * int intersectionSize = 0; List<RecommendedItem> recommendedItems =
		 * recommender.recommend( userID, at, rescorer);
		 * 
		 * long[] primitiveRecommendedItems = new long[recommendedItems.size()];
		 * int counter = 0;
		 * 
		 * for (RecommendedItem recommendedItem : recommendedItems) {
		 * primitiveRecommendedItems[counter++] = recommendedItem.getItemID();
		 * if (relevantItemIDs.contains(recommendedItem.getItemID())) {
		 * intersectionSize++; } }
		 * 
		 * //CremonesiValues rv = new CremonesiValues(relevantItemIDs.toArray(),
		 * primitiveRecommendedItems, userID);
		 * 
		 * //cremonesiValuesList.add(rv);
		 * 
		 * int numRecommendedItems = recommendedItems.size();
		 * 
		 * // Precision if (numRecommendedItems > 0) {
		 * precision.addDatum((double) intersectionSize / (double)
		 * numRecommendedItems); }
		 * 
		 * // Recall recall.addDatum((double) intersectionSize / (double)
		 * numRelevantItems);
		 */

		// Fall-out
		/*
		 * if (numRelevantItems < size) { fallOut.addDatum((double)
		 * (numRecommendedItems - intersectionSize) / (double) (numItems -
		 * numRelevantItems)); }
		 */

		// nDCG
		// In computing, assume relevant IDs have relevance 1 and others 0
		/*
		 * double cumulativeGain = 0.0; double idealizedGain = 0.0; for (int i =
		 * 0; i < numRecommendedItems; i++) { RecommendedItem item =
		 * recommendedItems.get(i); double discount = 1.0 / log2(i + 2.0); //
		 * Classical formulation // says log(i+1), but i // is 0-based here if
		 * (relevantItemIDs.contains(item.getItemID())) { cumulativeGain +=
		 * discount; } // otherwise we're multiplying discount by relevance 0 so
		 * it // doesn't do anything
		 * 
		 * // Ideally results would be ordered with all relevant ones // first,
		 * so this theoretical // ideal list starts with number of relevant
		 * items equal to the // total number of relevant items if (i <
		 * numRelevantItems) { idealizedGain += discount; } } if (idealizedGain
		 * > 0.0) { nDCG.addDatum(cumulativeGain / idealizedGain); }
		 */

		// Reach
		/*
		 * numUsersRecommendedFor++; if (numRecommendedItems > 0) {
		 * numUsersWithRecommendations++; }
		 */

		log.info("Precision/recall: {} / {} ", precision.getAverage(),
				recall.getAverage());

		return new IRStatisticsImplCD(precision.getAverage(),
				recall.getAverage());
	}
	
	private static Collection<Callable<Void>> wrapWithStatsCallables(
			Iterable<Callable<Void>> callables,
			AtomicInteger noEstimateCounter, RunningAverageAndStdDev timing) {
		Collection<Callable<Void>> wrapped = Lists.newArrayList();
		int count = 0;
		for (Callable<Void> callable : callables) {
			boolean logStats = count++ % 1000 == 0; // log every 1000 or so
													// iterations
			wrapped.add(new StatsCallable(callable, logStats, timing,
					noEstimateCounter));
		}
		return wrapped;
	}

	private void execute(Collection<Callable<Void>> estimateCallables,
			AtomicInteger noEstimateCounter, RunningAverageAndStdDev timing) throws TasteException {
		Collection<Callable<Void>> wrappedCallables = wrapWithStatsCallables(
				estimateCallables, noEstimateCounter, timing);
		int numProcessors = Runtime.getRuntime().availableProcessors();
		numProcessors = 3;//deixa um processador livre
	    ExecutorService executor = Executors.newFixedThreadPool(numProcessors);
		/*int numProcessors = 50; GAE
		ThreadFactory tf = ThreadManager.currentRequestThreadFactory();
		ExecutorService executor = Executors.newCachedThreadPool(tf);*/
		log.info("Starting timing of {} tasks in {} threads",
				wrappedCallables.size(), numProcessors);
		try {
			List<Future<Void>> futures = executor.invokeAll(wrappedCallables);
			// Go look for exceptions here, really
			for (Future<Void> future : futures) {
				future.get();
			}

		} catch (InterruptedException ie) {
			throw new TasteException(ie);
		} catch (ExecutionException ee) {
			throw new TasteException(ee.getCause());
		}

		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new TasteException(e.getCause());
		}
		
	}

	private void reset() {
		this.cremonesiValuesList = new ArrayList<CremonesiValues>();
		this.precision = new FullRunningAverage();
		this.recall = new FullRunningAverage();
		this.hits = new FullRunningAverage();
	}

	private DataModel generateDataModelWithEvaluationPercentage(
			DataModel trainingDataModel, double evaluationPercentage) {
		DataModel auxDataModel = trainingDataModel;
		
		if(evaluationPercentage == 1.0){
			return auxDataModel;
		}

		try {
			
			FastByIDMap<PreferenceArray> trainingRatings = new FastByIDMap<PreferenceArray>(
					auxDataModel.getNumUsers());
			
			LongPrimitiveIterator it = auxDataModel.getUserIDs();

			while (it.hasNext()) {

				long userID = it.nextLong();

				if (random.nextDouble() >= evaluationPercentage) {
					continue;
				}
				

				ContextualUserPreferenceArray prefs = (ContextualUserPreferenceArray) auxDataModel
						.getPreferencesFromUser(userID);

				List<ContextualPreferenceInterface> prefs2Training = Lists
						.newArrayListWithCapacity(prefs.length());

				for (Iterator<Preference> iterator = prefs.iterator(); iterator
						.hasNext();) {
					ContextualPreferenceInterface pref = (ContextualPreferenceInterface) iterator
							.next();
						prefs2Training.add(pref);
				}

				if (!prefs2Training.isEmpty()) {
					trainingRatings.put(userID,
							new ContextualUserPreferenceArray(prefs2Training));
				}
			}
			
			DataModel trainingModel =  new ContextualDataModel(trainingRatings);
			return trainingModel;
		}catch(TasteException e){
			e.printStackTrace();
		}
			
			
		
		return auxDataModel;
	}

	private FastIDSet getOtherUnratedItemsRandomly(long testedUserId, long itemId,
			FastIDSet itemsTest, DataModel dataModel) throws TasteException {
		FastIDSet otherItemIds = new FastIDSet();
		FastIDSet possibleItemIdsForTest = new FastIDSet(itemsTest.size());
		
		ContextualUserPreferenceArray prefsTestedUser = (ContextualUserPreferenceArray) dataModel.getPreferencesFromUser(testedUserId);
		
		LongPrimitiveIterator itemsTestIterator = itemsTest.iterator();
		
		while(itemsTestIterator.hasNext()){
			long itemTest = itemsTestIterator.next();
			if(!prefsTestedUser.hasPrefWithItemID(itemTest) && 
					itemTest != itemId){
				possibleItemIdsForTest.add(itemTest);
			}
		}
		
		if(possibleItemIdsForTest.size() < numOfSortedItens){
			throw new TasteException("It was not possible get other "+numOfSortedItens+" items randomly with user "+testedUserId+". Only "+otherItemIds.size()+" were included");
		}else{
			
			HashSet<Integer> indexesRandom = new HashSet<Integer>();
			
			while(indexesRandom.size() < numOfSortedItens){
				int indexRandom = random.nextInt(possibleItemIdsForTest.size());
				indexesRandom.add(indexRandom);
			}
			
			LongPrimitiveIterator possibleItemIdsForTestIterator = possibleItemIdsForTest.iterator();
			
			int index = 0;
			
			while(possibleItemIdsForTestIterator.hasNext()){
				long itemIdForTest = possibleItemIdsForTestIterator.next();
				if(indexesRandom.contains(index)){
					otherItemIds.add(itemIdForTest);
				}
				index++;
			}
			
			/*double percentage = (double) numOfSortedItens/(double)otherItemIds.size();
			LongPrimitiveIterator otherItemIdsIterator = otherItemIds.iterator();
			while(otherItemIds.size() > numOfSortedItens){
				if(otherItemIdsIterator.hasNext()){
					long key = otherItemIdsIterator.next();
					if(random.nextDouble() >= percentage){
						otherItemIds.remove(key);
					}
				}else{
					otherItemIdsIterator = otherItemIds.iterator();
					percentage = (double) numOfSortedItens/(double)otherItemIds.size();
				}
				
			}*/
		}
		
		return otherItemIds;
	}

	private int getItemPosition(List<RecommendedItem> topItems, long itemID) {
		for(int i=0;i<topItems.size();i++){
			if(topItems.get(i).getItemID() == itemID){
				return (i+1);
			}
		}
		return -1;
	}


	private final class PreferenceEstimateCallable implements Callable<Void> {

		private final Recommender recommender;
		private final long testUserID;
		private final long testItemID;
		private final IDRescorer rescorer;
		private final FastIDSet possibleItemIDs;
		private final int at;
		private final long prefContexts[];

		public PreferenceEstimateCallable(Recommender recommender,
				long testUserID, long testItemID, IDRescorer rescorer, FastIDSet possibleItemIDs, int at, long[] ls) {
			this.recommender = recommender;
			this.testUserID = testUserID;
			this.testItemID = testItemID;
			this.rescorer = rescorer;
			this.possibleItemIDs = possibleItemIDs;
			this.at = at;
			this.prefContexts = ls;
		}



		@Override
		public Void call() throws TasteException {
			long start = System.currentTimeMillis();
			
			List<RecommendedItem> topItems = getTopItems(testUserID, numOfSortedItens+1, possibleItemIDs, rescorer, recommender);

			int position = getItemPosition(topItems,testItemID);
			
			if(position <= at){
				hits.addDatum(1.0);
			}else{
				hits.addDatum(0.0);
			}
			
			CremonesiValues cv = new CremonesiValues(testItemID, position, testUserID,prefContexts);
			cremonesiValuesList.add(cv);
	
			long end = System.currentTimeMillis();

			log.debug("Evaluated with user {} for item {} in {}ms", testUserID, testItemID,end - start);
			return null;
		}

	}

	private void generateProbeDataset(double trainingPercentage,
			DataModel trainingDataModel,
			FastByIDMap<PreferenceArray> trainingRatings,
			FastByIDMap<PreferenceArray> probeRatings) {

		DataModel auxDataModel = trainingDataModel;

		HashMap<ItemDomain,HashMap<ContextualCriteria,ArrayList<ContextualPreferenceInterface>>> prefsPerDomainContext = new HashMap<ItemDomain, HashMap<ContextualCriteria,ArrayList<ContextualPreferenceInterface>>>();
		//HashMap<ContextualCriteria,ContextualPreferenceInterface> prefsPerContext = new HashMap<ContextualCriteria, ContextualPreferenceInterface>();

		try {
			LongPrimitiveIterator it = auxDataModel.getUserIDs();

			while (it.hasNext()) {

				long userID = it.nextLong();

				//long start = System.currentTimeMillis();

				ContextualUserPreferenceArray prefs = (ContextualUserPreferenceArray) auxDataModel
						.getPreferencesFromUser(userID);

				
				for (Iterator<Preference> iterator = prefs.iterator(); iterator
						.hasNext();) {
					ContextualPreferenceInterface pref = (ContextualPreferenceInterface) iterator
							.next();

					ItemDomain itemDomain =
							 dataset.getItemInformationByID(pref
							 .getItemID()).getItemDomain();
					
					ContextualCriteria cc = new
							  ContextualCriteria(pref.getContextualPreferences());
					
					if(prefsPerDomainContext.containsKey(itemDomain)){
						HashMap<ContextualCriteria,ArrayList<ContextualPreferenceInterface>> prefPerContextTest = 
								prefsPerDomainContext.get(itemDomain);
						if(prefPerContextTest.containsKey(cc)){
							prefPerContextTest.get(cc).add(pref);
						}else{
							ArrayList<ContextualPreferenceInterface> newPrefs = new ArrayList<ContextualPreferenceInterface>();
							newPrefs.add(pref);
							prefPerContextTest.put(cc, newPrefs);
						}
					}else{
						HashMap<ContextualCriteria,ArrayList<ContextualPreferenceInterface>> newContextMap = new HashMap<ContextualCriteria, ArrayList<ContextualPreferenceInterface>>();
						ArrayList<ContextualPreferenceInterface> newPrefs = new ArrayList<ContextualPreferenceInterface>();
						newPrefs.add(pref);
						newContextMap.put(cc, newPrefs);
						prefsPerDomainContext.put(itemDomain, newContextMap);
					}
					
				}
				
			}
			
			HashMap<Long,List<ContextualPreferenceInterface>> userPrefsMapTraining = new HashMap<Long, List<ContextualPreferenceInterface>>();
			HashMap<Long,List<ContextualPreferenceInterface>> userPrefsMapProbe = new HashMap<Long, List<ContextualPreferenceInterface>>();
			
			// Opcional: manter uma proporcao igualitária (OU MINIMA) entre dominios e
						// contextos no probe
			for(ItemDomain domain : prefsPerDomainContext.keySet()){
				HashMap<ContextualCriteria,ArrayList<ContextualPreferenceInterface>> contextsPerDomain = 
						prefsPerDomainContext.get(domain);
				for(ContextualCriteria context : contextsPerDomain.keySet()){
					ArrayList<ContextualPreferenceInterface> prefs = contextsPerDomain.get(context);
					
					if(prefs.size() > 0){
						int numberOfRandomRatings = (int)((1-trainingPercentage) * prefs.size()) + 1;
						
						HashSet<Integer> indexesRandom = new HashSet<Integer>();
						
						while(indexesRandom.size() < numberOfRandomRatings){
							int indexRandom = random.nextInt(prefs.size());
							indexesRandom.add(indexRandom);
						}
						
						for(int i=0;i<prefs.size();i++){
							ContextualPreferenceInterface pref = prefs.get(i);
							long user = pref.getUserID();
							if (!indexesRandom.contains(i)) {
								if(userPrefsMapTraining.containsKey(user)){
									List<ContextualPreferenceInterface> userPrefs = userPrefsMapTraining.get(user);
									userPrefs.add(pref);
								}else{
									List<ContextualPreferenceInterface> prefs2Training = new ArrayList<ContextualPreferenceInterface>();
									prefs2Training.add(pref);
									userPrefsMapTraining.put(user,prefs2Training);
//									trainingRatings.put(user,  new ContextualUserPreferenceArray(
//											prefs2Training));
								}
							}else{
								if(userPrefsMapProbe.containsKey(user)){
									List<ContextualPreferenceInterface> userPrefs = userPrefsMapProbe.get(user);
									userPrefs.add(pref);
								}else{
									List<ContextualPreferenceInterface> prefs2Probe = new ArrayList<ContextualPreferenceInterface>();
									prefs2Probe.add(pref);
									userPrefsMapProbe.put(user, prefs2Probe);
//									probeRatings.put(user,  new ContextualUserPreferenceArray(
//											prefs2Probe));
								}
							}
						}
					}
					
					
				}
			}
			
			/*List<ContextualPreferenceInterface> prefs2Probe = Lists
					.newArrayListWithCapacity(prefs.length());

			List<ContextualPreferenceInterface> prefs2Training = Lists
					.newArrayListWithCapacity(prefs.length());*/
			
			int counterTraining = 0;
			int counterProbe = 0;
			
			for(Long user : userPrefsMapTraining.keySet()){
				List<ContextualPreferenceInterface> userPrefs = userPrefsMapTraining.get(user);
				if(!userPrefs.isEmpty()){
					counterTraining = counterTraining + userPrefs.size();
					trainingRatings.put(user,
							new ContextualUserPreferenceArray(userPrefs));
				}
			}
			
			for(Long user : userPrefsMapProbe.keySet()){
				List<ContextualPreferenceInterface> userPrefs = userPrefsMapProbe.get(user);
				if(!userPrefs.isEmpty()){
					counterProbe = counterProbe + userPrefs.size();
					probeRatings.put(user,
							new ContextualUserPreferenceArray(userPrefs));
				}
			}
			
			//System.out.println(counterTraining+" "+counterProbe);
			/*if (!prefs2Probe.isEmpty()) {
				probeRatings.put(userID, new ContextualUserPreferenceArray(
						prefs2Probe));
			}
			if (!prefs2Training.isEmpty()) {
				trainingRatings.put(userID,
						new ContextualUserPreferenceArray(prefs2Training));
			}*/


		} catch (TasteException e) {
			e.printStackTrace();
		}
		// }
	}

	/*private FastByIDMap<PreferenceArray> copyRatings(DataModel trainingDataModel) {

		FastByIDMap<PreferenceArray> trainingCopied = null;
		try {
			trainingCopied = new FastByIDMap<PreferenceArray>(
					trainingDataModel.getNumUsers());

			LongPrimitiveIterator it = trainingDataModel.getUserIDs();

			while (it.hasNext()) {
				long userID = it.nextLong();

				PreferenceArray prefs2Array = trainingDataModel
						.getPreferencesFromUser(userID);

				trainingCopied.put(userID, prefs2Array);

			}
		} catch (TasteException e) {
			e.printStackTrace();
		}

		return trainingCopied;

	}*/

	@Override
	public IRStatistics evaluate(ContextualRecommenderBuilder recommenderBuilder,
			DataModelBuilder dataModelBuilder, DataModel trainingDataModel,
			IDRescorer rescorer, int at, double relevanceThreshold,
			double evaluationPercentage) throws TasteException {
		return null;
	}

	/*private static double computeThreshold(PreferenceArray prefs) {
		if (prefs.length() < 2) {
			// Not enough data points -- return a threshold that allows
			// everything
			return Double.NEGATIVE_INFINITY;
		}
		RunningAverageAndStdDev stdDev = new FullRunningAverageAndStdDev();
		int size = prefs.length();
		for (int i = 0; i < size; i++) {
			stdDev.addDatum(prefs.getValue(i));
		}
		return stdDev.getAverage() + stdDev.getStandardDeviation();
	}

	private static double log2(double value) {
		return Math.log(value) / LOG2;
	}*/

}
