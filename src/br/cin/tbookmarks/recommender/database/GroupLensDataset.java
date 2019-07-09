package br.cin.tbookmarks.recommender.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;






import br.cin.tbookmarks.recommender.database.item.ItemCategory;
import br.cin.tbookmarks.recommender.database.item.ItemDatasetInformation;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.database.item.ItemInformation;

import java.util.HashSet;

public final class GroupLensDataset extends AbstractDataset {

	private static final GroupLensDataset INSTANCE = new GroupLensDataset();
	private static final boolean initializeDM = false;
	
	{
		datasetURL = "\\resources\\datasets\\groupLens\\1M\\contextual_ratings.dat";
	}
	private String datasetInformationURL = "\\resources\\datasets\\groupLens\\1M\\movies.dat";
	private String datasetInformationDelimiter = ";";

	private GroupLensDataset() {
		try {
			if(initializeDM){
			//	initializeDataModel();	
			}
			
			initializeDBInfo();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static GroupLensDataset getInstance() {
		return INSTANCE;
	}


	private void initializeDBInfo() throws NumberFormatException, IOException {

		itemDatasetInformation = new ItemDatasetInformation();
		File fileEN = new File(System.getProperty("user.dir")
				+ this.datasetInformationURL);

		FileInputStream stream;

		stream = new FileInputStream(fileEN);

		InputStreamReader streamReader = new InputStreamReader(stream);
		BufferedReader reader = new BufferedReader(streamReader);

		String line;

		int countItemInformation = 0;

		ItemInformation itemInfo;
		int index;

		while ((line = reader.readLine()) != null) {
			index = 0;
			itemInfo = new ItemInformation();
			String row[] = line.split(datasetInformationDelimiter);
			itemInfo.setId(Long.parseLong(row[index++]));
			itemInfo.setName(row[index++]);
			String categories[] = row[index].split("\\|");

			Set<ItemCategory> itemCategories = new HashSet<ItemCategory>();

			for (int i = 0; i < categories.length; i++) {
//				itemCategories.add(ItemCategory.getCategoryEnum(categories[i]
//						.toUpperCase()));
			}

			itemInfo.setCategories(itemCategories);
			// itemInfo.setYearReleased(row[index++]);
			// itemInfo.setLink(row[index++]);

			itemInfo.setItemDomain(ItemDomain.MOVIE);

			itemDatasetInformation.getItens().add(itemInfo);
			countItemInformation++;
		}

		System.out.println(countItemInformation);

		reader.close();
		streamReader.close();

	}

}
