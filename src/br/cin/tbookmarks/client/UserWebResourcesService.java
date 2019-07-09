package br.cin.tbookmarks.client;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("userWebResource")
public interface UserWebResourcesService extends RemoteService {
	HashMap<String,String> getUserWebResources(String userId) throws Exception;
}
