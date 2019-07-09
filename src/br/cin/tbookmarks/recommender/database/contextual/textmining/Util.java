package br.cin.tbookmarks.recommender.database.contextual.textmining;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.BaseClassifierEvaluator;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassifierEvaluator;
import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.classify.PrecisionRecallEvaluation;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.ScoredClassifierEvaluator;
import com.aliasi.classify.ScoredPrecisionRecallEvaluation;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.spell.JaccardDistance;
import com.aliasi.spell.TfIdfDistance;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.FeatureExtractor;


public class Util {

	public static int SCORE = 0;
	public static int GUESSED_CLASS = 1;
	public static int ANNOTATION_OFFSET = 2;
	public static int TEXT_OFFSET = 3;

	public static String SCORE_LABEL = "SCORE";
	public static String GUESS_LABEL = "GUESS";
	public static String TRUTH_LABEL = "TRUTH";
	public static String TEXT_LABEL = "TEXT";

	public static String[] ANNOTATION_HEADER_ROW = {SCORE_LABEL,GUESS_LABEL,TRUTH_LABEL,TEXT_LABEL};
	public static int ROW_LENGTH = ANNOTATION_HEADER_ROW.length;

	public static void printPrecRecall(BaseClassifierEvaluator<CharSequence> evaluator) {
		for (String category : evaluator.categories()) {
			PrecisionRecallEvaluation prEval = evaluator.oneVersusAll(category);
			System.out.println("Category " + category);
			System.out.printf("Recall: %.2f\n", prEval.recall());
			System.out.printf("Prec  : %.2f\n", prEval.precision());
		}
	}

	public static <E> void printPRcurve(ScoredClassifierEvaluator<E> evaluator) {
		PrintWriter progressWriter = new PrintWriter(System.out,true);
		boolean interpolate = false;
		for (String category : evaluator.categories()) {
			ScoredPrecisionRecallEvaluation scoredPrEval = evaluator.scoredOneVersusAll(category);
			double[][] scoredCurve = scoredPrEval.prScoreCurve(interpolate);
			progressWriter.println("PR Curve for Category: " + category);
			ScoredPrecisionRecallEvaluation.printScorePrecisionRecallCurve(scoredCurve,progressWriter);
		}
	}

	public static XValidatingObjectCorpus<Classified<CharSequence>> loadXValCorpus(List<String[]> rows, int numFolds) throws IOException { //this is covered in Ch 1 How to train and evaluate with cross validation

		XValidatingObjectCorpus<Classified<CharSequence>> corpus 
		= new XValidatingObjectCorpus<Classified<CharSequence>>(numFolds);
		for (String[] row : rows) {
			String annotation = row[ANNOTATION_OFFSET];
			if (annotation.equals("")) {
				continue;
			}
			Classification classification = new Classification(row[ANNOTATION_OFFSET]);
			Classified<CharSequence> classified = new Classified<CharSequence>(row[TEXT_OFFSET],classification);
			corpus.handle(classified);
		}
		return corpus;
	}



	public static ObjectHandler<Classified<CharSequence>> corpusPrinter () {
		return new ObjectHandler<Classified<CharSequence>>() {

			@Override
			public void handle(Classified<CharSequence> e) {
				System.out.println(e.toString());
			}
		};
	}


	/*public static ObjectHandler<E> corpusPrinter2 () {
		return new ObjectHandler<E>() {

			@Override
			public void handle(E e) {
				System.out.println(e.toString());
			}
		};
	}
	 */


	public static String[] getCategoryArray(XValidatingObjectCorpus<Classified<CharSequence>> corpus) {
		final Set<String> categories =  new HashSet<String>();
		corpus.visitCorpus(new ObjectHandler<Classified<CharSequence>> () {
			@Override
			public void handle(Classified<CharSequence> e) {
				categories.add(e.getClassification().bestCategory());	
			}
		});
		return categories.toArray(new String[0]);
	}

	public static LogisticRegressionClassifier<CharSequence> trainLogReg(XValidatingObjectCorpus<Classified<CharSequence>> corpus, 
			TokenizerFactory tokenizerFactory, PrintWriter progressWriter) throws IOException {
		FeatureExtractor<CharSequence> featureExtractor
		= new TokenFeatureExtractor(tokenizerFactory);
		int minFeatureCount = 1;
		boolean addInterceptFeature = true;
		boolean noninformativeIntercept = true;
		RegressionPrior prior = RegressionPrior.gaussian(1.0,noninformativeIntercept);
		AnnealingSchedule annealingSchedule
		= AnnealingSchedule.exponential(0.00025,0.999);
		double minImprovement = 0.000000001;
		int minEpochs = 100;
		int maxEpochs = 2000;
		int blockSize = corpus.size(); 
		LogisticRegressionClassifier<CharSequence> hotStart = null;
		int rollingAvgSize = 10;
		ObjectHandler<LogisticRegressionClassifier<CharSequence>> classifierHandler
		= null;
		Reporter reporter = Reporters.writer(progressWriter);
		reporter.setLevel(LogLevel.WARN);
		/*return LogisticRegressionClassifier.<CharSequence>train(corpus,
				featureExtractor,
				minFeatureCount,
				addInterceptFeature,
				prior,
				blockSize,
				hotStart,
				annealingSchedule,
				minImprovement,
				rollingAvgSize,
				minEpochs,
				maxEpochs,
				classifierHandler,
				reporter);
				*/
		return LogisticRegressionClassifier.<CharSequence>train(corpus,
				featureExtractor,
				minFeatureCount,
				addInterceptFeature,
				prior,
				annealingSchedule,
				minImprovement,
				minEpochs,
				maxEpochs,
				reporter);
	}

	/*public static List<String[]> filterTFIDF(List<String[]> texts,
			TokenizerFactory tokFactory,
			double cutoff) {
		TfIdfDistance tfIdfdist = new TfIdfDistance(tokFactory);
		List<String[]> filteredTexts = new ArrayList<String[]>();
		for (int i = 0; i < texts.size(); ++i) {
			String targetText = texts.get(i)[TEXT_OFFSET];
			boolean addText = true;
			//big research literature on making the below loop more efficient
			for (int j = i + 1; j < texts.size(); ++j ) {
				String comparisionText = texts.get(j)[TEXT_OFFSET];
				double proximity 
				= 1- tfIdfdist.proximity(targetText,comparisionText);
				tfIdfdist.docFrequency(targetText);
				if (proximity >= cutoff) {
					addText = false;
					//System.out.printf(" Texts too close, proximity %.2f\n", proximity);
					//System.out.println("\t" + targetText);
					//System.out.println("\t" + comparisionText);
					break; //one nod to efficency
				}
			}
			if (addText) {
				filteredTexts.add(texts.get(i));
			}
		}
		return filteredTexts;
	}*/

	public static List<String[]> filterJaccard(List<String[]> texts,
			TokenizerFactory tokFactory,
			double cutoff) {
		JaccardDistance jaccardD = new JaccardDistance(tokFactory);
		List<String[]> filteredTexts = new ArrayList<String[]>();
		for (int i = 0; i < texts.size(); ++i) {
			String targetText = texts.get(i)[TEXT_OFFSET];
			boolean addText = true;
			//big research literature on making the below loop more efficient
			for (int j = i + 1; j < texts.size(); ++j ) {
				String comparisionText = texts.get(j)[TEXT_OFFSET];
				double proximity 
				= jaccardD.proximity(targetText,comparisionText);
				if (proximity >= cutoff) {
					addText = false;
					//System.out.printf(" Texts too close, proximity %.2f\n", proximity);
					//System.out.println("\t" + targetText);
					//System.out.println("\t" + comparisionText);
					break; //one nod to efficency
				}
			}
			if (addText) {
				filteredTexts.add(texts.get(i));
			}
		}
		return filteredTexts;
	}
	/*
	 * Described in Chapter 1: Deserializing and running a classifier
	 */
	public static void consoleInputBestCategory(BaseClassifier<CharSequence> classifier) throws IOException {
		BufferedReader reader = new BufferedReader(new 	InputStreamReader(System.in));
		while (true) {
			System.out.println("\nType a string to be classified. Empty string to quit.");
			String data = reader.readLine();
			if (data.equals("")) {
				return;
			}
			Classification classification 
			= classifier.classify(data);
			System.out.println("Best Category: " + classification.bestCategory());
		}
	}
	/*
	 * Described in Chapter 1: Getting confidence estimates from a classifier
	 */
	public static void consoleInputPrintClassification(BaseClassifier<CharSequence> classifier) throws IOException {
		BufferedReader reader = new BufferedReader(new 	InputStreamReader(System.in));
		while (true) {
			System.out.println("\nType a string to be classified. Empty string to quit.");
			String data = reader.readLine();
			if (data.equals("")) {
				return;
			}
			Classification classification 
			= classifier.classify(data);
			System.out.println(classification);
		}
	}
	/*
	 * Described in Chapter 1: Evaluation of classifiers—the confusion matrix
	 */
	/*public static List<String[]> readAnnotatedCsvRemoveHeader(File file) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(file);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,Strings.UTF8);
		CSVReader csvReader = new CSVReader(inputStreamReader);
		csvReader.readNext();//skip headers
		List<String[]> rows = new ArrayList<String[]>();
		String[] row;
	
		while ((row = csvReader.readNext()) != null) {
	
			if (row[ANNOTATION_OFFSET].equals("")) {
				continue;
			}
			rows.add(row);
		}
		csvReader.close();
		return rows;
	}*/

	/*
	 * Described in Chapter 1: Apply a classifier to a .csv file
	 */
	/*public static List<String[]> readCsvRemoveHeader(File file) throws IOException {
		FileInputStream fileIn = new FileInputStream(file);
		InputStreamReader inputStreamReader = new InputStreamReader(fileIn,Strings.UTF8);
		CSVReader csvReader = new CSVReader(inputStreamReader);
		csvReader.readNext();//skip headers
		List<String[]> rows = new ArrayList<String[]>();
		String[] row;
		while ((row = csvReader.readNext()) != null) {
			if (row[TEXT_OFFSET] == null || row[TEXT_OFFSET].equals("")) {
				continue;
			}
			rows.add(row);
		}
		csvReader.close();
		return rows; 
	}*/


	/*
	 * Described minimally in Chapter 1: Evaluation of classifiers—the confusion matrix
	 */

	public static String confusionMatrixToString(ConfusionMatrix confMatrix) {
		StringBuilder sb = new StringBuilder();
		String[] labels = confMatrix.categories();
		int[][] outcomes = confMatrix.matrix();
		sb.append("reference\\response\n");
		sb.append("          \\");
		for (String category : labels) {
			sb.append(category + ",");
		}
		for (int i = 0; i< outcomes.length; ++ i) {
			sb.append("\n         " + labels[i] + " ");
			for (int j = 0; j < outcomes[i].length; ++j) {
				sb.append(outcomes[i][j] + ",");
			}
		}
		return sb.toString();
	}

	/*
	 * Described in Chapter 1: Evaluation of classifiers—the confusion matrix
	 */

	public static String[] getCategories(List<String[]> data) {
		Set<String> categories = new HashSet<String>();
		for (String[] csvData : data) {
			if (!csvData[ANNOTATION_OFFSET].equals("")) {
				categories.add(csvData[ANNOTATION_OFFSET]);
			}
		}
		return categories.toArray(new String[0]);
	}

	/*public static List<String[]> readCsv(File file) throws IOException {
		CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(file),Strings.UTF8));
		List<String[]> data = csvReader.readAll();
		csvReader.close();
		return data;
	}

	public static void writeCsv(List<String[]> data, File file) throws IOException {
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file),Strings.UTF8));
		csvWriter.writeAll(data);
		csvWriter.close();
	}*/

	/**
	 * Writes csv outfile used throughout book with standard header. See ANNOTATION_HEADER_ROW for values
	 *
	 * @param data List<String[]> data with each list element a row in spreadsheet. Index 0 = score, 1 = response class, 2 = reference class (truth), 3 Text
	 * @param File file output file should end with .csv
	 * 
	 */

	/*
	 * Described in Chapter 1: Getting data from Twitter API
	 */
	/*public static void writeCsvAddHeader(List<String[]> data, File file) throws IOException {
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file),Strings.UTF8));
		csvWriter.writeNext(ANNOTATION_HEADER_ROW);
		csvWriter.writeAll(data);
		csvWriter.close();
	}

	public static void dedupeCsvFile(File inputCsv,File outputCsv, double threshold) throws IOException {

		List<String[]> data = readCsvRemoveHeader(inputCsv);
		System.out.println("Start with " + data.size());
		data = filterJaccard(data, IndoEuropeanTokenizerFactory.INSTANCE, threshold);
		System.out.println("Post dedupe size of " + data.size());
		writeCsvAddHeader(data, outputCsv);
	}*/

	/*public static void main(String args[]) throws IOException {
		File input = new File(args[0]);
		File output = new File(args[1]);
		dedupeCsvFile(input,output,.5d);
		System.out.println("Done");
	}*/
	
	
	
	/*
	 * Described in Chapter 1: Viewing error categories: False Positives
	 */
	public static <E> void printFalsePositives(String categoryToShow, ScoredClassifierEvaluator<E> evaluator, Corpus<ObjectHandler<Classified<E>>> corpus) throws IOException {
		final Map<E,Classification> truthMap = new HashMap<E,Classification>();
		corpus.visitCorpus(new ObjectHandler<Classified<E>>() {
			@Override
			public void handle(Classified<E> data) {
				truthMap.put(data.getObject(),data.getClassification());
			}
		});
		//System.out.println(evaluator.scoredOneVersusAll(category));
		List<Classified<E>> falsePositives 
			= evaluator.falsePositives(categoryToShow);
		System.out.println("\nFalse Positives for " + categoryToShow);
		System.out.println("*<category> is truth category");
		for (Classified<E> classified : falsePositives) {
			E data = classified.getObject();
			ScoredClassification sysClassification = (ScoredClassification) classified.getClassification();
			System.out.println("\n" + data);
			String truth = truthMap.get(data).bestCategory();
			for (int i = 0; i < evaluator.categories().length; ++i) {
				String category = sysClassification.category(i);
				String truthStar = category.equals(truth) ? "*" : " ";
				System.out.println(truthStar + category + " " + sysClassification.score(i));
			}
			
			//
			//System.out.println(data + " : " + truthClassification.bestCategory());
		}
	}


	public static void printConfusionMatrix(ConfusionMatrix confMatrix) {
		System.out.println(confusionMatrixToString(confMatrix));
	}

/* described in Chapter 3: TuneLogRegParam
 * 
 */
	public static <E> ConditionalClassifierEvaluator<E> xvalLogRegMultiThread(
			final XValidatingObjectCorpus<Classified<E>> corpus,
			final FeatureExtractor<E> featureExtractor,
			final int minFeatureCount, final boolean addInterceptFeature,
			final RegressionPrior prior, final AnnealingSchedule annealingSchedule,
			final double minImprovement, final int minEpochs, final int maxEpochs,
			final Reporter reporter, final int numFolds, final int numThreads, final String[] categories) throws IOException {
		
		corpus.setNumFolds(numFolds);
		corpus.permuteCorpus(new Random(11211));
		final boolean storeInputs = true;
		final ConditionalClassifierEvaluator<E> crossFoldEvaluator
			= new ConditionalClassifierEvaluator<E>(null, categories, storeInputs);
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < numFolds; ++i) {
			final XValidatingObjectCorpus<Classified<E>> fold = corpus.itemView();
			fold.setFold(i);
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						LogisticRegressionClassifier<E> classifier
							= LogisticRegressionClassifier.<E>train(fold,
								featureExtractor,
								minFeatureCount,
								addInterceptFeature,
								prior,
								annealingSchedule,
								minImprovement,
								minEpochs,
								maxEpochs,
								reporter);
						
						ConditionalClassifierEvaluator<E> withinFoldEvaluator 
							= new ConditionalClassifierEvaluator<E>(classifier, categories, storeInputs);
						crossFoldEvaluator.setClassifier(classifier);
						addToEvaluator2(fold,crossFoldEvaluator);
						//addToEvaluator(withinFoldEvaluator,crossFoldEvaluator);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			threads.add(new Thread(runnable,"Fold " + i));
		}
		runThreads(threads,numThreads);
		//printPRcurve(crossFoldEvaluator);
		return crossFoldEvaluator;
	}
	
	
	
	
	//does not increment data for prCurves
	/*public synchronized static <E> void addToEvaluator(BaseClassifierEvaluator<E> foldEval, ScoredClassifierEvaluator<E> crossFoldEval) {
		for (String category : foldEval.categories()) {
			for (Classified<E> classified : foldEval.truePositives(category)) {
				crossFoldEval.addClassification(category,classified.getClassification(),classified.getObject());
			}
			for (Classified<E> classified : foldEval.falseNegatives(category)) {
				crossFoldEval.addClassification(category,classified.getClassification(),classified.getObject());
			}
		}
	}
	*/
	
	public synchronized static <E> void addToEvaluator2(XValidatingObjectCorpus<Classified<E>> fold , 
														ObjectHandler<Classified<E>> crossFoldEvaluator) {
		fold.visitTest(crossFoldEvaluator);
	}
	
	
	public synchronized static <E> void addToEvaluator(final ConditionalClassifierEvaluator<E> foldEval, ConditionalClassifierEvaluator<E> crossFoldEval) {
		
		
		
		for (String category : foldEval.categories()) {
			for (Classified<E> classified : foldEval.truePositives(category)) {
				
				crossFoldEval.addClassification(category,classified.getClassification(),classified.getObject());
			}
			for (Classified<E> classified : foldEval.falseNegatives(category)) {
				crossFoldEval.addClassification(category,classified.getClassification(),classified.getObject());
			}
		}
	}
	
	public static void runThreads(List<Thread> threads, int maxThreads) {
		Set<Thread> threadsToRun = new HashSet<Thread>(threads);
		List<Thread> running = new ArrayList<Thread>();
		int numThreads = threads.size();
		int runCount = 0;
		while(threadsToRun.size() > 0 || running.size() > 0) {
			Iterator<Thread> it = running.iterator();
			while(it.hasNext()) {
				if(!it.next().isAlive()) {
					it.remove();
				}
			}
			it = threadsToRun.iterator();
			while(running.size() < maxThreads && it.hasNext()) {
				Thread t = it.next();
				it.remove();
				running.add(t);
				System.out.println("RUNNING thread " + t.getName() + " (" + (++runCount) + " of " + numThreads + ")");
				t.start();
			}
		}
	}


    public static <K, V extends Comparable<? super V>> Map<K, V> 
        sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
            new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
	
	
	/*public static List<String[]> extractContextualPossibleWords(
			List<String[]> tweets, TokenizerFactory tokFactory) {
		List<String[]> texts = new ArrayList<String[]>();
		
		TfIdfDistance tfIdfdist = new TfIdfDistance(tokFactory);
		
		HmmDecoder decoder = new HmmDecoder(PosTagger.getInstance().getHmm());
		
		
		String[] pronouns = {"i","me","my","mine","we","our","ours","myself","ourselves"};
		
		HashSet<String> pronounsSet = new HashSet<String>();
		for(String s : pronouns){
			pronounsSet.add(s);
		}
		
		for(String row[] : tweets){
			String text = row[TEXT_OFFSET];
			
			String[] newRow = new String[3];
			newRow[0] = row[0];
			newRow[1] = row[1];
			//newRow[2] = row[2];
			String tokens = removeNonContextualWords(text,tokFactory,tfIdfdist,decoder,pronounsSet);
			newRow[2] = tokens;
			//newRow[4] = row[4];
			
			texts.add(newRow);
			
		}
		
		HashMap<String,Double> termByIDF = new HashMap<String, Double>();
		
		for(String term : tfIdfdist.termSet()){
			termByIDF.put(term, tfIdfdist.idf(term));
		}
		
		Map<String, Double> sortedTermByIDF = sortByValue(termByIDF);
		
		int threshold = -1;
		
		for(String term : sortedTermByIDF.keySet()){
			if(threshold == -1){
				threshold = sortedTermByIDF.get(term).intValue();
			}
			
			if(sortedTermByIDF.get(term) >= threshold){
				System.out.println(term+" "+sortedTermByIDF.get(term));
			}else{
				break;
			}
			
		}
		
		return filteredTexts;
	}*/

	private static String removeUnfrequentNonContextualWords(String text,
			TokenizerFactory tokFactory, TfIdfDistance tfIdfdist, HashSet<String> pronounsSet, double threshold) {

		
		StringBuffer textWithoutNonContextualWords = new StringBuffer();
		
		List<String> tokenList = new ArrayList<String>();
		List<String> whiteList = new ArrayList<String>();
		Tokenizer tokenizer
		    = tokFactory.tokenizer(text.toCharArray(),
		                                  0,text.length());
		tokenizer.tokenize(tokenList,whiteList);
		
		String[] tokens = new String[tokenList.size()];
		String[] whites = new String[whiteList.size()];
		tokenList.toArray(tokens);
		whiteList.toArray(whites);		
		
		/*view tags in http://alias-i.com/lingpipe-3.9.3/docs/api/com/aliasi/corpus/parsers/BrownPosParser.html*/
		for (int i = 0; i < tokenList.size(); ++i){
			
			double idfValue = tfIdfdist.idf(tokenList.get(i));
			//tfIdfdist.
			
			if(pronounsSet.contains(tokenList.get(i))){
				textWithoutNonContextualWords.append(tokenList.get(i));
				textWithoutNonContextualWords.append(" ");
				System.out.println(tokenList.get(i)+" "+idfValue);
			}else if(idfValue > threshold){
				textWithoutNonContextualWords.append(tokenList.get(i));
				textWithoutNonContextualWords.append(" ");
				System.out.println(tokenList.get(i)+" "+idfValue);
			}
				
		}
		return textWithoutNonContextualWords.toString();
	
	}

	private static String removeNonContextualWords(String text, TokenizerFactory tokFactory, TfIdfDistance tfIdfdist, HmmDecoder decoder,HashSet<String> pronounsSet) {
		
		StringBuffer textWithoutNonContextualWords = new StringBuffer();
		
		List<String> tokenList = new ArrayList<String>();
		List<String> whiteList = new ArrayList<String>();
		Tokenizer tokenizer
		    = tokFactory.tokenizer(text.toCharArray(),
		                                  0,text.length());
		tokenizer.tokenize(tokenList,whiteList);
		
		String[] tokens = new String[tokenList.size()];
		String[] whites = new String[whiteList.size()];
		tokenList.toArray(tokens);
		whiteList.toArray(whites);
		
		Tagging<String> tagging = decoder.tag(tokenList);
		
		
		/*view tags in http://alias-i.com/lingpipe-3.9.3/docs/api/com/aliasi/corpus/parsers/BrownPosParser.html*/
		for (int i = 0; i < tagging.size(); ++i){
			if(tagging.tag(i).equals("nn") || pronounsSet.contains(tokenList.get(i)) || tagging.tag(i).equals("vbg")){
				tfIdfdist.handle(tokenList.get(i));
				textWithoutNonContextualWords.append(tokenList.get(i));
				textWithoutNonContextualWords.append("|");
				//System.out.println(tokenList.get(i)+" "+tagging.tag(i));
			}
				
		}
		return textWithoutNonContextualWords.toString();
	}


}
