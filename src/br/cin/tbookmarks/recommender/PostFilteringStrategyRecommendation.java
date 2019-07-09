package br.cin.tbookmarks.recommender;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.Recommender;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.GenreRulesByContextMap;
import br.cin.tbookmarks.recommender.database.UserCategoriesPrefsInContexts;
import br.cin.tbookmarks.recommender.database.contextual.AprioriRuleItemCategory;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.contextual.ContextualUserPreferenceArray;
import br.cin.tbookmarks.recommender.database.item.ItemCategory;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.database.user.RuleTuple;

public class PostFilteringStrategyRecommendation {
	public static enum PossibleFilteringStrategies implements AbstractPostFilteringStrategiesEnum{

		AT_LEAST_ONE_OCCURENCY, AT_LEAST_TWO_OCCURRENCIES, MOST_OCCURRED, AT_LEAST_MEDIA_OF_OCCURRENCIES;
		
	}
	
	public static enum PossibleAdjustingStrategies implements AbstractPostFilteringStrategiesEnum{

		NUMBER_OF_CATEGORIES, NUMBER_OF_OCCURENCIES;
		
	}
	
	private AbstractPostFilteringStrategiesEnum postFilteringStrategy;
	
	private float threshold_occurs;
	private boolean onlyWithGoodRatings;
	private float goodRatingMin;
	private double contextConfidenceInGenreRules;
	private double contextSupportInGenreRules;

	public double getContextConfidenceInGenreRules() {
		return contextConfidenceInGenreRules;
	}
	
	public double getContextSupportInGenreRules() {
		return contextSupportInGenreRules;
	}
	
	public float getThreshold_occurs() {
		return threshold_occurs;
	}
	
	public void setThreshold_occurs(float threshold_occurs) {
		this.threshold_occurs = threshold_occurs;
	}
	
	public boolean isOnlyWithGoodRatings() {
		return onlyWithGoodRatings;
	}
	
	public float getGoodRatingMin() {
		return goodRatingMin;
	}
	
	public void setGoodRatingMin(float goodRatingMin) {
		this.goodRatingMin = goodRatingMin;
	}
	
	public void setOnlyWithGoodRatings(boolean onlyWithGoodRatings) {
		this.onlyWithGoodRatings = onlyWithGoodRatings;
	}
	
	//private  HashMap<Long,HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>>> userPrefs = UserCategoriesPrefsInContexts.getInstance().getUserPrefs();
	
	private UserCategoriesPrefsInContexts userCategoriesPrefsInContextsInstance = UserCategoriesPrefsInContexts.getInstance();
	
	private GenreRulesByContextMap genreRulesByContextInstance = GenreRulesByContextMap.getInstance();
	
	public PostFilteringStrategyRecommendation(AbstractPostFilteringStrategiesEnum postFilteringStrategy, boolean onlyWithGoodRatings, float goodRatingMin,float threshold,double contextConfidenceInGenreRules, double contextSupportInGenreRules) {
		this.postFilteringStrategy = postFilteringStrategy;
		this.onlyWithGoodRatings = onlyWithGoodRatings;
		this.goodRatingMin = goodRatingMin;
		this.threshold_occurs = threshold;
		this.contextConfidenceInGenreRules = contextConfidenceInGenreRules;
		this.contextSupportInGenreRules = contextSupportInGenreRules;
	}
	
	/*private String[] convertToStringList(long[] n){
		
		String[] converted = new String[n.length];
		
		for(int i=0; i<n.length; i++){
			converted[i] = String.valueOf(n[i]);
		}
		
		return converted;
	}*/
		
	public AbstractPostFilteringStrategiesEnum getPostFilteringStrategy() {
		return postFilteringStrategy;
	}
	
	private HashMap<ItemDomain,HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>>> getContextualCategoryPreferences(
			long userId, DataModel model, AbstractDataset dataset, boolean onlyGoodRatings) throws TasteException {
		
		Long userKey = new Long(userId);
		
		HashMap<ItemDomain,HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>>> contexttualCategoryPrefs = userCategoriesPrefsInContextsInstance.getUserPrefsWithGoodRatingsOnly().get(userKey);
		
		if(contexttualCategoryPrefs == null){
			
			contexttualCategoryPrefs = new HashMap<ItemDomain,HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>>>();
			
			PreferenceArray prefs = model.getPreferencesFromUser(userId);
			
			int size = prefs.length();
		    boolean isInstanceOfContextualUserPreferenceArray = prefs instanceof ContextualUserPreferenceArray;
		    
		    if(!isInstanceOfContextualUserPreferenceArray){
		    	throw new TasteException("Prefs are not ContextualUserPreferenceArray for Post-Filtering approach");
		    }
			    
			for (int i = 0; i < size; i++) {
				
				if(onlyGoodRatings && prefs.getValue(i) < this.getGoodRatingMin()){
					continue;
				}
				
				long[] contexts = ((ContextualUserPreferenceArray)prefs).getContextualPreferences(i);
				
				ContextualCriteria cc = new ContextualCriteria(contexts);
				
				Set<ItemCategory> categories = dataset.getItemInformationByID(prefs.getItemID(i)).getCategories();
				
				ItemDomain domain = dataset.getItemInformationByID(prefs.getItemID(i)).getItemDomain();
				
				HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>> contexualCriteriaMap = contexttualCategoryPrefs.get(domain);
				
				if(contexualCriteriaMap == null){
					HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>> newContexualCriteriaMap = new HashMap<ContextualCriteria, HashMap<ItemCategory,Integer>>();
					
					
					HashMap<ItemCategory,Integer> newCategoryMap = new HashMap<ItemCategory, Integer>();
					
					for(ItemCategory category : categories){
						Integer numberOfOccurrences = newCategoryMap.get(category);
						if(numberOfOccurrences == null){
							newCategoryMap.put(category, 1);
						}else{
							newCategoryMap.put(category, ++numberOfOccurrences);
						}
						
					}
					newContexualCriteriaMap.put(cc, newCategoryMap);
					contexttualCategoryPrefs.put(domain, newContexualCriteriaMap);
					
				}else{

					//HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>> newContexualCriteriaMap = new HashMap<ContextualCriteria, HashMap<ItemCategory,Integer>>();
					
					HashMap<ItemCategory,Integer> categoryMap = contexualCriteriaMap.get(cc);
					
					if(categoryMap == null){
						HashMap<ItemCategory,Integer> newCategoryMap = new HashMap<ItemCategory, Integer>();
						
						for(ItemCategory category : categories){
							Integer numberOfOccurrences = newCategoryMap.get(category);
							if(numberOfOccurrences == null){
								newCategoryMap.put(category, 1);
							}else{
								newCategoryMap.put(category, ++numberOfOccurrences);
							}
							
						}
						contexualCriteriaMap.put(cc, newCategoryMap);
						//contexttualCategoryPrefs.put(cc, newCategoryMap);
					}else{
						for(ItemCategory category : categories){
							Integer numberOfOccurrences = categoryMap.get(category);
							if(numberOfOccurrences == null){
								categoryMap.put(category, 1);
							}else{
								categoryMap.put(category, ++numberOfOccurrences);
							}
							
						}
					}
				
				}
				
				
				
			}
		
			userCategoriesPrefsInContextsInstance.setUserPrefsWithGoodRatingsOnlyToUser(userKey, contexttualCategoryPrefs);
		}
		
		return contexttualCategoryPrefs;
	
	}
	
	/*private HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>> getContextualCategoryPreferences(long userId, DataModel model,AbstractDataset dataset) throws TasteException{
		
		Long userKey = new Long(userId);
		
		HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>> contexttualCategoryPrefs = userCategoriesPrefsInContextsInstance.getUserPrefs().get(userKey);
		
		if(contexttualCategoryPrefs == null){
			
			contexttualCategoryPrefs = new HashMap<ContextualCriteria, HashMap<ItemCategory,Integer>>();
			
			PreferenceArray prefs = model.getPreferencesFromUser(userId);
			
			int size = prefs.length();
		    boolean isInstanceOfContextualUserPreferenceArray = prefs instanceof ContextualUserPreferenceArray;
		    
		    if(!isInstanceOfContextualUserPreferenceArray){
		    	throw new TasteException("Prefs are not ContextualUserPreferenceArray for Post-Filtering approach");
		    }
			    
			for (int i = 0; i < size; i++) {
				
				long[] contexts = ((ContextualUserPreferenceArray)prefs).getContextualPreferences(i);
				
				ContextualCriteria cc = new ContextualCriteria(contexts);
				
				Set<ItemCategory> categories = dataset.getItemInformationByID(prefs.getItemID(i)).getCategories();
				
				HashMap<ItemCategory,Integer> categoryMap = contexttualCategoryPrefs.get(cc);
				
				if(categoryMap == null){
					HashMap<ItemCategory,Integer> newCategoryMap = new HashMap<ItemCategory, Integer>();
					
					for(ItemCategory category : categories){
						Integer numberOfOccurrences = newCategoryMap.get(category);
						if(numberOfOccurrences == null){
							newCategoryMap.put(category, 1);
						}else{
							newCategoryMap.put(category, ++numberOfOccurrences);
						}
						
					}
					contexttualCategoryPrefs.put(cc, newCategoryMap);
				}else{
					for(ItemCategory category : categories){
						Integer numberOfOccurrences = categoryMap.get(category);
						if(numberOfOccurrences == null){
							categoryMap.put(category, 1);
						}else{
							categoryMap.put(category, ++numberOfOccurrences);
						}
						
					}
				}
				
			}
		
			userCategoriesPrefsInContextsInstance.setUserPrefsToUser(userKey, contexttualCategoryPrefs);
		}
		
		return contexttualCategoryPrefs;
	}*/
	
	/*private boolean containsAtLeastOneCategory(ItemCategory categoryItem, Set<ItemCategory> categories){
		for(ItemCategory category : categories){
			if(categoryItem.equals(category)){
				return true;
			}
		}
		
		return false;
	}*/
	
	public float filterOrAdjustPreference(long userID, long itemID,
			ContextualCriteria contextualAttributes, Recommender delegated, AbstractDataset dataset) throws TasteException {
		
		ItemDomain domain = dataset.getItemInformationByID(itemID).getItemDomain();

		HashMap<ItemDomain,HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>>> contexttualCategoryPrefs;//= new HashMap<ItemDomain, HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>>>();
		if(this.isOnlyWithGoodRatings()){
			contexttualCategoryPrefs = getContextualCategoryPreferences(userID,delegated.getDataModel(),dataset,true);
		}else{
			contexttualCategoryPrefs =  getContextualCategoryPreferences(userID,delegated.getDataModel(),dataset,false);
		}
		
		if(this.postFilteringStrategy.equals(PossibleFilteringStrategies.AT_LEAST_ONE_OCCURENCY)){
			HashMap<ItemCategory, Integer> userCategoryPrefsByContext = getPreferredCategoriesByContext(contextualAttributes,contexttualCategoryPrefs,domain,true);
			
			if(userCategoryPrefsByContext.isEmpty()){ //target domain sem prefs, pega dos sources relacionados
				userCategoryPrefsByContext = getPreferredCategoriesByContext(contextualAttributes,contexttualCategoryPrefs,domain,false);
			}
			
			for(ItemCategory categoryPreferred : userCategoryPrefsByContext.keySet()){
				if(categoryPreferred.containsAtLeastOneCategory(dataset.getItemInformationByID(itemID).getCategories())){
					return delegated.estimatePreference(userID, itemID);
				}
			}
		}else if(this.postFilteringStrategy.equals(PossibleFilteringStrategies.AT_LEAST_TWO_OCCURRENCIES)){
			HashMap<ItemCategory, Integer> userCategoryPrefsByContext = getPreferredCategoriesByContext(contextualAttributes,contexttualCategoryPrefs,domain,true);
			
			if(userCategoryPrefsByContext.isEmpty()){ //target domain sem prefs, pega dos sources relacionados
				userCategoryPrefsByContext = getPreferredCategoriesByContext(contextualAttributes,contexttualCategoryPrefs,domain,false);
			}
			
			for(ItemCategory categoryPreferred : userCategoryPrefsByContext.keySet()){
				Integer numberOfOccurrences = userCategoryPrefsByContext.get(categoryPreferred);
				if(numberOfOccurrences >= 2 && categoryPreferred.containsAtLeastOneCategory(dataset.getItemInformationByID(itemID).getCategories())){
					return delegated.estimatePreference(userID, itemID);
				}
			}
			
		}else if(this.postFilteringStrategy.equals(PossibleFilteringStrategies.MOST_OCCURRED)){
			
			HashMap<ItemCategory, Integer> userCategoryPrefsByContext = getPreferredCategoriesByContext(contextualAttributes,contexttualCategoryPrefs,domain,true);
			
			if(userCategoryPrefsByContext.isEmpty()){ //target domain sem prefs, pega dos sources relacionados
				userCategoryPrefsByContext = getPreferredCategoriesByContext(contextualAttributes,contexttualCategoryPrefs,domain,false);
			}
			
			HashSet<ItemCategory> mostOccurred = new HashSet<ItemCategory>();
			Integer occurrencies = 0;
			for(ItemCategory categoryPreferred : userCategoryPrefsByContext.keySet()){
				if(mostOccurred.size() == 0){
					mostOccurred.add(categoryPreferred);
					occurrencies = userCategoryPrefsByContext.get(categoryPreferred);
				}else{
					if(userCategoryPrefsByContext.get(categoryPreferred) > occurrencies){
						mostOccurred = new HashSet<ItemCategory>();
						mostOccurred.add(categoryPreferred);
						occurrencies = userCategoryPrefsByContext.get(categoryPreferred);
					}else if(userCategoryPrefsByContext.get(categoryPreferred) == occurrencies){
						mostOccurred.add(categoryPreferred);
					}
				}
			}
			for(ItemCategory categoryMost : mostOccurred){
				if(categoryMost.containsAtLeastOneCategory(dataset.getItemInformationByID(itemID).getCategories())){
					return delegated.estimatePreference(userID, itemID);
				}
			}
			
		}else if(this.postFilteringStrategy.equals(PossibleFilteringStrategies.AT_LEAST_MEDIA_OF_OCCURRENCIES)){
			
			//if(contexttualCategoryPrefs.get(contextualAttributes) !=null){
			
			boolean useInferredRules = false;
			
			HashMap<ItemCategory, Integer> userCategoryPrefsByContext = getPreferredCategoriesByContext(contextualAttributes,contexttualCategoryPrefs,domain,true);
			
			if(userCategoryPrefsByContext.isEmpty()){ //target domain sem prefs, pega dos sources relacionados
				userCategoryPrefsByContext = getPreferredCategoriesByContext(contextualAttributes,contexttualCategoryPrefs,domain,false);
				useInferredRules = true;
			}
			
			int max = 0;
			
			Integer occurrencies = 0;
			for(ItemCategory categoryPreferred : userCategoryPrefsByContext.keySet()){
				occurrencies = userCategoryPrefsByContext.get(categoryPreferred);
				
				if(occurrencies > max){
					max = occurrencies;
				}
				
			}
			int media = new Float(max*threshold_occurs).intValue();
			
			for(ItemCategory categoryPreferred : userCategoryPrefsByContext.keySet()){
				Integer numberOfOccurrences = userCategoryPrefsByContext.get(categoryPreferred);
				
				if(numberOfOccurrences >= media){
					
					if(useInferredRules){

						RuleTuple condition = new RuleTuple(categoryPreferred.getCategoryDomain(), contextualAttributes, categoryPreferred);
						
						HashSet<RuleTuple> rtInferred = getInferredRuleTuple(condition,dataset);
						if(!rtInferred.isEmpty()){
							ItemCategory inferredCategory = null;
							for(RuleTuple rtTemp : rtInferred){
								if(rtTemp.getItemDomain().equals(domain) && contextualAttributes.containsAllContextualAttributesIgnoringUnknown(rtTemp.getContext())){
									inferredCategory = rtTemp.getItemCategory();
									if(inferredCategory != null && inferredCategory.containsAtLeastOneCategory(dataset.getItemInformationByID(itemID).getCategories())){
										return delegated.estimatePreference(userID, itemID);
									 }
								}
							}
						}
						
						
					}else if(categoryPreferred.containsAtLeastOneCategory(dataset.getItemInformationByID(itemID).getCategories())){
						return delegated.estimatePreference(userID, itemID);
					 }
				}
			}
			
		}
		
		//System.out.println("Not estimated because post-filtering: "+userID+" "+itemID+" "+delegated.estimatePreference(userID, itemID));
		//System.out.println(contexttualCategoryPrefs);
		
		return Float.NaN;
	}

	private HashSet<RuleTuple> getInferredRuleTuple(RuleTuple condition,
			AbstractDataset dataset) {
		HashSet<AprioriRuleItemCategory> rules = dataset.getGenreRulesDatasetInformation();
		double datasetConfidence = this.contextConfidenceInGenreRules;
		double datasetSupport  = this.contextSupportInGenreRules;
		
		//double datasetConfidence =-1;
		//double datasetSupport  = -1;
		
		HashSet<RuleTuple> inferredRules = new HashSet<RuleTuple>();
		
		if(genreRulesByContextInstance.getGenreRulesByContextMap().get(condition) == null){
		
			for(AprioriRuleItemCategory rule : rules){
				if(rule.getPrecedent().equals(condition.getItemCategory())){
					
					int total = rule.getPrecedentContexts().size();
					int counter2Support = 0;
					int counter2Confidence = 0;
					
					for(int i=0;i<total;i++){
						if(condition.getContext().containsAllContextualAttributesIgnoringUnknown(rule.getPrecedentContexts().get(i))){
							counter2Support++;
							if(condition.getContext().containsAllContextualAttributesIgnoringUnknown(rule.getConsequentContexts().get(i))){
								counter2Confidence++;
							}
						}
					}
					
					double ruleConfidence = (double) counter2Confidence / (double) counter2Support;
					double ruleSupport = (double) counter2Support / (double) total;
					
					if(ruleConfidence >= datasetConfidence && ruleSupport >= datasetSupport){
						RuleTuple rt = new RuleTuple(rule.getConsequent().getCategoryDomain(), condition.getContext(), rule.getConsequent());
						inferredRules.add(rt);
					}
				}
			}
			genreRulesByContextInstance.setGenreRulesByContextMap(condition, inferredRules);
		}else{
			inferredRules = genreRulesByContextInstance.getGenreRulesByContextMap().get(condition);
		}

		
		return inferredRules;
	}

	private HashMap<ItemCategory, Integer> getPreferredCategoriesByContext(
			ContextualCriteria contextualAttributes,
			HashMap<ItemDomain,HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>>> contexttualDomainCategoryPrefs, ItemDomain domain, boolean isTarget) {
		
		HashMap<ItemCategory, Integer> categoryOccurs = new HashMap<ItemCategory, Integer>();
		
		HashMap<ContextualCriteria,HashMap<ItemCategory,Integer>> contexttualCategoryPrefs;
		
		if(isTarget){
			contexttualCategoryPrefs = contexttualDomainCategoryPrefs.get(domain);
			
			if(contexttualCategoryPrefs != null){
				for(ContextualCriteria cc : contexttualCategoryPrefs.keySet()){
					if(contextualAttributes.containsAllContextualAttributesIgnoringUnknown(cc)){
						HashMap<ItemCategory, Integer> tempItemCategory = contexttualCategoryPrefs.get(cc);
						for(ItemCategory ic : tempItemCategory.keySet()){
							
							Integer numberOfOccurrences = categoryOccurs.get(ic);
							Integer numberOfOccursInThatContext = tempItemCategory.get(ic);
							
							if(numberOfOccurrences == null){
								categoryOccurs.put(ic, numberOfOccursInThatContext);
							}else{
								categoryOccurs.put(ic, numberOfOccurrences+numberOfOccursInThatContext);
							}
						}
					}
				}
			}
			
		}else{
			for(ItemDomain domainSource : contexttualDomainCategoryPrefs.keySet()){
				if(domainSource.equals(domain)){
					continue;
				}
				contexttualCategoryPrefs = contexttualDomainCategoryPrefs.get(domainSource);
				
				if(contexttualCategoryPrefs != null){
					for(ContextualCriteria cc : contexttualCategoryPrefs.keySet()){
						if(contextualAttributes.containsAllContextualAttributesIgnoringUnknown(cc)){
							HashMap<ItemCategory, Integer> tempItemCategory = contexttualCategoryPrefs.get(cc);
							for(ItemCategory ic : tempItemCategory.keySet()){
								
								Integer numberOfOccurrences = categoryOccurs.get(ic);
								Integer numberOfOccursInThatContext = tempItemCategory.get(ic);
								
								if(numberOfOccurrences == null){
									categoryOccurs.put(ic, numberOfOccursInThatContext);
								}else{
									categoryOccurs.put(ic, numberOfOccurrences+numberOfOccursInThatContext);
								}
							}
						}
					}
				}
			}
		}
		
		
		return categoryOccurs;
	}


	
}
