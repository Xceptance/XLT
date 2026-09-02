# PDF Report Generation

XLT supports generating an executive-ready PDF report alongside the standard HTML load test report. The PDF report compiles the key performance indicators (KPIs), executive summary, load profile, transaction/request statistics, primary charts, and optional ratings into a clean, printable A4 document.

The PDF generation is **100% pure Java and open-source**, powered by [OpenHTMLtoPDF](https://github.com/danfickle/openhtmltopdf) and Apache PDFBox. It requires no external tools, no headless browsers (like Chrome or Puppeteer), and no native binaries.

---

## Quick Start

PDF generation is **disabled by default** to keep standard report generation as fast as possible.

### 1. Enable via Command Line Flag

Pass the `--pdf` (or `-pdf`) flag to `create_report.sh` (or `create_report.cmd` on Windows):

```bash
./bin/create_report.sh --pdf ./results/20260901-100000
```

### 2. Enable via Configuration Property

You can also enable PDF generation persistently or via property override:

- In `config/reportgenerator.properties`:
  ```properties
  com.xceptance.xlt.reportgenerator.pdf = true
  ```
- Or on the command line via `-D`:
  ```bash
  ./bin/create_report.sh -Dcom.xceptance.xlt.reportgenerator.pdf=true ./results/20260901-100000
  ```

When enabled, the report generator will output `load-report.pdf` in the root of the generated report directory (e.g. `reports/<date>/load-report.pdf`).

---

## Accessing the PDF Report

When PDF generation is enabled, the PDF report is integrated into the HTML report navigation bar:

- A **"PDF Report"** download link appears on the top navigation bar.
- The link opens or downloads `load-report.pdf`.
- If PDF generation was not enabled, the download link is automatically omitted from the navigation bar.

---

## Test Rating & Executive Evaluation

You can inject external assessment data (such as automated CI/CD ratings or AI-generated evaluations) into both the HTML and PDF reports.

### Command-Line Arguments

The rating score and summary line can be passed directly as command-line arguments to `create_report.sh`:

| Argument (Short) | Argument (Long) | Description | Property Injected |
|---|---|---|---|
| `-rating <score>` | `--rating <score>` | Grade from `A` to `F` (e.g., `A`, `B`, `C`, `D`, `E`, `F`) | `com.xceptance.xtc.loadtest.rating` |
| `-rating-summary <summary>` | `--rating-summary <summary>` | Single-line summary description | `com.xceptance.xtc.loadtest.rating.summary` |

**Example:**
```bash
./bin/create_report.sh --pdf \
    --rating A \
    --rating-summary "All SLAs met; response times well below the 500ms target." \
    ./results/20260901-100000
```

### Configuration Properties

Alternatively, you can set the rating in `default.properties` within the test suite or pass them as system properties (`-D`):

| Property Key | Type | Description |
|---|---|---|
| `com.xceptance.xtc.loadtest.rating` | String (`A`–`F`) | Letter grade badge displayed with color-coding (A=Green, B=Light Green, C=Amber, D=Orange, E/F=Red). |
| `com.xceptance.xtc.loadtest.rating.summary` | String | A concise summary text displayed alongside the badge. |
| `com.xceptance.xtc.loadtest.rating.evaluation` | Markdown | Multi-paragraph assessment rendered from Markdown to formatted HTML. |

#### Evaluation Markdown Format in Properties

When supplying `com.xceptance.xtc.loadtest.rating.evaluation` in a `.properties` file, use standard Java properties newline escaping (`\n` or trailing `\` for multiline continuation):

```properties
com.xceptance.xtc.loadtest.rating = B
com.xceptance.xtc.loadtest.rating.summary = Performance acceptable with minor latency spikes during peak load.
com.xceptance.xtc.loadtest.rating.evaluation = ### Executive Summary\n\n\
  - **Peak Throughput**: 1,450 req/s\n\
  - **P95 Response Time**: 320 ms\n\
  - **Error Rate**: 0.02%\n\n\
  ### Recommendations\n\
  Review database connection pool settings under sustained high concurrency.
```

> [!NOTE]
> Command-line options (`--rating`, `--rating-summary`, or `-D...`) take precedence over values loaded from the test result property files.

---

## PDF Report Structure & Contents

The PDF report is designed as an executive document that captures all essential performance data and primary overview charts while omitting voluminous per-request stack traces or full-detail drilldowns:

1. **Header & Metadata**:
   - Report title, test start/end dates, total elapsed duration (`hh:mm:ss`).
2. **Rating & Assessment** *(if present)*:
   - Color-coded letter grade badge (`A` through `F`).
   - Summary text.
   - Rendered Markdown evaluation sections (tables, bullet points, bold/italic text).
3. **Test Comments**:
   - Test run comments and notes (plain text or Markdown).
4. **Load Profile & Configuration**:
   - Configured test cases, user counts, ramp-up times, measurement durations, and percentages.
5. **General Information & Charts**:
   - Key test execution parameters, total hits, sent/received bytes.
   - Concurrent Users chart, Requests per Second chart, and Request Runtime chart.
6. **Agent Summary & Utilization**:
   - Agent instances, total transactions/actions/requests executed per agent.
   - All Agents CPU Usage chart.
7. **Performance Summary & Network**:
   - High-level KPI summary table (Transactions, Actions, Requests, Custom Timers counts, error rates, runtime percentiles).
   - Incoming/outgoing network throughput statistics.
8. **Transactions**:
   - Overview runtime chart (`All Transactions.webp` with runtime scatter plot, moving average, error rates, and event distribution).
   - Detailed transaction statistics table (count, rates, errors, min/max/mean/dev, P50 through P99.9 percentiles).
9. **Actions**:
   - Overview runtime chart (`All Actions.webp`).
   - Detailed action performance table.
10. **Requests**:
    - Overview runtime chart (`All Requests.webp`).
    - Detailed request performance table including runtime intervals/segmentation.
11. **Custom Timers** *(if present)*:
    - Overview runtime chart (`All Custom Timers.webp`).
    - Detailed custom timers performance table.
12. **Errors & Events Overview**:
    - Aggregated error counts, error rates, and event summaries.

---

## Styling & Layout

The PDF report utilizes CSS Paged Media Level 3 rules (`config/testreport/css/pdf.css`) to ensure document formatting:

- **Page Layout**: Standard A4 landscape with consistent margins.
- **Running Headers & Footers**:
  - Running header: Report title and generation context.
  - Running footer: Product branding and automatic page numbering (`Page X of Y`).
- **Smart Page Breaks**:
  - `page-break-inside: avoid` on charts and summary boxes to prevent awkward mid-element page splits.
  - `thead { display: table-header-group; }` to repeat table header rows across page breaks on multi-page tables.
- **Typography & Colors**:
  - Clean font stack (sans-serif / Liberation Sans) compatible with standard PDF engines.
  - High-contrast table row shading and consistent status badge color coding.

---

## Architecture & Technical Details

- **OpenHTMLtoPDF**: Renders the XML/XSL-generated XHTML into a PDF DOM using Apache PDFBox.
- **SVG & Raster Images**: Charts rendered as high-resolution SVGs or PNGs are automatically embedded with crisp vector lines.
- **Memory & Resource Efficiency**: Fast-mode rendering ensures minimal memory overhead and rapid document creation (typically 1–3 seconds).
- **Zero Native Dependencies**: Does not require any OS packages (such as Chrome, fontconfig, or X11 libraries), making it fully compatible with headless containerized CI/CD environments (Docker, Kubernetes, GitHub Actions, GitLab CI).
