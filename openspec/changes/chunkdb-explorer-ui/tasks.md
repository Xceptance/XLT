# Tasks: Standalone Explorer Workbench & Opt-In Report Bridge (Part 3 of 3)

> **Full Reference & Context**: See [`docs/LIVEREPORTS.md`](../../../docs/LIVEREPORTS.md) for the complete architectural reference, UI wireframes, and design conversation.
> **Prerequisites**: `chunkdb-v3-completeness` (Part 1) and `chunkdb-query-server` (Part 2).

## Phase 1: Standalone `/explorer` Workbench (Zero JS Toolchain)

- [ ] **1.1 Vendor `uPlot` and create `/explorer` static assets**
  - Vendor `uPlot.min.js` + `uPlot.min.css` in `src/main/resources/com/xceptance/xlt/report/explorer/assets/`.
  - Mount `/explorer` on `ChunkExplorerServer` (`com.xceptance.xlt.report.server`).
- [ ] **1.2 Build `explorer.html`, `explorer.css`, and `explorer.js`**
  - Implement top facet filter bar (autocomplete powered by `/api/v1/metadata`).
  - Implement global timeline brush & dynamic-bucket `uPlot` chart (`p50/p95/p99`, throughput, network phases).
  - Implement group-by breakdown switcher (`Timer`, `Used IP`, `Agent`, `Scenario`, `Status`, `Content-Type`).
  - Implement bottom Forensic Outlier table + 1-click Virtual User Session Trace modal + Result Browser (`/dumps/*`) links.

## Phase 2: Opt-In `chunkdb-bridge.js` Adapter for the Modernized Report

- [ ] **2.1 Build `chunkdb-bridge.js` progressive enhancement**
  - Create a lightweight, decoupled DOM adapter (`chunkdb-bridge.js`) injected only when served via `--serve` (or `--live-bridge`), keeping standard static reports 100% untouched for external upload.
  - Hook 1: Global time-window & facet bar that refreshes `window.ALL_REQUESTS` / `ALL_TRANSACTIONS` live via `/api/v1/summary-table`.
  - Hook 2: `🔬 Live Breakdown` tab inside `#chart-workbench`.
  - Hook 3: Forensic Outliers & `👤 Trace User` table inside expanded `#drawer-*` rows.
