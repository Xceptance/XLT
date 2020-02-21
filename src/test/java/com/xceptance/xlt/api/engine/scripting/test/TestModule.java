/*
 * File: TestModule.java
 * Created on: Sep 22, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.xceptance.xlt.api.engine.scripting.test;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptModule;

/**
 * TODO: Add class description.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class TestModule extends AbstractHtmlUnitScriptModule
{
    public String doResolve(String resolvable)
    {
        return resolve(resolvable);
    }
}
