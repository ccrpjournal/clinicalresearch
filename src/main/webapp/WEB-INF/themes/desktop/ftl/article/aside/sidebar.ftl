<#include "downloads.ftl" />

<div class="aside-container">
  <#include "print.ftl" />
  <#include "share.ftl" />
</div>

<#include "crossmark.ftl" />

<#include "relatedArticles.ftl" />

<#include "subjectAreas.ftl" />

<#include "adSlotAside.ftl" />

<#if articleComments?size gt 0  >
<#include "comments.ftl"  />
</#if>

<#include "twitterModule.ftl" />

