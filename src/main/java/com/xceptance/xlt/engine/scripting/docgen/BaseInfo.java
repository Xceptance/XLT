/*
 * File: BaseInfo.java
 * Created on: Nov 20, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
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
