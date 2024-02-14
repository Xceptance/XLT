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
package org.htmlunit.javascript.host;

import java.util.HashSet;
import java.util.Set;

import org.htmlunit.PluginConfiguration;
import org.htmlunit.SimpleWebTestCase;
import org.htmlunit.junit.BrowserRunner;
import org.htmlunit.junit.BrowserRunner.Alerts;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link MimeType}.
 *
 * @author Marc Guillemot
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class MimeTypeTest extends SimpleWebTestCase {

    /**
     * Tests default configuration of Flash plugin for Firefox.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"[object MimeType]", "swf", "Shockwave Flash", "true", "true"},
            CHROME = "undefined")
    public void flashMimeType() throws Exception {
        final String html = "<html><head><script>\n"
            + "function test() {\n"
            + "  var mimeTypeFlash = navigator.mimeTypes['application/x-shockwave-flash'];\n"
            + "  alert(mimeTypeFlash);\n"
            + "  if (mimeTypeFlash) {\n"
            + "    alert(mimeTypeFlash.suffixes);\n"
            + "    var pluginFlash = mimeTypeFlash.enabledPlugin;\n"
            + "    alert(pluginFlash.name);\n"
            + "    alert(pluginFlash == navigator.plugins[pluginFlash.name]);\n"
            + "    alert(pluginFlash == navigator.plugins.namedItem(pluginFlash.name));\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'></body></html>";

        loadPage(html);
    }

    /**
     * Tests default configuration of Flash plugin for Firefox.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"undefined", "undefined", "null"})
    public void removeFlashMimeType() throws Exception {
        final String html = "<html><head><script>\n"
            + "function test() {\n"
            + "  var mimeTypeFlash = navigator.mimeTypes['application/x-shockwave-flash'];\n"
            + "  alert(mimeTypeFlash);\n"
            + "  alert(navigator.plugins['Shockwave Flash']);\n"
            + "  alert(navigator.plugins.namedItem('Shockwave Flash'));\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'></body></html>";

        final Set<PluginConfiguration> plugins = new HashSet<>(getBrowserVersion().getPlugins());
        getBrowserVersion().getPlugins().clear();
        try {
            loadPage(html);
        }
        finally {
            getBrowserVersion().getPlugins().addAll(plugins);
        }
    }
}
