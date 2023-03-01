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
package com.gargoylesoftware.htmlunit.javascript.host.html;

import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_TABLE_ROW_DELETE_CELL_REQUIRES_INDEX;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.IE;

import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxFunction;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxSetter;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Undefined;

/**
 * The JavaScript object {@code HTMLTableRowElement}.
 *
 * @author Marc Guillemot
 * @author Chris Erskine
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Frank Danek
 */
@JsxClass(domClass = HtmlTableRow.class)
public class HTMLTableRowElement extends HTMLTableComponent {

    /**
     * Creates an instance.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public HTMLTableRowElement() {
    }

    /**
     * Returns the index of the row within the parent table.
     * @return the index of the row within the parent table
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms534377.aspx">MSDN Documentation</a>
     */
    @JsxGetter
    public int getRowIndex() {
        final HtmlTableRow row = (HtmlTableRow) getDomNodeOrDie();
        final HtmlTable table = row.getEnclosingTable();
        if (table == null) { // a not attached document.createElement('TR')
            return -1;
        }
        return table.getRows().indexOf(row);
    }

    /**
     * Returns the index of the row within the enclosing thead, tbody or tfoot.
     * @return the index of the row within the enclosing thead, tbody or tfoot
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms534621.aspx">MSDN Documentation</a>
     * @see <a href="http://www.w3.org/TR/2000/WD-DOM-Level-1-20000929/level-one-html.html#ID-79105901">
     * DOM Level 1</a>
     */
    @JsxGetter
    public int getSectionRowIndex() {
        DomNode row = getDomNodeOrDie();
        final HtmlTable table = ((HtmlTableRow) row).getEnclosingTable();
        if (table == null) { // a not attached document.createElement('TR')
            return -1;
        }
        int index = -1;
        while (row != null) {
            if (row instanceof HtmlTableRow) {
                index++;
            }
            row = row.getPreviousSibling();
        }
        return index;
    }

    /**
     * Returns the cells in the row.
     * @return the cells in the row
     */
    @JsxGetter
    public Object getCells() {
        final HtmlTableRow row = (HtmlTableRow) getDomNodeOrDie();

        final HTMLCollection cells = new HTMLCollection(row, false);
        cells.setElementsSupplier(() -> new ArrayList<>(row.getCells()));
        return cells;
    }

    /**
     * Returns the value of the {@code bgColor} attribute.
     * @return the value of the {@code bgColor} attribute
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533505.aspx">MSDN Documentation</a>
     */
    @JsxGetter
    public String getBgColor() {
        return getDomNodeOrDie().getAttribute("bgColor");
    }

    /**
     * Sets the value of the {@code bgColor} attribute.
     * @param bgColor the value of the {@code bgColor} attribute
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533505.aspx">MSDN Documentation</a>
     */
    @JsxSetter
    public void setBgColor(final String bgColor) {
        setColorAttribute("bgColor", bgColor);
    }

    /**
     * Inserts a new cell at the specified index in the element's cells collection. If the index
     * is -1 or there is no index specified, then the cell is appended at the end of the
     * element's cells collection.
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms536455.aspx">MSDN Documentation</a>
     * @param index specifies where to insert the cell in the tr.
     *        The default value is -1, which appends the new cell to the end of the cells collection
     * @return the newly-created cell
     */
    @JsxFunction
    public Object insertCell(final Object index) {
        int position = -1;
        if (!Undefined.isUndefined(index)) {
            position = (int) Context.toNumber(index);
        }
        final HtmlTableRow htmlRow = (HtmlTableRow) getDomNodeOrDie();

        final boolean indexValid = position >= -1 && position <= htmlRow.getCells().size();
        if (indexValid) {
            final DomElement newCell = ((HtmlPage) htmlRow.getPage()).createElement("td");
            if (position == -1 || position == htmlRow.getCells().size()) {
                htmlRow.appendChild(newCell);
            }
            else {
                htmlRow.getCell(position).insertBefore(newCell);
            }
            return getScriptableFor(newCell);
        }
        throw Context.reportRuntimeError("Index or size is negative or greater than the allowed amount");
    }

    /**
     * Deletes the cell at the specified index in the element's cells collection. If the index
     * is -1 (or while simulating IE, when there is no index specified), then the last cell is deleted.
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms536406.aspx">MSDN Documentation</a>
     * @see <a href="http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109/html.html#ID-11738598">W3C DOM Level2</a>
     * @param index specifies the cell to delete.
     */
    @JsxFunction
    public void deleteCell(final Object index) {
        int position = -1;
        if (!Undefined.isUndefined(index)) {
            position = (int) Context.toNumber(index);
        }
        else if (getBrowserVersion().hasFeature(JS_TABLE_ROW_DELETE_CELL_REQUIRES_INDEX)) {
            throw Context.reportRuntimeError("No enough arguments");
        }

        final HtmlTableRow htmlRow = (HtmlTableRow) getDomNodeOrDie();

        if (position == -1) {
            position = htmlRow.getCells().size() - 1;
        }
        final boolean indexValid = position >= -1 && position <= htmlRow.getCells().size();
        if (!indexValid) {
            throw Context.reportRuntimeError("Index or size is negative or greater than the allowed amount");
        }

        htmlRow.getCell(position).remove();
    }

    /**
     * Overwritten to throw an exception.
     * @param value the new value for replacing this node
     */
    @Override
    public void setOuterHTML(final Object value) {
        throw Context.reportRuntimeError("outerHTML is read-only for tag 'tr'");
    }

    /**
     * Gets the {@code borderColor} attribute.
     * @return the attribute
     */
    @JsxGetter(IE)
    public String getBorderColor() {
        return getDomNodeOrDie().getAttribute("borderColor");
    }

    /**
     * Sets the {@code borderColor} attribute.
     * @param borderColor the new attribute
     */
    @JsxSetter(IE)
    public void setBorderColor(final String borderColor) {
        setColorAttribute("borderColor", borderColor);
    }

    /**
     * Gets the {@code borderColor} attribute.
     * @return the attribute
     */
    @JsxGetter(IE)
    public String getBorderColorDark() {
        return getDomNodeOrDie().getAttribute("borderColorDark");
    }

    /**
     * Sets the {@code borderColor} attribute.
     * @param borderColor the new attribute
     */
    @JsxSetter(IE)
    public void setBorderColorDark(final String borderColor) {
        setColorAttribute("borderColorDark", borderColor);
    }

    /**
     * Gets the {@code borderColor} attribute.
     * @return the attribute
     */
    @JsxGetter(IE)
    public String getBorderColorLight() {
        return getDomNodeOrDie().getAttribute("borderColorLight");
    }

    /**
     * Sets the {@code borderColor} attribute.
     * @param borderColor the new attribute
     */
    @JsxSetter(IE)
    public void setBorderColorLight(final String borderColor) {
        setColorAttribute("borderColorLight", borderColor);
    }
}
