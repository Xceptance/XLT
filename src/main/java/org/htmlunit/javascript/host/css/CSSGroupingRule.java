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

import static org.htmlunit.BrowserVersionFeatures.JS_GROUPINGRULE_INSERTRULE_INDEX_OPTIONAL;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;
import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import java.util.ArrayList;
import java.util.List;

import org.htmlunit.cssparser.dom.AbstractCSSRuleImpl;
import org.htmlunit.cssparser.dom.CSSCharsetRuleImpl;
import org.htmlunit.cssparser.dom.CSSMediaRuleImpl;
import org.htmlunit.cssparser.dom.CSSRuleListImpl;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.w3c.dom.DOMException;

/**
 * A JavaScript object for {@code CSSGroupingRule}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Frank Danek
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSGroupingRule">MDN doc</a>
 */
@JsxClass({CHROME, EDGE, FF, FF_ESR})
@JsxClass(isJSObject = false, value = IE)
public class CSSGroupingRule extends CSSRule {

    /** The collection of rules defined in this rule. */
    private CSSRuleList cssRules_;
    private List<Integer> cssRulesIndexFix_;

    /**
     * Creates a new instance.
     */
    public CSSGroupingRule() {
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
    protected CSSGroupingRule(final CSSStyleSheet stylesheet, final CSSMediaRuleImpl rule) {
        super(stylesheet, rule);
    }

    /**
     * Returns the collection of rules defined in this rule.
     * @return the collection of rules defined in this rule
     */
    @JsxGetter
    public CSSRuleList getCssRules() {
        initCssRules();
        return cssRules_;
    }

    /**
     * Inserts a new rule.
     * @param rule the CSS rule
     * @param position the position at which to insert the rule
     * @return the position of the inserted rule
     */
    @JsxFunction
    public int insertRule(final String rule, final Object position) {
        final int positionInt;
        if (position == null) {
            positionInt = 0;
        }
        else if (JavaScriptEngine.isUndefined(position)) {
            if (getBrowserVersion().hasFeature(JS_GROUPINGRULE_INSERTRULE_INDEX_OPTIONAL)) {
                positionInt = 0;
            }
            else {
                throw JavaScriptEngine.typeError("Failed to execute 'insertRule' on 'CSSGroupingRule':"
                        + " 2 arguments required, but only 1 present.");
            }
        }
        else {
            positionInt = JavaScriptEngine.toInt32(position);
        }

        try {
            initCssRules();
            getGroupingRule().insertRule(rule, fixIndex(positionInt));
            refreshCssRules();
            return positionInt;
        }
        catch (final DOMException e) {
            // in case of error try with an empty rule
            final int pos = rule.indexOf('{');
            if (pos > -1) {
                final String newRule = rule.substring(0, pos) + "{}";
                try {
                    getGroupingRule().insertRule(newRule, fixIndex(positionInt));
                    refreshCssRules();
                    return positionInt;
                }
                catch (final DOMException ex) {
                    throw JavaScriptEngine.throwAsScriptRuntimeEx(ex);
                }
            }
            throw JavaScriptEngine.throwAsScriptRuntimeEx(e);
        }
    }

    /**
     * Deletes an existing rule.
     * @param position the position of the rule to be deleted
     */
    @JsxFunction
    public void deleteRule(final int position) {
        try {
            initCssRules();
            getGroupingRule().deleteRule(fixIndex(position));
            refreshCssRules();
        }
        catch (final DOMException e) {
            throw JavaScriptEngine.throwAsScriptRuntimeEx(e);
        }
    }

    private void initCssRules() {
        if (cssRules_ == null) {
            cssRules_ = new CSSRuleList(this);
            cssRulesIndexFix_ = new ArrayList<>();
            refreshCssRules();
        }
    }

    private int fixIndex(int index) {
        for (final int fix : cssRulesIndexFix_) {
            if (fix > index) {
                return index;
            }
            index++;
        }
        return index;
    }

    private void refreshCssRules() {
        if (cssRules_ == null) {
            return;
        }

        cssRules_.clearRules();
        cssRulesIndexFix_.clear();

        final CSSRuleListImpl ruleList = getGroupingRule().getCssRules();
        final List<AbstractCSSRuleImpl> rules = ruleList.getRules();
        int pos = 0;
        for (final AbstractCSSRuleImpl rule : rules) {
            if (rule instanceof CSSCharsetRuleImpl) {
                cssRulesIndexFix_.add(pos);
                continue;
            }

            final CSSRule cssRule = CSSRule.create(getParentStyleSheet(), rule);
            if (null == cssRule) {
                cssRulesIndexFix_.add(pos);
            }
            else {
                cssRules_.addRule(cssRule);
            }
            pos++;
        }
    }

    /**
     * Returns the wrapped rule, as a media rule.
     * @return the wrapped rule, as a media rule
     */
    private CSSMediaRuleImpl getGroupingRule() {
        return (CSSMediaRuleImpl) getRule();
    }
}
