/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
