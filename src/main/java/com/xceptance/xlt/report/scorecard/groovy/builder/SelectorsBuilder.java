/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.scorecard.groovy.builder;

import java.util.ArrayList;
import java.util.List;

import com.xceptance.xlt.report.scorecard.SelectorDefinition;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for collecting multiple {@link SelectorDefinition} entries.
 * <p>
 * This container builder is used within the top-level {@code selectors} section to define reusable XPath selectors.
 * Each {@code selector { }} block creates a new selector that is added to the collection..
 * </p>
 *
 * @see SelectorDefinition
 * @see SelectorBuilder
 */
public class SelectorsBuilder
{
    /** Accumulated list of selectors built from nested selector {} blocks */
    private final List<SelectorDefinition> selectors = new ArrayList<>();

    /**
     * Defines a single selector within this selectors section.
     * <p>
     * Creates a new {@link SelectorBuilder}, applies the closure to configure the selector, builds it, and registers it
     * with the configuration.
     * </p>
     *
     * @param closure
     *            the closure defining selector properties using {@link SelectorBuilder}
     * @return the builder for potential method chaining
     */
    public SelectorBuilder selector(@DelegatesTo(SelectorBuilder.class) Closure<?> closure)
    {
        SelectorBuilder builder = new SelectorBuilder();
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();

        SelectorDefinition selector = builder.build();
        selectors.add(selector);

        return builder;
    }

    /**
     * Builds and returns all configured selectors as an array.
     *
     * @return array of {@link SelectorDefinition} instances in definition order
     */
    public SelectorDefinition[] build()
    {
        return selectors.toArray(new SelectorDefinition[0]);
    }
}
