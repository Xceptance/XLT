package com.xceptance.xlt.engine.har;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xceptance.xlt.engine.har.model.HarLog;
import com.xceptance.xlt.engine.har.model.HarLogRoot;

import java.io.File;
import java.io.IOException;

public class HarWriter
{
    public void writeHarLogToFile(HarLog harLog, File logFile) throws IOException
    {
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(logFile, JsonEncoding.UTF8);
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            jsonGenerator.setCodec(objectMapper);
            jsonGenerator.writeObject(new HarLogRoot(harLog));
        }
        finally
        {
            jsonGenerator.flush();
            jsonGenerator.close();
        }
    }
}
