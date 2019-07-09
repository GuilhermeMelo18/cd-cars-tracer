package br.cin.tbookmarks.recommender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.AmazonCrossDataset;
import br.cin.tbookmarks.recommender.database.contextual.ContextualPreferenceInterface;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.util.ItemResource;
import br.cin.tbookmarks.util.StructUserItem;
import br.cin.tbookmarks.util.UserItemResource;
import br.cin.tbookmarks.util.UserResource;

public class GenerateDataSet {
	
	
	
	public static void countUserItemRating(AbstractDataset dataset) throws TasteException {
		
		

		HashMap<Long, Integer> userMapDay = new HashMap<Long, Integer>();
		HashMap<Long, Integer> itensMapDay = new HashMap<Long, Integer>();
		
		int numUsersDayAttribute = 0;
		int numItensDayAttribute = 0;
		int numRatingsDayAttribute = 0;
		
		

		HashMap<Long, Integer> userMapLocation = new HashMap<Long, Integer>();
		HashMap<Long, Integer> itensMapLocation = new HashMap<Long, Integer>();
		
		int numUsersLocationAttribute = 0;
		int numItensLocationAttribute = 0;
		int numRatingsLocationAttribute = 0;
		

		HashMap<Long, Integer> userMapCompanion = new HashMap<Long, Integer>();
		HashMap<Long, Integer> itensMapCompanion = new HashMap<Long, Integer>();
		
		
		int numUsersCompanion = 0;
		int numItensCompanion = 0;
		int numRatingsCompanion = 0;
		
		DataModel dataModel = dataset.getModel();
		
		LongPrimitiveIterator userIdsIterator = dataModel.getUserIDs();
		

		while(userIdsIterator.hasNext()){
			
			
			long userId = userIdsIterator.next();

			PreferenceArray prefsForUser = dataModel.getPreferencesFromUser(userId);
			int maxItens = prefsForUser.getIDs().length;
			
			for (int j = 0; j < maxItens; j++) {
				
				ContextualPreferenceInterface cpRealPref = (ContextualPreferenceInterface) prefsForUser.get(j);
				long itemPrefId  = cpRealPref.getItemID();
				float rating = cpRealPref.getValue();
				long[] contextualPref = cpRealPref.getContextualPreferences();
				
				
				if(contextualPref[1] != -1) {
					
					numRatingsDayAttribute++;
					
					if(!userMapDay.containsKey(userId)) {
						
						numUsersDayAttribute++;
						userMapDay.put(userId, 0);
					}
						
					
					if(!itensMapDay.containsKey(itemPrefId)) {
						
						numItensDayAttribute++;
						itensMapDay.put(itemPrefId, 0);
					}
						
				}
				
				if(contextualPref[2] != -1) {
					
					numRatingsLocationAttribute++;
					
					if(!userMapLocation.containsKey(userId)) {
						
						numUsersLocationAttribute++;
						userMapLocation.put(userId, 0);
					}
						
					
					
					if(!itensMapLocation.containsKey(itemPrefId)) {
						
						numItensLocationAttribute++;
						itensMapLocation.put(itemPrefId, 0);
					}
					
				}
				
				if(contextualPref[6] != -1) {
					
					
					numRatingsCompanion++;
					
					if(!userMapCompanion.containsKey(userId)) {
						
						numUsersCompanion++;
						userMapCompanion.put(userId, 0);
					}
					
					if(!itensMapCompanion.containsKey(itemPrefId)) {
						
						numItensCompanion++;
						itensMapCompanion.put(itemPrefId, 0);
					}
					
				}
		
			}
			
		}
		
		
		System.out.println("Num Users DayAttribute : "+  numUsersDayAttribute);
		System.out.println("Num Itens DayAttribute : "+ numItensDayAttribute);
		System.out.println("Num Ratings DayAttribute : "+ numRatingsDayAttribute);
		System.out.println();
		
		System.out.println("Num Users Location : " + numUsersLocationAttribute);
		System.out.println("Num Itens Location : " + numItensLocationAttribute );
		System.out.println("Num Ratings Location : " + numRatingsLocationAttribute);
		System.out.println();
		
		System.out.println("Num Users Companion : " + numUsersCompanion);
		System.out.println("Num Itens Companion : " + numItensCompanion);
		System.out.println("Num Ratings Companion : " + numRatingsCompanion);
		
	}
	
	
	public static StructUserItem groupByBestsUserAndItens(AbstractDataset dataset, int sizeUsers, int sizeItens, int maxUsers) throws TasteException {
		
		DataModel dataModel = dataset.getModel();
		HashMap<Long, Integer> itensMap = new HashMap<Long, Integer>();
		ArrayList<UserResource> userList = new ArrayList<UserResource>();
		ArrayList<ItemResource> itemList = new ArrayList<ItemResource>();
		ArrayList<UserItemResource> userItemResource = new ArrayList<UserItemResource>();
		
		LongPrimitiveIterator userIdsIterator = dataModel.getUserIDs();
		
		while(userIdsIterator.hasNext()){
			
			long userId = userIdsIterator.next();
			
			PreferenceArray prefsForUser = dataModel.getPreferencesFromUser(userId);
			int maxItens = prefsForUser.getIDs().length;
			
			userList.add(new UserResource(userId, maxItens));
			
			
			for (int i = 0; i < maxItens; i++) {
				
				long itemId  = prefsForUser.getIDs()[i];
				int numPreferences = dataModel.getNumUsersWithPreferenceFor(itemId);
				
				if(!itensMap.containsKey(itemId)) {
					
					itensMap.put(itemId, itensMap.size());
					itemList.add(new ItemResource(itemId, numPreferences));
					
				}

			}
		}
		
		java.util.Collections.sort(userList);

		java.util.Collections.sort(itemList);
		
		ArrayList<UserResource> userListReturn = new ArrayList<UserResource>();
		HashMap<Long, Integer> usersMap = new HashMap<Long, Integer>();
		
		
		for (int i = 0; i < sizeUsers; i++) {
			
			long userId = userList.get(i).getIdUser();

			PreferenceArray prefsForUser = dataModel.getPreferencesFromUser(userId);
			int maxItens = prefsForUser.getIDs().length;
			
			
			for (int j = 0; j < maxItens; j++) {
				
				ContextualPreferenceInterface cpRealPref = (ContextualPreferenceInterface) prefsForUser.get(j);
				long itemPrefId  = cpRealPref.getItemID();
				float rating = cpRealPref.getValue();
					               
				for (int k = 0; k < sizeItens; k++) {
					
					long itemId = itemList.get(k).getIdItem();
					
					if(itemPrefId == itemId) {
						
						long[] contextualPref  = cpRealPref.getContextualPreferences();
						userItemResource.add(new UserItemResource(userId, itemId, rating, contextualPref));
						
						if(!usersMap.containsKey(userId)) {
							usersMap.put(userId, 0);
							userListReturn.add(userList.get(i));
						}
						
						break;
					}
				}
				
			}
			

			if(userListReturn.size()==maxUsers) {
				
				return new StructUserItem(userItemResource, userListReturn);
			}
		}
		
		
		return new StructUserItem(userItemResource, userListReturn);
	}
	
	
	public static ArrayList<UserResource>  getOverlap(ArrayList<UserResource> usersSource, ArrayList<UserResource> usersTarget, 
			int quantity, HashMap<Long, Integer>  convertMap) {
		
		ArrayList<UserResource> userList = new ArrayList<UserResource>();
		
		for (UserResource ut : usersTarget) {
			
			for (UserResource us : usersSource) {
				
				if(ut.getIdUser() == us.getIdUser() && !convertMap.containsKey(ut.getIdUser())) {
					
					userList.add(ut);
					if(userList.size()==quantity) {
						return userList;
					}
					break;
				}
				
			}
		}
		
		
		return userList;
	}
	
	public static void  exportPercentRatings(ArrayList<UserResource> userOverlappingList, 
			ArrayList<UserItemResource> userItemList, String overlapedFile ) throws IOException {
		
		
		File fileOutput = new File("C:\\Users\\guilh\\Documents\\UFRPE\\TCC\\TBookmarksRSProject-20190124T223454Z-001\\TBookmarksRSProject\\war\\WEB-INF\\resources\\datasets\\"+overlapedFile);
		
		FileWriter fileWriter = new FileWriter(fileOutput, true);
		
		
		for (UserResource userResource : userOverlappingList) {
			
			for (UserItemResource userItemResource : userItemList) {
				
				if(userResource.getIdUser() == userItemResource.getUserId()) {
					
					long userId = userItemResource.getUserId();
					long itemId = userItemResource.getItemId();
					float rating = userItemResource.getRating();
					
					long[] prefs = userItemResource.getContextualPreference();
					
					int index = 0;
					fileWriter.write(userId + "\t" + itemId + "\t"
							+ Float.valueOf(rating).intValue() + "\t"
							+ prefs[index++] + "|"
							+ prefs[index++] + "|"
							+ prefs[index++] + "|"
							+ prefs[index++] + "|"
							+ prefs[index++] + "|"
							+ prefs[index++] + "|"
							+ prefs[index++] + "|"
							+ prefs[index++] + "\n");
				}
				
			}
		}
		
		fileWriter.close();
		
	}
	
	public static void  exportRatings(ArrayList<UserItemResource> userItemList, String overlapedFile) throws IOException {
		
		
		File fileOutput = new File("C:\\Users\\guilh\\Documents\\UFRPE\\TCC\\TBookmarksRSProject-20190124T223454Z-001\\TBookmarksRSProject\\war\\WEB-INF\\resources\\datasets\\" + overlapedFile);
		
		FileWriter fileWriter = new FileWriter(fileOutput, true);
		
		
		for (UserItemResource userItemResource : userItemList) {
				
			long userId = userItemResource.getUserId();
			long itemId = userItemResource.getItemId();
			float rating = userItemResource.getRating();
			
			long[] prefs = userItemResource.getContextualPreference();
			
			int index = 0;
			fileWriter.write(userId + "\t" + itemId + "\t"
					+ Float.valueOf(rating).intValue() + "\t"
					+ prefs[index++] + "|"
					+ prefs[index++] + "|"
					+ prefs[index++] + "|"
					+ prefs[index++] + "|"
					+ prefs[index++] + "|"
					+ prefs[index++] + "|"
					+ prefs[index++] + "|"
					+ prefs[index++] + "\n");
		
		}
		
		fileWriter.close();
	}
	
	
	public static ArrayList<UserResource> removeByList(ArrayList<UserResource> userOverlappingList, ArrayList<UserResource> fullList, int limit) {
		
		
		ArrayList<UserResource> usersReturn = new ArrayList<UserResource>();
		
		for (int i = 0; i < userOverlappingList.size(); i++) {
			
			for (int j = 0; j < fullList.size(); j++) {
				
				if(userOverlappingList.get(i).getIdUser() == fullList.get(j).getIdUser() ) {
					
					fullList.remove(j);
					break;
				}
				
			}
			
		}
		
		Collections.sort(fullList);
		
		for (int m = 0; m < limit ; m++) {
			
			usersReturn.add(fullList.get(m));
		}
		
		return usersReturn;
	}
	
	public static ArrayList<UserResource> removeByListNotLimit(ArrayList<UserResource> removeList, ArrayList<UserResource> fullList) {
		
		
		for (int i = 0; i < removeList.size(); i++) {
			
			for (int j = 0; j < fullList.size(); j++) {
				
				if(removeList.get(i).getIdUser() == fullList.get(j).getIdUser() ) {
					
					fullList.remove(j);
					break;
				}
				
			}
			
		}
		
		Collections.sort(fullList);
		
		return fullList;
	}
	
	public static ArrayList<UserResource> getLimitUsers(ArrayList<UserResource> fullList, int limit) {
		
		
		ArrayList<UserResource> usersReturn = new ArrayList<UserResource>();

		Collections.sort(fullList);
		
		for (int j = 0; j < limit; j++) {
				
			usersReturn.add(fullList.get(j));
			
		}
	
		
		return usersReturn;
	}
	
	private static HashMap<Long, Integer> convertHashMap(ArrayList<UserResource> sourceItens){
		
		HashMap<Long, Integer>  convertMap = new HashMap<Long, Integer>();
		
		for (int i = 0; i < sourceItens.size(); i++) {
			
			convertMap.put(sourceItens.get(i).getIdUser(), i);
			
		}
		
		return convertMap;
	}
	
	
	public static void main(String[] args) {
		
		
		AbstractDataset datasetMovie = AmazonCrossDataset.getInstance(true, ItemDomain.MOVIE,"/datasets/Books_MUSIC/", "FULL-overlapping-database.dat");
		
		//AbstractDataset datasetMusic = AmazonCrossDataset.getInstance(true, ItemDomain.MUSIC, "/datasets/Books_MUSIC/" ,  "MUSIC-contextual-ratings-full-new-thesis.dat");
		
		//AbstractDataset datasetBook = AmazonCrossDataset.getInstance(true, ItemDomain.BOOK,"/datasets/Books_MOVIE/", "BOOK-contextual-ratings-full-new-thesis.dat");
		
		//boolean  runCountByContext = true;
		
		//boolean withoutOverlapping = true;
		
		
		
		try {
			
			countUserItemRating(datasetMovie);

		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
