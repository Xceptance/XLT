<#import "/common/sections/head.ftl" as head>
<#import "/common/sections/header.ftl" as header>
<#import "/sections/navigation.ftl" as nav>
<#import "/common/sections/footer.ftl" as footer>
<#import "/common/sections/javascript.ftl" as js>

<#import "/sections/custom-timers.ftl" as ct>

<!DOCTYPE html>
<html lang="en">
<head>
    <#assign projectNameNode = report.testreport.configuration.projectName>
    <#assign projectNameText = (projectNameNode?has_content)?then(projectNameNode[0], "")>
    <@head.head title="XLT Report - Custom Timers" projectName=projectNameText configuration=report.testreport.configuration />
</head>
<body id="loadtestreport">
<div id="container">
    <div id="content">
        <@header.header productName=productName 
                        productVersion=productVersion 
                        productUrl=productUrl 
                        projectName=projectNameText
                        scorecardPresent=scorecardPresent!false />

        <div id="data-content">
            <#-- 
                ************************************
                * Custom Timers
                ************************************
            -->
            <@ct.custom_timers />

        </div> <#-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <#-- content -->
</div> <#-- end container -->

<@js.javascript />

</body>
</html>
