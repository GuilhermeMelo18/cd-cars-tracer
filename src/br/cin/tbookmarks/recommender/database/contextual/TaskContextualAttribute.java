package br.cin.tbookmarks.recommender.database.contextual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.cin.tbookmarks.util.Functions;

public enum TaskContextualAttribute implements AbstractContextualAttribute{

	UNKNOWN(-1),
	LEARNING(0), DISCOVERING(0), STUDYING(0), TEACHING(0), UNDERSTANDING(0), EXPLAINING(0),
	ENTERTAINMENT(1), ENTERTAINING(1),
	WORKING(2),
	EXERCISING(3), WORKING_OUT(3), DANCING(3), RUNING(3), RUNNING(3),
	PLAYING(4), GAMING(4), GAMBLING(4),
	SELF_HELP(5), THINKING(5), SURVIVING(5),
	COOKING(6),
	TRAVELING(7), TRAVELLING(7),
	SLEEPING(8),
	FIXING(9), REPAIRING(9),
	PRAYING(10),PREACHING(10),
	STORYTELLING(11)
	;


	private long code;

	private TaskContextualAttribute(long value) {
		this.code = value;
	}

	@Override
	public long getCode() {
		return this.code;
	}

	public static TaskContextualAttribute getInstanceByCode(long code){
		
		for(TaskContextualAttribute d : TaskContextualAttribute.values()){
			if(d.getCode() == code){
				return d;
			}
		}
		
		return null;
	}
	
	
	
	public static TaskContextualAttribute getEnum(String name) {
		
		for(TaskContextualAttribute d : TaskContextualAttribute.values()){
			if(Functions.containsToken(name, d.name())){
				return d;
			}
		}
		
		if(Functions.containsToken(name, "working out")){
			return WORKING_OUT;
		}
		
		if(Functions.containsToken(name, "self-help")){
			return SELF_HELP;
		}
		
		return UNKNOWN;
		
		/*try{
			return valueOf(name.toUpperCase());
		}catch(Exception e){
		
			if (name.equalsIgnoreCase("working out")) {
				return WORKING_OUT;
			}else if(name.equalsIgnoreCase("self-help")){
				return SELF_HELP;
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
		for(TaskContextualAttribute attr : TaskContextualAttribute.values()){
			if(!attr.equals(TaskContextualAttribute.UNKNOWN) && !aux.contains(attr.getCode())){
				valuesForTest.add(attr);
				aux.add(attr.getCode());
			}
		}
		return valuesForTest;
	}

}
