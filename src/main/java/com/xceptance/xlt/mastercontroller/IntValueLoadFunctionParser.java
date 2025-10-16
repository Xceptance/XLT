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
package com.xceptance.xlt.mastercontroller;

import java.text.ParseException;

import com.xceptance.common.util.ParseUtils;

/**
 * A load function parser that reads and returns the value of a time/value pair as an int.
 */
public class IntValueLoadFunctionParser extends AbstractLoadFunctionParser
{
    /**
     * {@inheritDoc}
     */
    protected int parseValue(final String s) throws ParseException
    {
        return ParseUtils.parseInt(s);
    }
}
