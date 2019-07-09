package br.cin.tbookmarks.recommender.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.contextual.ContextualDataModel;
import br.cin.tbookmarks.recommender.database.contextual.ContextualUserPreferenceArray;

import com.google.common.primitives.Longs;

public class PreFilteringContextualBuildRecommenderByron implements
ContextualRecommenderBuilder {

	private ContextualCriteria contextualAttributes;
	private ContextualRecommenderBuilder recommenderBuilder;
	private DataModel dataModel;
	private IDRescorer rescorer;
	
	/*public PreFilteringContextualBuildRecommender(ContextualCriteria contexutalAttributes, RecommenderBuilder recommenderBuilder) {
		this.contextualAttributes = contexutalAttributes;
		this.recommenderBuilder = recommenderBuilder;
	}*/
	public PreFilteringContextualBuildRecommenderByron(ContextualRecommenderBuilder recommenderBuilder) {
		this.recommenderBuilder = recommenderBuilder;
	}
	
	public DataModel getDataModel() {
		return dataModel;
	}
	
	
	
	public ContextualCriteria getContextualAttributes() {
		return contextualAttributes;
	}

	public void setContextualAttributes(ContextualCriteria contextualAttributes) {
		this.contextualAttributes = contextualAttributes;
	}

	public IDRescorer getRescorer() {
		return rescorer;
	}

	public void setRescorer(IDRescorer rescorer) {
		this.rescorer = rescorer;
	}

	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	public ContextualRecommenderBuilder getRecommenderBuilder() {
		return recommenderBuilder;
	}

	public void setRecommenderBuilder(ContextualRecommenderBuilder recommenderBuilder) {
		this.recommenderBuilder = recommenderBuilder;
	}

	@Override
	public String toString() {
		return "PreF_Byron"+"(CF-based="+recommenderBuilder+")";
	}
	
	public DataModel preFilterDataModel(DataModel model) throws TasteException{
		
		//criar um novo datamodel verificando cada preferencia e adicionando no novo datamodel caso case com o contexto
		
		FastByIDMap<PreferenceArray> preferences = new FastByIDMap<PreferenceArray>();
		LongPrimitiveIterator userIdsIterator = model.getUserIDs();
		
		
		while(userIdsIterator.hasNext()){
			
			Long userId = userIdsIterator.next();
			PreferenceArray prefsForUser = model.getPreferencesFromUser(userId);
			if(contextualAttributes != null && prefsForUser instanceof ContextualUserPreferenceArray){
				ContextualUserPreferenceArray contextualPrefsForUser = (ContextualUserPreferenceArray) prefsForUser;
				ArrayList<Long> newItemIds = new ArrayList<Long>();
				ArrayList<Float> newPrefValues = new ArrayList<Float>();
				ArrayList<List<Long>> newContextualPrefs = new ArrayList<List<Long>>();
				
				for(int i = 0; i < contextualPrefsForUser.getIDs().length; i++){
					
					if((rescorer != null && rescorer.isFiltered(contextualPrefsForUser.get(i).getItemID())) ||
							
							(contextualAttributes.containsAllContextualAttributesIgnoringUnkwnown(contextualPrefsForUser.get(i).getContextualPreferences()))){
						
						newItemIds.add(contextualPrefsForUser.get(i).getItemID());
						newPrefValues.add(contextualPrefsForUser.get(i).getValue());
						Long[] longObjects = ArrayUtils.toObject(contextualPrefsForUser.get(i).getContextualPreferences());
						newContextualPrefs.add(Arrays.asList(longObjects));
					}
					
				}
				
				if(newItemIds.size() > 0 && newContextualPrefs.size() > 0){
					ContextualUserPreferenceArray newPrefsForUser = new ContextualUserPreferenceArray(newItemIds.size());
					newPrefsForUser.setUserID(0, userId);
					
					for(int n=0; n < newItemIds.size();n++){
						newPrefsForUser.setItemID(n, newItemIds.get(n));
						newPrefsForUser.setValue(n, newPrefValues.get(n));
						newPrefsForUser.setContextualPreferences(n, Longs.toArray(newContextualPrefs.get(n)));
						
					}
					
					preferences.put(userId, newPrefsForUser);
				}
			}else{
				preferences.put(userId, prefsForUser);
			}
			
		}
		//System.out.println(counter);
		DataModel filteredDataModel = new ContextualDataModel(preferences);
		
		return filteredDataModel;
	}
	
	

	@Override
	public Recommender buildRecommender(DataModel dataModel,
			ContextualCriteria criteria, IDRescorer rescorer, AbstractDataset dataset)
			throws TasteException {
		this.contextualAttributes = criteria;
		this.rescorer = rescorer;
		
		
		this.dataModel = this.preFilterDataModel(dataModel);
		
		return this.recommenderBuilder.buildRecommender(this.dataModel, criteria, rescorer, dataset);
	}

	
	
}
