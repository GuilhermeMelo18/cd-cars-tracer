package br.cin.tbookmarks.client;


import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;



public abstract class ResultsPersistence {
	
	private static final PersistenceManagerFactory pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	public static PersistenceManagerFactory getPersistenceManagerFactory() {
		return pmfInstance;
	}

	public static String addResult(Result result){
				
		Result resultPersisted = null;
		
		PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

		//Result result = new Result();

        try {
            resultPersisted = pm.makePersistent(result);
        } finally {
            pm.close();
        }
				
        if(resultPersisted != null){
        	return resultPersisted.getResultID().toString();
        }else{
        	return null;
        }
		
		
	}
	

}
