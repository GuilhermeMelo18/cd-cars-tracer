package br.cin.tbookmarks.server;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import br.cin.tbookmarks.database.UserProfilePersistence;



/**
 * Servlet implementation class TBookmarksServlet
 */
public class GreetingServiceImpl extends HttpServlet {
	//public static final String RESULTS_CURRENT_VIDEO = "resultsCurrentVideo";
	public static final String USER_I_DFROM_UI = "userIDfromUI";
	public static final String RETRIEVE_USER_DATA = "retrieveUserData";
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GreetingServiceImpl() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/*try {
			if(request.getAttribute(RETRIEVE_USER_DATA) != null && request.getAttribute(USER_I_DFROM_UI) != null){
				
				//HttpSession session = request.getSession();
				
				for(EnumRecommendationType recommendationType : EnumRecommendationType.values()){
					for(EnumCategoryType categoryType : EnumCategoryType.values()){
						List<WebResourceRecommended> webresources = UserProfilePersistence.retrieveWebResourcesAccordingCategoryAndRecommendationType(request.getParameter(USER_I_DFROM_UI), categoryType, recommendationType);
						
						//Collections.reverse(webresources); //TODO: tratar persistencia
						
						StringBuffer results = new StringBuffer();
						
						for (WebResourceRecommended webResourceRecommended : webresources) {
							results.append(webResourceRecommended.getUrl()).append(";");
						}
						
						if(!webresources.isEmpty()){
							response.addHeader("results"+recommendationType+categoryType, results.toString());
						}else{
							throw new Exception("results"+recommendationType+categoryType+" is EMPTY");
						}
						
					}
				}
				
			}
		} catch (Exception e) {
			
			throw new ServletException(e);
		}*/
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			
			if(request.getParameter("generateNewUserID") != null && request.getParameter("generateNewUserID").equals("true")){
				String newUserID = UserProfilePersistence.addUser();
				if(newUserID != null){
					response.addHeader("isNewUser", "true");
					response.addHeader("userID", newUserID);
				}else{
					response.addHeader("isNewUser", "false");
				}
			}else if(request.getParameter("deleteWebRecommendationHistoryFiles") != null && request.getParameter("deleteWebRecommendationHistoryFiles").equals("true")){
				
				System.out.println("UserID: "+request.getParameter("userID"));
				
				boolean filesDeleted = UserProfilePersistence.removeHistoryRecommendationUserResources(Long.valueOf(request.getParameter("userID")));
				if(filesDeleted){
					response.addHeader("filesDeleted", "true");
				}else{
					response.addHeader("filesDeleted", "false");
				}
			}else if(request.getParameter("userID") != null && request.getParameter("data") != null && 
					request.getParameter("category") != null && request.getParameter("persist_mode") != null){
			
				System.out.println("UserID: "+request.getParameter("userID"));

				long newUserID = UserProfilePersistence.saveUserResources(Long.valueOf(request.getParameter("userID")), request.getParameter("data"), request.getParameter("category"), request.getParameter("persist_mode"));
				response.addHeader("isNewUser", "false");
				response.addHeader("userID", Long.toString(newUserID));
				
			}/*else if(request.getParameter(RETRIEVE_USER_DATA) != null && request.getParameter(USER_I_DFROM_UI) != null){
				
				HttpSession session = request.getSession();
				
				for(EnumRecommendationType recommendationType : EnumRecommendationType.values()){
					for(EnumCategoryType categoryType : EnumCategoryType.values()){
						List<WebResourceRecommended> webresources = UserProfilePersistence.retrieveWebResourcesAccordingCategoryAndRecommendationType(request.getParameter(USER_I_DFROM_UI), categoryType, recommendationType);
						
						//Collections.reverse(webresources); //TODO: tratar persistencia
						
						StringBuffer results = new StringBuffer();
						
						for (WebResourceRecommended webResourceRecommended : webresources) {
							results.append(webResourceRecommended.getUrl()).append(";");
						}
						
						if(!webresources.isEmpty()){
							session.setAttribute("results"+recommendationType+categoryType, results.toString());
						}else{
							throw new Exception("results"+recommendationType+categoryType+" is EMPTY");
						}
						
					}
				}
				
				
				
				
			}*/
			/*else if(request.getParameter(RETRIEVE_USER_DATA) != null && request.getParameter(USER_I_DFROM_UI) != null){
				
				//HttpSession session = request.getSession();
				
				for(EnumRecommendationType recommendationType : EnumRecommendationType.values()){
					for(EnumCategoryType categoryType : EnumCategoryType.values()){
						List<WebResourceRecommended> webresources = UserProfilePersistence.retrieveWebResourcesAccordingCategoryAndRecommendationType(request.getParameter(USER_I_DFROM_UI), categoryType, recommendationType);
						
						//Collections.reverse(webresources); //TODO: tratar persistencia
						
						StringBuffer results = new StringBuffer();
						
						for (WebResourceRecommended webResourceRecommended : webresources) {
							results.append(webResourceRecommended.getUrl()).append(";");
						}
						
						if(!webresources.isEmpty()){
							response.addHeader("results"+recommendationType+categoryType, results.toString());
						}else{
							throw new Exception("results"+recommendationType+categoryType+" is EMPTY");
						}
						
					}
				}
				
				
				
				
			}*/
		} catch (Exception e) {
			
			throw new ServletException(e);
		}
	
	}


}
