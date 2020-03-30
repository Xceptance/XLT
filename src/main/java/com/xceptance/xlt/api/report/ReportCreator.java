/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.report;

/**
 * The {@link ReportCreator} defines the interface that report providers must implement to take part in report
 * generation.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public interface ReportCreator
{
    /**
     * Creates a report fragment to be added to the test report. The fragment is generated from the statistics generated
     * during processing the data records. The statistics are encapsulated by some object which forms the record
     * fragment.
     * 
     * @return the report fragment
     */
    public Object createReportFragment();
}
