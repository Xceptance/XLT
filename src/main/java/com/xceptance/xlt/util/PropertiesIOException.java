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
package com.xceptance.xlt.util;

/**
 * Indicate that a property file was supposed to be loaded but we ran into trouble
 *
 * @author René Schwietzke
 * @since 7.0.0
 */
public class PropertiesIOException extends RuntimeException
{
    /**
     * Pass in a message
     * @param msg the message for the caller
     */
    public PropertiesIOException(final String msg)
    {
        super(msg);
    }
}
