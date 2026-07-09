# Implementation Tasks

## 1. Preparation & Logic Audit

- [x] 1.1 Audit existing XSLT templates for business logic
- [x] 1.2 Document required Java calculations based on audit
- [x] 1.3 Review proposed Java calculations and XML changes with User <!-- WAIT -->
- [x] 1.4 Create baseline of current XSLT output for all report types (Load, Scorecard, Trend, Diff)

## 2. Core Implementation

- [x] 2.1 Relocate existing XSLT code to `com.xceptance.xlt.report.rendering.xsl`
- [x] 2.2 Create new package `com.xceptance.xlt.report.rendering.templating`
- [x] 2.3 Refactor `ReportTransformer` to support pluggable renderers
- [x] 2.4 Create `ReportRenderer` interface
- [x] 2.5 Implement `XsltReportRenderer` (legacy wrapper)
- [x] 2.6 Implement `FreeMarkerReportRenderer` (new engine)
- [x] 2.7 Update `ReportGenerator` to use `ReportRenderer` based on configuration
- [x] 2.8 Implement logic to copy static resources (`css/`, `js/`) verbatim

## 3. Template Creation

- [x] 3.1 Create `config/report-templates` directory structure
- [x] 3.2 Implement Load Report FreeMarker templates
  - [x] 3.2.1 Create initial proof-of-concept (index.ftl, header, footer, general)
  - [x] 3.2.2 Expand remaining sections (summary, agent-summary, network-summary)
  - [x] 3.2.3 Implement detail pages (transactions, actions, requests, errors)
  - [x] 3.2.4 Implement remaining pages (configuration, agents, custom-timers, events, network, custom-values, external, page-load-timings, slowest-requests, web-vitals)
- [x] 3.3 Implement Scorecard FreeMarker templates
- [x] 3.4 Implement Trend Report FreeMarker templates
- [x] 3.5 Implement Diff Report FreeMarker templates

## 4. Verification & Testing

- [x] 4.1 Implement "Output Parity" test suite (XSLT vs Direct HTML comparison)
  - [x] 4.1.1 Create basic FreeMarkerParityTest with 10 Load Report page tests
  - [x] 4.1.2 Add remaining Load Report page tests (custom-values, external, etc.)
  - [x] 4.1.3 Add Scorecard, Trend, and Diff report tests
- [x] 4.2 Run parity tests for Load Report
- [x] 4.3 Run parity tests for Scorecard
- [x] 4.4 Run parity tests for Trend/Diff reports
- [x] 4.5 Verify custom template override mechanism

## 5. Documentation & Finalization

- [x] 5.1 Update user manual with `renderingMode` configuration
- [x] 5.2 Create migration guide for custom XSLT users
- [x] 5.3 Archive `xslt-removal` change
