package com.xceptance.xlt.engine.metrics.otel;

import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.api.engine.Session;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ResourceAttributes;

public class AgentResourceProvider implements ResourceProvider
{
    @Override
    public Resource createResource(final ConfigProperties config)
    {
        final ProductInformation productInfo = ProductInformation.getProductInformation();
        Attributes atts = Attributes.of(ResourceAttributes.SERVICE_NAMESPACE, "com.xceptance.xlt",
                                        ResourceAttributes.SERVICE_NAME, "loadtest-agent",
                                        ResourceAttributes.SERVICE_VERSION, productInfo.getVersion(),
                                        ResourceAttributes.SERVICE_INSTANCE_ID, Session.getCurrent().getAgentID());
        return Resource.create(atts);
    }
}
