/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
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
package org.htmlunit.javascript.regexp;

import org.htmlunit.WebDriverTestCase;
import org.htmlunit.html.HtmlTextInput;
import org.htmlunit.junit.BrowserRunner;
import org.htmlunit.junit.BrowserRunner.Alerts;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Tests for {@link HtmlTextInput} validation based on regex.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class RegExpJsToJavaConverter2Test extends WebDriverTestCase {

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void validationPattern() throws Exception {
        validation("1234*", "123");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("false")
    public void validationPatternFailed() throws Exception {
        validation("1234*", "1235");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void validationPatternUnicode() throws Exception {
        validation("123\\u0077*", "123\u0077\u0077");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void validationPatternUnicodeWrong() throws Exception {
        validation("123\\u77 a*", "123\u0077 ");
        validation("123\\u77a*", "123\u0077aaaa");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void validationPatternUnicodeCodePointEscapes() throws Exception {
        validation("123\\u{1D306}", "123&#x1D306;");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("false")
    public void validationPatternUnicodeCodePointEscapesFails() throws Exception {
        validation("123\\u{1D306}", "123&#x1D307;");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void validationPatternUnicodePropertyEscapeLetter() throws Exception {
        validation("\\p{L}*", "Html");
        validation("\\p{L}*", "&#x043C;&#x0439;&#x0440;");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void validationPatternUnicodePropertyEscapeUppercaseLetter() throws Exception {
        validation("\\p{Lu}*", "HTML");
        validation("\\p{Lu}*", "&#x041C;&#x0419;&#x0420;");
        validation("\\p{uppercase letter}*", "&#x041C;&#x0419;&#x0420");
        validation("\\p{Uppercase Letter}*", "&#x041C;&#x0419;&#x0420");
        validation("\\p{Uppercase_Letter}*", "&#x041C;&#x0419;&#x0420");
        validation("\\p{Uppercase-Letter}*", "&#x041C;&#x0419;&#x0420");
        validation("\\p{uppercaseletter}*", "&#x041C;&#x0419;&#x0420");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void validationPatternUnicodePropertyEscapeLowercaseLetter() throws Exception {
        validation("\\p{Ll}*", "html");
        validation("\\p{Ll}*", "&#x043C;&#x0439;&#x0440;");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("true")
    public void validationPatternUnicodePropertyEscapePrivateUse() throws Exception {
        validation("[\\p{gc=Co}]+", "&#xE000;&#xF8FF;");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("false")
    public void validationPatternUnicodePropertyEscapeOther() throws Exception {
        validation("^\\p{gc=Cn}", "a");
    }

    private void validation(final String pattern, final String value) throws Exception {
        final String html =
                "<html><head>\n"
                + "  <script>\n"
                + LOG_TITLE_FUNCTION
                + "    function test() {\n"
                + "      var elem = document.getElementById('e1');\n"
                + "      log(elem.validity.valid);\n"
                + "    }\n"
                + "  </script>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <input type='text' id='e1' name='k' pattern='" + pattern + "' value='" + value + "'>\n"
                + "  <button id='myTest' type='button' onclick='test()'>Test</button>\n"
                + "</body></html>";

        expandExpectedAlertsVariables(URL_FIRST);

        final WebDriver driver = loadPage2(html, URL_FIRST);

        driver.findElement(By.id("myTest")).click();
        verifyTitle2(driver, getExpectedAlerts()[0]);
    }
}
