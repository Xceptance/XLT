<#import "/common/sections/header.ftl" as header>
<#import "/common/sections/footer.ftl" as footer>
<#import "/common/sections/head.ftl" as head>
<#import "sections/agents.ftl" as agents>
<#import "sections/descriptions.ftl" as descriptions>
<#import "/common/sections/javascript.ftl" as js>

<#compress>
<!DOCTYPE html>
<html lang="en">
<head>
    <#assign projectNameNode = report.testreport.configuration.projectName>
    <#assign projectNameText = (projectNameNode?has_content)?then(projectNameNode[0], "")>
    <@head.head title="XLT Report - Agents" projectName=projectNameText configuration=report.testreport.configuration />
</head>
<body id="loadtestreport">
<div id="container">
    <div id="content">
        <@header.header title=descriptions.headline_overview 
                        productName=productName 
                        productVersion=productVersion 
                        productUrl=productUrl 
                        projectName=projectNameText
                        scorecardPresent=scorecardPresent!false />

        <div id="data-content">
            <#-- 
                ************************************
                * Agents
                ************************************
            -->
            <@agents.agents rootNode=report.testreport.agents summaryNode=report.testreport.summary.agents />

        </div> <#-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <#-- content -->
</div> <#-- end container -->    
<@js.javascript />

</body>
</html>

</#compress>
