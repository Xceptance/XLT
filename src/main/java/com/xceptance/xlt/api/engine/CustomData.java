/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
 * The {@link CustomData} should be used only if the intended purpose does not match the semantics of the other data
 * record classes ({@link RequestData}, {@link ActionData}, and {@link TransactionData}). For example, if one wants to
 * measure a certain functionality during client-side processing, a custom timer may suit best.
 * </p>
 * <p style="color:green">
 * Note that {@link CustomData} objects have a "C" as their type code.
 * </p>
 * 
 * @see ActionData
 * @see RequestData
 * @see TransactionData
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class CustomData extends TimerData
{
    /**
     * The type code.
     */
    private static final String TYPE_CODE = "C";

    /**
     * Creates a new CustomData object.
     */
    public CustomData()
    {
        this(null);
    }

    /**
     * Creates a new CustomData object and gives it the specified name. Furthermore, the start time attribute is set to
     * the current time.
     * 
     * @param name
     *            the statistics name
     */
    public CustomData(final String name)
    {
        super(name, TYPE_CODE);
    }
}
