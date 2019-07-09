package br.cin.tbookmarks.recommender.evaluation;

public class PredictionValues {
	  private float realPref;
	  private float estimatedPref;
	  
	  private long userID;
	  private long itemID;
	  private long[] context;
	  
	  
	  public PredictionValues(float rp,float ep,long userid, long itemid,long[] c) {
		this.realPref = rp;
		this.estimatedPref = ep;
		this.userID = userid;
		this.itemID = itemid;
		this.context = c;
	  }
	  
	  public float getEstimatedPref() {
		return estimatedPref;
	  }
	  
	  public float getRealPref() {
		return realPref;
	}

	public long getUserID() {
		return userID;
	}

	public long getItemID() {
		return itemID;
	}
	
	public long[] getContext() {
		return context;
	}
	  
	  
}
