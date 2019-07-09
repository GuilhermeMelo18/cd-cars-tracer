package br.cin.tbookmarks.recommender.evaluation;

import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

final class StatsCallable implements Callable<Void> {
  
  private static final Logger log = LoggerFactory.getLogger(StatsCallable.class);
  
  private final Callable<Void> delegate;
  private final boolean logStats;
  private final RunningAverageAndStdDev timing;
  private final AtomicInteger noEstimateCounter;
  
  StatsCallable(Callable<Void> delegate,
                boolean logStats,
                RunningAverageAndStdDev timing,
                AtomicInteger noEstimateCounter) {
    this.delegate = delegate;
    this.logStats = logStats;
    this.timing = timing;
    this.noEstimateCounter = noEstimateCounter;
  }
  
  @Override
  public Void call() throws Exception {
    long start = System.currentTimeMillis();
    delegate.call();
    long end = System.currentTimeMillis();
    timing.addDatum(end - start);
    if (logStats) {
      Runtime runtime = Runtime.getRuntime();
      int average = (int) timing.getAverage();
      log.info("Average time per recommendation: {}ms", average);
      long totalMemory = runtime.totalMemory();
      long memory = totalMemory - runtime.freeMemory();
      log.info("Approximate memory used: {}MB / {}MB", memory / 1000000L, totalMemory / 1000000L);
      log.info("Unable to recommend in {} cases", noEstimateCounter.get());
    }
    return null;
  }

}
