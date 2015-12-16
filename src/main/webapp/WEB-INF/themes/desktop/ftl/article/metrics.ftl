<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">


<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />
<#assign cssFile="metrics.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />

<#include "analyticsArticleJS.ftl" />

<body class="article ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">
<#include "articleHeader.ftl" />
    <section class="metrics-body">
    <#include "tabs.ftl" />
    <@displayTabList 'metrics' />
    <#include "metricsBody.ftl" />
    </section>
    <aside class="article-aside">
    <#include "aside/sidebar.ftl" />
    </aside>
</div>

<#include "articleJs.ftl" />

<@js src="resource/js/components/table_open.js"/>
<@js src="resource/js/components/figshare.js"/>
<@js src="resource/js/components/tooltip_hover.js"/>

<@js src="resource/js/vendor/jquery.jsonp-2.4.0.js"/>
<@js src="resource/js/vendor/hover-enhanced.js"/>
<@js src="resource/js/highcharts.js"/>

<@js src="resource/js/metrics.js"/>
<@js src="resource/js/pages/article.js"/>
<#include "../common/footer/footer.ftl" />

<@renderJs />



<#include "aside/crossmarkIframe.ftl" />

</body>
</html>
