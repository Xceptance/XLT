<#import "/common/sections/head.ftl" as h>
<#import "/common/sections/header.ftl" as hdr>
<#import "/common/sections/footer.ftl" as f>
<#import "/common/sections/javascript.ftl" as js>

<#-- Import sections -->
<#import "/sections/slowest-requests.ftl" as sr>

<#compress>
<!DOCTYPE html>
<html lang="en">
<#assign projectNameNode = report.testreport.configuration.projectName>
<#assign projectNameText = (projectNameNode?size > 0)?then(projectNameNode[0], "")>

<#assign props = report.testreport.configuration.properties.property![]>
<#function getProp name>
    <#list props as p><#if p.@name == name><#return p.@value></#if></#list>
    <#return "">
</#function>
<#assign xtcOrganization = getProp("com.xceptance.xtc.organization")>
<#assign xtcProject = getProp("com.xceptance.xtc.project")>
<#assign xtcLoadTestId = getProp("com.xceptance.xtc.loadtest.run.id")>
<#assign xtcResultId = getProp("com.xceptance.xtc.loadtest.result.id")>
<#assign xtcReportId = getProp("com.xceptance.xtc.loadtest.report.id")>

<head>
    <@h.head title="XLT Report - Slowest Requests" projectName=projectNameText configuration=report.testreport.configuration />
</head>
<body id="loadtestreport">
<div id="container">
    <div id="content">
        <@hdr.header productName=productName 
                    productVersion=productVersion 
                    productUrl=productUrl 
                    projectName=projectNameText
                    xtcOrganization=xtcOrganization
                    xtcProject=xtcProject
                    xtcLoadTestId=xtcLoadTestId
                    xtcResultId=xtcResultId
                    xtcReportId=xtcReportId
                    scorecardPresent=scorecardPresent />

        <div id="data-content">
            <@sr.slowest_requests rootNode=report.testreport />
        </div> <!-- data-content -->

        <@f.footer productName=productName 
                   productVersion=productVersion 
                   productUrl=productUrl />
    </div> <!-- content -->
</div> <!-- end container -->    

<@js.javascript />

</body>
</html>

</#compress>
