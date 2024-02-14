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
package org.htmlunit.activex.javascript.msxml;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlunit.BrowserVersion;
import org.htmlunit.ScriptException;
import org.htmlunit.WebWindow;
import org.htmlunit.corejs.javascript.Scriptable;

/**
 * ActiveXObjectFactory for the MSXML ActiveX library.
 *
 * @author Frank Danek
 */
public class MSXMLActiveXObjectFactory {

    private static final Log LOG = LogFactory.getLog(MSXMLActiveXObjectFactory.class);

    private MSXMLJavaScriptEnvironment environment_;

    /**
     * Initializes the factory.
     *
     * @param browserVersion the browser version to use
     * @throws Exception if something goes wrong
     */
    public void init(final BrowserVersion browserVersion) throws Exception {
        environment_ = new MSXMLJavaScriptEnvironment(browserVersion);
    }

    /**
     * Checks if the given ActiveX name is supported by this factory.
     *
     * @param activeXName the ActiveX name to check
     * @return true if the given name is supported
     */
    public boolean supports(final String activeXName) {
        return isXMLDOMDocument(activeXName)
            || isXMLHTTPRequest(activeXName)
            || isXSLTemplate(activeXName);
    }

    /**
     * Indicates whether the ActiveX name is one flavor of XMLDOMDocument.
     * @param name the ActiveX name
     * @return {@code true} if this is an XMLDOMDocument
     */
    static boolean isXMLDOMDocument(String name) {
        if (name == null) {
            return false;
        }
        name = name.toLowerCase(Locale.ROOT);
        return "microsoft.xmldom".equals(name)
            || name.startsWith("msxml2.domdocument")
            || name.startsWith("msxml2.freethreadeddomdocument");
    }

    /**
     * Indicates whether the ActiveX name is one flavor of XMLHTTPRequest.
     * @param name the ActiveX name
     * @return {@code true} if this is an XMLHTTPRequest
     */
    static boolean isXMLHTTPRequest(String name) {
        if (name == null) {
            return false;
        }
        name = name.toLowerCase(Locale.ROOT);
        return "microsoft.xmlhttp".equals(name)
            || name.startsWith("msxml2.xmlhttp");
    }

    /**
     * Indicates if the ActiveX name is one flavor of XSLTemplate.
     * @param name the ActiveX name
     * @return {@code true} if this is an XSLTemplate
     */
    static boolean isXSLTemplate(String name) {
        if (name == null) {
            return false;
        }
        name = name.toLowerCase(Locale.ROOT);
        return name.startsWith("msxml2.xsltemplate");
    }

    /**
     * Creates an instance of the ActiveX object for the given name.
     *
     * @param activeXName the ActiveX name to create an object for
     * @param enclosingWindow the enclosing window
     * @return the created ActiveX object
     */
    public Scriptable create(final String activeXName, final WebWindow enclosingWindow) {
        if (isXMLDOMDocument(activeXName)) {
            return createXMLDOMDocument(enclosingWindow);
        }

        if (isXMLHTTPRequest(activeXName)) {
            return createXMLHTTPRequest();
        }

        if (isXSLTemplate(activeXName)) {
            return createXSLTemplate();
        }
        return null;
    }

    private XMLDOMDocument createXMLDOMDocument(final WebWindow enclosingWindow) {
        final XMLDOMDocument document = new XMLDOMDocument(enclosingWindow);
        initObject(document);

        try {
            document.setParentScope(enclosingWindow.getScriptableObject());
        }
        catch (final Exception e) {
            LOG.error("Exception while initializing JavaScript for the page", e);
            throw new ScriptException(null, e); // BUG: null is not useful.
        }
        return document;
    }

    private Scriptable createXMLHTTPRequest() {
        final XMLHTTPRequest request = new XMLHTTPRequest();
        initObject(request);
        return request;
    }

    private Scriptable createXSLTemplate() {
        final XSLTemplate template = new XSLTemplate();
        initObject(template);
        return template;
    }

    private void initObject(final MSXMLScriptable scriptable) {
        try {
            scriptable.setPrototype(environment_.getPrototype(scriptable.getClass()));
            scriptable.setEnvironment(environment_);
        }
        catch (final Exception e) {
            LOG.error("Exception while initializing JavaScript for the page", e);
            throw new ScriptException(null, e); // BUG: null is not useful.
        }
    }
}
