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

import com.xceptance.xlt.report.scorecard.RuleDefinition;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for collecting multiple {@link RuleDefinition} entries.
 * <p>
 * This container builder is used within the top-level {@code rules} section to define validation rules. Each
 * {@code rule { }} block creates a new rule that is added to the collection..
 * </p>
 * <p>
 * Rules defined here can later be referenced by groups using their IDs.
 * </p>
 *
 * @see RuleDefinition
 * @see RuleBuilder
 */
public class RulesBuilder
{
    /** Accumulated list of rules built from nested rule {} blocks */
    private final List<RuleDefinition> rules = new ArrayList<>();

    /**
     * Defines a single rule within this rules section.
     * <p>
     * Creates a new {@link RuleBuilder}, applies the closure to configure the rule, builds it, and registers it with
     * the configuration.
     * </p>
     *
     * @param closure
     *            the closure defining rule properties using {@link RuleBuilder}
     * @return the builder for potential method chaining
     */
    public RuleBuilder rule(@DelegatesTo(RuleBuilder.class) Closure<?> closure)
    {
        RuleBuilder builder = new RuleBuilder();
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();

        RuleDefinition rule = builder.build();
        rules.add(rule);

        return builder;
    }

    /**
     * Builds and returns all configured rules as an array.
     *
     * @return array of {@link RuleDefinition} instances in definition order
     */
    public RuleDefinition[] build()
    {
        return rules.toArray(new RuleDefinition[0]);
    }
}
