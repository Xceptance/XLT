# Property Expansion with Groovy Expressions

XLT properties support dynamic value expansion using Groovy expressions. This allows you to calculate load mixes, define relationships between properties, and perform arithmetic in your configuration.

## Getting Started

In any properties file, you can use two expansion syntaxes:

* **`${property.name}`** - Standard variable substitution (references other properties)
* **`#{...}`** - Groovy expression evaluation (Spring-like syntax)

```properties
# Standard variable substitution
base.url = https://example.com
login.url = ${base.url}/login

# Groovy expression
totalUsers = 100
com.xceptance.xlt.loadtests.TBrowse.users = #{ (${totalUsers} * 0.4).intValue() }
```

## Evaluation Order

1. **`${...}`** variable references are resolved first
2. **`#{...}`** Groovy expressions are evaluated second

Since `${}` is resolved before Groovy evaluation, property values are pasted literally into the script text:

```properties
maxUsers = 100

# ${maxUsers} is replaced with the literal text 100 before Groovy sees it
browse.users = #{ (${maxUsers} * 0.4).intValue() }  # Results in: 40
```

> **Important — String Quoting**: Because `${...}` pastes the raw value directly into the Groovy script, **numeric values** work naturally (e.g. `${totalUsers}` becomes `100`), but **string values** must be explicitly quoted so that Groovy interprets them as strings rather than variable names:
>
> ```properties
> mode = production
>
> # CORRECT — quoted: Groovy sees 'production' (a string literal)
> users = #{ '${mode}' == 'production' ? 100 : 10 }
>
> # WRONG — unquoted: Groovy sees production (an undefined variable name) → error
> users = #{ ${mode} == 'production' ? 100 : 10 }
> ```

## Multi-Line Scripts

Groovy expressions can span multiple lines:

```properties
com.xceptance.xlt.loadtests.TBrowse.users = #{ \
    def total = ${totalUsers}; \
    def browse = (total * 0.4).intValue(); \
    browse \
}
```

> **Note**: Java properties do not support multi-line text directly. If you need to use multi-line scripts, you must use the `\` line ending to tell the properties file parser that the data continues on the next line.

## Practical Examples

### Load Mix Calculation

```properties
# Base configuration
totalUsers = 100

# Calculate user distribution
com.xceptance.xlt.loadtests.TBrowse.users = #{ (${totalUsers} * 0.40).intValue() }
com.xceptance.xlt.loadtests.TSearch.users = #{ (${totalUsers} * 0.30).intValue() }
com.xceptance.xlt.loadtests.TOrder.users = #{ (${totalUsers} * 0.10).intValue() }
com.xceptance.xlt.loadtests.TCheckout.users = #{ (${totalUsers} * 0.20).intValue() }
```

### Environment-Based Configuration

```properties
# Environment multiplier (override per environment)
load.multiplier = 1.0
base.browse.users = 50

# Scale by multiplier
com.xceptance.xlt.loadtests.TBrowse.users = #{ (${base.browse.users} * ${load.multiplier}).intValue() }
```

### Dynamic Arrival Rates

```properties
totalRequests = 10000
testDurationHours = 1.5

# Calculate arrival rate per hour
com.xceptance.xlt.loadtests.TSearch.arrivalRate = #{ \
    (${totalRequests} / ${testDurationHours}).intValue() \
}
```

### Conditional Configuration

```properties
mode = production

# Quote ${mode} so Groovy treats it as a string
users = #{ '${mode}' == 'production' ? 100 : 10 }

# String concatenation — also requires quoting
env.label = #{ "Environment: " + '${mode}' }
```

## Supported Operations

Groovy expressions support:

* **Arithmetic**: `+`, `-`, `*`, `/`, `%`
* **Type conversion**: `as int`, `as double`, `as String`
* **String operations**: concatenation, interpolation
* **Collections**: Lists, Maps
* **Closures**: Functional operations
* **Conditionals**: `if/else`, ternary operator

## Security

Groovy scripts run in a sandbox. The following operations are **blocked**:

* File system access (`File`, `FileReader`, etc.)
* Network operations (`URL`, `Socket`, etc.)
* System access (`System`, `Runtime`)
* Thread creation
* Reflection

Only safe imports are allowed: `java.util.*`, `java.math.*`, `java.text.*`

## Error Handling

Invalid Groovy expressions throw `IllegalArgumentException` with the script content and error message:

```
Failed to evaluate Groovy expression: #{invalid syntax} - ...
```

## Tips

1. **Use `.intValue()`** for integer results after multiplication/division (Groovy defaults to `BigDecimal` which otherwise evaluates to a float string like `40.0`).
2. **Quote string values** when using `${...}` inside Groovy — write `'${myProp}'` so Groovy sees a string literal, not a bare identifier.
3. **Use `${...}` for property access** — all property values must be referenced via `${...}` variable substitution, which is resolved before Groovy evaluation.
4. **Test expressions** — verify calculations produce expected values.
