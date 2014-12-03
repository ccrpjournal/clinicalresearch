package org.ambraproject.wombat.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

/**
 * A utility class for creation and management of HTTP messages
 */
public class HttpMessageUtil {

  /**
   * Describes how to filter or modify a header while copying a response.
   */
  public static interface HeaderFilter {
    /**
     * Return the header value to copy into the outgoing response, under the same header name as the incoming response.
     * Return {@code null} to copy no header with this name.
     * <p/>
     * To copy the header value unchanged, do {@code return header.getValue();}.
     *
     * @param header a header for an incoming response
     * @return the header value to copy into the outgoing response, or {@code null} to not copy this header
     */
    public String getValue(Header header);
  }

  /**
   * Copy content with whitelisted headers between responses
   *
   * @param responseFrom an incoming response to copy from
   * @param responseTo   an outgoing response to copy into
   * @param headerFilter describes whether and how to copy headers
   * @throws IOException
   */
  public static void copyResponseWithHeaders(HttpResponse responseFrom, HttpServletResponse responseTo,
                                             HeaderFilter headerFilter)
      throws IOException {
    for (Header header : responseFrom.getAllHeaders()) {
      String newValue = headerFilter.getValue(header);
      if (newValue != null) {
        responseTo.setHeader(header.getName(), newValue);
      }
    }
    copyResponse(responseFrom, responseTo);
  }

  /**
   * Copy content between responses
   * @param responseFrom
   * @param responseTo
   * @throws IOException
   */
  public static void copyResponse(HttpResponse responseFrom, HttpServletResponse responseTo)
          throws IOException {

    try (InputStream streamFromService = responseFrom.getEntity().getContent();
         OutputStream streamToClient = responseTo.getOutputStream()) {
      IOUtils.copy(streamFromService, streamToClient);
    }
  }

  /**
   * Return a list of headers from a request, using an optional whitelist
   *
   * @param request a request
   * @return its headers
   */
  public static Collection<Header> getRequestHeaders(HttpServletRequest request, ImmutableSet<String> headerWhitelist) {
    Enumeration headerNames = request.getHeaderNames();
    List<Header> headers = Lists.newArrayList();
    while (headerNames.hasMoreElements()) {
      String headerName = (String) headerNames.nextElement();
      if (headerWhitelist.contains(headerName)) {
        String headerValue = request.getHeader(headerName);
        headers.add(new BasicHeader(headerName, headerValue));
      }
    }
    return headers;
  }



  public static Collection<NameValuePair> getRequestParameters(HttpServletRequest request) {
    return getRequestParameters(request, Predicates.<String>alwaysTrue());
  }

  public static Collection<NameValuePair> getRequestParameters(HttpServletRequest request, Set<String> paramNames) {
    return getRequestParameters(request, Predicates.in(paramNames));
  }

  private static Collection<NameValuePair> getRequestParameters(HttpServletRequest request, Predicate<String> includeParam) {
    Preconditions.checkNotNull(includeParam);
    List<NameValuePair> paramList = new ArrayList<>();
    Enumeration allParamNames = request.getParameterNames();
    while (allParamNames.hasMoreElements()) {
      String paramName = (String) allParamNames.nextElement();
      if (includeParam.apply(paramName)) {
        paramList.add(new BasicNameValuePair(paramName, request.getParameter(paramName)));
      }
    }
    return paramList;
  }


  public static HttpUriRequest buildRequest(URI fullUrl, String method) {
    return buildRequest(fullUrl, method, ImmutableSet.<Header>of(), ImmutableSet.<NameValuePair>of());
  }


  public static HttpUriRequest buildRequest(URI fullUrl, String method,
                                            Collection<? extends NameValuePair> params,
                                            NameValuePair... additionalParams) {
    return buildRequest(fullUrl, method, ImmutableSet.<Header>of(), params, additionalParams);
  }


  public static HttpUriRequest buildRequest(URI fullUrl, String method,
                                            Collection<? extends Header> headers,
                                            Collection<? extends NameValuePair> params,
                                            NameValuePair... additionalParams) {
    RequestBuilder reqBuilder = RequestBuilder.create(method).setUri(fullUrl);
    Preconditions.checkNotNull(headers);
    Preconditions.checkNotNull(params);
    Preconditions.checkNotNull(additionalParams);
    for (Header header : headers) {
      reqBuilder.addHeader(header);
    }
    if (!params.isEmpty()) {
      reqBuilder.addParameters(params.toArray(new NameValuePair[params.size()]));
    }
    for (NameValuePair param : additionalParams) {
      reqBuilder.addParameter(param);
    }
    return reqBuilder.build();
  }


}