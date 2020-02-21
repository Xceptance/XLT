<#macro none>
  <p class="empty">&mdash;&nbsp;none&nbsp;&mdash;</p>
</#macro>

<#macro printMap data>
<#local keys=data?keys />
<#if keys?size == 0>
  <@none />
<#else>
  <table>
    <thead class="light-blue darken-2 white-text">
      <tr>
        <th>Name</th>
        <th>Value</th>
      </tr>
    </thead>
    <tbody>
    <#list keys?sort as key>
      <tr>
        <td class="key">${key}</td>
        <#if data[key]??>
        <td class="value">${data[key]}</td>
        <#else>
        <td class="value na">N/A</td>
        </#if>
      </tr>
    </#list>
    </tbody>
  </table>
</#if>
</#macro>

<#macro heading level=1 attString="">
<h${level} ${attString}>
  <#nested />
</h${level}>
</#macro>

<#macro printTestData script separateTables=false headingLvl=3>
<#local idata=script.testData />
<#local edata=script.externalParameters />
<#local nextLvl=headingLvl+1 />

<#-- Test Data -->

<#if separateTables>
  <@heading nextLvl>Defined Inside</@heading>
  <div class="data-table">
    <@printMap data=idata />
  </div><!-- ./data-table -->

  <@heading nextLvl>Defined Outside</@heading>
  <#-- External Parameters -->
  <div class="data-table">
    <@printMap data=edata />
  </div><!-- /.data-table -->

<#else>
  <#local mergedData=idata+edata />
  <#local mergedDataKeys=mergedData?keys?sort />
  <#local eKeys=edata?keys />
  <div class="data-table">
    <#if mergedDataKeys?size == 0>
      <@none />
    <#else>
      <table>
        <thead class="light-blue darken-2 white-text">
          <tr>
            <th>Name</th>
            <th>Value</th>
          </tr>
        </thead>
        <tbody>
        <#list mergedDataKeys as key>
          <tr class="<#if eKeys?seq_contains(key)>external<#else>internal</#if>">
            <td class="key">${key}</td>
            <td class="value">${mergedData[key]!}</td>
          </tr>
        </#list>
        </tbody>
      </table>
    </#if>
  </div><!-- /.data-table -->
</#if>
</#macro>


<#macro printSteps steps>
<#escape x as x?html>
<#if steps?size == 0>
  <@none />
<#else>
  <ol class="step-list">
    <#list steps as step>
      <li data-disabled="${step.disabled?c}" class="step step-<#if step.moduleCall>module<#else>action</#if>">
        <div class="step-name">
          <#if step.moduleCall>
            <a href="modules.html#mod_${step.name}">${step.name}</a>
          <#else>
            ${step.name}
          </#if>
        </div>
        <#if step.condition?? && step.condition.expression!?has_content>
            <div class="step-condition z-depth-1" data-disabled="${step.condition.disabled?c}">
                <div class="label light-blue darken-2 white-text">Condition</div>
                <pre class="expression teal-text"><code class="language-javascript">${step.condition.expression}</code></pre>
            </div>
        </#if>
        <div class="step-description">
        <#if step.description!?has_content>
          <#noescape>${step.descriptionMarkup!}</#noescape>
        </#if>
        </div><!-- /.step-description -->
      </li><!-- /.step -->
  </#list>
  </ol><!-- /.step-list -->
</#if>
</#escape>
</#macro>

<#macro printScript script isTest=false printPackageName=true headingStart=2>
<#local nextLvl = headingStart+1 />
<#if isTest>
  <#local headAttString = "data-disabled=\"${script.disabled?c}\" id=\"test_${script.name}\"" />
<#else>
  <#local headAttString = "id=\"mod_${script.name}\"" />
</#if>

<div class="row">
<#escape x as x?html>
  <#-- Script Header -->
  <@heading headingStart headAttString+" class=\"light-blue darken-4 white-text\"">
    <#if printPackageName>
      <span class="smaller">${script.packageName}</span>
    </#if>
    ${script.simpleName}
    <#if !isTest && !script.isCalled()>
      <span class="warn"><i class="mdi-action-report-problem"></i>Unused</span>
    </#if>
  </@heading>

  <#if script.id!?has_content>
    <@heading nextLvl "class=\"script-id\"">${script.id}</@heading>
  </#if>

  <#-- Script Tags -->
  <div class="tags">
  <#if script.tags?has_content>
    <span class="tag-label">Tags:</span>
    <#list script.tags as tag>
      <span class="tag teal lighten-4">${tag}</span>
    </#list>
  </#if>
  </div><!-- /.tags -->
    
  <#-- Description -->
  <div class="description light-blue lighten-5">
  <#if script.description!?has_content>
    <#noescape>${script.descriptionMarkup}</#noescape>
  <#else>
    <p class="empty">This script has no description!</p>
  </#if>
  </div><!-- /.description -->
    
  <div class="collapsible">
    <div class="collapsible-header button"><i class="mdi-navigation-expand-more"></i>More</div>
    <div class="collapsible-body">

      <#-- Individual Test Steps -->
      <div class="steps">
        <@heading nextLvl "class=\"light-blue-text darken-3\"">Steps</@heading>
        <@printSteps steps=script.steps />
      </div><!-- /.steps -->

      <#if isTest>
      <div class="steps">
        <@heading nextLvl "class=\"light-blue-text darken-3\"">Post-Steps</@heading>
        <@printSteps steps=script.postSteps />
      </div>
      <#else>
      <div class="parameters">
        <@heading nextLvl "class=\"light-blue-text darken-3\"">Module Parameters</@heading>
        <#if script.parameters?has_content>
        <table>
          <thead class="light-blue darken-3 white-text">
            <tr>
              <th>Name</th>
              <th>Description</th>
            </tr>
          </thead>
          <tbody>
          <#list script.parameters as param>
            <tr>
              <td>${param.name}</td>
              <td>
              <#if param.description!?has_content>
                <#noescape>${param.descriptionMarkup}</#noescape>
              <#else>
                <@none />
              </#if>
              </td>
            </tr>
          </#list>
          </tbody>
        </table>
        <#else>
          <@none />
        </#if>
      </div><!-- /.parameters -->
      </#if>
    
      <#-- Test Data -->
      <div class="data">
        <@heading nextLvl "class=\"light-blue-text darken-3\"">Test Data</@heading>
        <@printTestData script true nextLvl />
      </div><!-- /.data -->

      <#-- Dynamic Variables -->
      <div class="vars">
        <@heading nextLvl "class=\"light-blue-text darken-3\"">Dynamic Variables</@heading>
        <#if script.stores?has_content>
          <ul class="square">
            <#list script.stores as dynvar>
              <li>${dynvar}</li>
            </#list>
          </ul>
        <#else>
          <@none />
        </#if>
      </div><!-- /.vars -->
              
    </div><!-- /.collapsible-body -->
    

  </div><!-- /.collapsible -->
</#escape>
</div><!-- /.row -->
</#macro>


<#macro printJavaModule module includePackageName=true headingStart=2>
<#local nextLvl = headingStart + 1 />
<div class="row">
<#escape x as x?html>
  <#-- Script Header -->
  <@heading headingStart "id=\"mod_${module.name}\" class=\"light-blue darken-4 white-text\"">
    <#if includePackageName><span class="smaller">${module.packageName}</span></#if>
    ${module.simpleName}
    <#if !module.isCalled()>
      <span class="warn"><i class="mdi-action-report-problem"></i>Unused</span>
    </#if>
  </@heading>
        
  <#-- Script Tags -->
  <div class="tags">
    <#list module.tags as tag>
      <span class="tag">${tag}</span>
    </#list>
  </div><!-- /.tags -->

  <#-- Description -->
  <div class="description light-blue lighten-5">
  <#if module.description!?has_content>
    <#noescape>${module.descriptionMarkup}</#noescape>
  <#else>
    <p class="empty">This script has no description!</p>
  </#if>
  </div><!-- /.description -->
  
  <div class="collapsible">
    <div class="collapsible-header"><i class="mdi-navigation-expand-more"></i>More</div>
    <div class="collapsible-body">
      <div class="parameters">
        <@heading nextLvl "class=\"light-blue-text darken-3\"">Module Parameters</@heading>
        <#if module.parameters?has_content>
          <table>
            <thead class="light-blue darken-3 white-text">
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
            <#list module.parameters as param>
              <tr>
                <td>${param.name}</td>
                <td>
                <#if param.description!?has_content>
                  <#noescape>${param.descriptionMarkup}</#noescape>
                <#else>
                  <@none />
                </#if>
                </td>
              </tr>
            </#list>
            </tbody>
          </table>
        <#else>
          <@none />
        </#if>
      </div><!-- /.parameters -->

    </div><!-- /.collapsible-body -->
  </div><!-- /.collapsible -->

</#escape>
</div><!-- /.row -->
</#macro>

