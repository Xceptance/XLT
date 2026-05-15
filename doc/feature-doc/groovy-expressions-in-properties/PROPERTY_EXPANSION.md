# Property Expansion with Groovy Expressions

XLT properties support dynamic value expansion using Groovy expressions. This allows you to calculate load mixes, define relationships between properties, and share computed values across your configuration.

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
com.xceptance.xlt.loadtests.TBrowse.users = #{ ((props['totalUsers'] as int) * 0.4).intValue() }
```

## Evaluation Order

1. **`${...}`** variable references are resolved first
2. **`#{...}`** Groovy expressions are evaluated second

Since `${}` is resolved before Groovy evaluation, you can use property values directly without needing `props`:

```properties
maxUsers = 100

# Preferred: use ${} for simple property access
browse.users = #{ (${maxUsers} * 0.4).intValue() }  # Results in: 40

# Equivalent but more verbose
browse.users = #{ ((props['maxUsers'] as int) * 0.4).intValue() }
```

> **Tip**: Use `${}` when you just need the property value. Use `props` only when you need dynamic key lookup or default values.

## Available Bindings

Groovy expressions have access to two variables:

### `props` - Property Access (When Needed)

Use `props` when you need dynamic key lookup or default values:

```properties
# Dynamic key lookup
env = production
value = #{ props.getProperty("limit.${props['env']}", '100') as int }

# Default values
orderUsers = #{ props.getProperty('order.users', '10') as int }
```

For simple property access, prefer `${}` syntax instead:

```properties
totalUsers = 100

# Preferred
browseUsers = #{ (${totalUsers} * 0.4).intValue() }

# Also works, but more verbose
browseUsers = #{ ((props['totalUsers'] as int) * 0.4).intValue() }
```

## Multi-Line Scripts

Groovy expressions can span multiple lines:

```properties
com.xceptance.xlt.loadtests.config = #{ \
    def total = props['totalUsers'] as int; \
    def browse = (total * 0.4).intValue(); \
    def order = (total * 0.1).intValue(); \
    \
    ctx['totalUsers'] = total; \
    ctx['browseUsers'] = browse; \
    ctx['orderUsers'] = order; \
    \
    'configured' \
}

com.xceptance.xlt.loadtests.TBrowse.users = #{ ctx['browseUsers'] }
com.xceptance.xlt.loadtests.TOrder.users = #{ ctx['orderUsers'] }
```

> **Note**: Java properties do not support multi-line text directly. If you need to use multi-line scripts, you must use the `\` line ending to tell the properties file parser that the data continues on the next line.

### `ctx` - Shared Context Map

Store and share values between expressions:

```properties
# Store computed values
init = #{ \
    ctx['base'] = props['totalUsers'] as int; \
    ctx['loaded'] = true; \
    'initialized' \
}

# Use stored values later
browse.users = #{ (ctx['base'] * 0.4).intValue() }
order.users = #{ (ctx['base'] * 0.1).intValue() }
```

#### Thread Safety

The `ctx` object is backed by a thread-safe `ConcurrentHashMap`. This ensures that under high concurrency (e.g., multiple threads or virtual users evaluating properties simultaneously), structural modifications like `ctx['base'] = 100` are completely safe and will not corrupt the map.

Additionally, the `props` map is exposed via a read-only wrapper, preventing scripts from inadvertently mutating the underlying properties tree.

> **Warning**: While the `ctx` map itself is thread-safe, complex state manipulations inside your script are **not** automatically atomic. For instance, executing `ctx['counter'] = (ctx['counter'] ?: 0) + 1` across multiple concurrent threads can lead to lost updates due to race conditions. Stick to idempotent configuration logic or ensure you account for concurrency if implementing advanced shared state.

## Practical Examples

### Load Mix Calculation

```properties
# Base configuration
totalUsers = 100

# Calculate user distribution using ${} (preferred)
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

# Scale by multiplier using ${}
com.xceptance.xlt.loadtests.TBrowse.users = #{ (${base.browse.users} * ${load.multiplier}).intValue() }
```

### Dynamic Arrival Rates

```properties
totalRequests = 10000
testDurationHours = 1.5

# Calculate arrival rate per hour
com.xceptance.xlt.loadtests.TSearch.arrivalRate = #{ \
    (props['totalRequests'] as int / props['testDurationHours'] as double).intValue() \
}
```

## Supported Operations

Groovy expressions support:

* **Arithmetic**: `+`, `-`, `*`, `/`, `%`
* **Type conversion**: `as int`, `as double`, `as String`
* **String operations**: concatenation, interpolation
* **Collections**: Lists, Maps
* **Closures**: Functional operations
* **Conditionals**: `if/else`, ternary operator

```properties
# Conditionals
mode = production
users = #{ props['mode'] == 'production' ? 100 : 10 }

# String operations
env.label = #{ "Environment: ${props['mode']}" }
```

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
2. **Store complex calculations** in `ctx` to avoid repetition
3. **Initialize early** - put setup expressions in properties that load first
4. **Test expressions** - verify calculations produce expected values
