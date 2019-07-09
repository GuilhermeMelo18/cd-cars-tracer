package br.cin.tbookmarks.recommender.database.contextual.textmining;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import br.cin.tbookmarks.recommender.database.contextual.ContextualExtractorFromReviews.EntryReviewFile;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.util.AbstractExternalizable;


public class ClusterReviews {
	 private int NUM_CLUSTERS;    // Total clusters.
	    private int TOTAL_DATA;      // Total data points.
	    
	    private ArrayList<EntryReviewFile> reviewList;
	    
	    private double SAMPLES[][] /*= {{3.26, 6.17,3.29,4.42,0.74}, 
											    	{1.95, 4.36,1.95,2.64,0.74}, 
											    	{1.39, 3.37,0.0,1.61,0.0}, 
											    	{1.10, 4.02,0.0,2.08,0.0}, 
											    	{2.08, 4.65,1.38,2.99,0.46}, 
											    	{2.08, 5.52,1.09,3.66,0.29}, 
											    	{1.39, 4.09,1.79,2.48,0.72}}*/;
	    
	    private ArrayList<DataTextMeasures> dataSet = new ArrayList<DataTextMeasures>();
	    private ArrayList<TextMeasures> centroids = new ArrayList<TextMeasures>();
	    
	    public ClusterReviews(ArrayList<EntryReviewFile> reviewList, int numClusters,TextMeasures min, TextMeasures max) {
			this.reviewList = reviewList;
	    	
	    	this.NUM_CLUSTERS = numClusters;
			this.TOTAL_DATA = reviewList.size();
			
			SAMPLES = new double[this.TOTAL_DATA][5];
			
			for(int i=0; i< this.TOTAL_DATA;i++){
				SAMPLES[i][0] = reviewList.get(i).getReviewTextMeasures().getLogSentences();
				SAMPLES[i][1] = reviewList.get(i).getReviewTextMeasures().getLogWords();
				SAMPLES[i][2] = reviewList.get(i).getReviewTextMeasures().getVBDsum();
				SAMPLES[i][3] = reviewList.get(i).getReviewTextMeasures().getVsum();
				SAMPLES[i][4] = reviewList.get(i).getReviewTextMeasures().getVratio();
			}
			
			initialize(min, max);
		}
	    	    
	    private void initialize(TextMeasures min, TextMeasures max)
	    {
	        System.out.println("Centroids initialized at:");
	        centroids.add(min); // lowest set.
	        centroids.add(max); // highest set.
	        System.out.println("     (" + centroids.get(0).toString() + ")");
	        System.out.println("     (" + centroids.get(1).toString() + ")");
	        System.out.print("\n");
	        return;
	    }
	    
	    public void kMeanCluster(ArrayList<EntryReviewFile> specific, ArrayList<EntryReviewFile> generic)
	    {
	        final double bigNumber = Math.pow(10, 10);    // some big number that's sure to be larger than our data range.
	        double minimum = bigNumber;                   // The minimum value to beat. 
	        double distance = 0.0;                        // The current minimum value.
	        int sampleNumber = 0;
	        int cluster = 0;
	        boolean isStillMoving = true;
	        DataTextMeasures newData = null;
	        
	        // Add in new data, one at a time, recalculating centroids with each new one. 
	        while(dataSet.size() < TOTAL_DATA)
	        {
	            newData = new DataTextMeasures(SAMPLES[sampleNumber][0], SAMPLES[sampleNumber][1], SAMPLES[sampleNumber][2], SAMPLES[sampleNumber][3], SAMPLES[sampleNumber][4]);
	            dataSet.add(newData);
	            minimum = bigNumber;
	            for(int i = 0; i < NUM_CLUSTERS; i++)
	            {
	                distance = dist(newData, centroids.get(i));
	                if(distance < minimum){
	                    minimum = distance;
	                    cluster = i;
	                }
	            }
	            newData.cluster(cluster);
	            
	            // calculate new centroids.
	            for(int i = 0; i < NUM_CLUSTERS; i++)
	            {
	                int totalLogS = 0;
	                int totalLogW = 0;
	                int totalVBDsum = 0;
	                int totalVsum = 0;
	                int totalVratio = 0;
	                
	                int totalInCluster = 0;
	                for(int j = 0; j < dataSet.size(); j++)
	                {
	                    if(dataSet.get(j).cluster() == i){
	                        totalLogS += dataSet.get(j).getTm().getLogSentences();
	                        totalLogW += dataSet.get(j).getTm().getLogWords();
	                        
	                        totalVBDsum += dataSet.get(j).getTm().getVBDsum();
	                        totalVsum += dataSet.get(j).getTm().getVsum();
	                        totalVratio += dataSet.get(j).getTm().getVratio();
	                        
	                        totalInCluster++;
	                    }
	                }
	                if(totalInCluster > 0){
	                    centroids.get(i).setLogSentences(totalLogS / totalInCluster);
	                    centroids.get(i).setLogWords(totalLogW / totalInCluster);
	                    
	                    centroids.get(i).setVBDsum(totalVBDsum / totalInCluster);
	                    centroids.get(i).setVsum(totalVsum / totalInCluster);
	                    centroids.get(i).setVratio(totalVratio / totalInCluster);
	                }
	            }
	            sampleNumber++;
	        }
	        
	        // Now, keep shifting centroids until equilibrium occurs.
	        while(isStillMoving)
	        {
	            // calculate new centroids.
	            for(int i = 0; i < NUM_CLUSTERS; i++)
	            {
	            	int totalLogS = 0;
	                int totalLogW = 0;
	                int totalVBDsum = 0;
	                int totalVsum = 0;
	                int totalVratio = 0;
	                
	                int totalInCluster = 0;
	                for(int j = 0; j < dataSet.size(); j++)
	                {
	                    if(dataSet.get(j).cluster() == i){
	                        totalLogS += dataSet.get(j).getTm().getLogSentences();
	                        totalLogW += dataSet.get(j).getTm().getLogWords();
	                        
	                        totalVBDsum += dataSet.get(j).getTm().getVBDsum();
	                        totalVsum += dataSet.get(j).getTm().getVsum();
	                        totalVratio += dataSet.get(j).getTm().getVratio();
	                        
	                        totalInCluster++;
	                    }
	                }
	                if(totalInCluster > 0){
	                    centroids.get(i).setLogSentences(totalLogS / totalInCluster);
	                    centroids.get(i).setLogWords(totalLogW / totalInCluster);
	                    
	                    centroids.get(i).setVBDsum(totalVBDsum / totalInCluster);
	                    centroids.get(i).setVsum(totalVsum / totalInCluster);
	                    centroids.get(i).setVratio(totalVratio / totalInCluster);
	                }
	            }
	            
	            // Assign all data to the new centroids
	            isStillMoving = false;
	            
	            for(int i = 0; i < dataSet.size(); i++)
	            {
	                DataTextMeasures tempData = dataSet.get(i);
	                minimum = bigNumber;
	                for(int j = 0; j < NUM_CLUSTERS; j++)
	                {
	                    distance = dist(tempData, centroids.get(j));
	                    if(distance < minimum){
	                        minimum = distance;
	                        cluster = j;
	                    }
	                }
	                tempData.cluster(cluster);
	                if(tempData.cluster() != cluster){
	                    tempData.cluster(cluster);
	                    isStillMoving = true;
	                }
	            }
	        }
	        
	        int i=0;
	        
	        //System.out.println("Cluster " + i + " includes:");
            for(int j = 0; j < TOTAL_DATA; j++)
            {
                if(dataSet.get(j).cluster() == i){
                	
                	if(!this.reviewList.get(j).getReviewTextMeasures().equals(dataSet.get(j).getTm())){
                		System.err.println("Incorrect clustering: "+this.reviewList.get(j).getSummaryText());
                	}
                	
                	//System.out.print(this.reviewList.get(j).getSummaryText());
                    //System.out.println("     (" + dataSet.get(j).toString() + ")");
                    generic.add(this.reviewList.get(j));
                }
            } // j
            //System.out.println();
            
            i++;
            //System.out.println("Cluster " + i + " includes:");
            for(int j = 0; j < TOTAL_DATA; j++)
            {
                if(dataSet.get(j).cluster() == i){
                	
                	if(!this.reviewList.get(j).getReviewTextMeasures().equals(dataSet.get(j).getTm())){
                		System.err.println("Incorrect clustering: "+this.reviewList.get(j).getSummaryText());
                	}
                	
                	//System.out.print(this.reviewList.get(j).getSummaryText());
                    //System.out.println("     (" + dataSet.get(j).toString() + ")");
                    specific.add(this.reviewList.get(j));
                }
            } // j
            //System.out.println();
            
         // Print out centroid results.
	        System.out.println("Centroids finalized at:");
	        for(int k = 0; k < NUM_CLUSTERS; k++)
	        {
	            System.out.println("     (" + centroids.get(k).toString());
	        }
	        System.out.print("\n");
	        
	        return;
	    }
	    
	    /**
	     * // Calculate Euclidean distance.
	     * @param d - Data object.
	     * @param c - Centroid object.
	     * @return - double value.
	     */
	    private static double dist(DataTextMeasures d, TextMeasures c) {
	    	TextMeasures tmD = d.getTm();
	        return Math.sqrt(Math.pow((c.getLogSentences() - tmD.getLogSentences()), 2) +
	        				Math.pow((c.getLogWords() - tmD.getLogWords()), 2) +
	        				Math.pow((c.getVBDsum() - tmD.getVBDsum()), 2) +
	        				Math.pow((c.getVsum() - tmD.getVsum()), 2) +
	        				Math.pow((c.getVratio() - tmD.getVratio()), 2));
	    }
	    
	    private static class DataTextMeasures
	    {
	    	private TextMeasures tm;
	        private int mCluster = 0;
	        
	        public DataTextMeasures()
	        {
	            return;
	        }
	        
	        public DataTextMeasures(double logSentences, double logWords, double vBDsum,
	    			double vsum, double vratio)
	        {
	            this.tm = new TextMeasures(logSentences, logWords, vBDsum, vsum, vratio);
	            return;
	        }
	        
	        public void cluster(int clusterNumber)
	        {
	            this.mCluster = clusterNumber;
	            return;
	        }
	        
	        public int cluster()
	        {
	            return this.mCluster;
	        }
	        
	        public TextMeasures getTm() {
				return tm;
			}
	        
	        @Override
	        public String toString() {
	        	return tm.toString();
	        }
	    }
	    
	    /*public static void main(String[] args)
	    {
	        initialize();
	        kMeanCluster();
	        
	        // Print out clustering results.
	        for(int i = 0; i < NUM_CLUSTERS; i++)
	        {
	            System.out.println("Cluster " + i + " includes:");
	            for(int j = 0; j < TOTAL_DATA; j++)
	            {
	                if(dataSet.get(j).cluster() == i){
	                    System.out.println("     (" + dataSet.get(j).toString() + ")");
	                }
	            } // j
	            System.out.println();
	        } // i
	        
	        // Print out centroid results.
	        System.out.println("Centroids finalized at:");
	        for(int i = 0; i < NUM_CLUSTERS; i++)
	        {
	            System.out.println("     (" + centroids.get(i).toString());
	        }
	        System.out.print("\n");
	        return;
	    }*/
	
}

