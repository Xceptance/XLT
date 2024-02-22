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
package org.htmlunit.html;

import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_ADD;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_BACK_SPACE;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_DECIMAL;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_DELETE;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_DIVIDE;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_END;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_EQUALS;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_HOME;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_LEFT;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_MULTIPLY;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_NUMPAD0;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_NUMPAD9;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_RIGHT;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_SEMICOLON;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_SEPARATOR;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_SPACE;
import static org.htmlunit.javascript.host.event.KeyboardEvent.DOM_VK_SUBTRACT;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.htmlunit.ClipboardHandler;
import org.htmlunit.html.impl.SelectionDelegate;

/**
 * The processor for {@link HtmlElement#doType(char, boolean)}
 * and {@link HtmlElement#doType(int, boolean)}.
 *
 * @author Marc Guillemot
 * @author Ronald Brill
 * @author Ahmed Ashour
 */
class DoTypeProcessor implements Serializable {

    private static final Map<Integer, Character> SPECIAL_KEYS_MAP_ = new HashMap<>();

    /**
     * Either {@link HtmlElement} or {@link DomText}.
     */
    private final DomNode domNode_;

    static {
        SPECIAL_KEYS_MAP_.put(DOM_VK_ADD, '+');
        SPECIAL_KEYS_MAP_.put(DOM_VK_DECIMAL, '.');
        SPECIAL_KEYS_MAP_.put(DOM_VK_DIVIDE, '/');
        SPECIAL_KEYS_MAP_.put(DOM_VK_EQUALS, '=');
        SPECIAL_KEYS_MAP_.put(DOM_VK_MULTIPLY, '*');
        SPECIAL_KEYS_MAP_.put(DOM_VK_SEMICOLON, ';');
        SPECIAL_KEYS_MAP_.put(DOM_VK_SEPARATOR, ',');
        SPECIAL_KEYS_MAP_.put(DOM_VK_SPACE, ' ');
        SPECIAL_KEYS_MAP_.put(DOM_VK_SUBTRACT, '-');

        for (int i = DOM_VK_NUMPAD0; i <= DOM_VK_NUMPAD9; i++) {
            SPECIAL_KEYS_MAP_.put(i, (char) ('0' + (i - DOM_VK_NUMPAD0)));
        }
    }

    DoTypeProcessor(final DomNode domNode) {
        domNode_ = domNode;
    }

    void doType(final String currentValue, final SelectionDelegate selectionDelegate,
            final char c, final HtmlElement element, final boolean lastType) {

        int selectionStart = selectionDelegate.getSelectionStart();
        selectionStart = Math.max(0, Math.min(selectionStart, currentValue.length()));

        int selectionEnd = selectionDelegate.getSelectionEnd();
        selectionEnd = Math.max(selectionStart, Math.min(selectionEnd, currentValue.length()));

        final StringBuilder newValue = new StringBuilder(currentValue);
        if (c == '\b') {
            if (selectionStart > 0) {
                newValue.deleteCharAt(selectionStart - 1);
                selectionStart--;
                selectionEnd--;
            }
        }
        else if (acceptChar(c)) {
            final boolean ctrlKey = element.isCtrlPressed();
            if (ctrlKey && (c == 'C' || c == 'c')) {
                final ClipboardHandler clipboardHandler = element.getPage().getWebClient().getClipboardHandler();
                if (clipboardHandler != null) {
                    final String content = newValue.substring(selectionStart, selectionEnd);
                    clipboardHandler.setClipboardContent(content);
                }
            }
            else if (ctrlKey && (c == 'V' || c == 'v')) {
                final ClipboardHandler clipboardHandler = element.getPage().getWebClient().getClipboardHandler();
                if (clipboardHandler != null) {
                    final String content = clipboardHandler.getClipboardContent();
                    add(newValue, content, selectionStart, selectionEnd);
                    selectionStart += content.length();
                    selectionEnd = selectionStart;
                }
            }
            else if (ctrlKey && (c == 'X' || c == 'x')) {
                final ClipboardHandler clipboardHandler = element.getPage().getWebClient().getClipboardHandler();
                if (clipboardHandler != null) {
                    final String content = newValue.substring(selectionStart, selectionEnd);
                    clipboardHandler.setClipboardContent(content);
                    newValue.delete(selectionStart, selectionEnd);
                    selectionEnd = selectionStart;
                }
            }
            else if (ctrlKey && (c == 'A' || c == 'a')) {
                selectionStart = 0;
                selectionEnd = newValue.length();
            }
            else {
                add(newValue, c, selectionStart, selectionEnd);
                selectionStart++;
                selectionEnd = selectionStart;
            }
        }

        typeDone(newValue.toString(), lastType);

        selectionDelegate.setSelectionStart(selectionStart);
        selectionDelegate.setSelectionEnd(selectionEnd);
    }

    private static void add(final StringBuilder newValue, final char c, final int selectionStart,
            final int selectionEnd) {
        if (selectionStart == newValue.length()) {
            newValue.append(c);
        }
        else {
            newValue.replace(selectionStart, selectionEnd, Character.toString(c));
        }
    }

    private static void add(final StringBuilder newValue, final String string, final int selectionStart,
            final int selectionEnd) {
        if (selectionStart == newValue.length()) {
            newValue.append(string);
        }
        else {
            newValue.replace(selectionStart, selectionEnd, string);
        }
    }

    private void typeDone(final String newValue, final boolean notifyAttributeChangeListeners) {
        if (domNode_ instanceof DomText) {
            ((DomText) domNode_).setData(newValue);
        }
        else {
            ((HtmlElement) domNode_).typeDone(newValue, notifyAttributeChangeListeners);
        }
    }

    private boolean acceptChar(final char ch) {
        if (domNode_ instanceof DomText) {
            return ((DomText) domNode_).acceptChar(ch);
        }
        return ((HtmlElement) domNode_).acceptChar(ch);
    }

    void doType(final String currentValue, final SelectionDelegate selectionDelegate,
            final int keyCode, final HtmlElement element, final boolean lastType) {

        int selectionStart = selectionDelegate.getSelectionStart();
        selectionStart = Math.max(0, Math.min(selectionStart, currentValue.length()));

        int selectionEnd = selectionDelegate.getSelectionEnd();
        selectionEnd = Math.max(selectionStart, Math.min(selectionEnd, currentValue.length()));

        final Character ch = SPECIAL_KEYS_MAP_.get(keyCode);
        if (ch != null) {
            doType(currentValue, selectionDelegate, ch, element, lastType);
            return;
        }

        final StringBuilder newValue = new StringBuilder(currentValue);
        switch (keyCode) {
            case DOM_VK_BACK_SPACE:
                if (selectionStart > 0) {
                    newValue.deleteCharAt(selectionStart - 1);
                    selectionStart--;
                }
                break;

            case DOM_VK_LEFT:
                if (element.isCtrlPressed()) {
                    while (selectionStart > 0 && newValue.charAt(selectionStart - 1) != ' ') {
                        selectionStart--;
                    }
                }
                else if (selectionStart > 0) {
                    selectionStart--;
                }
                break;

            case DOM_VK_RIGHT:
                if (element.isCtrlPressed()) {
                    if (selectionStart < newValue.length()) {
                        selectionStart++;
                    }
                    while (selectionStart < newValue.length() && newValue.charAt(selectionStart - 1) != ' ') {
                        selectionStart++;
                    }
                }
                else if (element.isShiftPressed()) {
                    selectionEnd++;
                }
                else if (selectionStart > 0) {
                    selectionStart++;
                }
                break;

            case DOM_VK_HOME:
                selectionStart = 0;
                break;

            case DOM_VK_END:
                if (element.isShiftPressed()) {
                    selectionEnd = newValue.length();
                }
                else {
                    selectionStart = newValue.length();
                }
                break;

            case DOM_VK_DELETE:
                if (selectionEnd == selectionStart) {
                    selectionEnd++;
                }
                newValue.delete(selectionStart, selectionEnd);
                selectionEnd = selectionStart;
                break;

            default:
                return;
        }

        if (!element.isShiftPressed()) {
            selectionEnd = selectionStart;
        }

        typeDone(newValue.toString(), lastType);

        selectionDelegate.setSelectionStart(selectionStart);
        selectionDelegate.setSelectionEnd(selectionEnd);
    }
}
