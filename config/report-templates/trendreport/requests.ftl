<#import "../common/sections/head.ftl" as head>
<#import "../common/sections/header.ftl" as header>
<#import "../common/sections/footer.ftl" as footer>
<#import "../common/sections/javascript.ftl" as javascript>
<#import "navigation.ftl" as navigation>
<#import "descriptions.ftl" as descriptions>
<#import "util/timer-section.ftl" as timerSection>

<#compress>
<!DOCTYPE html>
<html lang="en">
<head>
    <@head.head title="XLT Performance Trend Report - Requests" projectName=projectName configuration=(report.trendreport.configuration)!reportConfiguration />
</head>
<body id="trendreport">
<div id="container">
    <div id="content">
        <@header.header navNamespace=navigation title="Performance Trend Report" productName=productName productVersion=productVersion productUrl=productUrl projectName=projectName />

        <div id="data-content">

            <!--
                ************************************
                * Requests
                ************************************
            -->
            <div class="section" id="request-summary">
                <@descriptions.headline_request_summary />

                <div class="content">
                    <@descriptions.description_request_summary gid="request-summary-description" />

                    <@timerSection.render 
                        elements=report.trendreport.requests?children
                        summaryElement=report.trendreport.summary.requests
                        tableRowHeader="Request Name"
                        directory="requests"
                        type="request" />
                </div>
            </div>

        </div> <!-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <!-- content -->
</div> <!-- container -->    

<@javascript.render />

</body>
</html>

</#compress>
