package br.cin.tbookmarks.database;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class WebResourceRecommended implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7389377200332644477L;

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private String url;
	
	@Persistent
	private EnumRecommendationType recommendationType;
	
	@Persistent
	private EnumCategoryType categoryType;
	
	public WebResourceRecommended(String url,EnumRecommendationType reType, EnumCategoryType catType ) {
		this.url = url;
		this.recommendationType = reType;
		this.categoryType = catType;
	}

	public Key getKey() {
        return key;
    }
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public EnumRecommendationType getRecommendationType() {
		return recommendationType;
	}
	
	public void setRecommendationType(EnumRecommendationType recommendationType) {
		this.recommendationType = recommendationType;
	}
	
	public EnumCategoryType getCategoryType() {
		return categoryType;
	}
	
	public void setCategoryType(EnumCategoryType categoryType) {
		this.categoryType = categoryType;
	}

}
