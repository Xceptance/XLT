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

import org.htmlunit.Page;
import org.htmlunit.StringWebResponse;
import org.htmlunit.WebResponse;
import org.htmlunit.WebWindow;
import org.htmlunit.WebWindowImpl;

/**
 * The web window for a frame or iframe.
 *
 * @author Brad Clarke
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public class FrameWindow extends WebWindowImpl {

    /** The different deny states. */
    public enum PageDenied { NONE, BY_X_FRAME_OPTIONS, BY_CONTENT_SECURIRY_POLICY }

    private final BaseFrameElement frame_;
    private PageDenied pageDenied_;

    /**
     * Creates an instance for a given frame.
     */
    FrameWindow(final BaseFrameElement frame) {
        super(frame.getPage().getWebClient());
        frame_ = frame;
        performRegistration();

        final WebWindowImpl parent = (WebWindowImpl) frame_.getPage().getEnclosingWindow();
        parent.addChildWindow(this);
    }

    /**
     * {@inheritDoc}
     * A FrameWindow shares it's name with it's containing frame.
     */
    @Override
    public String getName() {
        return frame_.getNameAttribute();
    }

    /**
     * {@inheritDoc}
     * A FrameWindow shares it's name with it's containing frame.
     */
    @Override
    public void setName(final String name) {
        frame_.setNameAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebWindow getParentWindow() {
        return frame_.getPage().getEnclosingWindow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebWindow getTopWindow() {
        return getParentWindow().getTopWindow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isJavaScriptInitializationNeeded(final Page page) {
        return getScriptableObject() == null
            || !(page.getWebResponse() instanceof StringWebResponse
                    && ((StringWebResponse) page.getWebResponse()).isFromJavascript());
        // TODO: find a better way to distinguish content written by document.open(),...
    }

    /**
     * Returns the HTML page in which the &lt;frame&gt; or &lt;iframe&gt; tag is contained
     * for this frame window.
     * This is a facility method for <code>(HtmlPage) (getParentWindow().getEnclosedPage())</code>.
     * @return the page in the parent window
     */
    public HtmlPage getEnclosingPage() {
        return (HtmlPage) frame_.getPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnclosedPage(final Page page) {
        setPageDenied(PageDenied.NONE);
        super.setEnclosedPage(page);

        // we have updated a frame window by javascript write();
        // so we have to disable future updates during initialization
        // see org.htmlunit.html.HtmlPage.loadFrames()
        final WebResponse webResponse = page.getWebResponse();
        if (webResponse instanceof StringWebResponse) {
            final StringWebResponse response = (StringWebResponse) webResponse;
            if (response.isFromJavascript()) {
                final BaseFrameElement frame = getFrameElement();
                frame.setContentLoaded();
            }
        }
    }

    /**
     * Gets the DOM node of the (i)frame containing this window.
     * @return the DOM node
     */
    public BaseFrameElement getFrameElement() {
        return frame_;
    }

    /**
     * Gives a basic representation for debugging purposes.
     * @return a basic representation
     */
    @Override
    public String toString() {
        return "FrameWindow[name=\"" + getName() + "\"]";
    }

    /**
     * Closes this frame window.
     */
    public void close() {
        final WebWindowImpl parent = (WebWindowImpl) getParentWindow();
        parent.removeChildWindow(this);
        getWebClient().deregisterWebWindow(this);
    }

    /**
     * Marks that the page content as denied.
     * @param pageDenied the new state
     */
    public void setPageDenied(final PageDenied pageDenied) {
        pageDenied_ = pageDenied;
    }

    /**
     * @return PageDenied if the page was denied
     */
    public PageDenied getPageDenied() {
        return pageDenied_;
    }
}
