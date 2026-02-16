<#import "/sections/descriptions.ftl" as desc>
<#import "/common/util/load-profile-table.ftl" as lpt>

<#macro load_profile rootNode>
    <div class="section" id="load-profile">
        <@desc.headline_load_profile />

        <div class="content">
            <@desc.description_load_profile />

            <div class="data">
                <@lpt.load_profile_table rootNode=rootNode.loadProfile />
            </div>
        </div>
    </div>
</#macro>
