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

import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxGetter;

/**
 * A JavaScript object for MSXML's (ActiveX) XMLDOMParseError.<br>
 * Returns detailed information about the last parse error, including the error number, line number, character
 * position, and a text description.
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms757019.aspx">MSDN documentation</a>
 *
 * @author Ahmed Ashour
 * @author Frank Danek
 */
@JsxClass(IE)
public class XMLDOMParseError extends MSXMLScriptable {

    private int errorCode_;
    private int filepos_;
    private int line_;
    private int linepos_;
    private String reason_ = "";
    private String srcText_ = "";
    private String url_ = "";

    /**
     * Returns the error code of the last parse error.
     * @return the error code of the last parse error
     */
    @JsxGetter
    public int getErrorCode() {
        return errorCode_;
    }

    /**
     * Returns the absolute file position where the error occurred.
     * @return the absolute file position where the error occurred
     */
    @JsxGetter
    public int getFilepos() {
        return filepos_;
    }

    /**
     * Returns the line number that contains the error.
     * @return the line number that contains the error
     */
    @JsxGetter
    public int getLine() {
        return line_;
    }

    /**
     * Returns the character position within the line where the error occurred.
     * @return the character position within the line where the error occurred
     */
    @JsxGetter
    public int getLinepos() {
        return linepos_;
    }

    /**
     * Returns the reason for the error.
     * @return the reason for the error
     */
    @JsxGetter
    public String getReason() {
        return reason_;
    }

    /**
     * Returns the full text of the line containing the error.
     * @return the full text of the line containing the error
     */
    @JsxGetter
    public String getSrcText() {
        return srcText_;
    }

    /**
     * Returns the URL of the XML document containing the last error.
     * @return the URL of the XML document containing the last error
     */
    @JsxGetter
    public String getUrl() {
        return url_;
    }

    void setErrorCode(final int errorCode) {
        errorCode_ = errorCode;
    }

    void setFilepos(final int filepos) {
        filepos_ = filepos;
    }

    void setLine(final int line) {
        line_ = line;
    }

    void setLinepos(final int linepos) {
        linepos_ = linepos;
    }

    void setReason(final String reason) {
        reason_ = reason;
    }

    void setSrcText(final String srcText) {
        srcText_ = srcText;
    }

    void setUrl(final String url) {
        url_ = url;
    }
}
