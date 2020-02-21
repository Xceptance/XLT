package com.xceptance.xlt.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.xceptance.xlt.api.engine.NetworkData;
import com.xceptance.xlt.api.engine.NetworkDataManager;
import com.xceptance.xlt.api.engine.RequestFilter;

/**
 * Implementation of the interface {@link NetworkDataManager}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class NetworkDataManagerImpl implements NetworkDataManager
{
    /**
     * Request statistics.
     */
    private final List<NetworkData> data = Collections.synchronizedList(new ArrayList<NetworkData>());

    /**
     * {@inheritDoc}
     */
    @Override
    public void addData(final NetworkData networkData)
    {
        if (networkData != null)
        {
            data.add(networkData);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NetworkData> getData()
    {
        return getData(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NetworkData> getData(final RequestFilter filter)
    {
        final List<NetworkData> filteredData = new ArrayList<NetworkData>(data);
        if (filter != null)
        {
            for (final Iterator<NetworkData> it = filteredData.iterator(); it.hasNext();)
            {
                final NetworkData n = it.next();
                if (!filter.accepts(n.getRequest()))
                {
                    it.remove();
                }
            }
        }

        return Collections.unmodifiableList(filteredData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        data.clear();
    }

}
