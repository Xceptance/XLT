/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the collected custom log files written during the test run for one specific scope.
 */
@XStreamAlias("customLog")
public class CustomLogReport
{
    /**
     * The scope name of the given logs
     */
    public String scope;

    /**
     * The file size of the collected data
     */
    public long size;
    
    /**
     * The link to the collected data
     */
    public String path;
}
