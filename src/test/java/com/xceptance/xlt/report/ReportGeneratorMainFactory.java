/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Creates and initializes {@link ReportGenerator} instances with custom arguments plus some default arguments.
 */
class ReportGeneratorMainFactory
{
    static ReportGeneratorMain create(final String[] args) throws Exception
    {
        // add some default arguments
        final String[] finalArgs = ArrayUtils.addAll(args, "-timezone", "GMT", ".");

        ReportGeneratorMain rgm = new ReportGeneratorMain();
        rgm.init(finalArgs);

        return rgm;
    }
}
