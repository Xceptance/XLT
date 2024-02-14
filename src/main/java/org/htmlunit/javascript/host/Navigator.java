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

import static org.htmlunit.BrowserVersionFeatures.JS_NAVIGATOR_DO_NOT_TRACK_UNSPECIFIED;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;
import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.PluginConfiguration;
import org.htmlunit.WebClient;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.host.geo.Geolocation;
import org.htmlunit.javascript.host.media.MediaDevices;
import org.htmlunit.javascript.host.network.NetworkInformation;

/**
 * A JavaScript object for {@code Navigator}.
 *
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author Daniel Gredler
 * @author Chris Erskine
 * @author Ahmed Ashour
 * @author Marc Guillemot
 * @author Frank Danek
 * @author Ronald Brill
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms535867.aspx">MSDN documentation</a>
 */
@JsxClass
public class Navigator extends HtmlUnitScriptable {

    private PluginArray plugins_;
    private MimeTypeArray mimeTypes_;
    private MediaDevices mediaDevices_;

    /**
     * Creates an instance.
     */
    public Navigator() {
    }

    /**
     * JavaScript constructor.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
    }

    /**
     * Returns the {@code appCodeName} property.
     * @return the {@code appCodeName} property
     */
    @JsxGetter
    public String getAppCodeName() {
        return getBrowserVersion().getApplicationCodeName();
    }

    /**
     * Returns the {@code appMinorVersion} property.
     * @return the {@code appMinorVersion} property
     */
    @JsxGetter(IE)
    public String getAppMinorVersion() {
        return getBrowserVersion().getApplicationMinorVersion();
    }

    /**
     * Returns the {@code appName} property.
     * @return the {@code appName} property
     */
    @JsxGetter
    public String getAppName() {
        return getBrowserVersion().getApplicationName();
    }

    /**
     * Returns the {@code appVersion} property.
     * @return the {@code appVersion} property
     */
    @JsxGetter
    public String getAppVersion() {
        return getBrowserVersion().getApplicationVersion();
    }

    /**
     * Returns the language of the browser.
     * @return the language
     */
    @JsxGetter(IE)
    public String getBrowserLanguage() {
        return getLanguage();
    }

    /**
     * Returns the language of the browser.
     * @return the language
     */
    @JsxGetter
    public String getLanguage() {
        return getBrowserVersion().getBrowserLanguage();
    }

    /**
     * Returns the language of the browser.
     * @return the language
     */
    @JsxGetter({CHROME, EDGE, FF, FF_ESR})
    public Scriptable getLanguages() {
        final String acceptLang = getBrowserVersion().getAcceptLanguageHeader();
        if (StringUtils.isEmpty(acceptLang)) {
            return JavaScriptEngine.newArray(this, 0);
        }

        final ArrayList<String> res = new ArrayList<>();
        final String[] parts = org.htmlunit.util.StringUtils.splitAtComma(acceptLang);
        for (final String part : parts) {
            if (!StringUtils.isEmpty(part)) {
                final String lang = StringUtils.substringBefore(part, ";").trim();
                if (!StringUtils.isEmpty(part)) {
                    res.add(lang);
                }
            }
        }

        return JavaScriptEngine.newArray(this, res.toArray());
    }

    /**
     * Returns the {@code cookieEnabled} property.
     * @return the {@code cookieEnabled} property
     */
    @JsxGetter
    public boolean isCookieEnabled() {
        return getWindow().getWebWindow().getWebClient().getCookieManager().isCookiesEnabled();
    }

    /**
     * Returns the {@code cpuClass} property.
     * @return the {@code cpuClass} property
     */
    @JsxGetter(IE)
    public String getCpuClass() {
        return getBrowserVersion().getCpuClass();
    }

    /**
     * Returns the {@code onLine} property.
     * @return the {@code onLine} property
     */
    @JsxGetter
    public boolean isOnLine() {
        return getBrowserVersion().isOnLine();
    }

    /**
     * Returns the {@code platform} property.
     * @return the {@code platform} property
     */
    @JsxGetter
    public String getPlatform() {
        return getBrowserVersion().getPlatform();
    }

    /**
     * Returns the {@code product} property.
     * @return the {@code product} property
     */
    @JsxGetter
    public String getProduct() {
        return "Gecko";
    }

    /**
     * Returns the build number of the current browser.
     * @see <a href="https://developer.mozilla.org/en/navigator.productSub">Mozilla Doc</a>
     * @return false
     */
    @JsxGetter({CHROME, EDGE, FF, FF_ESR})
    public String getProductSub() {
        return getBrowserVersion().getProductSub();
    }

    /**
     * Returns the property {@code systemLanguage}.
     * @return the property {@code systemLanguag}
     */
    @JsxGetter(IE)
    public String getSystemLanguage() {
        return getBrowserVersion().getSystemLanguage();
    }

    /**
     * Returns the property {@code userAgent}.
     * @return the property {@code userAgent}
     */
    @JsxGetter
    public String getUserAgent() {
        return getBrowserVersion().getUserAgent();
    }

    /**
     * Returns the property {@code userLanguage}.
     * @return the property {@code userLanguage}
     */
    @JsxGetter(IE)
    public String getUserLanguage() {
        return getBrowserVersion().getUserLanguage();
    }

    /**
     * Returns an empty array because HtmlUnit does not support embedded objects.
     * @return an empty array
     */
    @JsxGetter
    public Object getPlugins() {
        initPlugins();
        return plugins_;
    }

    private void initPlugins() {
        if (plugins_ != null) {
            return;
        }
        plugins_ = new PluginArray();
        plugins_.setParentScope(this);
        plugins_.setPrototype(getPrototype(PluginArray.class));

        mimeTypes_ = new MimeTypeArray();
        mimeTypes_.setParentScope(this);
        mimeTypes_.setPrototype(getPrototype(MimeTypeArray.class));

        for (final PluginConfiguration pluginConfig : getBrowserVersion().getPlugins()) {
            final Plugin plugin = new Plugin(pluginConfig.getName(), pluginConfig.getDescription(),
                    pluginConfig.getVersion(), pluginConfig.getFilename());
            plugin.setParentScope(this);
            plugin.setPrototype(getPrototype(Plugin.class));
            plugins_.add(plugin);

            for (final PluginConfiguration.MimeType mimeTypeConfig : pluginConfig.getMimeTypes()) {
                final MimeType mimeType = new MimeType(mimeTypeConfig.getType(), mimeTypeConfig.getDescription(),
                    mimeTypeConfig.getSuffixes(), plugin);
                mimeType.setParentScope(this);
                mimeType.setPrototype(getPrototype(MimeType.class));
                mimeTypes_.add(mimeType);
                plugin.add(mimeType);
            }
        }
    }

    /**
     * Returns the {@code mimeTypes} property.
     * @return the {@code mimeTypes} property
     */
    @JsxGetter
    public Object getMimeTypes() {
        initPlugins();
        return mimeTypes_;
    }

    /**
     * Indicates if Java is enabled.
     * @return true/false (see {@link org.htmlunit.WebClientOptions#isAppletEnabled()}
     */
    @JsxFunction
    public boolean javaEnabled() {
        return getWindow().getWebWindow().getWebClient().getOptions().isAppletEnabled();
    }

    /**
     * Returns {@code false} always as data tainting support is not enabled in HtmlUnit.
     * @return false
     */
    @JsxFunction({FF, FF_ESR, IE})
    public boolean taintEnabled() {
        return false;
    }

    /**
     * Returns the {@code geolocation} property.
     * @return the {@code geolocation} property
     */
    @JsxGetter
    public Geolocation getGeolocation() {
        final Geolocation geolocation = new Geolocation();
        geolocation.setPrototype(getPrototype(geolocation.getClass()));
        geolocation.setParentScope(getParentScope());
        return geolocation;
    }

    /**
     * Returns the {@code buildID} property.
     * @return the {@code buildID} property
     */
    @JsxGetter({FF, FF_ESR})
    public String getBuildID() {
        return getBrowserVersion().getBuildId();
    }

    /**
     * Returns the {@code vendor} property.
     * @return the {@code vendor} property
     */
    @JsxGetter
    public String getVendor() {
        return getBrowserVersion().getVendor();
    }

    /**
     * Returns the {@code vendorSub} property.
     * @return the {@code vendorSub} property
     */
    @JsxGetter({CHROME, EDGE, FF, FF_ESR})
    public String getVendorSub() {
        return "";
    }

    /**
     * Returns the {@code doNotTrack} property.
     * @return the {@code doNotTrack} property
     */
    @JsxGetter({CHROME, EDGE, FF, FF_ESR})
    public Object getDoNotTrack() {
        final WebClient client = getWindow().getWebWindow().getWebClient();
        if (client.getOptions().isDoNotTrackEnabled()) {
            return 1;
        }
        if (client.getBrowserVersion().hasFeature(JS_NAVIGATOR_DO_NOT_TRACK_UNSPECIFIED)) {
            return "unspecified";
        }
        return null;
    }

    /**
     * Returns the {@code oscpu} property.
     * @return the {@code oscpu} property
     */
    @JsxGetter({FF, FF_ESR})
    public String getOscpu() {
        return "Windows NT 6.1";
    }

    /**
     * Returns the {@code connection} property.
     * @return the {@code connection} property
     */
    @JsxGetter({CHROME, EDGE})
    public NetworkInformation getConnection() {
        final NetworkInformation networkInformation = new NetworkInformation();
        networkInformation.setPrototype(getPrototype(networkInformation.getClass()));
        networkInformation.setParentScope(getParentScope());
        return networkInformation;
    }

    /**
     * Returns the {@code mimeTypes} property.
     * @return the {@code mimeTypes} property
     */
    @JsxGetter({CHROME, EDGE, FF, FF_ESR})
    public MediaDevices getMediaDevices() {
        if (mediaDevices_ == null) {
            mediaDevices_ = new MediaDevices();
            mediaDevices_.setPrototype(getPrototype(mediaDevices_.getClass()));
            mediaDevices_.setParentScope(getParentScope());
        }
        return mediaDevices_;
    }
}
