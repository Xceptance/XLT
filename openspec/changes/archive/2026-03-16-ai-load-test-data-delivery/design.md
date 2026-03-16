## Context

XLT generates three types of reports, each with a different data pipeline:

1. **Load report**: Raw CSV → `DataProcessor` → ~20 `ReportProvider` implementations → `XmlReportGenerator` (in-memory `TestReport` → XStream → `testreport.xml`) → XSLT/FreeMarker HTML
2. **Trend report**: Reads multiple existing `testreport.xml` files → DOM parsing → builds `TrendValue` aggregates → own XML DOM → XSLT/FreeMarker HTML
3. **Diff report**: Reads two existing `testreport.xml` files → DOM parsing → element-level diffs → own XML DOM → XSLT/FreeMarker HTML

The `TestReport` in-memory object only exists during load report generation. Trend and diff reports operate on XML files from previously generated load reports.

**Branch**: Work will be done on `features/ai-load-test-data-delivery`, branched from the current main integration point.

## Goals / Non-Goals

**Goals:**
- Generate an `ai-summary.md` file in YAML+Markdown hybrid format in the load report output directory
- Add an "AI Data" navigation link in all report types where the file is present
- Use the in-memory `TestReport` object for load reports (Option C — no XML round-trip)
- Make the feature configurable (enabled by default)
- Support all data sections: test metadata, transactions, actions, requests, errors, agents, events, page load timings, web vitals, custom timers/values, load profile, scorecard

**Non-Goals:**
- AI summary for trend reports (different data model — deferred to future iteration)
- AI summary for diff reports (different data model — deferred to future iteration)
- Any AI analysis or LLM integration — this is purely data packaging
- Token counting or context-window optimization beyond format choice

## Decisions

### 1. Data Source: In-Memory TestReport (Option C)

**Decision**: Read from the in-memory `TestReport` object after all `ReportProvider` fragments have been collected, but before/alongside XML serialization.

**Why not XML parsing?** XStream XML has no schema, is class-name-dependent, and parsing it back would be double work (serialize then deserialize). The `TestReport.getReportFragments()` list already contains all typed Java objects.

**Why not per-provider contribution?** Would touch 20+ provider classes and create tight coupling. Every future provider change would need an AI output update.

**Implementation**: Modify `XmlReportGenerator.createReport()` to return both the `File` and the `TestReport`, or add the AI export step inside `createReport()` after fragment collection. A new `AiSummaryWriter` class receives the `TestReport` and iterates its fragments via `instanceof` checks.

**Alternatives considered**: Reading `testreport.xml` after generation (fragile, double work); adding an `AiContributor` interface to each `ReportProvider` (too invasive).

### 2. Output Format: YAML + Markdown Hybrid

**Decision**: Single `.md` file using YAML frontmatter/sections for hierarchical data and Markdown tables for flat KPI data.

**Why?** Based on LLM token-efficiency research:
- YAML: highest reasoning accuracy for structured/hierarchical data, uses indentation instead of closing tags
- Markdown tables: 34-38% fewer tokens than JSON, positional fences help LLMs correlate headers to values
- Markdown KV lists: ~60.7% accuracy vs 44.3% for CSV in data extraction tasks

**Why not JSON?** More tokens, would require a library dependency (or hand-rolled serialization that's equivalent effort to YAML string building).

**Implementation**: Pure `StringBuilder`/`PrintWriter` string building — zero new dependencies.

### 3. Fragment Type Mapping

Each fragment in `TestReport.getReportFragments()` has a concrete type. The `AiSummaryWriter` maps them:

| Fragment Type | AI Summary Section | Format |
|---|---|---|
| `GeneralReport` | Test Metadata | YAML |
| `ConfigurationReport` | Load Profile & Settings | YAML |
| `TransactionsReport` | Transaction KPIs | Markdown table |
| `ActionsReport` | Action KPIs | Markdown table |
| `RequestsReport` | Request KPIs | Markdown table |
| `ErrorsReport` | Error Details | Markdown KV list |
| `EventsReport` | Events | Markdown table |
| `AgentsReport` | Agent Resources | Markdown table |
| `PageLoadTimingsReport` | Page Load Timings | Markdown table |
| `WebVitalsReports` | Web Vitals | Markdown table |
| `CustomTimersReport` | Custom Timers | Markdown table |
| `CustomValuesReportProvider` | Custom Values | Markdown table |
| `SummaryReport` | Performance Summary | YAML + table |
| Scorecard (if present) | Scorecard Results | YAML |

Unknown fragment types are silently skipped — forward-compatible with new providers.

### 4. Scope: Load Reports Only (Initially)

**Decision**: Only load reports get AI summary generation in the first iteration.

**Why?** Trend and diff reports have fundamentally different data models:
- Trend report: `TrendValue` aggregates across multiple reports — no `TestReport` object
- Diff report: Element-level diffs between two reports — no `TestReport` object

Adding AI summaries for these would require either:
- Parsing their XML DOMs (different approach from Option C)
- Or restructuring their pipelines (too invasive)

Both can be added in future iterations. The navigation link will only appear when `ai-summary.md` exists in the report directory.

### 5. Navigation Integration

**Decision**: Add the "AI Data" link conditionally in navigation templates, similar to how "Scorecard" and "Log" links work.

**Implementation**: Pass an `aiSummaryPresent` boolean parameter to the renderer, check for file existence. Update FreeMarker (`navigation.ftl`) and XSLT (`navigation.xsl`) templates for all report types. The link only renders when the file exists.

### 6. Configuration Property

**Decision**: `com.xceptance.xlt.reportgenerator.aiSummary.enabled = true` (default: enabled)

Follows the existing property naming convention (e.g., `com.xceptance.xlt.reportgenerator.reportLogging.level`).

## Risks / Trade-offs

- **Fragment list is `List<Object>`** → Requires `instanceof` checks for each fragment type. If a fragment class is renamed or restructured, the `AiSummaryWriter` will silently skip it. Mitigated by: unit tests that verify all expected sections are present.

- **Format is not machine-parseable** → Unlike JSON, the YAML+Markdown hybrid can't be trivially deserialized by a program. This is intentional — the format is optimized for LLM consumption, not programmatic parsing. If machine-parsing is needed later, a JSON export can be added as a separate file.

- **File size** → Large load tests with many request types could produce a sizable AI summary. Mitigated by: only including aggregated statistics (not per-second data), and potential future configuration to limit the number of rows per section.

