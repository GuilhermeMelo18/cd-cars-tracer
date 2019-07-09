package br.cin.tbookmarks.recommender.database.contextual.textmining;

public class TextMeasures{
	double logSentences;
	double logWords;
	double VBDsum;
	double Vsum;
	double Vratio;
	public TextMeasures(double logSentences, double logWords, double vBDsum,
			double vsum, double vratio) {
		super();
		this.logSentences = logSentences;
		this.logWords = logWords;
		VBDsum = vBDsum;
		Vsum = vsum;
		Vratio = vratio;
	}
	public double getLogSentences() {
		return logSentences;
	}

	public double getLogWords() {
		return logWords;
	}

	public double getVBDsum() {
		return VBDsum;
	}

	public double getVsum() {
		return Vsum;
	}
	public double getVratio() {
		return Vratio;
	}
	public void setLogSentences(double logSentences) {
		this.logSentences = logSentences;
	}
	public void setLogWords(double logWords) {
		this.logWords = logWords;
	}
	public void setVBDsum(double vBDsum) {
		VBDsum = vBDsum;
	}
	public void setVsum(double vsum) {
		Vsum = vsum;
	}
	public void setVratio(double vratio) {
		Vratio = vratio;
	}
	
	@Override
	public String toString() {
		return "LogS: "+logSentences+
				", LogW: "+logWords+
				", VBDsum: "+VBDsum+
				", Vsum: "+Vsum+
				", Vratio: "+Vratio;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TextMeasures){
			
			TextMeasures tm = (TextMeasures) obj;
			
			if(this.getLogSentences() == tm.getLogSentences() &&
        			this.getLogWords() == tm.getLogWords() &&
        			this.getVBDsum() == tm.getVBDsum() &&
        			this.getVsum() == tm.getVsum() &&
        			this.getVratio() == tm.getVratio()){
				return true;
			}
		}
		
		return false;
	}
	
}