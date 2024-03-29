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

package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.ambraproject.wombat.util.MockSiteUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = TestSpringConfiguration.class)
public class SearchControllerTest extends AbstractJUnit4SpringContextTests {
  @Autowired
  private SiteSet siteSet;

  @Test
  public void testCommonParams() throws IOException {
    Map<String, List<String>> params = new HashMap<>();
    params.put("page", Collections.singletonList("7"));
    params.put("filterSubjects", Arrays.asList("subject1", "subject2"));

    Site site = MockSiteUtil.getByUniqueJournalKey(siteSet, "journal1Key");
    SearchController.CommonParams commonParams = new SearchController.CommonParams(siteSet, site);
    commonParams.parseParams(params, false);

    assertEquals(90, commonParams.start);  // Default results per page should be 15
    assertEquals(SolrSearchApiImpl.SolrSortOrder.RELEVANCE, commonParams.sortOrder);
    assertEquals(SolrSearchApiImpl.SolrEnumeratedDateRange.ALL_TIME, commonParams.dateRange);
    assertTrue(commonParams.articleTypes.isEmpty());
    assertTrue(commonParams.journalKeys.isEmpty());
    assertTrue(commonParams.filterJournalNames.isEmpty());
    assertEquals(2, commonParams.subjectList.size());
    assertEquals("subject1", commonParams.subjectList.get(0));
    assertEquals("subject2", commonParams.subjectList.get(1));
    assertTrue(commonParams.isFiltered);
  }
}
