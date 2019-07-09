package br.cin.tbookmarks.recommender.database.user;

public class UserInformation {
	private long id;
	private String amazonID;
	
	private String address;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAmazonID() {
		return amazonID;
	}

	public void setAmazonID(String amazonID) {
		this.amazonID = amazonID;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	
}
