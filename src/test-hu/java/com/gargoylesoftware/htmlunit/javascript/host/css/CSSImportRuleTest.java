/*
 * Copyright (c) 2002-2021 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.javascript.host.css;

import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.WebDriverTestCase;
import com.gargoylesoftware.htmlunit.util.MimeType;

/**
 * Tests for {@link CSSImportRule}.
 *
 * @author Daniel Gredler
 * @author Marc Guillemot
 * @author Ronald Brill
 * @author Guy Burton
 * @author Frank Danek
 */
@RunWith(BrowserRunner.class)
public class CSSImportRuleTest extends WebDriverTestCase {

    /**
     * Regression test for Bug #789.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = {"[object CSSImportRule]", "§§URL§§second/", "", "0", "[object CSSStyleSheet]"},
            IE = {"[object CSSImportRule]", "§§URL§§second/",
                "all", "0", "[object CSSStyleSheet]"})
    public void getImportFromCssRulesCollection_absolute() throws Exception {
        expandExpectedAlertsVariables(URL_FIRST);
        getImportFromCssRulesCollection(URL_FIRST, URL_SECOND.toExternalForm(), URL_SECOND);
    }

    /**
     * Regression test for Bug #789.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = {"[object CSSImportRule]", "foo.css", "", "0", "[object CSSStyleSheet]"},
            IE = {"[object CSSImportRule]", "foo.css", "all", "0", "[object CSSStyleSheet]"})
    public void getImportFromCssRulesCollection_relative() throws Exception {
        final URL urlPage = new URL(URL_FIRST, "/dir1/dir2/foo.html");
        final URL urlCss = new URL(URL_FIRST, "/dir1/dir2/foo.css");
        getImportFromCssRulesCollection(urlPage, "foo.css", urlCss);
    }

    private void getImportFromCssRulesCollection(final URL pageUrl, final String cssRef, final URL cssUrl)
        throws Exception {
        final String html
            = "<html><body>\n"
            + "<style>@import url('" + cssRef + "');</style><div id='d'>foo</div>\n"
            + "<script>\n"
            + "  var item = document.styleSheets.item(0);\n"
            + "  if (item.cssRules) {\n"
            + "    var r = item.cssRules[0];\n"
            + "    alert(r);\n"
            + "    alert(r.href);\n"
            + "    alert(r.media);\n"
            + "    alert(r.media.length);\n"
            + "    alert(r.styleSheet);\n"
            + "  } else {\n"
            + "    alert('cssRules undefined');\n"
            + "  }\n"
            + "</script>\n"
            + "</body></html>";
        final String css = "#d { color: green }";

        getMockWebConnection().setResponse(cssUrl, css, MimeType.TEXT_CSS);
        loadPageWithAlerts2(html, pageUrl);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void importedStylesheetsLoaded() throws Exception {
        final String html
            = "<html><body>\n"
            + "<style>@import url('" + URL_SECOND + "');</style>\n"
            + "<div id='d'>foo</div>\n"
            + "<script>\n"
            + "var d = document.getElementById('d');\n"
            + "var s = window.getComputedStyle(d, null);\n"
            + "alert(s.color.indexOf('128') > 0);\n"
            + "</script>\n"
            + "</body></html>";
        final String css = "#d { color: rgb(0, 128, 0); }";

        getMockWebConnection().setResponse(URL_SECOND, css, MimeType.TEXT_CSS);

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void importedStylesheetsURLResolution() throws Exception {
        final String html = "<html><head>\n"
            + "<link rel='stylesheet' type='text/css' href='dir1/dir2/file1.css'></link>\n"
            + "<body>\n"
            + "<div id='d'>foo</div>\n"
            + "<script>\n"
            + "var d = document.getElementById('d');\n"
            + "var s = window.getComputedStyle(d, null);\n"
            + "alert(s.color.indexOf('128') > 0);\n"
            + "</script>\n"
            + "</body></html>";
        final String css1 = "@import url('file2.css');";
        final String css2 = "#d { color: rgb(0, 128, 0); }";

        final URL urlPage = URL_FIRST;
        final URL urlCss1 = new URL(urlPage, "dir1/dir2/file1.css");
        final URL urlCss2 = new URL(urlPage, "dir1/dir2/file2.css");
        getMockWebConnection().setResponse(urlCss1, css1, MimeType.TEXT_CSS);
        getMockWebConnection().setResponse(urlCss2, css2, MimeType.TEXT_CSS);

        loadPageWithAlerts2(html, urlPage);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void circularImportedStylesheets() throws Exception {
        final String html = "<html><head>\n"
            + "<link rel='stylesheet' type='text/css' href='dir1/dir2/file1.css'></link>\n"
            + "<body>\n"
            + "<div id='d'>foo</div>\n"
            + "<script>\n"
            + "  var d = document.getElementById('d');\n"
            + "  var s = window.getComputedStyle(d, null);\n"
            + "  alert(s.color.indexOf('128') > 0);\n"
            + "</script>\n"
            + "</body></html>";

        final String css1 = "@import url('file2.css');";
        final String css2 = "@import url('file1.css');\n"
            + "#d { color: rgb(0, 128, 0); }";

        final URL urlPage = URL_FIRST;
        final URL urlCss1 = new URL(urlPage, "dir1/dir2/file1.css");
        final URL urlCss2 = new URL(urlPage, "dir1/dir2/file2.css");
        getMockWebConnection().setResponse(urlCss1, css1, MimeType.TEXT_CSS);
        getMockWebConnection().setResponse(urlCss2, css2, MimeType.TEXT_CSS);

        loadPageWithAlerts2(html, urlPage);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({"true", "true", "true"})
    public void circularImportedStylesheetsComplexCase() throws Exception {
        // TODO [IE]SINGLE-VS-BULK test runs when executed as single but breaks as bulk
        shutDownRealIE();

        final String html = "<html><head>\n"
            + "<link rel='stylesheet' type='text/css' href='dir1/dir2/file1.css'></link>\n"
            + "<body>\n"
            + "<div id='d'>foo</div>\n"
            + "<div id='e'>foo</div>\n"
            + "<div id='f'>foo</div>\n"
            + "<script>\n"
            + "var d = document.getElementById('d');\n"
            + "var s = window.getComputedStyle(d, null);\n"
            + "alert(s.color.indexOf('128') > 0);\n"
            + "var e = document.getElementById('e');\n"
            + "s = window.getComputedStyle(e, null);\n"
            + "alert(s.color.indexOf('127') > 0);\n"
            + "var f = document.getElementById('f');\n"
            + "s = window.getComputedStyle(f, null);\n"
            + "alert(s.color.indexOf('126') > 0);\n"
            + "</script>\n"
            + "</body></html>";
        final String css1 = "@import url('file2.css');";
        final String css2 = "@import url('file3.css');\n"
            + "@import url('file4.css');";
        final String css3 = "#d { color: rgb(0, 128, 0); }";
        final String css4 = "@import url('file5.css');\n"
            + "#e { color: rgb(0, 127, 0); }";
        final String css5 = "@import url('file2.css');\n"
            + "#f { color: rgb(0, 126, 0); }";

        final URL urlPage = URL_FIRST;
        final URL urlCss1 = new URL(urlPage, "dir1/dir2/file1.css");
        final URL urlCss2 = new URL(urlPage, "dir1/dir2/file2.css");
        final URL urlCss3 = new URL(urlPage, "dir1/dir2/file3.css");
        final URL urlCss4 = new URL(urlPage, "dir1/dir2/file4.css");
        final URL urlCss5 = new URL(urlPage, "dir1/dir2/file5.css");
        getMockWebConnection().setResponse(urlCss1, css1, MimeType.TEXT_CSS);
        getMockWebConnection().setResponse(urlCss2, css2, MimeType.TEXT_CSS);
        getMockWebConnection().setResponse(urlCss3, css3, MimeType.TEXT_CSS);
        getMockWebConnection().setResponse(urlCss4, css4, MimeType.TEXT_CSS);
        getMockWebConnection().setResponse(urlCss5, css5, MimeType.TEXT_CSS);

        loadPageWithAlerts2(html, urlPage);
    }

    /**
     * Test that media specific imports work correctly.
     * Should import the first stylesheet and not the second
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("42px")
    public void importedStylesheetsLoadedAccordingToMediaType() throws Exception {
        // TODO [IE]SINGLE-VS-BULK test runs when executed as single but breaks as bulk
        shutDownRealIE();

        final String html
            = "<html><head>\n"
            + "  <style>\n"
            + "    @import url('" + URL_SECOND  + "');\n"
            + "    @import url('" + URL_THIRD + "') print;\n"
            + "  </style>\n"
            + "</head>\n"

            + "<body>\n"
            + "  <div id='d'>foo</div>\n"
            + "  <script>\n"
            + "    var d = document.getElementById('d');\n"
            + "    var s = window.getComputedStyle(d, null);\n"
            + "    alert(s.fontSize);\n"
            + "</script>\n"
            + "</body></html>";
        final String screenCss = "#d { font-size: 42px; }";
        final String printCss  = "#d { font-size: 13px; }";

        getMockWebConnection().setResponse(URL_SECOND, screenCss, MimeType.TEXT_CSS);
        getMockWebConnection().setResponse(URL_THIRD, printCss, MimeType.TEXT_CSS);

        loadPageWithAlerts2(html);
    }
}
