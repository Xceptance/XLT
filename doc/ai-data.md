# AI Data Export

XLT automatically generates an AI-friendly summary file alongside every load test report. This file, `ai-summary.md`, packages the key statistical data from your load test into a format optimized for analysis by large language models (LLMs).

## Quick Start

The AI summary is generated automatically — no configuration needed. After generating a load test report, you'll find `ai-summary.md` in the report output directory alongside `index.html` and `testreport.xml`.

To use it for AI analysis, simply copy the file contents into your preferred LLM (ChatGPT, Claude, Gemini, etc.) along with your question:

```
Here is my load test data:

[paste contents of ai-summary.md]

Analyze this data for performance bottlenecks and recommend improvements.
```

The file is also accessible from the report's navigation bar via the **"AI Data"** link.

## Why a Special Format?

The standard `testreport.xml` file is verbose and uses hierarchical XML tags and namespaces that can consume up to 50% more tokens than more efficient formats. This has two consequences for LLM analysis:

1. **Higher cost** — More tokens means higher API costs
2. **Lower accuracy** — "Attention diffusion" reduces the model's ability to correlate data points

The AI summary uses a **YAML + Markdown hybrid** format chosen based on LLM token-efficiency research:

| Format | Token Efficiency | Reasoning Accuracy |
|---|---|---|
| XML | Baseline (100%) | Lower (verbose tags dilute attention) |
| JSON | ~20% fewer tokens | Moderate |
| **YAML** | **~40% fewer tokens** | **Highest for hierarchical data** |
| **Markdown tables** | **34–38% fewer tokens than JSON** | **Highest for tabular data** |

- **YAML** is used for hierarchical metadata (test configuration, timing metadata)
- **Markdown tables** are used for flat statistical data (KPIs, percentiles)
- **Markdown KV lists** are used for error details (test case, action, trace)

## File Contents

The AI summary includes the following sections, each present only if the corresponding data exists in the report:

### Test Metadata

General test information in YAML format:

```yaml
startTime: "2024-01-15 10:30:00"
endTime: "2024-01-15 11:30:00"
duration: 3600
bytesSent: 1234567890
bytesReceived: 9876543210
hits: 500000
```

### XLT Version

Product and version information:

```yaml
product: "Xceptance LoadTest"
version: "8.5.0"
```

### Project Name

The project name as configured in the test suite:

```yaml
name: "My Load Test Project"
```

### Comments

Free-form comments added to the test configuration:

```markdown
- First run against staging environment
- Using 50 concurrent users
```

### Load Profile

A table showing the configured test cases and their parameters:

| Test Case | Users | Iterations | Measurement [s] | Ramp-Up [s] | Shutdown [s] |
| --- | ---: | ---: | ---: | ---: | ---: |
| TGuestOrder | 10 | 0 | 3600 | 300 | 0 |
| TRegisteredOrder | 5 | 0 | 3600 | 300 | 0 |

### Transactions

Per-transaction runtime statistics with all configured percentiles:

| Name | Count | Count/s | Errors | Error% | Min | Max | Mean | Median | Dev | P50 | P95 | P99 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |

### Actions

Per-action runtime statistics (same column structure as transactions).

### Requests

Per-request runtime statistics with additional network timing columns:

| Name | Count | Count/s | Errors | Error% | Min | Max | Mean | Median | Dev | ... | DNS | Connect | Send | ServerBusy | Receive | TTFB | BytesSent | BytesRecv |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |

The network timing columns (DNS, Connect, Send, ServerBusy, Receive, TTFB) show **mean** values. BytesSent and BytesRecv also show mean values.

### Page Load Timings

Browser-level page load timing data (same column structure as transactions).

### Custom Timers

Custom timer data collected via `CustomData` (same column structure as transactions).

### Custom Values

Custom value statistics:

| Name | Count | Count/s | Min | Max | Mean | StdDev |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |

### Errors

Each unique error is listed with its details:

```markdown
## Error: Connection refused

- **Test Case**: TGuestOrder
- **Action**: Homepage
- **Count**: 42
- **Stack Trace**:
```

Stack traces are truncated to 1000 characters to keep the file size manageable.

When no errors occurred, the section shows "No errors recorded."

### Events

A summary table of events:

| Test Case | Event Name | Count |
| --- | --- | ---: |

### Agents

Agent resource utilization:

| Agent | Transactions | Errors | Error% | CPU% (Mean) |
| --- | ---: | ---: | ---: | ---: |

### Web Vitals

Core Web Vitals scores and ratings:

| Action | CLS Score | CLS Rating | FCP Score | FCP Rating | LCP Score | LCP Rating | INP Score | INP Rating | TTFB Score | TTFB Rating |
| --- | ---: | --- | ---: | --- | ---: | --- | ---: | --- | ---: | --- |

## Configuration

### Disabling AI Summary Generation

The AI summary is generated as a standard report transformation. To disable it, comment out the transformation entry in `reportgenerator.properties`:

```properties
# com.xceptance.xlt.reportgenerator.transformations.18.templateFileName = ai-summary.ftl
# com.xceptance.xlt.reportgenerator.transformations.18.outputFileName = ai-summary.md
```

When disabled, the "AI Data" navigation link will still appear in the report but will lead to a 404. To also remove the link, edit `config/report-templates/sections/navigation.ftl` and `config/xsl/loadreport/sections/navigation.xsl`.

### Customizing the AI Summary

The AI summary is generated by a FreeMarker template at `config/report-templates/ai-summary.ftl`. You can modify this template to:

- **Add sections** — for example, include scorecard data or additional metrics
- **Remove sections** — delete sections you don't need to reduce token count
- **Change format** — adjust column headers, add context, change YAML structure
- **Change column selection** — include or exclude specific metrics per table

No compilation or build step is needed — just edit the `.ftl` file and regenerate the report.

See [FreeMarker Report Rendering](freemarker.md) for details on the template system and available data model.

## Scope and Limitations

### Supported Report Types

The AI summary is currently generated for **load reports only**. It is not available for:

- **Trend reports** — These aggregate data across multiple load tests and use a different data model
- **Diff reports** — These compare two load test results and use a different data model

AI summary support for trend and diff reports may be added in future releases.

### Data Included

The AI summary contains **aggregated statistics only** — the same numbers you see in the report tables. It does not include:

- Per-second time-series data (chart data)
- Individual request logs
- Result browser data
- Chart images or file paths

### File Size

For most load tests, the AI summary is a few kilobytes. Load tests with many distinct request types or many unique errors may produce larger files. The stack traces in the error section are truncated to 1000 characters each to limit file size.

## Example Usage Prompts

Here are some effective prompts to use with the AI summary data:

**Performance Analysis:**
> Analyze this load test data. Identify the top 3 performance bottlenecks based on response times and error rates. For each, suggest a likely root cause and recommended investigation steps.

**SLA Compliance:**
> Given an SLA requiring P95 response time under 2 seconds for all transactions and an error rate below 0.1%, which transactions are non-compliant? Summarize the violations.

**Capacity Planning:**
> Based on this load test data with 50 concurrent users, estimate the maximum number of users the system could support while maintaining P99 response times under 5 seconds. Explain your reasoning.

**Comparison (manual):**
> I have two load test results. Here is the baseline:
> [paste baseline ai-summary.md]
>
> And here is the result after our optimization:
> [paste optimized ai-summary.md]
>
> Compare these results and summarize the improvements.

**Root Cause Analysis:**
> This load test encountered errors. Analyze the error details, affected transactions, and agent distribution to determine the most likely root cause.
