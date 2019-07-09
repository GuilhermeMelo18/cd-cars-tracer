package br.cin.tbookmarks.recommender.database.contextual.textmining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import br.cin.tbookmarks.recommender.database.contextual.ContextualExtractorFromReviews;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.spell.TfIdfDistance;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.TokenLengthTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Strings;

public class LDA {
	
	static int ANNOTATION_OFFSET = 2;
	static int TEXT_OFFSET = 3;
	static int NUM_FOLDS = 4;

	static String[] CATEGORIES;

	static List<String[]> getCsvData(String file)  {
		
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

	/*public static void main(String[] args) throws Exception {
		String inFile = args.length > 0 ? args[0] : ContextualExtractorFromReviews.booksGenericFile;
        File corpusFile = new File(inFile);
        List<String[]> tweets = getCsvData(inFile);
        int minTokenCount = 1;
        //short numTopics = 90;
        double documentTopicPrior = .1;
        double wordPrior = 0.01;
        int burninEpochs = 0;
        int sampleLag = 1;
        int numSamples = 2000;
        long randomSeed = 6474835;
        SymbolTable symbolTable = new MapSymbolTable();
        TokenizerFactory tokFactory = new RegExTokenizerFactory("[^\\s]+"); 
        //tokFactory  = new IndoEuropeanTokenizerFactory(tokFactory);       
        
        tokFactory  = new LowerCaseTokenizerFactory(tokFactory);
        // tokFactory = new EnglishStopTokenizerFactory(tokFactory);
         //tokFactory = new Punc(tokFactory);
         //tokFactory = new PorterStemmerTokenizerFactory(tokFactory);
        
        tweets = Util.extractContextualPossibleWords(tweets,tokFactory);
        
        //tweets = Util.filterJaccard(tweets, tokFactory, .5);
        // tweets = Util.filterTFIDF(tweets, tokFactory, .5);
        System.out.println("Input file=" + corpusFile);
        System.out.println("Minimum token count=" + minTokenCount);
        //System.out.println("Number of topics=" + numTopics);
        System.out.println("Topic prior in docs=" + documentTopicPrior);
        System.out.println("Word prior in topics=" + wordPrior);
        System.out.println("Burnin epochs=" + burninEpochs);
        System.out.println("Sample lag=" + sampleLag);
        System.out.println("Number of samples=" + numSamples);
        // reportCorpus(articleTexts);
        String[] ldaTexts = new String[tweets.size()];
        for (int i = 0; i < tweets.size(); ++i) {
        	ldaTexts[i] = tweets.get(i)[Util.TEXT_OFFSET];
        }
        System.out.println("##########Got " + ldaTexts.length);
        int[][] docTokens
            = LatentDirichletAllocation
            .tokenizeDocuments(ldaTexts,tokFactory,symbolTable,minTokenCount);
        System.out.println("Number of unique words above count threshold=" + symbolTable.numSymbols());
        short numTopics = (short) symbolTable.numSymbols();
        
        int numTokens = 0;
        for (int[] tokens : docTokens) {
            numTokens += tokens.length;
        }
        System.out.println("Tokenized.  #Tokens After Pruning=" + numTokens);

        LdaReportingHandler handler
            = new LdaReportingHandler(symbolTable);

        LatentDirichletAllocation.GibbsSample sample
            = LatentDirichletAllocation
            .gibbsSampler(docTokens,
                          numTopics,
                          documentTopicPrior,
                          wordPrior,
                          burninEpochs,
                          sampleLag,
                          numSamples,
                          new Random(randomSeed),
                          handler);

        int maxWordsPerTopic = 1;
        int maxTopicsPerDoc = 10;
        boolean reportTokens = true;
        //handler.reportTopics(sample,maxWordsPerTopic,maxTopicsPerDoc,reportTokens);
        handler.reportDocuments(sample, maxWordsPerTopic, maxTopicsPerDoc, reportTokens);
    }*/
}

