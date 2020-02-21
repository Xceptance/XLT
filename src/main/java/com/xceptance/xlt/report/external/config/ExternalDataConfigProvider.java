package com.xceptance.xlt.report.external.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * @author matthias.ullrich
 */
public class ExternalDataConfigProvider
{
    private static final String CONFIG_FILE = "externaldataconfig";
    
    private static final String CONFIG_FILE_SCHEMA = CONFIG_FILE  + ".xsd";

    private static final String CONFIG_FILE_NAME = CONFIG_FILE + ".xml";

    private ExternalDataConfigProvider()
    {
    }

    /**
     * returns configuration instance
     * 
     * @return configuration instance
     * @throws FileNotFoundException
     *             if configuration file was not found
     * @throws JAXBException
     *             if configuration file could not get parsed
     * @throws SAXException
     */
    public static Config getConfig(final String inputDir) throws FileNotFoundException, JAXBException, SAXException
    {
        final File configFile = getConfigFile(inputDir);
        final Config config = loadConfigFile(configFile);

        return config;
    }

    /**
     * @param inputDir
     * @return the config file, or <code>null</code> if none found
     */
    private static File getConfigFile(final String inputDir)
    {
        // the directories to search for the config file
        final File[] dirs =
            {
                new File(inputDir, "config"), new File(inputDir), XltExecutionContext.getCurrent().getXltConfigDir()
            };

        // now check each directory until the config file is found
        for (final File dir : dirs)
        {
            final File configFile = new File(dir, CONFIG_FILE_NAME);
            if (configFile.isFile() && configFile.canRead())
            {
                return configFile;
            }
        }

        // config file not found
        return null;
    }

    private static Config loadConfigFile(final File configFile) throws JAXBException, FileNotFoundException, SAXException
    {
        if (configFile == null)
        {
            // return empty config
            return new Config();
        }

        final JAXBContext context = JAXBContext.newInstance(Config.class);
        final Unmarshaller um = context.createUnmarshaller();
        final InputStream in = new FileInputStream(configFile);
        final Reader reader = new InputStreamReader(in, Charset.forName("UTF-8"));

        // prepare schema validation
        final URL schemaUrl = ExternalDataConfigProvider.class.getResource(CONFIG_FILE_SCHEMA);
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = schemaFactory.newSchema(schemaUrl);

        final StringBuilder validationErrors = new StringBuilder();
        final ValidationEventHandler validationEventHandler = new ValidationEventHandler()
        {
            public boolean handleEvent(ValidationEvent ve)
            {
                if (ve.getSeverity() != ValidationEvent.WARNING)
                {
                    final ValidationEventLocator vel = ve.getLocator();
                    validationErrors.append("\n-> [" + vel.getLineNumber() + ":" + vel.getColumnNumber() + "] " + ve.getMessage());
                }

                return true;
            }
        };

        um.setSchema(schema);
        um.setEventHandler(validationEventHandler);

        // parse/validate
        Config config = (Config) um.unmarshal(reader);

        // check for validation errors
        if (validationErrors.length() > 0)
        {
            throw new XltException("Errors in config file: " + configFile.getAbsolutePath() + validationErrors);
        }

        return config;
    }
}
