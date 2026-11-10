## Why

Load test results in XLT are currently published as multi-page interactive HTML reports and machine-readable data files (XML, AI summary). Users and stakeholders often need a standalone, downloadable, executive-ready PDF summary report that can be easily shared, archived, and printed without requiring external browser-based PDF export tools.

## What Changes

- Add optional PDF report generation to the XLT report generator pipeline using OpenHTMLtoPDF (pure Java, LGPL 2.1+ / Apache-2.0).
- Add `--pdf` command line option to `ReportGeneratorMain` and `create_report.sh` to enable PDF generation on demand.
- Add configuration property `com.xceptance.xlt.reportgenerator.pdf.enable` (default: `false`) in `reportgenerator.properties`.
- Implement a dedicated landscape A4 XSL stylesheet (`pdf.xsl`) and print CSS to render an executive and statistical summary from `loadreport.xml`.
- Include in the PDF report:
  - Test metadata, configuration summary, and scorecard evaluation (if available)
  - Top-level runtime overview chart (excluding per-transaction / per-request individual charts)
  - Full runtime statistical tables for Transactions, Actions, Requests, and Network/HTTP status codes
  - Statistical summary tables for Errors and Events (omitting full trace dumps)
- Add a download link in the HTML report navigation bar to download `load-report.pdf` when generated.

## Capabilities

### New Capabilities
- `pdf-report`: Covers optional PDF report generation, CLI and configuration toggles, landscape layout template, chart/table content inclusion rules, and HTML report download integration.

### Modified Capabilities
<!-- No existing capability requirements are modified -->

## Impact

- **Dependencies**: Adds `openhtmltopdf-core` and `openhtmltopdf-pdfbox` (and transitive PDFBox/FontBox components) to `pom.xml` and third-party license notices (`NOTICE.md`).
- **Runtime**: No impact when PDF generation is disabled (default). When enabled, performs an additional XSLT transformation and PDF rendering step during `ReportGenerator.generateReport()`.
- **CLI / Configuration**: Adds `--pdf` CLI flag and `com.xceptance.xlt.reportgenerator.pdf.*` properties.
- **Templates**: Adds `config/xsl/loadreport/pdf.xsl` and print styling resources. Updates `config/xsl/loadreport/sections/navigation.xsl` to include a PDF download link.
