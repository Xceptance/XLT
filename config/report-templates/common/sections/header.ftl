<#import "/sections/navigation.ftl" as nav>
<#import "/common/util/properties.ftl" as props>

<#macro header title="Performance Test Report" productName="" productVersion="" productUrl="" projectName="" scorecardPresent=false xtcOrganization="" xtcProject="" xtcLoadTestId="" xtcResultId="" xtcReportId="" navNamespace="">
    <#-- If XTC parameters are not provided, try to extract them from configuration -->
    <#local xtcOrg = xtcOrganization?has_content?then(xtcOrganization, props.getXtcOrganization())>
    <#local xtcProj = xtcProject?has_content?then(xtcProject, props.getXtcProject())>
    <#local xtcLTId = xtcLoadTestId?has_content?then(xtcLoadTestId, props.getXtcLoadTestId())>
    <#local xtcResId = xtcResultId?has_content?then(xtcResultId, props.getXtcResultId())>
    <#local xtcRepId = xtcReportId?has_content?then(xtcReportId, props.getXtcReportId())>

    <#assign normProjName = projectName?string?trim>

    <#if navNamespace?has_content>
        <@navNamespace.navigation scorecardPresent=scorecardPresent />
    <#else>
        <@nav.navigation scorecardPresent=scorecardPresent />
    </#if>

    <header id="header">
        <div class="brand">
            <img src="images/xlt-logo.svg" class="logo" alt="The XLT tool logo">
        </div>
        <div class="title">
            <h1>
                <#if normProjName?has_content>
                    <span class="projectname">${normProjName}</span>
                     &#8212;
                </#if>
                ${title}
            </h1>
            <#if xtcOrg?has_content>
                <div>
                    <div class="ltinfo">
                        <span class="key">Organization:</span>
                        <span class="value">${xtcOrg}</span>
                    </div>
                    <div class="ltinfo">
                        <span class="key">Project:</span>
                        <span class="value">${xtcProj}</span>
                    </div>
                    <div class="ltinfo">
                        <span class="key">Load Test:</span>
                        <span class="value">${xtcLTId}</span>
                    </div>
                    <div class="ltinfo">
                        <span class="key">Result:</span>
                        <span class="value">${xtcResId}</span>
                    </div>
                    <div class="ltinfo">
                        <span class="key">Report:</span>
                        <span class="value">${xtcRepId}</span>
                    </div>
                </div>
            <#else>
                <h2 class="ltinfo hide-on-scroll">
                    Created with
                    <a href="${productUrl}?source=TestReport">
                        <span class="productname">${productName}</span>
                        <span class="productversion">${productVersion}</span>
                    </a>
                </h2>
                <div class="ltinfo show-on-scroll">
                    <#if normProjName?has_content>
                        <span class="value">${normProjName}</span>
                        &#8212;
                    </#if>
                    ${title}
                </div>
            </#if>
        </div>
    </header>
</#macro>
