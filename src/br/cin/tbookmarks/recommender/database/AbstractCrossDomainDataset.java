package br.cin.tbookmarks.recommender.database;

import java.io.IOException;
import java.util.ArrayList;

import br.cin.tbookmarks.recommender.database.item.ItemDatasetInformation;


public abstract class AbstractCrossDomainDataset extends AbstractDataset {
	
	protected ArrayList<AbstractDataset> datasets;
	
	public AbstractCrossDomainDataset() {
		datasets = new ArrayList<AbstractDataset>();
	}
	
	protected void initializeCrossDomainDataset(){

		try {
			//initializeDataModel();
			initializeDBInfo();
		} /*catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
	}

	protected void initializeDBInfo() {
		this.itemDatasetInformation = new ItemDatasetInformation();
		for (AbstractDataset dataset : datasets) {
			this.itemDatasetInformation.getItens().addAll(
					dataset.getItemDatasetInformation().getItens());
		}
	}

}
