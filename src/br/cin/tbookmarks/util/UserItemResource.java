package br.cin.tbookmarks.util;

import br.cin.tbookmarks.recommender.database.contextual.ContextualPreferenceInterface;

public class UserItemResource {
	
	private long userId;
	private long itemId;
	private float rating;
	private long [] contextualPreference;
	
	
	public UserItemResource(long userId, long itemId, float rating, long[] contextualPreference) {
		this.userId = userId;
		this.itemId = itemId;
		this.contextualPreference = contextualPreference;
		this.rating = rating;
	}


	public long getUserId() {
		return userId;
	}


	public void setUserId(long userId) {
		this.userId = userId;
	}


	public long getItemId() {
		return itemId;
	}


	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public float getRating() {
		return rating;
	}


	public void setRating(float rating) {
		this.rating = rating;
	}


	public long[] getContextualPreference() {
		return contextualPreference;
	}


	public void setContextualPreference(long[] contextualPreference) {
		this.contextualPreference = contextualPreference;
	}
	

}
