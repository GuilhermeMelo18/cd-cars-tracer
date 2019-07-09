package br.cin.tbookmarks.recommender.database;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;

import br.cin.tbookmarks.recommender.database.contextual.AprioriRuleItemCategory;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.contextual.ContextualFileDataModel;
import br.cin.tbookmarks.recommender.database.contextual.ContextualUserPreferenceArray;
import br.cin.tbookmarks.recommender.database.item.ItemDatasetInformation;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.database.item.ItemInformation;
import br.cin.tbookmarks.recommender.database.item.ItemInformationComparatorASIN;
import br.cin.tbookmarks.recommender.database.item.ItemInformationComparatorID;
import br.cin.tbookmarks.recommender.database.user.AddressDatasetInformation;
import br.cin.tbookmarks.recommender.database.user.AddressInformation;
import br.cin.tbookmarks.recommender.database.user.UserDatasetInformation;
import br.cin.tbookmarks.recommender.database.user.UserInformation;
import br.cin.tbookmarks.recommender.database.user.UserInformationComparatorAmazon;
import br.cin.tbookmarks.recommender.database.user.UserInformationComparatorID;

public abstract class AbstractDataset {
	protected DataModel model;
	protected ItemDatasetInformation itemDatasetInformation;
	protected UserDatasetInformation userDatasetInformation;
	protected AddressDatasetInformation addressDatasetInformation;
	//protected GenreRulesInformation genreRulesDatasetInformation;
	protected HashSet<AprioriRuleItemCategory> genreRulesDatasetInformation;
	protected static String datasetURL;
	protected static String datasetURLOverlap;
	
	protected static boolean itensSortedByASIN = false;
	protected static boolean itensSortedByID = false;
	
	protected static boolean usersSortedByAmazonID = false;
	protected static boolean usersSortedByID = false;
	
	protected static ItemDomain sourceDomain;
	protected static ItemDomain targetDomain;
	

	protected static final double confidenceLevelDMRules = 0.7;
	protected static final double supportLevelDMRules = 0.01;

	public DataModel getModel() {
		return model;
	}
	
	public AddressInformation getAddressInformationByText(String addressText){
		for(AddressInformation ai : getAddressDatasetInformation().getAddresses()){
			if(ai.getUserAddress().equals(addressText)){
				return ai;
			}
		}
		return null;
	}
	
	public static ItemDomain getSourceDomain() {
		return sourceDomain;
	}
	
	public static ItemDomain getTargetDomain() {
		return targetDomain;
	}
	
	public double getConfidenceleveldmrules() {
		return confidenceLevelDMRules;
	}
	
	public double getSupportleveldmrules() {
		return supportLevelDMRules;
	}
	
	public boolean containsRatingsInContext(ContextualCriteria cc) throws TasteException{
		LongPrimitiveIterator usersIterator = this.getModel().getUserIDs();
		while(usersIterator.hasNext()){
			Long userId = usersIterator.next();
			ContextualUserPreferenceArray cpa = (ContextualUserPreferenceArray)this.getModel().getPreferencesFromUser(userId);
			for (int i=0; i<cpa.length();i++) {
				ContextualCriteria ratingCC = new ContextualCriteria(cpa.get(i).getContextualPreferences());
				if(cc.containsAllContextualAttributesIgnoringUnknown(ratingCC)){
					return true;
				}
			}
			
		}
		return false;
		
	}
	
	public UserInformation getUserInformationByUserID(long userID) {
		/*
		 * for (ItemInformation item : getItemDatasetInformation().getItens()) {
		 * if (item.getId() == id) { return item; } } return null;
		 */
		
		UserInformation p = new UserInformation();
		p.setId(userID); // Essa pessoa ser� usada como crit�rio de compara��o para
						// a busca bin�ria
		
		if(!usersSortedByID){
			Collections.sort(getUserDatasetInformation()
					.getUsers(), new UserInformationComparatorID());
			
			usersSortedByID = true;
			usersSortedByAmazonID = false;
		}		
		
		
		int ResultIndex = Collections.binarySearch(getUserDatasetInformation()
				.getUsers(), p, new UserInformationComparatorID()); // Busca
																	// Bin�ria
																	// com o
																	// objeto
																	// comparador
		if (ResultIndex > -1) {
			return getUserDatasetInformation().getUsers().get(ResultIndex);
		} else {
			return null;
		}
	}
	
	public UserInformation getUserInformationByUserAmazonID(String amazonID) {
		/*
		 * for (ItemInformation item : getItemDatasetInformation().getItens()) {
		 * if (item.getId() == id) { return item; } } return null;
		 */
		
		UserInformation p = new UserInformation();
		p.setAmazonID(amazonID); // Essa pessoa ser� usada como crit�rio de compara��o para
						// a busca bin�ria
		
		if(!usersSortedByAmazonID){
			Collections.sort(getUserDatasetInformation()
					.getUsers(), new UserInformationComparatorAmazon());
			
			usersSortedByAmazonID = true;
			usersSortedByID = false;
		}		
		
		
		int ResultIndex = Collections.binarySearch(getUserDatasetInformation()
				.getUsers(), p, new UserInformationComparatorAmazon()); // Busca
																	// Bin�ria
																	// com o
																	// objeto
																	// comparador
		if (ResultIndex > -1) {
			return getUserDatasetInformation().getUsers().get(ResultIndex);
		} else {
			return null;
		}
	}

	public ItemInformation getItemInformationByAsin(String asin) {
		/*
		 * for (ItemInformation item : getItemDatasetInformation().getItens()) {
		 * if (item.getId() == id) { return item; } } return null;
		 */
		
		ItemInformation p = new ItemInformation();
		p.setAsin(asin);; // Essa pessoa ser� usada como crit�rio de compara��o para
						// a busca bin�ria
		
		if(!itensSortedByASIN){
			Collections.sort(getItemDatasetInformation()
					.getItens(), new ItemInformationComparatorASIN());
			
			itensSortedByASIN = true;
			itensSortedByID = false;
		}		
		
		
		int ResultIndex = Collections.binarySearch(getItemDatasetInformation()
				.getItens(), p, new ItemInformationComparatorASIN()); // Busca
																	// Bin�ria
																	// com o
																	// objeto
																	// comparador
		if (ResultIndex > -1) {
			return getItemDatasetInformation().getItens().get(ResultIndex);
		} else {
			return null;
		}
	}
	
	public ItemInformation getItemInformationByID(long id) {
		/*
		 * for (ItemInformation item : getItemDatasetInformation().getItens()) {
		 * if (item.getId() == id) { return item; } } return null;
		 */
		
		ItemInformation p = new ItemInformation();
		p.setId(id); // Essa pessoa ser� usada como crit�rio de compara��o para
						// a busca bin�ria
		
		if(!itensSortedByID){
			Collections.sort(getItemDatasetInformation()
					.getItens(), new ItemInformationComparatorID());
			
			itensSortedByID = true;
			itensSortedByASIN = false;
		}
		
		
		int ResultIndex = Collections.binarySearch(getItemDatasetInformation()
				.getItens(), p, new ItemInformationComparatorID()); // Busca
																	// Bin�ria
																	// com o
																	// objeto
																	// comparador
		if (ResultIndex > -1) {
			return getItemDatasetInformation().getItens().get(ResultIndex);
		} else {
			return null;
		}
	}
	
	/*public HashSet<RuleTuple> getGenreRuleTuplesInferredByRuleTuple(RuleTuple rtCondition) {
		HashSet<RuleTuple> rtInferred = genreRulesDatasetInformation.getConditionInferredMap().get(rtCondition);
		if(rtInferred == null){
			for(RuleTuple rt : genreRulesDatasetInformation.getConditionInferredMap().keySet()){
				if(rtCondition.getItemDomain().equals(rt.getItemDomain())
						&& rtCondition.getItemCategory().equals(rt.getItemCategory())
						&& rtCondition.getContext().containsAllContextualAttributesIgnoringUnknown(rt.getContext())){
					rtInferred = genreRulesDatasetInformation.getConditionInferredMap().get(rt);
					return rtInferred;
				}
			}
		}
		return rtInferred;
	}*/

	public ItemDatasetInformation getItemDatasetInformation() {
		return itemDatasetInformation;
	}
	
	public UserDatasetInformation getUserDatasetInformation() {
		return userDatasetInformation;
	}
	
	public AddressDatasetInformation getAddressDatasetInformation() {
		return addressDatasetInformation;
	}
	
	public HashSet<AprioriRuleItemCategory> getGenreRulesDatasetInformation() {
		return genreRulesDatasetInformation;
	}

	protected void initializeDataModel(String src) throws IOException {
		model = new ContextualFileDataModel(new File(
				System.getProperty("user.dir") + src));
	}
}
