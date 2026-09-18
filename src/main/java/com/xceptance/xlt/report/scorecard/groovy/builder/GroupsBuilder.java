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

import com.xceptance.xlt.report.scorecard.GroupDefinition;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for collecting multiple {@link GroupDefinition} entries.
 * <p>
 * This container builder is used within the top-level {@code groups} section to define rule groups. Each
 * {@code group { }} block creates a new group that is added to the collection.
 * </p>
 * <p>
 * Groups determine how rules are organized, how points are calculated, and when the test should fail.
 * </p>
 *
 * @see GroupDefinition
 * @see GroupBuilder
 */
public class GroupsBuilder
{
    /** Accumulated list of groups built from nested group {} blocks */
    private final List<GroupDefinition> groups = new ArrayList<>();

    /**
     * Defines a single group within this groups section.
     * <p>
     * Creates a new {@link GroupBuilder}, applies the closure to configure the group, builds it, and registers it with
     * the configuration.
     * </p>
     *
     * @param closure
     *            the closure defining group properties using {@link GroupBuilder}
     * @return the builder for potential method chaining
     */
    public GroupBuilder group(@DelegatesTo(GroupBuilder.class) Closure<?> closure)
    {
        GroupBuilder builder = new GroupBuilder();
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();

        GroupDefinition group = builder.build();
        groups.add(group);

        return builder;
    }

    /**
     * Builds and returns all configured groups as an array.
     *
     * @return array of {@link GroupDefinition} instances in definition order
     */
    public GroupDefinition[] build()
    {
        return groups.toArray(new GroupDefinition[0]);
    }
}
