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
