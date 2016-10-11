package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Mappings for requests for DOIs that belong to works of unknown type.
 */
@Controller
public class GeneralDoiController extends WombatController {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private CorpusContentApi corpusContentApi;
  @Autowired
  private ArticleResolutionService articleResolutionService;
  @Autowired
  private ArticleService articleService;

  @RequestMapping(name = "doi", value = "/doi")
  public RedirectView redirectFromDoi(HttpServletRequest request,
                                      @SiteParam Site site,
                                      RequestedDoiVersion id)
      throws IOException {
    return getRedirectFor(site, id).getRedirect(request);
  }

  Map<String, ?> getMetadataForDoi(RequestedDoiVersion id) throws IOException {
    ApiAddress address = ApiAddress.builder("dois").embedDoi(id.getDoi()).build();
    try {
      return articleApi.requestObject(address, Map.class);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException("DOI not found: " + id, e);
    }
  }

  private static final String ARTICLE = "article";
  private static final String ASSET = "asset";

  private String getTypeOf(RequestedDoiVersion id) throws IOException {
    Map<String, ?> metadata = getMetadataForDoi(id);
    String doiType = (String) metadata.get("type");
    if (doiType.equals(ARTICLE)) {
      return ARTICLE;
    } else if (!doiType.equals(ASSET)) {
      return doiType;
    }

    // The DOI belongs to an article asset. If it has one or more revisions, request the latest one to get its itemType.
    Map<String, ?> articleMetadata = (Map<String, ?>) metadata.get("article");
    Map<String, ?> revisionTable = (Map<String, ?>) articleMetadata.get("revisions");
    Optional<Integer> ingestionNumber = revisionTable.entrySet().stream()
        .max(Comparator.comparing(entry -> Integer.valueOf(entry.getKey()))) // find the latest revision
        .map(entry -> ((Number) entry.getValue()).intValue()); // extract its ingestion number
    if (!ingestionNumber.isPresent()) {
      // The article is unpublished. There is no particular ingestion whose itemType we should use.
      throw new NotFoundException();
    }
    String articleDoi = (String) articleMetadata.get("doi");
    Map<String, ?> itemView = articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleDoi)
            .addToken("ingestions").addToken(ingestionNumber.get().toString())
            .addToken("items").build(),
        Map.class);
    Map<String, ?> itemTable = (Map<String, ?>) itemView.get("items");
    String canonicalDoi = (String) metadata.get("doi");
    Map<String, ?> itemMetadata = (Map<String, ?>) itemTable.get(canonicalDoi);
    return Objects.requireNonNull((String) itemMetadata.get("itemType"));
  }

  private static final ImmutableMap<String, String> REDIRECT_HANDLERS = ImmutableMap.<String, String>builder()
      .put("volume", "browseVolumes")
      .put("issue", "browseIssues")
      .put("comment", "articleCommentTree")

      .put("article", "article")
      .put("figure", "figurePage")
      .put("table", "figurePage")
      // TODO: supp info

      .build();

  private Link getRedirectFor(Site site, RequestedDoiVersion id) throws IOException {
    String handlerName = REDIRECT_HANDLERS.get(getTypeOf(id));
    if (handlerName == null) {
      throw new RuntimeException("Unrecognized type: " + id);
    }
    Link.Factory.PatternBuilder handlerLink = Link.toLocalSite(site)
        .toPattern(requestMappingContextDictionary, handlerName);
    return pointLinkToDoi(handlerLink, id);
  }

  private static Link pointLinkToDoi(Link.Factory.PatternBuilder link, RequestedDoiVersion id) {
    link.addQueryParameter("id", id.getDoi());
    id.getRevisionNumber().ifPresent(revisionNumber ->
        link.addQueryParameter("rev", revisionNumber));
    return link.build();
  }

  @RequestMapping(name = "assetFile", value = "/article/file", params = {"type"})
  public void serveAssetFile(HttpServletResponse responseToClient,
                             @SiteParam Site site,
                             RequestedDoiVersion id,
                             @RequestParam(value = "type", required = true) String fileType,
                             @RequestParam(value = "download", required = false) String isDownload)
      throws IOException {
    AssetPointer asset = articleResolutionService.toParentIngestion(id);
    Map<String, ?> files = articleService.getItemFiles(asset);
    Map<String, ?> fileRepoKey = (Map<String, ?>) files.get(fileType);
    if (fileRepoKey == null) {
      String message = String.format("Unrecognized file type (\"%s\") for id: %s", fileType, id);
      throw new NotFoundException(message);
    }

    // TODO: Check visibility against site?

    ContentKey contentKey = createKey(fileRepoKey);
    try (CloseableHttpResponse responseFromApi = corpusContentApi.request(contentKey, ImmutableList.of())) {
      forwardAssetResponse(responseFromApi, responseToClient, booleanParameter(isDownload));
    }
  }

  private static ContentKey createKey(Map<String, ?> fileRepoKey) {
    // TODO: Account for bucket name
    String key = (String) fileRepoKey.get("crepoKey");
    UUID uuid = UUID.fromString((String) fileRepoKey.get("crepoUuid"));
    return ContentKey.createForUuid(key, uuid);
  }

}
