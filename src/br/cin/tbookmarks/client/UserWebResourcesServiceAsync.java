package br.cin.tbookmarks.client;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface UserWebResourcesServiceAsync {
	void getUserWebResources(String userId, AsyncCallback<HashMap<String,String>> callback)
			throws Exception;
}
