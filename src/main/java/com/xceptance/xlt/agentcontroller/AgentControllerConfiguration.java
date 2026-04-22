/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import com.xceptance.common.net.InetAddressUtils;
import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltExecutionContext;

import info.schnatterer.mobynamesgenerator.MobyNamesGenerator;

/**
 * The AgentControllerConfiguration is the central place where all configuration information of the agent controller can
 * be retrieved from.
 */
public class AgentControllerConfiguration extends AbstractConfiguration
{
    private static final String PROP_PREFIX = XltConstants.XLT_PACKAGE_PATH + ".agentcontroller.";

    private static final String PROP_AGENTS_DIR = PROP_PREFIX + "agentsdir";

    private static final String PROP_KEYSTORE_PASSWORD = PROP_PREFIX + "keystore.password";

    private static final String PROP_KEY_PASSWORD = PROP_PREFIX + "keystore.key.password";

    private static final String PROP_PASSWORD = PROP_PREFIX + "password";

    private static final String PROP_HOST = PROP_PREFIX + "host";

    private static final String PROP_PORT = PROP_PREFIX + "port";

    private static final String PROP_TEMP_DIR = PROP_PREFIX + "tempdir";

    private static final String PROP_PRIVATE_MACHINE_PREFIX = PROP_PREFIX + "privateMachine.";

    private static final String PROP_PRIVATE_MACHINE_ENABLED = PROP_PRIVATE_MACHINE_PREFIX + "enabled";

    private static final String PROP_PRIVATE_MACHINE_NAME = PROP_PRIVATE_MACHINE_PREFIX + "name";

    private static final String PROP_PRIVATE_MACHINE_TYPE = PROP_PRIVATE_MACHINE_PREFIX + "type";

    private static final String PROP_PRIVATE_MACHINE_XTC_PREFIX = PROP_PRIVATE_MACHINE_PREFIX + "xtc.";

    private static final String PROP_PRIVATE_MACHINE_XTC_HOST = PROP_PRIVATE_MACHINE_XTC_PREFIX + "host";

    private static final String PROP_PRIVATE_MACHINE_XTC_PORT = PROP_PRIVATE_MACHINE_XTC_PREFIX + "port";

    private static final String PROP_PRIVATE_MACHINE_XTC_RELAY_HOST = PROP_PRIVATE_MACHINE_XTC_PREFIX + "relayHost";

    private static final String PROP_PRIVATE_MACHINE_XTC_RELAY_PORT = PROP_PRIVATE_MACHINE_XTC_PREFIX + "relayPort";

    private static final String PROP_PRIVATE_MACHINE_XTC_CLIENT_ID = PROP_PRIVATE_MACHINE_XTC_PREFIX + "clientId";

    private static final String PROP_PRIVATE_MACHINE_XTC_CLIENT_SECRET = PROP_PRIVATE_MACHINE_XTC_PREFIX + "clientSecret";

    private static final String PROP_PRIVATE_MACHINE_XTC_ORG = PROP_PRIVATE_MACHINE_XTC_PREFIX + "org";

    private static final String PROP_PRIVATE_MACHINE_XTC_PROJECT = PROP_PRIVATE_MACHINE_XTC_PREFIX + "project";

    private static final Pattern PRIVATE_MACHINE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?$");

    private static final String PRIVATE_MACHINE_NAME_ERROR = "Invalid private machine name: '%s'. Valid names must be between 1–63 characters long, contain only alphanumeric characters or hyphens, and cannot start or end with a hyphen.";

    private final File agentBinDirectory;

    private final String[] agentCommand;

    private File agentsDirectory;

    private final File configDirectory;

    private final File homeDirectory;

    private final String keyPassword;

    private final File keyStoreFile;

    private final String keyStorePassword;

    private final String userName;

    private final String password;

    private InetAddress host = null;

    private final int port;

    private File tempDir;

    private final boolean privateMachineModeEnabled;

    private final String privateMachineName;

    private final PrivateMachineType privateMachineType;

    private final String xtcHost;

    private final int xtcPort;

    private final String xtcRelayHost;

    private final int xtcRelayPort;

    private final String xtcClientId;

    private final String xtcClientSecret;

    private final String xtcOrg;

    private final String xtcProject;

    /**
     * Creates a new AgentControllerConfiguration object.
     *
     * @param commandLineProperties
     *            the properties specified on the command line
     * @throws IOException
     *             if an I/O error occurs
     */
    public AgentControllerConfiguration(final Properties commandLineProperties) throws IOException
    {
        homeDirectory = XltExecutionContext.getCurrent().getXltHomeDir();
        configDirectory = XltExecutionContext.getCurrent().getXltConfigDir();

        final File propFile = new File(configDirectory, "agentcontroller.properties");

        loadProperties(propFile);

        if (commandLineProperties != null)
        {
            addProperties(commandLineProperties);
        }

        agentsDirectory = getFileProperty(PROP_AGENTS_DIR, new File("agent"));
        if (!agentsDirectory.isAbsolute())
        {
            agentsDirectory = new File(homeDirectory, agentsDirectory.getPath());
        }

        tempDir = getFileProperty(PROP_TEMP_DIR, new File(System.getProperty("java.io.tmpdir")));
        if (!tempDir.isAbsolute())
        {
            tempDir = new File(homeDirectory, tempDir.getPath());
        }
        if (!tempDir.exists())
        {
            FileUtils.forceMkdir(tempDir);
        }
        else if (!(tempDir.isDirectory() && tempDir.canWrite()))
        {
            final String msg = new StringBuilder().append("Temp directory does not exist or is not writable: ")
                                                  .append(tempDir.getCanonicalPath()).toString();
            System.out.println(msg);
            throw new IOException(msg);
        }

        agentBinDirectory = new File(homeDirectory, "bin");
        port = getIntProperty(PROP_PORT, 8500);
        keyStoreFile = new File(configDirectory, "keystore");
        keyStorePassword = getStringProperty(PROP_KEYSTORE_PASSWORD);
        keyPassword = getStringProperty(PROP_KEY_PASSWORD);

        userName = XltConstants.USER_NAME;
        password = getStringProperty(PROP_PASSWORD, null);

        final String hostName = getStringProperty(PROP_HOST, "");
        if (!hostName.isEmpty())
        {
            final InetAddress tempHost = InetAddress.getByName(hostName);

            if (InetAddressUtils.isLocalAddress(tempHost))
            {
                host = tempHost;
            }
            else
            {
                throw new XltException("The value '" + hostName + "' of property '" + PROP_HOST +
                                       "' does not denote a valid local address");
            }
        }

        final String scriptName = SystemUtils.IS_OS_WINDOWS ? "agent.cmd" : "agent.sh";

        // the command is a template only - some parameters needs to be replaced appropriately
        agentCommand = new String[]
            {
                new File(agentBinDirectory, scriptName).getAbsolutePath(), Integer.toString(port), "<agentControllerName>",
                "<agentControllerHost>", "<agentNumber>", "<totalAgentCount>", "<acRemoteAddress>"
            };

        // private machine configuration
        privateMachineModeEnabled = getBooleanProperty(PROP_PRIVATE_MACHINE_ENABLED, false);
        privateMachineName = getNonEmptyStringProperty(PROP_PRIVATE_MACHINE_NAME, getRandomPrivateMachineName());
        privateMachineType = getEnumProperty(PrivateMachineType.class, PROP_PRIVATE_MACHINE_TYPE, PrivateMachineType.MEDIUM);
        xtcHost = getNonEmptyStringProperty(PROP_PRIVATE_MACHINE_XTC_HOST, "xtc.xceptance.com");
        xtcPort = getIntProperty(PROP_PRIVATE_MACHINE_XTC_PORT, 443);
        xtcRelayHost = getNonEmptyStringProperty(PROP_PRIVATE_MACHINE_XTC_RELAY_HOST, "xtc-xlt-relay.xceptance.com");
        xtcRelayPort = getIntProperty(PROP_PRIVATE_MACHINE_XTC_RELAY_PORT, 443);

        if (privateMachineModeEnabled)
        {
            xtcClientId = getNonEmptyStringProperty(PROP_PRIVATE_MACHINE_XTC_CLIENT_ID);
            xtcClientSecret = getNonEmptyStringProperty(PROP_PRIVATE_MACHINE_XTC_CLIENT_SECRET);
            xtcOrg = getNonEmptyStringProperty(PROP_PRIVATE_MACHINE_XTC_ORG);
            xtcProject = getNonEmptyStringProperty(PROP_PRIVATE_MACHINE_XTC_PROJECT);
        }
        else
        {
            xtcClientId = null;
            xtcClientSecret = null;
            xtcOrg = null;
            xtcProject = null;
        }

        validatePrivateMachineName(privateMachineName);
    }

    static String getRandomPrivateMachineName()
    {
        // get a random name, but fix it to use "-" as the separator instead of "_"
        return MobyNamesGenerator.getRandomName().replace('_', '-');
    }

    static void validatePrivateMachineName(final String privateMachineName)
    {
        if (!PRIVATE_MACHINE_NAME_PATTERN.matcher(privateMachineName).matches())
        {
            throw new XltException(String.format(PRIVATE_MACHINE_NAME_ERROR, privateMachineName));
        }
    }

    /**
     * Returns the agent's "bin" directory.
     *
     * @return the bin directory
     */
    public File getAgentBinDirectory()
    {
        return agentBinDirectory;
    }

    /**
     * Returns the command file used to start the agent.
     *
     * @return the command file
     */
    public String[] getAgentCommandLine()
    {
        return agentCommand;
    }

    /**
     * Returns the root directory where all agents are installed to.
     *
     * @return the agents directory
     */
    public File getAgentsDirectory()
    {
        return agentsDirectory;
    }

    /**
     * Returns the agent controller's configuration directory.
     *
     * @return the config directory
     */
    public File getConfigDirectory()
    {
        return configDirectory;
    }

    /**
     * Returns the agent controller's home directory.
     *
     * @return the home directory
     */
    public File getHomeDirectory()
    {
        return homeDirectory;
    }

    /**
     * Returns the agent controller's host address, the local network interface to which the agent controller will be
     * bound.
     *
     * @return the host address, or <code>null</code> in case the agent controller is to be bound to all available local
     *         interfaces
     */
    public InetAddress getHostAddress()
    {
        return host;
    }

    /**
     * Returns the agent controller's host name, the local network interface to which the agent controller will be
     * bound.
     *
     * @return the host name, or <code>null</code> in case the agent controller is to be bound to all available local
     *         interfaces
     */
    public String getHostName()
    {
        return (host != null) ? host.getHostName() : null;
    }

    /**
     * Returns the agent controller key's password.
     *
     * @return the key store password
     */
    public String getKeyPassword()
    {
        return keyPassword;
    }

    /**
     * Returns the agent controller's key store file.
     *
     * @return the key store file
     */
    public File getKeyStoreFile()
    {
        return keyStoreFile;
    }

    /**
     * Returns the agent controller's key store password.
     *
     * @return the key store password
     */
    public String getKeyStorePassword()
    {
        return keyStorePassword;
    }

    /**
     * Returns the user name that must be provided by the master controller to be allowed to communicate with this agent
     * controller.
     *
     * @return the user name
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Returns the password that must be provided by the master controller to be allowed to communicate with this agent
     * controller.
     *
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Returns the agent controller's port.
     *
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Returns the agent controller's temp directory.
     *
     * @return the temp directory
     */
    public File getTempDir()
    {
        return tempDir;
    }

    public boolean isPrivateMachineModeEnabled()
    {
        return privateMachineModeEnabled;
    }

    public String getPrivateMachineName()
    {
        return privateMachineName;
    }

    public PrivateMachineType getPrivateMachineType()
    {
        return privateMachineType;
    }

    public String getXtcHost()
    {
        return xtcHost;
    }

    public int getXtcPort()
    {
        return xtcPort;
    }

    public String getXtcRelayHost()
    {
        return xtcRelayHost;
    }

    public int getXtcRelayPort()
    {
        return xtcRelayPort;
    }

    public String getXtcClientId()
    {
        return xtcClientId;
    }

    public String getXtcClientSecret()
    {
        return xtcClientSecret;
    }

    public String getXtcOrg()
    {
        return xtcOrg;
    }

    public String getXtcProject()
    {
        return xtcProject;
    }

    public enum PrivateMachineType
    {
        TINY,
        SMALL,
        MEDIUM,
        LARGE
    }
}
