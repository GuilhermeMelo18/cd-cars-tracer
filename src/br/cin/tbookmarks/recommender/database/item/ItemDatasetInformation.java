package br.cin.tbookmarks.recommender.database.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

public class ItemDatasetInformation {
	List<ItemInformation> itens;
	
	public ItemDatasetInformation() {
		itens = new ArrayList<ItemInformation>();
	}

	public List<ItemInformation> getItens() {
		return itens;
	}

	public void setItens(List<ItemInformation> itens) {
		this.itens = itens;
	}
	
	
}
