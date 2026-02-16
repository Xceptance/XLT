<#macro head title projectName configuration>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="generator" content="XLT">

    <#assign normProjName = projectName?string?trim>
    
    <title>
        <#if normProjName?has_content>
            <#if normProjName?length lt 32>
                ${normProjName}
            <#else>
                ${normProjName?substring(0, 29)}...
            </#if>
            | 
        </#if>
        ${title}
    </title>

    <link href="css/default.css" type="text/css" rel="stylesheet">
    <link href="css/print.css" type="text/css" rel="stylesheet" media="print">

    <script src="js/jquery-3.6.4.min.js"></script>
    <script src="js/jquery.hoverIntent-1.10.2.min.js"></script>
    <script src="js/jquery.scrollTo-2.1.3.min.js"></script>
    <script src="js/tabs.js"></script>
    <script src="js/table.js"></script>
    <script src="js/crosshair.js"></script>
    <script src="js/echarts-6.0.0.min.js"></script>

    <link rel="icon" href="images/favicon.png" sizes="any">
    <link rel="icon" href="images/favicon.svg" type="image/svg+xml">
    
    <style type="text/css">
        <#assign chartWidth = (configuration.chartWidth[0]!600)?number>
        <#assign chartHeight = (configuration.chartHeight[0]!400)?number>
        .chart-group .chart img, .chart-group .echart {
            width: ${chartWidth}px;
            height: ${chartHeight}px;
        }
        #transaction-summary .chart-group .overview .chart img, #transaction-summary .chart-group .overview .echart {
            height: ${chartHeight * 1.5}px;
        }
        #agents .chart-group .memory .chart img, #agents .chart-group .memory .echart {
            height: ${chartHeight * 2.3}px;
        }
    </style>
</#macro>
