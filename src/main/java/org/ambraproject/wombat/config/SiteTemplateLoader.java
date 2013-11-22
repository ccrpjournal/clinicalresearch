package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;

class SiteTemplateLoader extends DelegatingTemplateLoader {
  private static final Logger log = LoggerFactory.getLogger(SiteTemplateLoader.class);

  private final ImmutableMap<String, TemplateLoader> loaders; // mapped by site key

  SiteTemplateLoader(ServletContext servletContext, SiteSet siteSet) throws IOException {
    this.loaders = buildLoaders(servletContext, siteSet);
  }

  private static ImmutableMap<String, TemplateLoader> buildLoaders(ServletContext servletContext, SiteSet siteSet)
      throws IOException {
    ImmutableMap.Builder<String, TemplateLoader> builder = ImmutableMap.builder();

    // Add the loader for the application root page
    builder.put("", new WebappTemplateLoader(servletContext, "/WEB-INF/views/app/"));

    // Add loader for each site
    for (Site site : siteSet.getSites()) {
      Theme leaf = site.getTheme();

      List<TemplateLoader> loaders = Lists.newArrayList();
      for (Theme theme : leaf.getChain()) {
        loaders.add(theme.getTemplateLoader());
      }

      MultiTemplateLoader multiLoader = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()]));
      builder.put(site.getKey(), multiLoader);
    }

    return builder.build();
  }

  @Override
  protected TemplateLoader delegate(String key) {
    TemplateLoader loader = loaders.get(key);
    if (loader == null) {
      throw new RuntimeException("Key not matched: " + key);
    }
    return loader;
  }

}
