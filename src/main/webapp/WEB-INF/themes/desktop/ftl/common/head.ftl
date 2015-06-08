<#include "../macro/removeTags.ftl" />
<#include "title/titleFormat.ftl" />
<#--allows for page specific css-->
<#macro pageCSS>

  <#if !cssFile?has_content>
    <#assign cssFile="screen.css"/>
  </#if>

  <@cssLink target="resource/css/${cssFile}"/>

</#macro>
<#--allows for external css to be brought in via the content repo-->
<#macro externalCSS>

  <#if externalCssFile??>
    <#list collectionsData.css_sources as css_source>
    <link rel="stylesheet" type="text/css"
          href="<@siteLink path='indirect/'/>${css_source}"/>
    </#list>
  </#if>

</#macro>
<#--allows for external MEta tags-->
<#macro externalMetaTags>
  <#--<#list collectionsData.meta_tag as meta_tag>-->
  <#--<meta name="${meta_tag.name}" content="${meta_tag.content}" />-->
<#--</#list>-->
</#macro>


<head prefix="og: http://ogp.me/ns#">
  <title><@titleFormat removeTags(title) /></title>

  <@pageCSS/>

  <@renderCssLinks />

  <@externalCSS />
    <!--[if IE 8]>
  <link rel="stylesheet" type="text/css" href="<@siteLink path="resource/css/ie.css" />"/>
    <![endif]-->

  <link media="print" rel="stylesheet" type="text/css"  href="<@siteLink path="resource/css/print.css"/>"/>
  <#-- hack to put make global vars accessible in javascript. Would be good to come up with a better solution -->
    <script type="text/javascript">
        var siteUrlPrefix = "<@siteLink path=''/>";
    </script>

  <script type="text/javascript" src="<@siteLink path="resource/js/vendor/modernizr-v2.7.1.js"/>"></script>
  <#-- //html5shiv. js and respond.js - enable the use of foundation's dropdowns to work in IE8 -->
  <#-- //The rem  polyfill rewrites the rems in to pixels. I don't think we can call this using the asset manager. -->
  <!--[if IE 8]>
  <script src="<@siteLink path="resource/js/vendor/html5shiv.js"/>"></script>
  <script src="<@siteLink path="resource/js/vendor/respond.min.js"/>"></script>
  <script src="<@siteLink path="resource/js/vendor/rem.min.js"/>"></script>
  <![endif]-->
  <link rel="shortcut icon" href="<@siteLink path="resource/img/favicon.ico"/>" type="image/x-icon"/>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

<#-- // TODO: NEED BACKEND
  <meta name="description" content="${freemarker_config.getMetaDescription(journalContext)}"/>
  <meta name="keywords" content="${freemarker_config.getMetaKeywords(journalContext)}"/>-->


<#--External MetaTags-->
<@externalMetaTags />

<#if article??>
<#-- // citation meta tags -->
<#include "../article/metaTags.ftl" />

</#if>
<#include "analytics.ftl" />

</head>
<#-- //references js that is foundational like jquery and foundation.js. JS output is printed at the bottom of the body.
-->
<#include "baseJs.ftl" />

