<#include "authorItem.ftl" />
<dl>
<#list authors as author><#-- Before the expander -->
        <@authorItemFull author author_index author_has_next true false false/>
    </#list>
</dl>


<#if competingInterests?size gt 0>
<h2>Competing Interests</h2>
  <#list competingInterests as competingInterest>
  <p>${competingInterest}</p>
  </#list>
</#if>