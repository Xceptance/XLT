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
package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the total number of requests to a certain ip address.
 */
@XStreamAlias("ip")
public class IpReport
{
    /**
     * The ip address.
     */
    public String ip;
    
    /**
     * The hostname associated with this ip.
     */
    public String host;

    /**
     * The total number of requests to that ip.
     */
    public int count;
}
