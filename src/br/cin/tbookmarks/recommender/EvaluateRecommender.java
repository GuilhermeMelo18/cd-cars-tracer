package br.cin.tbookmarks.recommender;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.common.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.cin.tbookmarks.client.Result;
import br.cin.tbookmarks.recommender.algorithms.ContextualRecommenderBuilder;
import br.cin.tbookmarks.recommender.algorithms.PostFilteringContextualBuildRecommender;
import br.cin.tbookmarks.recommender.algorithms.PreFilteringContextualBuildRecommenderByron;
import br.cin.tbookmarks.recommender.algorithms.RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo;
import br.cin.tbookmarks.recommender.algorithms.RecommenderBuilderUserBasedTracer;
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
import br.cin.tbookmarks.recommender.database.contextual.TaskContextualAttribute;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.evaluation.AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain;
import br.cin.tbookmarks.recommender.evaluation.ContextualRecommenderEvaluator;
import br.cin.tbookmarks.recommender.evaluation.CremonesiValues;
import br.cin.tbookmarks.recommender.evaluation.CrossDomainContextualRecommenderIRStatsCremonesiEvaluator;
import br.cin.tbookmarks.recommender.evaluation.CrossDomainContextualRecommenderIRStatsEvaluator;
import br.cin.tbookmarks.recommender.evaluation.EvaluationConfig;
import br.cin.tbookmarks.recommender.evaluation.MAEAndRMSERecommenderEvaluatorCrossDomain;
import br.cin.tbookmarks.recommender.evaluation.MAEAndRMSERecommenderEvaluatorTracer;
import br.cin.tbookmarks.recommender.evaluation.PredictionValues;
import br.cin.tbookmarks.recommender.evaluation.RMSRecommenderEvaluatorCrossDomain;
import br.cin.tbookmarks.recommender.similarity.EuclideanDistanceContextualSimilarity;
import br.cin.tbookmarks.recommender.similarity.ItemDomainRescorer;
import br.cin.tbookmarks.util.Functions;


public class EvaluateRecommender {
	
	private static final Logger log = LoggerFactory.getLogger(EvaluateRecommender.class);
	
	private List<Result> results;
	
	private DataModel model;
	private static AbstractDataset dataset;
	
	private DataModel probeModel;
	private AbstractDataset probeDataset;
	
	private ArrayList<ContextualRecommenderBuilder> recommenders;
	private ContextualCriteria contextualCriteriaEval;
	
	private EvaluationConfig config;
	
	
	
	//private StringBuffer evaluationResults = new StringBuffer();
	
	public EvaluateRecommender(AbstractDataset dataset, ContextualCriteria contextualAttributes, ArrayList<ContextualRecommenderBuilder> recommenders, EvaluationConfig config) {
		this.dataset = dataset;
		model = this.dataset.getModel();
		
		this.config = config;
		
		//this.recommenders = new Recommenders(this.dataset,contextualAttributes);
		this.contextualCriteriaEval = contextualAttributes;
		this.recommenders = recommenders;
		this.results = new ArrayList<Result>();
	}
	
	public EvaluateRecommender(AbstractDataset trainingDataset,AbstractDataset probeDataset, ContextualCriteria contextualAttributes, ArrayList<ContextualRecommenderBuilder> recommenders, EvaluationConfig config) {
		this.dataset =trainingDataset;
		this.model = this.dataset.getModel();
		
		this.config = config;
	
		this.probeDataset =probeDataset;
		this.probeModel = this.probeDataset.getModel();
		
		this.contextualCriteriaEval = contextualAttributes;
		this.recommenders = recommenders;
		this.results = new ArrayList<Result>();
	}
	
	private void showDataModelParameters(DataModel datamodel,HashSet<ItemDomain> domains){
		log.info("Number of ratings: "+Functions.numOfRatings(datamodel));
		log.info("Number of items per domain: ");
		Functions.printNumOfItemsPerDomain(datamodel);
		Functions.getNumOfUsersAndOverlappedUsers(datamodel,this.dataset,domains);
	}
	
	public List<Result> getResults() {
		return results;
	}
	
	private double evaluateAndSetResultParameters(DataModel model, Result r, ContextualRecommenderEvaluator evaluator, ContextualRecommenderBuilder recommenderBuilder,HashSet<ItemDomain> domains) throws TasteException{
		double result = evaluator.evaluate(recommenderBuilder, null, model,this.config.getTrainingPercentage(), this.config.getDatasetPercentage());
		//showDataModelParameters(model);
		
		setNumItemInfo(r, model,domains);
		
		
		return result;
		//int numOfRatings = Functions.numOfRatings(model);
		//log.info("num ratings "+numOfRatings);
	}
	
	public void evaluateRecommender(ContextualRecommenderEvaluator evaluator, int trial,HashSet<ItemDomain> domains) throws TasteException{
		
		//log.info("\n"+evaluator);
		//this.evaluationResults.append(evaluator+"\n");
		for (ContextualRecommenderBuilder recommenderBuilder : this.recommenders) {
			if(this.config.isEnableFixedTestSeed()){
				RandomUtils.useTestSeed();
			}
			
			Result r = new Result();
			r.setTrial(trial);
			
			double result = -999;
			
			long timeMilis = System.currentTimeMillis();
			
			/*if(recommenderBuilder instanceof PreFilteringContextualBuildRecommender){
				DataModel contextualDM = ((PreFilteringContextualBuildRecommender) recommenderBuilder).preFilterDataModel(this.model);
				
				result = evaluateAndSetResultParameters(contextualDM,r,evaluator,recommenderBuilder);
				
			}else if(recommenderBuilder instanceof PostFilteringContextualBuildRecommender){
				((PostFilteringContextualBuildRecommender) recommenderBuilder).setContextAndDataset(this.contextualCriteriaEval, this.dataset);
				result = evaluateAndSetResultParameters(this.model,r,evaluator,recommenderBuilder);
			}else{*/
				result = evaluateAndSetResultParameters(this.model,r,evaluator,recommenderBuilder,domains);
				
			//}
			//log.info(recommenderBuilder+": "+result);
			if(evaluator instanceof AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain){
				
				AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain eval = (AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain)evaluator;
				r.setMaeValue(result);
				r.setTotalOfTestRatings(eval.getTotalOfTestRatings());
				r.setTotalOfTrainingRatingsFromSource(eval.getTotalOfTrainingRatingsFromSource());
				r.setTotalOfTrainingRatingsFromTargetWithContext(eval.getTotalOfTrainingRatingsFromTargetWithContext());
				r.setTotalOfTrainingRatingsFromTargetWithoutContext(eval.getTotalOfTrainingRatingsFromTargetWithoutContext());
				
			}else if(evaluator instanceof RMSRecommenderEvaluatorCrossDomain){
				RMSRecommenderEvaluatorCrossDomain eval = (RMSRecommenderEvaluatorCrossDomain)evaluator;
				r.setRmseValue(result);
				r.setTotalOfTestRatings(eval.getTotalOfTestRatings());
				r.setTotalOfTrainingRatingsFromSource(eval.getTotalOfTrainingRatingsFromSource());
				r.setTotalOfTrainingRatingsFromTargetWithContext(eval.getTotalOfTrainingRatingsFromTargetWithContext());
				r.setTotalOfTrainingRatingsFromTargetWithoutContext(eval.getTotalOfTrainingRatingsFromTargetWithoutContext());
				
			}else if(evaluator instanceof MAEAndRMSERecommenderEvaluatorCrossDomain){
				MAEAndRMSERecommenderEvaluatorCrossDomain eval = (MAEAndRMSERecommenderEvaluatorCrossDomain)evaluator;
				r.setPredictionValues(eval.getValues());
				r.setMaeValue(eval.getMAEResult());
				r.setRmseValue(eval.getRMSEResult());
				r.setTotalOfTestRatings(eval.getTotalOfTestRatings());
				r.setTotalOfTrainingRatingsFromSource(eval.getTotalOfTrainingRatingsFromSource());
				r.setTotalOfTrainingRatingsFromTargetWithContext(eval.getTotalOfTrainingRatingsFromTargetWithContext());
				r.setTotalOfTrainingRatingsFromTargetWithoutContext(eval.getTotalOfTrainingRatingsFromTargetWithoutContext());
				r.setNotEvaluateTestRatings(eval.getNoEstimateCounter());
				
			}else if(evaluator instanceof MAEAndRMSERecommenderEvaluatorTracer) {
				
				MAEAndRMSERecommenderEvaluatorTracer eval = (MAEAndRMSERecommenderEvaluatorTracer)evaluator;
				r.setMaeValue(eval.getMAEResult());
				r.setRmseValue(eval.getRMSEResult());
				r.setMAEValidate(eval.getMAEValidate());
				r.setRMSEValidate(eval.getRMSEValidate());
				r.setMAEPosFilter(eval.getMAEPosFilter());
				r.setRMSEPosFilter(eval.getRMSEPosFilter());
				r.setTotalOfTestRatings(eval.getTotalOfTestRatings());
				r.setTotalOfTrainingRatingsFromSource(eval.getTotalOfTrainingRatingsFromSource());
				r.setTotalOfTrainingRatingsFromTarget(eval.getTotalOfTrainingRatingsFromTarget());
				r.setPredictionValues(eval.getValues());
				r.setPredictionValuesPosFilter(eval.getValuesPosFilter());
				r.setNotEvaluateTestRatings(eval.getNoEstimateCounter());
	
			}
			
			r.setExecutionTime(System.currentTimeMillis()-timeMilis);
			r.setDate(new Date());
			r.setAlgorithmName(recommenderBuilder.toString());
			
			this.results.add(r);
			
			log.info(recommenderBuilder.getClass().getSimpleName().toString()+" MAE: "+r.getMaeValue());
			log.info(recommenderBuilder.getClass().getSimpleName().toString()+" RMSE: "+r.getRmseValue());
		}		
		
	}
	
	public void evaluateRecommenderIRStats(CrossDomainContextualRecommenderIRStatsEvaluator evaluator,int trial,HashSet<ItemDomain> domains) throws TasteException{
		for (ContextualRecommenderBuilder recommenderBuilder : this.recommenders) {
			if(this.config.isEnableFixedTestSeed()){
				RandomUtils.useTestSeed();
			}	
			
			Result r = new Result();
			r.setTrial(trial);
			
			IRStatistics result;
			
			long timeMilis = System.currentTimeMillis();
			
			/*if(recommenderBuilder instanceof PreFilteringContextualBuildRecommender){
				DataModel contextualDM = ((PreFilteringContextualBuildRecommender) recommenderBuilder).preFilterDataModel(this.model,this.contextualCriteriaEval);
				
				
				
				result = evaluator.evaluate(
						recommenderBuilder, null, contextualDM, evaluator.getIdrescorer() ,  this.config.getTop_n(),
						this.config.getRelevantThresholdPrecisionRecall(),
						this.config.getDatasetPercentage());
				
				setNumItemInfo(r, contextualDM);
				
			}else*/ //if(recommenderBuilder instanceof PostFilteringContextualBuildRecommender){
//				((PostFilteringContextualBuildRecommender) recommenderBuilder).setContextAndDataset(this.contextualCriteriaEval, this.dataset);
//				
//				setNumItemInfo(r, this.model);
//				
//				result = evaluator.evaluate(
//						recommenderBuilder, null, this.model, evaluator.getIdrescorer() , this.config.getTop_n(),
//						this.config.getRelevantThresholdPrecisionRecall(),
//						this.config.getDatasetPercentage());
			//}else{
				
				result = evaluator.evaluate(
						recommenderBuilder, null, this.model, evaluator.getIdrescorer() ,  this.config.getTop_n(),
						this.config.getRelevantThresholdPrecisionRecall(),
						this.config.getDatasetPercentage());
				
				setNumItemInfo(r, recommenderBuilder.getDataModel(),domains);
				
			//}
				r.setRankingValues(evaluator.getRankValuesList());
				r.setPrecisionValue(result.getPrecision());
				r.setRecallValue(result.getRecall());
				r.setF1MeasureValue(result.getF1Measure());
				r.setFallOutValue(result.getFallOut());
				r.setNDCGValue(result.getNormalizedDiscountedCumulativeGain());
				r.setNumOfTestedUsers(r.getRankingValues().size());
				
			
			
			r.setExecutionTime(System.currentTimeMillis()-timeMilis);
			r.setDate(new Date(new Long(timeMilis)*1000));
			
			r.setAlgorithmName(recommenderBuilder.toString());
			
			this.results.add(r);
			
			log.info(recommenderBuilder.getClass().getSimpleName().toString()+" Precision: "+result.getPrecision()+" at "+config.getTop_n());
			log.info(recommenderBuilder.getClass().getSimpleName().toString()+" Recall: "+result.getRecall()+" at "+config.getTop_n());
			log.info(recommenderBuilder.getClass().getSimpleName().toString()+" F1Measure: "+result.getF1Measure()+" at "+config.getTop_n());
		}		
	}
	
	public void evaluateRecommenderIRCremonesiStats(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator evaluator,int trial, ItemDomain target, ContextualCriteria criteria, HashSet<ItemDomain> domains, boolean balanceContextInProbe) throws TasteException{
		for (ContextualRecommenderBuilder recommenderBuilder : this.recommenders) {
			if(this.config.isEnableFixedTestSeed()){
				RandomUtils.useTestSeed();
			}	
			
			Result r = new Result();
			r.setTrial(trial);
			
			IRStatistics result;
			
			long timeMilis = System.currentTimeMillis();
			
			/*if(recommenderBuilder instanceof PreFilteringContextualBuildRecommender){
				DataModel contextualDM = ((PreFilteringContextualBuildRecommender) recommenderBuilder).preFilterDataModel(this.model,this.contextualCriteriaEval);
				
				//setNumItemInfo(r, contextualDM);
				
				result = evaluator.evaluate(
						recommenderBuilder, null, contextualDM, evaluator.getIdrescorer() , this.config.getTop_n(),
						this.config.getRelevantThresholdPrecisionRecall(),
						this.config.getDatasetPercentage(),this.config.getTrainingPercentage());
				
			}else if(recommenderBuilder instanceof PostFilteringContextualBuildRecommender){
				((PostFilteringContextualBuildRecommender) recommenderBuilder).setContextAndDataset(this.contextualCriteriaEval, this.dataset);
				
				//setNumItemInfo(r, this.model);
				
				result = evaluator.evaluate(
						recommenderBuilder, null, this.model, evaluator.getIdrescorer() , this.config.getTop_n(),
						this.config.getRelevantThresholdPrecisionRecall(),
						this.config.getDatasetPercentage(),this.config.getTrainingPercentage());
			}else{*/
				
				//setNumItemInfo(r, this.model);
				
				result = evaluator.evaluate(
						recommenderBuilder, null, this.model, evaluator.getIdrescorer() , this.config.getTop_n(),
						this.config.getRelevantThresholdPrecisionRecall(),
						this.config.getDatasetPercentage(),this.config.getTrainingPercentage(),domains,balanceContextInProbe);
				if(result == null){
					//System.out.println("PULOU");
					break;
				}
	
				
			//}
				r.setCremonesiValues(evaluator.getCremonesiValuesList());
				r.setPrecisionValue(result.getPrecision());
				r.setRecallValue(result.getRecall());
				r.setF1MeasureValue(result.getF1Measure());
				//r.setFallOutValue(result.getFallOut());
				//r.setNDCGValue(result.getNormalizedDiscountedCumulativeGain());
				r.setTotalOfTestRatings(r.getCremonesiValues().size());
				r.setNumOfItens(evaluator.getNumItensTraining());
				r.setNumOfUsers(evaluator.getNumUsersTraining());
				r.setNumOfOverlappedUsers(evaluator.getNumOverlapedUsersTraining());
				r.setNumOfItensInProbe(evaluator.getNumItensProbe());
				r.setNumOfUsersInProbe(evaluator.getNumUsersProbe());
				r.setNumOfRatingsInProbe(evaluator.getNumRatingsInProbe());
				r.setNumOfSortedItensCremonesi(evaluator.getNumOfSortedItens());
				r.setTopN(this.config.getTop_n());
				
			
			
			r.setExecutionTime(System.currentTimeMillis()-timeMilis);
			r.setDate(new Date(new Long(timeMilis)*1000));
			
			r.setAlgorithmName(recommenderBuilder.toString());
			
			this.results.add(r);
			
			log.info(recommenderBuilder.getClass().getSimpleName().toString()+" Precision: "+result.getPrecision()+" at "+config.getTop_n());
			log.info(recommenderBuilder.getClass().getSimpleName().toString()+" Recall: "+result.getRecall()+" at "+config.getTop_n());
			log.info(recommenderBuilder.getClass().getSimpleName().toString()+" F1Measure: "+result.getF1Measure()+" at "+config.getTop_n());
		}		
	}

	private void setNumItemInfo(Result r, DataModel contextualDM,HashSet<ItemDomain> domains)
			throws TasteException {
		int numOfUsers = contextualDM.getNumUsers();
		r.setNumOfUsers(numOfUsers);
		
		int numOfItens = contextualDM.getNumItems();
		r.setNumOfItens(numOfItens);
		
		int info[] = Functions.getNumOfUsersAndOverlappedUsers(contextualDM,this.dataset,domains);
		r.setNumOfOverlappedUsers(info[1]);
	}
	
	private static void evaluateSingleDomain(EvaluationConfig configuration, ArrayList<ContextualRecommenderBuilder> recommenders, int trial, double userOverlapLevel, ItemDomain sourceDomain, ItemDomain targetDomain, Class<?> evaluator) {

		List<Result> resultsEval = new ArrayList<Result>();
		
		try {
						
			AmazonCrossDataset dataset = (AmazonCrossDataset)AmazonCrossDataset.getInstance(userOverlapLevel,sourceDomain,targetDomain,true); //cross-domain
			//System.out.println(Functions.numOfRatings(dataset.getModel()));
			
			dataset.filterDataModelSingleDomain(targetDomain);
			//System.out.println(Functions.numOfRatings(dataset.getModel()));
			
			IDRescorer idrescorer = null; //single-domain
			
			HashSet<ItemDomain> target = new HashSet<ItemDomain>();
			target.add(targetDomain);
			
			EvaluateRecommender er2 = new EvaluateRecommender(dataset,null,recommenders,configuration);
			
			if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class)){
				ContextualRecommenderEvaluator evaluator2 = new MAEAndRMSERecommenderEvaluatorCrossDomain(idrescorer, null,dataset);
				er2.evaluateRecommender(evaluator2,trial,target);
			}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class)){
				CrossDomainContextualRecommenderIRStatsCremonesiEvaluator evaluator2 = new CrossDomainContextualRecommenderIRStatsCremonesiEvaluator(idrescorer, null,dataset);
				er2.evaluateRecommenderIRCremonesiStats(evaluator2, trial,targetDomain,null,target,false);
			}
	
			
			for(Result r : er2.getResults()){
				r.setContext("NO_CONTEXT");
				r.setSourceDomain(targetDomain.name());
				r.setTargetDomain(targetDomain.name());
				resultsEval.add(r);
				
				
					if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class)){
						log.warn(r.showPredictionPerformance());
						exportPredicitions(r);
					}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class)){
						log.warn(r.showCremonesiRankingPerformance());
						exportCremonesiRankings(r);
					}
				
				
			}

			log.warn("SINGLE DOMAIN TESTS ARE FINISHED!!");
			
			
			
		}catch (TasteException e) {
			// 
			e.printStackTrace();
		} 
		
	}
	
	private static Result getResultWithSameAlgorithm(HashSet<Result> set, String algoritm){
		for (Result result : set) {
			if(result.getAlgorithmName().equals(algoritm)){
				return result;
			}
		}
		return null;
	}

	private static void evaluateCrossDomain(EvaluationConfig configuration, ArrayList<ContextualRecommenderBuilder> recommenders, int trial, double userOverlapLevel, ItemDomain sourceDomain1,ItemDomain sourceDomain2, ItemDomain targetDomain, Class<?> evaluator, HashSet<Class<? extends AbstractContextualAttribute>> testedAttributes, boolean exportContextualFilesByValue) {

		
		
		try {
			
			AbstractDataset dataset = null;
			
			if(userOverlapLevel > 0) {
				
				dataset = AmazonCrossDataset.getInstance(true, ItemDomain.MUSIC,"/datasets/Books_MUSIC/", "FULL-overlapping-database.dat");
				
			}else{
				
				dataset = AmazonCrossDataset.getInstance(true, ItemDomain.MUSIC,"/datasets/Books_MUSIC/", "FULL-not-overlapping-database.dat");
				
			}
			
			HashSet<ItemDomain> overlapDomains = new HashSet<ItemDomain>();
			overlapDomains.add(sourceDomain1);
			overlapDomains.add(sourceDomain2);
			overlapDomains.add(targetDomain);
			
			HashSet<ItemDomain> domainsFilter = new HashSet<ItemDomain>();
			domainsFilter.add(sourceDomain1);
			domainsFilter.add(sourceDomain2);
		
			IDRescorer idrescorer = new ItemDomainRescorer(null,domainsFilter, dataset); // cross-domain

			ContextualCriteria cc = new ContextualCriteria(); 
			for(int i=0;i<cc.getContextualAttributes().size();i++){
				
				List<Result> resultsEval = new ArrayList<Result>();
				
				if(i==5){//pular companion type alone or not
					continue;
				}
				
				if(testedAttributes!= null && !testedAttributes.contains(cc.getContextualAttributes().get(i).getClass())){
					continue;
				}
				
				for(AbstractContextualAttribute contextualAttrValue : cc.getContextualAttributes().get(i).valuesForTest()){
					
					
					int index = 0;
					
					DayTypeContextualAttribute dayType = (DayTypeContextualAttribute)  ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
					index++;
					DayContextualAttribute day = (DayContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
					index++;
					LocationCountryContextualAttribute locationCountry = (LocationCountryContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
					index++;
					LocationStateContextualAttribute locationState = (LocationStateContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
					index++;
					LocationCityContextualAttribute locationCity = (LocationCityContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
					index++;
					CompanionContextualAttribute aloneOrNot = (CompanionContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
					index++;
					if(aloneOrNot.getCode()>CompanionContextualAttribute.ACCOMPANIED.getCode()){
						continue;
					}
					
					CompanionContextualAttribute companionType = (CompanionContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
					index++;
					TaskContextualAttribute task = (TaskContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
					index++;
					
					ContextualCriteria criteria = new ContextualCriteria(dayType,day,locationCountry,locationState
																		,locationCity,aloneOrNot,companionType,task);
					
					EvaluateRecommender er2 = new EvaluateRecommender(dataset,criteria,recommenders,configuration);
					
					if(evaluator.equals(MAEAndRMSERecommenderEvaluatorTracer.class)) {
						ContextualRecommenderEvaluator evaluator2 = new MAEAndRMSERecommenderEvaluatorTracer(idrescorer, criteria,dataset);
						er2.evaluateRecommender(evaluator2,trial,overlapDomains);
					}if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class)){
						ContextualRecommenderEvaluator evaluator2 = new MAEAndRMSERecommenderEvaluatorCrossDomain(idrescorer, criteria,dataset);
						er2.evaluateRecommender(evaluator2,trial,overlapDomains);
					}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsEvaluator.class)){
						CrossDomainContextualRecommenderIRStatsEvaluator evaluator2 = new CrossDomainContextualRecommenderIRStatsEvaluator(idrescorer, criteria,dataset);
						er2.evaluateRecommenderIRStats(evaluator2, trial,overlapDomains);
					}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class)){
						CrossDomainContextualRecommenderIRStatsCremonesiEvaluator evaluator2 = new CrossDomainContextualRecommenderIRStatsCremonesiEvaluator(idrescorer, criteria,dataset);
						er2.evaluateRecommenderIRCremonesiStats(evaluator2, trial,targetDomain,criteria,overlapDomains,true);
					}
	
					for(Result r : er2.getResults()){
				
						r.setContext(contextualAttrValue.name());
						r.setSourceDomain(sourceDomain1.name() +"-"+ sourceDomain2.name());
						r.setTargetDomain(targetDomain.name());
						r.setOverlapingRate(userOverlapLevel);
						resultsEval.add(r);
						
						
							if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class)){
								log.warn(r.showPredictionPerformance());
								if(exportContextualFilesByValue){
									exportPredicitions(r);
								}
							}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsEvaluator.class)){
								log.warn(r.showRankingPerformance());
								if(exportContextualFilesByValue){
									exportRankings(r);
								}
							}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class)){
								log.warn(r.showCremonesiRankingPerformance());
								if(exportContextualFilesByValue){
									exportCremonesiRankings(r);
								}
							}else if(evaluator.equals(MAEAndRMSERecommenderEvaluatorTracer.class)){ 
								
								if(r.getPredictionValuesPosFilter()==null) {
									log.warn(r.showTracerPerformance());
								}else {
									log.warn(r.showTracerPosFilterPerformance());
								}
								
								if(exportContextualFilesByValue){
									exportTracerResults(r);
								}
							}
						
					}
				
				}
				
				
				Result temp = new Result();
				int evaluatedContexts = 0;
				
				for (int j = 0; j < resultsEval.size(); j++) {
					
					Result r = resultsEval.get(j);
					
					if(r.getPredictionValues() == null || r.getPredictionValues().isEmpty()){
						continue;
					}else {
						System.out.println(r.getContext());
					}
				
					evaluatedContexts++;
					
					if(j == 0){
						
						temp = r;
						
						temp.setContext(cc.getContextualAttributes().get(i).getClass().getSimpleName());
						
					}else{
						if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class)){
							
							
							temp.setExecutionTime(temp.getExecutionTime()+r.getExecutionTime());
							temp.getPredictionValues().addAll(r.getPredictionValues());
							temp.setTotalOfTestRatings(temp.getTotalOfTestRatings()+r.getTotalOfTestRatings());
							temp.setTotalOfTrainingRatingsFromTargetWithContext(temp.getTotalOfTrainingRatingsFromTargetWithContext() + r.getTotalOfTrainingRatingsFromTargetWithContext());
							temp.setTotalOfTrainingRatingsFromSource(temp.getTotalOfTrainingRatingsFromSource() + r.getTotalOfTrainingRatingsFromSource());
							temp.setNumOfUsers(temp.getNumOfUsers() + r.getNumOfUsers());
							temp.setNumOfItens(temp.getNumOfItens() + r.getNumOfItens());
							temp.setNumOfOverlappedUsers(temp.getNumOfOverlappedUsers() + r.getNumOfOverlappedUsers());
							
						}else if(evaluator.equals(MAEAndRMSERecommenderEvaluatorTracer.class)){
							temp.setExecutionTime(temp.getExecutionTime()+r.getExecutionTime());
							temp.getPredictionValues().addAll(r.getPredictionValues());
							
							if(temp.getPredictionValuesPosFilter()!=null) {

								temp.getPredictionValuesPosFilter().addAll(r.getPredictionValuesPosFilter());
							}
							
							temp.setTotalOfTestRatings(temp.getTotalOfTestRatings()+r.getTotalOfTestRatings());
							temp.setTotalOfTrainingRatingsFromTarget(temp.getTotalOfTrainingRatingsFromTarget() + r.getTotalOfTrainingRatingsFromTarget());
							temp.setTotalOfTrainingRatingsFromSource(temp.getTotalOfTrainingRatingsFromSource() + r.getTotalOfTrainingRatingsFromSource());
							temp.setNumOfUsers(temp.getNumOfUsers() + r.getNumOfUsers());
							temp.setNumOfItens(temp.getNumOfItens() + r.getNumOfItens());
							temp.setNumOfOverlappedUsers(temp.getNumOfOverlappedUsers() + r.getNumOfOverlappedUsers());
						}
					}
					
				}
				
			
				temp.setContextsEvaluated(evaluatedContexts);
				double rmseValue = 0;
				double maeValue = 0;
					
				if(temp.getPredictionValues() != null){
						
						
						for(PredictionValues pvs : temp.getPredictionValues()){
							if(pvs != null){
								maeValue = maeValue + Math.abs(pvs.getRealPref()-pvs.getEstimatedPref());
								rmseValue = rmseValue + Math.pow(pvs.getRealPref()-pvs.getEstimatedPref(), 2);
							}
							
						}
						
						temp.setMaeValue(maeValue/(double)temp.getPredictionValues().size());
						
						temp.setRmseValue(Math.sqrt(rmseValue/(double)temp.getPredictionValues().size()));
						
						
						if(temp.getPredictionValuesPosFilter()!=null) {
							
							rmseValue = 0;
							maeValue = 0;
							
							for(PredictionValues pvs : temp.getPredictionValuesPosFilter()){
								if(pvs != null){
									maeValue = maeValue + Math.abs(pvs.getRealPref()-pvs.getEstimatedPref());
									rmseValue = rmseValue + Math.pow(pvs.getRealPref()-pvs.getEstimatedPref(), 2);
								}
								
							}
								
								temp.setMAEPosFilter(maeValue/(double)temp.getPredictionValuesPosFilter().size());
								
								temp.setRMSEPosFilter(Math.sqrt(rmseValue/(double)temp.getPredictionValuesPosFilter().size()));
							
						}
						
						
						if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class)){
							exportPredicitions(temp);
						}else if(evaluator.equals(MAEAndRMSERecommenderEvaluatorTracer.class)) {
							exportTracerResults(temp);
						}
					}
					
				
			
			
		}

		log.warn("CROSS DOMAIN TESTS ARE FINISHED!!");
			
		}catch (TasteException e) {
			// 
			e.printStackTrace();
		}
		
		
	}
	
	private static void evaluateCrossDomainCombiningContexts(EvaluationConfig configuration, ArrayList<ContextualRecommenderBuilder> recommenders, int trial, double userOverlapLevel, ItemDomain sourceDomain, ItemDomain targetDomain, Class<?> evaluator,  boolean exportContextualFilesByValue) {

		
		
		try {
						
			AbstractDataset dataset = AmazonCrossDataset.getInstance(userOverlapLevel,sourceDomain,targetDomain,true); //cross domain
			//Functions.getNumOfUsersAndOverlappedUsers(dataset.getModel(),dataset);
			
			HashSet<ItemDomain> overlapDomains = new HashSet<ItemDomain>();
			overlapDomains.add(sourceDomain);
			overlapDomains.add(targetDomain);
			
			HashSet<ItemDomain> domainsFilter = new HashSet<ItemDomain>();
			domainsFilter.add(sourceDomain);
			//domainsFilter.add(ItemDomain.MOVIE);
		
			IDRescorer idrescorer = new ItemDomainRescorer(null,domainsFilter, dataset); // cross-domain

			ContextualCriteria cc = new ContextualCriteria(); 
//			for(int i=0;i<cc.getContextualAttributes().size();i++){
				
				List<Result> resultsEval = new ArrayList<Result>();
				
//				if(i==5){//pular companion type alone or not
//					continue;
//				}
				
//				if(testedAttributes!= null && !testedAttributes.contains(cc.getContextualAttributes().get(i).getClass())){
//					continue;
//				}
				
				//int i = testedAttributes.get(0).
				//AbstractContextualAttribute var1 = DayTypeContextualAttribute.;
				
				for(AbstractContextualAttribute contextualAttrValue1 : cc.getContextualAttributes().get(1).valuesForTest()){
					for(AbstractContextualAttribute contextualAttrValue2 : cc.getContextualAttributes().get(4).valuesForTest()){
					
					
//					int index = 0;
//					
//					DayTypeContextualAttribute dayType = (DayTypeContextualAttribute)  ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
//					index++;
//					DayContextualAttribute day = (DayContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
//					index++;
//					LocationCountryContextualAttribute locationCountry = (LocationCountryContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
//					index++;
//					LocationStateContextualAttribute locationState = (LocationStateContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
//					index++;
//					LocationCityContextualAttribute locationCity = (LocationCityContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
//					index++;
//					CompanionContextualAttribute aloneOrNot = (CompanionContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
//					index++;
//					if(aloneOrNot.getCode()>CompanionContextualAttribute.ACCOMPANIED.getCode()){
//						continue;
//					}
//					
//					CompanionContextualAttribute companionType = (CompanionContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
//					index++;
//					TaskContextualAttribute task = (TaskContextualAttribute) ((i == index) ? contextualAttrValue : cc.getContextualAttributes().get(index));
//					index++;
					
					ContextualCriteria criteria = new ContextualCriteria(DayTypeContextualAttribute.UNKNOWN,(DayContextualAttribute)contextualAttrValue1,LocationCountryContextualAttribute.UNKNOWN,LocationStateContextualAttribute.UNKNOWN
																		,(LocationCityContextualAttribute)contextualAttrValue2,CompanionContextualAttribute.UNKNOWN,CompanionContextualAttribute.UNKNOWN,TaskContextualAttribute.UNKNOWN);
					
//					if(!dataset.containsRatingsInContext(criteria)){
//						continue;
//					}
					
					EvaluateRecommender er2 = new EvaluateRecommender(dataset,criteria,recommenders,configuration);
							
					if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class)){
						ContextualRecommenderEvaluator evaluator2 = new MAEAndRMSERecommenderEvaluatorCrossDomain(idrescorer, criteria,dataset);
						er2.evaluateRecommender(evaluator2,trial,overlapDomains);
					}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsEvaluator.class)){
						CrossDomainContextualRecommenderIRStatsEvaluator evaluator2 = new CrossDomainContextualRecommenderIRStatsEvaluator(idrescorer, criteria,dataset);
						er2.evaluateRecommenderIRStats(evaluator2, trial,overlapDomains);
					}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class)){
						CrossDomainContextualRecommenderIRStatsCremonesiEvaluator evaluator2 = new CrossDomainContextualRecommenderIRStatsCremonesiEvaluator(idrescorer, criteria,dataset);
						er2.evaluateRecommenderIRCremonesiStats(evaluator2, trial,targetDomain,criteria,overlapDomains,true);
					}
	
					for(Result r : er2.getResults()){
						r.setContext(contextualAttrValue1.name()+"_"+contextualAttrValue2.name());
						r.setSourceDomain(sourceDomain.name());
						r.setTargetDomain(targetDomain.name());
						resultsEval.add(r);
						
						
							if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class)){
								log.warn(r.showPredictionPerformance());
								if(exportContextualFilesByValue){
									exportPredicitions(r);
								}
							}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsEvaluator.class)){
								log.warn(r.showRankingPerformance());
								if(exportContextualFilesByValue){
									exportRankings(r);
								}
							}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class)){
								log.warn(r.showCremonesiRankingPerformance());
								if(exportContextualFilesByValue){
									exportCremonesiRankings(r);
								}
							}
						
						
					}
				
					}
				
				}
				HashSet<Result> globalResults = new HashSet<Result>();
				
				for(Result r : resultsEval){
					if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class) &&
							(r.getPredictionValues() == null || r.getPredictionValues().isEmpty())){
						continue;
					}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class)&&
							(r.getCremonesiValues() == null || r.getCremonesiValues().isEmpty())){
						continue;
					}
					
					Result temp = getResultWithSameAlgorithm(globalResults,r.getAlgorithmName());
					if(temp == null){
						r.addPredictionPerformanceByContext(r);
						r.addCremonesiPerformanceByContext(r);
						r.setContext(cc.getContextualAttributes().get(1).getClass().getSimpleName()+" "+
								cc.getContextualAttributes().get(4).getClass().getSimpleName());
						globalResults.add(r);
					}else{
						if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class)){
							temp.setExecutionTime(temp.getExecutionTime()+r.getExecutionTime());
							temp.setNumOfTestedUsers(temp.getNumOfTestedUsers()+r.getNumOfTestedUsers());
							temp.getPredictionValues().addAll(r.getPredictionValues());
							temp.setTotalOfTestRatings(temp.getTotalOfTestRatings()+r.getTotalOfTestRatings());
							temp.addPredictionPerformanceByContext(r);
						}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class)){
							temp.setExecutionTime(temp.getExecutionTime()+r.getExecutionTime());
							temp.setNumOfTestedUsers(temp.getNumOfTestedUsers()+r.getNumOfTestedUsers());
							temp.getCremonesiValues().addAll(r.getCremonesiValues());
							temp.setTotalOfTestRatings(temp.getTotalOfTestRatings()+r.getTotalOfTestRatings());
							temp.addCremonesiPerformanceByContext(r);
						}
					}
					
				}
				
				for(Result globalResult : globalResults){
					if(evaluator.equals(MAEAndRMSERecommenderEvaluatorCrossDomain.class)){
						
						double rmseValue = 0;
						double maeValue = 0;
						if(globalResult.getPredictionValues() != null){
							for(PredictionValues pvs : globalResult.getPredictionValues()){
								if(pvs != null){
									maeValue = maeValue + Math.abs(pvs.getRealPref()-pvs.getEstimatedPref());
									rmseValue = rmseValue + Math.pow(pvs.getRealPref()-pvs.getEstimatedPref(), 2);
								}
								
							}
							
							globalResult.setMaeValue(maeValue/(double)globalResult.getPredictionValues().size());
							
							globalResult.setRmseValue(Math.sqrt(rmseValue/(double)globalResult.getPredictionValues().size()));
							
							log.warn(globalResult.showPredictionPerformance());
							exportPredicitions(globalResult);
						}
						
					}else if(evaluator.equals(CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class)){
						double precisionValue = 0;
						double recallValue = 0;
						double f1MeasureValue = 0;
						
						int topN = globalResult.getTopN();
						
						int numOfHits = 0;
						int counter = 0;
						
						if(globalResult.getCremonesiValues() != null){
							for(CremonesiValues cv : globalResult.getCremonesiValues()){
								if(cv != null){
									if(cv.getPosition() <= topN){
										numOfHits++;
									}
									counter++;
								}
								
							}
							
							recallValue = (double) numOfHits/(double) counter;
							
							precisionValue = recallValue / (double) topN;
							
							if(recallValue+precisionValue != 0.0){
								f1MeasureValue = (2*recallValue*precisionValue)/(recallValue+precisionValue);
							}							
							
							globalResult.setPrecisionValue(precisionValue);
							
							globalResult.setRecallValue(recallValue);
							
							globalResult.setF1MeasureValue(f1MeasureValue);
							
							log.warn(globalResult.showCremonesiRankingPerformance());
							exportCremonesiRankings(globalResult);
						}
					}
				}
				
			//}

			log.warn("CROSS DOMAIN TESTS ARE FINISHED!!");

			
		}catch (TasteException e) {
			// 
			e.printStackTrace();
		}
		
		
	}
		
	
	
	private static void exportTracerResults(Result r) {
		

		String algorithmName[] = r.getAlgorithmName().split(":");
		algorithmName = algorithmName[0].split("@");
		
		File fileOutput = new File("C:/Users/guilh/Documents/UFRPE/TCC/TBookmarks/TBookmarksRSProject/ResultTests/export_tracer/"+algorithmName[0]+")_"+r.getContext()+" ("+ r.getTrial()+")"+".txt");
		
		
		try {
			
			FileWriter fileWriter = new FileWriter(fileOutput, true);

			
			
			if(r.getPredictionValuesPosFilter() == null) {
				
				fileWriter.write(r.showTracerPerformance());
				
				fileWriter.write("\n -------------------------------------- ( TRACER ) PREDICTIONS VALUES  -------------------------------------- \n\n");
				
				for(int i = 0; i < r.getPredictionValues().size(); i++){
					if(r.getPredictionValues() != null && r.getPredictionValues().get(i) != null){
						fileWriter.write(r.getPredictionValues().get(i).getUserID()+"\t"+
								r.getPredictionValues().get(i).getItemID()+"\t"+
								Functions.codeContextsToStringForFile(r.getPredictionValues().get(i).getContext())+"\t"+
								r.getPredictionValues().get(i).getRealPref()+"\t"+
								r.getPredictionValues().get(i).getEstimatedPref()+"\n");
					}
				}
			}
			
			if(r.getPredictionValuesPosFilter() != null) {
				
				fileWriter.write(r.showTracerPosFilterPerformance());
				
				fileWriter.write("\n -------------------------------------- (TRACER : POS-FILTER) PREDICTIONS VALUES  -------------------------------------- \n\n");
				
				for(int i = 0; i < r.getPredictionValuesPosFilter().size(); i++){
					fileWriter.write(r.getPredictionValuesPosFilter().get(i).getUserID()+"\t"+
							r.getPredictionValuesPosFilter().get(i).getItemID()+"\t"+
							Functions.codeContextsToStringForFile(r.getPredictionValuesPosFilter().get(i).getContext())+"\t"+
							r.getPredictionValuesPosFilter().get(i).getRealPref()+"\t"+
							r.getPredictionValuesPosFilter().get(i).getEstimatedPref()+"\n");
				
				}
				
				
			}
				
			fileWriter.close();
			log.info("File "+fileOutput.getName()+" exported!");

		} catch (FileNotFoundException e) {
			// 
			e.printStackTrace();
		} catch (IOException e) {
			// 
			e.printStackTrace();
		} 

		
	}
	
	private static void exportCremonesiRankings(Result r) {


		String algorithmName[] = r.getAlgorithmName().split(":");
		
		File fileOutput = new File("~/Documentos/UFRPE/TCC/TBookmarks/TBookmarksRSProject/ResultTests/export_cremonesi/"+algorithmName[0]+r.getContext()+"_"+r.getTargetDomain()+r.getTrial()+".txt");
		
		if (!fileOutput.exists()) {

			try {
								
				FileOutputStream streamOutput = new FileOutputStream(fileOutput);

				OutputStreamWriter streamWriter = new OutputStreamWriter(
						streamOutput);

				BufferedWriter bw = new BufferedWriter(streamWriter);
				
				bw.append(r.showCremonesiRankingPerformance()+"\n");
				for(String perfs : r.showCremonesiPerformancesByContext()){
					bw.append(perfs+"\n");
				}
				
				for(int i = 0; i < r.getCremonesiValues().size(); i++){
					if(r.getCremonesiValues() != null && r.getCremonesiValues().get(i) != null){
						bw.append(r.getCremonesiValues().get(i).getUserID()+"\t"+
								r.getCremonesiValues().get(i).getItemId()+"\t"+
								Functions.codeContextsToStringForFile(r.getCremonesiValues().get(i).getContext())+"\t"+
								r.getCremonesiValues().get(i).getPosition()+"\n");
					}
				}
				bw.close();
				log.info("File "+fileOutput.getName()+" exported!");

			} catch (FileNotFoundException e) {
				// 
				e.printStackTrace();
			} catch (IOException e) {
				// 
				e.printStackTrace();
			} 
		}
	

		
	}

	private static void exportPredicitions(Result r) {


		String algorithmName[] = r.getAlgorithmName().split(":");
		
		
		File fileOutput = new File("C:/Users/guilh/Documents/UFRPE/TCC/TBookmarks/TBookmarksRSProject/ResultTests/export_predictions/"+algorithmName[0]+")_"+r.getContext()+" ("+ r.getTrial()+")"+".txt");
		
		if (!fileOutput.exists()) {

			try {
								
				FileWriter fileWriter = new FileWriter(fileOutput, true);
				
				fileWriter.write(r.showPredictionPerformance());
				

				fileWriter.write("\n -------------------------------------- ( CREMONESI ) PREDICTIONS VALUES  -------------------------------------- \n\n");
				
				for(int i = 0; i < r.getPredictionValues().size(); i++){
					if(r.getPredictionValues() != null && r.getPredictionValues().get(i) != null){
						fileWriter.write(r.getPredictionValues().get(i).getUserID()+"\t"+
								r.getPredictionValues().get(i).getItemID()+"\t"+
								Functions.codeContextsToStringForFile(r.getPredictionValues().get(i).getContext())+"\t"+
								r.getPredictionValues().get(i).getRealPref()+"\t"+
								r.getPredictionValues().get(i).getEstimatedPref()+"\n");
					}
				}
				fileWriter.close();
				log.info("File "+fileOutput.getName()+" exported!");

			} catch (FileNotFoundException e) {
				// 
				e.printStackTrace();
			} catch (IOException e) {
				// 
				e.printStackTrace();
			} 
		}
	

		
	}

	private static void exportRankings(Result r) {


		String algorithmName[] = r.getAlgorithmName().split(":");
		
		
		
		File fileOutput = new File("~/Documentos/UFRPE/TCC/TBookmarksRSProject-20190124T223454Z-001/TBookmarksRSProject/ResultTests/rankings/"+algorithmName[0]+r.getContext()+"_"+r.getTargetDomain()+r.getTrial()+".txt");
		
		if (!fileOutput.exists()) {

			try {
								
				FileOutputStream streamOutput = new FileOutputStream(fileOutput);

				OutputStreamWriter streamWriter = new OutputStreamWriter(
						streamOutput);

				BufferedWriter bw = new BufferedWriter(streamWriter);
				
				bw.append(r.showRankingPerformance()+"\n");
				for(int i = 0; i < r.getRankingValues().size(); i++){
					if(r.getRankingValues() != null && r.getRankingValues().get(i) != null){
						bw.append(r.getRankingValues().get(i).getUserID()+"\t"+
								r.getRankingValues().get(i).toStringRelevantItems()+"\t"+
								r.getRankingValues().get(i).toStringRecommendedItems()+"\n");
					}
				}
				bw.close();
				log.info("File "+fileOutput.getName()+" exported!");

			} catch (FileNotFoundException e) {
				// 
				e.printStackTrace();
			} catch (IOException e) {
				// 
				e.printStackTrace();
			} 
		}

	}

	
	public static void main(String[] args) {
		
		/* Global Configuration */
		
		double trainingPercentage = 0.8;
		double datasetPercentage = 1.0;
		int top_n = 5;
		double relevantThresholdPrecisionRecall = 5.0;
		boolean enableFixedTestSeed = false;
		double overlapping = 0.10;
		
		/* TRACER Configuration */
		
		double userClusterTransfer = 15.0;
		double itemClusterTransfer = 15.0;
		double trainingIterations = 15;
		double alpha = 1.0;
		double beta = 0.0001;		
		
	
		
		EvaluationConfig configuration = new EvaluationConfig(trainingPercentage, datasetPercentage, top_n, relevantThresholdPrecisionRecall, enableFixedTestSeed);
		
		ArrayList<ContextualRecommenderBuilder> recommenders = new ArrayList<ContextualRecommenderBuilder>();
	    /* Descomentar Após testes -- Cross Domain sem uso de informação contextual */
		//recommenders.add(new RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(475, EuclideanDistanceSimilarity.class));
		
	    //recommenders.add(new RecommenderBuilderUserBasedNearestNeighborCremonesi(475, EuclideanDistanceContextualSimilarity.class));
		//recommenders.add(new RecommenderBuilderUserBasedTreshold(0.5, EuclideanDistanceSimilarity.class));
		//recommenders.add(new RecommenderBuilderItemBased(EuclideanDistanceSimilarity.class));
		//recommenders.add(new RecommenderBuilderSVD(10,0.05,80));
	
		
		/* TEST - Without Pré-Pós Filter */
		
		//recommenders.add(new RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(225, EuclideanDistanceSimilarity.class));
		//recommenders.add(new RecommenderBuilderUserBasedTracer(userClusterTransfer , itemClusterTransfer,  alpha, beta, trainingIterations,trainingPercentage));
	    
		
		/* TEST Pré-Filter Descomentar Após testes -- Cross Domain Pré-Filter */
		
		//recommenders.add(new PreFilteringContextualBuildRecommenderByron(new RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(225, EuclideanDistanceSimilarity.class)));
		//recommenders.add(new PreFilteringContextualBuildRecommenderByron(new RecommenderBuilderUserBasedTracer(userClusterTransfer , itemClusterTransfer,  alpha, beta, trainingIterations,trainingPercentage)) );

	
		 /* TEST Cross Domain Pós-Filter */
		
		PostFilteringStrategyRecommendation pfStrategy4 = new PostFilteringStrategyRecommendation(PostFilteringStrategyRecommendation.PossibleFilteringStrategies.AT_LEAST_MEDIA_OF_OCCURRENCIES,true,5.0f,(float)1/2,0.5,0.01);
		//recommenders.add(new PostFilteringContextualBuildRecommender(new RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo(225, EuclideanDistanceSimilarity.class),pfStrategy4));
		recommenders.add(new PostFilteringContextualBuilderRecommenderTracer(new RecommenderBuilderUserBasedTracer(userClusterTransfer , itemClusterTransfer,  alpha, beta, trainingIterations,trainingPercentage),pfStrategy4));
		
		
		HashSet<Class<? extends AbstractContextualAttribute>> testedContextualAttributes = new HashSet<Class<? extends AbstractContextualAttribute>>();
		//testedContextualAttributes.add(LocationCityContextualAttribute.class);
		
		//testedContextualAttributes.add(DayContextualAttribute.class);
		//testedContextualAttributes.add(CompanionContextualAttribute.class);
		
		//testedContextualAttributes.add(LocationStateContextualAttribute.class);
		testedContextualAttributes.add(LocationCountryContextualAttribute.class);
		
		//evaluateSingleDomain(recommenders,1,ItemDomain.MOVIE,ItemDomain.MOVIE,null);
//		evaluateCrossDomain(configuration,recommenders,1,0.1,ItemDomain.BOOK,ItemDomain.MUSIC, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class, testedContextualAttributes,false);
//		evaluateCrossDomain(configuration,recommenders,2,0.5,ItemDomain.BOOK,ItemDomain.MUSIC, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class, testedContextualAttributes,false);
//		evaluateCrossDomain(configuration,recommenders,3,1.0,ItemDomain.BOOK,ItemDomain.MUSIC, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class, testedContextualAttributes,false);
//		
//		evaluateCrossDomain(configuration,recommenders,4,0.1,ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class, testedContextualAttributes,false);
//		evaluateCrossDomain(configuration,recommenders,5,0.5,ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class, testedContextualAttributes,false);
//		evaluateCrossDomain(configuration,recommenders,6,1.0,ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class, testedContextualAttributes,false);
//		
//		HashSet<? extends AbstractContextualAttribute> testedContextualAttributes = new HashSet<? extends AbstractContextualAttribute>();
//		testedContextualAttributes.add(LocationCityContextualAttribute.class);
//		
//		testedContextualAttributes.add(DayContextualAttribute.class);
		
//		ArrayList<ContextualRecommenderBuilder> recommenders2 = new ArrayList<ContextualRecommenderBuilder>();
//		recommenders2.add(new RecommenderBuilderUserBasedNearestNeighbor(475, EuclideanDistanceSimilarity.class));
		
//		evaluateSingleDomain(configuration, recommenders2, 1, 0.1, ItemDomain.BOOK,ItemDomain.MUSIC, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		evaluateSingleDomain(configuration, recommenders2, 2, 0.5, ItemDomain.BOOK,ItemDomain.MUSIC, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		evaluateSingleDomain(configuration, recommenders2, 3, 1.0, ItemDomain.BOOK,ItemDomain.MUSIC, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		
//		evaluateSingleDomain(configuration, recommenders2, 4, 0.1, ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		evaluateSingleDomain(configuration, recommenders2, 5, 0.5, ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		evaluateSingleDomain(configuration, recommenders2, 6, 1.0, ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		
		
		/* EVALUATES TESTS */
		
		
		//for(int trial=3 ;trial <5;trial++){
	    
			//evaluateCrossDomain(configuration, recommenders, trial, overlapping, ItemDomain.BOOK, ItemDomain.MOVIE, ItemDomain.MUSIC ,MAEAndRMSERecommenderEvaluatorCrossDomain.class ,testedContextualAttributes,false);		
			
			evaluateCrossDomain(configuration, recommenders, 4, overlapping, ItemDomain.BOOK, ItemDomain.MOVIE, ItemDomain.MUSIC ,MAEAndRMSERecommenderEvaluatorTracer.class ,testedContextualAttributes,false);		
		
		//}


		
		
		
		//		evaluateCrossDomain(configuration, recommenders2, 8, 0.5, ItemDomain.BOOK,ItemDomain.MUSIC, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,testedContextualAttributes,false);
//		evaluateCrossDomain(configuration, recommenders2, 9, 1.0, ItemDomain.BOOK,ItemDomain.MUSIC, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,testedContextualAttributes,false);	
//		evaluateCrossDomain(configuration, recommenders2, 11, 0.5, ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,testedContextualAttributes,false);
//		evaluateCrossDomain(configuration, recommenders2, 12, 1.0, ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,testedContextualAttributes,false);
		
//		evaluateCrossDomain(configuration, recommenders, 13, 1.0, ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,testedContextualAttributes,false);
//		evaluateCrossDomain(configuration, recommenders2, 14, 1.0, ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,testedContextualAttributes,false);
		
//		evaluateSingleDomain(configuration, recommenders2, 4, 0.5, ItemDomain.MOVIE,ItemDomain.BOOK, MAEAndRMSERecommenderEvaluatorCrossDomain.class);
		
//		evaluateSingleDomain(configuration, recommenders2, 5, 0.1, ItemDomain.BOOK,ItemDomain.MOVIE, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		evaluateSingleDomain(configuration, recommenders2, 6, 0.5, ItemDomain.BOOK,ItemDomain.MOVIE, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		evaluateSingleDomain(configuration, recommenders2, 7, 1.0, ItemDomain.BOOK,ItemDomain.MOVIE, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		
//		
//		evaluateSingleDomain(configuration, recommenders2, 8, 0.1, ItemDomain.MOVIE,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		evaluateSingleDomain(configuration, recommenders2, 9, 0.5, ItemDomain.MOVIE,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
//		evaluateSingleDomain(configuration, recommenders2, 10, 1.0, ItemDomain.MOVIE,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class);
		
//		evaluateSingleDomain(configuration, recommenders2, 11, 1.0, ItemDomain.BOOK,ItemDomain.MOVIE, MAEAndRMSERecommenderEvaluatorCrossDomain.class);
//		evaluateSingleDomain(configuration, recommenders2, 12, 1.0, ItemDomain.MOVIE,ItemDomain.BOOK, MAEAndRMSERecommenderEvaluatorCrossDomain.class);
		
		
		
//		for(int trial=14;trial <= 14;trial++){
//			evaluateCrossDomainCombiningContexts(configuration,recommenders,trial,1.0,ItemDomain.BOOK,ItemDomain.MUSIC, MAEAndRMSERecommenderEvaluatorCrossDomain.class,false);
//		}
//		for(int trial=15;trial <= 15;trial++){
//			evaluateCrossDomainCombiningContexts(configuration,recommenders,trial,1.0,ItemDomain.MUSIC,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,false);
//		}
//		
//		for(int trial=1;trial <= 1;trial++){
//			evaluateCrossDomainCombiningContexts(configuration,recommenders2,trial,0.1,ItemDomain.MOVIE,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,false);
//		}
//		
//		for(int trial=2;trial <= 2;trial++){
//			evaluateCrossDomainCombiningContexts(configuration,recommenders2,trial,0.5,ItemDomain.BOOK,ItemDomain.MOVIE, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,false);
//		}
//		
//		for(int trial=2;trial <= 2;trial++){
//			evaluateCrossDomainCombiningContexts(configuration,recommenders2,trial,0.5,ItemDomain.MOVIE,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,false);
//		}
//
//		
//		for(int trial=3;trial <= 3;trial++){
//			evaluateCrossDomainCombiningContexts(configuration,recommenders2,trial,1.0,ItemDomain.BOOK,ItemDomain.MOVIE, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,false);
//		}
//		
//		for(int trial=3;trial <= 3;trial++){
//			evaluateCrossDomainCombiningContexts(configuration,recommenders2,trial,1.0,ItemDomain.MOVIE,ItemDomain.BOOK, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class,false);
//		}

		
//		evaluateCrossDomain(configuration,recommenders,1,1.0,ItemDomain.BOOK,ItemDomain.MOVIE, CrossDomainContextualRecommenderIRStatsCremonesiEvaluator.class, testedContextualAttributes,false);
		
		
		log.warn("EVAL IS FINISHED!!");
		
		/*AbstractDataset dataset = AmazonCrossDataset.getInstance();
		
		ContextualCriteria criteria = new ContextualCriteria(DayTypeContextualAttribute.WEEKDAY,PeriodOfDayContextualAttribute.DAWN);
		
		ItemDomain sourceDomain = ItemDomain.MOVIE;
		
		HashSet<ItemDomain> domainsFilter = new HashSet<ItemDomain>();
		domainsFilter.add(sourceDomain);
		
		IDRescorer idrescorer = new ItemDomainRescorer(null,domainsFilter, dataset);*/
		
		/*RecommenderIRStatsEvaluator evaluator =
				new CrossDomainContextualRecommenderIRStatsEvaluator(idrescorer, criteria);*/
		
		
		
		
		//domainsFilter.add(ItemDomain.MOVIE);
	
		
		
		/*DataModel dm = dataset.getModel();
		
		
		
		IRStatistics stats;
		try {
			
			DataModel contextualDM = ((PreFilteringContextualBuildRecommender) recommenders.get(0)).preFilterDataModel(dm,criteria);
			
			stats = evaluator.evaluate(
					recommenders.get(0), null, contextualDM, idrescorer, 5,	4.0, 0.10);
			System.out.println(stats.getPrecision());
			System.out.println(stats.getRecall());
		} catch (TasteException e) {
			// 
			e.printStackTrace();
		}*/
				

		/*List<Result> resultsEval = new ArrayList<Result>();
		
		int trial = 1;
		
		try {
					
			HashSet<ItemDomain> domainsDataset = new HashSet<ItemDomain>();
			domainsDataset.add(ItemDomain.BOOK);
			domainsDataset.add(ItemDomain.MOVIE);
			
			ItemDomain sourceDomain = ItemDomain.MOVIE;
			ItemDomain targetDomain = ItemDomain.BOOK;
			
			//AbstractDataset dataset = AmazonCrossDataset.getInstance(domainsDataset,20,true); //generate new dataset
			AbstractDataset dataset = AmazonCrossDataset.getInstance(true,targetDomain); //single domain
			//AbstractDataset dataset = AmazonCrossDataset.getInstance(); //use actual dataset
			
			
			
			//System.out.println((System.currentTimeMillis()-timeMilis)/60);
			
			HashSet<ItemDomain> domainsFilter = new HashSet<ItemDomain>();
			domainsFilter.add(ItemDomain.BOOK);
			//domainsFilter.add(ItemDomain.MOVIE);
		
			//IDRescorer idrescorer = new ItemDomainRescorer(null,domainsFilter, dataset); // cross-domain
			IDRescorer idrescorer = null; //single-domain
			
			//NO CONTEXT
//			EvaluateRecommender er = new EvaluateRecommender(dataset,null);
//			
//			RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain(idrescorer,null);
//			er.evaluateRecommender(evaluator);
//			
//			RecommenderEvaluator rmse = new RMSRecommenderEvaluatorCrossDomain(idrescorer,null);
//			er.evaluateRecommender(rmse);

			//er.exportEvaluationToTXT(dayType.name()+periodOfDay.name()+"exportedResults.txt");

			for(int i=1; i <= Integer.valueOf(trial); i++ ){
			
			/*for(DayTypeContextualAttribute dayType : DayTypeContextualAttribute.values()){
				for(PeriodOfDayContextualAttribute periodOfDay : PeriodOfDayContextualAttribute.values()){
					ContextualCriteria criteria = new ContextualCriteria(dayType,periodOfDay);
					//ContextualCriteria criteria = new ContextualCriteria(DayTypeContextualAttribute.WEEKDAY,PeriodOfDayContextualAttribute.DAWN);
					
					System.out.println("Contexutal criteria: "+dayType.name()+periodOfDay.name());
					
					EvaluateRecommender er = new EvaluateRecommender(dataset,criteria);
								
					RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain(idrescorer,criteria);
					er.evaluateRecommender(evaluator);
					
					RecommenderEvaluator rmse = new RMSRecommenderEvaluatorCrossDomain(idrescorer,criteria);
					er.evaluateRecommender(rmse);
	
					//er.exportEvaluationToTXT(dayType.name()+periodOfDay.name()+"exportedResults.txt");
				}
			}
			
			for(DayTypeContextualAttribute dayType : DayTypeContextualAttribute.values()){
					ContextualCriteria criteria = new ContextualCriteria(dayType,PeriodOfDayContextualAttribute.DAWN);
					//ContextualCriteria criteria = new ContextualCriteria(DayTypeContextualAttribute.WEEKDAY,PeriodOfDayContextualAttribute.DAWN);
					
					//log.info("Contexutal criteria: "+dayType.name());
					
					EvaluateRecommender er2 = new EvaluateRecommender(dataset,criteria);
							
					RecommenderEvaluator evaluator2 = new MAEAndRMSERecommenderEvaluatorCrossDomain(idrescorer,criteria);
					er2.evaluateRecommender(evaluator2,i);
					
					/*RecommenderEvaluator evaluator2 = new AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain(idrescorer,criteria);
					er2.evaluateRecommender(evaluator2,i);
					
					RecommenderEvaluator rmse2 = new RMSRecommenderEvaluatorCrossDomain(idrescorer,criteria);
					er2.evaluateRecommender(rmse2,i);
	
					for(Result r : er2.getResults()){
						r.setContext(dayType.name());
						r.setSourceDomain(sourceDomain.name());
						r.setTargetDomain(targetDomain.name());
						resultsEval.add(r);
						//log.info(r.toString());
					}
					
					
					//er.exportEvaluationToTXT(dayType.name()+"exportedResults.txt");
				
			}
			
			/*for(PeriodOfDayContextualAttribute periodOfDay : PeriodOfDayContextualAttribute.values()){
				ContextualCriteria criteria = new ContextualCriteria(null,periodOfDay);
				//ContextualCriteria criteria = new ContextualCriteria(DayTypeContextualAttribute.WEEKDAY,PeriodOfDayContextualAttribute.DAWN);
				
				System.out.println("Contexutal criteria: "+periodOfDay.name());
				
				EvaluateRecommender er = new EvaluateRecommender(dataset,criteria);
							
				RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain(idrescorer,criteria);
				er.evaluateRecommender(evaluator);
				
				RecommenderEvaluator rmse = new RMSRecommenderEvaluatorCrossDomain(idrescorer,criteria);
				er.evaluateRecommender(rmse);

				//er.exportEvaluationToTXT(periodOfDay.name()+"exportedResults.txt");
			
			}
			
			}
			/*RecommenderIRStatsEvaluator precisionRecall = new GenericRecommenderIRStatsEvaluator();
			er.evaluateRecommenderIRStats(precisionRecall,idrescorer);
			for(Result r : resultsEval){
				log.info(r.toString());
				exportPredicitions(r);
			}
			log.warn("EVAL IS FINISHED!!");
			
			
			
		}catch (TasteException e) {
			// 
			e.printStackTrace();
		} 
		
		*/
	
	}
}
	