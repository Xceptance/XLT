/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.clientperformance;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.clientperformance.ClientPerformanceExtensionConnector.ClientPerformanceExtensionConnection;
import com.xceptance.xlt.clientperformance.ClientPerformanceExtensionConnector.CommunicationException;
import com.xceptance.xlt.clientperformance.ClientPerformanceExtensionConnector.ConnectionListener;
import com.xceptance.xlt.clientperformance.ClientPerformanceExtensionConnector.Responder;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * Handles WebSocket connections to client-performance Web-Extensions.
 */
public class WebExtConnectionHandler implements ConnectionListener
{
    private static final Log LOG = LogFactory.getLog(WebExtConnectionHandler.class);

    /**
     * The XLT property to set the storage timeout. This defines how long we will wait for the data to be written to the
     * local storage of the browser.
     */
    // currently this is a hidden property only
    private static final String PROPERTY_STORAGE_TIMEOUT = "storage.timeout";

    /**
     * The default value for the property {@value #PROPERTY_STORAGE_TIMEOUT}.
     */
    private static final int STORAGE_TIMEOUT_DEFAULT = 60000;

    /**
     * Default connection timeout in msec.
     */
    private static final int CONNECTION_TIMEOUT_DEFAULT = 5000;

    /**
     * The current session. Kept as reference here since sessions are associated with calling thread and connection
     * listener callbacks are invoked by Grizzly's own worker threads.
     */
    private final SessionImpl session = SessionImpl.getCurrent();

    /**
     * The underlying WebSocket connection handler.
     */
    private final ClientPerformanceExtensionConnector connector;

    /**
     * The maximum time to wait (in msec) for a WebSocket connection to be established.
     */
    private final int connectionTimout;

    /**
     * The maximum time to wait (in msec) for an answer on a previously sent message.
     */
    private final int messageTimeout;

    /**
     * The maximum time to wait (in msec) for the WebExtension to gather its data.
     */
    private final int storageTimeout;

    /**
     * The current WebSocket connection.
     */
    private ClientPerformanceExtensionConnection currentConnection;

    /**
     * Constructor.
     * 
     * @param aConnectionTimout
     *            connection timeout
     * @param aMessageTimeout
     *            message timeout
     * @param aStorageTimeout
     *            storage timeout
     */
    public WebExtConnectionHandler(final int aConnectionTimout, final int aMessageTimeout, final int aStorageTimeout)
    {
        connector = new ClientPerformanceExtensionConnector(this);

        connectionTimout = aConnectionTimout;
        messageTimeout = aMessageTimeout;
        storageTimeout = aStorageTimeout;
    }

    /**
     * Creates a new WebExtConnectionHandler instance using the given property domain.
     * 
     * @param propertyDomain
     *            the property domain to use for reading in configuration
     * @return WebExtConnectionHandler instance
     */
    public static WebExtConnectionHandler newInstance(final String propertyDomain)
    {
        final XltProperties props = XltProperties.getInstance();

        final int storageTimeout = props.getProperty(propertyDomain + PROPERTY_STORAGE_TIMEOUT, STORAGE_TIMEOUT_DEFAULT);
        // might be configurable in the future
        final int connectionTimeout = CONNECTION_TIMEOUT_DEFAULT;
        final int messageTimeout = storageTimeout + 2000; // 2 more seconds to account for communication overhead
        return new WebExtConnectionHandler(connectionTimeout, messageTimeout, storageTimeout);
    }

    /**
     * Determine if we have an open connection.
     * 
     * @return true if we have a connection that is still open
     */
    public boolean isConnected()
    {
        return currentConnection != null && currentConnection.isOpen();
    }

    /**
     * Blocking wait some time for an incoming connection. If we already have an open connection then immediately
     * return. If no connection was made for a certain time then a timeout exception is thrown.
     * 
     * @param timeout
     *            - how long we should wait for a connection
     * @throws TimeoutException
     *             if no connection was made within the expected time
     * @throws CommunicationException
     *             if there was any communication problem with the client
     * @throws InterruptedException
     *             if the start or connect wait was interrupted
     */
    public void waitForConnect(long timeout) throws TimeoutException, CommunicationException, InterruptedException
    {
        if (!isConnected())
        {
            connector.waitForNextConnection(timeout);
        }
    }

    @Override
    public void onConnect(ClientPerformanceExtensionConnector connector, ClientPerformanceExtensionConnection connection)
    {
        LOG.debug("Connected: " + connection);

        if (currentConnection != null && currentConnection.isOpen())
        {
            currentConnection.close();
        }
        currentConnection = connection;
    }

    @Override
    public void onMessage(ClientPerformanceExtensionConnection connection, JSONObject data, Responder responder)
    {
        if (LOG.isTraceEnabled())
        {
            LOG.trace("Message received: " + data);
        }

        try
        {
            if ("DUMP_PERFORMANCE_DATA".equals(data.optString("action")))
            {
                dumpPerformanceData(data.optString("performanceData"));
            }
        }
        catch (Throwable t)
        {
            if (LOG.isWarnEnabled())
            {
                LOG.warn("Failed to handle message: " + data, t);
            }
        }
    }

    @Override
    public void onError(ClientPerformanceExtensionConnection connection, CommunicationException throwable)
    {
        LOG.error("Error from connection: " + connection, throwable);
    }

    @Override
    public void onClose(ClientPerformanceExtensionConnection connection)
    {
        LOG.debug("Extension connection closed: " + connection);
    }

    /**
     * Write JSON string as a set of performance data to the timer file.
     * 
     * @param rawData
     *            A json string with timings in the performance data format
     */
    private void dumpPerformanceData(String rawData)
    {
        try
        {
            final List<ClientPerformanceData> performanceData = PerformanceDataTransformator.getTransformedPerformanceDataList(rawData);

            ClientPerformanceMetrics.updatePerformanceData(session, performanceData);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Dumped client-performance metrics: " + rawData);
            }
        }
        catch (final Throwable t)
        {
            LOG.warn("Failed to dump client performance data", t);
        }
    }

    /**
     * Fetch the remaining timing data from the client which were not sent.
     */
    public String fetchPerformanceData() throws JSONException, TimeoutException, CommunicationException, InterruptedException
    {
        ClientPerformanceExtensionConnection conn = currentConnection;
        if (conn == null || !conn.isOpen())
        {
            conn = connector.waitForNextConnection(connectionTimout);
        }

        final JSONObject message = new JSONObject();
        message.put("action", "GET_DATA");
        message.put("storageTimeout", storageTimeout);

        return conn.sendRequest(message, messageTimeout).getString("data");
    }

    /**
     * Fetch and dump the remaining timing data from the client.
     */
    public void reportRemainingPerformanceData()
    {
        try
        {
            final String performanceDataRaw = fetchPerformanceData();
            dumpPerformanceData(performanceDataRaw);
        }
        catch (final Throwable t)
        {
            LOG.warn("Failed to update remaining client performance data", t);
        }
    }

    /**
     * Starts the WebSocket server if necessary and registers for WebSocket connection events.
     * 
     * @see ClientPerformanceExtensionConnector#start()
     * @throws CommunicationException
     */
    public void start() throws CommunicationException
    {
        connector.start();
    }

    /**
     * Closes the connection, de-registers for WebSocket connection events and shuts down the WebSocket server in case
     * there is no other listener registered at it.
     */
    public void stop()
    {
        connector.stop(CONNECTION_TIMEOUT_DEFAULT);
    }

    /**
     * Returns the port the underlying WebSocket server is listening on.
     * 
     * @return WebSocket server listening port
     */
    public int getPort()
    {
        return connector.getPort();
    }

    /**
     * Returns the ID of the underlying WebSocket connection handler.
     * 
     * @return ID of underlying WebSocket connection handler
     */
    public String getID()
    {
        return connector.getID();
    }
}
