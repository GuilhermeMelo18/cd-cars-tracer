package br.cin.tbookmarks.recommender.database;

public final class MoviesCrossEventsBooksDataset extends AbstractCrossDomainDataset {

	private static final MoviesCrossEventsBooksDataset INSTANCE = new MoviesCrossEventsBooksDataset();
	
	private MoviesCrossEventsBooksDataset() {
		datasetURL = "\\resources\\datasets\\cross-domain\\contextual_movies_cross_events_books.dat";
		datasets.add(GroupLensDataset.getInstance());
		datasets.add(EventsTwitterDataset.getInstance());
		datasets.add(BooksTwitterDataset.getInstance());
		initializeCrossDomainDataset();
	}

	public static MoviesCrossEventsBooksDataset getInstance() {
		return INSTANCE;
	}

}
