/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scripting.neg.mouseEventInvisible;

import org.junit.After;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * Mouse events must fail if target is invisible.
 */
public class mouseEventVisibilityHidden extends AbstractWebDriverScriptTestCase
{
    public static final String TARGET = "id=invisible_anchor_visibility_hidden";

    public mouseEventVisibilityHidden()
    {
        super(new XltDriver(true), null);
    }

    @Test(expected = XltException.class)
    public void mouseOver() throws Exception
    {
        PageOpener.examplePage(this);
        mouseOver(TARGET);
    }

    @Test(expected = XltException.class)
    public void mouseDown() throws Exception
    {
        PageOpener.examplePage(this);
        mouseDown(TARGET);
    }

    @Test(expected = XltException.class)
    public void mouseDownAt() throws Exception
    {
        PageOpener.examplePage(this);
        mouseDownAt(TARGET, "1,1");
    }

    @Test(expected = XltException.class)
    public void mouseMove() throws Exception
    {
        PageOpener.examplePage(this);
        mouseMove(TARGET);
    }

    @Test(expected = XltException.class)
    public void mouseMoveAt() throws Exception
    {
        PageOpener.examplePage(this);
        mouseMoveAt(TARGET, "1,1");
    }

    @Test(expected = XltException.class)
    public void mouseUp() throws Exception
    {
        PageOpener.examplePage(this);
        mouseUp(TARGET);
    }

    @Test(expected = XltException.class)
    public void mouseUpAt() throws Exception
    {
        PageOpener.examplePage(this);
        mouseUpAt(TARGET, "1,1");
    }

    @Test(expected = XltException.class)
    public void mouseOut() throws Exception
    {
        PageOpener.examplePage(this);
        mouseOut(TARGET);
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}
