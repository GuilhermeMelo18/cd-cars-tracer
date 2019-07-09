package br.cin.tbookmarks.recommender.database;

public final class MoviesCrossBooksDataset extends AbstractCrossDomainDataset {

	private static final MoviesCrossBooksDataset INSTANCE = new MoviesCrossBooksDataset();
	
	private MoviesCrossBooksDataset() {
		datasetURL = "\\resources\\datasets\\cross-domain\\movies_cross_books_test_user_without_books_rating.dat";
		datasets.add(GroupLensDataset.getInstance());
		datasets.add(BooksTwitterDataset.getInstance());
		initializeCrossDomainDataset();
	}

	public static MoviesCrossBooksDataset getInstance() {
		return INSTANCE;
	}

}
