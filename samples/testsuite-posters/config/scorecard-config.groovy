import com.xceptance.xlt.report.scorecard.builder.ScorecardBuilder

//
// Defines the page categories and their regex matchers for requests
//
def pages = [
    Homepage: "^Homepage",
    Catalog: "^(SelectTopCategory|SelectCategory|ProductDetailView)",
    Account: "^(Login|Logout|Register|GoToRegistrationForm|GoToSignIn)",
    Cart: "^(AddToCart|ViewCart)",
    Checkout: "^(StartCheckout|EnterShippingAddress|EnterBillingAddress|EnterPaymentMethod)"
]

//
// Defines the grading criteria
//
def grades = [
    Aplus: [p95: 100, p99: 250, points: 12],
    A:     [p95: 250, p99: 750, points: 11],
    B:     [p95: 500, p99: 1500, points: 10],
    C:     [p95: 1000, p99: 3000, points: 6],
    D:     [p95: 2000, p99: 7000, points: 2],
    F:     [p95: 2001, p99: 7001, points: 0] // F starts above D
]

//
// 1. Define Selectors
//
builder.selectors {
    // Generate selectors for each page category
    pages.each { pageName, regex ->
        // P95 selector
        selector {
            id "${pageName.toLowerCase()}P95"
            expression metrics.requestP95(regex)
        }
        // P99 selector
        selector {
            id "${pageName.toLowerCase()}P99"
            expression metrics.requestP99(regex)
        }
    }

    // Special Checkout handling from YAML (ensure count > 50)
    // We overwrite/add the specific checkout selectors if needed, or just rely on the above if simple.
    // The YAML had: matches(name, '...') and count > 50. Let's add that logic.
    // For simplicity of this demo, we keep the simple ones above, but show how to add specific ones manually:
    selector {
        id "checkoutP95Safe"
        expression "max(//requests/request[matches(name, '${pages.Checkout}') and count > 50]/percentiles/p95)"
    }
    
    // Additional Global Metrics
    selector { id 'transactionErrors'; expression metrics.globalErrorPercentage('transactions') }
    selector { id 'actionErrors'; expression metrics.globalErrorPercentage('actions') }
    selector { id 'requestErrors'; expression metrics.globalErrorPercentage('requests') }
    
    // Agent CPU
    selector { id 'agentCpuMean'; expression metrics.agentCpuMeanHigh(60) }
    selector { id 'agentCpuMax'; expression metrics.agentCpuMax() }
    selector { id 'agentCpuLow'; expression 'count(//agents/agent/totalCpuUsage/mean[number() < 25])' }
}

//
// 2. Define Rules
//
builder.rules {
    // Generate Grading Rules for each Page
    pages.each { pageName, regex ->
        grades.each { gradeName, limits ->
            rule {
                id "${pageName.toLowerCase()}${gradeName}"
                name "${pageName} ${gradeName}"
                enabled true
                points limits.points
                checks {
                    check {
                        selectorId "${pageName.toLowerCase()}P95"
                        
                        // The 'F' grade acts as our absolute failure threshold.
                        // If the performance is WORSE than our lowest acceptable grade (D), 
                        // it falls into the F category. Thus, we check if the value is GREATER THAN
                        // the D limit. For all other passing grades (A+, A, B, C, D), 
                        // we verify the performance is LESS THAN OR EQUAL TO their respective limits.
                        if (gradeName == 'F') {
                            isGreaterThan grades.D.p95
                        } else {
                            isLessThanOrEqualTo limits.p95
                        }
                    }
                    check {
                        selectorId "${pageName.toLowerCase()}P99"
                        if (gradeName == 'F') {
                            isGreaterThan grades.D.p99
                        } else {
                            isLessThanOrEqualTo limits.p99
                        }
                    }
                }
                messages {
                    success gradeName
                }
                // F logic: failsTest if enabled? YAML said F failsTest=true for generic F, disabled for specific?
                // YAML: homepageF failsTest: true, enabled: false.
                if (gradeName == 'F') {
                    enabled false
                    failsTest true
                }
            }
        }
    }
    
    // CPU Rules
    rule {
        id 'baseCpuCheck'
        name 'Agent CPU Usage'
        enabled true
        checks {
            check { selectorId 'agentCpuMean'; isEqualTo 0 }
        }
    }
    rule {
        id 'critialCpuCheck'
        name 'Max Agent CPU Usage'
        failsTest true
        checks {
            check { selectorId 'agentCpuMax'; isLessThan 95 }
        }
    }
    
    // Error Rules
    ['transactionErrors', 'actionErrors', 'requestErrors'].each { errSel ->
        rule {
            id "${errSel}Rule"
            name "${errSel} Check"
            failsTest true
            failsOn 'NOTPASSED'
            points 5
            checks {
                check { selectorId errSel; isLessThan 0.5 } // Simplified limit
            }
        }
    }
    
    // Fallback F Rule
    rule {
        id 'fallThroughF'
        name 'F'
        failsTest true
        failsOn 'PASSED'
        checks {} // No checks, always passes -> but failsOn PASSED means it triggers fail
    }
}

//
// 3. Define Groups
//
builder.groups {
    // Generate Groups for each Page to pick the best grade
    pages.each { pageName, regex ->
        group {
            id pageName
            name "${pageName} Rating"
            mode 'firstPassed'
            // Construct rule list: [homepageAplus, homepageA, ..., homepageF, fallThroughF]
            def ruleList = grades.keySet().collect { "${pageName.toLowerCase()}${it}" }
            ruleList << 'fallThroughF'
            rules(ruleList)
        }
    }
    
    // CPU Group
    group {
        id 'CPUs'
        name 'CPU Checks'
        mode 'allPassed'
        rules(['baseCpuCheck', 'critialCpuCheck'])
    }
}

//
// 4. Define Ratings
//
builder.ratings {
    rating { id 'A'; value 100.0; description "All good." }
    rating { id 'B'; value 90.0 }
    rating { id 'C'; value 80.0 }
    rating { id 'D'; value 60.0 }
    rating { id 'F'; value 50.0; failsTest true }
}

// Return the builder to let the Evaluator build the configuration
builder
