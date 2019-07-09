package br.cin.tbookmarks.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import com.google.common.primitives.Longs;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualDataModel;
import br.cin.tbookmarks.recommender.database.contextual.ContextualUserPreferenceArray;
import br.cin.tbookmarks.recommender.database.item.ItemCategory;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;

public class UsersPreferencesUtil {
		
	public static HashMap<ItemDomain, Integer> verifyUserOverlapping(PreferenceArray prefsForUser, AbstractDataset dataset){
		
		HashMap<ItemDomain, Integer> returnUserDomain = new HashMap<ItemDomain, Integer>();
		
		for (int i = 0; i < prefsForUser.getIDs().length; i++) {
			
			ItemDomain itemDomain = dataset.getItemInformationByID(prefsForUser.getIDs()[i]).getItemDomain();
			
			returnUserDomain.put(itemDomain, returnUserDomain.size());
			
			if(returnUserDomain.containsKey(ItemDomain.MOVIE) && returnUserDomain.containsKey(ItemDomain.BOOK) && returnUserDomain.containsKey(ItemDomain.MUSIC)){
				return returnUserDomain;
			}
			
		}
		
		return returnUserDomain;
	}
	
	public static boolean verifyUserSourceDomain(PreferenceArray prefsForUser, AbstractDataset dataset) {
		
		for (int i = 0; i < prefsForUser.getIDs().length; i++) {
			
			ItemDomain itemDomain = dataset.getItemInformationByID(prefsForUser.getIDs()[i]).getItemDomain();
			
			if(dataset.getSourceDomain().getCode()!=itemDomain.getCode()) {
				return false;
			}
			
		}
		
		return true;
		
	}
	
	private ArrayList<String> convertCategories(Set<ItemCategory> setCat){
		
		ArrayList<String> convCat = new ArrayList<String>();
		
		for (ItemCategory itemCat : setCat) {
			
			String catName =  itemCat.name().replace("_BOOK", "");
			catName = catName.replace("_MOVIE", "");
			
			convCat.add(catName);
			
		}
		
		return convCat;
	}
	
}
