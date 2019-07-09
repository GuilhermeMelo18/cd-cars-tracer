package br.cin.tbookmarks.database;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;


public abstract class ProfilesPersistence {
	
	private static final String TOKEN_NEXT_URL = "TOKEN_NEXT_URL";
	
	private static String[] convertDataClientToDataServer(String dataClient){
		String[] dataServer = dataClient.split(TOKEN_NEXT_URL);
				
		return dataServer;
	}
	
	public synchronized static boolean saveUserResources(long userID,String data, String category, String persistMode){

		PersistenceManager pm = UserProfilePersistence.getPersistenceManagerFactory().getPersistenceManager();
		
		try{
			
			
			User userFromDB = pm.getObjectById(User.class, new Long(userID));
			if(userFromDB != null){
				
				String[] urls = convertDataClientToDataServer(data);
				
				List<WebResourceRecommended> webResourceRecommendeds = new ArrayList<WebResourceRecommended>();
				
				for(String url : urls){
					String aux1 = persistMode, aux2 = category;
					WebResourceRecommended webResourceRecommended = new WebResourceRecommended(url,EnumRecommendationType.valueOf(aux1.toUpperCase()), EnumCategoryType.valueOf(aux2.toUpperCase()));
					webResourceRecommendeds.add(webResourceRecommended);
				}
				
				if(persistMode.equalsIgnoreCase(EnumRecommendationType.CURRENT.getName())){
					
					if(category.equalsIgnoreCase(EnumCategoryType.VIDEO.getName())){
						userFromDB.setCurrentVideoWebResources(webResourceRecommendeds);
					}else if(category.equalsIgnoreCase(EnumCategoryType.IMAGE.getName())){
						userFromDB.setCurrentImageWebResources(webResourceRecommendeds);
					}else if(category.equalsIgnoreCase(EnumCategoryType.NEWS.getName())){
						userFromDB.setCurrentNewsWebResources(webResourceRecommendeds);
					}else if(category.equalsIgnoreCase(EnumCategoryType.TWITTER.getName())){
						userFromDB.setCurrentTwitterWebResources(webResourceRecommendeds);
					}
				}else if(persistMode.equalsIgnoreCase(EnumRecommendationType.HISTORY.getName())){
					if(category.equalsIgnoreCase(EnumCategoryType.VIDEO.getName())){
						userFromDB.getHistoryVideoWebResources().addAll(webResourceRecommendeds);
					}else if(category.equalsIgnoreCase(EnumCategoryType.IMAGE.getName())){
						userFromDB.getHistoryImageWebResources().addAll(webResourceRecommendeds);
					}else if(category.equalsIgnoreCase(EnumCategoryType.NEWS.getName())){
						userFromDB.getHistoryNewsWebResources().addAll(webResourceRecommendeds);
					}else if(category.equalsIgnoreCase(EnumCategoryType.TWITTER.getName())){
						userFromDB.getHistoryTwitterWebResources().addAll(webResourceRecommendeds);
					}
				}
								
				System.out.println("Data from userID: "+userID+" saved on server!");
				return true;
			}else{
				System.out.println("userID: "+userID+" not found on DB!");
				return false;
			}
		}finally{
			pm.close();
		}

	}


	public static boolean removeHistoryRecommendationUserResources(long userID) {
		
		PersistenceManager pm = UserProfilePersistence.getPersistenceManagerFactory().getPersistenceManager();
		
		try{
			
			User userFromDB = pm.getObjectById(User.class, new Long(userID));
			if(userFromDB != null){
				
				userFromDB.getHistoryVideoWebResources().clear();
			
				userFromDB.getHistoryImageWebResources().clear();
			
				userFromDB.getHistoryNewsWebResources().clear();
			
				userFromDB.getHistoryTwitterWebResources().clear();
								
				System.out.println("Data from userID: "+userID+" saved on server!");
				return true;
			}else{
				System.out.println("userID: "+userID+" not found on DB!");
				return false;
			}
		}finally{
			pm.close();
		}

		
	}	
}
