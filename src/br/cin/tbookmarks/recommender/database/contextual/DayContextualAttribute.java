package br.cin.tbookmarks.recommender.database.contextual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public enum DayContextualAttribute implements AbstractContextualAttribute{

	UNKNOWN(-1),SUNDAY(1),MONDAY(2),TUESDAY(3),WEDNESDAY(4),THURSDAY(5),FRIDAY(6),SATURDAY(7);

	private long code;

	private DayContextualAttribute(long value) {
		this.code = value;
	}

	@Override
	public long getCode() {
		return this.code;
	}

	public static DayContextualAttribute getInstanceByCode(long code){
		
		for(DayContextualAttribute d : DayContextualAttribute.values()){
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
		for(DayContextualAttribute attr : DayContextualAttribute.values()){
			if(!attr.equals(DayContextualAttribute.UNKNOWN) && !aux.contains(attr.getCode())){
				valuesForTest.add(attr);
				aux.add(attr.getCode());
			}
		}
		
		return valuesForTest;
	}
}
