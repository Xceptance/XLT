/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.engine;

/**
 * The {@link DataLogger} logs custom data for a specific scope to a log file, from where they may be read again 
 * during test report generation. The {@link DataLogger} instance responsible for a certain test user may be 
 * obtained from the current session's data manager object via {@link Session#getDataManager()#dataLogger(String scope)}.
 */
public interface DataLogger
{
    /**
     * Adds a header to the log file. 
     * This only makes sense if no data is in the file yet, 
     * so the header may not be written if this is not the case.
     * @param header
     */
    public void setHeader(String header);
    
    /**
     * Sets the file extension. System requirements apply.
     * @param extension
     */
    public void setExtension(String extension);
    
    /**
     * Adds a line of custom data to the log file.
     * @param lineOfData
     */
    public void log(String lineOfData); 
}
