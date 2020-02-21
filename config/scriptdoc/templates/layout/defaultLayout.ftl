<#macro layout pageCSS="" pageJS="" title="ScriptDoc - ${suite.name}" sidebar="" pageId="">
<#local sections = [{"id":"general","target":"index.html","text":"Overview"}, 
                    {"id":"testdata","text":"Test Data"},
                    {"id":"packages","text":"Packages"},
                    {"id":"tests","text":"Tests"},
                    {"id":"modules","text":"Modules"}] />
<#escape x as x?html>
<!doctype html>
<html>
<head>

    <!-- Basic Page Needs -->
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${title}</title>

    <!-- Mobile Specific Meta -->
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">

    <!-- CSS -->
    <link rel="stylesheet" href="css/materialize.css">
    <link rel="stylesheet" href="css/custom.css">
    

    <!--[if lt IE 9]>
        <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <!-- Favicons -->
    <link rel="shortcut icon" href="images/favicon.ico">

    <#noescape>${pageCSS}</#noescape>
</head>
<body>

  <header>
    <div class="container">
      <div class="header row">
        <nav class="navbar">
          <div class="navbar-wrapper">
            <a href="#" data-activates="mobile-nav" class="button-collapse hide-on-med-and-up"><i class="mdi-navigation-menu"></i></a>
            <ul class="left hide-on-small-only">
            <#list sections as section>
              <#local classAtt = "button" />
              <#local hrefAtt = section.target!(section.id+".html") />
              <#if section.id = pageId>
                <#local classAtt = classAtt + " active" />
              </#if>
              <li><a class="${classAtt}" href="${hrefAtt}">${section.text}</a></li>
            </#list>
            </ul>
            <ul class="side-nav hide-on-med-and-up" id="mobile-nav">
            <#list sections as section>
              <#local classAtt = "button" />
              <#local hrefAtt = section.target!(section.id+".html") />
              <#if section.id = pageId>
                <#local classAtt = classAtt + " active" />
              </#if>
              <li><a class="${classAtt}" href="${hrefAtt}">${section.text}</a></li>
            </#list>
            </ul>
          </div>
        </nav>
      </div><!-- /.header.row -->
    </div><!-- /.container -->
  </header><!-- /header -->

  <main>
    <div class="container">
      <div id="main" class="row">
          <#if sidebar?has_content>
              <div class="col s4" id="nav-column">
                  <div class="sidebar-wrapper">
                      <#noescape>${sidebar}</#noescape>
                  </div>
              </div>
              <div class="col s8" id="content-root">
          <#else>
              <div id="content-root" class="root-left-padding">
          </#if>
              <#nested />
              </div>
      </div><!-- /#main -->
    </div><!-- /.container -->
  </main><!-- /main -->

  <footer>    
    <div id="footer">
      <div class="container">
        <div class="row red darken-4 z-depth-1">
          <div class="footer-copy white-text">Generated with Xceptance LoadTest<sup>&reg;</sup>&nbsp;<span class="product-version">${xlt_version}</span></div>
        </div><!-- /.row -->
      </div><!-- /.container -->
    </div><!-- /#footer -->
  </footer>

  <#-- Default JS comes here! -->
  <script type="text/javascript" src="js/jquery-1.11.2.min.js"></script>
  <#-- JS related to this page follows -->
  <script type="text/javascript" src="js/scriptdoc.js"></script>
  <#noescape>${pageJS}</#noescape>

</body>

</#escape>
</#macro>
