package br.cin.tbookmarks.recommender.database.contextual;

import java.io.Serializable;

import org.apache.mahout.cf.taste.model.Preference;

import com.google.common.base.Preconditions;

/**
 * <p>
 * A simple {@link Preference} encapsulating an item and preference value.
 * </p>
 */
public class ContextualPreference implements ContextualPreferenceInterface, Preference, Serializable {
  
  private final long userID;
  private final long itemID;
  private float value;
  private long contextualPreferences[];
  
  public ContextualPreference(long userID, long itemID, float value, long contextualPreferences[]) {
    Preconditions.checkArgument(!Float.isNaN(value), "NaN value");
    this.userID = userID;
    this.itemID = itemID;
    this.value = value;
    this.contextualPreferences = contextualPreferences;
  }
  
  @Override
  public long getUserID() {
    return userID;
  }
  
  @Override
  public long getItemID() {
    return itemID;
  }
  
  @Override
  public float getValue() {
    return value;
  }
  
  private String contextualPreferencesToString(long cps[]){
	  
	  StringBuffer sb = new StringBuffer();
	  
	  for(int j = 0; j < cps.length; j++){
		  sb.append(cps[j]);  
		  sb.append(j+1 != cps.length ? "|" : "");
      }
	  
	  return sb.toString();
  }
  
  @Override
  public void setValue(float value) {
    Preconditions.checkArgument(!Float.isNaN(value), "NaN value");
    this.value = value;
  }
  
  @Override
  public String toString() {
    return "ContextualPreference[userID: " + userID + ", itemID:" + itemID + ", value:" + value + ", contextualPreferences:" + contextualPreferencesToString(contextualPreferences) +']';
  }
  
  @Override
  public long[] getContextualPreferences() {
	return contextualPreferences;
  }
  
  @Override
  public void setContextualPreferences(long[] contextualPreferences) {
	this.contextualPreferences = contextualPreferences;
  }
 
}
