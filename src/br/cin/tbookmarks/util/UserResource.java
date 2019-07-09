package br.cin.tbookmarks.util;

public class UserResource implements Comparable<UserResource>{
	
	private long idUser;
	private int numRating;
	
	
	
	public UserResource(long idUser, int numRating) {
	
		this.idUser = idUser;
		this.numRating = numRating;
	}



	public long getIdUser() {
		return idUser;
	}



	public void setIdUser(long idUser) {
		this.idUser = idUser;
	}



	public int getNumRating() {
		return numRating;
	}



	public void setNumRating(int numRating) {
		this.numRating = numRating;
	}


	
	@Override
	public int compareTo(UserResource o) {
		if (this.numRating < o.numRating) {
            return 1;
        }
        if (this.numRating > o.numRating) {
            return -1;
        }
        return 0;
	}

}
