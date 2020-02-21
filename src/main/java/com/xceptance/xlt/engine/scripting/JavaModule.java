package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.util.List;

/**
 * Represents a Java module defined in a script file.
 */
public class JavaModule extends CodeModule
{
    /**
     * The name of the Java class to be run when this module is executed.
     */
    private final String className;

    /**
     * Constructor.
     * 
     * @param scriptFile
     *            the file the script was read from
     * @param parameterNames
     *            the module parameter names
     * @param className
     *            the class name
     */
    public JavaModule(final File scriptFile, final List<String> parameterNames, final String className)
    {
        super(scriptFile, parameterNames);
        this.className = className;
    }

    /**
     * Returns the name of the Java class to be run when this module is executed.
     * 
     * @return the class name
     */
    public String getClassName()
    {
        return className;
    }
}
