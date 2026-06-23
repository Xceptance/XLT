<#-- Replace forbidden chars by $<HEX> where <HEX> is the ASCII hex representation of that character -->
<#function convertIllegalCharactersInFileName filename>
    <#local result = filename>
    <#-- Dollar sign must be first replacement -->
    <#local result = result?replace('$', '$24')>
    <#local result = result?replace('?', '$3f')>
    <#local result = result?replace(':', '$3a')>
    <#local result = result?replace('/', '$2f')>
    <#local result = result?replace('#', '$23')>
    <#local result = result?replace('%', '$25')>
    <#local result = result?replace(',', '$2c')>
    <#local result = result?replace(';', '$3b')>
    <#local result = result?replace('*', '$2a')>
    <#local result = result?replace('|', '$7c')>
    <#local result = result?replace('\\', '$5c')>
    <#local result = result?replace('<', '$3c')>
    <#local result = result?replace('>', '$3e')>
    <#local result = result?replace('"', '$22')>
    <#return result>
</#function>
