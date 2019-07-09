package br.cin.tbookmarks.database;


import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;



public abstract class UserProfilePersistence {
	
	private static final PersistenceManagerFactory pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	public static PersistenceManagerFactory getPersistenceManagerFactory() {
		return pmfInstance;
	}

	public static String addUser(){
				
		User userPersisted = null;
		
		PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

        User user = new User();

        try {
            userPersisted = pm.makePersistent(user);
        } finally {
            pm.close();
        }
				
        if(userPersisted != null){
        	return userPersisted.getUserId().toString();
        }else{
        	return null;
        }
		
		
	}
	
	public static long saveUserResources(long userID, String data,
			String nameFile, String persistMode) {

		boolean persistedOnServer = ProfilesPersistence.saveUserResources(userID, data, nameFile,
				persistMode);
		
		if(persistedOnServer){
			return userID;
		}else{
			return -1;
		}
		

	}
	
	public static List<WebResourceRecommended> retrieveWebResourcesAccordingCategoryAndRecommendationType(String userID, EnumCategoryType categoryType,EnumRecommendationType recommendationType ) throws Exception{
		PersistenceManager pmtest = UserProfilePersistence.getPersistenceManagerFactory().getPersistenceManager();
		
		try{
			
			
			User userFromDB = pmtest.getObjectById(User.class, new Long(userID));
			List<WebResourceRecommended> webResourceRecommendeds = new ArrayList<WebResourceRecommended>();
			
			if(recommendationType.equals(EnumRecommendationType.CURRENT)){
				if(categoryType.equals(EnumCategoryType.VIDEO)){
					webResourceRecommendeds = userFromDB.getCurrentVideoWebResources();
				}else if(categoryType.equals(EnumCategoryType.IMAGE)){
					webResourceRecommendeds = userFromDB.getCurrentImageWebResources();
				}else if(categoryType.equals(EnumCategoryType.NEWS)){
					webResourceRecommendeds = userFromDB.getCurrentNewsWebResources();
				}else if(categoryType.equals(EnumCategoryType.TWITTER)){
					webResourceRecommendeds = userFromDB.getCurrentTwitterWebResources();
				}
			}else if(recommendationType.equals(EnumRecommendationType.HISTORY)){
				if(categoryType.equals(EnumCategoryType.VIDEO)){
					webResourceRecommendeds = userFromDB.getHistoryVideoWebResources();
				}else if(categoryType.equals(EnumCategoryType.IMAGE)){
					webResourceRecommendeds = userFromDB.getHistoryImageWebResources();
				}else if(categoryType.equals(EnumCategoryType.NEWS)){
					webResourceRecommendeds = userFromDB.getHistoryNewsWebResources();
				}else if(categoryType.equals(EnumCategoryType.TWITTER)){
					webResourceRecommendeds = userFromDB.getHistoryTwitterWebResources();
				}
			}
			
			
			return webResourceRecommendeds;
		}catch(JDOObjectNotFoundException e ){
			throw new Exception ("User "+userID+" not found!");
			//return new ArrayList<WebResourceRecommended>();
		}
		finally{
			pmtest.close();
		}
	}

	/*public static List<WebResourceRecommended> retrieveWebResourcesAccordingCategoryAndRecommendationType(String userID, EnumCategoryType categoryType,EnumRecommendationType recommendationType ) {
		PersistenceManager pm = UserProfilePersistence.getPersistenceManagerFactory().getPersistenceManager();
		
		String columnId = StringUtils.lowerCase(recommendationType.getName())+categoryType.getName()+"WebResources";
		
		Query queryUser = pm.newQuery("select "+columnId+" from "+User.class.getName()+" where userId == "+userID);

		
		try{
			
			List<WebResourceRecommended> webResourceRecommendeds = new ArrayList<WebResourceRecommended>();
			//List<WebResourceRecommended> webResourceRecommendedsReturned = new ArrayList<WebResourceRecommended>();
			
			List teste = (List) pm.newQuery(queryUser).execute();
			if(!teste.isEmpty()){
				webResourceRecommendeds = (List<WebResourceRecommended>)teste.get(0);
			}
			
			for(WebResourceRecommended webResourceRecommended : webResourceRecommendeds){//TODO Retirar if, fazer via BD
				//System.out.println(webResourceRecommended.getUrl()+" "+ webResourceRecommended.getCategoryType()+" " + webResourceRecommended.getRecommendationType());
				if(webResourceRecommended.getCategoryType().equals(categoryType) &&
						webResourceRecommended.getRecommendationType().equals(recommendationType)){
					//System.out.println(webResourceRecommended.getUrl());
					webResourceRecommendedsReturned.add(webResourceRecommended);
				}
			}
			
			return webResourceRecommendeds;
			
		}finally{
			pm.close();
			queryUser.closeAll();
		}
		
		
	}*/

	public static boolean removeHistoryRecommendationUserResources(long userID) {

		return ProfilesPersistence
				.removeHistoryRecommendationUserResources(userID);

	}

}
