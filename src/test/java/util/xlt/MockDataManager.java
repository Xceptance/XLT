/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
