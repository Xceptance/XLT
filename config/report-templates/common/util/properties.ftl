<#--
    Property extraction utility for FreeMarker templates.
-->

<#function getProp name rootNode=report.testreport.configuration>
    <#local props = (rootNode.properties.property)![]>
    <#list props as p>
        <#if p.@name == name>
            <#return p.@value>
        </#if>
    </#list>
    <#return "">
</#function>

<#function getXtcOrganization rootNode=report.testreport.configuration>
    <#return getProp("com.xceptance.xtc.organization", rootNode)>
</#function>

<#function getXtcProject rootNode=report.testreport.configuration>
    <#return getProp("com.xceptance.xtc.project", rootNode)>
</#function>

<#function getXtcLoadTestId rootNode=report.testreport.configuration>
    <#return getProp("com.xceptance.xtc.loadtest.run.id", rootNode)>
</#function>

<#function getXtcResultId rootNode=report.testreport.configuration>
    <#return getProp("com.xceptance.xtc.loadtest.result.id", rootNode)>
</#function>

<#function getXtcReportId rootNode=report.testreport.configuration>
    <#return getProp("com.xceptance.xtc.loadtest.report.id", rootNode)>
</#function>
