package br.cin.tbookmarks.recommender.database.contextual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import br.cin.tbookmarks.util.Functions;

public enum CompanionContextualAttribute implements AbstractContextualAttribute{

	UNKNOWN(-1),
	ALONE(0), SOLO(0), /*UNACCOMPANIED(0), I(0), ME(0), MY(0), MINE(0), MYSELF(0),*/
	ACCOMPANIED(1), COMPANION(1),/*WE(1), OUR(1), OURS(1), OURSELVES(1),*/
	FAMILY(2),FAMILIES(2), KID(2), CHILD(2), SON(2), PARENT(2), YOUNGSTER(2), BABY(2), /*BABIES(2),*/ 
	FATHER(2), MOTHER(2), DAD(2), MOM(2), RELATIVE(2), DADDY(2), DADDIES(2), MOMMY(2), 
	MOMMIES(2), KIDS(2),CHILDS(2),CHILDREN(2),CHILDREEN(2),PARENTS(2),RELATIVES(2),
	FRIEND(3),FRIENDS(3),
	PARTNER(4), COUPLE(4), SPOUSE(4), MATE(4), HUSBAND(4), WIFE(4), HUBBY(4), BOYFRIEND(4),
	GIRLFRIEND(4),PARTNERS(4), COUPLES(4),
	COLLEAGUE(5), COLLEAGUES(5),STUDENTS(5), CLASSROOM(5), SCHOOLROOM(5), CO_WORKER(5), FELLOW(5)
			, CO_WORKERS(5), FELLOWS(5), SEMINARY(5),SERMON(5);


	private long code;

	private CompanionContextualAttribute(long value) {
		this.code = value;
	}

	@Override
	public long getCode() {
		return this.code;
	}

	public static CompanionContextualAttribute getInstanceByCode(long code){
		
		for(CompanionContextualAttribute d : CompanionContextualAttribute.values()){
			if(d.getCode() == code){
				return d;
			}
		}
		
		return null;
	}
	
	public static CompanionContextualAttribute getEnum(String name) {
		
		for(CompanionContextualAttribute d : CompanionContextualAttribute.values()){
			if(Functions.containsToken(name, d.name())){
				return d;
			}
		}
		
		if(Functions.containsToken(name, "co-worker")){
			return CO_WORKER;
		}
		
		if(Functions.containsToken(name, "co-workers")){
			return CO_WORKERS;
		}
		
		return UNKNOWN;
		
		/*try{
			return valueOf(name.toUpperCase());
		}catch(Exception e){
		
			if (name.equalsIgnoreCase("co-worker")) {
				return CO_WORKER;
			}else{
				//System.out.println(name + " category unknown");
				return UNKNOWN;
			}
		}*/
	}

	@Override
	public List<AbstractContextualAttribute> valuesForTest() {
		
		HashSet<Long> aux = new HashSet<Long>();
		
		List<AbstractContextualAttribute> valuesForTest = new ArrayList<AbstractContextualAttribute>();
		for(CompanionContextualAttribute attr : CompanionContextualAttribute.values()){
			if(!attr.equals(CompanionContextualAttribute.UNKNOWN) && !aux.contains(attr.getCode())){
				valuesForTest.add(attr);
				aux.add(attr.getCode());
			}
		}
		
		return valuesForTest;
	}
}
