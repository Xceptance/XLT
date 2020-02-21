package com.xceptance.xlt.util;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * The FileReplicationIndex holds specific information about a sub tree in the file system, which is necessary for
 * differential file replication. Actually, this is a mapping from files to check sums calculated from their content.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class FileReplicationIndex extends TreeMap<File, Long>
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7634760326488435321L;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        for (final Map.Entry<File, Long> entry : entrySet())
        {
            final File file = entry.getKey();
            final long checkSum = entry.getValue();

            buf.append(file).append(" = ").append(checkSum).append('\n');
        }

        return buf.toString();
    }
}
