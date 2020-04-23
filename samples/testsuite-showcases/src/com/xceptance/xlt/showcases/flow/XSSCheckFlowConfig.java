/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
/**
 * 
 */
package com.xceptance.xlt.showcases.flow;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for the {@link XSSCheckFlow}.
 */
public class XSSCheckFlowConfig
{
    /**
     * Attack strings for xss check
     */
    private List<String> xssAttackStrings = new LinkedList<String>();

    /**
     * Runtime of xss check
     */
    private int runtime = 5;

    /**
     * @return the xssAttackStrings
     */
    public List<String> getXssAttackStrings()
    {
        return xssAttackStrings;
    }

    /**
     * @param xssAttackStrings
     *            the xssAttackStrings to set
     */
    public void setXssAttackStrings(final List<String> xssAttackStrings)
    {
        this.xssAttackStrings = xssAttackStrings;
    }

    /**
     * @return the runtime
     */
    public int getRuntime()
    {
        return runtime;
    }

    /**
     * @param runtime
     *            the runtime to set
     */
    public void setRuntime(final int runtime)
    {
        this.runtime = runtime;
    }
}
