<#import "/common/sections/header.ftl" as header>
<#import "/common/sections/footer.ftl" as footer>
<#import "/common/sections/head.ftl" as head>
<#import "/common/sections/javascript.ftl" as javascript>
<#import "/sections/navigation.ftl" as navigation>
<#import "/scorecard/scorecard.ftl" as scorecard_details>
<#import "/scorecard/ratings.ftl" as ratings>
<#import "/scorecard/groups.ftl" as groups>
<#import "/scorecard/rules.ftl" as rules>
<#import "/scorecard/rule-checks.ftl" as rule_checks>

<!DOCTYPE html>
<html lang="en">
<#assign scorecard = report.scorecard>
<#if !scorecard?has_content><#assign scorecard = report></#if>

<#-- Extract project name: use parameter if provided, otherwise extract from XML -->
<#if !projectName??>
  <#assign projectNameNode = (report.testreport.configuration.projectName)![]>
  <#assign projectName = (projectNameNode?size > 0)?then(projectNameNode[0]!"", "")>
</#if>

<head>
    <#if scorecard?has_content>
        <@head.head title="XLT Report - Scorecard" projectName=projectName configuration=scorecard["configuration"]/>
    <#else>
        <@head.head title="XLT Report - Scorecard" projectName=projectName configuration=[]/>
    </#if>
    <style type="text/css">
    /* No auto-stripe for table rows, use CSS class to derive color for row stripes. */
    table.no-auto-stripe tr.odd td {
        background-color: #f9f9fc;
    }
    table.no-auto-stripe tr.even td {
        background-color: #e8e8e8;
    }
    #scorecard-result .error > pre {
        border: 1px solid var(--main-color);
        padding: 1rem;
    }
    #scorecard-result .verdict {
        padding: 0px 1rem;
        font-weight: 500;
        font-style: italic;
    }
    #scorecard-ratings .inactive, #scorecard-rules .inactive {
        color: #5f5f5f;
    }
    </style>
</head>
<body id="loadtestreport">
<div id="container">
    <div id="content">
        <@header.header title="Performance Test Report" projectName=projectName!"" productName=productName!"" productUrl=productUrl!"" productVersion=productVersion!"" scorecardPresent=true/>

        <div id="data-content">
            <#if scorecard?has_content>
                <#assign config = scorecard["configuration"]>
                <#assign outcome = scorecard["outcome"]>
                <@scorecard_details.scorecard rootNode=outcome configuration=config/>
                <@ratings.ratings elements=config["ratings"]["rating"] active=outcome["rating"]/>
                <@groups.groups definitions=config["groups"]["group"] results=outcome["groups"]["group"]/>
                <@rules.rules definitions=config["rules"]["rule"] results=outcome["groups"]["group"]["rules"] configuration=config/>
                <@rule_checks.rule_checks definitions=config["rules"]["rule"] results=outcome["groups"]["group"]["rules"] configuration=config/>
            </#if>
        </div>

        <@footer.footer productName=productName!"" productUrl=productUrl!"" productVersion=productVersion!"" />
    </div>
</div>

<@javascript.javascript/>
</body>
</html>
