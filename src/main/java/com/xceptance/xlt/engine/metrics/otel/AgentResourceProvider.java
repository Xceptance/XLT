package com.xceptance.xlt.engine.metrics.otel;

import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.api.engine.Session;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ServiceAttributes;

public class AgentResourceProvider implements ResourceProvider
{
    // Service attributes that are still experimental

    private static final AttributeKey<String> SERVICE_NAMESPACE = AttributeKey.stringKey("service.namespace");

    private static final AttributeKey<String> SERVICE_INSTANCE_ID = AttributeKey.stringKey("service.instance.id");

    @Override
    public Resource createResource(final ConfigProperties config)
    {
        final ProductInformation productInfo = ProductInformation.getProductInformation();
        Attributes atts = Attributes.of(SERVICE_NAMESPACE, "com.xceptance.xlt", 
                                        ServiceAttributes.SERVICE_NAME, "loadtest-agent",
                                        ServiceAttributes.SERVICE_VERSION, productInfo.getVersion(), 
                                        SERVICE_INSTANCE_ID, Session.getCurrent().getAgentID());
        return Resource.create(atts);
    }
}
