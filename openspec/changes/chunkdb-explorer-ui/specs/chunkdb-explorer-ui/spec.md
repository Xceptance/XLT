# Delta Specification: Standalone Explorer Workbench & Opt-In Report Bridge

> **Background & Detailed Reference**: See [`docs/LIVEREPORTS.md`](../../../../docs/LIVEREPORTS.md) for the complete UI integration details and wireframes.

## ADDED Requirements

### Requirement: Standalone `/explorer` Diagnostic Workbench
When running with `--serve`, the embedded Jetty server SHALL serve a standalone `/explorer` web application backed by `/api/v1/*` and `/dumps/*` requiring no external JavaScript build toolchain.

#### Scenario: Interactive drill-down from time-series spike to virtual user session
- **WHEN** a user brushes a latency spike in `/explorer`, groups by `usedIpAddress` or `timerName`, clicks an outlier request in the forensic table, and selects `Trace User Session`
- **THEN** the UI displays the exact request URL, request/response IDs, form data, network timing waterfall, and the chronological Virtual User session trace with a 1-click link to the Result Browser dump (`/dumps/.../index.html`).

### Requirement: Opt-In Progressive Enhancement for Static Reports
Regular static report generation (`create_report` without `--serve` or `--live-bridge`) SHALL produce 100% static HTML with no live-server dependencies. When served via `--serve`, Jetty SHALL provide an optional `chunkdb-bridge.js` enhancement for the modernized static report.

#### Scenario: Uploading a regular static report to an external server
- **WHEN** a report is generated with standard `create_report` flags and uploaded to an external static web server
- **THEN** no requests are made to `/api/v1/*` and no dead interactive controls are rendered.
