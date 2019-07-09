package br.cin.tbookmarks.recommender.database.contextual;

import java.util.ArrayList;

public final class ContextualFileAttributeSequence {
	private ArrayList<Class<? extends AbstractContextualAttribute>> contextualFileAttributeSequence;
	
	private static final ContextualFileAttributeSequence INSTANCE = new ContextualFileAttributeSequence();
	
	public ContextualFileAttributeSequence() {
		contextualFileAttributeSequence = new ArrayList<Class<? extends AbstractContextualAttribute>>();
		contextualFileAttributeSequence.add(DayTypeContextualAttribute.class);
		contextualFileAttributeSequence.add(PeriodOfDayContextualAttribute.class);
	}
	
	public static ContextualFileAttributeSequence getInstance() {
		return INSTANCE;
	}
	
	public Class<? extends AbstractContextualAttribute> get(int i){
		return contextualFileAttributeSequence.get(i);
	}
	
	public int get(Class<? extends AbstractContextualAttribute> classContextual){
		for(int i = 0; i< this.contextualFileAttributeSequence.size();i++){
			if(classContextual.equals(this.contextualFileAttributeSequence.get(i))){
				return i;
			}
		}
		
		return -1;
	}
	
}
