package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.SoaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;

public class RecentArticleServiceImpl implements RecentArticleService {
  private static final Logger log = LoggerFactory.getLogger(RecentArticleServiceImpl.class);

  @Autowired
  private SoaService soaService;

  /*
   * This could be injected as a bean instead if needed.
   *
   * Does not need to be SecureRandom. Although it is important for feature correctness that we use a fair random
   * distribution, there is no security risk if the randomness becomes predictable.
   */
  private final Random random = new Random();

  private List<Object> getArticlesFromLastDays(String journalKey, double numberOfDays) throws IOException {
    String query = String.format("articles?journal=%s&publishedWithin=%f", journalKey, numberOfDays);
    return soaService.requestObject(query, ArrayList.class); // ArrayList is important; see shuffleSubset
  }

  /**
   * Select a random subset of elements and shuffle their order.
   * <p/>
   * The argument {@code sequence} must be mutable and {@link RandomAccess}. Its order will be clobbered by this method,
   * and the returned list will be a view into it.
   * <p/>
   * This is more efficient than using {@link Collections#shuffle(List)} if {@code n} is much smaller than {@code
   * sequence.size()}, as it shuffles only part of the list.
   *
   * @param sequence a mutable list of elements
   * @param n        the number of elements to select and return shuffled
   * @return a list of {@code n} elements selected at random from {@code sequence} and in a random order
   */
  private <T> List<T> shuffleSubset(List<T> sequence, int n) {
    final int size = sequence.size();
    Preconditions.checkArgument(size >= n);

    if (!(sequence instanceof RandomAccess)) {
      log.warn("Expected list to be RandomAccess; re-creating as ArrayList");
      sequence = new ArrayList<>(sequence);
    }

    for (int i = 0; i < n; i++) {
      int swapTarget = i + random.nextInt(size - i);
      Collections.swap(sequence, i, swapTarget);
    }
    return sequence.subList(0, n);
  }

  @Override
  public List<Object> getRecentArticles(Site site, int articleCount,
                                        double shuffleDuration, double defaultDuration)
      throws IOException {
    Preconditions.checkArgument(shuffleDuration <= defaultDuration);
    String journalKey = site.getJournalKey();
    List<Object> articles = getArticlesFromLastDays(journalKey, shuffleDuration);
    if (articles.size() >= articleCount) {
      // Because there were enough articles below the shuffling threshold, return a random selection of them
      return shuffleSubset(articles, articleCount);
    } else {
      // Not enough articles to shuffle. Return a number up to articleCount in chronological order.
      articles = getArticlesFromLastDays(journalKey, defaultDuration);
      return (articles.size() > articleCount) ? articles.subList(0, articleCount) : articles;
    }
  }

}
