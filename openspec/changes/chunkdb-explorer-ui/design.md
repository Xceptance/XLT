# Design: Standalone Explorer Workbench & Opt-In Report Bridge (Part 3 of 3)

> **Full Reference & Conversation History**: See [`docs/LIVEREPORTS.md`](../../../docs/LIVEREPORTS.md) for the complete UI layout wireframes and `XLT.report-update` integration analysis.

## 1. Decoupling from Evolving Static Report Prototypes (`XLT.report-update`)

Because the modernized static report is actively evolving as a prototype, the UI architecture strictly separates:
1. **Standalone `/explorer` Workbench**: Self-contained diagnostic page served from `src/main/resources/com/xceptance/xlt/report/explorer/` using the shared CSS design tokens (`--bg-surface`, `--text-primary`, `--font-mono`, etc.). Zero dependency on the static report's HTML/DOM structure.
2. **Optional Report Adapter (`chunkdb-bridge.js`)**: A small progressive-enhancement script injected only in Live Mode (`--serve` or `--live-bridge`) that checks for `#chart-workbench`, `#drawer-*`, and `window.ALL_REQUESTS` and attaches live controls if present.
3. **100% Clean Static Reports by Default**: Standard `create_report` runs do not inject `chunkdb-bridge.js` and render zero dead UI controls when uploaded to external static web servers.

## 2. Zero-JS-Toolchain Frontend Stack

- **Charting**: Vendored `uPlot` (`~45 KB`, MIT, zero dependencies) consuming the columnar `[timestamps[], series1[], series2[]...]` arrays directly from `POST /api/v1/timeseries`.
- **Modules**: Native browser ES6 modules (`explorer.js`, `chunkdb-bridge.js`) requiring zero Node.js/npm build steps during Maven builds.
