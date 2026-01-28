# Groovy Scorecard Configuration

The Groovy Scorecard feature allows you to define complex, dynamic scorecard configurations using a Groovy DSL. This is especially powerful when you need to dynamically adapt your scorecard based on the actual report data or external properties.

## Getting Started

To use a Groovy scorecard, create a file with the `.groovy` extension (e.g., `scorecard.groovy`) and point the XLT report generator to it.

A basic Groovy configuration looks like this:

```groovy
// We need rules to evaluate, each rule needs at least one check
// Each check needs a selector and a condition
builder.rules {
    rule {
        id 'rule1'
        name 'Max P95'
        points 10
        checks {
            check {
                // get us the max of all requests p95 values
                // we don't want to filter by name
                // the selector result must be comparable in the 
                // condition
                selector 'max(//requests/request/percentiles/p95)'
                condition '<= 500'
            }
        }
        messages {
            success "Runtimes within range"
            fail "Runtime exceeds 500 ms"
        }
    }
}

// We need groups to group rules together
// Each group needs at least one rule
builder.groups {
    group {
        id 'runtime'
        name 'Runtime KPIs'
        description 'Evaluate our runtime KPIs'
        rules(['rule1'])
    }
}

// We need ratings to rate the scorecard
// The ratings is based on a percentage of the total points
builder.ratings {
    rating { 
        id 'Fail'
        description 'We have not met all our criteria'
        value 99.99
        failsTest true
    }
    rating { 
        id 'Pass'
        description 'We have met all our criteria'
        value 100.0 
    }
}

// at the end, we have to return the builder
builder
```

## Core Concepts

### The `builder` Binding

Every Groovy scorecard script has access to a `builder` object (of type `ScorecardBuilder`). You use this object to define selectors, rules, groups, and ratings.

### Variable Reuse

Since it's a Groovy script, you can use variables to avoid repetition:

```groovy
def homepageXPath = '//requests/request[name="Homepage"]'
def threshold = 500

builder.rules {
    rule {
        id 'homepage_load'
        checks {
            check {
                selector "${homepageXPath}/performance"
                condition "< ${threshold}"
            }
        }
    }
}
```

### Accessing XLT Properties

You can access XLT properties using the `properties` binding:

```groovy
def limit = properties.getInt('scorecard.p95.limit', 1000)

builder.rules {
    rule {
        id 'dynamic_rule'
        checks {
            check {
                selector '//p95'
                condition "< ${limit}"
            }
        }
    }
}
```

### Fetching Live Data via XPath

One of the most powerful features is the `xpath` binding. It allows you to query the report XML *while* the configuration is being built. This is useful for dynamic group or rule generation.

```groovy
// Get a list of all request names from the report
def allRequests = xpath.getList('//requests/request/name')

builder.rules {
    allRequests.each { reqName ->
        rule {
            id "req_${reqName}"
            name "Performance of ${reqName}"
            checks {
                check {
                    selector "//requests/request[name='${reqName}']/p95"
                    condition "< 1000"
                }
            }
        }
    }
}

builder.groups {
    group {
        id 'all_requests'
        rules(allRequests.collect { "req_${it}" })
    }
}
```

Available `xpath` methods:

* `xpath.get(expression)`: Returns the string value of the first match.
* `xpath.getList(expression)`: Returns a List of string values for all matches.

### Value Formatting

You can optionally format the values displayed in the scorecard report using Java string formatter syntax.

```groovy
builder.rules {
    rule {
        id 'formatted_rule'
        checks {
            check {
                selector '//performance/value'
                condition '< 1000'
                formatter '%,.2f ms' // Formats 1234.5678 as 1,234.57 ms
            }
        }
    }
}
```

The evaluator automatically attempts to parse the value as a `Double` or `Long` before applying the formatter. If parsing fails, it persists as a `String`.

### Manual Results

In some cases, you may want to skip the evaluation (XPath and condition) for a rule and provide a result directly from your Groovy code. This can be achieved by setting the `status`, `value`, and optionally a `message` within a check:

```groovy
builder.rules {
    rule {
        id 'manual_result_rule'
        checks {
            check {
                status 'PASSED'     // SKIPPED, PASSED, FAILED, ERROR
                value 'Manually Set'
                message 'Optional message'
            }
        }
    }
}
```

When a `status` is provided, the `selector`, `condition` and `formatter` are ignored, and the check result is set exactly as specified.

### Manual Rating Selection

Instead of computing the rating automatically based on achieved points percentage, you can manually select a rating by marking it as `active`. When any rating has `active true`, the point-based calculation is bypassed entirely.

```groovy
builder.ratings {
    rating { id 'A'; value 100.0 }
    rating { id 'B'; value 80.0 }
    rating { id 'F'; value 0.0; active true; failsTest true }  // Always use this
}
```

> **Note**: If multiple ratings have `active true`, the first one in definition order is used.

## DSL Reference

### `selectors`

Defines reusable XPath selectors.

* `selector { ... }`
  * `id (String)`: Unique identifier.
  * `expression (String)`: The XPath expression.

### `rules`

Defines evaluation rules.

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
      * `condition (String)`: XPath condition (e.g., `< 500`, `exists`).
      * `displayValue (boolean)`: Whether to show the value in the report.
      * `enabled (boolean)`: Default true.
      * `formatter (String)`: Optional Java string formatter syntax (e.g., `%,.2f`).
  * `messages { ... }`
    * `success (String)`: Message to display when the rule passes.
    * `fail (String)`: Message to display when the rule fails.

### `groups`

Groups rules together for scoring.

* `group { ... }`
  * `id (String)`: Unique identifier.
  * `name (String)`: Display name.
  * `rules (List<String>)`: List of rule IDs to include.
  * `mode (String)`: `allPassed`, `firstPassed`, or `lastPassed`.
  * `failsTest (boolean)`: If group fails, test fails.
  * `enabled (boolean)`: Default true.

### `ratings`

Defines the overall scorecard rating thresholds.

* `rating { ... }`
  * `id (String)`: Rating name (e.g., 'A', 'Poor').
  * `value (double)`: Maximum percentage for this rating.
  * `failsTest (boolean)`: If this rating is achieved, test fails.
  * `enabled (boolean)`: Default true.
  * `active (boolean)`: Manually select this rating, bypassing point-based calculation. Default false.

## Advanced Example

```groovy
// Dynamic configuration based on report content
def criticalRequests = xpath.getList("//requests/request[importance='high']/name")
def defaultLimit = properties.getInt('scorecard.limit.default', 800)

builder.rules {
    criticalRequests.each { name ->
        rule {
            id "crit_${name}"
            name "Critical: ${name}"
            points 20
            failsTest true
            checks {
                check {
                    selector "//requests/request[name='${name}']/p95"
                    condition "< ${defaultLimit}"
                }
            }
        }
    }
}

builder.groups {
    group {
        id 'critical_kpis'
        name 'Critical KPIs'
        rules(criticalRequests.collect { "crit_${it}" })
        mode 'allPassed'
    }
}

builder.ratings {
    rating { id 'A'; value 100.0 }
    rating { id 'B'; value 90.0 }
    rating { id 'F'; value 50.0; failsTest true }
}
```

## Security

Groovy scripts run in a secured sandbox. Many standard Java classes (like `System`, `Runtime`, `File`) are blocked to prevent malicious scripts from affecting the system. Only whitelisted classes (like `java.util.*`, `java.text.*`) and project-specific DSL classes are allowed.
