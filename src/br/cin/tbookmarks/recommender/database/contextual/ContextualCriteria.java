package br.cin.tbookmarks.recommender.database.contextual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ContextualCriteria {
	private DayTypeContextualAttribute dayTypeContextualAttribute;
	//private PeriodOfDayContextualAttribute periodOfDayContextualAttribute;
	private DayContextualAttribute dayContextualAttribute;
	private LocationCountryContextualAttribute locationCountryContextualAttribute;
	private LocationStateContextualAttribute locationStateContextualAttribute;
	private LocationCityContextualAttribute locationCityContextualAttribute;
	private CompanionContextualAttribute aloneOrNotContextualAttribute;
	private CompanionContextualAttribute companionTypeContextualAttribute;
	private TaskContextualAttribute taskContextualAttribute;
	
	private List<AbstractContextualAttribute> contextualAttributes;
	
	private void initializeContextualAttributesList(){
		contextualAttributes = new ArrayList<AbstractContextualAttribute>();
		contextualAttributes.add(dayTypeContextualAttribute);
		contextualAttributes.add(dayContextualAttribute);
		contextualAttributes.add(locationCountryContextualAttribute);
		contextualAttributes.add(locationStateContextualAttribute);
		contextualAttributes.add(locationCityContextualAttribute);
		contextualAttributes.add(aloneOrNotContextualAttribute);
		contextualAttributes.add(companionTypeContextualAttribute);
		contextualAttributes.add(taskContextualAttribute);
	}
	
	public ContextualCriteria() {
		this.dayTypeContextualAttribute = DayTypeContextualAttribute.UNKNOWN;
		this.dayContextualAttribute = DayContextualAttribute.UNKNOWN;
		this.locationCountryContextualAttribute = LocationCountryContextualAttribute.UNKNOWN;
		this.locationStateContextualAttribute = LocationStateContextualAttribute.UNKNOWN;
		this.locationCityContextualAttribute = LocationCityContextualAttribute.getEnum("UNKNOWN");
		this.aloneOrNotContextualAttribute = CompanionContextualAttribute.UNKNOWN;
		this.companionTypeContextualAttribute = CompanionContextualAttribute.UNKNOWN;
		this.taskContextualAttribute = TaskContextualAttribute.UNKNOWN;
		
		initializeContextualAttributesList();
	}
	
	public ContextualCriteria(long contexts[]) {
		
		int index = 0;
		//ContextualFileAttributeSequence instance = ContextualFileAttributeSequence.getInstance();		
		this.dayTypeContextualAttribute = DayTypeContextualAttribute.getInstanceByCode(contexts[index++]);
		this.dayContextualAttribute = DayContextualAttribute.getInstanceByCode(contexts[index++]);
		this.locationCountryContextualAttribute = LocationCountryContextualAttribute.getInstanceByCode(contexts[index++]);
		this.locationStateContextualAttribute = LocationStateContextualAttribute.getInstanceByCode(contexts[index++]);
		this.locationCityContextualAttribute = LocationCityContextualAttribute.getInstanceByCode(contexts[index++]);
		this.aloneOrNotContextualAttribute = CompanionContextualAttribute.getInstanceByCode(contexts[index++]);
		this.companionTypeContextualAttribute = CompanionContextualAttribute.getInstanceByCode(contexts[index++]);
		this.taskContextualAttribute = TaskContextualAttribute.getInstanceByCode(contexts[index++]);
		
		initializeContextualAttributesList();
	}
	
	

	public ContextualCriteria(
			DayTypeContextualAttribute dayTypeContextualAttribute,
			DayContextualAttribute dayContextualAttribute,
			LocationCountryContextualAttribute locationCountryContextualAttribute,
			LocationStateContextualAttribute locationStateContextualAttribute,
			LocationCityContextualAttribute locationCityContextualAttribute,
			CompanionContextualAttribute aloneOrNotContextualAttribute,
			CompanionContextualAttribute companionTypeContextualAttribute,
			TaskContextualAttribute taskContextualAttribute) {
		if(dayTypeContextualAttribute == null){
			this.dayTypeContextualAttribute = DayTypeContextualAttribute.UNKNOWN;
		}else{
			this.dayTypeContextualAttribute = dayTypeContextualAttribute;
		}
		
		if(dayContextualAttribute == null){
			this.dayContextualAttribute = DayContextualAttribute.UNKNOWN;
		}else{
			this.dayContextualAttribute = dayContextualAttribute;
		}
		
		if(locationCountryContextualAttribute == null){
			this.locationCountryContextualAttribute = LocationCountryContextualAttribute.UNKNOWN;
		}else{
			this.locationCountryContextualAttribute = locationCountryContextualAttribute;
		}
		
		if(locationStateContextualAttribute == null){
			this.locationStateContextualAttribute = LocationStateContextualAttribute.UNKNOWN;
		}else{
			this.locationStateContextualAttribute = locationStateContextualAttribute;
		}
		
		if(locationCityContextualAttribute == null){
			this.locationCityContextualAttribute = LocationCityContextualAttribute.getEnum("UNKNOWN");
		}else{
			this.locationCityContextualAttribute = locationCityContextualAttribute;
		}
		
		if(aloneOrNotContextualAttribute == null){
			this.aloneOrNotContextualAttribute = CompanionContextualAttribute.UNKNOWN;
		}else{
			this.aloneOrNotContextualAttribute = aloneOrNotContextualAttribute;
		}
		
		if(companionTypeContextualAttribute == null){
			this.companionTypeContextualAttribute = CompanionContextualAttribute.UNKNOWN;
		}else{
			this.companionTypeContextualAttribute = companionTypeContextualAttribute;
		}
		
		if(taskContextualAttribute == null){
			this.taskContextualAttribute = TaskContextualAttribute.UNKNOWN;
		}else{
			this.taskContextualAttribute = taskContextualAttribute;
		}
		
		initializeContextualAttributesList();
	}

	private DayTypeContextualAttribute getDayTypeContextualAttribute() {
		return dayTypeContextualAttribute;
	}

	private DayContextualAttribute getDayContextualAttribute() {
		return dayContextualAttribute;
	}
	
	public LocationCountryContextualAttribute getLocationCountryContextualAttribute() {
		return locationCountryContextualAttribute;
	}

	public LocationStateContextualAttribute getLocationStateContextualAttribute() {
		return locationStateContextualAttribute;
	}

	public LocationCityContextualAttribute getLocationCityContextualAttribute() {
		return locationCityContextualAttribute;
	}

	public CompanionContextualAttribute getAloneOrNotContextualAttribute() {
		return aloneOrNotContextualAttribute;
	}

	public CompanionContextualAttribute getCompanionTypeContextualAttribute() {
		return companionTypeContextualAttribute;
	}

	public TaskContextualAttribute getTaskContextualAttribute() {
		return taskContextualAttribute;
	}

	public List<AbstractContextualAttribute> getContextualAttributes() {
		return contextualAttributes;
	}

	private long getCodeByIndex(int i){
		/*Class<? extends AbstractContextualAttribute> contextualAttributeClass = ContextualFileAttributeSequence.getInstance().get(i);
		if(this.getDayTypeContextualAttribute() != null && contextualAttributeClass.equals(DayTypeContextualAttribute.class)){
			return this.getDayTypeContextualAttribute().getCode();
		}else if(this.getPeriodOfDayContextualAttribute() != null && contextualAttributeClass.equals(PeriodOfDayContextualAttribute.class)){
			return this.getPeriodOfDayContextualAttribute().getCode();
		}
		return -1;*/
		return this.contextualAttributes.get(i).getCode();
	}
	
	public boolean containsAllContextualAttributesIgnoringUnkwnown(long[] contextualPreferences){
		if(contextualPreferences.length > 0){
			for(int i = 0; i < contextualPreferences.length; i++){
				
				if(this.getCodeByIndex(i) != -1 && contextualPreferences[i] != this.getCodeByIndex(i)){
					return false;
				}
				
			}
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ContextualCriteria){
			ContextualCriteria test = (ContextualCriteria) obj;
			
			if(this.contextualAttributes.size() == test.contextualAttributes.size()){
				for(int i=0;i<this.contextualAttributes.size();i++){
					
					if(this.contextualAttributes.get(i)!=null && test.contextualAttributes.get(i)!=null) {
						if(!this.contextualAttributes.get(i).equals(test.contextualAttributes.get(i))){
							return false;
						}
					}
					
				}
				return true;
			}
			
			//return this.toRawList().equals(test.toRawList());

			/*if(test.getDayTypeContextualAttribute().equals(this.getDayTypeContextualAttribute())
					&& test.getPeriodOfDayContextualAttribute().equals(this.getPeriodOfDayContextualAttribute())){
				return true;
			}*/
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		
		/*long dayType = -1;
		
		if(this.getDayTypeContextualAttribute() != null){
			dayType = this.getDayTypeContextualAttribute().getCode();
		}
		
		long periodOfDay = -1;
		
		if(this.getPeriodOfDayContextualAttribute() != null){
			periodOfDay = this.getPeriodOfDayContextualAttribute().getCode();
		}*/
		
		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
		
		for(AbstractContextualAttribute contextualAttr : this.contextualAttributes){
			
			if(contextualAttr!=null) {
				hcb.append(contextualAttr.getCode());
			}
			
		}
		
		/*return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
	            // if deriving: appendSuper(super.hashCode()).
	            append(dayType).
	            append(periodOfDay).
	            toHashCode();*/
		return hcb.toHashCode();
	}
	
	@Override
	public String toString() {
		
		/*String dayType = this.getDayTypeContextualAttribute() != null ? this.getDayTypeContextualAttribute().name() : "null";
		String periodOfDay = this.getPeriodOfDayContextualAttribute() != null ? this.getPeriodOfDayContextualAttribute().name() : "null";*/
		
		StringBuffer contextualCriteriaString = new StringBuffer();
		
		for(AbstractContextualAttribute contextualAtt : this.contextualAttributes){
			contextualCriteriaString.append(contextualAtt.name());
			contextualCriteriaString.append(", ");
		}
		
		return contextualCriteriaString.toString().substring(0,contextualCriteriaString.toString().length()-1);
	}
	
	public long[] toRawList(){
		long[] rawList = new long[contextualAttributes.size()];
		for (int i = 0; i < contextualAttributes.size(); i++) {
			
			if(contextualAttributes.get(i)!=null) {

				rawList[i] = contextualAttributes.get(i).getCode();
			}
		}
		return rawList;
	}

	public boolean containsAllContextualAttributesIgnoringUnknown(
			ContextualCriteria contextualAttributes) {
		long contextualPreferences[] = contextualAttributes.toRawList();
		if(contextualPreferences.length > 0){
			for(int i = 0; i < contextualPreferences.length; i++){
				
				if(this.getCodeByIndex(i) != -1 && contextualPreferences[i] != this.getCodeByIndex(i)){
					return false;
				}
				
			}
			return true;
		}else{
			return false;
		}
	}
}
