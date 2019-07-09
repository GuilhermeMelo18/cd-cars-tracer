package br.cin.tbookmarks.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.recommender.IDRescorer;

import br.cin.tbookmarks.recommender.PostFilteringStrategyRecommendation;
import br.cin.tbookmarks.recommender.algorithms.ContextualRecommenderBuilder;
import br.cin.tbookmarks.recommender.algorithms.PostFilteringContextualBuildRecommender;
import br.cin.tbookmarks.recommender.algorithms.PreFilteringContextualBuildRecommenderByron;
import br.cin.tbookmarks.recommender.algorithms.RecommenderBuilderUserBasedNearestNeighborCremonesiRicardo;
import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.AmazonCrossDataset;
import br.cin.tbookmarks.recommender.database.contextual.AbstractContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.CompanionContextualAttribute;
import br.cin.tbookmarks.recommender.database.contextual.ContextualCriteria;
import br.cin.tbookmarks.recommender.database.contextual.DayTypeContextualAttribute;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.similarity.ItemDomainRescorer;
import br.cin.tbookmarks.shared.FieldVerifier;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TBookmarksRS implements EntryPoint {

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	/*private final UserWebResourcesServiceAsync userWebResourcesService = GWT
			.create(UserWebResourcesService.class);*/
	
	private final ResultsEvalServiceAsync resultsEvalService = GWT
			.create(ResultsEvalService.class);
	
	
	

	
	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {
		
		
		String userAgent = Window.Navigator.getUserAgent().toLowerCase();
		
		String androidPage = "AndroidTBookmarksServerRS.html";
		String server = "http://tbookmarksrecommend.appspot.com/";
		
		
		boolean isAndroidAndPCpage = userAgent.contains("android") && !Window.Location.getPath().contains(androidPage);
		boolean isIphoneAndPCpage = userAgent.contains("iphone") && !Window.Location.getPath().contains(androidPage);
		
		if(isAndroidAndPCpage || isIphoneAndPCpage){
			Window.open(server+androidPage, "_self", "");
		}/*else if(!userAgent.contains("android") && Window.Location.getPath().contains(androidPage)){
			Window.open(server, "_self", "");
		}*/


		final Button sendButtonTest = new Button("Execute");
		final TextBox nameField = new TextBox();
		nameField.setText("e.x. 56240");
		final Label errorLabel = new Label();

		// We can add style names to widgets
		//sendButton.setStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel rootPanel = RootPanel.get("nameFieldContainer");
		rootPanel.add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButtonTest);
		RootPanel.get("errorLabelContainer").add(errorLabel);
		
		Label labelFieldMessage = new Label();
		labelFieldMessage.setText("Please enter the user ID number:");
		labelFieldMessage.setStyleName("field-Message");
		
		RootPanel.get("fieldMessage").add(labelFieldMessage);

		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);

		nameField.selectAll();

		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			@Override
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendNameToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a
			 * response.
			 */
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = nameField.getText();
				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel.setText("Please enter a nonnegative integer");
					return;
				}

				// Then, we send the input to the server.
				// sendButton.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");

				RootPanel.get("resultsEval").clear();
				//RootPanel.get("resultsHistory").clear();
				
				
				try {
					resultsEvalService.getResultsEval(textToServer,
							new AsyncCallback<String>() {
								@Override
								public void onFailure(Throwable caught) {
									Window.alert(caught.getMessage());
									
								}
								
								@Override
								public void onSuccess(String results) {
									
									RootPanel.get("resultsEval").clear();
									
									final Label labelCurrentResults = new Label();
									labelCurrentResults.setText(results);
									labelCurrentResults.addStyleName("results-label");
									
									RootPanel.get("resultsEval").add(labelCurrentResults);
									
									
								}

								/*@Override
								public void onSuccess(List<Result> results) {
									CellTable<Result> currentResultsTree = createTableToResultsEval(results);
									addResultPanelWithTable(currentResultsTree,"Results Eval","resultsEval", "results-panel-eval");
									
//									Tree historyResultsTree = createTreeToRecommendationType(results,EnumRecommendationType.HISTORY);
//									addResultPanelWithTree(historyResultsTree,"History Recommendation","resultsHistory", "results-panel-history");
								}*/
								
								/*private  CellTable<Result> createTableToResultsEval(List<Result> response) {
									// Create a CellTable.
								    CellTable<Result> table = new CellTable<Result>();

								   
								    TextColumn<Result> trialColumn = new TextColumn<Result>() {
								      @Override
								      public String getValue(Result result) {
								        return String.valueOf(result.getTrial());
								      }
								    };

								    // Make the trial column sortable.
								    trialColumn.setSortable(true);
								   
								    TextColumn<Result> contextColumn = new TextColumn<Result>() {
								      @Override
								      public String getValue(Result result) {
								        return result.getContext();
								      }
								    };

								    // Add the columns.
								    table.addColumn(trialColumn, "Trial");
								    table.addColumn(contextColumn, "Context");

								    // Create a data provider.
								    ListDataProvider<Result> dataProvider = new ListDataProvider<Result>();

								    // Connect the table to the data provider.
								    dataProvider.addDataDisplay(table);

								    // Add the data to the data provider, which automatically pushes it to the
								    // widget.
								    List<Result> list = dataProvider.getList();
								    for (Result r : response) {
								      list.add(r);
								    }

								    // Add a ColumnSortEvent.ListHandler to connect sorting to the
								    // java.util.List.
								    ListHandler<Result> columnSortHandler = new ListHandler<Result>(
								        list);
								    columnSortHandler.setComparator(trialColumn, new Comparator<Result>() {
										
										@Override
										public int compare(Result o1, Result o2) {
								            if (o1 == o2) {
								              return 0;
								            }

								            // Compare the trial columns.
								            if (o1 != null) {
								              return (o2 != null) ? String.valueOf(o1.getTrial()).compareTo(String.valueOf(o2.getTrial())) : 1;
								            }
								            return -1;
								          }
									});
								    table.addColumnSortHandler(columnSortHandler);

								    // We know that the data is sorted alphabetically by default.
								    table.getColumnSortList().push(trialColumn);

								    // Add it to the root panel.
								    return table;
									
								}*/
							});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButtonTest.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
	
	}

/*	private void addResultPanelWithTree(Tree t, String resultsName, String htmlElementId, String style) {
		HorizontalPanel horizontalPanelCurrentResults = new HorizontalPanel();
		horizontalPanelCurrentResults.setBorderWidth(1);
		horizontalPanelCurrentResults.addStyleName("results-panel");
		horizontalPanelCurrentResults.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);

		horizontalPanelCurrentResults.add(t);
		
		final Label labelCurrentResults = new Label();
		labelCurrentResults.setText(resultsName);
		labelCurrentResults.addStyleName("results-label");
		
		RootPanel.get(htmlElementId).clear();
		
		RootPanel.get(htmlElementId).add(labelCurrentResults);
		
		RootPanel.get(htmlElementId).add(horizontalPanelCurrentResults);
		
		RootPanel.get(htmlElementId).addStyleName(style);
	}*/
	
	/*private void addResultPanelWithTable(CellTable<Result> t, String resultsName, String htmlElementId, String style) {
		HorizontalPanel horizontalPanelCurrentResults = new HorizontalPanel();
		horizontalPanelCurrentResults.setBorderWidth(1);
		horizontalPanelCurrentResults.addStyleName("results-panel");
		horizontalPanelCurrentResults.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		horizontalPanelCurrentResults.add(t);
		
		final Label labelCurrentResults = new Label();
		labelCurrentResults.setText(resultsName);
		labelCurrentResults.addStyleName("results-label");
		
		RootPanel.get(htmlElementId).clear();
		
		RootPanel.get(htmlElementId).add(labelCurrentResults);
		
		RootPanel.get(htmlElementId).add(horizontalPanelCurrentResults);
		
		RootPanel.get(htmlElementId).addStyleName(style);
	}*/
}
