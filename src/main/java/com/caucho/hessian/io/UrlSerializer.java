/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.caucho.hessian.io;

import java.net.URL;

/**
 * A custom serializer that serializes {@link URL} objects as a string. URLs serialized this way can be deserialized
 * with {@link UrlDeserializer} only.
 * <p>
 * To become effective, this class must be registered as custom serializer in file
 * <code>META-INF/hessian/serializers</code>.
 * <p>
 * This new pair of serializer/deserializer was needed to quickly work around a bug in Hessian. Due to a change in the
 * URL class in Java 1.8.0_72, the standard object deserializer in Hessian is not able any longer to deserialize URLs
 * without errors. See #2590 for more information.
 */
public class UrlSerializer extends StringValueSerializer
{
    // The functionality of the super class is already sufficient, but we want to have a concrete class for URLs.
}
