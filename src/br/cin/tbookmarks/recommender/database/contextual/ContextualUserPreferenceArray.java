package br.cin.tbookmarks.recommender.database.contextual;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.iterator.CountingIterator;

/**
 * <p>
 * Like {@link ContextualItemPreferenceArray} but stores preferences for one user (all user IDs the same) rather
 * than one item.
 * </p>
 *
 * <p>
 * This implementation maintains two parallel arrays, of item IDs and values. The idea is to save allocating
 * {@link Preference} objects themselves. This saves the overhead of {@link Preference} objects but also
 * duplicating the user ID value.
 * </p>
 * 
 * @see BooleanUserPreferenceArray
 * @see ContextualItemPreferenceArray
 * @see GenericPreference
 */
public final class ContextualUserPreferenceArray implements PreferenceArray {

  private static final int ITEM = 1;
  private static final int VALUE = 2;
  private static final int VALUE_REVERSED = 3;

  private final long[] ids;
  private long id;
  private final float[] values;
  private final long[][] contextualPreferences;

  public ContextualUserPreferenceArray(int size) {
    this.ids = new long[size];
    values = new float[size];
    this.contextualPreferences = new long[size][];
    this.id = Long.MIN_VALUE; // as a sort of 'unspecified' value
  }

  public ContextualUserPreferenceArray(List<? extends Preference> prefs) {
    this(prefs.size());
    int size = prefs.size();
    long userID = Long.MIN_VALUE;
    for (int i = 0; i < size; i++) {
    	Preference pref = prefs.get(i);
      if (i == 0) {
        userID = pref.getUserID();
      } else {
        if (userID != pref.getUserID()) {
          throw new IllegalArgumentException("Not all user IDs are the same");
        }
      }
      ids[i] = pref.getItemID();
      values[i] = pref.getValue();
      if(pref instanceof ContextualPreference){
      	contextualPreferences[i] = ((ContextualPreference)pref).getContextualPreferences();
      }else if(pref instanceof ContextualUserPreferenceArray.PreferenceView){
    	  contextualPreferences[i] = ((ContextualUserPreferenceArray.PreferenceView) pref)
		.getContextualPreferences();
      }
    }
    id = userID;
  }

  /**
   * This is a private copy constructor for clone().
   */
  private ContextualUserPreferenceArray(long[] ids, long id, float[] values, long[][] contextualPreferences) {
    this.ids = ids;
    this.id = id;
    this.values = values;
    this.contextualPreferences = contextualPreferences;
  }

  @Override
  public int length() {
    return ids.length;
  }

  @Override
  public ContextualPreferenceInterface get(int i) {
    return new PreferenceView(i);
  }

  @Override
  public void set(int i, Preference pref) {
    id = pref.getUserID();
    ids[i] = pref.getItemID();
    values[i] = pref.getValue();
    if(pref instanceof ContextualPreference){
    	contextualPreferences[i] = ((ContextualPreference)pref).getContextualPreferences();
    }
  }
  

  @Override
  public long getUserID(int i) {
    return id;
  }

  /**
   * {@inheritDoc}
   * 
   * Note that this method will actually set the user ID for <em>all</em> preferences.
   */
  @Override
  public void setUserID(int i, long userID) {
    id = userID;
  }

  @Override
  public long getItemID(int i) {
    return ids[i];
  }

  @Override
  public void setItemID(int i, long itemID) {
    ids[i] = itemID;
  }

  /**
   * @return all item IDs
   */
  @Override
  public long[] getIDs() {
    return ids;
  }

  @Override
  public float getValue(int i) {
    return values[i];
  }

  @Override
  public void setValue(int i, float value) {
    values[i] = value;
  }
  
  public long[] getContextualPreferences(int i) {
		return contextualPreferences[i];
  }
	  
  public void setContextualPreferences(int i,long[] contextualPreferences) {
	  this.contextualPreferences[i] = contextualPreferences;
  }

  @Override
  public void sortByUser() { }

  @Override
  public void sortByItem() {
    lateralSort(ITEM);
  }

  @Override
  public void sortByValue() {
    lateralSort(VALUE);
  }

  @Override
  public void sortByValueReversed() {
    lateralSort(VALUE_REVERSED);
  }

  @Override
  public boolean hasPrefWithUserID(long userID) {
    return id == userID;
  }

  @Override
  public boolean hasPrefWithItemID(long itemID) {
    for (long id : ids) {
      if (itemID == id) {
        return true;
      }
    }
    return false;
  }

  private void lateralSort(int type) {
    //Comb sort: http://en.wikipedia.org/wiki/Comb_sort
    int length = length();
    int gap = length;
    boolean swapped = false;
    while (gap > 1 || swapped) {
      if (gap > 1) {
        gap /= 1.247330950103979; // = 1 / (1 - 1/e^phi)
      }
      swapped = false;
      int max = length - gap;
      for (int i = 0; i < max; i++) {
        int other = i + gap;
        if (isLess(other, i, type)) {
          swap(i, other);
          swapped = true;
        }
      }
    }
  }

  private boolean isLess(int i, int j, int type) {
    switch (type) {
      case ITEM:
        return ids[i] < ids[j];
      case VALUE:
        return values[i] < values[j];
      case VALUE_REVERSED:
        return values[i] > values[j];
      default:
        throw new IllegalStateException();
    }
  }

  private void swap(int i, int j) {
    long temp1 = ids[i];
    float temp2 = values[i];
    long temp3[] = contextualPreferences[i];
    ids[i] = ids[j];
    values[i] = values[j];
    contextualPreferences[i] = contextualPreferences[j];
    ids[j] = temp1;
    values[j] = temp2;
    contextualPreferences[j] = temp3;
  }

  @Override
  public ContextualUserPreferenceArray clone() {
    return new ContextualUserPreferenceArray(ids.clone(), id, values.clone(),contextualPreferences.clone());
  }

  @Override
  public int hashCode() {
    return (int) (id >> 32) ^ (int) id ^ Arrays.hashCode(ids) ^ Arrays.hashCode(values) ^ Arrays.hashCode(contextualPreferences);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ContextualUserPreferenceArray)) {
      return false;
    }
    ContextualUserPreferenceArray otherArray = (ContextualUserPreferenceArray) other;
    return id == otherArray.id && Arrays.equals(ids, otherArray.ids) && Arrays.equals(values, otherArray.values)  && Arrays.equals(contextualPreferences, otherArray.contextualPreferences);
  }

  @Override
  public Iterator<Preference> iterator() {
    return Iterators.transform(new CountingIterator(length()),
      new Function<Integer, Preference>() {
        @Override
        public Preference apply(Integer from) {
          return new PreferenceView(from);
        }
      });
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
  public String toString() {
    if (ids == null || ids.length == 0) {
      return "ContextualUserPreferenceArray[{}]";
    }
    StringBuilder result = new StringBuilder(20 * ids.length);
    result.append("ContextualUserPreferenceArray[userID:");
    result.append(id);
    result.append(",{");
    for (int i = 0; i < ids.length; i++) {
      if (i > 0) {
        result.append(',');
      }
      result.append(ids[i]);
      result.append('=');
      result.append(values[i]+" ");
      result.append(contextualPreferencesToString(contextualPreferences[i]));
      
    }
    result.append("}]");
    return result.toString();
  }

  private final class PreferenceView implements ContextualPreferenceInterface {

    private final int i;

    private PreferenceView(int i) {
      this.i = i;
    }

    @Override
    public long getUserID() {
      return ContextualUserPreferenceArray.this.getUserID(i);
    }

    @Override
    public long getItemID() {
      return ContextualUserPreferenceArray.this.getItemID(i);
    }
    
    @Override
	public long[] getContextualPreferences() {
        return ContextualUserPreferenceArray.this.getContextualPreferences(i);
      }

    @Override
    public float getValue() {
      return values[i];
    }

    @Override
    public void setValue(float value) {
      values[i] = value;
    }

	@Override
	public void setContextualPreferences(long[] contextualPreference) {
		contextualPreferences[i] = contextualPreference;
		
	}
    
    }

}
