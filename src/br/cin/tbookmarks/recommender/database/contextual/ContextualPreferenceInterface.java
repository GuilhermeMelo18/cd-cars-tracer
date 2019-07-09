package br.cin.tbookmarks.recommender.database.contextual;

import org.apache.mahout.cf.taste.model.Preference;

public interface ContextualPreferenceInterface extends Preference{
	public long[] getContextualPreferences();
	  
	  public void setContextualPreferences(long[] contextualPreferences);
}
