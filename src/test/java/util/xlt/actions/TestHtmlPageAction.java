/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package util.xlt.actions;

import java.net.URL;

import com.xceptance.xlt.api.actions.AbstractAction;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;

/**
 * A dummy specialization of {@link AbstractHtmlPageAction} for unit tests. {@link #execute()} will call
 * {@link #loadPage(URL)} with a URL that can be specified in the constructor or defaults to {@link #DEFAULT_URL}
 * 
 * @author daltin
 */
public class TestHtmlPageAction extends AbstractHtmlPageAction
{
    private static final String _DEFAULT_TIMERNAME = "TestHtmlPageAction";

    private static final String _DEFAULT_URL = "http://localhost:8081/";

    /**
     * The default URL that will be used if no URL is specified in the constructor ({@value #_DEFAULT_URL})
     */
    public static final URL DEFAULT_URL = createDefaultUrl();

    private static URL createDefaultUrl()
    {
        try
        {
            return new URL(_DEFAULT_URL);
        }
        catch (Throwable t)
        {
            throw new RuntimeException(t);
        }
    }

    private final URL url;

    /**
     * Create an action using the specified {@linkplain AbstractAction#getTimerName() timer name}, which will
     * {@linkplain #loadPage(URL) load} the specified URL
     * 
     * @param timerName
     * @param url
     */
    public TestHtmlPageAction(String timerName, URL url)
    {
        super(timerName);
        this.url = url;
    }

    /**
     * Create an action with {@linkplain AbstractAction#getTimerName() timer name} {@value #_DEFAULT_TIMERNAME}, which
     * will {@linkplain #loadPage(URL) load} the specified URL
     * 
     * @param url
     */
    public TestHtmlPageAction(URL url)
    {
        this(_DEFAULT_TIMERNAME, url);
    }

    /**
     * Create an action using the specified {@linkplain AbstractAction#getTimerName() timer name}, which will
     * {@linkplain #loadPage(URL) load} {@link #DEFAULT_URL}
     * 
     * @param timerName
     * @param url
     */
    public TestHtmlPageAction(String timerName)
    {
        this(timerName, DEFAULT_URL);
    }

    /**
     * Create an action with {@linkplain AbstractAction#getTimerName() timer name} {@value #_DEFAULT_TIMERNAME}, which
     * will {@linkplain #loadPage(URL) load} {@link #DEFAULT_URL}
     * 
     * @param url
     */
    public TestHtmlPageAction()
    {
        this(DEFAULT_URL);
    }

    @Override
    public void preValidate() throws Exception
    {
    }

    @Override
    protected void execute() throws Exception
    {
        loadPage(url);
    }

    @Override
    protected void postValidate() throws Exception
    {
    }
}
