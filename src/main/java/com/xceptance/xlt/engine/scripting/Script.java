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
package com.xceptance.xlt.engine.scripting;

import java.io.File;

/**
 * Represents a script read from a script file.
 */
public class Script
{
    /**
     * The file the script was read from.
     */
    private final File scriptFile;

    /**
     * Constructor.
     * 
     * @param scriptFile
     *            the file the script was read from
     */
    public Script(final File scriptFile)
    {
        this.scriptFile = scriptFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Script other = (Script) obj;
        if (scriptFile == null)
        {
            if (other.scriptFile != null)
            {
                return false;
            }
        }
        else if (!scriptFile.equals(other.scriptFile))
        {
            return false;
        }
        if (size != other.size)
        {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((scriptFile == null) ? 0 : scriptFile.hashCode());
        result = prime * result + size;
        return result;
    }

    /**
     * Returns the file the script was read from.
     * 
     * @return the script file
     */
    public File getScriptFile()
    {
        return scriptFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("%s", getScriptFile());
    }

    /**
     * The size of this script.
     */
    int size;

    /**
     * Returns the size of this script.
     * 
     * @return this script's size
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Sets the size of this script.
     * 
     * @param aSize
     *            the new size of this script
     */
    void setSize(final int aSize)
    {
        size = aSize;
    }
}
