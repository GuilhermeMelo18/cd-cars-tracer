package br.cin.tbookmarks.recommender.database.item;

import java.util.Set;

public enum ItemCategory {

	//Se mudar numeros, avaliar impacto (ex.: genre rules)
	UNKNOWN_BOOK(0,ItemDomain.BOOK), ACTION_ADVENTURE_BOOK(1,ItemDomain.BOOK), INTERNATIONAL_BOOK(2,ItemDomain.BOOK), 
	ANIMATION_BOOK(3,ItemDomain.BOOK), ANIME_BOOK(4,ItemDomain.BOOK), BOXED_SETS_BOOK(5,ItemDomain.BOOK),
	CLASSICS_BOOK(6,ItemDomain.BOOK), COMEDY_BOOK(7,ItemDomain.BOOK), DOCUMENTARY_BOOK(8,ItemDomain.BOOK), 
	DRAMA_BOOK(9,ItemDomain.BOOK), EDUCATIONAL_BOOK(10,ItemDomain.BOOK), HEALTH_BOOK(11,ItemDomain.BOOK)
	, RELIGION_BOOK(12,ItemDomain.BOOK), FANTASY_BOOK(13,ItemDomain.BOOK), LGBT_BOOK(14,ItemDomain.BOOK), 
	HOLIDAY_SEASONAL_BOOK(15,ItemDomain.BOOK), HORROR_BOOK(16,ItemDomain.BOOK), ARTISTICAL_BOOK(17,ItemDomain.BOOK),
	KIDS_FAMILY_BOOK(18,ItemDomain.BOOK)
	, WAR_BOOK(19,ItemDomain.BOOK), MUSICALS_BOOK(20,ItemDomain.BOOK), MYSTERY_BOOK(21,ItemDomain.BOOK),
	ROMANCE_BOOK(22,ItemDomain.BOOK), SCI_FI_BOOK(23,ItemDomain.BOOK), SPECIAL_BOOK(24,ItemDomain.BOOK), 
	SPORTS_BOOK(25,ItemDomain.BOOK), WESTERNS_BOOK(26,ItemDomain.BOOK),
	
	//Se mudar numeros, avaliar impacto (ex.: genre rules)
	UNKNOWN_MOVIE(100,ItemDomain.MOVIE), ACTION_ADVENTURE_MOVIE(101,ItemDomain.MOVIE), INTERNATIONAL_MOVIE(102,ItemDomain.MOVIE), 
	ANIMATION_MOVIE(103,ItemDomain.MOVIE), ANIME_MOVIE(104,ItemDomain.MOVIE), BOXED_SETS_MOVIE(105,ItemDomain.MOVIE),
	CLASSICS_MOVIE(106,ItemDomain.MOVIE), COMEDY_MOVIE(107,ItemDomain.MOVIE), DOCUMENTARY_MOVIE(108,ItemDomain.MOVIE), 
	DRAMA_MOVIE(109,ItemDomain.MOVIE), EDUCATIONAL_MOVIE(110,ItemDomain.MOVIE), HEALTH_MOVIE(111,ItemDomain.MOVIE)
	, RELIGION_MOVIE(112,ItemDomain.MOVIE), FANTASY_MOVIE(113,ItemDomain.MOVIE), LGBT_MOVIE(114,ItemDomain.MOVIE), 
	HOLIDAY_SEASONAL_MOVIE(115,ItemDomain.MOVIE), HORROR_MOVIE(116,ItemDomain.MOVIE), ARTISTICAL_MOVIE(117,ItemDomain.MOVIE),
	KIDS_FAMILY_MOVIE(118,ItemDomain.MOVIE)
	, WAR_MOVIE(119,ItemDomain.MOVIE), MUSICALS_MOVIE(120,ItemDomain.MOVIE), MYSTERY_MOVIE(121,ItemDomain.MOVIE),
	ROMANCE_MOVIE(122,ItemDomain.MOVIE), SCI_FI_MOVIE(123,ItemDomain.MOVIE), SPECIAL_MOVIE(124,ItemDomain.MOVIE), 
	SPORTS_MOVIE(125,ItemDomain.MOVIE), WESTERNS_MOVIE(126,ItemDomain.MOVIE),
	
	UNKNOWN_MUSIC(200,ItemDomain.MUSIC), JAZZ_MUSIC(101,ItemDomain.MUSIC), ROCK_MUSIC(102,ItemDomain.MUSIC), 
	CLASSIC_ROCK_MUSIC(103,ItemDomain.MUSIC), INTERNATIONAL_MUSIC(104,ItemDomain.MUSIC), CLASSICAL_MUSIC(105,ItemDomain.MUSIC),
	POP_MUSIC(106,ItemDomain.MUSIC), BLUES_MUSIC(107,ItemDomain.MUSIC), GOSPEL_MUSIC(108,ItemDomain.MUSIC), 
	DANCE_MUSIC(109,ItemDomain.MUSIC), NEW_AGE_MUSIC(110,ItemDomain.MUSIC), COUNTRY_MUSIC(111,ItemDomain.MUSIC)
	, FOLK_MUSIC(112,ItemDomain.MUSIC), VOCAL_MUSIC(113,ItemDomain.MUSIC), ALTERNATIVE_ROCK_MUSIC(114,ItemDomain.MUSIC), 
	HARD_ROCK_MUSIC(115,ItemDomain.MUSIC), KIDS_FAMILY_MUSIC(116,ItemDomain.MUSIC), RAP_MUSIC(117,ItemDomain.MUSIC),
	SPECIAL_MUSIC(118,ItemDomain.MUSIC), /*SCI_FI_MOVIE(119,ItemDomain.MOVIE), MUSICALS_MOVIE(120,ItemDomain.MOVIE), MYSTERY_MOVIE(121,ItemDomain.MOVIE),
	ROMANCE_MOVIE(122,ItemDomain.MOVIE), SCI_FI_MOVIE(123,ItemDomain.MOVIE), SPECIAL_MOVIE(124,ItemDomain.MOVIE), 
	SPORTS_MOVIE(125,ItemDomain.MOVIE), WESTERNS_MOVIE(126,ItemDomain.MOVIE),*/
	
	;

	private int code;
	private ItemDomain usualDomain;

	private ItemCategory(int value,ItemDomain domain) {
		this.code = value;
		this.usualDomain = domain;
		
	}

	public int getCode() {
		return this.code;
	}
	
	public ItemDomain getCategoryDomain() {
		return this.usualDomain;
	}

	public static ItemCategory getEnumByCode(int code){
		for(ItemCategory id : ItemCategory.values()){
			if(id.getCode() == code){
				return id;
			}
		}
		return null;
	}
	
	public boolean containsAtLeastOneCategory(Set<ItemCategory> categories){
		for(ItemCategory category : categories){
			if(this.equals(category)){
				return true;
			}
		}
		
		return false;
	}
	
	public static ItemCategory getCategoryEnum(String name, ItemDomain domain) {
		
		try{
			return valueOf(name.toUpperCase()+"_"+domain.name());
		}catch(Exception e){
		
			//if(domain.equals(ItemDomain.MOVIE)){
			if (name.toLowerCase().contains("action") || name.toLowerCase().contains("adventure")) {
				return ItemCategory.valueOf("ACTION_ADVENTURE"+"_"+domain.name());
			} else if (name.toLowerCase().contains("african") || name.toLowerCase().contains("hong kong")) {
				return ItemCategory.valueOf("INTERNATIONAL"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Anime & Manga")) {
				return ItemCategory.valueOf("ANIME"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Boxed Sets") || name.equalsIgnoreCase("TV Series")) {
				return ItemCategory.valueOf("BOXED_SETS"+"_"+domain.name());
			} else if (name.toLowerCase().equalsIgnoreCase("biograph")) {
				return ItemCategory.valueOf("DOCUMENTARY"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Business & Inveting")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Classic Comedies")) {
				return ItemCategory.valueOf("COMEDY"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Exercise & Fitness")|| name.equalsIgnoreCase("Fitness")) {
				return ItemCategory.valueOf("HEALTH"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Faith & Spirituality")) {
				return ItemCategory.valueOf("RELIGION"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Foreign Language & International") || name.equalsIgnoreCase("British")  || name.equalsIgnoreCase("United Kingdom")) {
				return ItemCategory.valueOf("INTERNATIONAL"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Gay & Lesbian")) {
				return ItemCategory.valueOf("LGBT"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Holiday & Seasonal")) {
				return ItemCategory.valueOf("HOLIDAY_SEASONAL"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Indie & Art House") || name.equalsIgnoreCase("Art House & International")) {
				return ItemCategory.valueOf("ARTISTICAL"+"_"+domain.name());
			} else if (name.toLowerCase().contains("intructional")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			} else if (name.toLowerCase().contains("family") || name.toLowerCase().contains("kids") || name.toLowerCase().contains("parenting") || name.toLowerCase().contains("child")) {
				return ItemCategory.valueOf("KIDS_FAMILY"+"_"+domain.name());
			} else if (name.toLowerCase().contains("romance")) {
				return ItemCategory.valueOf("ROMANCE"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Romantic Comedies")) {
				return ItemCategory.valueOf("COMEDY"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Military & War")) {
				return ItemCategory.valueOf("WAR"+"_"+domain.name());
			} else if (name.toLowerCase().contains("music") && !domain.equals(ItemDomain.MUSIC)) {
				return ItemCategory.valueOf("MUSICALS"+"_"+domain.name());
			} else if (name.toLowerCase().contains("horror")) {
				return ItemCategory.valueOf("HORROR"+"_"+domain.name());
			} else if (name.toLowerCase().contains("mystery") || name.toLowerCase().contains("suspense") || name.toLowerCase().contains("thriller")) {
				return ItemCategory.valueOf("MYSTERY"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Science Fiction")) {
				return ItemCategory.valueOf("MYSTERY"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Special Interests")) {
				return ItemCategory.valueOf("SPECIAL"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Martial Arts") || name.equalsIgnoreCase("Wrestling") || name.equalsIgnoreCase("World Wrestling Entertainment (WWE)")) {
				return ItemCategory.valueOf("SPORTS"+"_"+domain.name());
			}else
		
			if (name.equalsIgnoreCase("Arts & Photography")) {
				return ItemCategory.valueOf("ARTISTICAL"+"_"+domain.name());
			} else if ((name.toLowerCase().contains("biograph") || name.equalsIgnoreCase("Nonfiction") || name.equalsIgnoreCase("Cult Movies")) 
						&& !domain.equals(ItemDomain.MUSIC)){
				return ItemCategory.valueOf("DOCUMENTARY"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Business & Investing")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Children's Books")) {
				return ItemCategory.valueOf("KIDS_FAMILY"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Christian Books & Bibles")) {
				return ItemCategory.valueOf("RELIGION"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Comics & Graphic Novels")) {
				return ItemCategory.valueOf("ANIME"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Cookbooks, Food & Wine") || name.equalsIgnoreCase("Cooking, Food & Wine")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			} else if (name.equalsIgnoreCase("Crafts, Hobbies & Home")) {
				return ItemCategory.valueOf("DOCUMENTARY"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Computers & Technology") || name.equalsIgnoreCase("Computers & Internet")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Education & Reference") || name.equalsIgnoreCase("Engineering")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Gay & Lesbian")) {
				return ItemCategory.valueOf("LGBT"+"_"+domain.name());
			}else if ((name.toLowerCase().contains("health") || name.toLowerCase().contains("fitness")) 
					&& !domain.equals(ItemDomain.MUSIC)){
				return ItemCategory.valueOf("HEALTH"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("History")) {
				return ItemCategory.valueOf("DOCUMENTARY"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Home & Garden")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Humor & Entertainment")) {
				return ItemCategory.valueOf("COMEDY"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Law")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Libros en Español")) {
				return ItemCategory.valueOf("INTERNATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Literature & Fiction") && !domain.equals(ItemDomain.MUSIC)) {
				return ItemCategory.valueOf("FANTASY"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Money & Markets")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Medicine")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Parenting & Relationships") || name.equalsIgnoreCase("Parenting & Families")) {
				return ItemCategory.valueOf("KIDS_FAMILY"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Outdoors & Nature") ) {
				return ItemCategory.valueOf("DOCUMENTARY"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Politics & Social Sciences")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Professional & Technical")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Religion & Spirituality")) {
				return ItemCategory.valueOf("RELIGION"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Reference")) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if ((name.equalsIgnoreCase("Science & Math") || name.equalsIgnoreCase("Science")) 
					&& !domain.equals(ItemDomain.MUSIC)) {
				return ItemCategory.valueOf("EDUCATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Science Fiction & Fantasy") && !domain.equals(ItemDomain.MUSIC) ){
				return ItemCategory.valueOf("SCI_FI"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Sports & Outdoors")) {
				return ItemCategory.valueOf("SPORTS"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Teens")) {
				return ItemCategory.valueOf("KIDS_FAMILY"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Travel")) {
				return ItemCategory.valueOf("INTERNATIONAL"+"_"+domain.name());
			}
			
			else if (name.equalsIgnoreCase("Latin Music")) {
				return ItemCategory.valueOf("INTERNATIONAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Classic Rock")) {
				return ItemCategory.valueOf("CLASSIC_ROCK"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Opera & Vocal") || name.equalsIgnoreCase("Broadway & Vocalists")) {
				return ItemCategory.valueOf("VOCAL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Christian & Gospel")) {
				return ItemCategory.valueOf("GOSPEL"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Dance & DJ")) {
				return ItemCategory.valueOf("DANCE"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("New Age")) {
				return ItemCategory.valueOf("NEW_AGE"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Alternative Rock")) {
				return ItemCategory.valueOf("ALTERNATIVE_ROCK"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Hard Rock & Metal")) {
				return ItemCategory.valueOf("HARD_ROCK"+"_"+domain.name());
			}else if (name.equalsIgnoreCase("Rap & Hip-Hop")) {
				return ItemCategory.valueOf("RAP"+"_"+domain.name());
			}
			
			
			else{
				return ItemCategory.valueOf("UNKNOWN"+"_"+domain.name());
			}
			
			
		}
	}
	
	/*public static ItemCategory getBookCategoryEnum(String name) {
		
		try{
			return valueOf(name.toUpperCase());
		}catch(Exception e){
			if (name.equalsIgnoreCase("Arts & Photography")) {
				return ARTISTICAL;
			} else if (name.toLowerCase().contains("biograph") || name.equalsIgnoreCase("Nonfiction")) {
				return DOCUMENTARY;
			} else if (name.equalsIgnoreCase("Business & Investing")) {
				return EDUCATIONAL;
			} else if (name.equalsIgnoreCase("Children's Books")) {
				return KIDS_FAMILY;
			}else if (name.equalsIgnoreCase("Christian Books & Bibles")) {
				return RELIGION;
			}else if (name.equalsIgnoreCase("Comics & Graphic Novels")) {
				return ANIME;
			} else if (name.equalsIgnoreCase("Cookbooks, Food & Wine") || name.equalsIgnoreCase("Cooking, Food & Wine")) {
				return EDUCATIONAL;
			} else if (name.equalsIgnoreCase("Crafts, Hobbies & Home")) {
				return DOCUMENTARY;
			}else if (name.equalsIgnoreCase("Computers & Technology") || name.equalsIgnoreCase("Computers & Internet")) {
				return EDUCATIONAL;
			}else if (name.equalsIgnoreCase("Education & Reference") || name.equalsIgnoreCase("Engineering")) {
				return EDUCATIONAL;
			}else if (name.equalsIgnoreCase("Gay & Lesbian")) {
				return LGBT;
			}else if (name.equalsIgnoreCase("Health, Fitness & Dieting") || name.equalsIgnoreCase("Health, Mind & Body")) {
				return HEALTH;
			}else if (name.equalsIgnoreCase("History")) {
				return DOCUMENTARY;
			}else if (name.equalsIgnoreCase("Home & Garden")) {
				return EDUCATIONAL;
			}else if (name.equalsIgnoreCase("Humor & Entertainment")) {
				return COMEDY;
			}else if (name.equalsIgnoreCase("Law")) {
				return EDUCATIONAL;
			}else if (name.equalsIgnoreCase("Libros en Español")) {
				return INTERNATIONAL;
			}else if (name.equalsIgnoreCase("Literature & Fiction")) {
				return FANTASY;
			}else if (name.equalsIgnoreCase("Money & Markets")) {
				return EDUCATIONAL;
			}else if (name.equalsIgnoreCase("Medicine")) {
				return EDUCATIONAL;
			}else if (name.equalsIgnoreCase("Mystery, Thriller & Suspense") || name.equalsIgnoreCase("Mystery & Thrillers")) {
				return MYSTERY;
			}else if (name.equalsIgnoreCase("Parenting & Relationships") || name.equalsIgnoreCase("Parenting & Families")) {
				return KIDS_FAMILY;
			}else if (name.equalsIgnoreCase("Outdoors & Nature") ) {
				return DOCUMENTARY;
			}else if (name.equalsIgnoreCase("Politics & Social Sciences")) {
				return EDUCATIONAL;
			}else if (name.equalsIgnoreCase("Professional & Technical")) {
				return EDUCATIONAL;
			}else if (name.equalsIgnoreCase("Religion & Spirituality")) {
				return RELIGION;
			}else if (name.equalsIgnoreCase("Reference")) {
				return EDUCATIONAL;
			}else if (name.equalsIgnoreCase("Science & Math") || name.equalsIgnoreCase("Science")) {
				return EDUCATIONAL;
			}else if (name.equalsIgnoreCase("Science Fiction & Fantasy")) {
				return SCI_FI;
			}else if (name.equalsIgnoreCase("Sports & Outdoors")) {
				return SPORTS;
			}else if (name.equalsIgnoreCase("Teens")) {
				return KIDS_FAMILY;
			}else if (name.equalsIgnoreCase("Travel")) {
				return INTERNATIONAL;
			}else{
				System.out.println(name + " Book category unknown");
				return UNKNOWN;
			}
		}
		
		
	}*/
}
