/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.tyrus.server.Server;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection handler used internally to manage incoming {@link ClientPerformanceExtensionConnection}'s. Requires an
 * implementation of {@link ConnectionListener} which is notified about connection state changes.
 */
public class ClientPerformanceExtensionConnector
{
    private static final Logger LOG = LoggerFactory.getLogger(ClientPerformanceExtensionConnector.class);

    // private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Map<Session, ClientPerformanceExtensionConnection> connections = Collections.synchronizedMap(new HashMap<Session, ClientPerformanceExtensionConnection>());

    private final BlockingQueue<ClientPerformanceExtensionConnection> connectionQueue = new LinkedBlockingQueue<>();

    private final ConnectionListener connectionListener;

    private final String id = String.valueOf(System.identityHashCode(this));

    /**
     * Create a new extension connection handler to start communicating with an extension using WebSockets. provide a
     * call back handler to get notified when the connection state changes.
     * 
     * @param connectionListener
     *            the connection call back handler which is notified about connection state changes
     */
    public ClientPerformanceExtensionConnector(ConnectionListener connectionListener)
    {
        this.connectionListener = connectionListener;
    }

    /**
     * Get the unique id of this connection handler.
     * 
     * @return the unique id of this connection handler
     */
    public String getID()
    {
        return id;
    }

    /**
     * Get the port on which the web socket server is listening on.
     * 
     * @return the listening port of the web socket server
     */
    public int getPort()
    {
        InetSocketAddress address = WebSocketServerEndpoint.getAddress();
        if (address != null)
        {
            return address.getPort();
        }
        return 0;
    }

    /**
     * Start the web socket server listening on any free port using the default WebSocket service address which is
     * "/xlt/&lt;connectorID&gt;".
     * <p>
     * For example the WebSocket service address would look as following:
     * 
     * <pre>
     * String serviceAddress = "ws://localhost:"+this.getPort()+"/xlt/"+this.getID()
     * </pre>
     * </p>
     * 
     * @throws CommunicationException
     *             if starting the WebSocket server failed for any reason
     */
    public void start() throws CommunicationException
    {
        WebSocketServerEndpoint.start(this);
    }

    /**
     * Shutting down the communication and the WebSocket server.
     * 
     * @param timeout
     *            how long to wait for any still running asynchronous requests to be finished before terminating the
     *            communication hard
     */
    public void stop(int timeout)
    {
        synchronized (connections)
        {
            for (ClientPerformanceExtensionConnection eachConnection : connections.values())
            {
                eachConnection.close();
            }
        }
        WebSocketServerEndpoint.stop(this);

        // executorService.shutdownNow();
        // try
        // {
        // executorService.awaitTermination(timeout, TimeUnit.MILLISECONDS);
        // }
        // catch (InterruptedException e)
        // {
        // LOG.error("Shutting down", e);
        // }
        connections.clear();
        connectionQueue.clear();
    }

    /**
     * Get the next available connection that was made or block until a new connection is available. Starts the
     * WebSocket communication as {@link #start()} if required.
     * 
     * @param timeout
     *            how long to wait for connection
     * @return
     * @throws TimeoutException
     *             if no connection was made within the expected time
     * @throws CommunicationException
     *             if there was any communication problem with the client
     * @throws InterruptedException
     *             if the start or connect wait was interrupted
     */
    public ClientPerformanceExtensionConnection waitForNextConnection(long timeout)
        throws TimeoutException, CommunicationException, InterruptedException
    {
        start();

        ClientPerformanceExtensionConnection connection = null;
        while (connection == null || !connection.isOpen())
        {
            connection = connectionQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (connection == null)
            {
                throw new TimeoutException("No connection was made within the expected time frame");
            }
        }
        return connection;
    }

    /**
     * Like {@link #waitForNextConnection(long)} but blocking wait forever till a connection is available.
     * 
     * @return the next connection that is available
     * @throws InterruptedException
     *             if the start or connect wait was interrupted
     * @throws CommunicationException
     *             if there was any communication problem with the client
     */
    public ClientPerformanceExtensionConnection waitForNextConnection() throws InterruptedException, CommunicationException
    {
        start();

        ClientPerformanceExtensionConnection connection = null;
        while (connection == null || !connection.isOpen())
        {
            connection = connectionQueue.take();
        }
        return connection;
    }

    private void onOpen(Session conn)
    {
        ClientPerformanceExtensionConnection extensionConnection = new ClientPerformanceExtensionConnection(conn, connectionListener);
        conn.setMaxIdleTimeout(0);

        connections.put(conn, extensionConnection);
        connectionListener.onConnect(this, extensionConnection);
        try
        {
            connectionQueue.put(extensionConnection);
        }
        catch (InterruptedException e)
        {
            LOG.warn("", e);
        }
    }

    private void onClose(Session conn, CloseReason closeReason)
    {
        ClientPerformanceExtensionConnection extensionConnection = connections.remove(conn);
        if (extensionConnection != null)
        {
            extensionConnection.onClose();
        }
        else
        {
            LOG.warn("Closed unwrapped connection");
        }
    }

    private void onError(Session conn, Throwable ex)
    {
        ClientPerformanceExtensionConnection extensionConnection = connections.get(conn);
        if (extensionConnection != null)
        {
            extensionConnection.onError(ex);
        }
        else
        {
            LOG.warn("Error for unwrapped connection", ex);
        }
    }

    private void onMessage(Session conn, String message)
    {
        ClientPerformanceExtensionConnection extensionConnection = connections.get(conn);
        if (extensionConnection != null)
        {
            extensionConnection.onMessage(message);
        }
        else
        {
            LOG.warn("Message received for unwrapped connection");
        }
    }

    /**
     * The WebSocket end point implementation used internal by {@link ClientPerformanceExtensionConnector}.
     */
    // must be public because of the annotation for TyrusServer
    @ServerEndpoint("/{client-id}")
    public static final class WebSocketServerEndpoint
    {
        private static final Map<String, ClientPerformanceExtensionConnector> connectors = Collections.synchronizedMap(new HashMap<String, ClientPerformanceExtensionConnector>());

        private static volatile Server server;

        private static volatile InetSocketAddress address;

        private static synchronized InetSocketAddress getAddress()
        {
            return address;
        }

        private static boolean isRunning()
        {
            return server != null;
        }

        private static void addEndpointListener(ClientPerformanceExtensionConnector connector)
        {
            connectors.put(connector.getID(), connector);
        }

        private static void removeEndpointListener(ClientPerformanceExtensionConnector connector)
        {
            connectors.remove(connector.getID());
        }

        private static void start(ClientPerformanceExtensionConnector connector) throws CommunicationException
        {
            start("/xlt", connector);
        }

        private static void start(String endPointPath, ClientPerformanceExtensionConnector connector) throws CommunicationException
        {
            start(new InetSocketAddress("127.0.0.1", 0), endPointPath, connector);
        }

        private static synchronized void start(InetSocketAddress address, String endPointPath,
                                               ClientPerformanceExtensionConnector connector)
            throws CommunicationException
        {
            addEndpointListener(connector);
            if (!isRunning())
            {
                try
                {
                    int portToUse = address.getPort();
                    if (portToUse <= 0)
                    {
                        portToUse = -1;

                    }
                    server = new Server(address.getHostString(), portToUse, endPointPath, null, WebSocketServerEndpoint.class);
                    server.start();

                    WebSocketServerEndpoint.address = new InetSocketAddress(address.getAddress(), server.getPort());
                }
                catch (DeploymentException e)
                {
                    kill();
                    throw new CommunicationException("Initializing extension communication failed", e);
                }
            }
        }

        private static void stop(ClientPerformanceExtensionConnector connector)
        {
            if (connectors.size() <= 1)
            {
                kill();
            }
            removeEndpointListener(connector);
        }

        private static synchronized void kill()
        {
            if (isRunning())
            {
                server.stop();
            }
            connectors.clear();
            server = null;
        }

        @OnOpen
        public void onOpen(Session conn, EndpointConfig config, @PathParam("client-id") String clientID)
        {
            ClientPerformanceExtensionConnector connector = connectors.get(clientID);
            if (connector != null)
            {
                connector.onOpen(conn);
            }
            else
            {
                LOG.warn("No open handler available for clientID: " + clientID);
                try
                {
                    conn.close(new CloseReason(CloseCodes.TRY_AGAIN_LATER, "No connection handler available for clientID"));
                }
                catch (IOException e)
                {
                    LOG.warn("", e);
                }
            }
        }

        @OnClose
        public void onClose(Session conn, CloseReason closeReason, @PathParam("client-id") String clientID)
        {
            ClientPerformanceExtensionConnector connector = connectors.get(clientID);
            if (connector != null)
            {
                connector.onClose(conn, closeReason);
            }
            else
            {
                LOG.warn("No close handler available for clientID: " + clientID);
            }
        }

        @OnError
        public void onError(Session conn, Throwable ex, @PathParam("client-id") String clientID)
        {
            ClientPerformanceExtensionConnector connector = connectors.get(clientID);
            if (connector != null)
            {
                connector.onError(conn, ex);
            }
            else
            {
                LOG.warn("No error handler available for clientID: " + clientID);
            }
        }

        @OnMessage
        public void onMessage(Session conn, String message, @PathParam("client-id") String clientID)
        {
            ClientPerformanceExtensionConnector connector = connectors.get(clientID);
            if (connector != null)
            {
                connector.onMessage(conn, message);
            }
            else
            {
                LOG.warn("No message handler available for clientID: " + clientID);
            }
        }
    }

    /**
     * A call back handler used by {@link ClientPerformanceExtensionConnector} to get notified about connection state
     * changes.
     */
    public static interface ConnectionListener
    {
        /**
         * Called when a new connection is available.
         * 
         * @param connector
         *            the connection handler where the new connection is related to
         * @param connection
         *            the new connection that was made
         */
        public void onConnect(ClientPerformanceExtensionConnector connector, ClientPerformanceExtensionConnection connection);

        /**
         * Called for every incoming message for a connection from the client side.
         * 
         * @param connection
         *            for which the message was received
         * @param data
         *            which was transmitted
         * @param responder
         *            to submit a response for this message
         */
        public void onMessage(ClientPerformanceExtensionConnection connection, JSONObject data, Responder responder);

        /**
         * Called for any error that occurred for a connection.
         * 
         * @param connection
         *            for which the error occurred
         * @param throwable
         *            exception that occurred
         */
        public void onError(ClientPerformanceExtensionConnection connection, CommunicationException throwable);

        /**
         * Called when a connection was closed.
         * 
         * @param connection
         *            which was closed
         */
        public void onClose(ClientPerformanceExtensionConnection connection);
    }

    /**
     * A communication handler used internal to send messages and requests to an extension client.
     */
    public static class ClientPerformanceExtensionConnection
    {
        private final ConnectionListener connectionListener;

        private final Session connection;

        private final AtomicLong messageIndex = new AtomicLong(0);

        private final Map<String, ResponseWaitLock> responseWaits = Collections.synchronizedMap(new HashMap<String, ResponseWaitLock>());

        private ClientPerformanceExtensionConnection(Session con, ConnectionListener connectionListener)
        {
            this.connection = con;
            this.connectionListener = connectionListener;
        }

        /**
         * Determine if the communication channel is still open.
         * 
         * @return true if the communication is still open otherwise false
         */
        public boolean isOpen()
        {
            return connection.isOpen();
        }

        /**
         * Stop the communication and close the connection channel.
         * 
         * @return this connection
         */
        public ClientPerformanceExtensionConnection close()
        {
            try
            {
                if (connection.isOpen())
                {
                    connection.close();
                }
            }
            catch (IOException e)
            {
                notifyOnErrorListener(new CommunicationException("Failed to close connection", e));
            }

            synchronized (responseWaits)
            {
                for (ResponseWaitLock eachLock : responseWaits.values())
                {
                    eachLock.abort();
                }
            }
            responseWaits.clear();

            return this;
        }

        private void onMessage(String message)
        {
            try
            {
                final Message deserializedMessage = deserializeMessage(message);
                String messageID = deserializedMessage.getMessageID();

                if (LOG.isTraceEnabled())
                {
                    LOG.trace("Received message " + deserializedMessage);
                }

                ResponseWaitLock responseWait = responseWaits.get(messageID);
                if (responseWait != null)
                {
                    responseWait.setResponse(deserializedMessage);
                }
                else
                {
                    notifyOnMessageListener(deserializedMessage);
                }
            }
            catch (CommunicationException e)
            {
                notifyOnErrorListener(e);
            }
        }

        private void onError(Throwable ex)
        {
            notifyOnErrorListener(new CommunicationException("", ex));
        }

        private void onClose()
        {
            notifyOnCloseListener();
            close();
        }

        private void notifyOnMessageListener(final Message message)
        {
            connectionListener.onMessage(ClientPerformanceExtensionConnection.this, message.getMessageData(),
                                         new Responder(ClientPerformanceExtensionConnection.this, message));
        }

        private void notifyOnErrorListener(final CommunicationException error)
        {
            connectionListener.onError(ClientPerformanceExtensionConnection.this, error);
        }

        private void notifyOnCloseListener()
        {
            connectionListener.onClose(ClientPerformanceExtensionConnection.this);
        }

        private String nextMessageID()
        {
            return String.valueOf(System.identityHashCode(this)) + messageIndex.getAndIncrement();
        }

        private Message deserializeMessage(String message) throws CommunicationException
        {
            try
            {
                JSONObject messageObject = new JSONObject(message);
                String messageID = messageObject.getString("messageID");
                JSONObject data = null;

                if (messageObject.has("data") && !messageObject.isNull("data"))
                {
                    data = messageObject.getJSONObject("data");
                }
                else
                {
                    throw new CommunicationException("No data for message: \"" + message + "\"", null);
                }

                return new Message(messageID, data);
            }
            catch (JSONException e)
            {
                throw new CommunicationException("Failed to deserialize message: \"" + message + "\"", e);
            }
        }

        private String serializeMessage(Message message) throws CommunicationException
        {
            String messageID = message.getMessageID();
            String data = message.getMessageData().toString();

            if (StringUtils.isBlank(message.messageID))
            {
                throw new CommunicationException("No ID for message: \"" + message + "\"", null);
            }
            if (StringUtils.isBlank(data))
            {
                throw new CommunicationException("No data for message: \"" + message + "\"", null);
            }
            return "{\"messageID\":\"" + messageID + "\",\"data\":" + data + "}";
        }

        private ClientPerformanceExtensionConnection send(Message message) throws CommunicationException
        {
            String serializedMessage = serializeMessage(message);
            try
            {
                connection.getBasicRemote().sendText(serializedMessage);
            }
            catch (IOException e)
            {
                throw new CommunicationException("Failed to send message", e);
            }
            return this;
        }

        /**
         * Send a message to the client.
         * 
         * @param data
         *            that should be transmitted
         * @return this connection
         * @throws CommunicationException
         *             if any communication error occurred
         */
        public ClientPerformanceExtensionConnection sendMessage(JSONObject data) throws CommunicationException
        {
            return send(new Message(nextMessageID(), data));
        }

        /**
         * Send a request to the client and wait for the response.
         * 
         * @param data
         *            that should be transmitted
         * @param timeoutMilliseconds
         *            to wait for the response
         * @return the response data
         * @throws CommunicationException
         *             if any communication error occurred
         * @throws TimeoutException
         *             if waiting for the response was interrupted
         */
        public JSONObject sendRequest(JSONObject data, int timeoutMilliseconds) throws CommunicationException, TimeoutException
        {
            Message message = new Message(nextMessageID(), data);
            String messageID = message.getMessageID();

            ResponseWaitLock waitBarrier = new ResponseWaitLock();
            responseWaits.put(messageID, waitBarrier);

            send(message);

            try
            {
                boolean isTimeout = !waitBarrier.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);
                if (isTimeout)
                {
                    throw new TimeoutException("No answer was received within the maximum time");
                }
                else if (waitBarrier.isAborted())
                {
                    throw new CommunicationException("Communication aborted", null);
                }
            }
            catch (InterruptedException e)
            {
                throw new CommunicationException("Communication aborted", e);
            }
            finally
            {
                responseWaits.remove(messageID);
            }
            return waitBarrier.getResponse().getMessageData();
        }

        // /**
        // * Send a request asynchronous to the client. The {@link ResponseHandler} is called when finished.
        // *
        // * @param data
        // * that should be transmitted
        // * @param timeoutMilliseconds
        // * to wait for the response before the timeout callback is called
        // * @param responseHandler
        // * which is notified when the response was received, a timeout or any other error occurred
        // * @return this connection
        // */
        // public ClientPerformanceExtensionConnection sendRequestAsync(JSONObject data, final int timeoutMilliseconds,
        // final ResponseHandler responseHandler)
        // {
        // final Message message = new Message(nextMessageID(), data);
        // final String messageID = message.getMessageID();
        //
        // final ResponseWaitLock waitBarrier = new ResponseWaitLock();
        // responseWaits.put(messageID, waitBarrier);
        //
        // try
        // {
        // send(message);
        // }
        // catch (CommunicationException e)
        // {
        // responseHandler.onError(e);
        // return this;
        // }
        //
        // executorService.execute(new Runnable()
        // {
        // @Override
        // public void run()
        // {
        // try
        // {
        // boolean isTimeout = !waitBarrier.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);
        // responseWaits.remove(messageID);
        // if (isTimeout)
        // {
        // responseHandler.onTimeout();
        // }
        // else if (waitBarrier.isAborted())
        // {
        // responseHandler.onError(new CommunicationException("Communication aborted", null));
        // }
        // else
        // {
        // responseHandler.onResponse(waitBarrier.getResponse().getMessageData());
        // }
        // }
        // catch (InterruptedException e)
        // {
        // responseHandler.onError(new CommunicationException("Communication aborted", e));
        // }
        // }
        // });
        //
        // return this;
        // }
    }

    /**
     * A callback handler for asynchronous requests which is notified by {@link ClientPerformanceExtensionConnection}'s.
     */
    public static interface ResponseHandler
    {
        /**
         * Called when the response for a request was received.
         * 
         * @param data
         *            that was transmitted from the client
         */
        public void onResponse(JSONObject data);

        /**
         * Called when the response was not received within the expected time
         */
        public void onTimeout();

        /**
         * Called if any error occurred for a request.
         */
        public void onError(CommunicationException throwable);
    }

    /**
     * Indicates communication related errors for {@link ClientPerformanceExtensionConnection}'s.
     */
    public static class CommunicationException extends Exception
    {
        private static final long serialVersionUID = 1L;

        public CommunicationException(String message, Throwable throwable)
        {
            super(message, throwable);
        }
    }

    private static class ResponseWaitLock extends CountDownLatch
    {
        private Message response;

        private boolean aborted = false;

        public ResponseWaitLock()
        {
            super(1);
        }

        public Message getResponse()
        {
            return response;
        }

        public void setResponse(Message response)
        {
            this.response = response;
            countDown();
        }

        public boolean isAborted()
        {
            return aborted;
        }

        public void abort()
        {
            aborted = true;
            this.countDown();
        }
    }

    /**
     * Allows the {@link ConnectionListener#onMessage(ClientPerformanceExtensionConnection, JSONObject, Responder)}
     * callback to respond to a message from the client.
     */
    public static class Responder
    {
        private final Message request;

        private final ClientPerformanceExtensionConnection connection;

        private Responder(ClientPerformanceExtensionConnection connection, Message request)
        {
            this.request = request;
            this.connection = connection;
        }

        /**
         * Send response data to the client for a request.
         * 
         * @param response
         *            data that should be transmitted
         * @throws CommunicationException
         *             if any communication error occurred
         */
        public void respond(JSONObject response) throws CommunicationException
        {
            Message answer = new Message(request.getMessageID(), response);
            connection.send(answer);
        }
    }

    private static class Message
    {
        private final String messageID;

        private final JSONObject data;

        private Message(String messageID, JSONObject data)
        {
            this.messageID = messageID;
            this.data = data;
        }

        public String getMessageID()
        {
            return messageID;
        };

        public JSONObject getMessageData()
        {
            return data;
        };

        @Override
        public String toString()
        {
            return "messageID: " + messageID + ", data: " + data;
        }
    }
}
