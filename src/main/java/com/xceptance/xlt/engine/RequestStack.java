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

import java.net.URL;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.engine.resultbrowser.Request;

/**
 * Utility class that is mainly used to number a sequence of {@link Request} objects in a hierarchical way.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class RequestStack implements Cloneable
{
    /**
     * Current stack object as thread-local variable.
     */
    private static ThreadLocal<RequestStack> current = new ThreadLocal<RequestStack>();

    /**
     * Returns the current stack object.
     * 
     * @return the current stack object
     */
    public static RequestStack getCurrent()
    {
        // get the current stack object
        RequestStack requestStack = current.get();

        // no current stack object -> create one
        if (requestStack == null)
        {
            requestStack = new RequestStack();
            current.set(requestStack);
        }

        // return stack object
        return requestStack;
    }

    /**
     * Sets the current stack object.
     * 
     * @param requestStack
     *            the stack object to set
     */
    public static void setCurrent(final RequestStack requestStack)
    {
        current.set(requestStack);
    }

    /**
     * The request counter for the current nesting level.
     */
    private int counter;

    /**
     * Stack of integers used for hierarchical numbering.
     */
    private Stack<Integer> stack = new Stack<Integer>();

    /**
     * Name of timer.
     */
    private String timerName;

    /**
     * Creates and returns a clone of this stack object.
     * 
     * @return a clone of this stack object
     */
    @Override
    @SuppressWarnings("unchecked")
    public RequestStack clone()
    {
        try
        {
            // use Object.clone() to get a flat copy
            final RequestStack clone = (RequestStack) super.clone();

            // get a flat copy of the numbering stack of this stack object and
            // set it at the cloned stack object
            clone.stack = (Stack<Integer>) stack.clone();

            // set the counter and update it locally
            clone.counter = counter++;

            // return cloned stack object
            return clone;
        }
        catch (final CloneNotSupportedException ex)
        {
            throw new RuntimeException("Failed to clone?!?", ex);
        }
    }

    /**
     * Returns the hierarchical request name for the given URL.
     * 
     * @param url
     *            the URL to use
     * @return hierarchical request name
     */
    public String getHierarchicalRequestName(final URL url)
    {
        final StringBuilder buf = new StringBuilder(getHierarchicalTimerName());

        final String lastPathElement = getLastPathElement(url);
        if (!"".equals(lastPathElement))
        {
            buf.append("-").append(lastPathElement);
        }

        return buf.toString();
    }

    /**
     * Gets the hierarchical request name part for this timer name.
     * 
     * @return hierarchical request name part
     */
    public String getHierarchicalTimerName()
    {
        final StringBuilder buf = new StringBuilder(timerName);
        // index of last stack element
        final int maxIdx = stack.size();
        // process the elements of the numbering stack
        for (int i = 0; i < maxIdx; i++)
        {
            // get the ID of the sub-request
            final int subRequestId = stack.get(i);
            // if not the last sub-request or if ID of sub-request is greater
            // than one append the decremented ID of the sub-request as value of
            // a new numbering level
            if (i < (maxIdx - 1) || subRequestId > 0)
            {
                buf.append(".").append(subRequestId);
            }
        }

        return buf.toString();
    }

    /**
     * Same as {@link #getHierarchicalTimerName()} but instead the ID of the sub-request this method uses the CRC32
     * checksum of the given URL as numbering value.
     * 
     * @param url
     *            the URL to use for computing the numbering value
     * @return hierarchical request name part of this timer name for the given URL
     */
    public String getHierarchicalTimerName(final URL url)
    {
        final StringBuilder buf = new StringBuilder(timerName);
        // index of last stack element
        final int maxIdx = stack.size();
        // process sub-request by sub-request
        for (int i = 0; i < maxIdx; i++)
        {
            // get ID of ith sub-request
            final int subRequestId = stack.get(i);

            // if sub-request is not the last one or if the sub-request's ID is
            // greater than one append the CRC32 checksum of the given URL as
            // value of new numbering level
            if (i < (maxIdx - 1) || subRequestId > 0)
            {
                buf.append(".").append(com.xceptance.common.lang.StringUtils.crc32(url.toString()));
            }
        }

        return buf.toString();
    }

    /**
     * Gets the last part of the non-query part of the given URL.
     * 
     * @param url
     *            the URL to use
     * @return last part of the URL's non-query part
     */
    private String getLastPathElement(final URL url)
    {
        // get the path only, i.e. no host, no query string, no reference
        String path = url.getPath();

        // remove any session ID information (";sid=3278327878")
        path = StringUtils.substringBefore(path, ";");

        // return the last path element
        return StringUtils.substringAfterLast(path, "/");
    }

    /**
     * Returns the top element of the numbering stack.
     */
    public void popRequest()
    {
        counter = stack.pop();
    }

    /**
     * Pushes a new sub request onto the stack.
     */
    public void pushRequest()
    {
        stack.push(++counter);
    }

    /**
     * Returns the top element of the numbering stack.
     */
    public void popPage()
    {
        counter = stack.pop();
    }

    /**
     * Pushes a new page request onto the stack.
     */
    public void pushPage()
    {
        stack.push(++counter);
        counter = -1;
    }

    /**
     * Sets the new timer name.
     * 
     * @param timerName
     *            the timer name to set
     */
    public void setTimerName(final String timerName)
    {
        this.timerName = timerName;
    }

    /**
     * Returns the size of the numbering stack.
     * 
     * @return size of numbering stack
     */
    public int size()
    {
        return stack.size();
    }

    /**
     * 
     */
    public void reset()
    {
        counter = 0;
    }
}
