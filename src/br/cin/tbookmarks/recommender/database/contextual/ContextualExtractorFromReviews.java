package br.cin.tbookmarks.recommender.database.contextual;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliasi.hmm.HmmDecoder;
import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import br.cin.tbookmarks.recommender.database.AbstractDataset;
import br.cin.tbookmarks.recommender.database.AmazonCrossDataset;
import br.cin.tbookmarks.recommender.database.UserCategoriesPrefsInContexts;
import br.cin.tbookmarks.recommender.database.contextual.textmining.ClusterReviews;
import br.cin.tbookmarks.recommender.database.contextual.textmining.PosTagger;
import br.cin.tbookmarks.recommender.database.contextual.textmining.TextMeasures;
import br.cin.tbookmarks.recommender.database.item.ItemCategory;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.database.user.AddressInformation;
import br.cin.tbookmarks.recommender.database.user.RuleTuple;
import br.cin.tbookmarks.recommender.database.user.UserInformation;
import br.cin.tbookmarks.recommender.database.user.UserInformationComparatorID;
import br.cin.tbookmarks.util.Functions;

public class ContextualExtractorFromReviews {

	// private static final int READ_N_LINES = 20;//temp for tests

	private static final Logger log = LoggerFactory
			.getLogger(ContextualExtractorFromReviews.class);

	private static ContextualExtractorFromReviews instance = null;

	private static ArrayList<EntryReviewFile> bookReviewsList;
	private static ArrayList<EntryReviewFile> televisionReviewsList;

	private static ArrayList<EntryReviewFile> specificBookReviewsList;
	private static ArrayList<EntryReviewFile> genericBookReviewsList;

	private static ArrayList<EntryReviewFile> specificTelevisionReviewsList;
	private static ArrayList<EntryReviewFile> genericTelevisionReviewsList;

	private static TextMeasures booksReviewMinimal;
	private static TextMeasures booksReviewMaximal;

	private static TextMeasures televisionReviewMinimal;
	private static TextMeasures televisionReviewMaximal;
	
	private static final String PATH_AMAZON_META = "C:\\Users\\Douglas\\Desktop\\Cross_Domain_Tools\\datasets\\amazon\\amazon-meta.txt\\";

	public static final String newDatasetWithContext = PATH_AMAZON_META+"contextual-ratings-full-new-thesis.dat";
	public static final String datasetGenreRulesInformationURL = "C:\\Users\\Douglas\\Desktop\\Cross_Domain_Tools\\datasets\\amazon\\amazon-meta.txt\\genreRulesInformation.dat";
	
	public static final String booksFile = PATH_AMAZON_META+"datasetBooksReviews.txt";
	public static final String televisionFile = PATH_AMAZON_META+"datasetTelevisionReviews.txt";

	public static final String booksContextualTokensFile = PATH_AMAZON_META+"datasetContextualClassifiedBooksReviews.txt";
	public static final String televisionContextualTokensFile = PATH_AMAZON_META+"datasetContextualClassifiedTelevisionReviews.txt";
//	public static final String musicContextualTokensFile = PATH_AMAZON_META+"datasetContextualClassifiedMusicReviews.txt";
	
	public static final String booksReviewsContextualTopics = PATH_AMAZON_META+"booksReviewsContextualTopics.txt";
	public static final String televisionReviewsContextualTopics = PATH_AMAZON_META+"televisionReviewsContextualTopics.txt";

	public static final String booksSpecificFile = PATH_AMAZON_META+"datasetSpecificBooksReviews.txt";
	public static final String booksGenericFile = PATH_AMAZON_META+"datasetGenericBooksReviews.txt";
	public static final String tvSpecificFile = PATH_AMAZON_META+"datasetSpecificTelevisionReviews.txt";
	public static final String tvGenericFile = PATH_AMAZON_META+"datasetGenericTelevisionReviews.txt";

	private static final String musicFile = PATH_AMAZON_META+"datasetMusicReviews.txt";

	private static final String musicContextualTokensFile = PATH_AMAZON_META+"datasetContextualClassifiedMusicReviews.txt";;

	private static final String musicReviewsContextualTopics = PATH_AMAZON_META+"musicReviewsContextualTopics.txt";

	protected ContextualExtractorFromReviews() {
		this(false);
	}
	
	protected ContextualExtractorFromReviews(boolean separateSpecificFromGenric) {
		if(separateSpecificFromGenric){
			bookReviewsList = new ArrayList<ContextualExtractorFromReviews.EntryReviewFile>();
			televisionReviewsList = new ArrayList<ContextualExtractorFromReviews.EntryReviewFile>();
	
			specificBookReviewsList = new ArrayList<ContextualExtractorFromReviews.EntryReviewFile>();
			genericBookReviewsList = new ArrayList<ContextualExtractorFromReviews.EntryReviewFile>();
	
			specificTelevisionReviewsList = new ArrayList<ContextualExtractorFromReviews.EntryReviewFile>();
			genericTelevisionReviewsList = new ArrayList<ContextualExtractorFromReviews.EntryReviewFile>();
	
			booksReviewMinimal = new TextMeasures(10, 10, 10, 10, 10);
			booksReviewMaximal = new TextMeasures(0, 0, 0, 0, 0);
			televisionReviewMinimal = new TextMeasures(10, 10, 10, 10, 10);
			televisionReviewMaximal = new TextMeasures(0, 0, 0, 0, 0);
	
			readReviewsFromFile(bookReviewsList, booksFile);
			readReviewsFromFile(televisionReviewsList, televisionFile);
	
			separateSpecificFromGenericReviews(bookReviewsList, booksReviewMinimal,
					booksReviewMaximal, specificBookReviewsList,
					genericBookReviewsList);
			separateSpecificFromGenericReviews(televisionReviewsList,
					televisionReviewMinimal, televisionReviewMaximal,
					specificTelevisionReviewsList, genericTelevisionReviewsList);
	
			// System.err.println("BOOKS:");
			// showSpecificAndGenericReviews(specificBookReviewsList,
			// genericBookReviewsList);
	
			exportSpecificAndGenericReviews(specificBookReviewsList,
					booksSpecificFile);
			exportSpecificAndGenericReviews(genericBookReviewsList,
					booksGenericFile);
			exportSpecificAndGenericReviews(specificTelevisionReviewsList,
					tvSpecificFile);
			exportSpecificAndGenericReviews(genericTelevisionReviewsList,
					tvGenericFile);
			// System.err.println("TV:");
			// showSpecificAndGenericReviews(specificTelevisionReviewsList,
			// genericTelevisionReviewsList);
	
			System.out.println(specificBookReviewsList.size());
			System.out.println(genericBookReviewsList.size());
			System.out.println(specificTelevisionReviewsList.size());
			System.out.println(genericTelevisionReviewsList.size());
		}
	}
	
	private void exportTopics(List<String> rows, String outputFile) {
		File fileOutputReviewsFile = new File(outputFile);

		FileOutputStream streamOutputReviewsFile;
		OutputStreamWriter streamWriterReviewsFile;
		BufferedWriter bwReviewsFile;
		try {
			streamOutputReviewsFile = new FileOutputStream(
					fileOutputReviewsFile);

			streamWriterReviewsFile = new OutputStreamWriter(
					streamOutputReviewsFile);

			bwReviewsFile = new BufferedWriter(streamWriterReviewsFile);

			for (int i = 0; i < rows.size(); i++) {
				//for (int index = 0; index < rows.get(i).length; index++) {
					bwReviewsFile.append(rows.get(i));
					bwReviewsFile.newLine();
				//}

			}

			bwReviewsFile.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}

	}
	
	
	
//	private List<EntryContextualizedReviewFile> getSortedEntriesByUser(){
//		
//	}

	public void convertOverlapedDatasetFilesIntoFullContexutalOverlapFiles(String[] ovelapedFiles, String fullDataset){
		
		
		List<String[]> entriesFullDataset = getCsvData(fullDataset);
		//EntriesContextualizedReviewFile entriesFullDatasetList = new EntriesContextualizedReviewFile(entriesFullDataset);
		
		for(String overFile : ovelapedFiles){
			
			int lastIndex = 0;
			List<String[]> entries = getCsvData(overFile);
			
			List<String[]> newEntries = new ArrayList<String[]>();
			String[] newRow;
			
			for(String[] row : entries){
				newRow = new String[4];
				newRow[0] = row[0];
				newRow[1] = row[1];
				
				float rating = Float.valueOf(row[2]);
				
				newRow[2] = String.valueOf((long)rating);
				
				for(int i = lastIndex;lastIndex < entriesFullDataset.size();i++){
					String[] fullDatasetRow = entriesFullDataset.get(i);
					
					if(Float.valueOf(newRow[0]).equals(Float.valueOf(fullDatasetRow[0])) &&
							Float.valueOf(newRow[1]).equals(Float.valueOf(fullDatasetRow[1])) &&	
							Float.valueOf(newRow[2]).equals(Float.valueOf(fullDatasetRow[2]))){
						newRow[3] = fullDatasetRow[3];
						break;
					}
					lastIndex++;
				}
				newEntries.add(newRow);
			}
			exportRows(newEntries, overFile+"_new.dat");
		}
	}
	
	public void exportDatasetPerDomain(AmazonCrossDataset dataset,HashSet<ItemDomain> domains, String domainFileSrc, String fullDataset){
		
		
		List<String[]> entriesFullDataset = getCsvData(fullDataset);
		//EntriesContextualizedReviewFile entriesFullDatasetList = new EntriesContextualizedReviewFile(entriesFullDataset);
		
		for(ItemDomain domain : domains){
			
			List<String[]> newEntries = new ArrayList<String[]>();
			String[] newRow;
			
			for(String[] row : entriesFullDataset){
				if(!dataset.getItemInformationByID(Long.valueOf(row[1])).getItemDomain().equals(domain)){
					continue;
				}
				newRow = new String[4];
				newRow[0] = row[0];
				newRow[1] = row[1];
				
				float rating = Float.valueOf(row[2]);
				
				newRow[2] = String.valueOf((long)rating);
				
				newRow[3] = row[3];
				
				newEntries.add(newRow);
			}
			exportRows(newEntries, domainFileSrc+domain+".dat");
		}
	}
	
	
	public void generateNewContextualDatasetWithCompanionTaskAndLocationAttributes(AmazonCrossDataset dataset,String booksTokenizedFile, ItemDomain domain1, 
			String televisionTokenizedFile,ItemDomain domain2, String newDatasetFile) throws TasteException{
		DataModel dm = dataset.getModel();
		
		List<String[]> newDatasetRows = new ArrayList<String[]>();

		List<String[]> bookEntries = new ArrayList<String[]>();
		List<String[]> televisionEntries = new ArrayList<String[]>();

		bookEntries = getCsvData(booksTokenizedFile);
		EntriesContextualizedReviewFile booksEntriesList = new EntriesContextualizedReviewFile(bookEntries);
		//bookEntries = booksEntriesList.sortedEntries();
		
		televisionEntries = getCsvData(televisionTokenizedFile);
		EntriesContextualizedReviewFile televisionEntriesList = new EntriesContextualizedReviewFile(televisionEntries);
		//televisionEntries = televisionEntriesList.sortedEntries();
		
		LongPrimitiveIterator users = dm.getUserIDs();
		
		while(users.hasNext()){
			long userID = users.next();
			ContextualUserPreferenceArray prefs = (ContextualUserPreferenceArray)dm.getPreferencesFromUser(userID);
			
			long locationContexts[] = getLocationContexts(userID, dataset);
			
			for (int i = 0; i < prefs.length(); i++) {
				String[] row = new String[4];
				row[0] = String.valueOf(prefs.getUserID(i));
				row[1] = String.valueOf(prefs.getItemID(i));
				row[2] = String.valueOf((int)prefs.getValue(i));
				
				long contexts[] = new long[8];
				contexts[0]	=	prefs.getContextualPreferences(i)[0];
				contexts[1] = prefs.getContextualPreferences(i)[1];
				contexts[2] = locationContexts[0];//country
				contexts[3] = locationContexts[1];//state
				contexts[4] = locationContexts[2];//city
				
//				long companionAndTaskContexts[]  = getCompanionAndTaskContexts(userID,prefs.getItemID(i),dataset,booksEntriesList,domain1,televisionEntriesList,domain2);
				long companionAndTaskContexts[] = {prefs.getContextualPreferences(i)[5],
						prefs.getContextualPreferences(i)[6],prefs.getContextualPreferences(i)[7]};
				contexts[5] = companionAndTaskContexts[0]; //alone or not-alone
				contexts[6] = companionAndTaskContexts[1]; //companion type
				contexts[7] = companionAndTaskContexts[2]; //task
				
				//contexts = addLocationContexts(contexts,prefs.getUserID(i),dataset);
				
				row[3] = Functions.codeContextsToStringForFile(contexts);
				newDatasetRows.add(row);
				
				if (newDatasetRows.size() % 20000 == 0) {
					log.info(newDatasetRows.size()
							+ " new entries with location, companion and task contextual information already"
							+ " extracted to the new contextual dataset of the thesis " + newDatasetFile);
				}
			}
		}
		exportRows(newDatasetRows, newDatasetFile);
	}
	
	private long[] getCompanionAndTaskContexts(long userID, long itemID,
			AmazonCrossDataset dataset, EntriesContextualizedReviewFile bookEntries, ItemDomain domain1,
			EntriesContextualizedReviewFile televisionEntries,ItemDomain domain2) {
		long newContexts[] = new long[3];
		
		long companionAloneOrNot = -1 , companionType = -1, task = -1;
		
		ItemDomain itemDomain = dataset.getItemInformationByID(itemID).getItemDomain();
		if(itemDomain.equals(domain1)){
			
			String tokensFound = bookEntries.getEntryTokens(userID, itemID);
			//for(String[] bookEntry : bookEntries){
				if(tokensFound != null){
					String[] tokens = tokensFound.split("\\|");
					companionType = getCompanionType(tokens);
					companionAloneOrNot = getCompanionAloneOrNot(companionType);					
					task = getTask(tokens);
					//break;
				}
			//}
		}else if(itemDomain.equals(domain2)){
			
			String tokensFound = televisionEntries.getEntryTokens(userID, itemID);
			
			//for(String[] televisionEntry : televisionEntries){
				if(tokensFound != null){
					String[] tokens = tokensFound.split("\\|");
					companionType = getCompanionType(tokens);
					companionAloneOrNot = getCompanionAloneOrNot(companionType);					
					task = getTask(tokens);
					//break;
				}
			//}
		}
		
		newContexts[0] = companionAloneOrNot;
		newContexts[1] = companionType;
		newContexts[2] = task;
		
		return newContexts;
	}

	private long getTask(String[] tokens) {
		long task = TaskContextualAttribute.UNKNOWN.getCode();
		
		for(String token : tokens){
			if(!TaskContextualAttribute.getEnum(token).equals(TaskContextualAttribute.UNKNOWN)){
				task = TaskContextualAttribute.getEnum(token).getCode();
				break;
			}
		}
		
		return task;
	}
	
	private long getCompanionType(String[] tokens) {
		long companionType = CompanionContextualAttribute.UNKNOWN.getCode();
		
		boolean containsPersonalIToken = false;
		boolean containsPersonalWEToken = false;
		
		for(String token : tokens){
			if(containsPersonalWEToken == false){
				containsPersonalWEToken = containsPersonalWEToken(token);
			}
			
			 if(containsPersonalIToken == false){
				 containsPersonalIToken = containsPersonalIToken(token);
			 }
			
			 //CompanionContextualAttribute e = CompanionContextualAttribute.getEnum(token);
			 
			 //System.out.println(token+" "+e);
			
			if(!CompanionContextualAttribute.getEnum(token).equals(CompanionContextualAttribute.UNKNOWN)){
				companionType = CompanionContextualAttribute.getEnum(token).getCode();
				break;
			}
		}
		
		if(companionType == CompanionContextualAttribute.UNKNOWN.getCode()){
			if(containsPersonalWEToken == true){
				companionType = CompanionContextualAttribute.ACCOMPANIED.getCode();
			}else if(containsPersonalIToken == true){
				companionType = CompanionContextualAttribute.ALONE.getCode();
			}
		}
		
		return companionType;
	}

	private boolean containsPersonalIToken(String token) {
		String[] pronounsI = { "i", "me", "my", "mine", "myself"};
		for(String pronoun : pronounsI){
			if(token.toLowerCase().trim().equals(pronoun) || Functions.containsToken(token, pronoun)){
				return true;
			}
		}
		return false;
	}

	private boolean containsPersonalWEToken(String token) {
		String[] pronounsWE = { "we", "our", "ours", "ourselves" };

		
		for(String pronoun : pronounsWE){
			if(token.toLowerCase().trim().equals(pronoun) || Functions.containsToken(token, pronoun)){
				return true;
			}
		}
		return false;
	}

	private long getCompanionAloneOrNot(long companionType) {
		long companionAloneOrNot = companionType;
		if(CompanionContextualAttribute.ALONE.getCode() != companionAloneOrNot
				&& CompanionContextualAttribute.ACCOMPANIED.getCode() != companionAloneOrNot
				&& CompanionContextualAttribute.UNKNOWN.getCode() != companionAloneOrNot){
			companionAloneOrNot = CompanionContextualAttribute.ACCOMPANIED.getCode();
		}
		return companionAloneOrNot;
	}

	private long[] getLocationContexts(long userID, AmazonCrossDataset dataset) {
		long newContexts[] = new long[3];
		//newContexts[0] = contexts[0];
		//newContexts[1] = contexts[1];
		
		long countryCode = -1 , stateCode = -1, cityCode = -1;
		
		String userAddressText = dataset.getUserInformationByUserID(userID).getAddress();

		AddressInformation ai = dataset.getAddressInformationByText(userAddressText);
		
		if(ai != null){
			countryCode = LocationCountryContextualAttribute.getEnum(ai.getCountry()).getCode();
			stateCode = LocationStateContextualAttribute.getEnum(ai.getState()).getCode();
			cityCode = LocationCityContextualAttribute.getEnum(ai.getCity()).getCode();
		}
		
		newContexts[0] = countryCode;
		newContexts[1] = stateCode;
		newContexts[2] = cityCode;
				
		
		return newContexts;
	}

	private void exportString(String text, String outputFile) {
		File fileOutputReviewsFile = new File(outputFile);

		FileOutputStream streamOutputReviewsFile;
		OutputStreamWriter streamWriterReviewsFile;
		BufferedWriter bwReviewsFile;
		try {
			streamOutputReviewsFile = new FileOutputStream(
					fileOutputReviewsFile);

			streamWriterReviewsFile = new OutputStreamWriter(
					streamOutputReviewsFile);

			bwReviewsFile = new BufferedWriter(streamWriterReviewsFile);

			
			bwReviewsFile.append(text);

			bwReviewsFile.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}

	}
	
	private void exportRows(List<String[]> rows, String outputFile) {
		File fileOutputReviewsFile = new File(outputFile);

		FileOutputStream streamOutputReviewsFile;
		OutputStreamWriter streamWriterReviewsFile;
		BufferedWriter bwReviewsFile;
		try {
			streamOutputReviewsFile = new FileOutputStream(
					fileOutputReviewsFile);

			streamWriterReviewsFile = new OutputStreamWriter(
					streamOutputReviewsFile);

			bwReviewsFile = new BufferedWriter(streamWriterReviewsFile);

			for (int i = 0; i < rows.size(); i++) {
				StringBuffer tmp = new StringBuffer();
				for (int index = 0; index < rows.get(i).length; index++) {
					tmp.append(rows.get(i)[index]);
					if (index + 1 < rows.get(i).length) {
						tmp.append("\t");
					}
					
				}
				bwReviewsFile.append(tmp);
				bwReviewsFile.newLine();
			}

			bwReviewsFile.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}

	}

	private void exportSpecificAndGenericReviews(
			ArrayList<EntryReviewFile> entries, String outputFile) {
		File fileOutputReviewsFile = new File(outputFile);

		FileOutputStream streamOutputReviewsFile;
		OutputStreamWriter streamWriterReviewsFile;
		BufferedWriter bwReviewsFile;
		try {
			streamOutputReviewsFile = new FileOutputStream(
					fileOutputReviewsFile);

			streamWriterReviewsFile = new OutputStreamWriter(
					streamOutputReviewsFile);

			bwReviewsFile = new BufferedWriter(streamWriterReviewsFile);

			for (EntryReviewFile erf : entries) {
				bwReviewsFile.append(erf.toString());
				bwReviewsFile.newLine();
			}

			bwReviewsFile.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void showSpecificAndGenericReviews(
			ArrayList<EntryReviewFile> specific,
			ArrayList<EntryReviewFile> generic) {
		System.out.println("Specifics:");
		for (EntryReviewFile erf : specific) {
			System.out.println(erf);
		}
		System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\nGenerics:");
		for (EntryReviewFile erf : generic) {
			System.out.println(erf);
		}
	}

	private void separateSpecificFromGenericReviews(
			ArrayList<EntryReviewFile> reviewList, TextMeasures min,
			TextMeasures max, ArrayList<EntryReviewFile> specific,
			ArrayList<EntryReviewFile> generic) {
		ClusterReviews cr = new ClusterReviews(reviewList, 2, min, max);
		cr.kMeanCluster(specific, generic);

	}

	public static ContextualExtractorFromReviews getInstance(boolean separeteSpecificfromGeneric) {
		if (instance == null) {
			instance = new ContextualExtractorFromReviews(separeteSpecificfromGeneric);
		}
		return instance;
	}

	private void readReviewsFromFile(ArrayList<EntryReviewFile> reviewList,
			String file) {

		TextMeasures minimal = new TextMeasures(10, 10, 10, 10, 10);
		TextMeasures maximal = new TextMeasures(0, 0, 0, 0, 0);

		if (file.equals(booksFile)) {
			minimal = booksReviewMinimal;
			maximal = booksReviewMaximal;
		} else if (file.equals(televisionFile)) {
			minimal = televisionReviewMinimal;
			maximal = televisionReviewMaximal;
		}

		File fileEN = new File(file);

		FileInputStream stream;
		InputStreamReader streamReader;
		BufferedReader reader;
		String line = "";

		try {
			stream = new FileInputStream(fileEN);

			streamReader = new InputStreamReader(stream);
			reader = new BufferedReader(streamReader);

			line = reader.readLine();

			while (line != null) {

				String splitedLine[] = line.split("\t");

				EntryReviewFile entry = new EntryReviewFile(splitedLine[0],
						splitedLine[1], splitedLine[2], splitedLine[3]);

				extractAndSetTextMeasure(entry);

				if (entry.getReviewTextMeasures().getLogSentences() > maximal
						.getLogSentences()) {
					maximal.setLogSentences(entry.getReviewTextMeasures()
							.getLogSentences());
				} else if (entry.getReviewTextMeasures().getLogSentences() < minimal
						.getLogSentences()) {
					minimal.setLogSentences(entry.getReviewTextMeasures()
							.getLogSentences());
				}

				if (entry.getReviewTextMeasures().getLogWords() > maximal
						.getLogWords()) {
					maximal.setLogWords(entry.getReviewTextMeasures()
							.getLogWords());
				} else if (entry.getReviewTextMeasures().getLogWords() < minimal
						.getLogWords()) {
					minimal.setLogWords(entry.getReviewTextMeasures()
							.getLogWords());
				}

				if (entry.getReviewTextMeasures().getVBDsum() > maximal
						.getVBDsum()) {
					maximal.setVBDsum(entry.getReviewTextMeasures().getVBDsum());
				} else if (entry.getReviewTextMeasures().getVBDsum() < minimal
						.getVBDsum()) {
					minimal.setVBDsum(entry.getReviewTextMeasures().getVBDsum());
				}

				if (entry.getReviewTextMeasures().getVsum() > maximal.getVsum()) {
					maximal.setVsum(entry.getReviewTextMeasures().getVsum());
				} else if (entry.getReviewTextMeasures().getVsum() < minimal
						.getVsum()) {
					minimal.setVsum(entry.getReviewTextMeasures().getVsum());
				}

				if (entry.getReviewTextMeasures().getVratio() > maximal
						.getVratio()) {
					maximal.setVratio(entry.getReviewTextMeasures().getVratio());
				} else if (entry.getReviewTextMeasures().getVratio() < minimal
						.getVratio()) {
					minimal.setVratio(entry.getReviewTextMeasures().getVratio());
				}

				reviewList.add(entry);

				// System.out.println(entry.getReviewTextMeasures().toString());

				line = reader.readLine();

				/*
				 * if(reviewList.size() == READ_N_LINES){ break; }
				 */

				if (reviewList.size() % 20000 == 0) {
					log.info(reviewList.size()
							+ " reviews already extracted from file " + file);
				}
			}

			reader.close();
			streamReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			log.error(line + " from file " + file);
			// e.printStackTrace();
		}

	}

	private void extractAndSetTextMeasure(EntryReviewFile entry) {
		PosTagger pt = PosTagger.getInstance();
		try {
			pt.setTextMeasuresFromText(entry);
		} catch (ClassNotFoundException e) {
			log.error(entry.getReviewText());
			e.printStackTrace();
		} catch (IOException e) {
			log.error(entry.getReviewText());
			e.printStackTrace();
		}
	}

	public class EntryReviewFile {
		String usedID;
		String ratingID;
		String summaryText;
		String reviewText;

		TextMeasures reviewTextMeasures;

		public EntryReviewFile(String usedID, String ratingID,
				String summaryText, String reviewText) {
			super();
			this.usedID = usedID;
			this.ratingID = ratingID;
			this.summaryText = summaryText;
			this.reviewText = reviewText;
		}

		public String getUsedID() {
			return usedID;
		}

		public String getRatingID() {
			return ratingID;
		}

		public String getSummaryText() {
			return summaryText;
		}

		public String getReviewText() {
			return reviewText;
		}

		public void setReviewTextMeasures(TextMeasures reviewTextMeasures) {
			this.reviewTextMeasures = reviewTextMeasures;
		}

		public TextMeasures getReviewTextMeasures() {
			return reviewTextMeasures;
		}

		@Override
		public String toString() {
			return usedID + "\t" + ratingID + "\t" + summaryText + "\t"
					+ reviewText + "\t" + reviewTextMeasures;
		}
	}

	private String removeNonContextualWords(String text,
			TokenizerFactory tokFactory, TfIdfDistance tfIdfdist,
			HmmDecoder decoder, HashSet<String> pronounsSet) {

		StringBuffer textWithoutNonContextualWords = new StringBuffer();

		List<String> tokenList = new ArrayList<String>();
		List<String> whiteList = new ArrayList<String>();
		Tokenizer tokenizer = tokFactory.tokenizer(text.toCharArray(), 0,
				text.length());
		tokenizer.tokenize(tokenList, whiteList);

		String[] tokens = new String[tokenList.size()];
		String[] whites = new String[whiteList.size()];
		tokenList.toArray(tokens);
		whiteList.toArray(whites);

		Tagging<String> tagging = decoder.tag(tokenList);

		/*
		 * view tags in
		 * http://alias-i.com/lingpipe-3.9.3/docs/api/com/aliasi/corpus
		 * /parsers/BrownPosParser.html
		 */
		for (int i = 0; i < tagging.size(); ++i) {
			if (tagging.tag(i).equals("nn")
					|| pronounsSet.contains(tokenList.get(i))
					|| tagging.tag(i).equals("vbg")) {
				tfIdfdist.handle(tokenList.get(i));
				textWithoutNonContextualWords.append(tokenList.get(i));
				textWithoutNonContextualWords.append("|");
				// System.out.println(tokenList.get(i)+" "+tagging.tag(i));
			}

		}
		
		int sizeText = textWithoutNonContextualWords.toString().length();
		
		if(sizeText >0){
			String textWithoutLastChar = textWithoutNonContextualWords.toString().substring(0, sizeText-1);
			return textWithoutLastChar;
		}
		
		return textWithoutNonContextualWords.toString();
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	private long[] getPreferenceContext(DataModel dm, long userID, long itemID) throws TasteException {
	    PreferenceArray prefs = dm.getPreferencesFromUser(userID);
	    if(prefs instanceof ContextualUserPreferenceArray){
	    	ContextualUserPreferenceArray cPrefs = (ContextualUserPreferenceArray) prefs;
	    	int size = prefs.length();
	 	    for (int i = 0; i < size; i++) {
	 	      if (prefs.getItemID(i) == itemID) {
	 	        return cPrefs.getContextualPreferences(i);
	 	      }
	 	    }
	    }
	   
	    return null;
	}
	
	public void exportReviewsFileWithContextualClassified(int numberOfReviewsPerContext, AbstractDataset dataset, String fileInput, String fileOutput) throws NumberFormatException, TasteException {
		
		List<String[]> entries = getCsvData(fileInput);

		List<String[]> texts = new ArrayList<String[]>();
		
		HashMap<CompanionContextualAttribute,Long> numberOfReviewsPerContextMap = new HashMap<CompanionContextualAttribute, Long>();

		//ContextualDataModel cdm = (ContextualDataModel)dataset.getModel();
		
		for (String row[] : entries) {
			
			long context[] = getPreferenceContext(dataset.getModel(),new Long(row[0]), new Long(row[1]));
			CompanionContextualAttribute c = CompanionContextualAttribute.getInstanceByCode(context[6]);
			
			if(c.equals(CompanionContextualAttribute.UNKNOWN)){
				continue;
			}
			
			if(numberOfReviewsPerContextMap.containsKey(c)){
				Long amount = numberOfReviewsPerContextMap.get(c);
				if(amount < numberOfReviewsPerContext){
					numberOfReviewsPerContextMap.put(c, amount+1);
				}else{
					continue;
				}
				
			}else{
				numberOfReviewsPerContextMap.put(c, new Long(1));
			}
			

			String[] newRow = new String[4];
			newRow[0] = row[0];
			newRow[1] = row[1];
			newRow[2] = row[3];
			newRow[3] = c.toString();

			texts.add(newRow);

			if (texts.size() % 10000 == 0) {
				log.info(texts.size()
						+ " reviews already tokenized from file " + fileInput);
			}
		}
		
		exportRows(texts, fileOutput);

	}

	public void exportTokenizedReviewsFile(String fileInput, String fileOutput,
			String topicListOutputFile) {
		File corpusFile = new File(fileInput);
		List<String[]> entries = getCsvData(fileInput);

		TokenizerFactory tokFactory = new RegExTokenizerFactory("[^\\s]+");
		// tokFactory = new IndoEuropeanTokenizerFactory(tokFactory);

		tokFactory = new LowerCaseTokenizerFactory(tokFactory);
		// tokFactory = new EnglishStopTokenizerFactory(tokFactory);
		// tokFactory = new Punc(tokFactory);
		// tokFactory = new PorterStemmerTokenizerFactory(tokFactory);

		List<String[]> texts = new ArrayList<String[]>();

		TfIdfDistance tfIdfdist = new TfIdfDistance(tokFactory);

		HmmDecoder decoder = new HmmDecoder(PosTagger.getInstance().getHmm());

		String[] pronouns = { "i", "me", "my", "mine", "we", "our", "ours",
				"myself", "ourselves" };

		HashSet<String> pronounsSet = new HashSet<String>();
		for (String s : pronouns) {
			pronounsSet.add(s);
		}
		
		final int TOKENS_ROW_INDEX = 2;

		int counterLine = 1;
		
		for (String row[] : entries) {
			counterLine++;
			if(row.length != 4){
				System.err.println(counterLine);
				continue;
			}
			String text = row[3];

			String[] newRow = new String[3];
			newRow[0] = row[0];
			newRow[1] = row[1];
			// newRow[2] = row[2];
			String tokens = removeNonContextualWords(text, tokFactory,
					tfIdfdist, decoder, pronounsSet);
			newRow[TOKENS_ROW_INDEX] = tokens;
			// newRow[4] = row[4];

			texts.add(newRow);

			if (texts.size() % 10000 == 0) {
				log.info(texts.size()
						+ " reviews already tokenized from file " + fileInput);
			}
			
		}
		
		exportRows(texts, fileOutput);

		HashMap<String, Double> termByIDF = new HashMap<String, Double>();

		for (String term : tfIdfdist.termSet()) {
			termByIDF.put(term, tfIdfdist.idf(term));
		}

		Map<String, Double> sortedTermByIDF = sortByValue(termByIDF);

		int threshold = -1;
		
		List<String> relevantTopics = new ArrayList<String>();

		for (String term : sortedTermByIDF.keySet()) {
			if (threshold == -1) {
				threshold = sortedTermByIDF.get(term).intValue();
			}

			if (sortedTermByIDF.get(term) >= threshold) {
				relevantTopics.add(term);
			} else {
				break;
			}

		}
		
		/*List<String[]> textsWithoutIrrelevantTopics = new ArrayList<String[]>();
		
		for(String[] row : texts){
			
			String[] newRow = new String[row.length];
			newRow[0] = row[0];
			newRow[1] = row[1];
			
			String[] tokens = row[TOKENS_ROW_INDEX].split("\\|");
			
			StringBuffer newTokens = new StringBuffer();
			
			for(String t : tokens){
				if(relevantTopics.contains(t)){
					newTokens.append(t);
					newTokens.append("|");
				}
			}
			
			if(newTokens.toString().length()>0){
				newRow[TOKENS_ROW_INDEX] = newTokens.toString().substring(0,  newTokens.toString().length()-1);
			}else{
				newRow[TOKENS_ROW_INDEX] = newTokens.toString();
			}
			
			
			
			textsWithoutIrrelevantTopics.add(newRow);
		}*/
		
		
		exportTopics(relevantTopics, topicListOutputFile);
		// return filteredTexts;

	}
	
	private boolean hasRow(List<String[]> rows, String[] row){
		boolean has = false;
		for(String[] r : rows){
			if(r.length != row.length){
				return false;
			}
			has = true;
			for (int i = 0; i < r.length; i++) {
				if(!r[i].equals(row[i])){
					has = false;
					break;
				}
			}
			if(has == true){
				return has;
			}
		}
		return has;
	}
	
	public HashSet<AprioriRuleItemCategory> generateItemCategoryRulesApriori(AbstractDataset dataset, boolean onlyGoodRatings, float goodRatingMin, double confidence, double support, boolean onlyBestRules) throws TasteException{

		DataModel model = dataset.getModel();
		
		LongPrimitiveIterator users = model.getUserIDs();
		
		HashMap<Long,Set<ItemCategory>> userCategoriesMap = new HashMap<Long, Set<ItemCategory>>();
		
		
		while(users.hasNext()){
			Long userId = users.next();
			
			PreferenceArray prefs = model.getPreferencesFromUser(userId);
			
			int size = prefs.length();
		    boolean isInstanceOfContextualUserPreferenceArray = prefs instanceof ContextualUserPreferenceArray;
		    
		    if(!isInstanceOfContextualUserPreferenceArray){
		    	throw new TasteException("Prefs are not ContextualUserPreferenceArray for Post-Filtering approach");
		    }
			    
			for (int i = 0; i < size; i++) {
				
				if(onlyGoodRatings && prefs.getValue(i) < goodRatingMin){
					continue;
				}
				
				Set<ItemCategory> categories = dataset.getItemInformationByID(prefs.getItemID(i)).getCategories();
				
				HashSet<ItemCategory> newCategories = new HashSet<ItemCategory>();
				
				for(ItemCategory cat : categories){
					newCategories.add(cat);
				}
				
				if(userCategoriesMap.containsKey(userId)){
					userCategoriesMap.get(userId).addAll(newCategories);
				}else{
					userCategoriesMap.put(userId, newCategories);
					
				}
				
			}
		}
		
		HashSet<AprioriRuleItemCategory> rules = new HashSet<AprioriRuleItemCategory>();
		
		HashSet<AprioriRuleItemCategory> filteredRules = new HashSet<AprioriRuleItemCategory>();
		
		int numOfTransactions = userCategoriesMap.keySet().size();
		
		for(ItemCategory ic : ItemCategory.values()){
			if(ic.getCategoryDomain().equals(dataset.getSourceDomain())){
				for(ItemCategory icMovie : ItemCategory.values()){
					if(icMovie.getCategoryDomain().equals(dataset.getTargetDomain())){
						AprioriRuleItemCategory rule = new AprioriRuleItemCategory(ic, icMovie,numOfTransactions);
						for(Long userID : userCategoriesMap.keySet()){
							if(userCategoriesMap.get(userID).contains(ic)){
								rule.setAmountOfPrecedent(rule.getAmountOfPrecedent()+1);
								if(userCategoriesMap.get(userID).contains(icMovie)){
									rule.setAmountOfConsequent(rule.getAmountOfConsequent()+1);
								}
							}
						}
						rules.add(rule);
					}
				}
			}
			
		}
		
		for(ItemCategory ic : ItemCategory.values()){
			if(ic.getCategoryDomain().equals(dataset.getTargetDomain())){
				for(ItemCategory icBook : ItemCategory.values()){
					if(icBook.getCategoryDomain().equals(dataset.getSourceDomain())){
						AprioriRuleItemCategory rule = new AprioriRuleItemCategory(ic, icBook,numOfTransactions);
						for(Long userID : userCategoriesMap.keySet()){
							if(userCategoriesMap.get(userID).contains(ic)){
								rule.setAmountOfPrecedent(rule.getAmountOfPrecedent()+1);
								if(userCategoriesMap.get(userID).contains(icBook)){
									rule.setAmountOfConsequent(rule.getAmountOfConsequent()+1);
								}
							}
						}
						rules.add(rule);
					}
				}
			}
			
		}
		
		if(onlyBestRules){
			HashMap<ItemCategory,AprioriRuleItemCategory> bestRules = new HashMap<ItemCategory, AprioriRuleItemCategory>();
			
			for(ItemCategory category : ItemCategory.values()){
				AprioriRuleItemCategory bestRule = null;
				for(AprioriRuleItemCategory rule : rules){
					if(rule.getPrecedent().equals(category)){
						if(bestRule == null){
							bestRule = rule;
						}else if(bestRule.getConfidence() + bestRule.getSupport() < rule.getConfidence() + rule.getSupport()){
							bestRule = rule;
						}
					}
				}
				bestRules.put(category, bestRule);
			}
			
			for(ItemCategory category : bestRules.keySet()){
				AprioriRuleItemCategory rule = bestRules.get(category);
				if(rule.getConfidence() > confidence && rule.getSupport() > support){
					//System.out.println(rule);
					filteredRules.add(rule);
				}
			}
		}else{
			for(AprioriRuleItemCategory rule : rules){
				if(rule.getConfidence() > confidence && rule.getSupport() > support){
					//System.out.println(rule);
					filteredRules.add(rule);
				}
			}
		}
		
		for(AprioriRuleItemCategory rule : filteredRules){
			LongPrimitiveIterator usersIds = model.getUserIDs();
			
			while(usersIds.hasNext()){
				Long userId = usersIds.next();
				
				if(userCategoriesMap.get(userId) == null 
						|| !userCategoriesMap.get(userId).contains(rule.getPrecedent())
						|| !userCategoriesMap.get(userId).contains(rule.getConsequent())){
					continue;
				}
				
				PreferenceArray prefs = model.getPreferencesFromUser(userId);
				
				int size = prefs.length();
			    boolean isInstanceOfContextualUserPreferenceArray = prefs instanceof ContextualUserPreferenceArray;
			    
			    ContextualUserPreferenceArray contextualPrefs;
			    
			    if(!isInstanceOfContextualUserPreferenceArray){
			    	throw new TasteException("Prefs are not ContextualUserPreferenceArray for Post-Filtering approach");
			    }else{
			    	contextualPrefs = (ContextualUserPreferenceArray) prefs;
			    }
				    
			    HashSet<ContextualCriteria> precedentCategoryContext = new HashSet<ContextualCriteria>();
			    HashSet<ContextualCriteria> consequentCategoryContext = new HashSet<ContextualCriteria>();
			    
				for (int i = 0; i < size; i++) {
					
					if(onlyGoodRatings && prefs.getValue(i) < goodRatingMin){
						continue;
					}
					
					Set<ItemCategory> categories = dataset.getItemInformationByID(contextualPrefs.getItemID(i)).getCategories();
					
					
					if(rule.getPrecedent().containsAtLeastOneCategory(categories)){
						precedentCategoryContext.add(new ContextualCriteria(contextualPrefs.getContextualPreferences(i)));
					}else if(rule.getConsequent().containsAtLeastOneCategory(categories)){
						consequentCategoryContext.add(new ContextualCriteria(contextualPrefs.getContextualPreferences(i)));
					}
					
				}
				
				if(!precedentCategoryContext.isEmpty() && !consequentCategoryContext.isEmpty()){
					for(ContextualCriteria precedentContext : precedentCategoryContext){
						for(ContextualCriteria consequentContext : consequentCategoryContext){
							rule.getPrecedentContexts().add(precedentContext);
							rule.getConsequentContexts().add(consequentContext);
						}
					}
				}
			}
		}
		
		return filteredRules;
		
		
		//exportString(textToFile.toString(), outputFile);
	
	
	}
	
	private void exportUserPreferences(AbstractDataset dataset, boolean onlyGoodRatings, float goodRatingMin,String outputFile) throws TasteException{

		DataModel model = dataset.getModel();
		
		LongPrimitiveIterator users = model.getUserIDs();
		
		HashMap<Long,Set<ItemCategory>> userCategoriesMap = new HashMap<Long, Set<ItemCategory>>();
		
		int countUsers = 0;
		
		while(users.hasNext()){
			countUsers++;
			Long userId = users.next();
			
			PreferenceArray prefs = model.getPreferencesFromUser(userId);
			
			int size = prefs.length();
		    boolean isInstanceOfContextualUserPreferenceArray = prefs instanceof ContextualUserPreferenceArray;
		    
		    if(!isInstanceOfContextualUserPreferenceArray){
		    	throw new TasteException("Prefs are not ContextualUserPreferenceArray for Post-Filtering approach");
		    }
			    
			for (int i = 0; i < size; i++) {
				
				if(onlyGoodRatings && prefs.getValue(i) < goodRatingMin){
					continue;
				}
				
				Set<ItemCategory> categories = dataset.getItemInformationByID(prefs.getItemID(i)).getCategories();
				
				HashSet<ItemCategory> newCategories = new HashSet<ItemCategory>();
				
				for(ItemCategory cat : categories){
					newCategories.add(cat);
				}
				
				if(userCategoriesMap.containsKey(userId)){
					userCategoriesMap.get(userId).addAll(newCategories);
				}else{
					userCategoriesMap.put(userId, newCategories);
					
				}
				
			}
		}
		
		System.out.println(countUsers);
		
		StringBuffer textToFile = new StringBuffer();
		
		textToFile.append("@relation 'genreRulesInference'");
		textToFile.append("\n");
		textToFile.append("\n");
		
		for(ItemCategory ic : ItemCategory.values()){
			textToFile.append("@attribute ");
			textToFile.append(ic);
			textToFile.append(" {y,n}");
			textToFile.append("\n");
		}
		
		textToFile.append("\n");
		textToFile.append("@data");
		textToFile.append("\n");
		
		for(Long userID : userCategoriesMap.keySet()){
			for(ItemCategory ic : ItemCategory.values()){
				if(userCategoriesMap.get(userID).contains(ic)){
					textToFile.append("y");
					textToFile.append(",");
				}else{
					textToFile.append("?");
					textToFile.append(",");
				}
			}
			textToFile = new StringBuffer(textToFile.substring(0, textToFile.length()-1));
			textToFile.append("\n");
		}
		
		exportString(textToFile.toString(), outputFile);
	
	
	}

	private void generateGenreRulesInformationFile(AbstractDataset dataset, String outputFile) throws TasteException{
		
		//HashSet<String> contexts = new HashSet<String>();
		
		DataModel dm = dataset.getModel();
		
		LongPrimitiveIterator users = dm.getUserIDs();
		
		List<String[]> rows = new ArrayList<String[]>();
		
		while(users.hasNext()){
			long userID = users.next();
			ContextualUserPreferenceArray prefs = (ContextualUserPreferenceArray)dm.getPreferencesFromUser(userID);
			
			String[] row;
			
			for (int i = 0; i < prefs.length(); i++) {
				
				StringBuffer contextText = new StringBuffer();
				long context[] = prefs.getContextualPreferences(i);
				for(int index = 0;index<context.length;index++){
					contextText.append(context[index]);
					contextText.append("|");
				}
				
				row = new String[6];
				
				//TODO: registar codigo ao inves de nome
				row[0] = dataset.getItemInformationByID(prefs.getItemID(i)).getItemDomain().name();
				row[1] = contextText.substring(0, contextText.length()-1);
				
				StringBuffer categoriesText = new StringBuffer();
				//TODO: gerar uma categoria por row apenas
				for(ItemCategory category : dataset.getItemInformationByID(prefs.getItemID(i)).getCategories()){
					categoriesText.append(category.name());
					categoriesText.append("|");
				}
				
				row[2] = categoriesText.substring(0, categoriesText.length()-1);
				
				row[3] = row[0].equals(ItemDomain.BOOK.name())? ItemDomain.MOVIE.name() : ItemDomain.BOOK.name();
				row[4] = row[1];
				row[5] = row[2];
				
				if(!hasRow(rows,row)){
					rows.add(row);
				}
			}
			
		}
		
		exportRows(rows, outputFile);
	}
	
	private void convertToNewGenreRulesInformationFileFormat(String oldFile, String outputFile) throws TasteException{
		
		//HashSet<String> contexts = new HashSet<String>();
		
		
		
		List<String[]> rows = new ArrayList<String[]>();
		
		List<String[]> oldRows = getCsvData(oldFile);
		
		for (int i = 0; i < oldRows.size(); i++) {
			
			String[] oldRow = oldRows.get(i);
			
			if(!oldRow[2].contains("|") && !oldRow[5].contains("|")){
				String[] row = new String[6];
				
				row[0] = String.valueOf(ItemDomain.valueOf(oldRow[0]).getCode());
				row[1] = oldRow[1];
				
				row[2] = String.valueOf(ItemCategory.valueOf(oldRow[2]+"_"+ItemDomain.valueOf(oldRow[0])).getCode());
				
				row[3] = String.valueOf(ItemDomain.valueOf(oldRow[3]).getCode());
				row[4] = oldRow[4];
				row[5] = String.valueOf(ItemCategory.valueOf(oldRow[5]+"_"+ItemDomain.valueOf(oldRow[3])).getCode());
				
//				if(!hasRow(rows,row)){
					rows.add(row);
					if (rows.size() % 10000 == 0) {
						log.info(rows.size()
								+ " rules already extracted from file " + oldFile);
					}
//				}
			}else{
				String[] categories = oldRow[2].split("\\|");
				for(String category : categories){
					String[] row = new String[6];
					
					row[0] = String.valueOf(ItemDomain.valueOf(oldRow[0]).getCode());
					row[1] = oldRow[1];
					
					row[2] = String.valueOf(ItemCategory.valueOf(category+"_"+ItemDomain.valueOf(oldRow[0])).getCode());
					
					row[3] = String.valueOf(ItemDomain.valueOf(oldRow[3]).getCode());
					row[4] = oldRow[4];
					row[5] = String.valueOf(ItemCategory.valueOf(category+"_"+ItemDomain.valueOf(oldRow[3])).getCode());
					
//					if(!hasRow(rows,row)){
						rows.add(row);
						if (rows.size() % 10000 == 0) {
							log.info(rows.size()
									+ " rules already extracted from file " + oldFile);
						}
//					}
				}
			}
		}
			
		
		exportRows(rows, outputFile);
	}
	
	private List<String[]> getCsvData(String file) {

		List<String[]> rows = new ArrayList<String[]>();

		File fileEN = new File(file);

		FileInputStream stream;
		InputStreamReader streamReader;
		BufferedReader reader;

		String line;

		try {
			stream = new FileInputStream(fileEN);

			streamReader = new InputStreamReader(stream);
			reader = new BufferedReader(streamReader);

			line = reader.readLine();

			String[] row;

			while (line != null) {

				row = line.split("\t");

				rows.add(row);

				line = reader.readLine();
			}

			reader.close();
			streamReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return rows;
	}

	public class EntriesContextualizedReviewFile {
		
		private class RatingTokens{
			private Long rating;
			private String tokens;
			
			public RatingTokens(Long rating) {
				this.rating = rating;
			}
			
			public RatingTokens(Long rating, String tokens) {
				this.rating = rating;
				this.tokens = tokens;
			}
			
			public Long getRating() {
				return rating;
			}
			
			public String getTokens() {
				return tokens;
			}
		}
		
		private class UserRating{
			private Long userID;
			private List<RatingTokens> ratings;
			
			public UserRating(Long userID) {
				this.userID = userID;
				this.ratings = new ArrayList<ContextualExtractorFromReviews.EntriesContextualizedReviewFile.RatingTokens>();
			}
			
			public Long getUserID() {
				return userID;
			}
			
			public List<RatingTokens> getRatings() {
				return ratings;
			}
			
			/*public void setRatings(List<RatingTokens> ratings) {
				this.ratings = ratings;
			}*/
		}
		
		private List<UserRating> entries;
		
		public String getEntryTokens(Long userID, Long itemID){
			
			UserRating ur = new UserRating(userID);
			//ur.setId(userID); // Essa pessoa será usada como critério de comparação para
							// a busca binária
	
			
			
			int ResultIndex = Collections.binarySearch(this.entries, ur, new EntryComparatorUserID()); // Busca
																		// Binária
																		// com o
																		// objeto
																		// comparador
			if (ResultIndex > -1) {
				UserRating urFound = this.entries.get(ResultIndex);
				
				RatingTokens rt = new RatingTokens(itemID);
				
				int ResultIndexItem = Collections.binarySearch(urFound.getRatings(), rt, new RatingComparatorItemID()); // Busca
				if(ResultIndexItem > -1){
					RatingTokens rtFound = urFound.getRatings().get(ResultIndexItem);
					return rtFound.getTokens();
				}
			}
			return null;
		}
		
		public List<String[]> sortedEntries(){
			List<String[]> sortedEntries = new ArrayList<String[]>();
			String[] entry;
			for(UserRating ur : this.entries){
				for(RatingTokens rt : ur.getRatings()){
					entry = new String[3];
					entry[0] = String.valueOf(ur.getUserID());
					entry[1] = String.valueOf(rt.getRating());
					entry[2] = rt.getTokens();
					sortedEntries.add(entry);
				}
			}
			return sortedEntries;
		}

		public EntriesContextualizedReviewFile(List<String[]> rawEntries) {
			this.entries = new ArrayList<UserRating>();

			for(String[] entry : rawEntries){
				
				Long userID = Long.valueOf(entry[0]);
				Long itemID = Long.valueOf(entry[1]);
				String tokens = "";
				
				if(entry.length == 3){
					tokens = entry[2];
				}
				
				UserRating ur = getEntryByUserID(userID);
				if(ur == null){
					RatingTokens rt = new RatingTokens(itemID, tokens);
					UserRating urTemp = new UserRating(userID);
					urTemp.getRatings().add(rt);
					this.entries.add(urTemp);
				}else{
					RatingTokens rt = new RatingTokens(itemID, tokens);
					ur.getRatings().add(rt);
					//this.entries.add(ur);
				}
			}
			
			Collections.sort(this.entries, new EntryComparatorUserID());
			for(UserRating ur : this.entries){
				Collections.sort(ur.getRatings(),new RatingComparatorItemID());
			}
		}
		
		private UserRating getEntryByUserID(Long userID){
			for(UserRating ur : this.entries){
				if(ur.getUserID().equals(userID)){
					return ur;
				}
			}
			return null;
		}
		
		private class EntryComparatorUserID implements Comparator<UserRating> {

			@Override
			public int compare(UserRating o1, UserRating o2) {
				return o1.getUserID().compareTo(o2.getUserID());
			}

		}
		
		private class RatingComparatorItemID implements Comparator<RatingTokens> {

			@Override
			public int compare(RatingTokens o1, RatingTokens o2) {
				return o1.getRating().compareTo(o2.getRating());
			}

		}
	}

	public static void main(String[] args) {

		AmazonCrossDataset dataset = AmazonCrossDataset.getInstance(1.0,ItemDomain.BOOK,ItemDomain.MUSIC,true); //cross domain
		
		DataModel dm = dataset.getModel();
		
		
		
		ContextualExtractorFromReviews cefr = ContextualExtractorFromReviews
				.getInstance(false);
		
		//
		//cefr.exportTokenizedReviewsFile(ContextualExtractorFromReviews.televisionFile, ContextualExtractorFromReviews.televisionContextualTokensFile, ContextualExtractorFromReviews.televisionReviewsContextualTopics);
//		cefr.exportTokenizedReviewsFile(ContextualExtractorFromReviews.musicFile, ContextualExtractorFromReviews.musicContextualTokensFile, ContextualExtractorFromReviews.musicReviewsContextualTopics);
//		cefr.exportTokenizedReviewsFile(ContextualExtractorFromReviews.booksFile, ContextualExtractorFromReviews.booksContextualTokensFile, ContextualExtractorFromReviews.booksReviewsContextualTopics);
		// cefr.showSpecificAndGenericReviews(specific, generic);
		
//		String[] files = {PATH_AMAZON_META+"contextual-ratings10overlapTargetMUSIC.dat",
//				PATH_AMAZON_META+"contextual-ratings25overlapTargetMUSIC.dat",
//				PATH_AMAZON_META+"contextual-ratings50overlapTargetMUSIC.dat",
//				PATH_AMAZON_META+"contextual-ratings75overlapTargetMUSIC.dat",
//				PATH_AMAZON_META+"contextual-ratings10overlapTargetBOOK.dat",
//				PATH_AMAZON_META+"contextual-ratings25overlapTargetBOOK.dat",
//				PATH_AMAZON_META+"contextual-ratings50overlapTargetBOOK.dat",
//				PATH_AMAZON_META+"contextual-ratings75overlapTargetBOOK.dat",
//				};
//		HashSet<ItemDomain> domains = new HashSet<ItemDomain>();
//		domains.add(ItemDomain.MUSIC);
//		domains.add(ItemDomain.BOOK);
		
//		cefr.exportDatasetPerDomain(dataset, domains, PATH_AMAZON_META+"contextual-ratings-full-new-thesis-", ContextualExtractorFromReviews.newDatasetWithContext);
		try {
//			cefr.generateGenreRulesInformationFile(dataset, datasetGenreRulesInformationURL);
//			cefr.convertToNewGenreRulesInformationFileFormat(datasetGenreRulesInformationURL, "C:\\Users\\Douglas\\Desktop\\Cross_Domain_Tools\\datasets\\amazon\\amazon-meta.txt\\genreRulesInformationNEW.dat");
//			cefr.generateNewContextualDatasetWithCompanionTaskAndLocationAttributes(dataset, booksContextualTokensFile, televisionContextualTokensFile, newDatasetWithContext);
//			cefr.generateNewContextualDatasetWithCompanionTaskAndLocationAttributes(dataset, booksContextualTokensFile,ItemDomain.BOOK, musicContextualTokensFile,ItemDomain.MUSIC, newDatasetWithContext);
//			cefr.convertOverlapedDatasetFilesIntoFullContexutalOverlapFiles(files, newDatasetWithContext);
//			cefr.exportUserPreferences(dataset, true, 4.0f, "C:\\Users\\Douglas\\Desktop\\Cross_Domain_Tools\\datasets\\amazon\\amazon-meta.txt\\userCategoryPrefs.arff");
//			cefr.generateItemCategoryRulesApriori(dataset, true, 4.0f,-1,-1,true);
			cefr.exportReviewsFileWithContextualClassified(50, dataset, ContextualExtractorFromReviews.booksFile, ContextualExtractorFromReviews.booksContextualTokensFile);
//			cefr.exportReviewsFileWithContextualClassified(50, dataset, ContextualExtractorFromReviews.televisionFile, ContextualExtractorFromReviews.televisionContextualTokensFile);
			cefr.exportReviewsFileWithContextualClassified(50, dataset, ContextualExtractorFromReviews.musicFile, ContextualExtractorFromReviews.musicContextualTokensFile);
			
			/*LongPrimitiveIterator it = dm.getUserIDs();
			
			HashSet<String> ukCities = new HashSet<String>();
			HashSet<String> ukCountries = new HashSet<String>();
			HashSet<String> ukStates = new HashSet<String>();
			
			while(it.hasNext()){
				long userID = it.nextLong();
				String address = dataset.getUserInformationByUserID(userID).getAddress();
				
				AddressInformation ai = dataset.getAddressInformationByText(address);
				
				if(ai == null){
					continue;
				}
				
				String country = ai.getCountry();
				String state = ai.getState();
				String city = ai.getCity();
				
				if(country.length() > 1
						&& LocationCountryContextualAttribute.getEnum(country).equals(LocationCountryContextualAttribute.UNKNOWN)){
					//System.out.println(country + " unknown country for user "+userID+", address: "+address);
					ukCountries.add(country);
				}
				
				if(state.length() > 1
						&&LocationStateContextualAttribute.getEnum(state).equals(LocationStateContextualAttribute.UNKNOWN)){
					//System.out.println(state + " unknown state for user "+userID+", address: "+address);
					ukStates.add(state);
				}
				
				if(city.length() > 1
						&&LocationCityContextualAttribute.getEnum(city).equals(LocationCityContextualAttribute.UNKNOWN)){
					//System.out.println(city + " unknown city for user "+userID+", address: "+address);
					ukCities.add(city);
				}
			}
			int indexCountry = 106;
			System.out.println("\nCountries:");
			for(String country : ukCountries){
				System.out.print(country.toUpperCase()+"("+indexCountry+"),");
				indexCountry++;
			}
			
			int indexState = 311;
			System.out.println("\nStates:");
			for(String state : ukStates){
				System.out.print(state.toUpperCase().replaceAll("\\s", "_").replaceAll("\\-", "_").replaceAll("\\'", "_")+"("+indexState+"),");
				indexState++;
			}
			
			int indexCity = 2262;
			System.out.println("\nCities:");
			for(String city : ukCities){
				System.out.print(city.toUpperCase().replaceAll("\\s", "_").replaceAll("\\-", "_").replaceAll("\\'", "_")+"("+indexCity+"),");
				indexCity++;
			}*/
		} catch (TasteException e) {
			e.printStackTrace();
		}
		
		System.out.println("FINISHED");
		// cefr.readReviewsFromFile(reviewList, file);

	}
}
