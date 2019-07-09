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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.RandomUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.google.gwt.thirdparty.guava.common.util.concurrent.SimpleTimeLimiter;
import com.google.gwt.thirdparty.guava.common.util.concurrent.TimeLimiter;

import br.cin.tbookmarks.recommender.database.contextual.AprioriRuleItemCategory;
import br.cin.tbookmarks.recommender.database.contextual.ContextualDataModel;
import br.cin.tbookmarks.recommender.database.contextual.ContextualExtractorFromReviews;
import br.cin.tbookmarks.recommender.database.contextual.ContextualFileDataModel;
import br.cin.tbookmarks.recommender.database.contextual.ContextualUserPreferenceArray;
import br.cin.tbookmarks.recommender.database.item.ItemCategory;
import br.cin.tbookmarks.recommender.database.item.ItemDatasetInformation;
import br.cin.tbookmarks.recommender.database.item.ItemDomain;
import br.cin.tbookmarks.recommender.database.item.ItemInformation;
import br.cin.tbookmarks.recommender.database.item.ItemInformationComparatorID;
import br.cin.tbookmarks.recommender.database.user.AddressDatasetInformation;
import br.cin.tbookmarks.recommender.database.user.AddressInformation;
import br.cin.tbookmarks.recommender.database.user.AddressInformationComparatorName;
import br.cin.tbookmarks.recommender.database.user.UserDatasetInformation;
import br.cin.tbookmarks.recommender.database.user.UserInformation;
import br.cin.tbookmarks.recommender.database.user.UserInformationComparatorID;
import br.cin.tbookmarks.util.ContextualFileGenerator;
import br.cin.tbookmarks.util.Functions;

public final class AmazonCrossDataset extends AbstractDataset {

	private static final Logger log = LoggerFactory.getLogger(AmazonCrossDataset.class);

	private static AmazonCrossDataset INSTANCE;
	private static boolean generateNewFiles;
	private static boolean isSingleDomain;
	private static ItemDomain singleDomain;

	private static Random random;

	public static String folderResources = System.getProperty("user.dir") + "/war/WEB-INF/resources";
	//private static String folder = folderResources + "/datasets/Books_MOVIE/";
	private static String folder = folderResources + "/datasets/Books_MUSIC/";
	private static final String pathPattern = "//";

	//GAE
//	 public static String folderResources = "WEB-INF/resources"; //GAE
//	 private static String folder = folderResources+"/datasets/Books_MOVIE/";
//	 private static final String pathPattern = "/";
	// //GAE

	private String datasetURLOriginal = "C:/Users/Douglas/Desktop/Cross_Domain_Tools/datasets/amazon/amazon-meta.txt/amazon-meta.txt.001";
	//private String datasetInformationURL = folder + "full-ratings-information.zip";
	private static String datasetInformationURL = folder + "full-ratings-information.dat";
	private static String datasetUserIDsMap = folder + "userIDsMap.dat";
	private static String datasetAddressesInformationURL = folder + "addressesInformation.dat";
	private static String datasetGenreRulesInformationURL = folder + "genreRulesInformation.dat";
	private static String datasetInformationDelimiter = ";";
	private static String timestampFormat = "yyyy-MM-dd";

	/*static {
		datasetURL = folder + "contextual-ratings-full-new-thesis.zip";
		datasetURLOverlap = folder + "contextual-ratings.zip";
	}*/
	static {
		datasetURL = folder + "contextual-ratings50overlapTargetMUSIC.dat";
		datasetURLOverlap = folder /*+ "contextual-ratings"*/;
	}

	@Override
	protected void initializeDataModel(String src) throws IOException {

		
		
		if (src.contains(".zip")) {
			
			ZipFile zip = new ZipFile(src);
			
			String splitPathFile[] = src.split(pathPattern);
			
			String fileName = splitPathFile[splitPathFile.length-1].replace("zip", "dat");
			
			ZipEntry entry = zip.getEntry(fileName);
			
			model = new ContextualFileDataModel(entry,zip);

		} else {
			model = new ContextualFileDataModel(new File(src));
		}
	}

	/*
	 * private AmazonCrossDataset() { try {
	 * convertDatasetFileToDefaultPattern(null); initializeDataModel();
	 * initializeDBInfo(); } catch (IOException e) { e.printStackTrace(); } catch
	 * (NumberFormatException e) { // ; }
	 * 
	 * }
	 */

	private AmazonCrossDataset(HashSet<ItemDomain> domains, int minRatingsPerUser, boolean onlyOverlap, String url) {
		random = RandomUtils.getRandom();
		try {

			convertDatasetFileToDefaultPattern(domains, minRatingsPerUser, onlyOverlap);

			if (url == null) {
				url = datasetURL;
			}

			
			initializeDataModel(url);
			if (datasetInformationURL.contains(".zip")) {
				initializeDBItemInfoZipFile();
			}else {
				initializeDBItemInfo();
			}
			initializeDBUserInfo();
			initializeDBAddressInfo();
			initializeDMRules();
			// if(isSingleDomain && singleDomain != null){
			// filterDataModelSingleDomain();
			// }

		} catch (IOException e) {

			e.printStackTrace();
		} catch (NumberFormatException e) {

			e.printStackTrace();
		} catch (TasteException e) {

			e.printStackTrace();
		}

	}

	/*
	 * private void initializeDMRules() throws IOException {
	 * 
	 * 
	 * genreRulesDatasetInformation = new GenreRulesInformation();
	 * 
	 * int countGenreRulesInformation = 0;
	 * 
	 * //for(int splitedFilesNumber = 1; splitedFilesNumber <= 1;
	 * splitedFilesNumber++){ //File fileEN = new
	 * File(datasetInformationURL+splitedFilesNumber+".dat");
	 * 
	 * File fileEN = new File(datasetGenreRulesInformationURL);
	 * 
	 * FileInputStream stream;
	 * 
	 * stream = new FileInputStream(fileEN);
	 * 
	 * InputStreamReader streamReader = new InputStreamReader(stream);
	 * BufferedReader reader = new BufferedReader(streamReader);
	 * 
	 * String line;
	 * 
	 * 
	 * GenreRulesInformation gerGenreRulesInformation;
	 * 
	 * line = reader.readLine();
	 * 
	 * while (line != null) {
	 * 
	 * String row[] = line.split("/t");
	 * 
	 * int numOfColumns = 6;
	 * 
	 * if (row.length != numOfColumns) { log.error("Line " + line +
	 * " have more or less than " +numOfColumns+" terms"); }
	 * 
	 * int index = 0;
	 * 
	 * //ItemDomain conditionItemDomain = ItemDomain.valueOf(row[index++]);
	 * ItemDomain conditionItemDomain = ItemDomain.getEnumByCode(new
	 * Integer(row[index++]));
	 * 
	 * String splittedValue[] = row[index++].split("/|");
	 * 
	 * long contextualAttributesCondition[] = new long[splittedValue.length];
	 * for(int i=0; i < splittedValue.length; i++){ contextualAttributesCondition[i]
	 * = new Long(splittedValue[i]); }
	 * 
	 * ContextualCriteria conditionContext = new
	 * ContextualCriteria(contextualAttributesCondition);
	 * 
	 * //ItemCategory conditionItemCategory =
	 * ItemCategory.valueOf(row[index++]+"_"+conditionItemDomain); ItemCategory
	 * conditionItemCategory = ItemCategory.getEnumByCode(new
	 * Integer(row[index++]));
	 * 
	 * RuleTuple condition = new RuleTuple(conditionItemDomain, conditionContext,
	 * conditionItemCategory);
	 * 
	 * //ItemDomain inferenceItemDomain = ItemDomain.valueOf(row[index++]);
	 * ItemDomain inferenceItemDomain = ItemDomain.getEnumByCode(new
	 * Integer(row[index++]));
	 * 
	 * String splittedValueInference[] = row[index++].split("/|");
	 * 
	 * long contextualAttributesInference[] = new
	 * long[splittedValueInference.length]; for(int i=0; i <
	 * splittedValueInference.length; i++){ contextualAttributesInference[i] = new
	 * Long(splittedValueInference[i]); }
	 * 
	 * ContextualCriteria inferenceContext = new
	 * ContextualCriteria(contextualAttributesInference);
	 * 
	 * //ItemCategory inferenceItemCategory =
	 * ItemCategory.valueOf(row[index++]+"_"+inferenceItemDomain); ItemCategory
	 * inferenceItemCategory = ItemCategory.getEnumByCode(new
	 * Integer(row[index++]));
	 * 
	 * RuleTuple inference = new RuleTuple(inferenceItemDomain, inferenceContext,
	 * inferenceItemCategory);
	 * 
	 * if(!genreRulesDatasetInformation.getConditionInferredMap().containsKey(
	 * condition)){ HashSet<RuleTuple> ruleTupleInferred = new HashSet<RuleTuple>();
	 * ruleTupleInferred.add(inference);
	 * genreRulesDatasetInformation.getConditionInferredMap().put(condition,
	 * ruleTupleInferred); countGenreRulesInformation++;
	 * if(countGenreRulesInformation % 100000 == 0){
	 * log.warn(countGenreRulesInformation+" rules read from file"); } }else{
	 * HashSet<RuleTuple> ruleTupleInferred =
	 * genreRulesDatasetInformation.getConditionInferredMap().get(condition);
	 * if(!ruleTupleInferred.contains(inference)){ ruleTupleInferred.add(inference);
	 * }else{
	 * log.warn("DUPLICATED RULE IN GENRE INFERRENCE: "+inference+" line: "+line); }
	 * 
	 * }
	 * 
	 * 
	 * line = reader.readLine(); } reader.close(); streamReader.close(); //}
	 * 
	 * //TODO: sort if(countGenreRulesInformation > 0){
	 * Collections.sort(genreRulesDatasetInformation.getRules(), new
	 * AddressInformationComparatorName());
	 * 
	 * }
	 * 
	 * log.info("Num of genre rules information: "+countGenreRulesInformation);
	 * 
	 * }
	 */

	private HashSet<AprioriRuleItemCategory> getRuleByPrecedent(HashSet<AprioriRuleItemCategory> rules,
			ItemCategory precedent) {
		HashSet<AprioriRuleItemCategory> categoryPrecedenceRules = new HashSet<AprioriRuleItemCategory>();
		for (AprioriRuleItemCategory rule : rules) {
			if (rule.getPrecedent().equals(precedent)) {
				categoryPrecedenceRules.add(rule);
			}
		}
		return categoryPrecedenceRules;
	}

	private void initializeDMRules() throws IOException, TasteException {

		ContextualExtractorFromReviews cefr = ContextualExtractorFromReviews.getInstance(false);

		genreRulesDatasetInformation = cefr.generateItemCategoryRulesApriori(this, true, 4.0f, confidenceLevelDMRules,
				supportLevelDMRules, false);

		/*
		 * File fileOutput = new File(datasetGenreRulesInformationURL);
		 * 
		 * FileOutputStream streamOutput = new FileOutputStream(fileOutput);
		 * 
		 * OutputStreamWriter streamWriter = new OutputStreamWriter( streamOutput);
		 * 
		 * BufferedWriter bw = new BufferedWriter(streamWriter);
		 * for(AprioriRuleItemCategory rule : genreRulesDatasetInformation){
		 * bw.append(rule.toString()); bw.append("\n"); }
		 * 
		 * bw.close(); log.info("File "+fileOutput.getName()+" exported!");
		 * 
		 * log.info("Num of genre rules information: "+genreRulesDatasetInformation.size
		 * ());
		 */

	}

	public static AmazonCrossDataset getInstance() {
		if (INSTANCE == null) {
			generateNewFiles = false;
			return new AmazonCrossDataset(null, 0, false, null);
		}
		return INSTANCE;
	}

	public static AmazonCrossDataset getInstance(double userOverlapLevel, ItemDomain sd, ItemDomain td,
			boolean useOverlapedFile) {

		sourceDomain = sd;
		targetDomain = td;
		
		
		Preconditions.checkArgument(userOverlapLevel > 0.0, "userOverlapLevel must be positive (max 1.0)");

		if (INSTANCE == null) {
			generateNewFiles = false;

			AmazonCrossDataset acd;

			if (userOverlapLevel == 1.0) {
				acd = new AmazonCrossDataset(null, 0, false, null);
				return acd;
			}

			String ext = "/.zip";
			String[] splitExt = datasetURLOverlap.split(ext);
			splitExt = splitExt[0].split("/.dat");
			String overlapedFile = splitExt[0] + new Integer((int) (userOverlapLevel * 100)).toString()
					+ "overlapTarget" + targetDomain + ".dat";
			

			if (useOverlapedFile == true) {
				acd = new AmazonCrossDataset(null, 0, false, overlapedFile);
			} else {
				acd = new AmazonCrossDataset(null, 0, false, null);

				if (userOverlapLevel < 1.0) {
					removeRatingsInTheTarget(acd, userOverlapLevel, targetDomain);
				}
				exportOverlapedFile(acd, overlapedFile);
			}

			return acd;
		}
		return INSTANCE;
	}

	private static void exportOverlapedFile(AmazonCrossDataset acd, String overlapedFile) {

		File fileOutput = new File(overlapedFile);

		if (!fileOutput.exists()) {

			try {

				FileOutputStream streamOutput = new FileOutputStream(fileOutput);

				OutputStreamWriter streamWriter = new OutputStreamWriter(streamOutput);

				BufferedWriter bw = new BufferedWriter(streamWriter);

				DataModel dm = acd.getModel();

				LongPrimitiveIterator userIterator;
				try {
					userIterator = dm.getUserIDs();

					while (userIterator.hasNext()) {

						long userId = userIterator.nextLong();

						ContextualUserPreferenceArray prefs = (ContextualUserPreferenceArray) dm
								.getPreferencesFromUser(userId);

						for (int i = 0; i < prefs.length(); i++) {

							int index = 0;
							bw.append(userId + "\t" + prefs.getItemID(i) + "\t"
									+ Float.valueOf(prefs.getValue(i)).intValue() + "\t"
									+ prefs.getContextualPreferences(i)[index++] + "|"
									+ prefs.getContextualPreferences(i)[index++] + "|"
									+ prefs.getContextualPreferences(i)[index++] + "|"
									+ prefs.getContextualPreferences(i)[index++] + "|"
									+ prefs.getContextualPreferences(i)[index++] + "|"
									+ prefs.getContextualPreferences(i)[index++] + "|"
									+ prefs.getContextualPreferences(i)[index++] + "|"
									+ prefs.getContextualPreferences(i)[index++] + "\n");
						}

					}
				} catch (TasteException e) {

					e.printStackTrace();
				}

				bw.close();
				log.info("File " + fileOutput.getName() + " exported!");

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

	}

	private static void removeRatingsInTheTarget(AmazonCrossDataset acd, double userOverlap, ItemDomain targetDomain) {
		DataModel dm = acd.getModel();

		try {

			FastByIDMap<PreferenceArray> newUsers = new FastByIDMap<PreferenceArray>(dm.getNumUsers());

			LongPrimitiveIterator usersIterator = dm.getUserIDs();
			while (usersIterator.hasNext()) {

				long userId = usersIterator.nextLong();

				PreferenceArray prefs2Array = dm.getPreferencesFromUser(userId);

				if (random.nextDouble() <= userOverlap) {
					newUsers.put(userId, prefs2Array);
					continue;
				}

				List<Preference> prefs2 = Lists.newArrayListWithCapacity(prefs2Array.length());
				for (Preference pref : prefs2Array) {
					prefs2.add(pref);
				}
				for (Iterator<Preference> iterator = prefs2.iterator(); iterator.hasNext();) {
					Preference pref = iterator.next();
					if (acd.getItemInformationByID(pref.getItemID()).getItemDomain().equals(targetDomain)) {
						iterator.remove();
					}
				}
				if (!prefs2.isEmpty()) {
					newUsers.put(userId, new ContextualUserPreferenceArray(prefs2));
				}

			}
			acd.model = new ContextualDataModel(newUsers);
		} catch (TasteException e) {
			e.printStackTrace();
		}

	}

	public static AmazonCrossDataset getInstance(boolean getSingleDomain, ItemDomain domain, String folderURL, String nameFile) {
		if (INSTANCE == null) {
			
			folder = folderResources + folderURL;
			datasetInformationURL = folder + "full-ratings-information.dat";
			datasetUserIDsMap = folder + "userIDsMap.dat";
			datasetAddressesInformationURL = folder + "addressesInformation.dat";
			datasetGenreRulesInformationURL = folder + "genreRulesInformation.dat";
			
			datasetURL = folder ;
			datasetURLOverlap = folder ;
			
			generateNewFiles = false;
			isSingleDomain = getSingleDomain;
			singleDomain = domain;
			
			datasetURL = datasetURL + nameFile ;
			return new AmazonCrossDataset(null, 0, false, null);
		}
		return INSTANCE;
	}

	
	public static AmazonCrossDataset getInstance(HashSet<ItemDomain> domains, int minRatingsPerUser,
			boolean onlyOverlap) {
		if (INSTANCE == null) {
			generateNewFiles = true;
			return new AmazonCrossDataset(domains, minRatingsPerUser, onlyOverlap, null);
		}
		return INSTANCE;
	}

	private long getDifference(String text) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(this.timestampFormat);
		Date d = sdf.parse(text);

		return d.getTime();
	}

	private class EntryDBItemInformation {
		private String itemId;
		private String amazonItemID;
		private String title;
		private String categories;
		private String domain;

		public EntryDBItemInformation(String itemId, String amazonItemID, String title, String categories,
				String domain) {

			this.itemId = itemId;
			this.amazonItemID = amazonItemID;
			this.title = title;
			this.categories = categories;
			this.domain = domain;
		}

		public String getItemId() {
			return itemId;
		}

		public String getTitle() {
			return title;
		}

		public String getCategories() {
			return categories;
		}

		public String getDomain() {
			return domain;
		}

		public String getAmazonItemID() {
			return amazonItemID;
		}

		@Override
		public boolean equals(Object o) {
			EntryDBItemInformation entry;
			if (o instanceof EntryDBItemInformation) {
				entry = (EntryDBItemInformation) o;
				return this.itemId.equals(entry.itemId);
			}
			return false;
		}

	}

	private class EntryRatingContextualFile {

		private long userText;
		private String amazonUserID;
		private String itemId;
		private String amazonItemID;
		private String rating;
		private long dayType;
		private long periodOfDay;
		// private long countryContextual;
		private String domain;

		public EntryRatingContextualFile(long userText, String amazonID, String itemId, String amazonItemID,
				String rating, long dayType, long periodOfDay, String domain) {

			this.userText = userText;
			this.amazonUserID = amazonID;
			this.itemId = itemId;
			this.amazonItemID = amazonItemID;
			this.rating = rating;
			this.dayType = dayType;
			this.periodOfDay = periodOfDay;
			this.domain = domain;
		}

		public long getUserText() {
			return userText;
		}

		public String getItemId() {
			return itemId;
		}

		public String getRating() {
			return rating;
		}

		public long getDayType() {
			return dayType;
		}

		public long getPeriodOfDay() {
			return periodOfDay;
		}

		public String getDomain() {
			return domain;
		}

		public String getAmazonUserID() {
			return amazonUserID;
		}

		/*
		 * public String getAmazonItemID() { return amazonItemID; }
		 */

	}

	private class EntryRatingContextualFileComparator implements Comparator<EntryRatingContextualFile> {
		@Override
		public int compare(EntryRatingContextualFile p1, EntryRatingContextualFile p2) {
			if (p1.getUserText() < p2.getUserText()) {
				return -1;
			} else if (p1.getUserText() > p2.getUserText()) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public void filterDataModelSingleDomain(ItemDomain targetDomain) throws TasteException {

		DataModel model = this.model;
		singleDomain = targetDomain;

		// criar um novo datamodel verificando cada preferencia e adicionando no novo
		// datamodel caso case com o domain

		FastByIDMap<PreferenceArray> preferences = new FastByIDMap<PreferenceArray>();
		LongPrimitiveIterator userIdsIterator = model.getUserIDs();

		while (userIdsIterator.hasNext()) {

			Long userId = userIdsIterator.next();
			PreferenceArray prefsForUser = model.getPreferencesFromUser(userId);
			if (prefsForUser instanceof ContextualUserPreferenceArray) {
				ContextualUserPreferenceArray contextualPrefsForUser = (ContextualUserPreferenceArray) prefsForUser;
				ArrayList<Long> newItemIds = new ArrayList<Long>();
				ArrayList<Float> newPrefValues = new ArrayList<Float>();
				ArrayList<List<Long>> newContextualPrefs = new ArrayList<List<Long>>();

				for (int i = 0; i < contextualPrefsForUser.getIDs().length; i++) {

					if (singleDomain.equals(
							this.getItemInformationByID(contextualPrefsForUser.get(i).getItemID()).getItemDomain())) {
						newItemIds.add(contextualPrefsForUser.get(i).getItemID());
						newPrefValues.add(contextualPrefsForUser.get(i).getValue());
						Long[] longObjects = ArrayUtils
								.toObject(contextualPrefsForUser.get(i).getContextualPreferences());
						newContextualPrefs.add(Arrays.asList(longObjects));
					}

				}

				if (newItemIds.size() > 0 && newContextualPrefs.size() > 0) {
					ContextualUserPreferenceArray newPrefsForUser = new ContextualUserPreferenceArray(
							newItemIds.size());
					newPrefsForUser.setUserID(0, userId);

					for (int n = 0; n < newItemIds.size(); n++) {
						newPrefsForUser.setItemID(n, newItemIds.get(n));
						newPrefsForUser.setValue(n, newPrefValues.get(n));
						newPrefsForUser.setContextualPreferences(n, Longs.toArray(newContextualPrefs.get(n)));

					}

					preferences.put(userId, newPrefsForUser);
				}
			} else {
				throw new TasteException(
						"prefs not ContextualUserPreferenceArray in AmazonCrossDataset for SingleDomain");
			}

		}
		// System.out.println(counter);
		DataModel filteredDataModel = new ContextualDataModel(preferences);

		this.model = filteredDataModel;

		// return filteredDataModel;
	}

	private void convertDatasetFileToDefaultPattern(HashSet<ItemDomain> domains, int minRatingsPerUser,
			boolean onlyOverlap) {

		if (generateNewFiles) {

			File fileEN = new File(datasetURLOriginal);
			File fileOutput = new File(datasetURL);
			File fileOutputInfo = new File(datasetInformationURL);

			FileInputStream stream;

			Integer counter = 1;

			String line = "";
			try {
				stream = new FileInputStream(fileEN);

				InputStreamReader streamReader = new InputStreamReader(stream);
				BufferedReader reader = new BufferedReader(streamReader);

				FileOutputStream streamOutput = new FileOutputStream(fileOutput);

				OutputStreamWriter streamWriter = new OutputStreamWriter(streamOutput);

				BufferedWriter bw = new BufferedWriter(streamWriter);

				FileOutputStream streamOutputItemInfo = new FileOutputStream(fileOutputInfo);

				OutputStreamWriter streamWriterItemInfo = new OutputStreamWriter(streamOutputItemInfo);

				BufferedWriter bwInfo = new BufferedWriter(streamWriterItemInfo);

				HashMap<String, Integer> idMaps = new HashMap<String, Integer>();

				Integer userId = 1;

				HashSet<String> unknownCategories = new HashSet<String>();

				ArrayList<EntryRatingContextualFile> entriesRatingContextualFile = new ArrayList<AmazonCrossDataset.EntryRatingContextualFile>();
				ArrayList<EntryDBItemInformation> entriesDBItemInformation = new ArrayList<AmazonCrossDataset.EntryDBItemInformation>();

				line = reader.readLine();

				boolean hasNextFile = true;
				do {

					while (line != null) {

						line = line.replaceAll(";", " ");

						if (line.contains("Id:")) {
							String itemId = line.split(":")[1].trim();

							String amazonItemId = "";
							String title = "";
							String domain = "";
							String categories = "";
							String rating = "";
							String userText = "";
							String timestamp = "";

							HashSet<String> usersRatingThisItem = new HashSet<String>();

							while ((line = reader.readLine()) != null && !line.contains("Id:")) {

								line = line.replaceAll(";", " ");

								if (line.contains("ASIN:")) {
									amazonItemId = line.split(":")[1].trim();
								} else if (line.contains("title:")) {
									String patternString = "title:(.*)";

									Pattern pattern = Pattern.compile(patternString);

									Matcher matcher = pattern.matcher(line);
									if (matcher.find()) {
										title = matcher.group(1).trim();
									}
								} else if (line.contains("group:")) {
									domain = line.split(":")[1].trim();

									if (domain.equalsIgnoreCase("DVD") || domain.equalsIgnoreCase("Video")) {
										domain = ItemDomain.MOVIE.name();
									} else if (domain.equalsIgnoreCase("Musicals")) {
										domain = ItemDomain.MUSIC.name();
									} else if (domain.equalsIgnoreCase("Video Games")) {
										domain = ItemDomain.VIDEO_GAME.name();
									} else if (domain.equalsIgnoreCase("Baby Product")) {
										domain = ItemDomain.BABY_PRODUCT.name();
									}

									if (!domains.contains(ItemDomain.valueOf(domain.toUpperCase()))) {
										// System.out.println(domain+" UNKNOWN");
										break;
									}

								} else if (line.contains("categories:")) {
									HashSet<ItemCategory> itemCategories = new HashSet<ItemCategory>();
									while (!(line = reader.readLine()).contains("reviews:")) {
										line = line.replaceAll(";", " ");

										String patternString = "Subjects/[1000/]/|(.*?)/|";

										Pattern pattern = Pattern.compile(patternString);

										Matcher matcher = pattern.matcher(line);
										if (matcher.find()) {
											String categoryText = matcher.group().split("/|")[1].trim()
													.split("/[")[0];
											ItemCategory category = null;
											try {
												category = ItemCategory.getCategoryEnum(categoryText,
														ItemDomain.valueOf(domain.toUpperCase()));
											} catch (IllegalArgumentException e) {
												System.out.println(line + " " + amazonItemId + " " + counter);
												e.printStackTrace();
											}
											if (!itemCategories.contains(category)) {
												itemCategories.add(category);
												if (!category.equals(ItemCategory.UNKNOWN_BOOK)
														|| !category.equals(ItemCategory.UNKNOWN_MOVIE)
														|| categories.equals("")) {
													categories = categories + category + "|";
												}
											}

											if ((category.equals(ItemCategory.UNKNOWN_BOOK)
													|| category.equals(ItemCategory.UNKNOWN_MOVIE))
													&& !unknownCategories.contains(categoryText)) {
												unknownCategories.add(categoryText);
												System.out.println(categoryText + " category unknown");
											}
										} else {
											patternString = "Styles/[301668/]/|(.*?)/|";

											pattern = Pattern.compile(patternString);

											matcher = pattern.matcher(line);

											if (matcher.find()) {
												if (ItemDomain.valueOf(domain.toUpperCase()).equals(ItemDomain.MUSIC)) {
													String categoryText = matcher.group().split("/|")[1].trim()
															.split("/[")[0];
													ItemCategory category = ItemCategory.getCategoryEnum(categoryText,
															ItemDomain.valueOf(domain.toUpperCase()));
													if (!category.equals(ItemCategory.UNKNOWN_MUSIC)
															|| categories.equals("")) {
														categories = category + "|";
													}
													if (category.equals(ItemCategory.UNKNOWN_MUSIC)
															&& !unknownCategories.contains(categoryText)) {
														unknownCategories.add(categoryText);
														System.out.println(categoryText + " category unknown");
													}
												} else if (!categories.contains("MUSICALS")) {
													categories = ItemCategory.getCategoryEnum("MUSICALS",
															ItemDomain.valueOf(domain.toUpperCase())) + "|";
												}

											} else {
												patternString = "Genres/[.*/]/|(.*?)/|";

												pattern = Pattern.compile(patternString);

												matcher = pattern.matcher(line);

												if (matcher.find()) {
													String categoryText = matcher.group().split("/|")[1].trim()
															.split("/[")[0];

													ItemCategory category = ItemCategory.getCategoryEnum(categoryText,
															ItemDomain.valueOf(domain.toUpperCase()));
													if (!itemCategories.contains(category)) {
														itemCategories.add(category);
														if (!category.equals(ItemCategory.UNKNOWN_BOOK)
																|| !category.equals(ItemCategory.UNKNOWN_MOVIE)
																|| categories.equals("")) {
															categories = categories + category + "|";
														}
													}
													if ((category.equals(ItemCategory.UNKNOWN_BOOK)
															|| category.equals(ItemCategory.UNKNOWN_MOVIE))
															&& !unknownCategories.contains(categoryText)) {
														unknownCategories.add(categoryText);
														System.out.println(categoryText + " category unknown");
													}
												}
											}

										}
									}
									if (categories.length() == 0) {
										categories = ItemCategory
												.getCategoryEnum("UNKNOWN", ItemDomain.valueOf(domain.toUpperCase()))
												.name();
										// categories = "Unknown";
										// continue;
									} else {
										categories = categories.substring(0, categories.length() - 1);
									}
									while ((line = reader.readLine()) != null && line.contains("rating:")) {

										line = line.replaceAll(";", " ");

										String patternString = "/d/d/d/d-/d+-/d+";

										Pattern pattern = Pattern.compile(patternString);

										Matcher matcher = pattern.matcher(line);

										if (matcher.find()) {
											timestamp = String.valueOf(this.getDifference(matcher.group()));
										}

										long dayType = ContextualFileGenerator.getDayType(timestamp, 1);
										long periodOfDay = ContextualFileGenerator.getPeriodOfDay(timestamp, 1);

										patternString = "cutomer:(.*)rating";

										pattern = Pattern.compile(patternString);

										matcher = pattern.matcher(line);

										if (matcher.find()) {
											userText = matcher.group(1).trim();
										}

										if (!usersRatingThisItem.contains(userText)) {
											usersRatingThisItem.add(userText);
										} else {
											continue; // do not insert duplicated
														// rating
										}

										String userTextTemp;

										if (!idMaps.keySet().contains(userText)) {
											idMaps.put(userText, userId);
											userTextTemp = String.valueOf(userId);
											userId++;
										} else {
											userTextTemp = String.valueOf(idMaps.get(userText));
										}

										patternString = "rating:(.*)votes";

										pattern = Pattern.compile(patternString);

										matcher = pattern.matcher(line);

										if (matcher.find()) {
											rating = matcher.group(1).trim();
										}

										entriesRatingContextualFile.add(
												this.new EntryRatingContextualFile(new Long(userTextTemp), userText,
														itemId, amazonItemId, rating, dayType, periodOfDay, domain));

									}

									entriesDBItemInformation.add(this.new EntryDBItemInformation(itemId, amazonItemId,
											title, categories, domain));

								}
							}
						} else {
							line = reader.readLine();
						}

					}

					counter++;
					fileEN = new File(datasetURLOriginal.substring(0, datasetURLOriginal.length() - 1)
							.concat(counter.toString()));

					if (fileEN.exists()) {
						stream = new FileInputStream(fileEN);

						streamReader = new InputStreamReader(stream);
						reader = new BufferedReader(streamReader);

						line = reader.readLine();
					} else {
						hasNextFile = false;
					}

				} while (hasNextFile);

				Collections.sort(entriesRatingContextualFile, new EntryRatingContextualFileComparator());

				StringBuffer entriesRatingAux = new StringBuffer();
				EntryRatingContextualFile currentEntry;
				// long currentUserID = -1;
				int numUsersCounter = 0;

				if (entriesRatingContextualFile.size() > 0) {
					System.out.println("entries file " + entriesRatingContextualFile.size());
					currentEntry = entriesRatingContextualFile.get(0);
					numUsersCounter = 1;

					int ratingsCounter = 0;

					HashSet<ItemDomain> itemDomainAux = (HashSet<ItemDomain>) domains.clone();

					// int counterUserWithBooksAndMovieRatingsWithoutMusic = 0;

					for (EntryRatingContextualFile entry : entriesRatingContextualFile) {

						if (entry.getUserText() == currentEntry.getUserText()) {
							entriesRatingAux
									.append(entry.getUserText() + "\t" + entry.getItemId() + "\t" + entry.getRating()
											+ "\t" + entry.getDayType() + "|" + entry.getPeriodOfDay() + "\n");
							ratingsCounter++;

							if (onlyOverlap && !itemDomainAux.isEmpty()) {
								itemDomainAux.remove(ItemDomain.valueOf(entry.getDomain().toUpperCase()));
							}

						} else {
							if (ratingsCounter >= minRatingsPerUser) {
								if (onlyOverlap) {
									if (itemDomainAux.isEmpty()) {
										bw.append(entriesRatingAux);
										numUsersCounter++;
									} else {
										/*
										 * if(itemDomainAux.size() == 1 && itemDomainAux.contains(ItemDomain.MUSIC)){
										 * System.out.println(currentEntry.getAmazonUserID()
										 * +" HAS MOVIE AND BOOK RATINGS, BUT HASNT MUSIC RATINGS");
										 * counterUserWithBooksAndMovieRatingsWithoutMusic++; }
										 */
										idMaps.remove(currentEntry.getAmazonUserID());
									}
								} else {
									bw.append(entriesRatingAux);
									numUsersCounter++;
								}

							} else {
								idMaps.remove(currentEntry.getAmazonUserID());
								// System.out.println(currentUserID);
							}
							entriesRatingAux = new StringBuffer();
							entriesRatingAux
									.append(entry.getUserText() + "\t" + entry.getItemId() + "\t" + entry.getRating()
											+ "\t" + entry.getDayType() + "|" + entry.getPeriodOfDay() + "\n");
							ratingsCounter = 1;
							itemDomainAux = (HashSet<ItemDomain>) domains.clone();

							if (onlyOverlap && !itemDomainAux.isEmpty()) {
								itemDomainAux.remove(ItemDomain.valueOf(entry.getDomain().toUpperCase()));
							}

							currentEntry = entry;

						}

						// bw.newLine();
					}

					System.out.println("Number of users: " + numUsersCounter);

					if (ratingsCounter >= minRatingsPerUser) {// adiciona ultimo
																// rating (caso min
																// = 0 ou 1)
						if (onlyOverlap) {
							if (itemDomainAux.isEmpty()) {
								bw.append(entriesRatingAux);
								numUsersCounter++;
							} else {
								idMaps.remove(currentEntry.getAmazonUserID());
							}
						} else {
							bw.append(entriesRatingAux);
							numUsersCounter++;
						}
					} else {
						idMaps.remove(currentEntry.getAmazonUserID());
						// System.out.println(currentUserID);
					}
				}

				for (EntryDBItemInformation entry : entriesDBItemInformation) {
					bwInfo.append(entry.getItemId() + datasetInformationDelimiter + entry.getAmazonItemID()
							+ datasetInformationDelimiter + entry.getTitle() + datasetInformationDelimiter
							+ entry.getCategories() + datasetInformationDelimiter + entry.getDomain());
					bwInfo.newLine();
				}

				generateUserIDMap(idMaps);

				reader.close();
				streamReader.close();
				bw.close();
				bwInfo.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
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
			} catch (Exception e) {
				System.out.println(line);
				e.printStackTrace();
			}
		}

	}

	private void generateUserIDMap(HashMap<String, Integer> idMaps) throws IOException {
		File fileOutputUserIDsMap = new File(datasetUserIDsMap);

		FileOutputStream streamOutputUserIDsMap = new FileOutputStream(fileOutputUserIDsMap);

		OutputStreamWriter streamWriterUserIDsMap = new OutputStreamWriter(streamOutputUserIDsMap);

		BufferedWriter bwUserIDMap = new BufferedWriter(streamWriterUserIDsMap);

		for (final String key : idMaps.keySet()) {

			TimeLimiter limiter = new SimpleTimeLimiter();
			String address;
			try {
				address = limiter.callWithTimeout(new Callable<String>() {
					public String call() {
						return getUserAddressFromAmazon(key);
					}
				}, 15, TimeUnit.SECONDS, false);
			} catch (Exception e) {
				address = " ";
				log.warn("Address for user " + key + " was not extracted before the timeout");
			}

			// System.out.println(address);
			// String address =
			// extractContextFromAmazonUserID(key);

			bwUserIDMap.append(idMaps.get(key) + ";" + key + ";" + address);
			bwUserIDMap.newLine();
		}

		bwUserIDMap.close();
	}

	private String getUserAddressFromAmazon(String key) {
		UserInformation ai = this.getUserInformationByUserAmazonID(key);

		String contextWeb = "";

		if (ai == null) {
			try {
				HashMap<String, String> properties = new HashMap<String, String>();
				// properties.put("Connection", "keep-alive");
				properties.put("Cache-Control", "max-age=0");
				properties.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				properties.put("Upgrade-Insecure-Requests", "1");
				properties.put("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");
				// properties.put("Accept-Encoding", "gzip, deflate, sdch");
				properties.put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.6,en;q=0.4");
				properties.put("Cookie",
						"apn-user-id=8abd42d1-3fb0-40e3-92a4-f1b72e0ae9a9; x-wl-uid=1pMBOSHB2hpDj8++q1uNxukG6k79kabb/GLEOu+6oJbtZ+vsbBo1vkaYv2vu6bba/UkUQgBYCM5NFf6w05DbhixTzEtECHrY6dnnAGioJ26dJUKxySeI06PliYgaRgYcxPv+Xi48q+dU=; s_vn=1461791914953%26vn%3D4; aws-target-visitor-id=1430255914658-641299.20_12; aws-target-data=%7B%22support%22%3A%221%22%7D; s_dslv=1434560390470; _mkto_trk=id:810-GRW-452&token:_mch-amazon.com-1438785762234-75403; s_pers=%20s_fid%3D7FB3F1DFBE87E3A4-2C0A2B674970AB18%7C1501944162409%3B%20s_dl%3D1%7C1438787562416%3B%20gpv_page%3DUS%253AAS%253ASOA-category%7C1438787562423%3B%20s_ev15%3D%255B%255B%2527NSGoogle%2527%252C%25271438785762458%2527%255D%255D%7C1596638562458%3B%20s_vnum%3D1441566307287%2526vn%253D1%7C1441566307287%3B%20s_invisit%3Dtrue%7C1438976107287%3B%20s_nr%3D1438974307298-Repeat%7C1446750307298%3B; __utma=194891197.639278582.1430255911.1430830019.1438974308.4; __utmz=194891197.1438974308.4.4.utmccn=(referral)|utmcsr=google.com|utmcct=/|utmcmd=referral; session-token=JtSl8nqbrEMvp4rfmdLhVuG+b4Vw0mtwgYlPYgIwbhOXHgg6ZM6NKj4WmMheQfNlrfT8WW76LXP0efMZnCBsy8ho5P7S0TovZ+C1rPEk7nfhtrXqAiBqFavEX3jMVHPL0nINQaY9HsNHo5aWHqlDM50HZzoCqFRLQnxAGb5zne7FTIR9KyGSzl6UMStRvOJKfoDpA0O7fmAHaxkf9fwCsT9Bmqay21CFEDI2Ph5DFmryLcRE+yFX/deYWjHvQ+WN; ubid-main=187-1114539-0952120; session-id-time=2082787201l; session-id=178-5520214-9875866; csm-hit=1CZJ0QS01V667RA900PE+s-1RRAGVM8ZRRXTX1RMS93|1445554269104");

				log.warn("Starting location (country extraction for user " + key);

				String response = Functions.httpGet("http://www.amazon.com/gp/pdp/profile/" + key, properties);

				// log.warn("Waitig location (country extraction for user "+key);

				// String response = Functions.httpGet("http://www.amazon.com");

				String patternString = "<div class=\"a-fixed-right-grid location-and-occupation-holder\"><div class=\"a-fixed-right-grid-inner\" style=\"padding-right:0.0px\"><span>(.+?)</span>";

				Pattern pattern = Pattern.compile(patternString);

				Matcher matcher = pattern.matcher(response);

				if (matcher.find()) {
					contextWeb = matcher.group(1);
					log.info("Location found to " + key + ": " + contextWeb);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			contextWeb = ai.getAddress();
		}

		return contextWeb;
	}

	public void generateReviewFileFromRatings() throws IOException, JSONException, TasteException {
		// extractReviewsInDomain("C:/Users/Douglas/Desktop/Cross_Domain_Tools/datasets/amazon/amazon-meta.txt/datasetMusicReviews.txt",
		// "C:/Users/Douglas/Desktop/Cross_Domain_Tools/datasets/amazon/giga/reviews_Digital_Music.json.gz2.");

		extractReviewsInDomain(
				"C:/Users/Douglas/Desktop/Cross_Domain_Tools/datasets/amazon/amazon-meta.txt/datasetBooksReviews.txt",
				"C:/Users/Douglas/Desktop/Cross_Domain_Tools/datasets/amazon/giga/reviews_Books.txt.");

		// extractReviewsInDomain("C:/Users/Douglas/Desktop/Cross_Domain_Tools/datasets/amazon/amazon-meta.txt/datasetTelevisionReviews.txt",
		// "C:/Users/Douglas/Desktop/Cross_Domain_Tools/datasets/amazon/giga/reviews_Movies_&_TV.txt.");
	}

	private void extractReviewsInDomain(String outputFile, String inputFile)
			throws FileNotFoundException, IOException, JSONException, TasteException {
		File fileOutputReviewsFile = new File(outputFile);

		FileOutputStream streamOutputReviewsFile = new FileOutputStream(fileOutputReviewsFile);

		OutputStreamWriter streamWriterReviewsFile = new OutputStreamWriter(streamOutputReviewsFile);

		BufferedWriter bwReviewsFile = new BufferedWriter(streamWriterReviewsFile);

		boolean hasNextFile = true;

		int counterBookReviewsFile = 1;

		File fileReaderBook = new File(inputFile + StringUtils.leftPad(String.valueOf(counterBookReviewsFile), 3, "0"));

		FileInputStream stream;
		InputStreamReader streamReader;
		BufferedReader reader;

		String line;

		String tempPreviousFile = "";
		boolean appendPreviousFileLine = false;

		HashSet<String> notRatedItemIds = new HashSet<String>();
		HashSet<String> nonUsersIds = new HashSet<String>();

		do {

			if (fileReaderBook.exists()) {

				log.info("Extracting reviews from " + fileReaderBook.getName());

				stream = new FileInputStream(fileReaderBook);

				streamReader = new InputStreamReader(stream);
				reader = new BufferedReader(streamReader);

				line = reader.readLine();

				while (line != null) {
					if (line.startsWith("{") && line.endsWith("}")) {
						JSONObject jsonObj = new JSONObject(line);
						String itemASIN = jsonObj.getString("asin");

						if (notRatedItemIds.contains(itemASIN)) {
							line = reader.readLine();
							continue;
						}

						ItemInformation itemInfo = this.getItemInformationByAsin(itemASIN);

						if (itemInfo == null) {
							notRatedItemIds.add(itemASIN);
							line = reader.readLine();
							continue;
						}

						String userAmazonID = jsonObj.getString("reviewerID");

						if (nonUsersIds.contains(userAmazonID)) {
							line = reader.readLine();
							continue;
						}

						UserInformation userInfo = this.getUserInformationByUserAmazonID(userAmazonID);

						if (userInfo == null) {
							nonUsersIds.add(userAmazonID);
							line = reader.readLine();
							continue;
						}

						Float pref = this.getModel().getPreferenceValue(Long.valueOf(userInfo.getId()),
								Long.valueOf(itemInfo.getId()));

						/*
						 * try{ pref =
						 * this.getModel().getPreferenceValue(Long.valueOf(userInfo.getId()),
						 * Long.valueOf(itemInfo.getId())); }catch(NoSuchUserException e){
						 * System.err.println("User "+userInfo.getId()
						 * +" is on userIDsMap, but isnt on contextual model");
						 * nonUsersIds.add(userAmazonID); line = reader.readLine(); continue; }
						 */

						if (pref != null) {

							String summary = jsonObj.getString("summary");
							String reviewText = jsonObj.getString("reviewText");

							bwReviewsFile.append(
									userInfo.getId() + "\t" + itemInfo.getId() + "\t" + summary + "\t" + reviewText);
							bwReviewsFile.newLine();
						}
					} else {

						if (appendPreviousFileLine) {
							line = tempPreviousFile + line;
							appendPreviousFileLine = false;
							continue;
						}

						tempPreviousFile = line;
						appendPreviousFileLine = true;
						break;
					}

					line = reader.readLine();
				}

			} else {
				hasNextFile = false;
			}

			counterBookReviewsFile++;

			fileReaderBook = new File(inputFile + StringUtils.leftPad(String.valueOf(counterBookReviewsFile), 3, "0"));
		} while (hasNextFile);

		bwReviewsFile.close();
	}

	public void createAddressFileWithGoogle() throws IOException {

		// HashSet<String> includedAddresses = new HashSet<String>();
		HashSet<String> includedAddresses = readPartialFile();
		// long contextValue = -1;
		// String contextWeb = "";
		String country = "";
		String city = "";
		String state = "";

		File fileOutputAddressesFile = new File(
				"C:/Users/Douglas/Desktop/Cross_Domain_Tools/datasets/amazon/amazon-meta.txt/addressesInformation_full.dat");

		FileOutputStream streamOutputAddressesFile = new FileOutputStream(fileOutputAddressesFile);

		OutputStreamWriter streamWriterAddressesFile = new OutputStreamWriter(streamOutputAddressesFile);

		BufferedWriter bwAddressesFile = new BufferedWriter(streamWriterAddressesFile);
		// int counterMissingAddresses = 0;
		for (UserInformation userInfo : userDatasetInformation.getUsers()) {
			if (!includedAddresses.contains(userInfo.getAddress()) && userInfo.getAddress().length() > 1) {
				// counterMissingAddresses++;
				/// System.out.println(userInfo.getAddress());
				includedAddresses.add(userInfo.getAddress());

				String urlGoogle = "https://maps.googleapis.com/maps/api/geocode/json?address="
						+ userInfo.getAddress().replaceAll("/s+", "%20");
				urlGoogle = urlGoogle + "&key=AIzaSyCI2L-OcSbTXHYmRf_avJwJRDZ4qLV1wFI";

				String responseGoogle;

				city = " ";
				state = " ";
				country = " ";
				try {
					responseGoogle = Functions.httpGet(urlGoogle, null);

					JSONObject jsonObj = new JSONObject(responseGoogle);
					JSONArray arrayResults = jsonObj.getJSONArray("results");

					JSONObject jsonObjectResults = arrayResults.getJSONObject(0);

					JSONArray array = jsonObjectResults.getJSONArray("address_components");

					for (int i = 0; i < array.length(); i++) {
						if (array.getJSONObject(i).getString("types").contains("locality")) {
							city = array.getJSONObject(i).getString("long_name");
						} else if (array.getJSONObject(i).getString("types").contains("administrative_area_level_1")) {
							state = array.getJSONObject(i).getString("long_name");
						} else if (array.getJSONObject(i).getString("types").contains("country")) {
							country = array.getJSONObject(i).getString("long_name");
						}
					}

					String exportLine = userInfo.getAddress() + datasetInformationDelimiter + country
							+ datasetInformationDelimiter + state + datasetInformationDelimiter + city;

					System.out.println(exportLine);
					bwAddressesFile.append(exportLine);
					bwAddressesFile.newLine();

				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				/*
				 * for(LocationCountryContextualAttribute location :
				 * LocationCountryContextualAttribute.values()){
				 * if(contextGoogle.replaceAll("/s+",
				 * "").toUpperCase().equals(location.name())){ contextValue =
				 * location.getCode(); //System.out.println(location.name()); break; } }
				 */

			}
		}
		// System.out.println(counterMissingAddresses);
		bwAddressesFile.close();

	}

	private HashSet<String> readPartialFile() {
		HashSet<String> addressesInPartialFile = new HashSet<String>();

		File fileEN = new File(datasetAddressesInformationURL);

		FileInputStream stream;
		InputStreamReader streamReader;
		BufferedReader reader;

		try {
			stream = new FileInputStream(fileEN);

			streamReader = new InputStreamReader(stream);
			reader = new BufferedReader(streamReader);

			String line = reader.readLine();

			while (line != null) {
				String splitedLine[] = line.split(";");
				addressesInPartialFile.add(splitedLine[0]);

				line = reader.readLine();
			}

			reader.close();
			streamReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return addressesInPartialFile;
	}

	private Set<ItemCategory> getCategoriesFromDB(String row) {
		HashSet<ItemCategory> returnedItemCateogories = new HashSet<ItemCategory>();
		String extractedItemCateogories[] = row.split("\\|");
		for (String itemCategoryText : extractedItemCateogories) {
			returnedItemCateogories.add(ItemCategory.valueOf(itemCategoryText.toUpperCase()));
		}
		return returnedItemCateogories;
	}

	protected void initializeDBItemInfoZipFile() throws NumberFormatException, IOException {

		itemDatasetInformation = new ItemDatasetInformation();

		int countItemInformation = 0;

		
		ZipFile zip = new ZipFile(datasetInformationURL);
		
		String splitPathFile[] = datasetInformationURL.split(pathPattern);
		
		String fileName = splitPathFile[splitPathFile.length-1].replace("zip", "dat");
		
		ZipEntry entry = zip.getEntry(fileName);

		String line;

		ItemInformation itemInfo;

		if (entry == null) {
		      throw new FileNotFoundException(zip.getName());
		 }
		
	    InputStreamReader streamReader = new InputStreamReader(zip.getInputStream(entry));
		BufferedReader reader = new BufferedReader(streamReader);

		line = reader.readLine();

		while (line != null) {
			itemInfo = new ItemInformation();

			String row[] = line.split(datasetInformationDelimiter);
			if (row.length != 5) {
				log.error("Line " + line + " have more or less than five terms");
			}

			int index = 0;

			String itemId = row[index++];

			itemInfo.setId(Long.parseLong(itemId));

			itemInfo.setAsin(row[index++]);

			itemInfo.setName(row[index++]);
			// String categories[] = row[index].split("/|");

			// index++;

			itemInfo.setCategories(getCategoriesFromDB(row[index++]));
			itemInfo.setItemDomain(ItemDomain.valueOf(row[index++].toUpperCase()));

			// itemInfo.setYearReleased(row[index++]);
			// itemInfo.setLink(row[index++]);

			itemDatasetInformation.getItens().add(itemInfo);
			countItemInformation++;
			line = reader.readLine();
		}
		streamReader.close();
		reader.close();

		if (countItemInformation > 0) {
			Collections.sort(itemDatasetInformation.getItens(), new ItemInformationComparatorID());

		}

		log.info("Num of item informations: " + countItemInformation);

	}

	protected void initializeDBItemInfo() throws NumberFormatException, IOException {

		itemDatasetInformation = new ItemDatasetInformation();
		HashMap<Long, Integer> mapItens = new HashMap<Long, Integer>();

		int countItemInformation = 0;
		
		String [] datasetInformationURL = {"C:\\Users\\guilh\\Documents\\UFRPE\\TCC\\TBookmarks\\TBookmarksRSProject\\war\\WEB-INF\\resources\\datasets\\Books_MUSIC\\full-ratings-information.dat",
				"C:\\Users\\guilh\\Documents\\UFRPE\\TCC\\TBookmarks\\TBookmarksRSProject\\war\\WEB-INF\\resources\\datasets\\Books_MOVIE\\full-ratings-information.dat"										
		};   
		
		for (int splitedFilesNumber = 0; splitedFilesNumber < datasetInformationURL.length; splitedFilesNumber++) {
			// File fileEN = new File(datasetInformationURL+splitedFilesNumber+".dat");

			File fileEN = new File(datasetInformationURL[splitedFilesNumber]);

			FileInputStream stream;

			stream = new FileInputStream(fileEN);

			InputStreamReader streamReader = new InputStreamReader(stream);
			BufferedReader reader = new BufferedReader(streamReader);

			String line;

			ItemInformation itemInfo;

			line = reader.readLine();

			while (line != null) {
				
				itemInfo = new ItemInformation();

				String row[] = line.split(datasetInformationDelimiter);
				if (row.length != 5) {
					log.error("Line " + line + " have more or less than five terms");
				}

				int index = 0;

				String itemId = row[index++];
				long itemIdLong = Long.parseLong(itemId);
				
				if(!mapItens.containsKey(itemIdLong)) {
					
					itemInfo.setId(Long.parseLong(itemId));
					
					mapItens.put(itemIdLong, 0);
					
					itemInfo.setAsin(row[index++]);

					itemInfo.setName(row[index++]);
					// String categories[] = row[index].split("/|");

					// index++;

					itemInfo.setCategories(getCategoriesFromDB(row[index++]));
					itemInfo.setItemDomain(ItemDomain.valueOf(row[index++].toUpperCase()));

					// itemInfo.setYearReleased(row[index++]);
					// itemInfo.setLink(row[index++]);

					itemDatasetInformation.getItens().add(itemInfo);
					countItemInformation++;
					
				}
				
				line = reader.readLine();
			}
			reader.close();
			streamReader.close();
		}

		if (countItemInformation > 0) {
			Collections.sort(itemDatasetInformation.getItens(), new ItemInformationComparatorID());

		}

		log.info("Num of item informations: " + countItemInformation);

	}

	protected void initializeDBAddressInfo() throws NumberFormatException, IOException {

		addressDatasetInformation = new AddressDatasetInformation();

		int countAddressInformation = 0;

		// for(int splitedFilesNumber = 1; splitedFilesNumber <= 1;
		// splitedFilesNumber++){
		// File fileEN = new File(datasetInformationURL+splitedFilesNumber+".dat");

		File fileEN = new File(datasetAddressesInformationURL);

		FileInputStream stream;

		stream = new FileInputStream(fileEN);

		InputStreamReader streamReader = new InputStreamReader(stream);
		BufferedReader reader = new BufferedReader(streamReader);

		String line;

		AddressInformation addressInfo;

		line = reader.readLine();

		while (line != null) {
			addressInfo = new AddressInformation();

			String row[] = line.split(datasetInformationDelimiter);

			int numOfColumns = 4;

			if (row.length != numOfColumns) {
				log.error("Line " + line + " have more or less than " + numOfColumns + " terms");
			}

			int index = 0;

			addressInfo.setUserAddress(row[index++]);

			addressInfo.setCountry(row[index++]);

			addressInfo.setState(row[index++]);

			addressInfo.setCity(row[index++]);

			// itemInfo.setYearReleased(row[index++]);
			// itemInfo.setLink(row[index++]);

			addressDatasetInformation.getAddresses().add(addressInfo);
			countAddressInformation++;
			line = reader.readLine();
		}
		reader.close();
		streamReader.close();
		// }

		if (countAddressInformation > 0) {
			Collections.sort(addressDatasetInformation.getAddresses(), new AddressInformationComparatorName());

		}

		log.info("Num of address informations: " + countAddressInformation);

	}

	protected void initializeDBUserInfo() throws NumberFormatException, IOException {

		userDatasetInformation = new UserDatasetInformation();
		HashMap<Long, Integer> mapUser = new HashMap<Long, Integer>();

		int countUserInformation = 0;

		String [] datasetInformationURL = {"C:\\Users\\guilh\\Documents\\UFRPE\\TCC\\TBookmarks\\TBookmarksRSProject\\war\\WEB-INF\\resources\\datasets\\Books_MUSIC\\userIDsMap.dat",
				"C:\\Users\\guilh\\Documents\\UFRPE\\TCC\\TBookmarks\\TBookmarksRSProject\\war\\WEB-INF\\resources\\datasets\\Books_MOVIE\\userIDsMap.dat"										
		};   

		 for(int splitedFilesNumber = 0; splitedFilesNumber < datasetInformationURL.length ; splitedFilesNumber++){
		// File fileEN = new File(datasetInformationURL+splitedFilesNumber+".dat");

			File fileEN = new File(datasetInformationURL[splitedFilesNumber]);
	
			FileInputStream stream;
	
			stream = new FileInputStream(fileEN);
	
			InputStreamReader streamReader = new InputStreamReader(stream);
			BufferedReader reader = new BufferedReader(streamReader);
	
			String line;
	
			UserInformation userInfo;
	
			line = reader.readLine();
	
			while (line != null) {
				userInfo = new UserInformation();
	
				String row[] = line.split(datasetInformationDelimiter);
	
				int numOfColumns = 3;
	
				if (row.length != numOfColumns) {
					log.error("Line " + line + " have more or less than " + numOfColumns + " terms");
					line = reader.readLine();
					continue;
				}
	
				int index = 0;
	
				String userId = row[index++];
				
				long userIdLong = Long.parseLong(userId);
				
				if(!mapUser.containsKey(userIdLong)) {
	
					userInfo.setId(Long.parseLong(userId));
					
					mapUser.put(userIdLong, 0);;
		
					userInfo.setAmazonID(row[index++]);
		
					userInfo.setAddress(row[index++]);
		
					// itemInfo.setYearReleased(row[index++]);
					// itemInfo.setLink(row[index++]);
		
					userDatasetInformation.getUsers().add(userInfo);
					countUserInformation++;
				}
				
				line = reader.readLine();
			}
			reader.close();
			streamReader.close();
		}

		if (countUserInformation > 0) {
			Collections.sort(userDatasetInformation.getUsers(), new UserInformationComparatorID());

		}

		log.info("Num of user informations: " + countUserInformation);

	}

	/*
	 * public static AbstractDataset getInstance(double userOverlapLevel, ItemDomain
	 * targetDomain, boolean b, boolean c, double trainingPercentage) { //
	 * Auto-generated method stub return null; }
	 */

	/*
	 * private String removeSemiColonInField(String line) {
	 * 
	 * String regex = "\".*?\"";
	 * 
	 * Pattern pattern = Pattern.compile(regex);
	 * 
	 * Matcher matcher = pattern.matcher(line);
	 * 
	 * String replaced = line;
	 * 
	 * while (matcher.find()) { String auxReplace = matcher.group().replaceAll(",",
	 * ""); replaced = replaced.replace(matcher.group(), auxReplace); } return
	 * replaced; }
	 */

	public static void main(String[] args) {
		HashSet<ItemDomain> domainsFilter = new HashSet<ItemDomain>();
		domainsFilter.add(ItemDomain.MOVIE);
		domainsFilter.add(ItemDomain.BOOK);
		// domainsFilter.add(ItemDomain.MUSIC);

		// AbstractDataset dataset =
		// AmazonCrossDataset.getInstance(0.1,ItemDomain.MUSIC,false); //generate
		// overlapped File
		// dataset = AmazonCrossDataset.getInstance(0.5,ItemDomain.MUSIC,false);
		// //generate overlapped File
		// dataset = AmazonCrossDataset.getInstance(0.1,ItemDomain.BOOK,false);
		// //generate overlapped File
		// dataset = AmazonCrossDataset.getInstance(0.5,ItemDomain.BOOK,false);
		// //generate overlapped File

		AmazonCrossDataset dataset = AmazonCrossDataset.getInstance(domainsFilter, 20, true); // generate cross domain
		// AmazonCrossDataset dataset =
		// AmazonCrossDataset.getInstance(1.0,ItemDomain.MOVIE,ItemDomain.BOOK,true);
		// //cross domain

		// HashMap<String,Integer> countryCounter = new HashMap<String, Integer>();
		//
		// for(AddressInformation ai :
		// dataset.getAddressDatasetInformation().getAddresses()){
		//
		// String aiCountry = ai.getState()+":"+ai.getCity();
		//
		// if(ai.getCountry().equals("United States") &&
		// countryCounter.containsKey(aiCountry)){
		// countryCounter.put(aiCountry, countryCounter.get(aiCountry)+1);
		// }else{
		// if(ai.getCountry().equals("United States")){
		// System.out.println(ai.getCountry()+" "+aiCountry);
		// countryCounter.put(aiCountry,1);
		// }
		//
		// }
		//
		// }
		//
		// for(String country : countryCounter.keySet()){
		// System.out.println(country+" : "+countryCounter.get(country));
		// }

		/*
		 * try { dataset.createAddressFileWithGoogle(); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */

		/*
		 * try { dataset.generateReviewFileFromRatings(); } catch (IOException e) {
		 * e.printStackTrace(); } catch (JSONException e) { e.printStackTrace(); } catch
		 * (TasteException e) { e.printStackTrace(); }
		 */
	}

}
