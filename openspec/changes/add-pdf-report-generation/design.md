## Context

See [proposal.md](proposal.md) for motivation and [specs/pdf-report/spec.md](specs/pdf-report/spec.md) for behavioral requirements.

XLT's report generation crunches raw test data into an XML report (`loadreport.xml`) and creates chart images in the report output directory. The existing pipeline uses XSLT (`XSLTUtils`) to transform `loadreport.xml` into multiple HTML files (`index.html`, `transactions.html`, etc.).

This design defines the architecture for rendering an optional, self-contained, downloadable PDF summary report using OpenHTMLtoPDF and a dedicated XSL stylesheet.

## Goals / Non-Goals

**Goals:**
- Pure Java, 100% open-source PDF generation without external OS binaries or headless browsers.
- Clean A4 Landscape layout suitable for wide tabular performance metrics and overview charts.
- Configurable on/off switch via `--pdf` CLI flag and `reportgenerator.properties` (default: `false`).
- Direct integration into HTML report navigation for seamless downloads.

**Non-Goals:**
- Replicating every individual transaction/request drill-down chart or huge stack trace dump into the PDF.
- Supporting interactive PDF features (forms, JavaScript).

## Decisions

### Decision 1: PDF Engine — OpenHTMLtoPDF
- **Choice**: `com.openhtmltopdf:openhtmltopdf-pdfbox` (and `openhtmltopdf-core`).
- **Rationale**: Built on top of Apache PDFBox, OpenHTMLtoPDF provides a robust CSS Paged Media layout engine (`@page`, running headers/footers, repeating `<thead>` across pages, `page-break-inside: avoid`). It allows XLT to leverage its existing XSLT template architecture to produce print HTML that is rendered directly to PDF in 5 lines of Java.
- **Alternatives Considered**:
  - *Raw Apache PDFBox*: Required hundreds of lines of procedural drawing math for column widths, line wrapping, and multi-page tables.
  - *Apache FOP*: Required maintaining complex XSL-FO stylesheets instead of standard HTML/CSS.
  - *FlyingSaucer*: Older CSS support, less active maintenance.

### Decision 2: Orientation & Typography — A4 Landscape
- **Choice**: `@page { size: A4 landscape; margin: 12mm; }`.
- **Rationale**: Performance metrics tables with 8–12 columns (Count, Rates, Errors, Mean, Min, Max, Percentiles) and 16:9 time-series charts naturally require horizontal width to maintain readable typography (8–10pt font) without awkward column truncation.

### Decision 3: Pipeline Architecture
- **Flow**:
  ```
  loadreport.xml ──[pdf.xsl]──> Printable HTML ──[OpenHTMLtoPDF]──> load-report.pdf
  ```
1. `ReportGenerator.java` checks if `config.isPdfReportEnabled()`.
2. When enabled, `ReportTransformer` transforms `loadreport.xml` with `config/xsl/loadreport/pdf.xsl` into a temporary/intermediate HTML stream.
3. `PdfRendererBuilder` parses the HTML (with base URI set to `outputDir`), resolves embedded charts and static CSS assets, and writes `load-report.pdf` to `outputDir`.
4. The intermediate HTML is not retained in final output if only the PDF is desired, or retained as `pdf-preview.html` if helpful for debugging.

### Decision 4: CLI & Configuration Wiring
- **CLI Option**: Added `--pdf` (or `-pdf`) in `ReportGeneratorMain.java` options definition.
- **Property**: `com.xceptance.xlt.reportgenerator.pdf.enable = false` in `reportgenerator.properties`.
- **Flag propagation**: Passed through `ReportGeneratorConfiguration` into `ReportGenerator`.

### Decision 5: HTML Report Download Link
- In `config/xsl/loadreport/sections/navigation.xsl`, conditionally render a download button `[📥 Download PDF]` pointing to `./load-report.pdf` when PDF generation is enabled (via dynamic parameter `pdfReportPresent`).

## Risks / Trade-offs

- **Memory overhead during rendering** → Mitigation: Use OpenHTMLtoPDF `useFastMode()` and stream output directly to `FileOutputStream` without buffering entire PDF in memory.
- **Missing chart references when `--noCharts` is active** → Mitigation: Use XSL conditionals (`<xsl:if test="...">`) in `pdf.xsl` to omit `<img>` tags if charts are disabled.
- **Font rendering across environments** → Mitigation: Rely on standard built-in PDF sans-serif fonts (Helvetica/Arial) with robust CSS fallback rules.
