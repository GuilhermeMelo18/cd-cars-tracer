package br.cin.tbookmarks.recommender.evaluation;

import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.recommender.IDRescorer;

import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;

public class AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain  extends AbstractDifferenceRecommenderEvaluatorCrossDomain{
	  
	  private RunningAverage average;
	  
	  public AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain() {
			
	  }
	  
	  public AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain(IDRescorer idrescorer, ContextualCriteria contextualCriteria) {
		this.idrescorer = idrescorer;
		this.contextualCriteria = contextualCriteria;
	  }
	  
	  @Override
	  protected void reset() {
	    average = new FullRunningAverage();
	  }
	  
	  @Override
	  protected void processOneEstimate(float estimatedPreference, Preference realPref) {
	    average.addDatum(Math.abs(realPref.getValue() - estimatedPreference));
	  }
	  
	  @Override
	  protected double computeFinalEvaluation() {
	    return average.getAverage();
	  }
	  
	  @Override
	  public String toString() {
	    return "AverageAbsoluteDifferenceRecommenderEvaluatorCrossDomain";
	  }
	  
}
