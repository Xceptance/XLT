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
package org.htmlunit.javascript.host.dom;

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;

import org.htmlunit.corejs.javascript.Function;
import org.htmlunit.corejs.javascript.NativeArray;
import org.htmlunit.corejs.javascript.NativeObject;
import org.htmlunit.corejs.javascript.ScriptRuntime;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.corejs.javascript.TopLevel;
import org.htmlunit.html.CharacterDataChangeEvent;
import org.htmlunit.html.CharacterDataChangeListener;
import org.htmlunit.html.HtmlAttributeChangeEvent;
import org.htmlunit.html.HtmlAttributeChangeListener;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.PostponedAction;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxConstructorAlias;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.host.Window;

/**
 * A JavaScript object for {@code MutationObserver}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Atsushi Nakagawa
 */
@JsxClass
public class MutationObserver extends HtmlUnitScriptable implements HtmlAttributeChangeListener,
        CharacterDataChangeListener {

    private Function function_;
    private Node node_;
    private boolean attaributes_;
    private boolean attributeOldValue_;
    private NativeArray attributeFilter_;
    private boolean characterData_;
    private boolean characterDataOldValue_;
    private boolean subtree_;

    /**
     * Creates an instance.
     */
    public MutationObserver() {
    }

    /**
     * Creates an instance.
     * @param function the function to observe
     */
    @JsxConstructor
    @JsxConstructorAlias(value = {CHROME, EDGE}, alias = "WebKitMutationObserver")
    public void jsConstructor(final Function function) {
        function_ = function;
    }

    /**
     * Registers the {@link MutationObserver} instance to receive notifications of DOM mutations on the specified node.
     * @param node the node
     * @param options the options
     */
    @JsxFunction
    public void observe(final Node node, final NativeObject options) {
        if (node == null) {
            throw JavaScriptEngine.throwAsScriptRuntimeEx(new IllegalArgumentException("Node is undefined"));
        }
        if (options == null) {
            throw JavaScriptEngine.throwAsScriptRuntimeEx(new IllegalArgumentException("Options is undefined"));
        }

        node_ = node;
        attaributes_ = Boolean.TRUE.equals(options.get("attributes"));
        attributeOldValue_ = Boolean.TRUE.equals(options.get("attributeOldValue"));
        characterData_ = Boolean.TRUE.equals(options.get("characterData"));
        characterDataOldValue_ = Boolean.TRUE.equals(options.get("characterDataOldValue"));
        subtree_ = Boolean.TRUE.equals(options.get("subtree"));
        attributeFilter_ = (NativeArray) options.get("attributeFilter");

        final boolean childList = Boolean.TRUE.equals(options.get("childList"));

        if (!attaributes_ && !childList && !characterData_) {
            throw JavaScriptEngine.throwAsScriptRuntimeEx(new IllegalArgumentException(
                        "One of childList, attributes, od characterData must be set"));
        }

        if (attaributes_ && node_.getDomNodeOrDie() instanceof HtmlElement) {
            ((HtmlElement) node_.getDomNodeOrDie()).addHtmlAttributeChangeListener(this);
        }
        if (characterData_) {
            node.getDomNodeOrDie().addCharacterDataChangeListener(this);
        }
    }

    /**
     * Stops the MutationObserver instance from receiving notifications of DOM mutations.
     */
    @JsxFunction
    public void disconnect() {
        if (attaributes_ && node_.getDomNodeOrDie() instanceof HtmlElement) {
            ((HtmlElement) node_.getDomNodeOrDie()).removeHtmlAttributeChangeListener(this);
        }
        if (characterData_) {
            node_.getDomNodeOrDie().removeCharacterDataChangeListener(this);
        }
    }

    /**
     * Empties the MutationObserver instance's record queue and returns what was in there.
     * @return an {@link NativeArray} of {@link MutationRecord}s
     */
    @JsxFunction
    public NativeArray takeRecords() {
        return new NativeArray(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characterDataChanged(final CharacterDataChangeEvent event) {
        final HtmlUnitScriptable target = event.getCharacterData().getScriptableObject();
        if (subtree_ || target == node_) {
            final MutationRecord mutationRecord = new MutationRecord();
            final Scriptable scope = getParentScope();
            mutationRecord.setParentScope(scope);
            mutationRecord.setPrototype(getPrototype(mutationRecord.getClass()));

            mutationRecord.setType("characterData");
            mutationRecord.setTarget(target);
            if (characterDataOldValue_) {
                mutationRecord.setOldValue(event.getOldValue());
            }

            final Window window = getWindow();
            final HtmlPage owningPage = (HtmlPage) window.getDocument().getPage();
            final JavaScriptEngine jsEngine =
                    (JavaScriptEngine) window.getWebWindow().getWebClient().getJavaScriptEngine();
            jsEngine.addPostponedAction(new PostponedAction(owningPage, "MutationObserver.characterDataChanged") {
                @Override
                public void execute() {
                    final NativeArray array = new NativeArray(new Object[] {mutationRecord});
                    ScriptRuntime.setBuiltinProtoAndParent(array, scope, TopLevel.Builtins.Array);
                    jsEngine.callFunction(owningPage, function_, scope, MutationObserver.this, new Object[] {array});
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attributeAdded(final HtmlAttributeChangeEvent event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attributeRemoved(final HtmlAttributeChangeEvent event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attributeReplaced(final HtmlAttributeChangeEvent event) {
        final HtmlElement target = event.getHtmlElement();
        if (subtree_ || target == node_.getDomNodeOrDie()) {
            final String attributeName = event.getName();
            if (attributeFilter_ == null || attributeFilter_.contains(attributeName)) {
                final MutationRecord mutationRecord = new MutationRecord();
                final Scriptable scope = getParentScope();
                mutationRecord.setParentScope(scope);
                mutationRecord.setPrototype(getPrototype(mutationRecord.getClass()));

                mutationRecord.setAttributeName(attributeName);
                mutationRecord.setType("attributes");
                mutationRecord.setTarget(target.getScriptableObject());
                if (attributeOldValue_) {
                    mutationRecord.setOldValue(event.getValue());
                }

                final Window window = getWindow();
                final HtmlPage owningPage = (HtmlPage) window.getDocument().getPage();
                final JavaScriptEngine jsEngine =
                        (JavaScriptEngine) window.getWebWindow().getWebClient().getJavaScriptEngine();
                jsEngine.addPostponedAction(new PostponedAction(owningPage, "MutationObserver.attributeReplaced") {
                    @Override
                    public void execute() {
                        final NativeArray array = new NativeArray(new Object[] {mutationRecord});
                        ScriptRuntime.setBuiltinProtoAndParent(array, scope, TopLevel.Builtins.Array);
                        jsEngine.callFunction(owningPage, function_, scope,
                                                MutationObserver.this, new Object[] {array});
                    }
                });
            }
        }
    }
}
