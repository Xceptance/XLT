<#import "/common/sections/head.ftl" as h>
<#import "/common/sections/header.ftl" as hdr>
<#import "/common/sections/footer.ftl" as f>
<#import "/common/sections/javascript.ftl" as js>

<#-- Import sections -->
<#import "/sections/network.ftl" as network>
<#import "/sections/hosts.ftl" as hosts>
<#import "/sections/ips.ftl" as ips>
<#import "/sections/response-codes.ftl" as responseCodes>
<#import "/sections/request-methods.ftl" as requestMethods>
<#import "/sections/content-types.ftl" as contentTypes>

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
    <@h.head title="XLT Report - Network" projectName=projectNameText configuration=report.testreport.configuration />
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
            <@network.network rootNode=report.testreport.general />
            <@hosts.hosts rootNode=report.testreport.hosts totalHits=report.testreport.general.hits />
            <@ips.ips rootNode=report.testreport.ips totalHits=report.testreport.general.hits />
            <@requestMethods.request_methods rootNode=report.testreport.requestMethods totalHits=report.testreport.general.hits />
            <@responseCodes.response_codes rootNode=report.testreport.responseCodes totalHits=report.testreport.general.hits />
            <@contentTypes.content_types rootNode=report.testreport.contentTypes totalHits=report.testreport.general.hits />
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
