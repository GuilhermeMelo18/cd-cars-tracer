package br.cin.tbookmarks.recommender.database.contextual.textmining;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.cin.tbookmarks.recommender.database.contextual.ContextualExtractorFromReviews.EntryReviewFile;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;


public class PosTagger {
	
	private static final String hmmModelPath = System.getProperty("user.dir")+"\\war\\WEB-INF\\resources\\pos-en-general-brown.HiddenMarkovModel";
	
	private static final TokenizerFactory TOKENIZER_FACTORY  = IndoEuropeanTokenizerFactory.INSTANCE;
	private static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
	private static HiddenMarkovModel hmm;

	private static PosTagger instance = null;
	   protected PosTagger() {
	      // Exists only to defeat instantiation.
		   
		   try {
			hmm = (HiddenMarkovModel) AbstractExternalizable.readObject(new File(hmmModelPath));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	   }
	   public static PosTagger getInstance() {
	      if(instance == null) {
	         instance = new PosTagger();
	      }
	      return instance;
	   }
	  
	   public static HiddenMarkovModel getHmm() {
		return hmm;
	}
	   
	   public static TokenizerFactory getTokenizerFactory() {
		return TOKENIZER_FACTORY;
	}
	   
	  private static int countWords(String s){

		    int wordCount = 0;

		    boolean word = false;
		    int endOfLine = s.length() - 1;

		    for (int i = 0; i < s.length(); i++) {
		        // if the char is a letter, word = true.
		        if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
		            word = true;
		            // if char isn't a letter and there have been letters before,
		            // counter goes up.
		        } else if (!Character.isLetter(s.charAt(i)) && word) {
		            wordCount++;
		            word = false;
		            // last word of String; if it doesn't end with a non letter, it
		            // wouldn't count without this.
		        } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
		            wordCount++;
		        }
		    }
		    return wordCount;
		}   
	   
	public void setTextMeasuresFromText(EntryReviewFile entry) throws ClassNotFoundException, IOException{
		
		String text = entry.getReviewText();
		
		List<String> tokenList = new ArrayList<String>();
		List<String> whiteList = new ArrayList<String>();
		Tokenizer tokenizer
		    = TOKENIZER_FACTORY.tokenizer(text.toCharArray(),
		                                  0,text.length());
		tokenizer.tokenize(tokenList,whiteList);
		
		String[] tokens = new String[tokenList.size()];
		String[] whites = new String[whiteList.size()];
		tokenList.toArray(tokens);
		whiteList.toArray(whites);
		int[] sentenceBoundaries
		    = SENTENCE_MODEL.boundaryIndices(tokens,whites);
		
		double logSentences = Math.log(sentenceBoundaries.length+1);
		double logWords= Math.log(countWords(entry.getReviewText())+1);
		
		HmmDecoder decoder = new HmmDecoder(hmm);
		Tagging<String> tagging = decoder.tag(tokenList);
		
		int vBDsumCounter= 0;
		int vsumCounter= 0;
		
		
		/*view tags in http://alias-i.com/lingpipe-3.9.3/docs/api/com/aliasi/corpus/parsers/BrownPosParser.html*/
		for (int i = 0; i < tagging.size(); ++i){
			if(tagging.tag(i).equals("vbd") ||
					tagging.tag(i).equals("bed") ||
					tagging.tag(i).equals("bedz") ||
					tagging.tag(i).equals("hvd")){
				vsumCounter++;
				vBDsumCounter++;
			}else if(tagging.tag(i).charAt(0) == 'v' ||
					tagging.tag(i).charAt(0) == 'b' ||
					tagging.tag(i).charAt(0) == 'h'){
				vsumCounter++;
			}
				
		}
		
		double vBDsum= Math.log(vBDsumCounter+1);
		double vsum= Math.log(vsumCounter+1);
		double vratio = (vsum > 0) ? vBDsum/vsum : 0;
		
		TextMeasures tm = new TextMeasures(logSentences, logWords, vBDsum, vsum, vratio);
		entry.setReviewTextMeasures(tm);
	}
	
	
	public static void main(String[] args) 
			throws ClassNotFoundException, IOException {
		TokenizerFactory tokFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		// = args.length > 0 ? args[0] : "WEB-INF/resources/pos-en-general-brown.HiddenMarkovModel";
		HiddenMarkovModel hmm = (HiddenMarkovModel) AbstractExternalizable.readObject(new File(hmmModelPath));
		HmmDecoder decoder = new HmmDecoder(hmm);
		BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("\n\nINPUT> ");
			System.out.flush();
			String input = bufReader.readLine();
			Tokenizer tokenizer = tokFactory.tokenizer(input.toCharArray(),0,input.length());
			String[] tokens = tokenizer.tokenize();
			List<String> tokenList = Arrays.asList(tokens);
			firstBest(tokenList,decoder);
			
			/*System.out.println(">>>>>> TagLattice:");
			
			TagLattice<String> lattice = decoder.tagMarginal(tokenList);
			for (int tokenIndex = 0; tokenIndex < tokenList.size(); ++tokenIndex) {
			    ConditionalClassification tagScores = lattice.tokenClassification(tokenIndex);
			    System.out.print(Integer.toString(tokenIndex));
			    System.out.print(tokenList.get(tokenIndex));
			    for (int i = 0; i < 4; ++i) {
			        double conditionalProb = tagScores.score(i);
			        String tag = tagScores.category(i);
			        System.out.print(" " + conditionalProb 
			                         + ":" +tag);
			    }
			    System.out.println();
			}*/
			
		}
	}

	static void firstBest(List<String> tokenList, HmmDecoder decoder) {
		Tagging<String> tagging = decoder.tag(tokenList);
		System.out.println("\nFIRST BEST");
		for (int i = 0; i < tagging.size(); ++i)
			System.out.print(tagging.token(i) + "_" + tagging.tag(i) + " ");
		System.out.println();
	}
	
	

	/*static void nBest(List<String> tokenList, HmmDecoder decoder, int maxNBest) {
		System.out.println("\nN BEST");
		System.out.println("#   JointLogProb         Analysis");
		Iterator<ScoredTagging<String>> nBestIt = decoder.tagNBest(tokenList,maxNBest);
		for (int n = 0; n < maxNBest && nBestIt.hasNext(); ++n) {
			ScoredTagging<String> scoredTagging = nBestIt.next();
			double score = scoredTagging.score();
			System.out.print(n + "   " + format(score) + "  ");
			for (int i = 0; i < tokenList.size(); ++i)
				System.out.print(scoredTagging.token(i) + "_" + pad(scoredTagging.tag(i),5));
			System.out.println();
		}        
	}

	static void confidence(List<String> tokenList, HmmDecoder decoder) {
		System.out.println("\nCONFIDENCE");
		System.out.println("#   Token          (Prob:Tag)*");
		TagLattice<String> lattice = decoder.tagMarginal(tokenList);
		for (int tokenIndex = 0; tokenIndex < tokenList.size(); ++tokenIndex) {
			ConditionalClassification tagScores = lattice.tokenClassification(tokenIndex);
			System.out.print(pad(Integer.toString(tokenIndex),4));
			System.out.print(pad(tokenList.get(tokenIndex),15));
			for (int i = 0; i < 5; ++i) {
				double conditionalProb = tagScores.score(i);
				String tag = tagScores.category(i);
				System.out.print(" " + format(conditionalProb) 
						+ ":" + pad(tag,4));
			}
			System.out.println();
		}
	}

	static String format(double x) {
		return String.format("%9.3f",x);
	}

	static String pad(String in, int length) {
		if (in.length() > length) return in.substring(0,length-3) + "...";
		if (in.length() == length) return in;
		StringBuilder sb = new StringBuilder(length);
		sb.append(in);
		while (sb.length() < length) sb.append(' ');
		return sb.toString();

	}
*/
}
