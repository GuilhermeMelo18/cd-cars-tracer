package br.cin.tbookmarks.recommender.evaluation;

import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.recommender.IDRescorer;

import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

/**
 * <p>
 * A {@link ContextualRecommenderEvaluator} which computes the "root mean squared"
 * difference between predicted and actual ratings for users. This is the square root of the average of this
 * difference, squared.
 * </p>
 */
public final class RMSRecommenderEvaluatorCrossDomain extends AbstractDifferenceRecommenderEvaluatorCrossDomain {
  
  private RunningAverage average;
  
  public RMSRecommenderEvaluatorCrossDomain() {
		
  }
  
  public RMSRecommenderEvaluatorCrossDomain(IDRescorer idrescorer, ContextualCriteria contextualCriteria) {
	this.idrescorer = idrescorer;
	this.contextualCriteria = contextualCriteria;
  }
  
  @Override
  protected void reset() {
    average = new FullRunningAverage();
  }
  
  @Override
  protected void processOneEstimate(float estimatedPreference, Preference realPref) {
    double diff = realPref.getValue() - estimatedPreference;
    average.addDatum(diff * diff);
  }
  
  @Override
  protected double computeFinalEvaluation() {
    return Math.sqrt(average.getAverage());
  }
  
  @Override
  public String toString() {
    return "RMSRecommenderEvaluatoCrossDomain";
  }
  
}

