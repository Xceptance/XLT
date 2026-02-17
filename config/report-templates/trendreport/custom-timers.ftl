<#import "../common/sections/head.ftl" as head>
<#import "../common/sections/header.ftl" as header>
<#import "../common/sections/footer.ftl" as footer>
<#import "../common/sections/javascript.ftl" as javascript>
<#import "navigation.ftl" as navigation>
<#import "descriptions.ftl" as descriptions>
<#import "util/timer-section.ftl" as timerSection>

<!DOCTYPE html>
<html lang="en">
<head>
    <@head.head title="XLT Performance Trend Report - Custom Timers" projectName=projectName configuration=(report.trendreport.configuration)!reportConfiguration />
</head>
<body id="trendreport">
<div id="container">
    <div id="content">
        <@header.header navNamespace=navigation title="Performance Trend Report" productName=productName productVersion=productVersion productUrl=productUrl projectName=projectName />

        <div id="data-content">

            <!--
                ************************************
                * Custom Timers
                ************************************
            -->
            <div class="section" id="custom-timer-summary">
                <@descriptions.headline_custom_timer_summary />

                <div class="content">
                    <@descriptions.description_custom_timer_summary />

                    <@timerSection.render 
                        elements=report.trendreport.customTimers?children
                        summaryElement=report.trendreport.summary.customTimers
                        tableRowHeader="Custom Timer Name"
                        directory="custom"
                        type="custom" />
                </div>
            </div>

        </div> <!-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <!-- content -->
</div> <!-- container -->    

<@javascript.render />

</body>
</html>
