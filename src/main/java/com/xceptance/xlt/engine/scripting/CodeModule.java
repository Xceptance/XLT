package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.util.List;

/**
 * The super class of all code module script files.
 */
public class CodeModule extends Script
{
    /**
     * The names of the parameters to pass to the module when the module is executed.
     */
    private final List<String> parameterNames;

    /**
     * Constructor.
     * 
     * @param scriptFile
     *            the file the script was read from
     * @param parameterNames
     *            the module parameter names
     */
    public CodeModule(final File scriptFile, final List<String> parameterNames)
    {
        super(scriptFile);
        this.parameterNames = parameterNames;
    }

    /**
     * Returns the names of the parameters to pass to the module when the module is executed.
     * 
     * @return the module parameters
     */
    public List<String> getParameterNames()
    {
        return parameterNames;
    }
}
