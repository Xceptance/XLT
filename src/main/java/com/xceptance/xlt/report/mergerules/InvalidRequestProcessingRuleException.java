/*
 * File: InvalidMergeRuleException.java
 * Created on: May 21, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.xceptance.xlt.report.mergerules;

/**
 * Exception used to indicate an invalid request processing rule.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class InvalidRequestProcessingRuleException extends Exception
{
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = 7981238688848743328L;

    /**
     * 
     */
    public InvalidRequestProcessingRuleException()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public InvalidRequestProcessingRuleException(String message)
    {
        super(message);
        // TODO Auto-generated constructor stub
    }
}
