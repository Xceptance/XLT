# Groovy Scorecard Configuration

The Groovy Scorecard feature allows you to define complex, dynamic scorecard configurations using a Groovy DSL. This is especially powerful when you need to dynamically adapt your scorecard based on the actual report data, external properties, or when you want to avoid writing verbose XML configurations.

## 1. Core Engine & Bindings

When your `.groovy` scorecard script executes, it runs inside a secured sandbox with several pre-injected "bindings" (variables) ready for you to use:

* `builder`: The core `ScorecardBuilder` used to configure schema version, selectors, rules, groups, and ratings.
* `xpathBuilder`: A helper object that generates complex XPath strings for standard report metrics automatically.
* `properties`: Access to your XLT test properties (`ScorecardProperties`):
  * `properties.get(key)`: Returns the property value as a string, or `null` if not found.
  * `properties.get(key, defaultValue)`: Returns the property value, or the default string value.
  * `properties.getBoolean(key, defaultValue)`: Returns the boolean property value.
  * `properties.getInt(key, defaultValue)`: Returns the int property value.
  * `properties.getLong(key, defaultValue)`: Returns the long property value.
* `data`: A query helper to inspect live `testreport.xml` data during script execution (`ScorecardData`):
  * `data.get(xpath)`: Executes the XPath expression and returns the string value of the first matching node, or `null`.
  * `data.getList(xpath)`: Executes the XPath expression and returns a `List<String>` of all matching values.
  * `data.getDocument()`: Returns the underlying Saxon `XdmNode` of the XML report document.
* `log`: A logger for recording diagnostic messages during script execution (`ScorecardLogger`):
  * `log.info(message)`, `log.warn(message)`, `log.error(message)`
  * `log.error(throwable)`, `log.error(message, throwable)`
  * Recorded logs are stored in `scorecard.xml` under `<result><logs>` and displayed in the report output.

The engine injects `builder` into the script binding and calls `builder.build()` automatically when script execution finishes. Explicitly writing `return builder` at the end of the script is optional.

---

## 2. A Basic Example

To get started, create a file with the `.groovy` extension (e.g., `scorecard.groovy`) inside your test suite's `config/` directory (e.g., `<testsuite>/config/scorecard.groovy`). Then point the XLT report generator to it by configuring the following property in your `project.properties` (or any other file in the XLT property hierarchy):

```properties
com.xceptance.xlt.scorecard.config = scorecard.groovy
```

> [!NOTE]
> During report generation, XLT copies configuration files from the test suite's `config/` directory into `<reportDir>/config/` and resolves `com.xceptance.xlt.scorecard.config` relative to that directory.

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
                // Use the xpathBuilder helper to get the P95 for the Homepage
                selector xpathBuilder.requestP95('^Homepage')
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
                selector xpathBuilder.agentCpuMax()
                isLessThan 90
            }
        }
    }
}
```

**Condition Evaluation:** To evaluate the selected value, you can use fluent assertion methods or specify a raw XPath condition directly using `condition(expression)`:
* **Fluent assertions:** `isLessThan(value)`, `isLessThanOrEqualTo(value)`, `isGreaterThan(value)`, `isGreaterThanOrEqualTo(value)`, `isEqualTo(value)`, `isNotEqualTo(value)`, or `matchesRegex(pattern)`.
* **Raw condition:** `condition "<= 500"`, `condition "= 0"`, or `condition "exists(.)"`. If an expression begins with a comparison operator (`=`, `!=`, `<`, `<=`, `>`, `>=`), the current value context (`.`) is prepended automatically.

**The `xpathBuilder` Helper:** Writing raw XPath is tedious. The `xpathBuilder` object abstracts this away using named parameters:
* `xpathBuilder.requestP95('^Homepage')` (legacy shorthand: matches by regex against the name)
* `xpathBuilder.requestP95(name: '^Homepage')` (explicit regex match against name)

**Label Selection:**
If your test run makes use of labels to group requests, you can use the built-in named parameters to match by label instead of by name regex:
*   `requestP95(label: 'homepage')`
This performs an exact match on the label property rather than a regex over the name.

**Excluding Elements:**
You can compute metrics across all elements *except* those matching a specific regex by using the `excludeName` or `excludeLabel` parameters. This is useful for finding worst-case metrics excluding known outliers:
*   `xpathBuilder.requestP95(excludeName: '^OrderSubmit')`

You can freely combine these constraints:
*   `xpathBuilder.requestP95(name: '^Homepage', excludeLabel: 'cached')`

Similarly, these helpers are available for **transactions**, **actions** and **customTimers**:
*   `transactionP95(name: '...Regex')`, `transactionP95(label: '...Label')`, `transactionCount(label: '...')`, `actionErrorPercentage(label: '...')`, etc. (See [Section 8](#8-xpathbuilder-reference-cheat-sheet) for the complete reference).

**Global Summaries & Errors:**
You can retrieve pre-computed global totals and specialized error metrics directly:
*   `xpathBuilder.globalCountPerHour('transactions')` (or 'requests', 'actions')
*   `xpathBuilder.globalErrorPercentage('requests')`
*   `xpathBuilder.httpErrorCount('5..')` (Counts all HTTP 500–599 errors by analyzing `//responseCodes/responseCode`)
*   `xpathBuilder.httpErrorCount('404|400')` (Counts specific HTTP errors)

**Per-Hour Calculation Wrapper:**
If you need a per-hour rate for an absolute metric (like HTTP errors), wrap it with `xpathBuilder.perHour(...)`:
*   `xpathBuilder.perHour(xpathBuilder.httpErrorCount('5..'))` (Yields the number of 5xx errors per hour of test duration)

**Agent Helpers:**
You can retrieve CPU and error metrics for the load generator agents:
*   `xpathBuilder.agentCpuMax()` (XPath expression for the maximum CPU usage across all agents)
*   `xpathBuilder.agentCountCpuMeanAbove(threshold)` (XPath expression counting how many agents had a mean CPU usage above the threshold)
*   `xpathBuilder.agentCount()` (XPath expression counting the total number of agents used)
*   `xpathBuilder.agentCountWithErrors()` (XPath expression counting how many agents had transaction errors)
*   `xpathBuilder.agentTransactionErrorsTotal()` (XPath expression for the pre-computed total of transaction errors across all agents)

### 3.2 Groups
Groups logically organize your rules. A group calculates its achieved points based on its `mode`:
* `allPassed`: The standard mode. The group's achieved points are the sum of all passed rules. The group's total achievable points are the sum of all rules.
* `firstPassed` / `lastPassed`: Used for tiered grading (e.g., trying to assign an A, B, or C grade to a specific page). The group evaluates rules in order and takes the points from the *first* (or *last*) rule that passes. The group's total achievable points are set to the *maximum* points of any single rule in the group.

Groups also support optional `description`, `failsTest`, `failsOn` (`NOTPASSED` or `PASSED`), and custom `messages`:

```groovy
builder.groups {
    group {
        id 'system_health'
        name 'System Health'
        description 'Checks agent resource utilization and stability'
        mode 'allPassed'
        failsTest true
        failsOn 'NOTPASSED' // NOTPASSED (default) or PASSED
        rules(['cpu_check', 'memory_check'])
        messages {
            success 'System health checks passed'
            fail 'System health thresholds exceeded'
        }
    }
}
```

### 3.3 Ratings
Ratings determine the final outcome of the load test by summing up all achieved points from all groups and dividing by the total achievable points to calculate a percentage.

The engine processes your ratings in the exact order they are defined. The **first rating** whose `value` is greater than or equal to the achieved percentage is selected as the final rating. This means `value` acts as the **upper limit**.

> [!IMPORTANT]
> Because ratings are evaluated in definition order against their upper-limit thresholds, **ratings must be defined in ascending order of their `value`** (lowest percentage to highest percentage). If defined in descending order, the highest threshold (e.g., 100%) will match all scores and shadow all subsequent ratings.

```groovy
builder.ratings {
    rating { id 'Poor'; value 50.0; failsTest true } // 0.0% to 50.0%
    rating { id 'Ok'; value 95.0 }                   // 50.1% to 95.0%
    rating { id 'A'; value 100.0 }                   // 95.1% to 100.0%
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
        expression xpathBuilder.requestP95('^Homepage')
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
Because this is Groovy, you can use loops and live data to generate rules dynamically. The `data` binding allows you to query the `testreport.xml` while the configuration is being built!

```groovy
// Read a property from project.properties
def defaultLimit = properties.getInt('scorecard.limit.default', 800)

// Get a list of all request names that actually occurred in this specific test
def allRequests = data.getList('//requests/request/name')

builder.rules {
    allRequests.each { reqName ->
        rule {
            id "req_${reqName}"
            checks {
                check {
                    selector xpathBuilder.requestP95(reqName)
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
    selector xpathBuilder.requestMean('^Homepage')
    isLessThan 1000
    formatter '%,.2f ms' // Formats 1234.5678 as 1,234.57 ms in the report
}
```

### 4.4 Manual Results & Ratings
If you want to bypass the point-based evaluation entirely, you can force a check to a specific status, or force the entire scorecard to a specific rating.

**Manual Check Status:**
```groovy
check {
    status 'FAILED'     // PASSED, FAILED, ERROR, SKIPPED
    value 'Manually Set'
    message 'Custom failure reason shown in report'
}
```

**Manual Rating Selection:**

You can manually force a rating using `forced true`. When a rating is marked as forced, it will be selected regardless of the achieved points percentage (if multiple ratings are forced, the first one in definition order wins). This is especially useful for conditionally overriding the scorecard outcome when infrastructure limits are exceeded (for example, if the load generator agents were overloaded, rendering the performance measurements unreliable):

```groovy
// Retrieve CPU utilization from agent statistics using XPath
def maxCpuStr = data.get("max(//agents/agent/totalCpuUsage/max)")
def maxCpu = maxCpuStr ? Double.parseDouble(maxCpuStr) : 0.0

// Conditionally force the rating to 'F' if max CPU exceeded 90%
def isOverloaded = maxCpu > 90.0

builder.ratings {
    if (isOverloaded) {
        rating { id 'F'; forced true; failsTest true }
    }
    rating { id 'Poor'; value 50.0; failsTest true }
    rating { id 'Ok'; value 95.0 }
    rating { id 'A'; value 100.0 }
}
```

### 4.5 Debugging with the Log Binding
If your script is doing complex logic and you need to see what's happening, use the `log` binding.

```groovy
log.info("Generating rules for ${allRequests.size()} requests...")
if (allRequests.isEmpty()) {
    log.warn("No requests found matching criteria")
}
```
Supported methods include `log.info(String)`, `log.warn(String)`, `log.error(String)`, `log.error(Throwable)`, and `log.error(String, Throwable)`. These logs are recorded in the scorecard results (`scorecard.xml` under `<result><logs>`) and displayed in the generated HTML report.

---

## 5. Security Sandbox

Groovy scripts run inside a secured compilation sandbox (`GroovySecurityUtils`). Standard Java classes for file I/O, process execution, and network access are blocked to prevent malicious operations.

* **Allowed imports:** Star imports from `java.util.*`, `java.math.*`, and `java.text.*`.
* **Blocked constructors:** `java.io.*`, `java.nio.*`, `java.net.*`, `groovy.lang.*`, `org.codehaus.groovy.*`, `Runtime`, `ProcessBuilder`, `Thread`, and `ClassLoader`.
* **Blocked receiver classes:** Method calls on `System`, `Runtime`, `ProcessBuilder`, `Thread`, `ClassLoader`, `Class`, `File`, `FileReader`, `FileWriter`, `FileInputStream`, `FileOutputStream`, `RandomAccessFile`, `URL`, `URI`, `Socket`, `ServerSocket`, `HttpURLConnection`.
* **Blocked runtime escapes:** Invoking dynamic `.execute()` (process execution) on strings, `.getClass()`, and `.class` property access (reflection).
* **Allowed capabilities:** Full Groovy closures, collection manipulation, loops, arithmetic, string interpolation, and the injected binding APIs (`builder`, `xpathBuilder`, `properties`, `data`, `log`).

---

## 6. Complete Advanced Example

This example demonstrates how to dynamically fetch critical requests from the report, apply a configurable threshold, and group them securely.

```groovy
// 1. Fetch live data and properties
def criticalRequests = data.getList("//requests/request[importance='high']/name")
def defaultLimit = properties.getInt('scorecard.limit.default', 800)

log.info("Found ${criticalRequests.size()} critical requests to evaluate.")

// 2. Dynamically build rules
builder.rules {
    criticalRequests.each { reqName ->
        rule {
            id "crit_${reqName}"
            name "Critical: ${reqName}"
            description "P95 response time validation for ${reqName}"
            points 20
            failsTest true
            failsOn 'NOTPASSED'
            checks {
                check {
                    selector xpathBuilder.requestP95(reqName)
                    isLessThan defaultLimit
                    formatter '%,.2f ms'
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
        description 'Performance budget checks for all critical endpoints'
        rules(criticalRequests.collect { "crit_${it}" })
        mode 'allPassed'
        failsTest true
        failsOn 'NOTPASSED'
        messages {
            success 'All critical endpoints met their response time SLA'
            fail 'One or more critical endpoints violated SLA'
        }
    }
}

// 4. Set Ratings (defined in ascending threshold order)
builder.ratings {
    rating { id 'F'; name 'Failed'; value 50.0; failsTest true }
    rating { id 'B'; name 'Acceptable'; value 90.0 }
    rating { id 'A'; name 'Excellent'; value 100.0 }
}
```

---

## 7. DSL Reference

### `builder`
* `version(int)`: Sets configuration schema version (default `1`).

### `selectors`
* `selector { ... }`
  * `id (String)`: Unique identifier used in `selectorId` references.
  * `expression (String)`: The XPath expression (e.g., `xpathBuilder.requestP95('...')`).
  * `comment (String)`: Optional documentation comment.

### `rules`
* `rule { ... }`
  * `id (String)`: Unique identifier.
  * `name (String)`: Display name.
  * `description (String)`: Optional explanation of what the rule validates.
  * `enabled (boolean)`: Default is `true`.
  * `points (int)`: Achievable points when the rule passes (default `0`).
  * `failsTest (boolean)`: Whether this rule can cause the overall test to fail (default `false`).
  * `failsOn (String)`: Trigger condition: `'NOTPASSED'` (default, fails test if rule does not pass) or `'PASSED'` (fails test if rule passes).
  * `negateResult (boolean)`: Inverts the outcome (passed becomes failed, failed becomes passed). Default `false`.
  * `checks { ... }`
    * `check { ... }`
      * `selector (String)`: Inline XPath expression.
      * `selectorId (String)`: Reference to a predefined selector ID.
      * `condition (String)`: Direct XPath condition expression (e.g., `'<= 500'`, `'= 0'`).
      * Fluent Assertions:
        * `isLessThan(value)`
        * `isLessThanOrEqualTo(value)`
        * `isGreaterThan(value)`
        * `isGreaterThanOrEqualTo(value)`
        * `isEqualTo(value)`
        * `isNotEqualTo(value)`
        * `matchesRegex(String)`
      * `displayValue (boolean)`: Whether to show the value in the report (default `true`).
      * `enabled (boolean)`: Default `true`.
      * `formatter (String)`: Optional `java.util.Formatter` pattern (e.g., `'%,.2f ms'`), evaluated with `Locale.US`.
      * Manual results:
        * `status (String)`: Bypass evaluation and set `'PASSED'`, `'FAILED'`, `'ERROR'`, or `'SKIPPED'`.
        * `value (String)`: Display value when using manual status.
        * `message (String)`: Error message when using manual `'FAILED'` or `'ERROR'`.
  * `messages { ... }`
    * `success (String)`: Message to display when the rule passes.
    * `fail (String)`: Message to display when the rule fails.

### `groups`
* `group { ... }`
  * `id (String)`: Unique identifier.
  * `name (String)`: Display name.
  * `description (String)`: Optional description.
  * `rules (List<String>)`: List of rule IDs to include (evaluated in order).
  * `mode (String)`:
    * `'allPassed'` (default): Group points = sum of passed rules; total achievable points = sum of all rules.
    * `'firstPassed'`: Group points = points of the first passing rule; total achievable points = max points of any rule in the group.
    * `'lastPassed'`: Group points = points of the last passing rule; total achievable points = max points of any rule in the group.
  * `failsTest (boolean)`: If group triggers failure, overall test fails (default `false`).
  * `failsOn (String)`: `'NOTPASSED'` (default) or `'PASSED'`.
  * `enabled (boolean)`: Default `true`.
  * `messages { ... }`
    * `success (String)`: Message when group passes.
    * `fail (String)`: Message when group fails.

### `ratings`
* `rating { ... }`
  * `id (String)`: Rating identifier (e.g., `'A'`, `'F'`).
  * `name (String)`: Optional human-readable name (e.g., `'Excellent'`).
  * `description (String)`: Optional description.
  * `value (double)`: Maximum score percentage for this rating (0.0 to 100.0). Evaluated in ascending order.
  * `failsTest (boolean)`: If this rating is assigned, mark the overall test as failed (default `false`).
  * `enabled (boolean)`: Default `true`.
  * `forced (boolean)`: Manually forces this rating, bypassing point-based calculation (default `false`).

---

## 8. xpathBuilder Reference Cheat Sheet

Every metric helper in `xpathBuilder` supports two call forms:
1. **Positional regex:** `xpathBuilder.requestP95('^Homepage')` (matches by regex on the element `name`).
2. **Named parameters:** `xpathBuilder.requestP95(name: '^Homepage', excludeName: '^Order', label: 'critical', excludeLabel: 'cached')`.
   * `name`: Matches regex against the element's `name`.
   * `excludeName`: Rejects elements whose `name` matches the regex.
   * `label`: Exact match on the element's `labels`.
   * `excludeLabel`: Rejects elements whose `labels` equals the string.

### Requests, Transactions & Actions

| Metric | Requests | Transactions | Actions | Description |
|:---|:---|:---|:---|:---|
| **P50 (Median)** | `requestP50(...)` | `transactionP50(...)` | `actionP50(...)` | 50th percentile response time |
| **P95** | `requestP95(...)` | `transactionP95(...)` | `actionP95(...)` | 95th percentile response time |
| **P99** | `requestP99(...)` | `transactionP99(...)` | `actionP99(...)` | 99th percentile response time |
| **P99.9** | `requestP99_9(...)` | `transactionP99_9(...)` | `actionP99_9(...)` | 99.9th percentile response time |
| **Mean** | `requestMean(...)` | `transactionMean(...)` | `actionMean(...)` | Mean response time |
| **Median** | `requestMedian(...)` | `transactionMedian(...)` | `actionMedian(...)` | Median response time |
| **Min** | `requestMin(...)` | `transactionMin(...)` | `actionMin(...)` | Minimum response time |
| **Max** | `requestMax(...)` | `transactionMax(...)` | `actionMax(...)` | Maximum response time |
| **Errors** | `requestErrors(...)` | `transactionErrors(...)` | `actionErrors(...)` | Error count |
| **Error %** | `requestErrorPercentage(...)` | `transactionErrorPercentage(...)` | `actionErrorPercentage(...)` | Error rate percentage |
| **Count** | `requestCount(...)` | `transactionCount(...)` | `actionCount(...)` | Total execution count |
| **Arbitrary Metric** | `requestValue(..., path)` | `transactionValue(..., path)` | `actionValue(..., path)` | Relative XPath path (e.g. `'bytesSent/mean'`) |

### Custom Timers

| Method | Parameters | Description |
|:---|:---|:---|
| `customTimerP50(...)` | regex or map | 50th percentile runtime |
| `customTimerP95(...)` | regex or map | 95th percentile runtime |
| `customTimerP99(...)` | regex or map | 99th percentile runtime |
| `customTimerMean(...)` | regex or map | Mean runtime |
| `customTimerMax(...)` | regex or map | Maximum runtime |
| `customTimerValue(..., path)` | regex or map, path | Custom sub-metric |

### Global Summaries, HTTP Errors & Rate Calculations

| Method | Parameters | Description |
|:---|:---|:---|
| `globalCountPerHour(type)` | `'transactions'`, `'requests'`, `'actions'` | Pre-computed hourly rate from report summary |
| `globalErrorPercentage(type)` | `'transactions'`, `'requests'`, `'actions'` | Global summary error percentage |
| `httpErrorCount(regex)` | Regex string (e.g., `'5..'`, `'404\|400'`) | Sum of counts for matching HTTP status codes |
| `perHour(metricExpression)` | XPath expression string | Divides metric by test duration in hours (`/testreport/general/duration div 3600`) |

### Agent & Resource Utilization

| Method | Parameters | Description |
|:---|:---|:---|
| `agentCpuMax()` | *(none)* | Maximum CPU usage percentage across all agents |
| `agentCountCpuMeanAbove(threshold)` | `Number` threshold | Count of agents whose mean CPU usage exceeded the threshold |
| `agentCount()` | *(none)* | Total number of agents utilized in the test |
| `agentCountWithErrors()` | *(none)* | Count of agents that encountered transaction errors |
| `agentTransactionErrorsTotal()` | *(none)* | Pre-computed total transaction errors across all agents |

