package br.cin.tbookmarks.recommender.database.item;

public enum ItemDomain {

	//Se mudar numeros, avaliar impacto (ex.: genre rules)
	MOVIE(0),EVENT(1), BOOK(2), MUSIC(3),TOY(4), VIDEO_GAME(5), SOFTWARE(6), BABY_PRODUCT(7), CE(8), SPORTS(9);

	private int code;

	private ItemDomain(int value) {
		this.code = value;
	}

	public int getCode() {
		return this.code;
	}

	public static ItemDomain getEnumByCode(int code){
		for(ItemDomain id : ItemDomain.values()){
			if(id.getCode() == code){
				return id;
			}
		}
		return null;
	}
	
}
