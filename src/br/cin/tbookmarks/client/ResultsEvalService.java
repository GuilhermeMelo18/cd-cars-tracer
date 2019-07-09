package br.cin.tbookmarks.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


/**
 * The client side stub for the RPC service.
 */

@RemoteServiceRelativePath("resultsEvalService")
public interface ResultsEvalService extends RemoteService {
	String getResultsEval(String trial) throws Exception;
}
