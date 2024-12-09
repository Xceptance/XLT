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
