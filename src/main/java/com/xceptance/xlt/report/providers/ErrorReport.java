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
package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the statistics for a certain exception in a test report.
 */
@XStreamAlias("error")
public class ErrorReport
{
    /**
     * The number how often a certain exception has occurred.
     */
    public int count;

    /**
     * The name of the test case.
     */
    public String testCaseName;

    /**
     * The name of the action that caused the test case to fail.
     */
    public String actionName;

    /**
     * The exception's message attribute.
     */
    public String message;

    /**
     * The exception's stack trace.
     */
    public String trace;

    /**
     * The unique chartID for the error details
     */
    public int detailChartID;

    /**
     * The list of directory hints (for example: "ac1/TAuthor/1/1216803080255") where to find additional information to
     * locate the error.
     */
    public List<String> directoryHints = new ArrayList<String>();
}
