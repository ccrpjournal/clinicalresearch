<#include 'default.ftl' />

<#macro page_header>
  <@base_page_head />
  <#include "../common/bodyAnalytics.ftl" />

  <div id="container-main">
    <#include "../article/backToArticle.ftl" />
  <@page_header_extra />
</#macro>

<#macro page_footer>
  <@page_footer_extra />
  <#include "../common/footer/footer.ftl" />

  </div>
  <!--end container-main-->

  <#include "../common/bodyJs.ftl" />
</body>
</html>
</#macro>