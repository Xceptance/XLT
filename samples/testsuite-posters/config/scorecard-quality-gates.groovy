// =====================================================================
// E-Commerce Load Test — Quality Gate Scorecard
//
// Requirements:
//   1. >= 10,000 transactions/hour (all types combined)
//   2. >= 500 order transactions
//   3. >= 2,500 search transactions
//   4. Request error rate < 0.001%
//   5. Zero HTTP 5xx server errors
//   6. P95 runtime <= 500ms for all requests (except order submission)
//
// Adapt the regex patterns below to your actual transaction/request names.
// =====================================================================

// ── Regex patterns – adapt to your transaction and request names ─────────
def TX_ORDER        = '^TOrder'
def TX_SEARCH       = '^TSearch'
def REQ_ORDER_SUBMIT = '^(PlaceOrder|SubmitOrder|CheckoutConfirm)'

// ── Runtime thresholds (ms) ──────────────────────────────────────────────
def P95_LIMIT = 500


// -----------------------------------------------------------------------
// 1. Selectors
// -----------------------------------------------------------------------
builder.selectors {

    // All transactions per hour across every transaction type.
    // XLT pre-computes countPerHour in the summary – no division required.
    selector {
        id 'allTxPerHour'
        expression metrics.globalCountPerHour('transactions')
    }

    // Order transaction count (total for the whole run)
    selector {
        id 'orderCount'
        expression metrics.transactionCount(TX_ORDER)
    }

    // Search transaction count (total for the whole run)
    selector {
        id 'searchCount'
        expression metrics.transactionCount(TX_SEARCH)
    }

    // Global request error percentage (0–100 scale)
    selector {
        id 'requestErrorRate'
        expression metrics.globalErrorPercentage('requests')
    }

    // Count of all HTTP 5xx server errors (500–599) recorded in this run.
    selector {
        id 'http5xxErrors'
        expression metrics.httpErrorCount('5..')
    }

    // Worst-case P95 across ALL requests EXCEPT the order-submit step
    selector {
        id 'maxRequestP95'
        expression metrics.requestP95(excludeName: REQ_ORDER_SUBMIT)
    }

    // Order-submit P95 – tracked for visibility but not gated
    selector {
        id 'orderSubmitP95'
        expression metrics.requestP95(REQ_ORDER_SUBMIT)
    }
}


// -----------------------------------------------------------------------
// 2. Rules
// -----------------------------------------------------------------------
builder.rules {

    // ── Throughput ──────────────────────────────────────────────────────

    rule {
        id 'txThroughput'
        name 'Transaction Throughput ≥ 10,000/h'
        points 25
        failsTest true
        checks {
            check {
                selectorId 'allTxPerHour'
                isGreaterThanOrEqualTo 10_000
                formatter '%,.0f tx/h'
            }
        }
        messages {
            success 'Throughput meets the 10,000 tx/h minimum'
            fail    'Throughput is below 10,000 tx/h'
        }
    }

    rule {
        id 'orderVolume'
        name 'Order Volume ≥ 500'
        points 20
        failsTest true
        checks {
            check {
                selectorId 'orderCount'
                isGreaterThanOrEqualTo 500
                formatter '%,.0f orders'
            }
        }
        messages {
            success 'At least 500 orders placed'
            fail    'Fewer than 500 orders – target not reached'
        }
    }

    rule {
        id 'searchVolume'
        name 'Search Volume ≥ 2,500'
        points 20
        failsTest true
        checks {
            check {
                selectorId 'searchCount'
                isGreaterThanOrEqualTo 2_500
                formatter '%,.0f searches'
            }
        }
        messages {
            success 'At least 2,500 searches executed'
            fail    'Fewer than 2,500 searches – target not reached'
        }
    }

    // ── Quality ──────────────────────────────────────────────────────────

    rule {
        id 'requestErrorRate'
        name 'Request Error Rate < 0.001%'
        points 20
        failsTest true
        checks {
            check {
                selectorId 'requestErrorRate'
                isLessThan 0.001
                formatter '%.5f %%'
            }
        }
        messages {
            success 'Request error rate is within the 0.001% threshold'
            fail    'Request error rate exceeds 0.001%'
        }
    }

    rule {
        id 'no5xxErrors'
        name 'Zero HTTP 5xx Server Errors'
        points 20
        failsTest true
        checks {
            check {
                selectorId 'http5xxErrors'
                isEqualTo 0
                formatter '%,.0f 5xx errors'
            }
        }
        messages {
            success 'No HTTP 5xx server errors detected'
            fail    'HTTP 5xx server errors were detected'
        }
    }

    // ── Runtime ──────────────────────────────────────────────────────────

    rule {
        id 'requestP95Limit'
        name "Request P95 ≤ ${P95_LIMIT}ms (excl. order submit)"
        points 15
        failsTest true
        checks {
            check {
                selectorId 'maxRequestP95'
                isLessThanOrEqualTo P95_LIMIT
                formatter '%,.0f ms'
            }
        }
        messages {
            success "All request P95 runtimes are within ${P95_LIMIT}ms"
            fail    "At least one request P95 exceeds ${P95_LIMIT}ms"
        }
    }

    // Informational only – 0 points, does not affect the score
    rule {
        id 'orderSubmitP95Info'
        name 'Order Submit P95 (Informational)'
        points 0
        checks {
            check {
                selectorId 'orderSubmitP95'
                isGreaterThan 0   // trivially true; just surfaces the value
                formatter '%,.0f ms'
            }
        }
        messages {
            success 'Order submit P95 recorded'
        }
    }
}


// -----------------------------------------------------------------------
// 3. Groups
// -----------------------------------------------------------------------
builder.groups {

    group {
        id 'throughput'
        name 'Throughput KPIs'
        mode 'allPassed'
        rules(['txThroughput', 'orderVolume', 'searchVolume'])
    }

    group {
        id 'quality'
        name 'Quality KPIs'
        mode 'allPassed'
        rules(['requestErrorRate', 'no5xxErrors'])
    }

    group {
        id 'runtimes'
        name 'Runtime KPIs'
        mode 'allPassed'
        rules(['requestP95Limit', 'orderSubmitP95Info'])
    }
}


// -----------------------------------------------------------------------
// 4. Ratings
// All rules above carry failsTest true, so any single failure fails the
// test regardless of the percentage. The two-tier rating makes the
// pass/fail verdict explicit in the report.
// -----------------------------------------------------------------------
builder.ratings {
    rating { id 'Fail'; value 99.99; failsTest true }  // any rule missed
    rating { id 'Pass'; value 100.0 }                   // all rules passed
}

builder
