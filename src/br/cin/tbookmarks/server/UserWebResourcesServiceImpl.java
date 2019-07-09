package br.cin.tbookmarks.server;


import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import br.cin.tbookmarks.client.UserWebResourcesService;
import br.cin.tbookmarks.database.EnumCategoryType;
import br.cin.tbookmarks.database.EnumRecommendationType;
import br.cin.tbookmarks.database.UserProfilePersistence;
import br.cin.tbookmarks.database.WebResourceRecommended;


/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class UserWebResourcesServiceImpl extends RemoteServiceServlet implements
		UserWebResourcesService {

	@Override
	public HashMap<String,String> getUserWebResources(String userId) throws Exception {

		HashMap<String,String> response = new HashMap<String, String>();
		
		for(EnumRecommendationType recommendationType : EnumRecommendationType.values()){
			for(EnumCategoryType categoryType : EnumCategoryType.values()){
				
				try{
					List<WebResourceRecommended> webresources = UserProfilePersistence.retrieveWebResourcesAccordingCategoryAndRecommendationType(userId, categoryType, recommendationType);
					
					//Collections.reverse(webresources); //TODO: tratar persistencia
					
					StringBuffer results = new StringBuffer();
					
					for (WebResourceRecommended webResourceRecommended : webresources) {
						results.append(webResourceRecommended.getUrl()).append(";");
					}
					
					if(!webresources.isEmpty()){
						response.put("results"+recommendationType+categoryType, results.toString());
					}else{
						throw new Exception("results"+recommendationType+categoryType+" is EMPTY");
					}
				}catch(Exception e){
					throw e;
				}
				
				
			}
		}

		return response;
	}

}
