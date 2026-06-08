/**
 * XLT Scorecard Configuration (Groovy DSL)
 *
 * Equivalent configuration migrated from scorecard-config.json.
 */

// Define categories and grades to dynamically generate the repetitive page rules
def pages = ['homepage', 'catalog', 'cart', 'account', 'checkout', 'order']
def grades = [
    [id: 'Aplus', name: 'A+', p95: 100, p99: 250, points: 12, enabled: true, failsTest: false],
    [id: 'A',     name: 'A',  p95: 250, p99: 750, points: 11, enabled: true, failsTest: false],
    [id: 'B',     name: 'B',  p95: 500, p99: 1500, points: 10, enabled: true, failsTest: false],
    [id: 'C',     name: 'C',  p95: 1000, p99: 3000, points: 6,  enabled: true, failsTest: false],
    [id: 'D',     name: 'D',  p95: 2000, p99: 7000, points: 2,  enabled: true, failsTest: false],
    [id: 'F',     name: 'F',  p95: 2000, p99: 7000, points: 0,  enabled: false, failsTest: true]
]

// ----------------------------------------------------------------------------
// 1. Selectors
// ----------------------------------------------------------------------------

builder.selectors {
    // Get us the max of all P95 of homepage like requests
    selector {
        id 'homepageP95'
        expression metrics.requestP95(label: 'homepage')
    }
    // Get us the max of all P99 of homepage like requests
    selector {
        id 'homepageP99'
        expression metrics.requestP99(label: 'homepage')
    }
    // Get us the max of all P95 of catalog like requests
    selector {
        id 'catalogP95'
        expression metrics.requestP95(label: 'catalog')
    }
    // Get us the max of all P99 of catalog like requests
    selector {
        id 'catalogP99'
        expression metrics.requestP99(label: 'catalog')
    }
    // Get us the max of all P95 of account like requests
    selector {
        id 'accountP95'
        expression metrics.requestP95(label: 'account')
    }
    // Get us the max of all P99 of account like requests
    selector {
        id 'accountP99'
        expression metrics.requestP99(label: 'account')
    }
    // Get us the max of all P95 of cart like requests
    selector {
        id 'cartP95'
        expression metrics.requestP95(label: 'cart')
    }
    // Get us the max of all P99 of cart like requests
    selector {
        id 'cartP99'
        expression metrics.requestP99(label: 'cart')
    }
    // Get us the max of all P95 of checkout like requests and have enough samples to consider it.
    selector {
        id 'checkoutP95'
        expression "max(//requests/request[labels = 'checkout' and count > 50]/percentiles/p95)"
    }
    // Get us the max of all P99 of checkout like requests and have enough samples to consider it.
    selector {
        id 'checkoutP99'
        expression "max(//requests/request[labels = 'checkout' and count > 50]/percentiles/p99)"
    }
    // Get us the max of all P95 of order like requests
    selector {
        id 'orderP95'
        expression metrics.requestP95(label: 'order')
    }
    // Get us the max of all P99 of order like requests
    selector {
        id 'orderP99'
        expression metrics.requestP99(label: 'order')
    }
    // Get us the order count
    selector {
        id 'orderCount'
        expression "max(//requests/request[labels = 'order']/count)"
    }
    // Get us the order percentage errors
    selector {
        id 'orderErrors'
        expression "sum(//actions/action[labels = 'order']/errorPercentage)"
    }
    // Get transaction error percentage
    selector {
        id 'transactionErrors'
        expression metrics.globalErrorPercentage('transactions')
    }
    // Get action errors
    selector {
        id 'actionErrors'
        expression metrics.globalErrorPercentage('actions')
    }
    // Get requests errors
    selector {
        id 'requestErrors'
        expression metrics.globalErrorPercentage('requests')
    }
}

// ----------------------------------------------------------------------------
// 2. Rules
// ----------------------------------------------------------------------------

builder.rules {
    // Agent CPU and wasting rules
    rule {
        id 'baseCpuCheck'
        name 'Agent CPU Usage'
        description 'Verify that the CPU usage of agents is low enough'
        failsTest false
        enabled true
        points 0
        checks {
            check {
                selector 'count(//agents/agent/totalCpuUsage/mean[number() > 60])'
                isEqualTo 0
            }
        }
        messages {
            success 'Agent CPU usage is within limits'
            fail 'Agent CPU usage might have influenced the result, apply caution'
        }
    }

    rule {
        id 'critialCpuCheck'
        name 'Max Agent CPU Usage'
        description 'Verify that the CPU usage was never too high'
        failsTest true
        enabled true
        points 0
        checks {
            check {
                selector 'max(//agents/agent/totalCpuUsage/max)'
                isLessThan 95
            }
        }
        messages {
            success 'Agent CPU usage is within limits'
            fail 'Max CPU usage was too high for a moment'
        }
    }

    rule {
        id 'wastingResources'
        name 'Agent Wasting'
        description 'Check that we use our machines wisely'
        failsTest false
        enabled true
        negateResult false
        points 0
        checks {
            check {
                selector 'count(//agents/agent/totalCpuUsage/mean[number() < 25])'
                isEqualTo 0
            }
        }
        messages {
            success 'All agents seem to be utilized well enough'
            fail 'You might have too many '
        }
    }

    rule {
        id 'fallThroughF'
        name 'F'
        description 'F'
        failsTest true
        failsOn 'PASSED'
        enabled true
        negateResult false
        points 0
        checks {
        }
        messages {
            success 'F'
            fail ''
        }
    }

    // Generate repetitive page rules dynamically
    pages.each { page ->
        grades.each { grade ->
            rule {
                id "${page}${grade.id}"
                name "${page.capitalize()} ${grade.name}"

                if (grade.id == 'F') {
                    if (page == 'homepage') {
                        description "Homepage F rating criteria, this is for information only, we don't need that here, rather we have a generic F."
                    } else {
                        description "${page.capitalize()} F rating criteria, just for information, a generic F rule wil catch it all."
                    }
                } else {
                    description "${page.capitalize()} ${grade.name} rating criteria"
                }

                failsTest grade.failsTest
                enabled grade.enabled
                negateResult false

                checks {
                    check {
                        selectorId "${page}P95"
                        if (grade.id == 'F') {
                            isGreaterThan grade.p95
                        } else {
                            isLessThanOrEqualTo grade.p95
                        }
                    }
                    check {
                        selectorId "${page}P99"
                        if (grade.id == 'F') {
                            isGreaterThan grade.p99
                        } else {
                            isLessThanOrEqualTo grade.p99
                        }
                    }
                }

                messages {
                    success grade.name
                    fail ''
                }
                points grade.points
            }
        }
    }

    // Error rules
    rule {
        id 'orderErrorsRule'
        name 'Order Errors'
        description 'Ensure that only few orders failed'
        failsTest true
        enabled true
        failsOn 'NOTPASSED'
        negateResult false
        checks {
            check {
                selectorId 'orderErrors'
                isLessThan 1.0
            }
        }
        messages {
            success 'Order failure rate is ok'
            fail 'Too many orders failed'
        }
        points 5
    }

    rule {
        id 'requestErrorsRule'
        name 'Request Errors'
        description 'Request error rate'
        failsTest true
        enabled true
        failsOn 'NOTPASSED'
        negateResult false
        checks {
            check {
                selectorId 'requestErrors'
                isLessThan 0.5
            }
        }
        messages {
            success 'Request failure rate is ok'
            fail 'Too many requests failed'
        }
        points 5
    }

    rule {
        id 'actionErrorsRule'
        name 'Action Errors'
        description 'Action error rate'
        failsTest true
        enabled true
        failsOn 'NOTPASSED'
        negateResult false
        checks {
            check {
                selectorId 'actionErrors'
                isLessThan 0.5
            }
        }
        messages {
            success 'Action failure rate is ok'
            fail 'Too many actions failed'
        }
        points 5
    }

    rule {
        id 'transactionErrorsRule'
        name 'Transaction Errors'
        description 'Transaction error rate'
        failsTest true
        enabled true
        failsOn 'NOTPASSED'
        negateResult false
        checks {
            check {
                selectorId 'transactionErrors'
                isLessThan 1.0
            }
        }
        messages {
            success 'Transaction failure rate is ok'
            fail 'Too many transactions failed'
        }
        points 5
    }
}

// ----------------------------------------------------------------------------
// 3. Groups
// ----------------------------------------------------------------------------

builder.groups {
    group {
        id 'CPUs'
        name 'Agent CPU Usage'
        description 'Verify that the CPU usage of agents is ok'
        failsTest true
        mode 'allPassed'
        enabled true
        rules(['baseCpuCheck', 'critialCpuCheck', 'wastingResources'])
        messages {
            success 'All CPU metrics are ok'
            fail 'Verify the test setup, because some CPU metrics are off'
        }
    }

    // Dynamic page groups
    pages.each { page ->
        group {
            id page.capitalize()
            enabled true
            name "${page.capitalize()} Rating"
            description (page == 'order' ? 'Rates the ordering' : "Rates the ${page}")
            failsTest false
            mode 'firstPassed'
            rules(["${page}Aplus", "${page}A", "${page}B", "${page}C", "${page}D", "${page}F", 'fallThroughF'])
            if (page == 'homepage') {
                messages {
                    success 'Homepage Success'
                    fail 'Homepage fail'
                }
            }
        }
    }

    group {
        id 'Errors'
        enabled true
        name 'Error Check'
        description 'Error occurrences'
        failsTest true
        mode 'allPassed'
        rules(['requestErrorsRule', 'actionErrorsRule', 'transactionErrorsRule', 'orderErrorsRule'])
        messages {
            success 'Error rates seem low enough'
            fail 'Too many errors in some sectors'
        }
    }
}

// ----------------------------------------------------------------------------
// 4. Ratings
// ----------------------------------------------------------------------------

builder.ratings {
    rating {
        id 'poor'
        name 'Poor'
        enabled true
        description 'Load test performed poorly'
        value 50.0
        failsTest false
    }
    rating {
        id 'ok'
        name 'Ok'
        enabled true
        description 'Load test result likely valid'
        value 80.0
        failsTest false
    }
    rating {
        id 'success'
        name 'Success'
        enabled true
        description 'Load test performed nicely'
        value 100.0
        failsTest false
    }
}
