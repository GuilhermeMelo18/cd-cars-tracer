package br.cin.tbookmarks.client;

public enum EnumRecommendationType {
	CURRENT("Current") ,
	HISTORY ("History");
	
	EnumRecommendationType(String name){
		this.name = name;
	}
	
	private String name;
	
	public String getName(){
		return this.name;
	}
	
}
