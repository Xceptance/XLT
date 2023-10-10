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
package com.xceptance.xlt.report.mergerules;

/**
 * Container for placeholder information within a pattern. It is thread safe and can be shared.
 *
 * @author rschwietzke
 */
public class PlaceholderPosition
{
    public final String typeCode;

    public final int typeCodeHashCode;

    public final int index;

    public final int start;

    public final int end;

    public final int length;

    public PlaceholderPosition(final String typeCode, final int index, final int start, final int end, final int length)
    {
        this.typeCode = typeCode;
        this.typeCodeHashCode = typeCode.hashCode();

        this.index = index;
        this.start = start;
        this.end = end;
        this.length = length;
    }
}
