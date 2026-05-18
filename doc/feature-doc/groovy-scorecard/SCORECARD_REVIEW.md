# XLT Scorecard Feature — Multi-Persona Review

## Executive Summary

The XLT Scorecard is a **post-execution quality gate and grading system** for load test reports. It reads XLT's `testreport.xml` via XPath, evaluates user-defined rules against extracted metrics, and produces a scored verdict (Pass/Fail, letter grades, or custom ratings). Configuration is defined via a **Groovy DSL** (with legacy JSON/YAML support).

This review evaluates the feature from seven perspectives: Performance Engineer, DevOps/CI-CD Engineer, Developer Experience, Product Manager, AI Agent, Framework Architect, and XLT Developer.

---

## 1. Competitive Landscape

| Feature | **XLT Scorecard** | **Gatling** | **k6 (Grafana)** | **NeoLoad** | **JMeter** | **LoadRunner** | **Locust** |
|:---|:---|:---|:---|:---|:---|:---|:---|
| **Gate Type** | Post-run scoring | Post-run assertions | In-run + post-run thresholds | Pre-defined SLA profiles | Per-sample assertions | SLA wizard | DIY (Python hooks) |
| **Config Format** | Groovy DSL, YAML, JSON | Scala DSL (inline) | JS `options` object | YAML/GUI | GUI + JTL | GUI wizard | Python code |
| **Programmability** | ✅ Full Groovy (loops, variables, closures) | ✅ Scala | ✅ JavaScript | ⚠️ Limited YAML | ⚠️ JSR223 bolt-on | ❌ Wizard-only | ✅ Python |
| **Tiered Grading** | ✅ `firstPassed` mode (A+→F) | ❌ Binary pass/fail | ❌ Binary pass/fail | ⚠️ Warn + Fail only | ❌ Binary | ❌ Binary | ❌ Binary |
| **Weighted Scoring** | ✅ Points per rule, percentage rating | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Group Modes** | ✅ `allPassed`, `firstPassed`, `lastPassed` | ❌ Flat list | ❌ Flat list | ❌ Flat list | ❌ Flat list | ❌ Per-transaction SLAs | ❌ |
| **Helper API** | ✅ `metrics.*` (typed, composable) | ✅ Fluent DSL | ✅ Metric selectors | ⚠️ Fixed KPI names | ❌ Raw strings | ❌ GUI picks | ❌ Raw stats |
| **Abort on Fail** | ❌ Post-run only | ❌ Post-run only | ✅ `abortOnFail` | ✅ Real-time alerts | ❌ | ⚠️ Interval SLAs | ❌ |
| **CI/CD Exit Code** | ✅ `failsTest` → non-zero exit | ✅ Automatic | ✅ Automatic | ✅ Automatic | ⚠️ Requires parsing | ✅ SLA status | ⚠️ Manual `sys.exit()` |
| **HTML Report** | ✅ Embedded scorecard section | ✅ Assertion summary | ✅ Threshold summary | ✅ SLA tab | ❌ Requires plugins | ✅ Summary report | ❌ |
| **Regex Filtering** | ✅ XPath 2.0 `matches()` | ✅ Path-based `details()` | ⚠️ Tag-based filtering | ⚠️ Named elements only | ❌ | ⚠️ Transaction names | ❌ |
| **Exclusion Filtering** | ✅ `excludeName`, `excludeLabel` | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **HTTP Error Drill-down** | ✅ `httpErrorCount('5..')` regex | ❌ Global only | ⚠️ `http_req_failed` rate | ⚠️ Error rate only | ⚠️ Response assertions | ⚠️ Error count | ❌ |

---

## 2. Persona Reviews

### 🔧 Persona 1: Performance Engineer

**Overall Rating: ⭐⭐⭐⭐½ (4.5/5)**

**Strengths:**
- The **tiered grading system** (`firstPassed` mode with A+ → F) is genuinely unique in the load testing space. No competitor offers this. It transforms a binary "did we pass?" into a nuanced "how well did we perform?", which is exactly how performance budgets work in practice.
- The `excludeName` filter is a real power feature. Every project has that one slow checkout endpoint that you *know* is slow and don't want polluting your "all requests P95" gate. Being able to write `metrics.requestP95(excludeName: '^PlaceOrder')` instead of a 200-character XPath is excellent.
- The `httpErrorCount('5..')` regex approach is elegant. Being able to write `httpErrorCount('50[23]')` to catch only Bad Gateway and Service Unavailable is very precise.

**Concerns:**
- **No real-time abort.** k6's `abortOnFail` is a killer feature for long-running soak tests. If I'm 20 minutes into a 4-hour test and my error rate already blew past 5%, I want to stop immediately rather than waste 3h40m of agent time. This is a gap.
- **Missing: trend comparison.** I can check "is P95 < 500ms?" but I cannot check "is P95 within 10% of the previous run?" Gatling Enterprise and NeoLoad both support baseline comparison. This would be very valuable for regression detection.
- **`perHour()` wrapper feels manual.** The fact that I need to know the XPath for duration and manually wrap expressions feels like a workaround. Ideally, every count-based metric should have a native `PerHour` variant or a first-class `testDurationHours` variable.

**Recommendation:** Add `abortOnFail` support (even if it's a flag that the report generator checks mid-run) and consider a `baseline` comparison mode.

---

### 🔄 Persona 2: DevOps / CI-CD Engineer

**Overall Rating: ⭐⭐⭐⭐ (4/5)**

**Strengths:**
- The `failsTest true` flag on rules directly maps to a non-zero exit code. This is exactly what I need for Jenkins/GitLab CI pipeline integration. No post-processing scripts required.
- The Groovy DSL means I can use environment variables, conditionally enable rules, and parameterize thresholds — all things I cannot do with static YAML/JSON.
- The HTML report with embedded scorecard section means stakeholders can see results without CI access.

**Concerns:**
- **No structured machine-readable output.** After the scorecard evaluates, I need a JSON/JUnit XML artifact that my pipeline can parse for detailed failure reporting. The HTML report is for humans; I need something for my pipeline dashboard. JUnit XML would allow me to see individual rule failures as "test cases" in Jenkins.
- **No Slack/Teams webhook integration.** k6 Cloud and NeoLoad both support notification integrations. Having to parse exit codes and write custom notification scripts is friction.
- **Configuration discovery.** The property `com.xceptance.xlt.scorecard.config = scorecard-quality-gates.groovy` is simple, but I'd love a convention-over-configuration default (e.g., auto-discover `scorecard.groovy` in the config directory).

**Recommendation:** Emit a `scorecard.json` alongside `scorecard.xml` and `scorecard.html` with structured results. Consider JUnit XML format for native CI integration.

---

### 💻 Persona 3: Developer Experience (DX)

**Overall Rating: ⭐⭐⭐⭐⭐ (5/5)**

**Strengths:**
- The `metrics.*` helper API is the crown jewel. Compare the developer experience:

  ```groovy
  // XLT Scorecard (today)
  expression metrics.requestP95(excludeName: '^PlaceOrder')

  // What it would look like without the helper
  expression "max(//requests/request[not(matches(name, '^PlaceOrder'))]/percentiles/p95)"
  ```

  This is a **massive** DX win. The helper makes the scorecard readable by anyone, not just XPath specialists.

- The Groovy DSL with loops eliminates the #1 maintenance nightmare of static configs. The YAML version of the same scorecard was **847 lines**. The Groovy version is **205 lines** and covers more ground.
- Named parameters (`name:`, `excludeName:`, `label:`, `excludeLabel:`) read like natural language.
- The `formatter` property on checks (`'%,.0f tx/h'`) is a thoughtful touch for report readability.

**Concerns:**
- **IDE support.** There's no Groovy LSP completion for the DSL methods. The `metrics.*` methods won't autocomplete in IntelliJ unless the user adds the XLT JAR to their IDE classpath. A `.gdsl` descriptor file would fix this.
- **Error messages on typos.** If I write `metrics.requestP95(exludeName: '^PlaceOrder')` (typo in `excludeName`), the map-based API silently ignores the unknown key and throws "Must provide at least one parameter." The error should say which keys are valid.

**Recommendation:** Ship a `.gdsl` file for IntelliJ autocompletion and validate map keys to produce helpful error messages on typos.

---

### 📊 Persona 4: Product Manager / Stakeholder

**Overall Rating: ⭐⭐⭐⭐ (4/5)**

**Strengths:**
- **Unique market positioning.** No other open-source load testing tool offers weighted, tiered scoring. This is a genuine differentiator that can be marketed as "Performance Scorecards" — a concept business stakeholders immediately understand.
- The quality-gates example (`scorecard-quality-gates.groovy`) reads almost like a requirements document. A PM can look at it and understand every gate:
  ```
  ≥ 10,000 transactions/hour
  ≥ 500 orders
  < 0.001% error rate
  Zero 5xx errors
  P95 ≤ 500ms
  ```
- The letter-grade output (A+, A, B, C, D, F) maps directly to executive dashboards and stakeholder communication.

**Concerns:**
- **No visualization of trends over time.** A single scorecard is useful, but the real power would be showing "Run #42: B+, Run #43: A, Run #44: A+" in a trend chart. This would make the feature a first-class reporting tool rather than just a gate.
- **Two example files is confusing.** Having both `scorecard-config.groovy` (grading) and `scorecard-quality-gates.groovy` (pass/fail) without a clear "start here" guide may overwhelm new users.

**Recommendation:** Create a "Getting Started" section in the documentation that points users to the quality-gates example first (simpler mental model) and the grading example second (advanced).

---

### 🤖 Persona 5: AI Agent (Code Generation & Maintenance)

**Overall Rating: ⭐⭐⭐⭐⭐ (5/5)**

**Strengths:**
- **Highly structured DSL.** The `builder.selectors { ... }`, `builder.rules { ... }`, `builder.groups { ... }`, `builder.ratings { ... }` pattern is extremely predictable. An AI agent can generate scorecards from natural language requirements with high reliability because the structure is rigid.
- **`metrics.*` methods are self-documenting.** An AI doesn't need to know XPath internals to generate `metrics.requestP95(excludeName: '^PlaceOrder')`. The method name IS the documentation.
- **Composability.** The `perHour()` wrapper means I can compose arbitrary expressions without needing to understand the underlying XML structure. I can generate `metrics.perHour(metrics.httpErrorCount('5..'))` from the prompt "give me 5xx errors per hour."
- **Map-based parameters are LLM-friendly.** Named parameters like `name:`, `excludeName:`, `label:` are much easier for an LLM to generate correctly than positional arguments or XPath fragments.

**Concerns:**
- **No schema validation.** If I generate a scorecard with a typo in a `selectorId` reference (e.g., `'maxRequestP96'` instead of `'maxRequestP95'`), the error only surfaces at evaluation time. A pre-flight validation step would catch this immediately and allow me to self-correct.
- **No dry-run mode.** I'd love a `--validate-only` flag that parses and validates the Groovy config without needing a full `testreport.xml`, so I can verify generated configs before a test run.

**Recommendation:** Add a validation pass that checks all `selectorId` references resolve to defined selectors, and flag undefined ones with clear error messages.

---

### 🏗️ Persona 6: Framework Architect

**Overall Rating: ⭐⭐⭐⭐ (4/5)**

**Strengths:**
- **Clean separation of concerns.** The `MetricsHelper` generates XPath strings; the `Evaluator` executes them. This means the helper is purely functional and easily testable (as proven by the reflection-based test achieving 65% coverage).
- **Backward compatibility.** Supporting YAML, JSON, and Groovy simultaneously is well-architected. The Groovy DSL doesn't break existing users.
- **The `aggregateValue` engine** with its condition builder pattern is extensible. Adding new filter types (e.g., `minCount:`, `maxErrors:`) only requires adding a few lines to the condition builder.

**Concerns:**
- **XPath 2.0 dependency.** The entire metrics engine is built on XPath 2.0 (`matches()`, `replace()`). This couples the scorecard tightly to the XML report format. If XLT ever moves to a different report format (JSON, Parquet, database), the entire `MetricsHelper` needs rewriting.
- **No caching of XPath evaluations.** If a scorecard defines 50 selectors, each one triggers a fresh XPath evaluation against the full XML document. For very large reports (100k+ requests), this could be slow.
- **`escapeRegex` scope.** The helper escapes user regex for XPath safety, but it's unclear if all injection vectors are covered. A malicious or malformed regex in a Groovy config could potentially cause XPath injection.

**Recommendation:** Consider an abstraction layer between the `MetricsHelper` and the XML evaluation engine, so the helper generates query descriptors rather than raw XPath strings. This would make format migration feasible.

---

### 🧪 Persona 7: XLT Developer (Day-to-Day User)

**Overall Rating: ⭐⭐⭐⭐ (4/5)**

**Strengths:**
- **Immediate productivity.** As someone who already knows XLT's transaction/request naming conventions, the `metrics.*` API feels like it was built for me. I can write `metrics.transactionCount('^TOrder')` without thinking about XML structure. The cognitive load to create a basic scorecard is near zero.
- **Variables and constants at the top.** The pattern of defining `TX_ORDER`, `REQ_ORDER_SUBMIT`, and `P95_LIMIT` as variables at the top of the file is a huge win for maintainability. When a test suite renames transactions (e.g., `TOrder` → `TGuestOrder`), I change one line, not twenty.
- **The quality-gates example is copy-paste ready.** For 90% of my projects, I can take `scorecard-quality-gates.groovy`, change three regex patterns and two threshold numbers, and I'm done. That's exactly the right level of effort for a production scorecard.
- **`failsTest true` on every rule is the right default for gates.** In my experience, the most common use case is hard pass/fail, not grading. The quality-gates example models this perfectly.

**Concerns:**
- **Discovery of available `metrics.*` methods.** There's no quick-reference card. I know `requestP95()` exists, but does `requestP99_9()` exist? What about `transactionCountPerHour()`? I find myself reading `MetricsHelper.java` source code to discover what's available. A cheat sheet in `GROOVY_SCORECARD.md` listing every method signature would save me time.
- **The `selector` → `selectorId` indirection is verbose for simple cases.** For a quality-gate scorecard where every selector is used exactly once, having to define it in `selectors {}` and then reference it by ID in `rules {}` feels like unnecessary ceremony. An inline variant like `check { selector metrics.requestP95(...); isLessThan 500 }` would cut the file size in half for simple scorecards.
- **`globalCountPerHour('transactions')` — magic string.** The parameter `'transactions'` is a raw string that maps to an XML element name. If I misspell it as `'transaction'` (singular), I get a silent `NaN` or 0 instead of a helpful error. This has bitten me before with XPath-based APIs.
- **Where do I put the scorecard file?** The property-based activation (`com.xceptance.xlt.scorecard.config`) works, but the path resolution rules are unclear. Is it relative to the config directory? The project root? An absolute path? I've had colleagues waste time debugging "file not found" errors because of this ambiguity.
- **No way to test locally.** After writing a scorecard, I can only validate it by running a full report generation against a real `testreport.xml`. There's no lightweight `xlt-scorecard-check my-config.groovy sample-report.xml` CLI tool. For iterating on a config, I have to wait for the full report pipeline, which is slow.

**Wish List:**
1. **Method cheat sheet** in the documentation — every `metrics.*` method with signature and example output
2. **Inline selector shorthand** for simple one-shot rules
3. **Enum or constants** for `globalCountPerHour()` / `globalErrorPercentage()` type parameters instead of magic strings
4. **Standalone validation CLI** — parse and dry-run a scorecard against a sample report without the full XLT report pipeline
5. **Better error messages** — when a selector XPath returns `NaN` or empty, tell me which selector failed and what XPath was evaluated

---

## 3. Summary Verdict

| Persona | Rating | Key Insight |
|:---|:---|:---|
| **Performance Engineer** | ⭐⭐⭐⭐½ | Tiered grading is unique; needs trend comparison and abort-on-fail |
| **DevOps/CI-CD** | ⭐⭐⭐⭐ | `failsTest` is clean; needs structured JSON/JUnit output |
| **Developer Experience** | ⭐⭐⭐⭐⭐ | `metrics.*` API is best-in-class; IDE completion would perfect it |
| **Product Manager** | ⭐⭐⭐⭐ | Strong differentiator; needs trend visualization |
| **AI Agent** | ⭐⭐⭐⭐⭐ | Highly structured and composable; needs validation mode |
| **Framework Architect** | ⭐⭐⭐⭐ | Clean design; XPath coupling is a long-term risk |
| **XLT Developer** | ⭐⭐⭐⭐ | Copy-paste ready; needs cheat sheet, inline selectors, better errors |

### What XLT Does Better Than Everyone Else
1. **Weighted, tiered scoring** — no competitor has this
2. **Exclusion filters** (`excludeName`, `excludeLabel`) — no competitor has this
3. **Programmable config** with loops, variables, and closures — only Gatling (Scala) and k6 (JS) come close, but neither has the scoring model
4. **`metrics.*` helper API** — abstracts XPath completely; the cleanest helper API in the space

### What XLT Should Consider Adding
1. **Real-time abort** (`abortOnFail`) for long-running tests
2. **Baseline comparison** (regression detection against previous runs)
3. **JUnit XML / structured JSON output** for CI/CD dashboards
4. **IntelliJ `.gdsl` file** for IDE autocompletion
5. **Config validation mode** (`--validate-only`) for pre-flight checks
6. **Map key validation** in `MetricsHelper` to catch typos like `exludeName`
