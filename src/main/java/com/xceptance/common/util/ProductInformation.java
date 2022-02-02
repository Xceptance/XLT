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
package com.xceptance.common.util;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.xceptance.xlt.common.XltConstants;

/**
 * The ProductInformation class provides access to product-specific data like the product name, the version and the
 * build number. This data is read from the Manifest file and must be maintained there.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
@XStreamAlias("version")
public final class ProductInformation
{
    /**
     * Singleton implementation of {@link ProductInformation}.
     */
    private static final class ProductInformationSingleton
    {
        // singleton instance
        private static final ProductInformation instance = new ProductInformation();

        /**
         * Default constructor.
         */
        private ProductInformationSingleton()
        {
        }
    }

    /**
     * Class logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ProductInformation.class);

    /**
     * Returns the ProductInformation singleton.
     * 
     * @return the one and only ProductInformation instance.
     */
    public static ProductInformation getProductInformation()
    {
        return ProductInformationSingleton.instance;
    }

    /**
     * The day when the build was made.
     */
    private Date buildDate = new Date();

    /**
     * The product's name.
     */
    private String productName = XltConstants.PRODUCT_NAME;

    /**
     * The product's logogram.
     */
    private String logogram = XltConstants.PRODUCT_LOGOGRAM;

    /**
     * The product's home page.
     */
    private URL productURL;

    /**
     * The revision number.
     */
    private String revisionNumber = "????";

    /**
     * The product's vendor.
     */
    private String vendorName = XltConstants.PRODUCT_VENDOR_NAME;

    /**
     * The product's version.
     */
    private String version = "?.?.?";

    /**
     * Creates a new ProductInformation object and initializes it from the values in the Manifest file.
     */
    private ProductInformation()
    {
        // Find the classes directory / JAR file where this class is located
        // and try to find the manifest file relative to it.

        // do not hard-code the class name to make it refactorable
        final String resourceName = "/" + this.getClass().getName().replace('.', '/') + ".class";

        final URL url = getClass().getResource(resourceName);
        if (url != null)
        {
            final String urlString = url.toString();
            final int i = urlString.lastIndexOf(resourceName);

            if (i != -1)
            {
                // replace this class with the manifest file in the URL
                final String baseUrlString = urlString.substring(0, i);
                final String manifestUrlString = baseUrlString + "/META-INF/MANIFEST.MF";

                try
                {
                    // read the manifest contents
                    final Manifest manifest = new Manifest(new URL(manifestUrlString).openStream());
                    final Attributes mainAttrs = manifest.getMainAttributes();

                    productName = getValue(mainAttrs, "Implementation-Title", productName);
                    logogram = getValue(mainAttrs, "Implementation-Logogram", logogram);
                    vendorName = getValue(mainAttrs, "Implementation-Vendor", vendorName);
                    version = getValue(mainAttrs, "Implementation-Version", version);
                    productURL = new URL(getValue(mainAttrs, "Implementation-URL", XltConstants.PRODUCT_URL));

                    // get our extensions
                    revisionNumber = getValue(mainAttrs, "X-Implementation-Revision", revisionNumber);

                    final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                    buildDate = format.parse(getValue(mainAttrs, "X-Implementation-Date", format.format(buildDate)));
                }
                catch (final Exception ex)
                {
                    log.error("Failed to read product information from manifest file: " + manifestUrlString, ex);
                }
            }
        }
    }

    /**
     * Returns the value for the specified attribute from the given manifest properties. If the attribute cannot be
     * found or its value is empty, the given default value is returned.
     * 
     * @param manifestAttributes
     *            the manifest attributes
     * @param attributeName
     *            the name of the attribute
     * @param defaultValue
     *            the default value
     * @return the value of the attribute
     */
    private String getValue(final Attributes manifestAttributes, final String attributeName, final String defaultValue)
    {
        String value = manifestAttributes.getValue(attributeName);

        if (value == null || value.length() == 0)
        {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Returns the date when the product was built.
     * 
     * @return the build date
     */
    public Date getBuildDate()
    {
        return buildDate;
    }

    /**
     * Returns the product's name.
     * 
     * @return the product name
     */
    public String getProductName()
    {
        return productName;
    }

    /**
     * Returns the product's logogram.
     * 
     * @return the product's logogram
     */
    public String getLogogram()
    {
        return logogram;
    }

    /**
     * Returns the product's home page.
     * 
     * @return the home page
     */
    public URL getProductURL()
    {
        return productURL;
    }

    /**
     * Returns the product's revision number.
     * 
     * @return the revision number
     */
    public String getRevisionNumber()
    {
        return revisionNumber;
    }

    /**
     * Returns the name of the product's vendor.
     * 
     * @return the vendor name
     */
    public String getVendorName()
    {
        return vendorName;
    }

    /**
     * Returns the product's version.
     * 
     * @return the version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns an identifier string for the product.
     * 
     * @return the product identifier
     */
    public String getProductIdentifier()
    {
        return getProductName() + " " + getVersion() + " (r" + getRevisionNumber() + ")";
    }

    /**
     * Returns a condensed identifier string for the product.
     * 
     * @return the condensed product identifier
     */
    public String getCondensedProductIdentifier()
    {
        return getLogogram() + " " + getVersion() + ".r" + getRevisionNumber();
    }

    /**
     * Returns a string representation of this object for debugging purposes.
     * 
     * @return the string representation
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder(64);

        buf.append(this.getClass().getSimpleName()).append('[');
        buf.append("productName=").append(productName);
        buf.append(",version=").append(version);
        buf.append(",revisionNumber=").append(revisionNumber);
        buf.append(",buildDate=").append(buildDate);
        buf.append(",productURL=").append(productURL);
        buf.append(",vendorName=").append(vendorName);
        buf.append(']');

        return buf.toString();
    }
}
