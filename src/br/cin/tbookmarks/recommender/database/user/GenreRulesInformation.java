package br.cin.tbookmarks.recommender.database.user;

import java.util.HashMap;
import java.util.HashSet;

public class GenreRulesInformation {
	
	private HashMap<RuleTuple,HashSet<RuleTuple>> conditionInferredMap;
	//private RuleTuple inferred;
	
	public GenreRulesInformation() {
		this.conditionInferredMap = new HashMap<RuleTuple, HashSet<RuleTuple>>();
	}
	
	public HashMap<RuleTuple, HashSet<RuleTuple>> getConditionInferredMap() {
		return conditionInferredMap;
	}
	
	public void setConditionInferredMap(
			HashMap<RuleTuple, HashSet<RuleTuple>> conditionInferredMap) {
		this.conditionInferredMap = conditionInferredMap;
	}
	
	
}
