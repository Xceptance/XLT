<#import "../../common/util/load-profile-table.ftl" as loadProfileTable>

<#macro render rootNode id>
    <div class="subsection" id="${id}">
        <div class="content">
            <div class="data">
                <@loadProfileTable.load_profile_table rootNode=rootNode />
            </div>
        </div>
    </div>
</#macro>
