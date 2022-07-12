package com.xceptance.xlt.report.benchmark;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;

import com.xceptance.xlt.api.util.XltLogger;

public class ReportGeneratorBenchmarkTest
{
    public static final int CHUNKSIZE = 1000;
    public static final int THREADS = 1;
    
    /**
     * This is just for performance testing and not meant to be a useful tool
     * @param args
     * @throws FileSystemException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws FileSystemException, InterruptedException
    {
        if (args.length < 1)
        {
            System.err.println("Parameters missing");
            System.exit(-1);
        }
    
        // parameter is just the result dir for now to read from
        // setup program
        ReportGeneratorBenchmarkTest test = new ReportGeneratorBenchmarkTest();
        test.execute(args[0]);
    }

    
    public void execute(final String resultDir) throws FileSystemException, InterruptedException
    {
        XltLogger.runTimeLogger.info("Checking " + resultDir);
        
        final FileObject resultDirObject = VFS.getManager().resolveFile(resultDir);;
        DataProcessor processor = new DataProcessor(resultDirObject);
        processor.readDataRecords();
    }
}
