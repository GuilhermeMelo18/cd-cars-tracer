package br.cin.tbookmarks.recommender.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import br.cin.tbookmarks.recommender.database.item.ItemCategory;
import br.cin.tbookmarks.recommender.database.item.ItemDatasetInformation;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.database.item.ItemInformation;

import java.util.HashSet;

public final class EventsTwitterDataset extends AbstractDataset {

	private static final EventsTwitterDataset INSTANCE = new EventsTwitterDataset();
	private static final boolean initializeDM = false;

	/*
	 * public static String datasetURL =
	 * "\\resources\\datasets\\groupLens\\100K\\ua.base"; public static String
	 * datasetInformationURL = "\\resources\\datasets\\groupLens\\100K\\u.item";
	 * public static String datasetInformationDelimiter = "\\|";
	 */

	private String datasetURLOriginal = "\\resources\\datasets\\twitter\\events\\events_ratings.dat";
	private String datasetInformationURL = "\\resources\\datasets\\twitter\\events\\events.dat";
	private String datasetInformationDelimiter = ",";
	private String timestampFormat = "YYYY-MM-DD hh:mm:ss";
	private boolean haveHeader = true;
	private HashMap<String, String> implicitExplicitMapping;
	{
		datasetURL = "\\resources\\datasets\\twitter\\events\\contextual_events_ratings.dat";
		implicitExplicitMapping = new HashMap<String, String>();
		implicitExplicitMapping.put("Maybe", "2.5");
		implicitExplicitMapping.put("Yes", "4.0");
	}

	private EventsTwitterDataset() {
		try {
			if(initializeDM){
				convertDatasetFileToDefaultPattern();
				//initializeDataModel();
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

	public static EventsTwitterDataset getInstance() {
		return INSTANCE;
	}
	
	private long getDifference(String text) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(this.timestampFormat);
		Date d = sdf.parse(text);

		return d.getTime();
	}

	private void convertDatasetFileToDefaultPattern() {

		File fileEN = new File(System.getProperty("user.dir")
				+ datasetURLOriginal);
		File fileOutput = new File(System.getProperty("user.dir") + datasetURL);

		if (!fileOutput.exists()) {/*

			FileInputStream stream;

			String line = "";
			try {
				stream = new FileInputStream(fileEN);

				InputStreamReader streamReader = new InputStreamReader(stream);
				BufferedReader reader = new BufferedReader(streamReader);

				FileOutputStream streamOutput = new FileOutputStream(fileOutput);

				OutputStreamWriter streamWriter = new OutputStreamWriter(
						streamOutput);

				BufferedWriter bw = new BufferedWriter(streamWriter);

				HashMap<String, Integer> idMaps = new HashMap<String, Integer>();

				Integer userId = 1;

				// StringBuffer texto = new StringBuffer();
				if (haveHeader) {
					line = reader.readLine();// pula primeira linha - cabecalho
				}

				

				while ((line = reader.readLine()) != null) {

					String replaced = removeCommaInField(line);
					// System.out.println(replaced);

					String[] aux = replaced.split(this.datasetInformationDelimiter);
					
					String itemId = aux[3];
					if(itemId.contains("-")){
						itemId = itemId.split("-")[0];
					}
					
					String timeStamp = String.valueOf(this.getDifference(aux[6]));
					
					String rating = aux[2];
					if (implicitExplicitMapping.containsKey(aux[2])) {
						rating = implicitExplicitMapping.get(rating);
					}
					
					String userIdText = "";
					
					if (!idMaps.keySet().contains(aux[0])) {
						idMaps.put(aux[0], userId);
						userIdText = String.valueOf(userId);
						userId++;
					} else {
						userIdText = String.valueOf(idMaps.get(aux[0]));
					}
					
					bw.append(userIdText + "\t" + itemId + "\t" + rating + "\t"
							+ timeStamp);
					bw.newLine();

				}

				reader.close();
				streamReader.close();
				bw.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println(line);
				e.printStackTrace();
			} catch (PatternSyntaxException e) {
				System.err.println(line);
				e.printStackTrace();
			} catch (ParseException e) {
				System.err.println(line);
				e.printStackTrace();
			}
		*/}

	}

	private String removeCommaInField(String line) {
		
		String regex = "\".*?\"";

		Pattern pattern = Pattern.compile(regex);
		
		Matcher matcher = pattern.matcher(line);

		String replaced = line;

		while (matcher.find()) {
			String auxReplace = matcher.group().replaceAll(",", "");
			replaced = replaced
					.replace(matcher.group(), auxReplace);
		}
		return replaced;
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

		if(haveHeader){
			line = reader.readLine();
		}
		
		while ((line = reader.readLine()) != null) {
			itemInfo = new ItemInformation();
			
			String replaced = removeCommaInField(line);
			// System.out.println(replaced);
			
			String row[] = replaced.split(datasetInformationDelimiter);
			String itemId = row[0].replace("/", "");
			
			if(itemId.contains("-")){
				itemId = itemId.split("-")[0];
			}
			
			itemInfo.setId(Long.parseLong(itemId));
			
			itemInfo.setName(row[7]);
			//String categories[] = row[index].split("\\|");

			Set<ItemCategory> itemCategories = new HashSet<ItemCategory>();
			//itemCategories.add(ItemCategory.MUSICALS);
			

			itemInfo.setCategories(itemCategories);
			// itemInfo.setYearReleased(row[index++]);
			// itemInfo.setLink(row[index++]);
			
			itemInfo.setItemDomain(ItemDomain.EVENT);

			itemDatasetInformation.getItens().add(itemInfo);
			countItemInformation++;
		}

		System.out.println(countItemInformation);

		reader.close();
		streamReader.close();

	}

}
