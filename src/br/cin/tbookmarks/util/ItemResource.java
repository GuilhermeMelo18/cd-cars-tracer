package br.cin.tbookmarks.util;

public class ItemResource implements Comparable<ItemResource>{
	
	private long idItem;
	private int numRating;
	
	
	
	public ItemResource(long idItem, int numRating) {

		this.idItem = idItem;
		this.numRating = numRating;
	}


	public long getIdItem() {
		return idItem;
	}

	public void setIdItem(long idItem) {
		this.idItem = idItem;
	}


	public float getNumRating() {
		return numRating;
	}



	public void setNumRating(int numRating) {
		this.numRating = numRating;
	}



	@Override
	public int compareTo(ItemResource o) {
		if (this.numRating < o.numRating) {
            return 1;
        }
        if (this.numRating > o.numRating) {
            return -1;
        }
        return 0;
	}
	
	
	
	
	

}
