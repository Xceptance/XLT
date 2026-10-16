# Proposal: Standalone Explorer Workbench & Opt-In Report Bridge (Part 3 of 3)

> **Full Reference & Conversation History**: See [`docs/LIVEREPORTS.md`](../../../docs/LIVEREPORTS.md) for the complete UI wireframes, `XLT.report-update` integration strategy, and the chronological design conversation.
> **Sequence**: Part 3 of 3 (`chunkdb-v3-completeness` $\rightarrow$ `chunkdb-query-server` $\rightarrow$ `chunkdb-explorer-ui`).

## Why

With `ChunkDB` V3 (`chunkdb-v3-completeness`) and the vectorized query engine + Jetty `/api/v1/*` endpoints (`chunkdb-query-server`) in place, engineers need a fast, interactive frontend to slice time windows, re-bucket charts, group by secondary dimensions (`usedIpAddress`, `agentName`, `responseCode`), inspect exact outlier payloads, and trace Virtual User sessions—without coupling backend capabilities to ongoing layout prototypes in the static report (`XLT.report-update`) or polluting static reports generated for external upload.

## What Changes

1. **Standalone `/explorer` Diagnostic Workbench (Zero JS Toolchain)**:
   - Serve `/explorer` from classpath resources on the embedded Jetty server using vendored `uPlot` + vanilla ES6 modules + CSS variables matching `XLT.report-update`.
   - Include a top facet filter bar, global timeline brush, dynamic-bucket `uPlot` chart (`p50/p95/p99`, throughput, network phases), group-by breakdown switcher (`Timer`, `Used IP`, `Agent`, `Scenario`, `Status`, `Content-Type`), bottom Forensic Outlier table, and 1-click Virtual User Session Trace modal with Result Browser links.
2. **Opt-In `chunkdb-bridge.js` Adapter for the Modernized Report**:
   - Provide a lightweight, decoupled DOM adapter (`chunkdb-bridge.js`) injected only when served via `--serve` (or `--live-bridge`), leaving standard static reports (`create_report`) 100% untouched for external upload.
   - Progressively enhance the modernized static report when present:
     - Global time-window & facet bar that refreshes `window.ALL_REQUESTS` / `ALL_TRANSACTIONS` live via `/api/v1/summary-table`.
     - `🔬 Live Breakdown` tab inside `#chart-workbench`.
     - Forensic Outliers & `👤 Trace User` table inside expanded `#drawer-*` rows.

## Non-Goals

- No Node.js/npm/Webpack build toolchain required to build or maintain the explorer UI.
- No mandatory modifications to static reports generated for external upload (`create_report` without `--serve` or `--live-bridge` remains 100% static).
