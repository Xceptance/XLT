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
package com.xceptance.xlt.api.engine;

/**
 * <p>
 * The {@link PageLoadTimingData} ...
 * </p>
 * <p>
 * The {@link CustomData} should be used only if the intended purpose does not match the semantics of the other data
 * record classes ({@link RequestData}, {@link ActionData}, and {@link TransactionData}). For example, if one wants to
 * measure a certain functionality during client-side processing, a custom timer may suit best.
 * </p>
 * <p style="color:green">
 * Note that {@link PageLoadTimingData} objects have a "P" as their type code.
 * </p>
 * 
 * @see ActionData
 * @see RequestData
 * @see TransactionData
 */
public class PageLoadTimingData extends TimerData
{
    /**
     * The type code.
     */
    private static final char TYPE_CODE = 'P';

    /**
     * Creates a new PageLoadData object.
     */
    public PageLoadTimingData()
    {
        super(TYPE_CODE);
    }

    /**
     * Creates a new PageLoadTimingData object and gives it the specified name. Furthermore, the start time attribute is
     * set to the current time.
     * 
     * @param name
     *            the data name
     */
    public PageLoadTimingData(final String name)
    {
        super(name, TYPE_CODE);
    }
}
