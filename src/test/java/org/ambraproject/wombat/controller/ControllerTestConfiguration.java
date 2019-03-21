package org.ambraproject.wombat.controller;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ambraproject.rhombat.gson.Iso8601DateAdapter;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.TestRuntimeConfiguration;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.HoneypotService;
import org.ambraproject.wombat.service.remote.*;
import org.ambraproject.wombat.util.JodaTimeLocalDateAdapter;
import org.ambraproject.wombat.util.ThemeTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebAppConfiguration
public class ControllerTestConfiguration {
  private static final RuntimeConfiguration runtimeConfiguration = new TestRuntimeConfiguration();

  protected static final String DESKTOP_PLOS_ONE = "DesktopPlosOne";

  @Bean
  protected Gson gson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeAdapter(Date.class, new Iso8601DateAdapter());
    builder.registerTypeAdapter(org.joda.time.LocalDate.class, JodaTimeLocalDateAdapter.INSTANCE);
    return builder.create();
  }

  @Bean
  protected JsonService jsonService() {
    return new JsonService();
  }

  @Bean
  protected RuntimeConfiguration runtimeConfiguration() {
    return runtimeConfiguration;
  }

  @Bean
  protected Charset charset() {
    return Charsets.UTF_8;
  }

  @Bean
  protected SiteResolver siteResolver() {
    return new SiteResolver();
  }

  @Bean
  protected DoiVersionArgumentResolver doiVersionArgumentResolver() {
    return new DoiVersionArgumentResolver();
  }

  @Bean
  protected RequestMappingContextDictionary handlerDirectory() {
    final RequestMappingContextDictionary handlerDirectory = new RequestMappingContextDictionary();
    return handlerDirectory;
  }

  @Bean
  protected HoneypotService honeypotService() {
    final HoneypotService honeypotService = mock(HoneypotService.class);
    return honeypotService;
  }

  @Bean
  protected UserApi userApi() {
    final UserApi userApi = mock(UserApi.class);
    return userApi;
  }

  @Bean
  protected CachedRemoteService<Reader> cachedRemoteReader() {
    @SuppressWarnings("unchecked")
    final CachedRemoteService<Reader> cachedRemoteReader = mock(CachedRemoteService.class);
    return cachedRemoteReader;
  }

  @Bean
  protected SolrSearchApi solrSearchApi() {
    final SolrSearchApi solrSearchApi = spy(SolrSearchApiImpl.class);
    return solrSearchApi;
  }

  @Bean
  protected CachedRemoteService<InputStream> cachedRemoteInputStream() {
    @SuppressWarnings("unchecked")
    final CachedRemoteService<InputStream> cachedRemoteReader = mock(CachedRemoteService.class);
    return cachedRemoteReader;
  }

  @Bean
  protected ArticleApi articleApi() {
    final ArticleApi articleApi = mock(ArticleApiImpl.class);
    return articleApi;
  }

  @Bean
  protected ArticleTransformService articleTransformService() {
    final ArticleTransformService articleTransformService = mock(ArticleTransformService.class);
    return articleTransformService;
  }

  @Bean
  protected Theme activeTheme() {
    final Theme theme = mock(ThemeTest.class);
    return theme;
  }

  /**
   *  Unit test can only work with a single site.
   */
  @Bean
  protected Site activeSite(Theme theme) {
    final SiteRequestScheme mockRequestScheme = mock(SiteRequestScheme.class);
    doAnswer(invocation -> {
      final Object[] args = invocation.getArguments();
      final HttpServletRequest request = (HttpServletRequest) args[0];
      return true;
    }).when(mockRequestScheme).isForSite(any(HttpServletRequest.class));


    final Site mockSite = mock(Site.class);
    when(mockSite.getRequestScheme()).thenReturn(mockRequestScheme);
    when(mockSite.getTheme()).thenReturn(theme);
    when(mockSite.getKey()).thenReturn(DESKTOP_PLOS_ONE);
    when(mockSite.toString()).thenReturn(DESKTOP_PLOS_ONE);
    when(mockSite.getJournalKey()).thenReturn(DESKTOP_PLOS_ONE);
    return mockSite;
  }

  @Bean
  protected SiteSet siteSet(Site site) {
    final SiteSet siteSet = mock(SiteSet.class);
    when(siteSet.getSites()).thenReturn(ImmutableSet.of(site));
    return siteSet;
  }
}
