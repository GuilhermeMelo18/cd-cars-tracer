package br.cin.tbookmarks.recommender.database.contextual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public enum DayTypeContextualAttribute implements AbstractContextualAttribute{

	UNKNOWN(-1),WEEKDAY(0),WEEKEND(1);

	private long code;

	private DayTypeContextualAttribute(long value) {
		this.code = value;
	}

	@Override
	public long getCode() {
		return this.code;
	}
	
	public static DayTypeContextualAttribute getInstanceByCode(long code){
		
		for(DayTypeContextualAttribute d : DayTypeContextualAttribute.values()){
			if(d.getCode() == code){
				return d;
			}
		}
		
		return null;
	}
	
	@Override
	public List<AbstractContextualAttribute> valuesForTest() {
		
		HashSet<Long> aux = new HashSet<Long>();
		
		List<AbstractContextualAttribute> valuesForTest = new ArrayList<AbstractContextualAttribute>();
		for(DayTypeContextualAttribute attr : DayTypeContextualAttribute.values()){
			if(!attr.equals(DayTypeContextualAttribute.UNKNOWN) && !aux.contains(attr.getCode())){
				valuesForTest.add(attr);
				aux.add(attr.getCode());
			}
		}
		return valuesForTest;
	}
}
