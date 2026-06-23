## Context

The current XLT report generator uses an XSLT-based pipeline (XML -> XSLT -> HTML), which is complex and hard to maintain. We are introducing a direct Java-based HTML generation path using FreeMarker to simplify maintenance while preserving backward compatibility.

## Goals / Non-Goals

**Goals:**

- Provide a "Direct HTML" generation mode that produces HTML functionally identical to the XSLT output.
- Improve generation performance by moving calculations from XSLT to Java.
- Maintain full backward compatibility for users with custom XSLT.
- Ensure custom CSS/JS and report resources work exactly as before.

**Non-Goals:**

- Redesigning the report HTML structure or styling (visuals remain identical).
- Removing the XSLT pipeline immediately (it remains as a supported option).
- Changing the intermediate XML format in a breaking way.

## Decisions

### 1. Templating Engine: FreeMarker

We will use FreeMarker as the templating engine.
**Rationale**: It is already a dependency in XLT (used for ScriptDoc), familiar to Java developers, and offers better performance/maintainability than XSLT.

### 2. Rendering Architecture

A new `ReportRenderer` interface will allow pluggable strategies:

- `XsltReportRenderer`: The legacy implementation.
- `FreeMarkerReportRenderer`: The new implementation.
Selection will be driven by the `com.xceptance.xlt.reportgenerator.renderingMode` property.

### 3. Metric Pre-computation

Calculation logic currently embedded in XSLT (e.g., specific formatting, conditional logic based on node counts) will be moved to Java "View Models" or enhanced XML data objects.
**Rationale**: Keeps templates dumb (View) and logic in Java (Controller/Model), improving testability and performance.

### 4. Static Resources

The `css/`, `js/`, and image directories will be copied *verbatim* from the `config/report-resources` (or equivalent) source to the output directory.
**Rationale**: Ensures 100% compatibility with existing user customizations and overrides.

### 5. Testing Strategy

We will implement "Output Parity" testing where the exact same test data is fed to both renderers, and the output HTML is compared.
**Rationale**: Guarantees that the new implementation is a drop-in replacement.

## Risks / Trade-offs

- **Risk**: Custom XSLT logic in user templates.
  - *Mitigation*: Users can keep using XSLT mode. Migration guide provided.
- **Trade-off**: Duplicate maintenance for a period of time (maintaining both XSLT and FreeMarker templates).
  - *Acceptance*: Necessary for safe non-breaking migration.
- **Risk**: Subtle differences in whitespace or ordering.
  - *Mitigation*: Comparison tools will normalize whitespace; strictly ordered data models in Java.
- **Trade-off**: Minor formatting differences (e.g., byte units) due to standard library adoption.
  - *Acceptance*: Accepted preference for standard libraries over exact legacy emulation.
