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
package com.xceptance.xlt.engine.scripting.docgen;

import org.apache.commons.lang3.StringUtils;

/**
 * Information common to scripts and Java modules.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class BaseInfo implements Comparable<BaseInfo>
{
    String description;

    String tags;

    final String name;

    final String id;

    BaseInfo(final String name)
    {
        this(name, null);
    }

    BaseInfo(final String name, final String id)
    {
        this.name = name;
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the tags
     */
    public String[] getTags()
    {
        return StringUtils.split(StringUtils.deleteWhitespace(StringUtils.defaultString(tags)), ',');
    }

    private transient String descriptionMarkup;

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the descriptionMarkup
     */
    public String getDescriptionMarkup()
    {
        if (descriptionMarkup == null)
        {
            descriptionMarkup = Marked.getInstance().markdownToHTML(description);
        }

        return descriptionMarkup;
    }

    public String getSimpleName()
    {
        final int idx = name.lastIndexOf('.');
        if (idx > -1 && idx < name.length() - 1)
        {
            return name.substring(idx + 1);
        }
        return name;
    }

    public String getPackageName()
    {
        final int idx = name.lastIndexOf('.');
        if (idx < 0)
        {
            return "(default package)";
        }
        return name.substring(0, idx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(BaseInfo o)
    {
        int result = getPackageName().compareToIgnoreCase(o.getPackageName());
        if (result == 0)
        {
            result = getSimpleName().compareTo(o.getSimpleName());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
        this.descriptionMarkup = null;
    }
}
