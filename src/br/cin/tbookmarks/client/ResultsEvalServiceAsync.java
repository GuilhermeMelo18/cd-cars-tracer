package br.cin.tbookmarks.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface ResultsEvalServiceAsync {
	void getResultsEval(String trial, AsyncCallback<String> asyncCallback);
}
