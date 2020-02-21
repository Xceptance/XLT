<#import "util/helpers.ftl" as helper />
<#import "layout/defaultLayout.ftl" as myLayout />

<#macro printPackageData>
<#list suite.packages?values?sort_by("name") as pkg>
    <#if pkg.isDefaultPackage()>
    <h4>default package</h4>
    <#else>
    <h4>${pkg.name}</h4>
    </#if>
    <div class="data-table">
      <@helper.printMap pkg.testData />
    </div>
    <#if pkg_has_next>
    <div class="divider"></div>
    </#if>
</#list>
</#macro>

<@myLayout.layout pageId="testdata">
  <div class="section" id="testdata">
    <div class="section-title">
      <h2>Test Data</h2>
    </div><!-- /.section-title -->
    <div class="section-content">
      <h3 class="light-blue-text darken-3">Global</h3>
      <div class="data-table">
        <@helper.printMap suite.globalTestData />
      </div>

      <div class="package-level-test-data">
        <h3 class="light-blue-text darken-3">Package-Level</h3>
        <@printPackageData />
      </div>
    </div><!-- /.section-content -->
  </div><!-- /.section -->

</@myLayout.layout>
