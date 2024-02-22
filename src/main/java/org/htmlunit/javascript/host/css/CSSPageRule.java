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
package org.htmlunit.javascript.host.css;

import static org.htmlunit.BrowserVersionFeatures.CSS_CSSTEXT_FF_STYLE;
import static org.htmlunit.BrowserVersionFeatures.CSS_CSSTEXT_IE_STYLE;
import static org.htmlunit.BrowserVersionFeatures.JS_PAGERULE_SELECTORTEXT_EMPTY;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.BrowserVersion;
import org.htmlunit.css.WrappedCssStyleDeclaration;
import org.htmlunit.cssparser.dom.CSSPageRuleImpl;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;
import org.w3c.dom.DOMException;

/**
 * A JavaScript object for {@code CSSPageRule}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Frank Danek
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSPageRule">MDN doc</a>
 */
@JsxClass
public class CSSPageRule extends CSSRule {

    /**
     * Creates a new instance.
     */
    public CSSPageRule() {
    }

    /**
     * Creates an instance.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    @Override
    public void jsConstructor() {
        super.jsConstructor();
    }

    /**
     * Creates a new instance.
     * @param stylesheet the Stylesheet of this rule.
     * @param rule the wrapped rule
     */
    protected CSSPageRule(final CSSStyleSheet stylesheet, final CSSPageRuleImpl rule) {
        super(stylesheet, rule);
    }

    /**
     * Returns the textual representation of the selector for the rule set.
     * @return the textual representation of the selector for the rule set
     */
    @JsxGetter
    public String getSelectorText() {
        if (getBrowserVersion().hasFeature(JS_PAGERULE_SELECTORTEXT_EMPTY)) {
            return "";
        }

        final String selectorText = getPageRule().getSelectorText();
        if (selectorText != null) {
            return selectorText.toLowerCase(Locale.ROOT);
        }
        return null;
    }

    /**
     * Sets the textual representation of the selector for the rule set.
     * @param selectorText the textual representation of the selector for the rule set
     */
    @JsxSetter({CHROME, EDGE, FF, FF_ESR})
    public void setSelectorText(final String selectorText) {
        try {
            getPageRule().setSelectorText(selectorText);
        }
        catch (final DOMException e) {
            // ignore
        }
    }

    /**
     * Returns the declaration-block of this rule set.
     * @return the declaration-block of this rule set
     */
    @JsxGetter
    public CSSStyleDeclaration getStyle() {
        final WrappedCssStyleDeclaration styleDeclaration
                = new WrappedCssStyleDeclaration(getPageRule().getStyle(), getBrowserVersion());
        return new CSSStyleDeclaration(getParentStyleSheet(), styleDeclaration);
    }

    /**
     * Returns the wrapped rule, as a page rule.
     * @return the wrapped rule, as a page rule
     */
    private CSSPageRuleImpl getPageRule() {
        return (CSSPageRuleImpl) getRule();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCssText() {
        String cssText = super.getCssText();
        final BrowserVersion browserVersion = getBrowserVersion();
        if (browserVersion.hasFeature(CSS_CSSTEXT_IE_STYLE)) {
            cssText = StringUtils.replace(cssText, " { }", "  {\n\t\n}");
            cssText = StringUtils.replace(cssText, " { ", "  {\n\t");
            cssText = StringUtils.replace(cssText, "; }", ";\n}");
        }
        else if (browserVersion.hasFeature(CSS_CSSTEXT_FF_STYLE)) {
            cssText = StringUtils.replace(cssText, "@page {", "@page  {");
        }

        return cssText;
    }
}
