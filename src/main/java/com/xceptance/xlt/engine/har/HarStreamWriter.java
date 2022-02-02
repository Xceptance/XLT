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
package com.xceptance.xlt.engine.har;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xceptance.xlt.engine.har.model.HarBrowser;
import com.xceptance.xlt.engine.har.model.HarCreator;
import com.xceptance.xlt.engine.har.model.HarEntry;
import com.xceptance.xlt.engine.har.model.HarPage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HarStreamWriter
{
    private final JsonGenerator jsonGenerator;

    private HarStreamWriter(File harFile, String version, HarCreator creator, HarBrowser browser, List<HarPage> pages,
                                   String comment, boolean usePrettyPrint)
        throws IOException
    {
        jsonGenerator = new JsonFactory().createGenerator(harFile, JsonEncoding.UTF8);
        final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonGenerator.setCodec(objectMapper);
        if (usePrettyPrint)
        {
            jsonGenerator.useDefaultPrettyPrinter();
        }

        jsonGenerator.writeStartObject();
        writeHarLogFields(version, creator, browser, pages, comment);
        jsonGenerator.writeFieldName("entries");
        jsonGenerator.writeStartArray();
    }

    private void writeHarLogFields(String version, HarCreator creator, HarBrowser browser, List<HarPage> pages, String comment)
        throws IOException
    {
        jsonGenerator.writeFieldName("log");
        jsonGenerator.writeStartObject();

        // Add optional fields if they are not null
        if (comment != null)
        {
            jsonGenerator.writeFieldName("comment");
            jsonGenerator.writeObject(comment);
        }
        if (browser != null)
        {
            jsonGenerator.writeFieldName("browser");
            jsonGenerator.writeObject(browser);
        }
        if (pages != null)
        {
            jsonGenerator.writeFieldName("pages");
            jsonGenerator.writeObject(pages);
        }

        jsonGenerator.writeFieldName("creator");
        jsonGenerator.writeObject(creator);
        jsonGenerator.writeFieldName("version");
        jsonGenerator.writeObject(version);
    }

    public void addEntry(HarEntry harEntry) throws IOException
    {
        jsonGenerator.writeObject(harEntry);
        jsonGenerator.flush();
    }

    public void closeHar() throws IOException
    {
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        jsonGenerator.close();
    }
}
