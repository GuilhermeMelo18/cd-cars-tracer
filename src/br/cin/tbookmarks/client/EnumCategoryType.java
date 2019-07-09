package br.cin.tbookmarks.client;

public enum EnumCategoryType {
	VIDEO("Video"),
	NEWS("News"),
	//BOOK("Book"),
	IMAGE("Image"),
	TWITTER("Twitter");
	
	EnumCategoryType(String name){
		this.name = name;
	}
	
	private String name;
	
	public String getName(){
		return this.name;
	}
}
