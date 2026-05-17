import com.xceptance.xlt.report.scorecard.builder.ScorecardBuilder

/**
 * XLT Scorecard Configuration (Groovy DSL)
 * 
 * This file replaces the legacy JSON/YAML scorecards with a powerful, programmable Groovy DSL.
 * It demonstrates how to use the 'metrics' helper object to avoid raw XPath strings,
 * how to use loops to generate repetitive rules, and how to use modern features like 'excludeName'.
 */

//
// 1. Define Categories and Limits
//
// Instead of copy-pasting rules for every page type (which made the YAML massive), 
// we define the pages and their matching regular expressions in a Map. 
// We can then loop over this map to generate everything dynamically.
def pages = [
    Homepage: "^Homepage",
    Catalog:  "^(SelectTopCategory|SelectCategory|ProductDetailView)",
    Account:  "^(Login|Logout|Register|GoToRegistrationForm|GoToSignIn)",
    Cart:     "^(AddToCart|ViewCart)",
    
    // For Checkout, we want to match checkout steps but NOT the final PlaceOrder step.
    // We will use the 'excludeName' parameter in the metrics helper for this later.
    Checkout: "^(StartCheckout|EnterShippingAddress|EnterBillingAddress|EnterPaymentMethod)",
    
    Order:    "^PlaceOrder"
]

// Grading criteria for each page type. The 'points' represent the weight of the grade.
def grades = [
    Aplus: [p95: 100,  p99: 250,  points: 12],
    A:     [p95: 250,  p99: 750,  points: 11],
    B:     [p95: 500,  p99: 1500, points: 10],
    C:     [p95: 1000, p99: 3000, points: 6],
    D:     [p95: 2000, p99: 7000, points: 2],
    F:     [p95: 2001, p99: 7001, points: 0] // F starts above D
]

//
// 2. Define Selectors
//
builder.selectors {
    
    // Dynamically generate P95 and P99 selectors for every page category.
    pages.each { pageName, regex ->
        
        // P95 selector
        selector {
            id "${pageName.toLowerCase()}P95"
            
            // Example of using the modern metrics helper!
            // If the category is 'Checkout', we explicitly exclude the PlaceOrder transaction.
            if (pageName == 'Checkout') {
                expression metrics.requestP95(name: regex, excludeName: "^PlaceOrder")
            } else {
                expression metrics.requestP95(regex)
            }
        }
        
        // P99 selector
        selector {
            id "${pageName.toLowerCase()}P99"
            
            if (pageName == 'Checkout') {
                expression metrics.requestP99(name: regex, excludeName: "^PlaceOrder")
            } else {
                expression metrics.requestP99(regex)
            }
        }
    }
    
    // Global Error Metrics
    // Using the built-in summary helpers
    selector { id 'transactionErrors'; expression metrics.globalErrorPercentage('transactions') }
    selector { id 'actionErrors';      expression metrics.globalErrorPercentage('actions') }
    selector { id 'requestErrors';     expression metrics.globalErrorPercentage('requests') }
    
    // Example of using the HTTP Error metrics helper
    selector { id 'http5xxErrors';     expression metrics.httpErrorCount('5..') }
    
    // Agent CPU usage
    selector { id 'agentCpuMax';       expression metrics.agentCpuMax() }
}

//
// 3. Define Rules
//
builder.rules {
    
    // Generate tiered grading rules for each Page
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
                    success "${gradeName}"
                }
                
                // If performance hits the F grade, we immediately fail the load test.
                if (gradeName == 'F') {
                    enabled false // Just informational in this demo, but you could enable it!
                    failsTest true
                }
            }
        }
    }
    
    // Global Health Rules
    rule {
        id 'agentCpuRule'
        name 'Agent CPU Usage'
        failsTest true
        checks {
            check { selectorId 'agentCpuMax'; isLessThan 95 }
        }
    }
    
    // HTTP Error Rule
    rule {
        id 'httpErrorRule'
        name 'Zero 5xx Errors'
        failsTest true
        checks {
            check { selectorId 'http5xxErrors'; isEqualTo 0 }
        }
    }
    
    // Fallback rule used to force a group failure if no valid grades are achieved
    rule {
        id 'fallThroughF'
        name 'F'
        failsTest true
        failsOn 'PASSED' // A rule with no checks always passes. failsOn='PASSED' inverts it to a failure.
        checks {} 
    }
}

//
// 4. Define Groups
//
builder.groups {
    
    // Generate a 'firstPassed' group for each Page category.
    // The rules evaluate top-down (A+ -> F). The first one that passes determines the points.
    pages.each { pageName, regex ->
        group {
            id pageName
            name "${pageName} Rating"
            mode 'firstPassed'
            
            // Build the prioritized list of rule IDs (e.g., [homepageAplus, homepageA, ...])
            def ruleList = grades.keySet().collect { "${pageName.toLowerCase()}${it}" }
            ruleList << 'fallThroughF' // Catch-all failure
            
            rules(ruleList)
        }
    }
    
    // Infrastructure checks
    group {
        id 'Infrastructure'
        mode 'allPassed'
        rules(['agentCpuRule', 'httpErrorRule'])
    }
}

//
// 5. Define Overall Ratings
//
builder.ratings {
    rating { id 'A'; value 100.0; description "Excellent performance across the board." }
    rating { id 'B'; value 90.0 }
    rating { id 'C'; value 80.0 }
    rating { id 'D'; value 60.0 }
    rating { id 'F'; value 50.0; failsTest true }
}

// Return the builder to let the Evaluator build the configuration
builder
