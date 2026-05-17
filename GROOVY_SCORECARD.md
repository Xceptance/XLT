# Groovy Scorecard Configuration

The Groovy Scorecard feature allows you to define complex, dynamic scorecard configurations using a Groovy DSL. This is especially powerful when you need to dynamically adapt your scorecard based on the actual report data, external properties, or when you want to avoid writing verbose XML configurations.

## 1. Core Engine & Bindings

When your `.groovy` scorecard script executes, it runs inside a secured sandbox with several pre-injected "bindings" (variables) ready for you to use:

* `builder`: The core `ScorecardBuilder` used to define your rules, groups, and ratings.
* `metrics`: A helper object that generates complex XPath strings for standard report metrics automatically.
* `properties`: Access to your XLT test properties.
* `xpath`: A tool to query the live `testreport.xml` data during script execution.
* `log`: A logger for debugging your script.

At the very end of your script, you must return the `builder` object so the engine can evaluate it.

---

## 2. A Basic Example

To get started, create a file with the `.groovy` extension (e.g., `scorecard.groovy`) and point the XLT report generator to it by configuring the following property in your `project.properties` (or any other file in the XLT property hierarchy):

```properties
com.xceptance.xlt.scorecard.config = scorecard.groovy
```

Here is a minimal, complete example:

```groovy
// 1. Define our rules
builder.rules {
    rule {
        id 'homepage_p95'
        name 'Homepage Max P95'
        points 10
        checks {
            check {
                // Use the metrics helper to get the P95 for the Homepage
                selector metrics.requestP95('^Homepage')
                isLessThanOrEqualTo 500
            }
        }
        messages {
            success "Homepage runtime is within range"
            fail "Homepage runtime exceeds 500 ms"
        }
    }
}

// 2. Group the rules
builder.groups {
    group {
        id 'runtime'
        name 'Runtime KPIs'
        rules(['homepage_p95'])
    }
}

// 3. Define the final ratings
builder.ratings {
    rating { id 'Fail'; value 99.99; failsTest true }
    rating { id 'Pass'; value 100.0 }
}

// 4. Return the builder
builder
```

---

## 3. Step-by-Step Approach

Building a scorecard generally follows a three-step process: defining rules, grouping them, and setting the overall grading scale.

### 3.1 Rules & Checks
Rules are the heart of the scorecard. Each rule contains one or more `checks` and defines a number of achievable `points`. If all checks pass, the rule contributes its points to the final score. If a rule has `points 0`, it acts as an informational rule and won't affect the final rating percentage.

```groovy
builder.rules {
    rule {
        id 'cpu_check'
        points 5
        checks {
            check {
                selector metrics.agentCpuMax()
                isLessThan 90
            }
        }
    }
}
```

**Fluent Assertions:** You must use one of the following methods to evaluate the selector's value: `isLessThan(value)`, `isLessThanOrEqualTo(value)`, `isGreaterThan(value)`, `isGreaterThanOrEqualTo(value)`, `isEqualTo(value)`, `isNotEqualTo(value)`, or `matchesRegex(pattern)`.

**The `metrics` Helper:** Writing raw XPath is tedious. The `metrics` object abstracts this away using named parameters:
* `metrics.requestP95('^Homepage')` (legacy shorthand: matches by regex against the name)
* `metrics.requestP95(name: '^Homepage')` (explicit regex match against name)

**Label Selection:**
If your test run makes use of labels to group requests, you can use the built-in named parameters to match by label instead of by name regex:
*   `requestP95(label: 'homepage')`
This performs an exact match on the label property rather than a regex over the name.

You can also combine both conditions to be extremely precise:
*   `requestP95(name: '^Homepage', label: 'critical')`
This performs an `and` match against both the regex and the label property.

Similarly, these helpers are available for **transactions**, **actions** and **customTimers**:
*   `transactionP95(name: '...Regex')`, `transactionP95(label: '...Label')`, `transactionCount(label: '...')`, `actionErrorPercentage(label: '...')`, etc.

### 3.2 Groups
Groups logically organize your rules. A group calculates its achieved points based on its `mode`:
* `allPassed`: The standard mode. The group's achieved points are the sum of all passed rules. The group's total achievable points are the sum of all rules.
* `firstPassed` / `lastPassed`: Used for tiered grading (e.g., trying to assign an A, B, or C grade to a specific page). The group evaluates rules in order and takes the points from the *first* (or *last*) rule that passes. The group's total achievable points are set to the *maximum* points of any single rule in the group.

```groovy
builder.groups {
    group {
        id 'system_health'
        mode 'allPassed'
        rules(['cpu_check', 'memory_check'])
    }
}
```

### 3.3 Ratings
Ratings determine the final outcome of the load test by summing up all achieved points from all groups and dividing by the total achievable points to calculate a percentage. 

The engine processes your ratings in the exact order they are defined. The **first rating** whose `value` is greater than or equal to the achieved percentage is selected as the final rating. This means `value` acts as the **upper limit**.

```groovy
builder.ratings {
    rating { id 'Poor'; value 50.0; failsTest true } // <= 50.0%
    rating { id 'Ok'; value 95.0 }                   // <= 95.0%
    rating { id 'A'; value 100.0 }                   // <= 100.0%
}
```

---

## 4. Advanced Concepts

### 4.1 Reusable Selectors
If you use the same selector across multiple rules, you can define it once in the `builder.selectors` block and reference it by ID.

```groovy
builder.selectors {
    selector {
        id 'homepage_p95'
        expression metrics.requestP95('^Homepage')
    }
}

builder.rules {
    rule {
        id 'rule1'
        checks {
            check {
                selectorId 'homepage_p95'
                isLessThan 500
            }
        }
    }
}
```

### 4.2 Dynamic Rules via XPath & Properties
Because this is Groovy, you can use loops and live data to generate rules dynamically. The `xpath` binding allows you to query the `testreport.xml` while the configuration is being built!

```groovy
// Read a property from project.properties
def defaultLimit = properties.getInt('scorecard.limit.default', 800)

// Get a list of all request names that actually occurred in this specific test
def allRequests = xpath.getList('//requests/request/name')

builder.rules {
    allRequests.each { reqName ->
        rule {
            id "req_${reqName}"
            checks {
                check {
                    selector metrics.requestP95(reqName)
                    isLessThan defaultLimit
                }
            }
        }
    }
}
```

### 4.3 Value Formatting
You can optionally format the raw values retrieved by the selector using Java string formatter syntax. This formatting is applied **after** the evaluation is complete, meaning it only affects how the value is visually presented in the final HTML report output (the fluent assertions will always run against the raw unformatted data).

```groovy
check {
    selector metrics.requestMean('^Homepage')
    isLessThan 1000
    formatter '%,.2f ms' // Formats 1234.5678 as 1,234.57 ms in the report
}
```

### 4.4 Manual Results & Ratings
If you want to bypass the point-based evaluation entirely, you can force a check to a specific status, or force the entire scorecard to a specific rating.

**Manual Check Status:**
```groovy
check {
    status 'PASSED'     // SKIPPED, PASSED, FAILED, ERROR
    value 'Manually Set'
}
```

**Manual Rating Selection:**
```groovy
builder.ratings {
    rating { id 'A'; value 100.0 }
    rating { id 'F'; value 0.0; active true; failsTest true }  // Always force an F
}
```

### 4.5 Debugging with the Log Binding
If your script is doing complex logic and you need to see what's happening, use the `log` binding.

```groovy
log.info("Generating rules for ${allRequests.size()} requests...")
if (allRequests.isEmpty()) {
    log.error("No requests found in the report!")
}
```
These logs will be printed to the console during report generation, and are also attached to the scorecard in your final HTML report (typically visible under the log output or error details).

---

## 5. Security

Groovy scripts run in a secured sandbox. Many standard Java classes (like `System`, `Runtime`, `File`, `Thread`) are blocked to prevent malicious scripts from affecting the system. Only whitelisted classes (like `java.util.*`, `java.text.*`) and project-specific DSL classes are allowed.

---

## 6. Complete Advanced Example

This example demonstrates how to dynamically fetch critical requests from the report, apply a configurable threshold, and group them securely.

```groovy
// 1. Fetch live data and properties
def criticalRequests = xpath.getList("//requests/request[importance='high']/name")
def defaultLimit = properties.getInt('scorecard.limit.default', 800)

log.info("Found ${criticalRequests.size()} critical requests to evaluate.")

// 2. Dynamically build rules
builder.rules {
    criticalRequests.each { reqName ->
        rule {
            id "crit_${reqName}"
            name "Critical: ${reqName}"
            points 20
            failsTest true
            checks {
                check {
                    selector metrics.requestP95(reqName)
                    isLessThan defaultLimit
                }
            }
        }
    }
}

// 3. Group the dynamic rules
builder.groups {
    group {
        id 'critical_kpis'
        name 'Critical KPIs'
        rules(criticalRequests.collect { "crit_${it}" })
        mode 'allPassed'
    }
}

// 4. Set Ratings
builder.ratings {
    rating { id 'A'; value 100.0 }
    rating { id 'B'; value 90.0 }
    rating { id 'F'; value 50.0; failsTest true }
}

builder
```

---

## 7. DSL Reference

### `selectors`
* `selector { ... }`
  * `id (String)`: Unique identifier.
  * `expression (String)`: The XPath expression (e.g., `metrics.requestP95('...')`).

### `rules`
* `rule { ... }`
  * `id (String)`: Unique identifier.
  * `name (String)`: Display name.
  * `enabled (boolean)`: Default is `true`.
  * `points (int)`: Achievable points.
  * `failsTest (boolean)`: Whether failing this rule fails the whole test.
  * `failsOn (String)`: `FAILED` or `ERROR`.
  * `negateResult (boolean)`: Flip the outcome.
  * `checks { ... }`
    * `check { ... }`
      * `selector (String)`: Inline XPath.
      * `selectorId (String)`: Reference to a predefined selector.
      * Fluent Assertions (required):
        * `isLessThan(value)`
        * `isLessThanOrEqualTo(value)`
        * `isGreaterThan(value)`
        * `isGreaterThanOrEqualTo(value)`
        * `isEqualTo(value)`
        * `isNotEqualTo(value)`
        * `matchesRegex(String)`
      * `displayValue (boolean)`: Whether to show the value in the report.
      * `enabled (boolean)`: Default true.
      * `formatter (String)`: Optional Java string formatter syntax (e.g., `%,.2f`).
  * `messages { ... }`
    * `success (String)`: Message to display when the rule passes.
    * `fail (String)`: Message to display when the rule fails.

### `groups`
* `group { ... }`
  * `id (String)`: Unique identifier.
  * `name (String)`: Display name.
  * `rules (List<String>)`: List of rule IDs to include.
  * `mode (String)`: `allPassed`, `firstPassed`, or `lastPassed`.
  * `failsTest (boolean)`: If group fails, test fails.
  * `enabled (boolean)`: Default true.

### `ratings`
* `rating { ... }`
  * `id (String)`: Rating name (e.g., 'A', 'Poor').
  * `value (double)`: Maximum percentage for this rating.
  * `failsTest (boolean)`: If this rating is achieved, test fails.
  * `enabled (boolean)`: Default true.
  * `active (boolean)`: Manually select this rating, bypassing point-based calculation. Default false.
