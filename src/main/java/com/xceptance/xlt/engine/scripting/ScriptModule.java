package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.util.List;

/**
 * Represents a module script read from a script file.
 */
public class ScriptModule extends CommandScript
{
    /**
     * Constructor.
     * 
     * @param scriptFile
     *            the file the script was read from
     * @param scriptElements
     *            the script elements
     * @param parameters
     *            the script parameters
     */
    public ScriptModule(final File scriptFile, final List<ScriptElement> scriptElements, final List<String> parameters)
    {
        super(scriptFile, scriptElements, parameters);
    }
}
