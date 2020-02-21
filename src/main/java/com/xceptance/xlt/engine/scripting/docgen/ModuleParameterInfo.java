/*
 * File: ModuleParameterInfo.java
 * Created on: Nov 20, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.xceptance.xlt.engine.scripting.docgen;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Information about module parameters.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@XStreamAlias("parameter")
public class ModuleParameterInfo
{
    @XStreamAsAttribute
    final String name;

    @XStreamAsAttribute
    final String description;

    /**
     * 
     */
    ModuleParameterInfo(final String name, final String description)
    {
        this.name = name;
        this.description = description;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
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
            descriptionMarkup = StringUtils.substringBetween(Marked.getInstance().markdownToHTML(getDescription()), "<p>", "</p>");
        }
        return descriptionMarkup;
    }
}
