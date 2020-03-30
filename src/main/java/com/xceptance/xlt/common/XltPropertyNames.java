/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.common;

public class XltPropertyNames
{
    private static final String BASE = "com.xceptance.xlt.";

    public static class ReportGenerator
    {
        private static final String BASE = XltPropertyNames.BASE + "reportgenerator.";

        public static class Errors
        {
            private static final String BASE = ReportGenerator.BASE + "errors.";

            public static final String REQUEST_ERROR_OVERVIEW_CHARTS_LIMIT = BASE + "requestErrorOverviewChartsLimit";

            public static final String TRANSACTION_ERROR_OVERVIEW_CHARTS_LIMIT = BASE + "transactionErrorOverviewChartsLimit";

            public static final String TRANSACTION_ERROR_DETAIL_CHARTS_LIMIT = BASE + "transactionErrorDetailChartsLimit";
        }
    }
}
