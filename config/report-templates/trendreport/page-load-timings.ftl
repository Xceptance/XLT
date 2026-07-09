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
    <@head.head title="XLT Performance Trend Report - Page Load Timings" projectName=projectName configuration=(report.trendreport.configuration)!reportConfiguration />
</head>
<body id="trendreport">
<div id="container">
    <div id="content">
        <@header.header navNamespace=navigation title="Performance Trend Report" productName=productName productVersion=productVersion productUrl=productUrl projectName=projectName />

        <div id="data-content">

            <!--
                ************************************
                * Page Load Timings
                ************************************
            -->
            <div class="section" id="page-load-timing-summary">
                <@descriptions.headline_page_load_timing_summary />

                <div class="content">
                    <@descriptions.description_page_load_timing_summary />

                    <@timerSection.render 
                        elements=report.trendreport.pageLoadTimings?children
                        summaryElement=report.trendreport.summary.pageLoadTimings
                        tableRowHeader="Page Load Timing Name"
                        directory="pageLoadTimings"
                        type="pageLoadTiming" />
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
