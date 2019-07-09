package br.cin.tbookmarks.recommender.database.contextual;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import br.cin.tbookmarks.recommender.database.item.ItemCategory;

public class AprioriRuleItemCategory {

	private ItemCategory precedent;
	private ArrayList<ContextualCriteria> precedentContexts;
	private ItemCategory consequent;
	private ArrayList<ContextualCriteria> consequentContexts;
	private int amountOfPrecedent;
	private int amountOfConsequent;
	private int amountTotal;
	
	public AprioriRuleItemCategory(ItemCategory precedent,
			ItemCategory consequent, int amountOfPrecedent, int amountOfConsequent,int total) {
		this.precedent = precedent;
		this.precedentContexts = new ArrayList<ContextualCriteria>();
		this.consequent = consequent;
		this.consequentContexts = new ArrayList<ContextualCriteria>();
		this.amountOfPrecedent = amountOfPrecedent;
		this.amountOfConsequent = amountOfConsequent;
		this.amountTotal = total;
	}
	
	public AprioriRuleItemCategory(ItemCategory precedent,
			ItemCategory consequent,int total) {
		this.precedent = precedent;
		this.precedentContexts = new ArrayList<ContextualCriteria>();
		this.consequent = consequent;
		this.consequentContexts = new ArrayList<ContextualCriteria>();
		this.amountOfPrecedent = 0;
		this.amountOfConsequent = 0;
		this.amountTotal = total;
	}

	public int getAmountOfConsequent() {
		return amountOfConsequent;
	}
	
	public int getAmountOfPrecedent() {
		return amountOfPrecedent;
	}
	
	public void setAmountOfConsequent(int amountOfConsequent) {
		this.amountOfConsequent = amountOfConsequent;
	}
	
	public void setAmountOfPrecedent(int amountOfPrecedent) {
		this.amountOfPrecedent = amountOfPrecedent;
	}
	
	public ItemCategory getPrecedent() {
		return precedent;
	}
	
	public ItemCategory getConsequent() {
		return consequent;
	}
	
	public ArrayList<ContextualCriteria> getPrecedentContexts() {
		return precedentContexts;
	}
	
	public ArrayList<ContextualCriteria> getConsequentContexts() {
		return consequentContexts;
	}
	
	public double getConfidence(){
		return (double)this.getAmountOfConsequent()/(double)this.getAmountOfPrecedent();
	}
	
	public double getSupport(){
		return (double) this.getAmountOfConsequent()/(double)this.amountTotal;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AprioriRuleItemCategory){
			AprioriRuleItemCategory test = (AprioriRuleItemCategory) obj;
			
			if(this.getPrecedent().equals(test.getPrecedent()) && 
					this.getConsequent().equals(test.getConsequent())
					){
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {

		
		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
		
		//for(AbstractContextualAttribute contextualAttr : this.contextualAttributes){
			hcb.append(this.getPrecedent().getCode());
			hcb.append(this.getConsequent().getCode());
		//}
		
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
		
		//for(AbstractContextualAttribute contextualAtt : this.contextualAttributes){
			contextualCriteriaString.append(this.getPrecedent().name());
			contextualCriteriaString.append(" ");
			contextualCriteriaString.append(this.getAmountOfPrecedent());
			contextualCriteriaString.append(" => ");
			contextualCriteriaString.append(this.getConsequent().name());
			contextualCriteriaString.append(" ");
			contextualCriteriaString.append(this.getAmountOfConsequent());
			contextualCriteriaString.append(" conf: ");
			contextualCriteriaString.append(this.getConfidence());
			contextualCriteriaString.append(" supp: ");
			contextualCriteriaString.append(this.getSupport());
		//}
		
		return contextualCriteriaString.toString();
	}
	
	

}
