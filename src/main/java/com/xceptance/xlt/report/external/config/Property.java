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
package com.xceptance.xlt.report.external.config;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author matthias.ullrich
 */
public class Property
{
    private String key;

    private String value = null;

    /**
     * property name
     * 
     * @return property name
     */
    @XmlAttribute(name = "key", required = true)
    public String getKey()
    {
        return key;
    }

    /**
     * property value
     * 
     * @return property value
     */
    @XmlAttribute(name = "value", required = true)
    public String getValue()
    {
        return value;
    }

    /*
     * DO NOT REMOVE METHODS BELOW !
     */

    @SuppressWarnings("unused")
    private void setKey(final String key)
    {
        this.key = key;
    }

    @SuppressWarnings("unused")
    private void setValue(final String value)
    {
        this.value = value;
    }
}
