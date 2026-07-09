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
    <@head.head title="XLT Performance Comparison Report - Requests" projectName=projectName configuration=report.testreport.configuration />
</head>
<body id="diffreport">
<div id="container">
    <div id="content">
        <@header.header navNamespace=navigation title="Performance Comparison Report" productName=productName productVersion=productVersion productUrl=productUrl projectName=projectName />

        <div id="data-content">

            <!--
                ************************************
                * Requests
                ************************************
            -->
            <div class="section" id="request-summary">
                <@descriptions.headline_request_summary />

                <div class="content">
                    <@descriptions.description_request_summary />

                    <@timerSection.render 
                        elements=report.testreport.requests?children
                        summaryElement=report.testreport.summary.requests
                        tableRowHeader="Request Name"
                        type="request" />
                </div>
            </div>

        </div> <!-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <!-- content -->
</div> <!-- container -->    

<@javascript.javascript />

</body>
</html>

</#compress>
