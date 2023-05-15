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
package com.xceptance.xlt.api.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is only a utility class for testing
 * 
 * @author rschwietzke
 *
 */
public class XltCharBufferUtil
{
    public static List<XltCharBuffer> toList(final List<String> list)
    {
        return list.stream().map(s -> new XltCharBuffer(s.toCharArray())).collect(Collectors.toList());
    }

    public static List<XltCharBuffer> toList(final String[] list)
    {
        return Arrays.stream(list).map(s -> new XltCharBuffer(s.toCharArray())).collect(Collectors.toList());
    }

    public static SimpleArrayList<XltCharBuffer> toSimpleArrayList(final String[] list)
    {
        var result = new SimpleArrayList<XltCharBuffer>(32);
        
        Arrays.stream(list).map(s -> XltCharBuffer.valueOf(s)).forEach(result::add);
        
        return result;
    }
}
