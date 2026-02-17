<#import "../common/sections/head.ftl" as head>
<#import "../common/sections/header.ftl" as header>
<#import "../common/sections/footer.ftl" as footer>
<#import "../common/sections/javascript.ftl" as javascript>
<#import "navigation.ftl" as navigation>
<#import "descriptions.ftl" as descriptions>

<!DOCTYPE html>
<html lang="en">
<head>
    <@head.head title="XLT Performance Trend Report - Overview" projectName=projectName configuration=((report.trendreport.configuration)!reportConfiguration)!reportConfiguration />
</head>
<body id="trendreport">
<div id="container">
    <div id="content">
        <@header.header navNamespace=navigation title="Performance Trend Report" productName=productName productVersion=productVersion productUrl=productUrl projectName=projectName />

        <div id="data-content">

            <!--
                ************************************
                * General section
                ************************************
            -->
            <div class="section" id="general">
                <@descriptions.headline_general />

                <div class="content">
                    <@descriptions.description_general />
                </div>
            </div>
        </div> <!-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <!-- content -->
</div> <!-- container -->    

<@javascript.javascript />

</body>
</html>
