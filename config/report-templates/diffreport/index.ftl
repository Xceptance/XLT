<#import "../common/sections/head.ftl" as head>
<#import "../common/sections/header.ftl" as header>
<#import "../common/sections/footer.ftl" as footer>
<#import "../common/sections/javascript.ftl" as javascript>
<#import "navigation.ftl" as navigation>
<#import "descriptions.ftl" as descriptions>
<#import "sections/comment.ftl" as testcomment>
<#import "sections/load-profile.ftl" as loadProfile>
<#import "sections/general.ftl" as general>

<!DOCTYPE html>
<html lang="en">
<head>
    <@head.head title="XLT Performance Comparison Report - Overview" projectName=projectName configuration=(report.testreport.configuration)!reportConfiguration />
</head>
<body id="diffreport">
<div id="container">
    <div id="content">
        <@header.header navNamespace=navigation title="Performance Comparison Report" productName=productName productVersion=productVersion productUrl=productUrl projectName=projectName />

        <div id="data-content">

            <!--
                ************************************
                * Load Profile
                ************************************
            -->
            <div class="section" id="general">
                <@descriptions.headline_general />

                <div class="content">
                    <@descriptions.description_general />

                    <@descriptions.headline_general_report1 />
                    <@testcomment.render rootNode=report.testreport.testReport1.comments />
                    <@general.render 
                        rootNode=report.testreport.testReport1.general 
                        name=report.testreport.testReport1.name 
                        id="general-report1" />

                    <@loadProfile.render 
                        rootNode=report.testreport.testReport1.loadProfile 
                        id="load-profile-report1" />

                    <@descriptions.headline_general_report2 />
                    <@testcomment.render rootNode=report.testreport.testReport2.comments />
                    <@general.render 
                        rootNode=report.testreport.testReport2.general 
                        name=report.testreport.testReport2.name 
                        id="general-report2" />

                    <@loadProfile.render 
                        rootNode=report.testreport.testReport2.loadProfile 
                        id="load-profile-report2" />
                </div>	
            </div>

        </div> <!-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <!-- content -->
</div> <!-- container -->    

<@javascript.javascript />

</body>
</html>
