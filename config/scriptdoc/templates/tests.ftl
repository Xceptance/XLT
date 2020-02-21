<#import "util/helpers.ftl" as helper />
<#import "layout/defaultLayout.ftl" as myLayout />

<#assign tests = suite.tests?sort_by("simpleName") />

<#assign testIndex>
<h3>Index</h3>
<ul class="sidebar">
<#list tests as test>
<li><a href="#test_${test.name}" title="${test.name}">${test.simpleName}</a></li>
</#list>
</ul>
</#assign>

<@myLayout.layout sidebar=testIndex pageId="tests">

  <#list tests as test>
    <@helper.printScript test true true 2 />
  </#list>

</@myLayout.layout>
