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
    <@head.head title="XLT Performance Comparison Report - Page Load Timings" projectName=projectName configuration=report.testreport.configuration />
</head>
<body id="diffreport">
<div id="container">
    <div id="content">
        <@header.header navNamespace=navigation title="Performance Comparison Report" productName=productName productVersion=productVersion productUrl=productUrl projectName=projectName />

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
                        elements=report.testreport.pageLoadTimings?children
                        summaryElement=report.testreport.summary.pageLoadTimings
                        tableRowHeader="Page Load Timing Name"
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
