/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.ambraproject.wombat.service.remote.SolrSearchApi.MAXIMUM_SOLR_RESULT_COUNT;

public class RecentArticleServiceImpl implements RecentArticleService {
  private static final Logger log = LoggerFactory.getLogger(RecentArticleServiceImpl.class);

  //todo: turn this field into a flag in homepage.yaml instead of treating as an article type.
  //The current usage of this wildcard behaves like so: If the specified article types does not meet
  //the minimum, fill the rest of the list with articles of any type.
  private static final String ARTICLE_TYPE_WILDCARD = "*";

  @Autowired
  private SolrSearchApi solrSearchApi;
  @Autowired
  private Cache cache;

  /**
   * Select a random subset of elements and shuffle their order.
   * <p>
   * The argument {@code sequence} is not mutated.
   * <p>
   * This is more efficient than using {@link Collections#shuffle(List)} if {@code n} is much smaller than {@code
   * sequence.size()}, as it shuffles only part of the list.
   *
   * @param elements a collection of elements
   * @param n        the number of elements to select and return shuffled
   * @return a list of {@code n} elements selected at random from {@code sequence} and in a random order
   */
  private <T> List<T> shuffleSubset(Collection<? extends T> elements, int n) {
    List<T> sequence = new ArrayList<>(elements);
    final int size = sequence.size();
    Preconditions.checkArgument(size >= n);

    for (int i = 0; i < n; i++) {
      int swapTarget = i + ThreadLocalRandom.current().nextInt(size - i);
      Collections.swap(sequence, i, swapTarget);
    }
    return sequence.subList(0, n);
  }

  @Override
  public List<SolrArticleAdapter> getRecentArticles(Site site,
                                                    int articleCount,
                                                    double numberOfDaysAgo,
                                                    boolean shuffle,
                                                    List<String> articleTypes,
                                                    List<String> articleTypesToExclude,
                                                    Optional<Integer> cacheDuration)
      throws IOException {
    String journalKey = site.getJournalKey();
    String cacheKey = "recentArticles:" + journalKey;
    List<SolrArticleAdapter> articles = null;
    if (cacheDuration.isPresent()) {
      articles = cache.get(cacheKey); // remains null if not cached
    }
    if (articles == null) {
      articles = retrieveRecentArticles(journalKey, articleCount, numberOfDaysAgo, articleTypes, articleTypesToExclude, site);
      if (cacheDuration.isPresent()) {
        /*
         * Casting to Serializable relies on all data structures that Gson uses to be serializable, which is safe
         * enough. We could avoid the cast with a shallow copy to a serializable List, but we would still rely on all
         * nested Lists and Maps being serializable. We'd rather avoid a deep copy until it's necessary.
         */
        cache.put(cacheKey, (Serializable) articles, cacheDuration.get());
      }
    }

    if (articles.size() > articleCount) {
      articles = shuffle ? shuffleSubset(articles, articleCount) : articles.subList(0, articleCount);
    }

    /*
     * Returning this object, we rely on the caller not to modify the contents, as documented for
     * RecentArticleService.getRecentArticles. Depending on cache implementation and whether we made a copy to shuffle,
     * mutating the returned object (or its contents) could disrupt future calls to this method. Merely wrapping the
     * return value in java.util.Collections.unmodifiableList would (similar to the serializability thing above) leave
     * nested Lists and Maps mutable. Let's not recursively wrap every data structure until it's necessary.
     */
    return articles;
  }

  private List<SolrArticleAdapter> retrieveRecentArticles(String journalKey,
                                                          int articleCount,
                                                          double numberOfDaysAgo,
                                                          List<String> articleTypes,
                                                          List<String> articleTypesToExclude,
                                                          Site site)
      throws IOException {

    List<String> journalKeys = ImmutableList.of(journalKey);

    LocalDate startDate = LocalDate.now().minusDays((long) numberOfDaysAgo);
    SolrSearchApiImpl.SolrExplicitDateRange dateRange = new SolrSearchApiImpl.SolrExplicitDateRange
        ("Recent Articles", startDate.toString(), LocalDate.now().toString());

    Set<String> uniqueDois = new HashSet<>();

    // Query for each article type separately and concatenate the results,
    // in order to preserve the "preference order" in the articleTypes list.
    List<SolrArticleAdapter> articles = new ArrayList<>();
    for (String articleType : articleTypes) {
      List<SolrArticleAdapter> recentArticles;
      if (articleType.equals(ARTICLE_TYPE_WILDCARD)) {
        recentArticles = getAllRecentArticles(articleTypesToExclude, journalKeys, dateRange, site);
      } else {
        recentArticles = getRecentArticlesByType(articleType, articleTypesToExclude,
            journalKeys, dateRange, site);
      }

      // Add each query result to 'results' only if the DOI is not already in 'uniqueDois'
      for (SolrArticleAdapter article : recentArticles) {
        if (uniqueDois.add(article.getDoi())) {
          articles.add(article);
        }
      }
    }

    if (articles.size() < articleCount) {
      if (articleTypes.size() > 1 && !articleTypes.contains(ARTICLE_TYPE_WILDCARD)) {
        String errorMessage = "" +
            "Recent articles results did not contain more articles than the minimum number " +
            "configured via the resultCount parameter. To make a valid query, alter homepage.yaml: " +
            "(1) use no more than one article type in the articleType list, or " +
            "(2) use the wildcard type parameter (*).";
        throw new RuntimeException(errorMessage);
      } else {

        // the results of the new query may contain previously added articles
        int limit = articleCount + articles.size();

        // Not enough results. Get outside the date range in order to meet the minimum.
        // Ignore order of articleTypes and get the union of all.
        List<SolrArticleAdapter> articlesByType = getAllArticlesByType(articleTypes, articleTypesToExclude,
            journalKeys, limit, site);
        for (SolrArticleAdapter article : articlesByType) {
          if (articles.size() < articleCount && uniqueDois.add(article.getDoi())) {
            articles.add(article);
          }
        }
      }
    }
    return articles;
  }

  private List<SolrArticleAdapter> getRecentArticlesByType(String articleType,
                                                           List<String> articleTypesToExclude,
                                                           List<String> journalKeys,
                                                           SolrSearchApiImpl.SolrExplicitDateRange dateRange,
                                                           Site site)
      throws IOException {
    ArticleSearchQuery recentArticleSearchQuery = ArticleSearchQuery.builder()
        .setStart(0)
        .setRows(MAXIMUM_SOLR_RESULT_COUNT)
        .setSortOrder(SolrSearchApiImpl.SolrSortOrder.DATE_NEWEST_FIRST)
        .setArticleTypes(ImmutableList.of(articleType))
        .setArticleTypesToExclude(articleTypesToExclude)
        .setDateRange(dateRange)
        .setJournalKeys(journalKeys)
        .build();

    Map<String, ?> results = solrSearchApi.search(recentArticleSearchQuery, site);
    return SolrArticleAdapter.unpackSolrQuery(results);
  }

  private List<SolrArticleAdapter> getAllRecentArticles(List<String> articleTypesToExclude,
                                                        List<String> journalKeys,
                                                        SolrSearchApiImpl.SolrExplicitDateRange dateRange,
                                                        Site site)
      throws IOException {
    ArticleSearchQuery recentArticleSearchQuery = ArticleSearchQuery.builder()
        .setStart(0)
        .setRows(MAXIMUM_SOLR_RESULT_COUNT)
        .setSortOrder(SolrSearchApiImpl.SolrSortOrder.DATE_NEWEST_FIRST)
        .setArticleTypesToExclude(articleTypesToExclude)
        .setDateRange(dateRange)
        .setJournalKeys(journalKeys)
        .build();

    Map<String, ?> results = solrSearchApi.search(recentArticleSearchQuery, site);
    return SolrArticleAdapter.unpackSolrQuery(results);
  }

  private List<SolrArticleAdapter> getAllArticlesByType(List<String> articleTypes,
                                                        List<String> articleTypesToExclude,
                                                        List<String> journalKeys,
                                                        int limit, Site site)
      throws IOException {
    ArticleSearchQuery allArticleSearchQuery = ArticleSearchQuery.builder()
        .setStart(0)
        .setRows(limit)
        .setSortOrder(SolrSearchApiImpl.SolrSortOrder.DATE_NEWEST_FIRST)
        .setArticleTypes(articleTypes)
        .setArticleTypesToExclude(articleTypesToExclude)
        .setJournalKeys(journalKeys)
        .build();
    return SolrArticleAdapter.unpackSolrQuery(solrSearchApi.search(allArticleSearchQuery, site));
  }

}
