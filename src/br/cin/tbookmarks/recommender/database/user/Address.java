package br.cin.tbookmarks.recommender.database.user;

import br.cin.tbookmarks.recommender.database.contextual.LocationCountryContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.LocationStateContextualAttribute;

@Deprecated
public class Address {
	
	private String fullAddress;
	
	private LocationCountryContextualAttribute country;
	private LocationStateContextualAttribute state;
	
	public Address(String fullAddress,
			LocationCountryContextualAttribute country,
			LocationStateContextualAttribute state) {
		super();
		this.fullAddress = fullAddress;
		this.country = country;
		this.state = state;
	}

	public LocationCountryContextualAttribute getCountry() {
		return country;
	}
	
	public void setCountry(LocationCountryContextualAttribute country) {
		this.country = country;
	}
	
	public LocationStateContextualAttribute getState() {
		return state;
	}
	
	public void setState(LocationStateContextualAttribute state) {
		this.state = state;
	}
	
	public String getFullAddress() {
		return fullAddress;
	}
	
	public void setFullAddress(String fullAddress) {
		this.fullAddress = fullAddress;
	}
}
