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
package org.htmlunit.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that wraps static content.
 *
 * @author Ahmed Ashour
 */
public abstract class ServletContentWrapper extends HttpServlet {

    private final String content_;

    /**
     * Creates an instance.
     *
     * @param content the HTML content of this servlet
     */
    public ServletContentWrapper(final String content) {
        content_ = content;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType(MimeType.TEXT_HTML);
        response.getWriter().write(content_);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Helper.
     * @return the content
     */
    protected String getContent() {
        return content_;
    }

    /**
     * Helper.
     * @return the length of the content
     */
    protected int getContentLength() {
        return content_.length();
    }
}
