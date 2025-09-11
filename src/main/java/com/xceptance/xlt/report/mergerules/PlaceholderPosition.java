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
package com.xceptance.xlt.report.mergerules;

/**
 * Container for placeholder information within a pattern.
 *
 * @author rschwietzke
 */
public class PlaceholderPosition
{
    /**
     * The type code of the placeholder.
     */
    public final String typeCode;

    /**
     * The index of the capturing group in the pattern.
     */
    public final int capturingGroupIndex;

    /**
     * The start position of the placeholder in the pattern.
     */
    public final int start;

    /**
     * The end position of the placeholder in the pattern.
     */
    public final int end;

    /**
     * The length of the placeholder in the pattern.
     */
    public final int length;
    
    /**
     * Indicates whether the placeholder is marked as used.
     */
    public boolean used = false;

    /**
     * The request filter associated with the placeholder, if any.
     */
    public AbstractRequestFilter requestFilter = null;
    
    /**
     * Constructor to initialize all fields of the placeholder.
     *
     * @param typeCode The type code of the placeholder.
     * @param capturingGroupIndex The index of the capturing group in the pattern.
     * @param start The start position of the placeholder in the pattern.
     * @param end The end position of the placeholder in the pattern.
     * @param length The length of the placeholder in the pattern.
     */
    public PlaceholderPosition(final String typeCode, final int capturingGroupIndex, final int start, final int end, final int length)
    {
        this.typeCode = typeCode;
        this.capturingGroupIndex = capturingGroupIndex;
        this.start = start;
        this.end = end;
        this.length = length;
    }
    
    /**
     * Constructor to initialize a placeholder with minimal information.
     *
     * @param typeCode The type code of the placeholder.
     * @param capturingGroupIndex The index of the capturing group in the pattern.
     * @param start The start position of the placeholder in the pattern.
     * @param used Indicates whether the placeholder is marked as used.
     */
    public PlaceholderPosition(final String typeCode, final int capturingGroupIndex, final int start, final boolean used)
    {
        this.typeCode = typeCode;
        this.capturingGroupIndex = capturingGroupIndex;
        this.start = start;
        this.end = start;
        this.length = 0;
        this.used = used;
    }
}
