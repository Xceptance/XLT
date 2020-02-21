<#import "util/helpers.ftl" as helper />
<#import "layout/defaultLayout.ftl" as myLayout />

<#macro makeList seq>
<ul>
<#if seq?has_content>
<#list seq as script>
  <li><a href="#mod_${script.name}" title="${script.name}">${script.simpleName}</a></li>
</#list>
<#else>
  <li><@helper.none /></li>
</#if>
</ul>
</#macro>

<#assign scriptmods = suite.modules?sort_by("simpleName") />
<#assign javamods = suite.javaModules?sort_by("simpleName") />

<#assign scriptIndex>
<h3>Index</h3>
<ul class="sidebar">
  <li>
    <h5>Script Modules</h5>
    <@makeList scriptmods />
  </li>
  <li>
    <h5>Java Modules</h5>
    <@makeList javamods />
  </li>
</ul>
</#assign>

<@myLayout.layout sidebar=scriptIndex pageId="modules">

  <div class="section">
    <div class="section-title">
      <h1>Script Modules</h1>
    </div>
    <div class="section-content">
    <#if scriptmods?has_content>
    <#list scriptmods as script>
      <@helper.printScript script false true 2 />
    </#list>
    <#else>
      <@helper.none />
    </#if>
    </div>
  </div>

  <div class="section">
    <div class="section-title">
      <h1>Java Modules</h1>
    </div>
    <div class="section-content">
    <#if javamods?has_content>
    <#list javamods as script>
      <@helper.printJavaModule script true 2 />
    </#list>
    <#else>
      <@helper.none />
    </#if>
    </div>
  </div>

</@myLayout.layout>
