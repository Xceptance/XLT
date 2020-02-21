package util.xlt;

import java.util.ArrayList;
import java.util.List;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.engine.DataManagerImpl;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * Mock implementation of {@link DataManagerImpl} that allows easy access to the data records logged.
 */
public class MockDataManager extends DataManagerImpl
{
    public List<Data> dataRecords = new ArrayList<>();

    public MockDataManager(final SessionImpl session)
    {
        super(session);
    }

    @Override
    public void logDataRecord(final Data data)
    {
        dataRecords.add(data);
    }
}
