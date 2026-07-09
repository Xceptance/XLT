<#import "../../common/util/format.ftl" as format>

<#macro render timerElement directory>
    <#local encodedName = format.convertIllegalCharactersInFileName(timerElement.name?trim)>
    <#local gid = "timerchart-" + timerElement?index>

    <a id="${gid}">
        <!-- This is a placeholder for the anchor. -->
    </a>

    <div id="tabs-${gid}" class="chart-group tabs c-tabs no-print" data-name="${timerElement.name}">
        <ul class="c-tabs-nav">
            <li class="c-tabs-nav-link c-is-active">
                <a href="#RunTime-${gid}">Run Times</a>
            </li>
            <li class="c-tabs-nav-link">
                <a href="#Errors-${gid}">Errors</a>
            </li>
            <li class="c-tabs-nav-link">
                <a href="#Throughput-${gid}">Throughput</a>
            </li>
        </ul>

        <div id="RunTime-${gid}" class="c-tab c-is-active">
            <div class="c-tab-content chart">
                <@create_chart_img_tag directory=directory name=encodedName suffix="_RunTime.webp" />
            </div>
        </div>

        <div id="Errors-${gid}" class="c-tab">
            <div class="c-tab-content chart">
                <@create_chart_img_tag directory=directory name=encodedName suffix="_Errors.webp" />
            </div>
        </div>

        <div id="Throughput-${gid}" class="c-tab">
            <div class="c-tab-content chart">
                <@create_chart_img_tag directory=directory name=encodedName suffix="_Throughput.webp" />
            </div>
        </div>
    </div>

    <div class="chart-group print">
        <h3>${timerElement.name}</h3>
        <div class="chart">
            <h5>Runtimes</h5>
            <img alt="charts/${directory}/${encodedName}_RunTime.webp" src="charts/${directory}/${encodedName}_RunTime.webp" />
        </div>

        <div class="chart">
            <h5>Errors</h5>
            <img alt="charts/${directory}/${encodedName}_Errors.webp" src="charts/${directory}/${encodedName}_Errors.webp" />
        </div>

        <div class="chart">
            <h5>Throughput</h5>
            <img alt="charts/${directory}/${encodedName}_Throughput.webp" src="charts/${directory}/${encodedName}_Throughput.webp" />
        </div>
    </div>
</#macro>

<#macro create_chart_img_tag directory name suffix>
    <img src="charts/${directory}/${name}${suffix}" loading="lazy" />
</#macro>
