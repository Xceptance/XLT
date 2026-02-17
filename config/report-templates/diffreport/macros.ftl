<#macro show_n_hide gid>
    <span id="more-${gid}-show" onclick="$('#more-${gid}').show();$('#more-${gid}-hide').show(); $(this).hide();"
        class="link more-show">More...</span>
    <span id="more-${gid}-hide" onclick="$('#more-${gid}').hide();$('#more-${gid}-show').show(); $(this).hide();"
        class="link more-hide">Hide...</span>
</#macro>
