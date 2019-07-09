package br.cin.tbookmarks.recommender.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import br.cin.tbookmarks.recommender.database.item.ItemCategory;
import br.cin.tbookmarks.recommender.database.item.ItemDatasetInformation;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.database.item.ItemInformation;

import java.util.HashSet;

public final class BooksTwitterDataset extends AbstractDataset {

	private static final BooksTwitterDataset INSTANCE = new BooksTwitterDataset();
	private static final boolean initializeDM = true;
	
	/*
	 * public static String datasetURL =
	 * "\\resources\\datasets\\groupLens\\100K\\ua.base"; public static String
	 * datasetInformationURL = "\\resources\\datasets\\groupLens\\100K\\u.item";
	 * public static String datasetInformationDelimiter = "\\|";
	 */

	private String datasetURLOriginal = "\\resources\\datasets\\twitter\\books\\books_ratings.dat";
	{
		datasetURL = "\\resources\\datasets\\twitter\\books\\contextual_books_ratings.dat";
	}
	private String datasetInformationURL = "\\resources\\datasets\\twitter\\books\\books.dat";
	private String datasetInformationDelimiter = "::";
	private boolean haveHeader = false;

	/*
	 * private HashMap<String, String> implicitExplicitMapping; {
	 * implicitExplicitMapping = new HashMap<String, String>();
	 * implicitExplicitMapping.put("Maybe", "2.5");
	 * implicitExplicitMapping.put("Yes", "4.0"); }
	 */

	private BooksTwitterDataset() {
		try {
			if(initializeDM){
				convertDatasetFileToDefaultPattern();
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

	public static BooksTwitterDataset getInstance() {
		return INSTANCE;
	}
	
	private Integer getBookID(HashMap<String, Integer> itemIdMaps, String bookText){
		for(String bookTitle : itemIdMaps.keySet()){
			if(bookTitle.equalsIgnoreCase(bookText)){
				return itemIdMaps.get(bookTitle);
			}
		}
		return -1;
	}

	private void convertDatasetFileToDefaultPattern() {/*

		File fileEN = new File(System.getProperty("user.dir")
				+ datasetURLOriginal);
		File fileOutput = new File(System.getProperty("user.dir") + datasetURL);

		File fileOutputInformation = new File(System.getProperty("user.dir")
				+ datasetInformationURL);

		if (!fileOutput.exists() && !fileOutputInformation.exists()) {

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

				FileOutputStream streamOutputInfo = new FileOutputStream(
						fileOutputInformation);

				OutputStreamWriter streamWriterInfo = new OutputStreamWriter(
						streamOutputInfo);

				BufferedWriter bwInfo = new BufferedWriter(streamWriterInfo);

				HashMap<String, Integer> idMaps = new HashMap<String, Integer>();
				HashMap<String, Integer> itemIdMaps = new HashMap<String, Integer>();
				ArrayList<String> itemIdInformation = new ArrayList<String>();

				Integer userId = 1;
				Integer itemId = 100000;

				// StringBuffer texto = new StringBuffer();
				if (haveHeader) {
					line = reader.readLine();// pula primeira linha - cabecalho
				}

				while ((line = reader.readLine()) != null) {

					String replaced = removeCommaInField(line);
					// System.out.println(replaced);

					String[] aux = replaced
							.split(this.datasetInformationDelimiter);

					String itemIdText = aux[3];
					String itemIDRated = "";
					
					if (itemIdText.contains("(")) {
						itemIdText = itemIdText.split("\\(")[0].trim();
					} else if (itemIdText.contains(".")) {
						itemIdText = itemIdText.split("\\.")[0];
					}
					Integer itemIDaux = getBookID(itemIdMaps,itemIdText);
					if (itemIDaux == -1) {
						itemIdMaps.put(itemIdText, itemId);
						itemIDRated = String.valueOf(itemId);
						itemId++;
					} else {
						itemIDRated = String.valueOf(itemIDaux);
					}

					String timeStamp = aux[5];

					String rating = aux[4];

					String userIdText = "";

					if (!idMaps.keySet().contains(aux[0])) {
						idMaps.put(aux[0], userId);
						userIdText = String.valueOf(userId);
						userId++;
					} else {
						userIdText = String.valueOf(idMaps.get(aux[0]));
					}

					String delimiter = "\t";
					
					bw.append(userIdText + delimiter + itemIDRated + delimiter + rating
							+ delimiter + timeStamp);
					bw.newLine();

					if (!itemIdInformation.contains(itemIDRated)) {
						String categoriesText = getCategoryFromResponseAPI(itemIdText);
						bwInfo.append(itemIDRated + delimiter + itemIdText + delimiter
												+ categoriesText);
						bwInfo.newLine();
						itemIdInformation.add(itemIDRated);
					}

				}

				reader.close();
				streamReader.close();
				// streamOutput.close();
				// streamOutputInfo.close();
				bw.close();
				bwInfo.close();

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
			} 
		}
	*/}

	private String removeCommaInField(String line) {

		String regex = "\".*?\"";

		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(line);

		String replaced = line;

		while (matcher.find()) {
			String auxReplace = matcher.group().replaceAll(",", "");
			replaced = replaced.replace(matcher.group(), auxReplace);
		}
		return replaced;
	}

	private String httpGet(String urlStr) throws IOException, InterruptedException {
		  URL url = new URL(urlStr.replaceAll(" ", "%20"));
		  
		  HttpURLConnection conn =
		      (HttpURLConnection) url.openConnection();
		  		  
		  if (conn.getResponseCode() != 200) {
		    throw new IOException(conn.getResponseMessage());
		  }

		  // Buffer the result into a string
		  BufferedReader rd = new BufferedReader(
		      new InputStreamReader(conn.getInputStream()));
		  StringBuilder sb = new StringBuilder();
		  String line;
		  while ((line = rd.readLine()) != null) {
		    sb.append(line);
		  }
		  rd.close();

		  conn.disconnect();
		  return sb.toString();
		}
	
	private String getCategoryFromResponseAPI(String bookTitle){
		StringBuffer categories =  new StringBuffer();
		try {
			String result = httpGet("https://www.goodreads.com/book/show/"+bookTitle);
			JSONObject obj;
		
			obj = new JSONObject(result);
		 
			JSONArray items = obj.getJSONArray("items");
			for (int i = 0; i < items.length(); i++)
			{
			    String bookTitleJSON = items.getJSONObject(i).getJSONObject("volumeInfo").getString("title");
			    boolean bookSubtitleJSONIsNull = items.getJSONObject(i).getJSONObject("volumeInfo").isNull("subtitle");
			    
			    String titlePlusSub = "";
			    
			    if(!bookSubtitleJSONIsNull){
			    	titlePlusSub = bookTitleJSON+": "+items.getJSONObject(i).getJSONObject("volumeInfo").getString("subtitle");
			    }
			    
			    if(bookTitleJSON.equalsIgnoreCase(bookTitle) || titlePlusSub.equalsIgnoreCase(bookTitle)){
			    	JSONArray categoriesJSON = items.getJSONObject(i).getJSONObject("volumeInfo").getJSONArray("categories");
			    	for(int j=0; j < categoriesJSON.length();j++){
				    		categories.append(categoriesJSON.get(j).toString());
				    		categories.append("|");
				    	}
				    return categories.substring(0, categories.length()-1);
			    	
			    }
			    
			}
		}
		catch (JSONException e) {
			System.err.println(e.getMessage()+" for "+bookTitle);
			//System.err.println("Categories of"+bookTitle+" not found");
		} catch (IOException e) {
			System.err.println(e.getMessage()+" for "+bookTitle);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		categories.append("Unknown");
		return categories.toString();
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

		if (haveHeader) {
			line = reader.readLine();
		}

		while ((line = reader.readLine()) != null) {
			itemInfo = new ItemInformation();

			String replaced = removeCommaInField(line);
			// System.out.println(replaced);

			String row[] = replaced.split(datasetInformationDelimiter);
			/*
			 * String itemId = row[0].replace("/", "");
			 * 
			 * if(itemId.contains("-")){ itemId = itemId.split("-")[0]; }
			 */

			itemInfo.setId(Long.parseLong(row[0]));

			itemInfo.setName(row[1]);
			//System.out.println(line);
			String categories[] = row[2].split("\\|");
			
			 Set<ItemCategory> itemCategories = new HashSet<ItemCategory>();
			 for(String category : categories){
			//	 itemCategories.add(ItemCategory.getCategoryEnum(category.toUpperCase()));
			 }			  
			  
			 itemInfo.setCategories(itemCategories);
			 
			// itemInfo.setYearReleased(row[index++]);
			// itemInfo.setLink(row[index++]);

			itemInfo.setItemDomain(ItemDomain.BOOK);

			itemDatasetInformation.getItens().add(itemInfo);
			countItemInformation++;
		}

		System.out.println(countItemInformation);

		reader.close();
		streamReader.close();

	}

}
