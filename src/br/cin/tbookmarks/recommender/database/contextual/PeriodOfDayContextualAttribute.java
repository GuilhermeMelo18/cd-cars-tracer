package br.cin.tbookmarks.recommender.database.contextual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public enum PeriodOfDayContextualAttribute implements AbstractContextualAttribute{

	UNKNOWN(-1),DAWN(0),MORNING(1),AFTERNOON(2),NIGHT(3);

	private long code;

	private PeriodOfDayContextualAttribute(long value) {
		this.code = value;
	}

	@Override
	public long getCode() {
		return this.code;
	}

	public static PeriodOfDayContextualAttribute getInstanceByCode(long code){
		
		for(PeriodOfDayContextualAttribute d : PeriodOfDayContextualAttribute.values()){
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
		for(PeriodOfDayContextualAttribute attr : PeriodOfDayContextualAttribute.values()){
			if(!attr.equals(PeriodOfDayContextualAttribute.UNKNOWN) && !aux.contains(attr.getCode())){
				valuesForTest.add(attr);
				aux.add(attr.getCode());
			}
		}
		return valuesForTest;
	}
}
