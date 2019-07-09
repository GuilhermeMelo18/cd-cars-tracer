package br.cin.tbookmarks.recommender.evaluation;

public class CremonesiValues {
	  private long itemId;
	  private long position;
	  
	  private long userID;	  
	  
	  private long[] context;
	  
	  public CremonesiValues(long itemId, long position, long userID,long[] c) {
			super();
			this.itemId = itemId;
			this.position = position;
			this.userID = userID;
			this.context = c;
		}

	

	public long getItemId() {
		return itemId;
	}



	public long getPosition() {
		return position;
	}



	public long getUserID() {
		return userID;
	}

	  public long[] getContext() {
		return context;
	}
}
