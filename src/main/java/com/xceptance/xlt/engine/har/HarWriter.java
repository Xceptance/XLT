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
