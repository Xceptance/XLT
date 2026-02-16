<#import "/common/sections/head.ftl" as h>
<#import "/common/sections/header.ftl" as hdr>
<#import "/common/sections/footer.ftl" as f>
<#import "/common/sections/javascript.ftl" as js>

<#-- Import main sections -->
<#import "/sections/load-profile.ftl" as lp>
<#import "/sections/comment.ftl" as c>
<#import "/sections/general.ftl" as g>
<#import "/sections/agent-summary.ftl" as agents>
<#import "/sections/summary.ftl" as s>
<#import "/sections/network-summary.ftl" as ns>

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
    <@h.head title="XLT Report - Overview" projectName=projectNameText configuration=report.testreport.configuration />
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
            <@lp.load_profile rootNode=report.testreport.configuration />
            <@c.testcomment rootNode=report.testreport.configuration.comments />
            <@g.general rootNode=report.testreport.general />
            <@agents.agent_summary rootNode=report.testreport.agents />
            <@s.summary />
            <@ns.network_summary rootNode=report.testreport.general />
        </div> <!-- data-content -->

        <@f.footer productName=productName 
                   productVersion=productVersion 
                   productUrl=productUrl />
    </div> <!-- content -->
</div> <!-- container -->    

<@js.javascript />

</body>
</html>
