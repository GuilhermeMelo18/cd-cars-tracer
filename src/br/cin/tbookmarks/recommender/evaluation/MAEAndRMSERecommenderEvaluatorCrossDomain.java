package br.cin.tbookmarks.recommender.evaluation;

import java.util.ArrayList;

import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.recommender.IDRescorer;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.contextual.ContextualPreferenceInterface;

/**
 * <p>
 * A {@link ContextualRecommenderEvaluator} which computes the "root mean squared"
 * difference between predicted and actual ratings for users. This is the square root of the average of this
 * difference, squared.
 * </p>
 */
public final class MAEAndRMSERecommenderEvaluatorCrossDomain extends AbstractDifferenceRecommenderEvaluatorCrossDomain {
  

  private ArrayList<PredictionValues> values;
  private RunningAverage averageMAE;
  private RunningAverage averageRMSE;
  
  public MAEAndRMSERecommenderEvaluatorCrossDomain() {
		
  }
  
  public MAEAndRMSERecommenderEvaluatorCrossDomain(IDRescorer idrescorer, ContextualCriteria contextualCriteria,AbstractDataset dataset) {
	this.idrescorer = idrescorer;
	this.contextualCriteria = contextualCriteria;
	this.dataset = dataset;
	this.noEstimateCounter = 0;
  }
  
  @Override
  protected void reset() {
    averageMAE = new FullRunningAverage();
    averageRMSE = new FullRunningAverage();
    values = new ArrayList<PredictionValues>();
  }
  
  @Override
  protected void processOneEstimate(float estimatedPreference, Preference realPref) {
	  ContextualPreferenceInterface cpRealPref = (ContextualPreferenceInterface) realPref;
	  
	values.add(new PredictionValues(cpRealPref.getValue(), estimatedPreference, cpRealPref.getUserID(),cpRealPref.getItemID(),cpRealPref.getContextualPreferences()));
   
	double diff = realPref.getValue() - estimatedPreference;
    averageMAE.addDatum(Math.abs(diff));
    averageRMSE.addDatum(diff * diff);

  }
  
  public double getMAEResult(){
	  return averageMAE.getAverage();
  }
  
  public double getRMSEResult(){
	  return Math.sqrt(averageRMSE.getAverage());
  }
  
  public ArrayList<PredictionValues> getValues() {
	return values;
  }
  
  @Override
  protected double computeFinalEvaluation() {
    return -1;
  }
  
  @Override
  public String toString() {
    return "MAEAndRMSERecommenderEvaluatorCrossDomain";
  }
  
}

