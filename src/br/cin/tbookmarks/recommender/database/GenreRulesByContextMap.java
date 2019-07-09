package br.cin.tbookmarks.recommender.database;

import java.util.HashMap;
import java.util.HashSet;

import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.item.ItemCategory;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.database.user.RuleTuple;

public class GenreRulesByContextMap {
	private static GenreRulesByContextMap INSTANCE;
	
	private static HashMap<RuleTuple,HashSet<RuleTuple>> genreRulesByContextMap;
	
	private GenreRulesByContextMap() {
		genreRulesByContextMap = new HashMap<RuleTuple,HashSet<RuleTuple>>();
	}
	
	public static synchronized GenreRulesByContextMap getInstance() {
		if (INSTANCE == null) {
			return new GenreRulesByContextMap();
		}
		return INSTANCE;
	}

	
	public HashMap<RuleTuple,HashSet<RuleTuple>> getGenreRulesByContextMap() {
		return genreRulesByContextMap;
	}
	
	public void setGenreRulesByContextMap(RuleTuple condition, HashSet<RuleTuple> inferred){
		genreRulesByContextMap.put(condition, inferred);
	}

}
