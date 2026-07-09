# FreeMarker Report Rendering

XLT supports two rendering engines for generating HTML reports from the collected load test data: **XSLT** (legacy) and **FreeMarker** (default). This document describes how the FreeMarker rendering engine works and how to customize it.

## Overview

When XLT generates a report, it follows this pipeline:

1. **Data collection** — Raw CSV timer data is parsed by registered `ReportProvider` implementations
2. **XML generation** — The collected data is serialized to `testreport.xml` via XStream
3. **Template rendering** — Each registered transformation is processed, reading the XML data and producing output files (HTML, Markdown, etc.)

The FreeMarker engine handles step 3. Each transformation maps a `.ftl` template to an output file. The template has access to the full `testreport.xml` DOM as the `report` variable.

## Selecting the Rendering Engine

The rendering engine is configured in `reportgenerator.properties`:

```properties
# Valid values: "freemarker" (default), "xslt"
com.xceptance.xlt.reportgenerator.renderingEngine = freemarker
```

When set to `freemarker`, the engine uses `templateFileName` entries from the transformation list. When set to `xslt`, it uses `styleSheetFileName` entries instead. Both can coexist in the same configuration — the engine simply ignores the property it doesn't use.

## Transformation Configuration

Transformations are numbered entries in `reportgenerator.properties` that map templates to output files:

```properties
com.xceptance.xlt.reportgenerator.transformations.1.styleSheetFileName = index.xsl
com.xceptance.xlt.reportgenerator.transformations.1.templateFileName = index.ftl
com.xceptance.xlt.reportgenerator.transformations.1.outputFileName = index.html
```

Each entry has up to three properties:

| Property | Description |
|---|---|
| `styleSheetFileName` | XSLT stylesheet (used when engine is `xslt`). Located in `config/xsl/loadreport/`. |
| `templateFileName` | FreeMarker template (used when engine is `freemarker`). Located in `config/report-templates/`. |
| `outputFileName` | The generated file, written to the report output directory. |

The numbering only determines processing order — gaps are allowed (e.g., indices 1–8, then 11–18).

### Default Transformations

| # | Template | Output | Content |
|---|---|---|---|
| 1 | `index.ftl` | `index.html` | Overview page with summary, load profile, general stats |
| 2 | `transactions.ftl` | `transactions.html` | Per-transaction runtime statistics and charts |
| 3 | `actions.ftl` | `actions.html` | Per-action runtime statistics and charts |
| 4 | `requests.ftl` | `requests.html` | Per-request runtime statistics and charts |
| 5 | `custom-timers.ftl` | `custom-timers.html` | Custom timer data and charts |
| 6 | `errors.ftl` | `errors.html` | Error details, stack traces, result browser links |
| 7 | `agents.ftl` | `agents.html` | Agent resource utilization (CPU, memory, threads) |
| 8 | `configuration.ftl` | `configuration.html` | Load profile, test settings, JVM configuration |
| 11 | `external.ftl` | `external.html` | External data (response codes, content types, etc.) |
| 12 | `network.ftl` | `network.html` | Network timing breakdown (DNS, connect, etc.) |
| 13 | `custom-values.ftl` | `custom-values.html` | Custom value statistics |
| 14 | `events.ftl` | `events.html` | Event details grouped by test case |
| 15 | `page-load-timings.ftl` | `page-load-timings.html` | Browser page load timing data |
| 16 | `web-vitals.ftl` | `web-vitals.html` | Core Web Vitals (CLS, FCP, LCP, INP, TTFB) |
| 17 | `slowest-requests.ftl` | `slowest-requests.html` | Slowest individual requests |
| 18 | `ai-summary.ftl` | `ai-summary.md` | AI-optimized data summary (see [AI Data](ai-data.md)) |

### Adding a Custom Transformation

To add a custom output file, create a new `.ftl` template and register it:

```properties
com.xceptance.xlt.reportgenerator.transformations.20.templateFileName = my-custom-report.ftl
com.xceptance.xlt.reportgenerator.transformations.20.outputFileName = my-report.html
```

Place the template in `config/report-templates/`. It will automatically receive the full `testreport.xml` data model.

### Disabling a Transformation

To disable a specific transformation, comment out all its lines:

```properties
# com.xceptance.xlt.reportgenerator.transformations.18.templateFileName = ai-summary.ftl
# com.xceptance.xlt.reportgenerator.transformations.18.outputFileName = ai-summary.md
```

## Template Structure

The FreeMarker templates are organized as follows:

```
config/report-templates/
├── index.ftl                    # Page-level templates (one per HTML page)
├── transactions.ftl
├── actions.ftl
├── ...
├── ai-summary.ftl               # Non-HTML output (Markdown)
├── sections/                     # Content section macros (load-report-specific)
│   ├── navigation.ftl            # Navigation bar
│   ├── transactions.ftl          # Transaction tables and charts
│   ├── actions.ftl
│   └── ...
├── common/                       # Shared macros (used across report types)
│   ├── sections/
│   │   ├── head.ftl              # HTML <head> with CSS/JS
│   │   ├── header.ftl            # Page header with branding
│   │   ├── footer.ftl            # Page footer
│   │   └── javascript.ftl        # JavaScript includes
│   └── util/
│       └── properties.ftl        # Property extraction utilities
├── diffreport/                   # Diff report templates
├── trendreport/                  # Trend report templates
├── scorecard/                    # Scorecard templates
└── util/                         # Utility macros
```

### Page-Level Templates

Each page-level template (e.g., `index.ftl`) follows a standard pattern:

```html
<#import "/common/sections/head.ftl" as h>
<#import "/common/sections/header.ftl" as hdr>
<#import "/common/sections/footer.ftl" as f>
<#import "/common/sections/javascript.ftl" as js>
<#import "/sections/transactions.ftl" as txn>

<#compress>
<!DOCTYPE html>
<html lang="en">
<head>
    <@h.head title="XLT Report - Transactions" ... />
</head>
<body id="loadtestreport">
<div id="container">
    <div id="content">
        <@hdr.header productName=productName scorecardPresent=scorecardPresent ... />
        <div id="data-content">
            <@txn.transactions ... />
        </div>
        <@f.footer ... />
    </div>
</div>
<@js.javascript />
</body>
</html>
</#compress>
```

## Data Model

All templates receive the same data model, which provides access to both the XML report data and rendering parameters.

### XML Data Access

The `report` variable is the root of the XML DOM parsed from `testreport.xml`. Access data using dot notation:

```
report.testreport.general[0].startTime    → Test start time
report.testreport.general[0].duration     → Test duration in seconds
report.testreport.general[0].hits         → Total request count

report.testreport.configuration[0].projectName  → Project name
report.testreport.configuration[0].version[0].version  → XLT version

report.testreport.transactions[0]         → Container for transaction elements
report.testreport.actions[0]              → Container for action elements
report.testreport.requests[0]             → Container for request elements
report.testreport.errors[0]               → Container for error elements
report.testreport.agents[0]               → Container for agent elements
report.testreport.events[0]               → Container for event elements
report.testreport.customTimers[0]         → Container for custom timer elements
report.testreport.customValues[0]         → Container for custom value elements
report.testreport.pageLoadTimings[0]      → Container for page load timing elements
report.testreport.webVitalsList[0]        → Container for web vitals elements
```

**Important**: XML elements are accessed as sequences. A single element like `<general>` is still a sequence with one item, so you access it with `[0]`. Child element lists are accessed via filtering:

```ftl
<#-- Get all transaction child elements -->
<#assign txns = report.testreport.transactions[0]?children?filter(c -> c?node_type == "element")>

<#-- Iterate -->
<#list txns as tx>
    ${tx.name}  → Transaction name
    ${tx.count} → Execution count
    ${tx.mean}  → Mean runtime
</#list>
```

### Percentiles

Percentile data is nested under each timer element:

```ftl
<#-- Access percentile values -->
<#assign pcts = element.percentiles[0]?children?filter(c -> c?node_type == "element")>
<#list pcts as p>
    ${p?node_name}  → e.g., "p50.0", "p95.0", "p99.0"
    ${p}            → The percentile value
</#list>
```

### Rendering Parameters

In addition to the XML data, templates receive these parameters:

| Variable | Type | Description |
|---|---|---|
| `productName` | String | XLT product name |
| `productVersion` | String | XLT version number |
| `productUrl` | String | XLT product URL |
| `scorecardPresent` | Boolean | Whether a scorecard was generated |
| `noCharts` | Boolean | Whether chart generation was disabled |

### Useful FreeMarker Patterns

**Safe node access** (handles missing elements):
```ftl
<#-- Returns empty string if node doesn't exist -->
${element.optionalField[0]!"default value"}
```

**Conditional sections**:
```ftl
<#if report.testreport.events?has_content>
    <#assign evts = report.testreport.events[0]?children?filter(c -> c?node_type == "element")>
    <#if evts?size gt 0>
        <#-- Render events table -->
    </#if>
</#if>
```

**Plain text output** (for non-HTML files like AI summary):
```ftl
<#ftl output_format="plainText">
```

## Customization Guide

### Modifying an Existing Page

To change the layout or content of an existing report page:

1. Find the page-level template (e.g., `transactions.ftl`)
2. Identify which section macro it uses (e.g., `/sections/transactions.ftl`)
3. Edit the section macro to add/remove/modify content
4. Regenerate the report to see changes — no build/compilation needed

### Adding a New Section to a Page

1. Create a new macro file in `sections/` (e.g., `sections/my-section.ftl`)
2. Import it in the page-level template
3. Call the macro in the appropriate location within the page

### Creating a Non-HTML Output

Templates can produce any text format. Use `<#ftl output_format="plainText">` to disable HTML escaping. See `ai-summary.ftl` for an example that produces Markdown output.

### Navigation Bar

The navigation bar is defined in `sections/navigation.ftl`. To add a link to a new page:

```ftl
<li><a href="my-report.html">My Report</a></li>
```

Add the line within the `<ul>` element in the navigation macro. The link will appear on all report pages.
