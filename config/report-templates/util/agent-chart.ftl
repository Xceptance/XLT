<#macro agent_chart directory isSummary gid="">
    <#-- unique id -->
    <#if gid == "">
        <#local _gid = directory?keep_before(".") + "_" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
        <#local _gid = "agent_" + _gid>
    <#else>
        <#local _gid = gid>
    </#if>

    <#if isSummary>
        <a id="">
            <#-- This is a placeholder for the anchor. -->
        </a>
    <#else>
        <a id="${directory}">
            <#-- This is a placeholder for the anchor. -->
        </a>
    </#if>

    <div class="chart-group tabs c-tabs no-print" data-name="${isSummary?then('', directory)}" id="chart-${_gid}">
        <ul class="c-tabs-nav">
            <li class="c-tabs-nav-link img-tab c-is-active">
                <a href="#CPU-${_gid}">CPU</a>
            </li>
            <li class="c-tabs-nav-link img-tab">
                <a href="#Memory-${_gid}">Memory</a>
            </li>
            <li class="c-tabs-nav-link img-tab">
                <a href="#Threads-${_gid}">Threads</a>
            </li>
        </ul>

        <#if !isSummary>
            <a href="#tableEntry-${_gid}" class="backlink">Back to Table</a>
        </#if>

        <div id="CPU-${_gid}" class="c-tab img-tab c-is-active">
            <div class="c-tab-content chart">
                <img src="charts/agents/${directory}/CpuUsage.webp" alt="charts/agents/${directory}/CpuUsage.webp" loading="lazy"/>
            </div>
        </div>

        <div id="Memory-${_gid}" class="c-tab img-tab memory">
            <div class="c-tab-content chart">
                <img src="charts/placeholder.webp" alt="charts/agents/${directory}/MemoryUsage.webp"/>
            </div>
        </div>

        <div id="Threads-${_gid}" class="c-tab img-tab">
            <div class="c-tab-content chart">
                <img src="charts/placeholder.webp" alt="charts/agents/${directory}/Threads.webp"/>
            </div>
        </div>
    </div>

    <div class="chart-group print">
        <div class="chart">
            <h5>Memory</h5>
            <img alt="charts/agents/${directory}/MemoryUsage.webp"/>
        </div>

        <div class="chart">
            <h5>CPU</h5>
            <img alt="charts/agents/${directory}/CpuUsage.webp"/>

            <h5>Threads</h5>
            <img alt="charts/agents/${directory}/Threads.webp"/>
        </div>
    </div>
</#macro>
