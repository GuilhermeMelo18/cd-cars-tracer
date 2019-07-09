package br.cin.tbookmarks.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import br.cin.tbookmarks.recommender.evaluation.CremonesiValues;
import br.cin.tbookmarks.recommender.evaluation.PredictionValues;
import br.cin.tbookmarks.recommender.evaluation.RankingValues;

@PersistenceCapable
public class Result {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long resultID;

	@Persistent
	private int trial;

	@Persistent
	private String algorithmName;

	@Persistent
	private double maeValue = -1;

	@Persistent
	private double rmseValue = -1;

	@Persistent
	private long executionTime;

	@Persistent
	private Date date;

	@Persistent
	private String context;

	@Persistent
	private String sourceDomain;

	@Persistent
	private String targetDomain;

	@Persistent
	private int totalOfTrainingRatingsFromSource;

	@Persistent
	private int totalOfTrainingRatingsFromTargetWithoutContext;

	@Persistent
	private int totalOfTrainingRatingsFromTargetWithContext;
	
	@Persistent
	private int totalOfTrainingRatingsFromTarget;

	@Persistent
	private int totalOfTestRatings;

	@Persistent
	private int numOfUsers;
	
	@Persistent
	private int numOfUsersInProbe;
	
	@Persistent
	private int numOfRatingsInProbe;

	@Persistent
	private int numOfOverlappedUsers;

	@Persistent
	private int numOfItens;
	
	@Persistent
	private int numOfItensInProbe;

	@Persistent
	private ArrayList<PredictionValues> predictionValues;
	
	private ArrayList<PredictionValues> predictionValuesPosFilter;
	
	@Persistent
	private ArrayList<RankingValues> rankingValues;
	
	@Persistent
	private ArrayList<CremonesiValues> cremonesiValues;

	private double precisionValue = -1;

	private double recallValue = -1;

	private double f1MeasureValue = -1;

	private int numOfTestedUsers;

	private double falloutValue;

	private double ncdgValue;
	
	private double quantityUsersOverlaping;
	
	private double overlapingRate;
	
	private int numOfSortedItensCremonesi;

	private int topN;
	
	private double MAEValidate;
	
	private double RMSEValidate;
	
	private double MAEPosFilter;
	
	private double RMSEPosFilter;
	
	private int NotEvaluateTestRatings;
	
	private int contextsEvaluated;
	
	private ArrayList<String> predictivePerformances;
	
	private ArrayList<String> cremonesiPerformances;

	public Result() {
		this.predictivePerformances = new ArrayList<String>();
		this.cremonesiPerformances = new ArrayList<String>();
	}
	
	public int getTopN() {
		return topN;
	}
	
	public void setTopN(int topN) {
		this.topN = topN;
	}
	
	
	public int getContextsEvaluated() {
		return contextsEvaluated;
	}

	public void setContextsEvaluated(int contextsEvaluated) {
		this.contextsEvaluated = contextsEvaluated;
	}

	public int getNumOfSortedItensCremonesi() {
		return numOfSortedItensCremonesi;
	}
	
	public void setNumOfSortedItensCremonesi(int numOfSortedItensCremonesi) {
		this.numOfSortedItensCremonesi = numOfSortedItensCremonesi;
	}
	
	
	public double getOverlapingRate() {
		return overlapingRate;
	}

	public void setOverlapingRate(double overlapingRate) {
		this.overlapingRate = overlapingRate;
	}
	
	

	public int getNotEvaluateTestRatings() {
		return NotEvaluateTestRatings;
	}

	public void setNotEvaluateTestRatings(int notEvaluateTestRatings) {
		NotEvaluateTestRatings = notEvaluateTestRatings;
	}

	public Long getResultID() {
		return resultID;
	}

	public void setResultID(Long resultID) {
		this.resultID = resultID;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}
	
	

	public double getQuantityUsersOverlaping() {
		return quantityUsersOverlaping;
	}

	public void setQuantityUsersOverlaping(double quantityUsersOverlaping) {
		this.quantityUsersOverlaping = quantityUsersOverlaping;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public double getMaeValue() {
		return maeValue;
	}

	public void setMaeValue(double maeValue) {
		this.maeValue = maeValue;
	}

	public double getRmseValue() {
		return rmseValue;
	}

	public void setRmseValue(double rmseValue) {
		this.rmseValue = rmseValue;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setRateOverlaping(double overlapingRate ) {
		this.overlapingRate = overlapingRate;
		
	}
	
	public double getRateOverlaping() {
		return this.overlapingRate;
	}
	
	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public int getTotalOfTrainingRatingsFromSource() {
		return totalOfTrainingRatingsFromSource;
	}

	public void setTotalOfTrainingRatingsFromSource(
			int totalOfTrainingRatingsFromSource) {
		this.totalOfTrainingRatingsFromSource = totalOfTrainingRatingsFromSource;
	}

	public int getTotalOfTrainingRatingsFromTargetWithoutContext() {
		return totalOfTrainingRatingsFromTargetWithoutContext;
	}

	public void setTotalOfTrainingRatingsFromTargetWithoutContext(
			int totalOfTrainingRatingsFromTargetWithoutContext) {
		this.totalOfTrainingRatingsFromTargetWithoutContext = totalOfTrainingRatingsFromTargetWithoutContext;
	}

	public int getTotalOfTrainingRatingsFromTargetWithContext() {
		return totalOfTrainingRatingsFromTargetWithContext;
	}

	public void setTotalOfTrainingRatingsFromTargetWithContext(
			int totalOfTrainingRatingsFromTargetWithContext) {
		this.totalOfTrainingRatingsFromTargetWithContext = totalOfTrainingRatingsFromTargetWithContext;
	}
	
	public void setTotalOfTrainingRatingsFromTarget(int totalTrainingRatingsFromTarget) {
		
		this.totalOfTrainingRatingsFromTarget = totalTrainingRatingsFromTarget;
	}
	
	public int getTotalOfTrainingRatingsFromTarget() {
		
		return this.totalOfTrainingRatingsFromTarget;
	}

	public int getTotalOfTestRatings() {
		return totalOfTestRatings;
	}

	public void setTotalOfTestRatings(int totalOfTestRatings) {
		this.totalOfTestRatings = totalOfTestRatings;
	}

	public int getNumOfUsers() {
		return numOfUsers;
	}

	public void setNumOfUsers(int numOfUsers) {
		this.numOfUsers = numOfUsers;
	}
	
	

	public double getMAEValidate() {
		return MAEValidate;
	}

	public void setMAEValidate(double mAEValidate) {
		MAEValidate = mAEValidate;
	}
	
	

	public double getMAEPosFilter() {
		return MAEPosFilter;
	}

	public void setMAEPosFilter(double mAEPosFilter) {
		MAEPosFilter = mAEPosFilter;
	}

	public double getRMSEPosFilter() {
		return RMSEPosFilter;
	}

	public void setRMSEPosFilter(double rMSEPosFilter) {
		RMSEPosFilter = rMSEPosFilter;
	}

	public double getRMSEValidate() {
		return RMSEValidate;
	}

	public void setRMSEValidate(double rMSEValidate) {
		RMSEValidate = rMSEValidate;
	}

	public int getNumOfOverlappedUsers() {
		return numOfOverlappedUsers;
	}

	public void setNumOfOverlappedUsers(int numOfOverlappedUsers) {
		this.numOfOverlappedUsers = numOfOverlappedUsers;
	}

	public int getNumOfItens() {
		return numOfItens;
	}

	public void setNumOfItens(int numOfItens) {
		this.numOfItens = numOfItens;
	}

	public int getTrial() {
		return trial;
	}

	public void setTrial(int trial) {
		this.trial = trial;
	}

	public String getSourceDomain() {
		return sourceDomain;
	}

	public void setSourceDomain(String sourceDomain) {
		this.sourceDomain = sourceDomain;
	}

	public String getTargetDomain() {
		return targetDomain;
	}

	public void setTargetDomain(String targetDomain) {
		this.targetDomain = targetDomain;
	}

	public ArrayList<PredictionValues> getPredictionValues() {
		return predictionValues;
	}
	
	public void setPredictionValues(ArrayList<PredictionValues> predictionValues) {
		this.predictionValues = predictionValues;
	}

	public ArrayList<PredictionValues> getPredictionValuesPosFilter() {
		return predictionValuesPosFilter;
	}

	public void setPredictionValuesPosFilter(ArrayList<PredictionValues> predictionValuesPosFilter) {
		this.predictionValuesPosFilter = predictionValuesPosFilter;
	}

	public ArrayList<RankingValues> getRankingValues() {
		return rankingValues;
	}
	
	public void setRankingValues(ArrayList<RankingValues> rankingValues) {
		this.rankingValues = rankingValues;
	}
	
	public double getPrecisionValue() {
		return precisionValue;
	}

	public void setPrecisionValue(double precisionValue) {
		this.precisionValue = precisionValue;
	}

	public double getRecallValue() {
		return recallValue;
	}

	public void setRecallValue(double recallValue) {
		this.recallValue = recallValue;
	}

	public double getF1MeasureValue() {
		return f1MeasureValue;
	}

	public void setF1MeasureValue(double f1MeasureValue) {
		this.f1MeasureValue = f1MeasureValue;
	}
	
	public int getNumOfTestedUsers() {
		return numOfTestedUsers;
	}
	
	public void setNumOfTestedUsers(int numOfTestedUsers) {
		this.numOfTestedUsers = numOfTestedUsers;
	}

	public String showPredictionPerformance(){
		return  "\n ------------------------------------------  CREMONESI RESULTS  ------------------------------------------------ \n" 
				
				+"\n Date : "+ date 
				+"\n Trial : " + trial 
				+"\n Context : " + context 
				+"\n Algorithm : "+ algorithmName 
				+"\n Result ( MAE ) : " + maeValue 
				+"\n Result ( RMSE ) : " + rmseValue 
				+"\n Source Domains : "+ sourceDomain 
				+"\n Target Domain : " + targetDomain
				+"\n Execution Time : " + (executionTime/1000) + " s"
				+"\n Quantity Evalueted Contexts : "+  contextsEvaluated
				+"\n Total Training Ratings Source : " + totalOfTrainingRatingsFromSource 
				+"\n Total Training Ratings Target : " + totalOfTrainingRatingsFromTargetWithContext 
				+"\n Total Test Ratings : "+ totalOfTestRatings
				+"\n Predicting Test Ratings Available: " + (predictionValues.size())
				+"\n Number Users : " + numOfUsers 
				+"\n Number Overlapping Users : "+ numOfOverlappedUsers 
				+"\n Number Itens : " + numOfItens
				+"\n";
			
	}
	
	public String showTracerPerformance(){
		
		
		String nameTracerAlgorithmName = "";
		
		if(MAEPosFilter != 0) {
			
			nameTracerAlgorithmName = "(CF-based=br.cin.tbookmarks.recommender.algorithms.RecommenderBuilderUserBasedTracer)";
		}else {
			nameTracerAlgorithmName = algorithmName;
		}
		
		return   "\n ------------------------------------------   TRACER RESULTS  ----------------------------------------------- \n" 
				
				+"\n Date : "+ date 
				+"\n Trial : " + trial 
				+"\n Context : " + context 
				+"\n Algorithm : "+ nameTracerAlgorithmName 
				+"\n Result ( MAE ) : " + maeValue 
				+"\n Result ( RMSE ) : " + rmseValue
				+"\n Source Domains : "+ sourceDomain 
				+"\n Target Domain : " + targetDomain
				+"\n Execution Time : " + (executionTime/1000) + " s"
				+"\n Quantity Evalueted Contexts : "+  contextsEvaluated
				+"\n Total Training Ratings Source : " + totalOfTrainingRatingsFromSource 
				+"\n Total Training Ratings Target : " + totalOfTrainingRatingsFromTarget 
				+"\n Total Test Ratings : "+ totalOfTestRatings
				+"\n Predicting Test Ratings Available: " + (predictionValues.size())
				+"\n Number Users : " + numOfUsers 
				+"\n Number Overlapping Users : "+ numOfOverlappedUsers 
				+"\n Number Itens : " + numOfItens
				+"\n";
	}
	
	public String showTracerPosFilterPerformance(){

		return   "\n -------------------------------------------  TRACER POS-FILTER RESULTS  ----------------------------------------------- \n" 

				+"\n Date : "+ date 
				+"\n Trial : " + trial 
				+"\n Context : " + context 
				+"\n Algorithm : "+ algorithmName 
				+"\n Result ( MAE ) : " + MAEPosFilter 
				+"\n Result ( RSME ) : " + RMSEPosFilter
				+"\n Source Domains : "+ sourceDomain 
				+"\n Target Domain : " + targetDomain
				+"\n Execution Time : " + (executionTime/1000) + " s"
				+"\n Quantity Evalueted Contexts : "+  contextsEvaluated
				+"\n Total Training Ratings Source : " + totalOfTrainingRatingsFromSource 
				+"\n Total Training Ratings Target : " + totalOfTrainingRatingsFromTarget 
				+"\n Total Test Ratings : "+ totalOfTestRatings
				+"\n Predicting Test Ratings Available: " + (predictionValuesPosFilter.size())
				+"\n Number Users : " + numOfUsers 
				+"\n Number Overlapping Users : "+ numOfOverlappedUsers 
				+"\n Number Itens : " + numOfItens
				+"\n";
	}
	
	public String showRankingPerformance(){
		return resultID + "\t" + date + "\t" + trial + "\t" + context + "\t"
				+ algorithmName + "\t" + topN + "\t" + precisionValue + "\t" + recallValue + "\t"
				+ falloutValue + "\t" + ncdgValue + "\t"
				+ f1MeasureValue + "\t" + sourceDomain + "\t" + targetDomain + "\t" + executionTime
				+ "\t" + numOfTestedUsers + "\t" + numOfUsers + "\t"
				+ numOfOverlappedUsers + "\t" + numOfItens;
	}

	public void setFallOutValue(double fallOut) {
		this.falloutValue = fallOut;
		
	}
	
	public double getFalloutValue() {
		return falloutValue;
	}

	public void setNDCGValue(double normalizedDiscountedCumulativeGain) {
		this.ncdgValue = normalizedDiscountedCumulativeGain;
		
	}
	
	public double getNcdgValue() {
		return ncdgValue;
	}

	public ArrayList<CremonesiValues> getCremonesiValues() {
		return cremonesiValues;
	}

	public void setCremonesiValues(ArrayList<CremonesiValues> cremonesiValues) {
		this.cremonesiValues = cremonesiValues;
	}
	
	public int getNumOfItensInProbe() {
		return numOfItensInProbe;
	}
	
	public void setNumOfItensInProbe(int numOfItensInProbe) {
		this.numOfItensInProbe = numOfItensInProbe;
	}
	
	public int getNumOfUsersInProbe() {
		return numOfUsersInProbe;
	}
	
	public void setNumOfUsersInProbe(int numOfUsersInProbe) {
		this.numOfUsersInProbe = numOfUsersInProbe;
	}
	
	public void setNumOfRatingsInProbe(int numOfRatingsInProbe) {
		this.numOfRatingsInProbe = numOfRatingsInProbe;
	}
	
	public int getNumOfRatingsInProbe() {
		return numOfRatingsInProbe;
	}

	public String showCremonesiRankingPerformance() {
		return resultID + "\t" + date + "\t" + trial + "\t" + context + "\t"
				+ algorithmName + "\t" + topN + "\t" + precisionValue + "\t" + recallValue + "\t"
				+ f1MeasureValue + "\t" + sourceDomain + "\t" + targetDomain + "\t" + executionTime
				+ "\t" + totalOfTestRatings + "\t" + numOfUsers + "\t"
				+ numOfOverlappedUsers + "\t" + numOfItens + "\t" + numOfUsersInProbe + "\t"
				+ numOfItensInProbe + "\t" + numOfRatingsInProbe + "\t"	+ numOfSortedItensCremonesi;
	}
	
	public void addPredictionPerformanceByContext(Result r){
		this.predictivePerformances.add(r.showPredictionPerformance());
	}
	
	public void addPredictionPerformanceTracer(Result r) {
		
		this.predictivePerformances.add(r.showTracerPerformance());
		
	}

	public String[] showPredictionPerformancesByContext() {
		String[] perfs = new String[predictivePerformances.size()];
		for(int i=0;i<predictivePerformances.size();i++){
			perfs[i] = predictivePerformances.get(i);
		}
		return perfs;
	}
	
	public void addCremonesiPerformanceByContext(Result r){
		this.cremonesiPerformances.add(r.showCremonesiRankingPerformance());
	}

	public String[] showCremonesiPerformancesByContext() {
		String[] perfs = new String[cremonesiPerformances.size()];
		for(int i=0;i<cremonesiPerformances.size();i++){
			perfs[i] = cremonesiPerformances.get(i);
		}
		return perfs;
	}
	
	
}
