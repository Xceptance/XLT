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
package org.htmlunit.javascript.host.geo;

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlunit.BrowserVersion;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebWindow;
import org.htmlunit.corejs.javascript.Function;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.background.BackgroundJavaScriptFactory;
import org.htmlunit.javascript.background.JavaScriptJob;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;

/**
 * A JavaScript object for {@code Geolocation}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass
public class Geolocation extends HtmlUnitScriptable {

    private static final Log LOG = LogFactory.getLog(Geolocation.class);

    /* Do not use this URL without Google permission! */
    private static String PROVIDER_URL_ = "https://maps.googleapis.com/maps/api/browserlocation/json";
    private Function successHandler_;

    /**
     * Creates an instance.
     */
    public Geolocation() {
    }

    /**
     * JavaScript constructor.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
    }

    /**
     * Gets the current position.
     * @param successCallback success callback
     * @param errorCallback optional error callback
     * @param options optional options
     */
    @JsxFunction
    public void getCurrentPosition(final Function successCallback, final Object errorCallback,
            final Object options) {
        successHandler_ = successCallback;

        final WebWindow webWindow = getWindow().getWebWindow();
        if (webWindow.getWebClient().getOptions().isGeolocationEnabled()) {
            final JavaScriptJob job = BackgroundJavaScriptFactory.theFactory()
                    .createJavaScriptJob(0, null, () -> doGetPosition());
            webWindow.getJobManager().addJob(job, webWindow.getEnclosedPage());
        }
    }

    /**
     * Notifies the callbacks whenever the position changes, till clearWatch() is called.
     * @param successCallback success callback
     * @param errorCallback optional error callback
     * @param options optional options
     * @return the watch id
     */
    @JsxFunction
    public int watchPosition(final Function successCallback, final Object errorCallback,
            final Object options) {
        return 0;
    }

    /**
     * Clears the specified watch ID.
     * @param watchId the watch id
     */
    @JsxFunction
    public void clearWatch(final int watchId) {
    }

    void doGetPosition() {
        final String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String wifiStringString = null;
        if (os.contains("win")) {
            wifiStringString = getWifiStringWindows();
        }

        if (wifiStringString == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Operating System not supported: " + os);
            }
        }
        else {
            String url = PROVIDER_URL_;
            if (url.contains("?")) {
                url += '&';
            }
            else {
                url += '?';
            }
            url += "browser=firefox&sensor=true";
            url += wifiStringString;

            while (url.length() >= 1900) {
                url = url.substring(0, url.lastIndexOf("&wifi="));
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("Invoking URL: " + url);
            }

            try (WebClient webClient = new WebClient(BrowserVersion.FIREFOX)) {
                final Page page = webClient.getPage(url);
                final String content = page.getWebResponse().getContentAsString();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Receieved Content: " + content);
                }
                final double latitude = Double.parseDouble(getJSONValue(content, "lat"));
                final double longitude = Double.parseDouble(getJSONValue(content, "lng"));
                final double accuracy = Double.parseDouble(getJSONValue(content, "accuracy"));

                final Coordinates coordinates = new Coordinates(latitude, longitude, accuracy);
                coordinates.setPrototype(getPrototype(coordinates.getClass()));

                final Position position = new Position(coordinates);
                position.setPrototype(getPrototype(position.getClass()));

                final WebWindow ww = getWindow().getWebWindow();
                final JavaScriptEngine jsEngine =
                        (JavaScriptEngine) ww.getWebClient().getJavaScriptEngine();
                jsEngine.callFunction((HtmlPage) ww.getEnclosedPage(), successHandler_, this,
                        getParentScope(), new Object[] {position});
            }
            catch (final Exception e) {
                LOG.error("", e);
            }
        }
    }

    private static String getJSONValue(final String content, final String key) {
        final StringBuilder builder = new StringBuilder();
        int index = content.indexOf("\"" + key + "\"") + key.length() + 2;
        for (index = content.indexOf(':', index) + 1; index < content.length(); index++) {
            final char ch = content.charAt(index);
            if (ch == ',' || ch == '}') {
                break;
            }
            builder.append(ch);
        }
        return builder.toString().trim();
    }

    static String getWifiStringWindows() {
        final StringBuilder builder = new StringBuilder();
        try {
            final List<String> lines = runCommand("netsh wlan show networks mode=bssid");
            for (final Iterator<String> it = lines.iterator(); it.hasNext();) {
                String line = it.next();
                if (line.startsWith("SSID ")) {
                    final String name = line.substring(line.lastIndexOf(' ') + 1);
                    if (it.hasNext()) {
                        it.next();
                    }
                    if (it.hasNext()) {
                        it.next();
                    }
                    if (it.hasNext()) {
                        it.next();
                    }
                    while (it.hasNext()) {
                        line = it.next();
                        if (line.trim().startsWith("BSSID ")) {
                            final String mac = line.substring(line.lastIndexOf(' ') + 1);
                            if (it.hasNext()) {
                                line = it.next().trim();
                                if (line.startsWith("Signal")) {
                                    final String signal = line.substring(line.lastIndexOf(' ') + 1, line.length() - 1);
                                    final int signalStrength = Integer.parseInt(signal) / 2 - 100;
                                    builder.append("&wifi=mac:")
                                        .append(mac.replace(':', '-'))
                                        .append("%7Cssid:")
                                        .append(name)
                                        .append("%7Css:")
                                        .append(signalStrength);
                                }
                            }
                        }
                        if (StringUtils.isBlank(line)) {
                            break;
                        }
                    }
                }
            }
        }
        catch (final IOException e) {
            //
        }
        return builder.toString();
    }

    private static List<String> runCommand(final String command) throws IOException {
        final List<String> list = new ArrayList<>();
        final Process p = Runtime.getRuntime().exec(command);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }

    static void setProviderUrl(final String url) {
        PROVIDER_URL_ = url;
    }
}
