/*
 * Copyright (c) 2002-2022 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit.libraries;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.gargoylesoftware.htmlunit.WebDriverTestCase;
import com.gargoylesoftware.htmlunit.junit.BrowserRunner;

/**
 * Tests for compatibility with <a href="http://mochikit.com">MochiKit</a>.
 * <p>
 * Note: the tests test_MochiKit-DOM-Safari.html, test_MochiKit-DragAndDrop.html and test_MochiKit-JSAN.html
 * are not run as they don't even pass in a "real" Firefox 3.
 * </p>
 * @author Marc Guillemot
 * @author Frank Danek
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class MochiKitTest extends WebDriverTestCase {

    private static final String BASE_FILE_PATH = "libraries/MochiKit/1.4.1";

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void async() throws Exception {
        doTest("Async");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void base() throws Exception {
        doTest("Base");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void color() throws Exception {
        doTest("Color");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void dateTime() throws Exception {
        doTest("DateTime");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void DOM() throws Exception {
        doTest("DOM");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void format() throws Exception {
        doTest("Format");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void iter() throws Exception {
        doTest("Iter");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void logging() throws Exception {
        doTest("Logging");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void mochiKit() throws Exception {
        doTest("MochiKit");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void selector() throws Exception {
        doTest("Selector");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void signal() throws Exception {
        doTest("Signal");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void style() throws Exception {
        doTest("Style");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void visual() throws Exception {
        doTest("Visual");
    }

    private void doTest(final String testName) throws Exception {
        final String url = URL_FIRST + "tests/test_MochiKit-" + testName + ".html";
        assertNotNull(url);

        final WebDriver driver = getWebDriver();
        driver.get(url);

        // make single test results visible
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.findElement(By.linkText("Toggle passed tests")).click();
        driver.findElement(By.linkText("Toggle failed tests")).click();
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);

        String expected = loadExpectation(testName);
        expected = expected.trim();
        expected = StringUtils.replace(expected, "\r\n", "\n");
        final WebElement div = driver.findElement(By.xpath("//div[@class = 'tests_report']"));

        assertNotNull(div);
        String actual = div.getText().trim();
        actual = StringUtils.replace(actual, "\n\n", "\n");
        assertEquals(expected.trim(), actual);
    }

    private String loadExpectation(final String testName) throws Exception {
        final String resourcePrefix = "/" + BASE_FILE_PATH + "/test-" + testName;
        return loadExpectation(resourcePrefix, ".expected.txt");
    }

    /**
     * Performs pre-test initialization.
     * @throws Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception {
        startWebServer("src/test/resources/libraries/MochiKit/1.4.1", null, null);
    }
}
