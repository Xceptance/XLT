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
package com.xceptance.xlt.engine;

import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.htmlunit.corejs.javascript.Script;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.css.dom.CSSStyleSheetImpl;
import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.common.collection.ConcurrentLRUCache;

/**
 * The {@link XltCache} class is a specialization of HtmlUnit's {@link Cache} class for compiled JavaScript and CSS
 * artifacts. In contrast to {@link Cache}, JavaScript pieces are always cached based on their source code (instead of
 * their URL) since this approach also covers in-line scripts.
 * <p>
 * Note that some HtmlUnit classes had to be changed to allow for caching based on JavaScript sources.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class XltCache extends Cache
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8636191853037193204L;

    /**
     * The log facility of this class.
     */
    private static final Log LOG = LogFactory.getLog(XltCache.class);

    /**
     * The CSS cache, a mapping from request URLs or source code strings to compiled style sheets.
     */
    private final ConcurrentLRUCache<String, CSSStyleSheetImpl> cssCache;

    /**
     * The number of CSS cache hits.
     */
    private final AtomicInteger cssHits = new AtomicInteger();

    /**
     * The number of CSS cache reads.
     */
    private final AtomicInteger cssReads = new AtomicInteger();

    /**
     * The JS cache, a mapping from request URLs or source code strings to compiled scripts.
     */
    private final ConcurrentLRUCache<String, Script> jsCache;

    /**
     * The number of JS cache hits.
     */
    private final AtomicInteger jsHits = new AtomicInteger();

    /**
     * The number of JS cache reads.
     */
    private final AtomicInteger jsReads = new AtomicInteger();

    /**
     * Creates a new combined cache using the given sizes.
     * 
     * @param jsCacheSize
     *            size of the JavaScript cache
     * @param cssCacheSize
     *            size of the CSS cache
     */
    public XltCache(final int jsCacheSize, final int cssCacheSize)
    {
        // use synchronized maps since the cache may be used concurrently
        cssCache = new ConcurrentLRUCache<>(cssCacheSize);
        jsCache = new ConcurrentLRUCache<>(jsCacheSize);

        // reduce the super class's memory demands, we do everything on our own
        super.setMaxSize(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cache(final String css, final CSSStyleSheetImpl styleSheet)
    {
        cssCache.put(css, styleSheet);
    }

    /**
     * Caches the compiled script with the script source as the key.
     * 
     * @param scriptSource
     *            the script source
     * @param script
     *            the compiled script
     */
    public void cache(final String scriptSource, final Script script)
    {
        jsCache.put(scriptSource, script);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cacheIfPossible(final WebRequest request, final WebResponse response, final Object object)
    {
        boolean storedInCache = false;
        if (isCacheable(request, response))
        {
            final String requestUrl = request.getUrl().toString();
            if (object instanceof CSSStyleSheetImpl)
            {
                cssCache.put(requestUrl, (CSSStyleSheetImpl) object);
                storedInCache = true;
            }
            else if (object instanceof Script)
            {
                jsCache.put(requestUrl, (Script) object);
                storedInCache = true;
            }
        }
        // we did our best -> let our caching web connection take over now
        return storedInCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        // our cache will be shared so never clear it
    }

    /**
     * Returns the compiled script for the passed script source.
     * 
     * @param scriptSource
     *            the script source
     * @return the compiled script, or <code>null</code> if none was in the cache
     */
    public Script getCachedScript(final String scriptSource)
    {
        final Script script = jsCache.get(scriptSource);
        onJSCacheAccess(script != null);

        return script;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getCachedObject(final WebRequest request)
    {
        final String requestUrl = request.getUrl().toString();
        final Object cssStyleSheet = cssCache.get(requestUrl);
        final Object script = jsCache.get(requestUrl);

        // found a match in both caches
        // -> since we don't know which artifact is meant, remove both from the caches
        // and handle this case as cache miss
        if (cssStyleSheet != null && script != null)
        {
            cssCache.remove(requestUrl);
            jsCache.remove(requestUrl);
        }
        else
        {
            if (cssStyleSheet != null)
            {
                onCSSCacheAccess(true);
                return cssStyleSheet;
            }
            if (script != null)
            {
                onJSCacheAccess(true);
                return script;
            }
        }

        onCSSCacheAccess(false);
        onJSCacheAccess(false);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CSSStyleSheetImpl getCachedStyleSheet(final String css)
    {
        final CSSStyleSheetImpl styleSheet = cssCache.get(css);
        onCSSCacheAccess(styleSheet != null);

        return styleSheet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxSize()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxSize(final int maxSize)
    {
        throw new UnsupportedOperationException();
    }

    protected void onCSSCacheAccess(final boolean cacheHit)
    {
        final int reads = cssReads.incrementAndGet();
        final int hits = cacheHit ? cssHits.incrementAndGet() : cssHits.intValue();

        // print cache statistic every 1000 reads
        if (reads % 1000 == 0)
        {
            final int size = cssCache.size();
            final double ratio = (double) hits / reads;

            if (LOG.isInfoEnabled())
            {
                LOG.info(String.format("CSS cache statistics: size = %d / hits = %d / reads = %d / ratio = %.2f", size, hits, reads,
                                       ratio));
            }
        }
    }

    protected void onJSCacheAccess(final boolean cacheHit)
    {
        final int reads = jsReads.incrementAndGet();
        final int hits = cacheHit ? jsHits.incrementAndGet() : jsHits.intValue();

        // print cache statistic every 1000 reads
        if (reads % 1000 == 0)
        {
            final int size = jsCache.size();
            final double ratio = (double) hits / reads;

            if (LOG.isInfoEnabled())
            {
                LOG.info(String.format("JS cache statistics: size = %d / hits = %d / reads = %d / ratio = %.2f", size, hits, reads, ratio));
            }
        }
    }
}
