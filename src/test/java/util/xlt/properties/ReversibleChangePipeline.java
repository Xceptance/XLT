/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package util.xlt.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a pipeline of reversible changes to have a convenient way to apply / reverse several
 * ReversibleChanges with one call.
 * 
 * @author Sebastian Oerding
 */
public class ReversibleChangePipeline
{
    private final List<ReversibleChange> changes = new ArrayList<ReversibleChange>();

    public ReversibleChangePipeline()
    {
    }

    /**
     * Adds the argument change to the internal data structure and applies it.
     */
    public void addAndApply(final String key, final String newValue)
    {
        final ReversibleChange change = new ReversibleChange(key, newValue);
        change.apply();
        changes.add(change);
    }

    /**
     * Adds the argument change to the internal data structure and applies it.
     */
    public void addAndApply(final String key, final long newValue)
    {
        final ReversibleChange change = new ReversibleChange(key, String.valueOf(newValue));
        change.apply();
        changes.add(change);
    }

    /**
     * Reverses all changes of this pipeline.
     * <p>
     * The changes are reverted in the inverse order they were added. This is required to correctly reverse them in case
     * of different changes sharing the same key.
     * </p>
     */
    public void reverseAll()
    {
        for (int i = changes.size() - 1; i >= 0; i--)
        {
            final ReversibleChange rc = changes.get(i);
            rc.reverse();
        }
    }
}
