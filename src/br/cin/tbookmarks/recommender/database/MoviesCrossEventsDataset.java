package br.cin.tbookmarks.recommender.database;

public final class MoviesCrossEventsDataset extends AbstractCrossDomainDataset {

	private static final MoviesCrossEventsDataset INSTANCE = new MoviesCrossEventsDataset();

	private MoviesCrossEventsDataset() {

		datasetURL = "\\resources\\datasets\\cross-domain\\movies_cross_events_test_user_without_events_rating.dat";
		datasets.add(GroupLensDataset.getInstance());
		datasets.add(EventsTwitterDataset.getInstance());

		initializeCrossDomainDataset();

	}

	public static MoviesCrossEventsDataset getInstance() {
		return INSTANCE;
	}

}
