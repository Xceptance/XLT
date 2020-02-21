<#escape x as x?html>
<#import "layout/defaultLayout.ftl" as myLayout />

<#macro makeCard cardTitle="" cardContent="">
  <div class="col l3 m6 s12">
    <div class="card teal lighten-1">
      <div class="card-content white-text center">
        <span class="card-title">${cardTitle}</span>
        <p>${cardContent}</p>
      </div><!-- /.card-content -->
    </div><!-- /.card -->
  </div><!-- /.col -->
</#macro>

<@myLayout.layout pageId="general">
  <div class="section" id="general">
    <div class="section-title">
      <h1>General Information</h1>
    </div><!-- /.section-title -->
    <div class="section-content">
      <div id="suite-info">
        <div class="row">
          <div class="col s12">
            <div class="z-depth-1">
              <div class="suite-name grey darken-1 white-text">${suite.name}</div>
              <div class="white description">
                <#noescape>${suite.descriptionMarkup!"<p class=\"empty\">Your testsuite has no description!</p>"}</#noescape>
              </div>
            </div><!-- /.z-depth-1 -->
          </div><!-- /.col -->
        </div><!-- /.row -->
      </div><!-- /suite-info -->
      <div id="statistics">
        <div class="row">
          <@makeCard suite.tests?size "Test Case Scripts" />
          <@makeCard suite.modules?size "Script Modules" />
          <@makeCard suite.javaModules?size "Java Modules" />
          <@makeCard suite.packages?size "Script Packages" />
        </div><!-- /.row -->
      </div><!-- /statistics -->
    </div><!-- /.section-content -->
  </div><!-- /.section -->
    
</@myLayout.layout>
</#escape>
