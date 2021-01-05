/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.engine.clientperformance;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.xceptance.common.io.FileUtils;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltChromeDriver;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.resultbrowser.RequestHistory.DumpMode;
import com.xceptance.xlt.engine.util.DefaultWebDriverFactory;

@SuppressWarnings("restriction")
public class _3016_XltChromeDriverTest extends AbstractWebDriverScriptTestCase
{
    private HttpServer server;

    private static String IMAGE = "1234";

    public _3016_XltChromeDriverTest()
    {
        super();
        XltProperties.getInstance().setProperty("xlt.webDriver", getWebdriverName());
        WebDriver driver = DefaultWebDriverFactory.getWebDriver();
        if (!getWebdriverClass().equals(driver.getClass()))
        {
            driver.quit();
            throw new Error("Expected " + getWebdriverClass().getSimpleName() + " but got " + driver.getClass().getSimpleName());
        }
        setWebDriver(driver);
    }

    public String getWebdriverName()
    {
        return "chrome_clientperformance";
    }

    public Class<? extends WebDriver> getWebdriverClass()
    {
        return XltChromeDriver.class;
    }

    @BeforeClass
    public static void beforeClass() throws IOException
    {
        // ensure a new session
        SessionImpl.removeCurrent();

        SessionImpl.getCurrent().getRequestHistory().setDumpMode(DumpMode.ALWAYS);
    }

    @Before
    public void before() throws IOException
    {
        server = startServer();

        File resultsDir = getResultsDir();
        FileUtils.deleteFile(resultsDir);
    }

    @Test
    public void test() throws Exception
    {
        startAction("Test");
        open("http://admin:admin@localhost:" + server.getAddress().getPort() + "/image.png");
    }

    @After
    public void after() throws Exception
    {
        getWebDriver().quit();
    }

    @AfterClass
    public static void afterClass() throws Exception
    {
        try
        {
            validateDataJsonResults();
        }
        finally
        {
            // clean-up
            SessionImpl.removeCurrent();
        }
    }

    private static HttpServer startServer() throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        HttpContext context = server.createContext("/", new HttpHandler()
        {
            @Override
            public void handle(HttpExchange exchange) throws IOException
            {
                String url = exchange.getRequestURI().getPath();
                if (url.endsWith("/image.png"))
                {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "image/png");

                    sendResponse(exchange, 200, IMAGE, headers);
                }
                else
                {
                    System.out.println("Unknown: " + exchange.getRequestURI().toURL().toExternalForm());
                    sendResponse(exchange, 404, "", new HashMap<>());
                }
            }
        });

        context.setAuthenticator(new BasicAuthenticator("test")
        {
            @Override
            public boolean checkCredentials(String user, String password)
            {
                return "admin".equals(user) && "admin".equals(password);
            }

            @Override
            public Result authenticate(HttpExchange exchange)
            {
                exchange.getResponseHeaders().add("Content-Type", "text/html;charset=ISO-8859-1");
                return super.authenticate(exchange);
            }
        });

        server.setExecutor(null); // creates a default executor
        server.start();

        return server;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String content, Map<String, String> headers) throws IOException
    {
        byte[] data = content.getBytes();

        exchange.getResponseHeaders().clear();
        for (Entry<String, String> eachHeader : headers.entrySet())
        {
            exchange.getResponseHeaders().add(eachHeader.getKey(), eachHeader.getValue());
        }

        exchange.sendResponseHeaders(statusCode, data.length);

        OutputStream os = exchange.getResponseBody();
        os.write(data);
        os.close();
    }

    private static File getResultsDir()
    {
        return SessionImpl.getCurrent().getResultsDirectory();
    }

    private static void validateDataJsonResults() throws Exception
    {
        File results = new File(getResultsDir(), "output");
        File[] children = results.listFiles();
        File resultFolder = null;
        for (File eachChild : children)
        {
            if (eachChild.isDirectory())
            {
                if (resultFolder == null)
                {
                    resultFolder = eachChild;
                }
                else
                {
                    throw new Exception("Too many results found");
                }
            }
        }

        if (resultFolder == null)
        {
            throw new Exception("No results found");
        }

        File dataFile = new File(resultFolder, "data.js");
        String data = new String(Files.readAllBytes(dataFile.toPath()));
        String json = RegExUtils.getFirstMatch(data, "\\{.*\\}");
        JSONObject dataJason = new JSONObject(json);
        JSONArray requests = dataJason.getJSONArray("actions").getJSONObject(0).getJSONArray("requests");

        {
            String mimeType = requests.getJSONObject(0).getString("mimeType");
            if (!"image/png".equals(mimeType))
            {
                throw new Exception("Expected image/png content type but got " + mimeType);
            }
        }
    }
}
