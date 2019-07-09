package br.cin.tbookmarks.database;

import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


@PersistenceCapable
public class User {
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long userId;
	
	@Persistent
	private List<WebResourceRecommended> currentVideoWebResources;
	
	@Persistent
	private List<WebResourceRecommended> currentNewsWebResources;
	
	@Persistent
	private List<WebResourceRecommended> currentImageWebResources;
	
	@Persistent
	private List<WebResourceRecommended> currentTwitterWebResources;
	
	@Persistent
	private List<WebResourceRecommended> historyVideoWebResources;
	
	@Persistent
	private List<WebResourceRecommended> historyNewsWebResources;
	
	@Persistent
	private List<WebResourceRecommended> historyImageWebResources;
	
	@Persistent
	private List<WebResourceRecommended> historyTwitterWebResources;
	
	public User() {}
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public List<WebResourceRecommended> getCurrentVideoWebResources() {
		return currentVideoWebResources;
	}

	public void setCurrentVideoWebResources(
			List<WebResourceRecommended> currentVideoWebResources) {
		this.currentVideoWebResources = currentVideoWebResources;
	}

	public List<WebResourceRecommended> getCurrentNewsWebResources() {
		return currentNewsWebResources;
	}

	public void setCurrentNewsWebResources(
			List<WebResourceRecommended> currentNewsWebResources) {
		this.currentNewsWebResources = currentNewsWebResources;
	}

	public List<WebResourceRecommended> getCurrentImageWebResources() {
		return currentImageWebResources;
	}

	public void setCurrentImageWebResources(
			List<WebResourceRecommended> currentImageWebResources) {
		this.currentImageWebResources = currentImageWebResources;
	}

	public List<WebResourceRecommended> getCurrentTwitterWebResources() {
		return currentTwitterWebResources;
	}

	public void setCurrentTwitterWebResources(
			List<WebResourceRecommended> currentTwitterWebResources) {
		this.currentTwitterWebResources = currentTwitterWebResources;
	}

	public List<WebResourceRecommended> getHistoryVideoWebResources() {
		return historyVideoWebResources;
	}

	public void setHistoryVideoWebResources(
			List<WebResourceRecommended> historyVideoWebResources) {
		this.historyVideoWebResources = historyVideoWebResources;
	}

	public List<WebResourceRecommended> getHistoryNewsWebResources() {
		return historyNewsWebResources;
	}

	public void setHistoryNewsWebResources(
			List<WebResourceRecommended> historyNewsWebResources) {
		this.historyNewsWebResources = historyNewsWebResources;
	}

	public List<WebResourceRecommended> getHistoryImageWebResources() {
		return historyImageWebResources;
	}

	public void setHistoryImageWebResources(
			List<WebResourceRecommended> historyImageWebResources) {
		this.historyImageWebResources = historyImageWebResources;
	}

	public List<WebResourceRecommended> getHistoryTwitterWebResources() {
		return historyTwitterWebResources;
	}

	public void setHistoryTwitterWebResources(
			List<WebResourceRecommended> historyTwitterWebResources) {
		this.historyTwitterWebResources = historyTwitterWebResources;
	}

		
	
}
