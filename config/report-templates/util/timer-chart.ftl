<#import "/common/util/convert-filename.ftl" as cf>

<#macro timer_chart element directory type gid="summary">
    <#local name = element.name>
    <#local encodedName = cf.convertIllegalCharactersInFileName(name)>


    <#local dynamicChartsEnabled = (report.testreport.configuration.reportGeneratorConfiguration.dynamicChartsEnabled[0])!"false">
    <#local timeZoneLabel = (report.testreport.configuration.reportGeneratorConfiguration.timeZoneLabel[0])!"">
    <#local timeZoneOffset = (report.testreport.configuration.reportGeneratorConfiguration.timeZoneOffset[0])!"">

    <a id="${name}"><!--
                This is a placeholder for the anchor.
            --></a>
        <div class="chart-group tabs c-tabs no-print" data-name="${name}" id="chart-${gid}">
            <ul class="c-tabs-nav">
                <li class="c-tabs-nav-link img-tab c-is-active">
                    <a href="#Overview-${gid}">Overview</a>
                </li>
                <#if dynamicChartsEnabled == "true">
                <li class="c-tabs-nav-link echart-tab">
                    <a href="#DynamicOverview-${gid}">Dynamic Overview</a>
                </li>
                </#if>
                <li class="c-tabs-nav-link img-tab">
                    <a href="#Averages-${gid}">Averages</a>
                </li>
                <li class="c-tabs-nav-link img-tab">
                    <a href="#Count-${gid}">Count/s</a>
                </li>
                <#if type == "transaction">
                <li class="c-tabs-nav-link img-tab">
                    <a href="#ArrivalRate-${gid}">Arrival Rate</a>
                </li>
                <li class="c-tabs-nav-link img-tab">
                    <a href="#ConcurrentUsers-${gid}">Concurrent Users</a>
                </li>
                </#if>
                <#if type == "request">
                <li class="c-tabs-nav-link img-tab">
                    <a href="#Distribution-${gid}">Distribution</a>
                </li>
                <li class="c-tabs-nav-link img-tab">
                    <a href="#Network-${gid}">Response Size</a>
                </li>
                </#if>
            </ul>

            <#if gid != "summary">
                <a href="#tableEntry-${gid}" class="backlink">Back to Table</a>
            </#if>

            <div id="Overview-${gid}" class="c-tab c-is-active img-tab overview">
                <div class="c-tab-content chart">
                    <img src="charts/${directory}/${encodedName}.webp" alt="charts/${directory}/${encodedName}.webp" loading="lazy"/>
                </div>
            </div>

            <#if dynamicChartsEnabled == "true">
            <div id="DynamicOverview-${gid}" class="c-tab echart-tab overview">
                <div class="c-tab-content echart" src="charts/${directory}/${encodedName}.json" name="${name}" data-timezone-label="${timeZoneLabel}" data-timezone-offset="${timeZoneOffset}">
                </div>
            </div>
            </#if>

            <div id="Averages-${gid}" class="c-tab img-tab">
                <div class="c-tab-content chart">
                    <img src="charts/placeholder.webp" alt="charts/${directory}/${encodedName}_Average.webp"/>
                </div>
            </div>

            <div id="Count-${gid}" class="c-tab img-tab">
                <div class="c-tab-content chart">
                    <img src="charts/placeholder.webp" alt="charts/${directory}/${encodedName}_CountPerSecond.webp"/>
                </div>
            </div>

            <#if type == "transaction">
            <div id="ArrivalRate-${gid}" class="c-tab img-tab">
                <div class="c-tab-content chart">
                    <img src="charts/placeholder.webp" alt="charts/${directory}/${encodedName}_ArrivalRate.webp"/>
                </div>
            </div>
            <div id="ConcurrentUsers-${gid}" class="c-tab img-tab">
                <div class="c-tab-content chart">
                    <img src="charts/placeholder.webp" alt="charts/${directory}/${encodedName}_ConcurrentUsers.webp"/>
                </div>
            </div>
            </#if>

            <#if type == "request">
            <div id="Distribution-${gid}" class="c-tab img-tab">
                <div class="c-tab-content chart">
                    <img src="charts/placeholder.webp" alt="charts/${directory}/${encodedName}_Histogram.webp"/>
                </div>
            </div>
            <div id="Network-${gid}" class="c-tab img-tab">
                <div class="c-tab-content chart">
                    <img src="charts/placeholder.webp" alt="charts/${directory}/${encodedName}_ResponseSize.webp"/>
                </div>
            </div>
            </#if>
        </div>

        <div class="chart-group print">
            <h3>${name}</h3>
            <#if type == "transaction">
                <div class="chart">
                    <h5>Overview</h5>
                    <img alt="charts/${directory}/${encodedName}.webp"/>

                    <h5>Averages</h5>
                    <img alt="charts/${directory}/${encodedName}_Average.webp"/>
                </div>
                <div class="chart">
                    <h5>Count/s</h5>
                    <img alt="charts/${directory}/${encodedName}_CountPerSecond.webp"/>

                    <h5>Arrival Rate</h5>
                    <img alt="charts/${directory}/${encodedName}_ArrivalRate.webp"/>

                    <h5>Concurrent Users</h5>
                    <img alt="charts/${directory}/${encodedName}_ConcurrentUsers.webp"/>
                </div>
            <#else>
                <div class="chart">
                    <h5>Overview</h5>
                    <img alt="charts/${directory}/${encodedName}.webp"/>
                </div>
                <div class="chart">
                    <h5>Count/s</h5>
                    <img alt="charts/${directory}/${encodedName}_CountPerSecond.webp"/>
                </div>
                <div class="chart">
                    <h5>Averages</h5>
                    <img alt="charts/${directory}/${encodedName}_Average.webp"/>
                </div>
                <#if type == "request">
                <div class="chart">
                    <h5>Response Size</h5>
                    <img alt="charts/${directory}/${encodedName}_ResponseSize.webp"/>
                </div>
                <div class="chart">
                    <h5>Distribution</h5>
                    <img alt="charts/${directory}/${encodedName}_Histogram.webp"/>
                </div>
                </#if>
            </#if>
        </div>
</#macro>
