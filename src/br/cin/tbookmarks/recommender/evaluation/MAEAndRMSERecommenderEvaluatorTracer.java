package br.cin.tbookmarks.recommender.evaluation;

import java.util.ArrayList;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import br.cin.tbookmarks.recommender.GenericUserBasedRecommenderTracer;
import br.cin.tbookmarks.recommender.PostFilteringContextualRecommenderTracer;
import br.cin.tbookmarks.recommender.algorithms.ContextualRecommenderBuilder;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

public class MAEAndRMSERecommenderEvaluatorTracer extends AbstractDifferenceRecommenderEvaluatorCrossDomain {

	private double MAE;
	private double RMSE;
	private double MAEPosFilter;
	private double RMSEPosFilter;
	private double MAEValidate;
	private double RMSEValidate;
	private int totalOfTrainingRatingsFromSource;
	private int totalOfTrainingRatingsFromTarget;
	private int totalOfTestRatings;
	private int NotCounterAvaliateTestRatings;
	private double overlapingRate;
	private double quantityUserOvelaping;
	private static final Logger log = LoggerFactory
			.getLogger(AbstractDifferenceRecommenderEvaluatorCrossDomain.class);
	
	private ArrayList<PredictionValues> values;
	private ArrayList<PredictionValues> valuesPosFilter;
	
	
	
	
	public MAEAndRMSERecommenderEvaluatorTracer(IDRescorer idrescorer, ContextualCriteria contextualCriteria,AbstractDataset dataset) {
		
		this.idrescorer = idrescorer;
		this.contextualCriteria = contextualCriteria;
		this.dataset = dataset;
	}
	
	@Override
	protected void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processOneEstimate(float estimatedPreference, Preference realPref) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected double computeFinalEvaluation() {
		// TODO Auto-generated method stub
		return -1;
	}
	
	public double getMAEResult(){
		 
		return this.MAE;
	}
	  
	public double getRMSEResult(){
		  return this.RMSE;
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
	
	

	public int getNotCounterAvaliateTestRatings() {
		return NotCounterAvaliateTestRatings;
	}

	public void setNotCounterAvaliateTestRatings(int notCounterAvaliateTestRatings) {
		NotCounterAvaliateTestRatings = notCounterAvaliateTestRatings;
	}

	public int getTotalOfTrainingRatingsFromSource() {
		return totalOfTrainingRatingsFromSource;
	}

	public void setTotalOfTrainingRatingsFromSource(int totalOfTrainingRatingsFromSource) {
		this.totalOfTrainingRatingsFromSource = totalOfTrainingRatingsFromSource;
	}

	public int getTotalOfTrainingRatingsFromTarget() {
		return totalOfTrainingRatingsFromTarget;
	}

	public void setTotalOfTrainingRatingsFromTarget(int totalOfTrainingRatingsFromTarget) {
		this.totalOfTrainingRatingsFromTarget = totalOfTrainingRatingsFromTarget;
	}

	public int getTotalOfTestRatings() {
		return totalOfTestRatings;
	}

	public void setTotalOfTestRatings(int totalOfTestRatings) {
		this.totalOfTestRatings = totalOfTestRatings;
	}
	
	

	public double getOverlapingRate() {
		return overlapingRate;
	}

	public void setOverlapingRate(double overlapingRate) {
		this.overlapingRate = overlapingRate;
	}

	public double getQuantityUserOvelaping() {
		return quantityUserOvelaping;
	}

	public void setQuantityUserOvelaping(double quantityUserOvelaping) {
		this.quantityUserOvelaping = quantityUserOvelaping;
	}

	public void setMAEResult(double MAE){
		 
		this.MAE = MAE;
	}
	  
	public void setRMSEResult(double RMSE){
		 
		this.RMSE = RMSE;
	}
	 
	public double getMAEValidate() {
		return MAEValidate;
	}

	public void setMAEValidate(double MAEValidate) {
		this.MAEValidate = MAEValidate;
	}

	public double getRMSEValidate() {
		return RMSEValidate;
	}

	public void setRMSEValidate(double RMSEValidate) {
		this.RMSEValidate = RMSEValidate;
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

	@Override
	public double evaluate(ContextualRecommenderBuilder recommenderBuilder,
			DataModelBuilder dataModelBuilder, DataModel dataModel,
			double trainingPercentage, double evaluationPercentage)
			throws TasteException {
		
		Preconditions.checkNotNull(recommenderBuilder);
		Preconditions.checkNotNull(dataModel);
		Preconditions.checkArgument(trainingPercentage >= 0.0
				&& trainingPercentage <= 1.0, "Invalid trainingPercentage: "
				+ trainingPercentage
				+ ". Must be: 0.0 <= trainingPercentage <= 1.0");
		Preconditions.checkArgument(evaluationPercentage >= 0.0
				&& evaluationPercentage <= 1.0,
				"Invalid evaluationPercentage: " + evaluationPercentage
						+ ". Must be: 0.0 <= evaluationPercentage <= 1.0");


		
		
		Recommender recommender = recommenderBuilder.buildRecommender(dataModel, contextualCriteria, idrescorer, this.dataset);
		GenericUserBasedRecommenderTracer rec = null;
		PostFilteringContextualRecommenderTracer recPos = null;
		
		if(recommender instanceof GenericUserBasedRecommenderTracer) {

			rec = ((GenericUserBasedRecommenderTracer) recommender);
			
		}else if(recommender instanceof PostFilteringContextualRecommenderTracer){

			recPos = ((PostFilteringContextualRecommenderTracer) recommender);
		}
		

		
		double result = -1;
		
		if(rec != null){
			
			result = rec.getMAE();
			getEvaluation(null, rec);
			
			setMAEResult(rec.getMAE());
			setRMSEResult(rec.getRMSE());
			setMAEValidate(rec.getMAEValidate());
			setRMSEValidate(rec.getRMSEValidate());
			setTotalOfTestRatings(rec.getTotalOfTestRatings());
			setTotalOfTrainingRatingsFromSource(rec.getTotalOfTrainingRatingsFromSource());
			setTotalOfTrainingRatingsFromTarget(rec.getTotalOfTrainingRatingsFromTarget());
			setValues(rec.getValues());
			setNoEstimateCounter(rec.getNotCounterAvaliateTestRatings());
			
			
		}else if(recPos != null) {
			
			result = recPos.getMAE();
			getEvaluation(null, recPos);
			
			setMAEResult(recPos.getMAE());
			setRMSEResult(recPos.getRMSE());
			setMAEValidate(recPos.getMAEValidate());
			setRMSEValidate(recPos.getRMSEValidate());
			setMAEPosFilter(recPos.getMAEPosFilter());
			setRMSEPosFilter(recPos.getRMSEPosFilter());
			setTotalOfTestRatings(recPos.getTotalOfTestRatings());
			setTotalOfTrainingRatingsFromSource(recPos.getTotalOfTrainingRatingsFromSource());
			setTotalOfTrainingRatingsFromTarget(recPos.getTotalOfTrainingRatingsFromTarget());
			setValues(recPos.getValues());
			setValuesPosFilter(recPos.getValuesPosFilter());
			setNoEstimateCounter(recPos.getNotCounterAvaliateTestRatings());
			
		}
		

		//double 
		log.info("Evaluation result: {}", result);
		return result;
	}
	
	@Override
	public String toString() {
	   return "MAEAndRMSERecommenderEvaluatorTracer";
	}
	  
	
	
	
	
	

}
