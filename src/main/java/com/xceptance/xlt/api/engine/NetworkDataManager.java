package com.xceptance.xlt.api.engine;

import java.util.List;

/**
 * The {@link NetworkDataManager} provides access to all the network requests made during an action. The
 * {@link NetworkDataManager} instance responsible for a certain test user may be obtained from the current session
 * object via {@link Session#getNetworkDataManager()}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public interface NetworkDataManager
{
    /**
     * Adds the given network data.
     * 
     * @param data
     *            network data to be added
     */
    public void addData(final NetworkData data);

    /**
     * Returns the collected network data.
     * 
     * @return network data
     */
    public List<NetworkData> getData();

    /**
     * Returns the collected network data filtered by the given request filter.
     * 
     * @param filter
     *            the request filter to be used for filtering
     * @return filtered network data
     */
    public List<NetworkData> getData(final RequestFilter filter);

    /**
     * Clears all data.
     */
    public void clear();
}
