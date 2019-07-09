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
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.eval.RelevantItemsDataSplitter;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.eval.IRStatisticsImpl;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
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
import br.cin.tbookmarks.recommender.database.contextual.ContextualUserPreferenceArray;

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
public final class CrossDomainContextualRecommenderIRStatsEvaluator implements
		ContextualRecommenderIRStatsEvaluator {

	private static final Logger log = LoggerFactory
			.getLogger(CrossDomainContextualRecommenderIRStatsEvaluator.class);

	private static final double LOG2 = Math.log(2.0);

	/**
	 * Pass as "relevanceThreshold" argument to
	 * {@link #evaluate(ContextualRecommenderBuilder, DataModelBuilder, DataModel, IDRescorer, int, double, double)}
	 * to have it attempt to compute a reasonable threshold. Note that this will
	 * impact performance.
	 */
	public static final double CHOOSE_THRESHOLD = Double.NaN;

	private final Random random;

	private ArrayList<RankingValues> rankValuesList;
	private RunningAverage precision;
	private RunningAverage recall;
	private RunningAverage fallOut;
	private RunningAverage nDCG;
	private int numUsersRecommendedFor;
	private int numUsersWithRecommendations;
	
	
	private IDRescorer idrescorer;
	private ContextualCriteria contextualCriteria;
	private AbstractDataset dataset;

	// private final RelevantItemsDataSplitter dataSplitter;

	/*
	 * public GenericRecommenderIRStatsEvaluator() { this(new
	 * GenericRelevantItemsDataSplitter()); }
	 * 
	 * public GenericRecommenderIRStatsEvaluator(RelevantItemsDataSplitter
	 * dataSplitter) { Preconditions.checkNotNull(dataSplitter); random =
	 * RandomUtils.getRandom(); this.dataSplitter = dataSplitter; }
	 */
	
	public IDRescorer getIdrescorer() {
		return idrescorer;
	}
	
	public ArrayList<RankingValues> getRankValuesList() {
		return rankValuesList;
	}

	public CrossDomainContextualRecommenderIRStatsEvaluator(IDRescorer rescorer, ContextualCriteria cc,AbstractDataset dataset) {
		this.idrescorer = rescorer;
		this.contextualCriteria = cc;
		this.dataset = dataset;
		
		reset();
		
		random = RandomUtils.getRandom();
	}

	private FastIDSet getRelevantItemsIDs(long userID, int at,
			double relevanceThreshold, DataModel dataModel)
			throws TasteException {
		ContextualUserPreferenceArray prefs = (ContextualUserPreferenceArray) dataModel.getPreferencesFromUser(userID);
		FastIDSet relevantItemIDs = new FastIDSet(at);
		prefs.sortByValueReversed();
		for (int i = 0; i < prefs.length() && relevantItemIDs.size() < at; i++) {
			if (prefs.getValue(i) >= relevanceThreshold &&
					this.contextualCriteria.containsAllContextualAttributesIgnoringUnkwnown(prefs.getContextualPreferences(i))
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
				trainingUsers.put(otherUserID, new ContextualUserPreferenceArray(
						prefs2));
			}
		} else {
			// otherwise just add all those other user's prefs
			trainingUsers.put(otherUserID, prefs2Array);
		}
	}

	@Override
	public IRStatistics evaluate(ContextualRecommenderBuilder recommenderBuilder,
			DataModelBuilder dataModelBuilder, DataModel dataModel,
			IDRescorer rescorer, int at, double relevanceThreshold,
			double evaluationPercentage) throws TasteException {

		reset();
		Preconditions.checkArgument(recommenderBuilder != null,
				"recommenderBuilder is null");
		Preconditions.checkArgument(dataModel != null, "dataModel is null");
		Preconditions.checkArgument(at >= 1, "at must be at least 1");
		Preconditions.checkArgument(evaluationPercentage > 0.0
				&& evaluationPercentage <= 1.0,
				"Invalid evaluationPercentage: " + evaluationPercentage
						+ ". Must be: 0.0 < evaluationPercentage <= 1.0");

		int numItems = dataModel.getNumItems();

		LongPrimitiveIterator it = dataModel.getUserIDs();
		while (it.hasNext()) {

			long userID = it.nextLong();

			if (random.nextDouble() >= evaluationPercentage) {
				// Skipped
				continue;
			}

			long start = System.currentTimeMillis();

			PreferenceArray prefs = dataModel.getPreferencesFromUser(userID);

			// List some most-preferred items that would count as (most)
			// "relevant" results
			double theRelevanceThreshold = Double.isNaN(relevanceThreshold) ? computeThreshold(prefs)
					: relevanceThreshold;
			FastIDSet relevantItemIDs = getRelevantItemsIDs(
					userID, at, theRelevanceThreshold, dataModel);

			int numRelevantItems = relevantItemIDs.size();
			if (numRelevantItems <= 0) {
				continue;
			}

			FastByIDMap<PreferenceArray> trainingUsers = new FastByIDMap<PreferenceArray>(
					dataModel.getNumUsers());
			LongPrimitiveIterator it2 = dataModel.getUserIDs();
			while (it2.hasNext()) {
				processOtherUser(userID, relevantItemIDs,trainingUsers, it2.nextLong(), dataModel);
			}

			DataModel trainingModel = dataModelBuilder == null ? new ContextualDataModel(
					trainingUsers) : dataModelBuilder
					.buildDataModel(trainingUsers);
			try {
				trainingModel.getPreferencesFromUser(userID);
			} catch (NoSuchUserException nsee) {
				continue; // Oops we excluded all prefs for the user -- just
							// move on
			}

			int size = numRelevantItems
					+ trainingModel.getItemIDsFromUser(userID).size();
			if (size < 2 * at) {
				// Really not enough prefs to meaningfully evaluate this user
				continue;
			}

			Recommender recommender = recommenderBuilder
					.buildRecommender(trainingModel, contextualCriteria, rescorer, null);

			int intersectionSize = 0;
			List<RecommendedItem> recommendedItems = recommender.recommend(
					userID, at, rescorer);
			
			long[] primitiveRecommendedItems = new long[recommendedItems.size()];
			int counter = 0;
			
			for (RecommendedItem recommendedItem : recommendedItems) {
				primitiveRecommendedItems[counter++] = recommendedItem.getItemID();
				if (relevantItemIDs.contains(recommendedItem.getItemID())) {
					intersectionSize++;
				}
			}
			
			RankingValues rv = new RankingValues(relevantItemIDs.toArray(), primitiveRecommendedItems, userID);
			
			rankValuesList.add(rv);

			int numRecommendedItems = recommendedItems.size();

			// Precision
			if (numRecommendedItems > 0) {
				precision.addDatum((double) intersectionSize
						/ (double) numRecommendedItems);
			}

			// Recall
			recall.addDatum((double) intersectionSize
					/ (double) numRelevantItems);

			// Fall-out
			if (numRelevantItems < size) {
				fallOut.addDatum((double) (numRecommendedItems - intersectionSize)
						/ (double) (numItems - numRelevantItems));
			}

			// nDCG
			// In computing, assume relevant IDs have relevance 1 and others 0
			double cumulativeGain = 0.0;
			double idealizedGain = 0.0;
			for (int i = 0; i < numRecommendedItems; i++) {
				RecommendedItem item = recommendedItems.get(i);
				double discount = 1.0 / log2(i + 2.0); // Classical formulation
														// says log(i+1), but i
														// is 0-based here
				if (relevantItemIDs.contains(item.getItemID())) {
					cumulativeGain += discount;
				}
				// otherwise we're multiplying discount by relevance 0 so it
				// doesn't do anything

				// Ideally results would be ordered with all relevant ones
				// first, so this theoretical
				// ideal list starts with number of relevant items equal to the
				// total number of relevant items
				if (i < numRelevantItems) {
					idealizedGain += discount;
				}
			}
			if (idealizedGain > 0.0) {
				nDCG.addDatum(cumulativeGain / idealizedGain);
			}

			// Reach
			numUsersRecommendedFor++;
			if (numRecommendedItems > 0) {
				numUsersWithRecommendations++;
			}

			long end = System.currentTimeMillis();

			log.info("Evaluated with user {} in {}ms", userID, end - start);
			log.info(
					"Precision/recall/fall-out/nDCG/reach: {} / {} / {} / {} / {}",
					precision.getAverage(), recall.getAverage(),
					fallOut.getAverage(), nDCG.getAverage(),
					(double) numUsersWithRecommendations
							/ (double) numUsersRecommendedFor);
		}

		return new IRStatisticsImplCD(precision.getAverage(),
				recall.getAverage(), fallOut.getAverage(), nDCG.getAverage(),
				(double) numUsersWithRecommendations
						/ (double) numUsersRecommendedFor);
	}

	private void reset() {
		this.rankValuesList = new ArrayList<RankingValues>();
		this.precision = new FullRunningAverage();
		this.recall = new FullRunningAverage();
		this.fallOut = new FullRunningAverage();
		this.nDCG = new FullRunningAverage();
		this.numUsersRecommendedFor = 0;
		this.numUsersWithRecommendations = 0;
	}

	private static double computeThreshold(PreferenceArray prefs) {
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
	}

}
