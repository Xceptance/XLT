<#import "/common/sections/head.ftl" as head>
<#import "/common/sections/header.ftl" as header>
<#import "/sections/navigation.ftl" as nav>
<#import "/common/sections/footer.ftl" as footer>
<#import "/common/sections/javascript.ftl" as js>

<#import "/sections/custom-values.ftl" as cv>

<!DOCTYPE html>
<html lang="en">
<head>
    <#assign projectNameNode = report.testreport.configuration.projectName>
    <#assign projectNameText = (projectNameNode?has_content)?then(projectNameNode[0], "")>
    <@head.head title="XLT Report - Custom Values" projectName=projectNameText configuration=report.testreport.configuration />
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
                * Custom Values
                ************************************
            -->
            <@cv.custom_values />

        </div> <#-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <#-- content -->
</div> <#-- end container -->

<@js.javascript />

</body>
</html>
