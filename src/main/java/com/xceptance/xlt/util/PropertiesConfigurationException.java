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
package com.xceptance.xlt.util;

/**
 * Communicates to all callers that the properties are not correctly setup.
 * Deal with the outcome at will.
 *
 * @author rschwietzke
 */
public class PropertiesConfigurationException extends RuntimeException
{
    /**
     * Pass in a message
     * @param msg the message for the caller
     */
    public PropertiesConfigurationException(final String msg)
    {
        super(msg);
    }
}
