## Why

Load test results contain rich statistical data (transactions, requests, errors, agents, events, web vitals, page load timings, custom values, etc.) that is currently only consumable via human-oriented HTML reports or raw XML (`testreport.xml`). Neither format is well-suited as input for AI-driven analysis — HTML is presentation-focused, and the XStream-serialized XML is verbose (up to 50% more tokens than efficient formats), schema-less, and causes "attention diffusion" in LLMs.

We want to package the same statistical data into a clean, token-efficient, AI-friendly container format that ships alongside the regular HTML report. This makes it trivial for LLMs to consume, compare, and reason about load test results — enabling automated performance regression analysis, anomaly detection, and natural-language summaries.

## What Changes

- **New output file** (`ai-summary.md`): A **YAML + Markdown hybrid** file generated during report creation, written into the report output directory. This format is chosen based on LLM token-efficiency research:
  - **YAML sections** for hierarchical metadata (test config, load profile) — highest reasoning accuracy for structured data
  - **Markdown tables** for flat KPI data (transactions, requests, actions) — 34-38% fewer tokens than JSON with positional fences for accurate value correlation
  - **Markdown KV lists** for error narratives and events — highest data extraction accuracy (~60.7% vs 44.3% for CSV)
  - **Zero new dependencies** — pure string building in Java, no Jackson/Gson needed
- **Data reduction**: Apply statistical distillation before writing:
  - Focus on steady-state data (ramp-up/ramp-down can be noted but not detailed)
  - Highlight SLA violations and errors prominently
  - Include all percentile breakdowns configured in the report
- **New navigation entry**: All report types (load, trend, diff) get a new "AI Data" menu link.
- **Data content sections**:
  - Test metadata (name, start/end time, duration, XLT version)
  - Transaction KPIs (count, rates, error %, min/max/mean/median/percentiles, Apdex)
  - Action KPIs (same statistical breakdown)
  - Request KPIs (same + DNS/connect/send/receive/TTFB, bytes sent/received)
  - Error details (test case, action, message, count, stack traces as KV lists)
  - Agent resource usage (CPU, memory)
  - Events overview
  - Page load timings and web vitals
  - Custom timers and custom values
  - Load profile configuration
  - Scorecard results (if present)
- **Configurable**: Enabled/disabled via property (enabled by default).

## Capabilities

### New Capabilities
- `ai-data-export`: Generating and writing the AI-friendly data file from report data, and integrating it into the report navigation across all report types (load, trend, diff).

### Modified Capabilities
_(none — this is purely additive)_

## Impact

- **Code**: `ReportGenerator.generateReport()` pipeline gains a new step after XML report creation. New class(es) to serialize data from the existing report model into the chosen output format.
- **Templates**: Navigation templates (`navigation.ftl` / `navigation.xsl`) for all 3 report types gain a new menu entry.
- **Dependencies**: Possibly a JSON library (Jackson or Gson) if JSON format is chosen — or no new dependency if Markdown is used.
- **APIs**: No external API changes. The new file is a static artifact in the report directory.
- **Breaking changes**: None. Purely additive feature.
