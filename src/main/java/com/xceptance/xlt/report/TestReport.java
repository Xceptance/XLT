/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Represents the over-all test report. So far, this includes:
 * <ul>
 * <li>the configuration for the test</li>
 * <li>the timer statistics per test case</li>
 * <li>the over-all timer statistics</li>
 * </ul>
 */
@XStreamAlias("testreport")
public class TestReport
{
    @XStreamImplicit
    private final List<Object> reportFragments = new ArrayList<Object>();

    public void addReportFragment(final Object fragment)
    {
        reportFragments.add(fragment);
    }

    public List<Object> getReportFragments()
    {
        return reportFragments;
    }
}
