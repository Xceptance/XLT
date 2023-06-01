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
package com.xceptance.xlt.mastercontroller;

import java.text.ParseException;

import com.xceptance.common.util.ParseUtils;

/**
 * A load function parser that reads the value of a time/value pair as a double and returns it normalized as a
 * per mil value. For instance, a value of '1.5' will be converted to '1500'.
 */
public class DoubleValueLoadFunctionParser extends AbstractLoadFunctionParser
{
    /**
     * {@inheritDoc}
     */
    protected int parseValue(final String s) throws ParseException
    {
        return (int) Math.ceil(ParseUtils.parseDouble(s) * 1_000);
    }
}
