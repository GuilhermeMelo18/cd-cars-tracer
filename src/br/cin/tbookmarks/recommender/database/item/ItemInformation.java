package br.cin.tbookmarks.recommender.database.item;

import java.util.Set;


public class ItemInformation {
	private long id;
	private String asin;
	
	private String name;
	private String yearReleased;
	private String link;
	private Set<ItemCategory> categories;
	private ItemDomain itemDomain;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public void setAsin(String asin) {
		this.asin = asin;
	}
	
	public String getAsin() {
		return asin;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getYearReleased() {
		return yearReleased;
	}
	public void setYearReleased(String yearReleased) {
		this.yearReleased = yearReleased;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	
	public void setCategories(Set<ItemCategory> categories) {
		this.categories = categories;
	}
	
	public Set<ItemCategory> getCategories() {
		return categories;
	}
	
	public void setItemDomain(ItemDomain itemDomain) {
		this.itemDomain = itemDomain;
	}
	
	public ItemDomain getItemDomain() {
		return itemDomain;
	}

}
