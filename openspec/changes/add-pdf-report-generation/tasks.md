## 1. Dependencies & Configuration

- [x] 1.1 Add `openhtmltopdf-core` and `openhtmltopdf-pdfbox` dependencies to `pom.xml` and update `NOTICE.md` with third-party license information.
- [x] 1.2 Add PDF configuration property (`com.xceptance.xlt.reportgenerator.pdf.enable`) to `config/reportgenerator.properties` and add getter/setter methods in `ReportGeneratorConfiguration.java`.
- [x] 1.3 Add `--pdf` command-line option in `ReportGeneratorMain.java` to enable PDF generation and pass flag into `ReportGenerator`.

## 2. Template & Styling (Landscape A4)

- [x] 2.1 Create print stylesheet `config/testreport/css/pdf.css` with `@page { size: A4 landscape; margin: 12mm; }`, page counters, running headers/footers, table page-break rules, and typography.
- [x] 2.2 Create `config/xsl/loadreport/pdf.xsl` transforming `loadreport.xml` into self-contained landscape HTML covering: metadata, scorecard, top runtime chart, transactions, actions, requests, network summary, and error/event statistics.
- [x] 2.3 Update `config/xsl/loadreport/sections/navigation.xsl` to display a PDF download link in the HTML report navigation bar when PDF generation is enabled.

## 3. PDF Generator Core

- [x] 3.1 Implement `PdfReportGenerator.java` (or utility class) to execute the XSL transformation and render PDF using OpenHTMLtoPDF `PdfRendererBuilder` in fast mode with proper base URI resolution.
- [x] 3.2 Wire `PdfReportGenerator` into `ReportGenerator.java` (`generateReport()`) to trigger PDF generation when enabled via CLI or properties.

## 4. Verification & Testing

- [x] 4.1 Add automated tests verifying that PDF generation produces a valid `load-report.pdf` when enabled, and produces no PDF when disabled.
- [x] 4.2 Run report generation against test data and inspect generated PDF layout, tables, charts, and HTML download link.
