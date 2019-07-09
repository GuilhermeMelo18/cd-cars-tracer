package br.cin.tbookmarks.recommender.evaluation;

public class EvaluationConfig{
	private double trainingPercentage;
	private double datasetPercentage;
	private int top_n;
	private double relevantThresholdPrecisionRecall;
	private boolean enableFixedTestSeed;
	
	public EvaluationConfig(double trainingPercentage,
			double datasetPercentage, int top_n,
			double relevantThresholdPrecisionRecall,
			boolean enableFixedTestSeed) {
		this.trainingPercentage = trainingPercentage;
		this.datasetPercentage = datasetPercentage;
		this.top_n = top_n;
		this.relevantThresholdPrecisionRecall = relevantThresholdPrecisionRecall;
		this.enableFixedTestSeed = enableFixedTestSeed;
	}

	public double getTrainingPercentage() {
		return trainingPercentage;
	}

	public double getDatasetPercentage() {
		return datasetPercentage;
	}

	public int getTop_n() {
		return top_n;
	}

	public double getRelevantThresholdPrecisionRecall() {
		return relevantThresholdPrecisionRecall;
	}

	public boolean isEnableFixedTestSeed() {
		return enableFixedTestSeed;
	}
	
	
	
}
