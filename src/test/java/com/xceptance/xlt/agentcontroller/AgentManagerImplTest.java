/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.agentcontroller;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.agent.AgentInfo;

/**
 * Tests for the {@link AgentManagerImpl}. Unfortunately tests accessing the file system are somehow incomplete to
 * principal limitations of Java. Actually testing the deletion of read only files would require something like a shell
 * script to create such a file (even when setting a file as read-only within Java you can delete it within Java). But
 * even the tests lacking some features are better than nothing.
 * 
 * @author Sebastian Oerding
 */
public class AgentManagerImplTest
{
    /**
     * Tests simple deletion of the &quot;results&quot; directory.
     */
    @Test
    public void removeResultsDirectory()
    {
        final AgentInfo ai = new AgentInfo("007", new File("dummyAgent"));

        checkAgentManagerCanDeleteResultsAndCleanUp(ai);
    }

    /**
     * Tests simple deletion of the &quot;results&quot; directory containing a read only directory.
     */
    @Test
    public void removeResultsDirectoryWithRemainingEmptyDirs()
    {
        final AgentInfo ai = createAgentInfoWithBaseDirCreated();

        final File unremoveableDir = new File(ai.getResultsDirectory().getAbsolutePath() + File.separator + "unremoveableDir");
        unremoveableDir.mkdir();
        unremoveableDir.setReadOnly();
        Assert.assertTrue("Directory \"" + unremoveableDir.getAbsolutePath() + "\" is expected to exist but does not",
                          unremoveableDir.exists());

        checkAgentManagerCanDeleteResultsAndCleanUp(ai);
    }

    /**
     * Tests simple deletion of the &quot;results&quot; directory containing a read only file.
     */
    @Test
    public void removeResultsDirectoryWithRemainingNonEmptyDirs() throws Exception
    {
        final AgentInfo ai = createAgentInfoWithBaseDirCreated();

        final File unremoveableFile = new File(ai.getResultsDirectory(), "unremoveableFile");
        unremoveableFile.createNewFile();
        unremoveableFile.setReadOnly();
        Assert.assertTrue("File \"" + unremoveableFile.getAbsolutePath() + "\" is expected to exist but does not",
                          unremoveableFile.exists());

        checkAgentManagerCanDeleteResultsAndCleanUp(ai);
    }

    /**
     * Initializes a new {@link AgentManagerImpl} which is called to remove the result directory. Afterwards checks that
     * the directory has been deleted as intended and then removes any remaining files / folders created during the test
     * to avoid side effects on other tests.
     * 
     * @param ai
     *            the AgentInfo to use for the AgentManager
     */
    private void checkAgentManagerCanDeleteResultsAndCleanUp(final AgentInfo ai)
    {
        final AgentManager am = new AgentManagerImpl(ai, null, null);
        am.removeResultsDirectory();

        checkResultsHaveBeenDeleted(am.getAgentInfo());

        cleanUp(ai);
    }

    /**
     * Checks that the results directory has been deleted or is empty or includes only sub directories that does not
     * contain any files. This is checked recursively for the sub directories.
     * 
     * @param ai
     *            the AgentInfo from which to get the results directory
     */
    private void checkResultsHaveBeenDeleted(final AgentInfo ai)
    {
        final File resultsBaseDir = ai.getResultsDirectory();
        if (resultsBaseDir != null && resultsBaseDir.exists())
        {
            checkRecursivelyForDirectories(resultsBaseDir);
        }
    }

    /**
     * @return a new AgentInfo with "dummyAgent" as relative base directory created
     */
    private AgentInfo createAgentInfoWithBaseDirCreated()
    {
        final File agentDir = new File("dummyAgent");
        agentDir.mkdir();
        final AgentInfo ai = new AgentInfo("007", agentDir);
        ai.getResultsDirectory().mkdir();
        return ai;
    }

    /**
     * Checks recursively that the argument file is a folder that does not contain any non-folders.
     * 
     * @param file
     *            the file which to check recursively
     */
    private void checkRecursivelyForDirectories(final File file)
    {
        // if (file == null) omitted as long as it remains only a private method
        if (file.isDirectory())
        {
            for (final File f : file.listFiles())
            {
                checkRecursivelyForDirectories(f);
            }
        }
        else
        {
            Assert.assertFalse("Only empty directories are allowed to survive the deletion but got file \"" + file.getAbsolutePath() + "\"",
                               file.isFile());
        }
    }

    /**
     * Deletes the argument file, deletes it recursively if it is a folder.
     * 
     * @param file
     *            the file / folder which to delete recursively.
     */
    private void recursiveDelete(final File file)
    {
        // if (file == null) omitted as long as it remains only a private method
        if (file.isDirectory())
        {
            for (final File f : file.listFiles())
            {
                recursiveDelete(f);
            }
        }
        file.delete();
    }

    /**
     * Removes all directories / files created during the test to ensure that there are no side effects for following
     * tests.
     * 
     * @param ai
     *            the AgentInfo whose directory to delete.
     */
    private void cleanUp(final AgentInfo ai)
    {
        recursiveDelete(ai.getAgentDirectory());
        // This is not a test for functionality of AgentManagerImpl but ensures that the testcase is not broken due to
        // other reasons
        Assert.assertFalse("Directory \"" + ai.getAgentDirectory().getAbsolutePath() + "\" is expected NOT to exist but does so",
                           ai.getAgentDirectory().exists());
    }
}
