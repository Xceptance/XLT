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
package com.xceptance.xlt.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to define all global needed loggers and their properties.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class XltLogger
{
    /**
     * The runtime logger.
     */
    public static final Logger runTimeLogger = LoggerFactory.getLogger("runtime");

    /**
     * The report logger.
     */
    public static final Logger reportLogger = LoggerFactory.getLogger("report");
}
