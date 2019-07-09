package br.cin.tbookmarks.recommender.evaluation;

public class RankingValues {
	  private long[] relevantItems;
	  private long[] recommendedItems;
	  
	  private long userID;	  
	  
	 
	  
	  public RankingValues(long[] relevantItems, long[] recommendedItems,
			long userID) {
		super();
		this.relevantItems = relevantItems;
		this.recommendedItems = recommendedItems;
		this.userID = userID;
	}

	public String toStringRelevantItems(){
		
		StringBuffer relevantItemsStr = new StringBuffer("{");
		
		for(long itemId : relevantItems){
			relevantItemsStr.append(itemId+", ");
		}
		
		relevantItemsStr.append("}");
		
		return relevantItemsStr.toString();
	}
	
	public String toStringRecommendedItems(){
		
		StringBuffer recommendedItemsStr = new StringBuffer("{");
		
		for(long itemId : recommendedItems){
			recommendedItemsStr.append(itemId+", ");
		}
		
		recommendedItemsStr.append("}");
		
		return recommendedItemsStr.toString();
	}

	public long[] getRelevantItems() {
		return relevantItems;
	}



	public void setRelevantItems(long[] relevantItems) {
		this.relevantItems = relevantItems;
	}



	public long[] getRecommendedItems() {
		return recommendedItems;
	}



	public void setRecommendedItems(long[] recommendedItems) {
		this.recommendedItems = recommendedItems;
	}



	public long getUserID() {
		return userID;
	}



	public void setUserID(long userID) {
		this.userID = userID;
	}

	
	  
	  
}
