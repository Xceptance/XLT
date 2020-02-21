<#import "util/helpers.ftl" as helper />
<#import "layout/defaultLayout.ftl" as myLayout />

<#macro makeLink package>
<#if package.defaultPackage>
<#local packageID = "pkg__default"  />
<#local packageName = "default package" />
<#else>
<#local packageID = "pkg_"+package.name />
<#local packageName = package.name />
</#if>
<li>
<a href="#${packageID}">${packageName}</a>
</li>
</#macro>

<#assign pkgIndex>
<h3>Index</h3>
<ul class="sidebar">
<#list suite.packages?keys?sort as pkgName>
<@makeLink suite.packages[pkgName] />
</#list>
</ul>
</#assign>


<#macro printScripts seq linkPrefix>
<#if seq?has_content>
  <ul>
  <#list seq?sort_by("name") as item>
    <li><a href="${linkPrefix + "_" + item.name}">${item.simpleName}</a></li>
  </#list>
  </ul>
<#else>
  <@helper.none />
</#if>
</#macro>

<#macro printPackage package>
<div class="section" id="pkg_<#if package.defaultPackage>_default<#else>${package.name}</#if>">
  <div class="section-title">
    <h2 class="light-blue darken-4 white-text"><#if package.defaultPackage>default package<#else>${package.name}</#if></h2>
  </div>
  <div class="section-content">
    <div class="data">
      <h3 class="light-blue-text darken-3">Test Data</h3>
      <@helper.printMap package.testData />
    </div>

    <div class="scripts">
      <h3 class="light-blue-text darken-3">Scripts</h3>

      <h4>Tests</h4>
      <@printScripts package.tests "tests.html#test" />

      <h4>Script Modules</h4>
      <@printScripts package.scriptModules "modules.html#mod" />

      <h4>Java Modules</h4>
      <@printScripts package.javaModules "modules.html#mod" />
    </div>
  </div>
</div>
</#macro>


<@myLayout.layout sidebar=pkgIndex pageId="packages">

  <#list suite.packages?keys?sort as pkgName>
      <@printPackage suite.packages[pkgName] />
  </#list>

</@myLayout.layout>
