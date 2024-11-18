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
     * This only makes sense for csv files and only if no data is in the file yet, 
     * so the header may not be written if this is not the case.
     * @param header
     */
    public void setHeader(String header);
    //TODO what about format? is this freetext string or do we expect csv, i.e. a number of strings seperated 
    //by a fixed delimiter defined seperately?
    
    /**
     * Sets the file extension. System requirements apply.
     * @param extension
     */
    public void setExtension(String extension);
    //TODO is this csv by default or do we want other options? can user set any extension and basically save a 
    //textfile? are there system limits as to what is allowed as a file extension (guess so)?
    
    /**
     * @param lineOfData
     */
    public void log(String lineOfData); 
    //TODO is this supposed to to plausibility check, e.g. for csv?

}
