<#import "../common/sections/head.ftl" as head>
<#import "../common/sections/header.ftl" as header>
<#import "../common/sections/footer.ftl" as footer>
<#import "../common/sections/javascript.ftl" as javascript>
<#import "navigation.ftl" as navigation>
<#import "descriptions.ftl" as descriptions>
<#import "util/timer-section.ftl" as timerSection>

<#-- Note: the trendreport XML root is available as 'report.trendreport' if using the standard ReportTransformer -->
<#-- Actually, ReportTransformer passes the root element directly to the renderer. -->

<!DOCTYPE html>
<html lang="en">
<head>
    <@head.head title="XLT Performance Trend Report - Transactions" projectName=projectName configuration=(report.trendreport.configuration)!reportConfiguration />
</head>
<body id="trendreport">
<div id="container">
    <div id="content">
        <@header.header navNamespace=navigation title="Performance Trend Report" productName=productName productVersion=productVersion productUrl=productUrl projectName=projectName />

        <div id="data-content">

            <!--
                ************************************
                * Transactions
                ************************************
            -->
            <div class="section" id="transaction-summary">
                <@descriptions.headline_transaction_summary />

                <div class="content">
                    <@descriptions.description_transaction_summary gid="transaction-summary-description" />

                    <@timerSection.render 
                        elements=report.trendreport.transactions?children
                        summaryElement=report.trendreport.summary.transactions
                        tableRowHeader="Transaction Name"
                        directory="transactions"
                        type="transaction" />
                </div>
            </div>

        </div> <!-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <!-- content -->
</div> <!-- container -->    

<@javascript.render />

</body>
</html>
