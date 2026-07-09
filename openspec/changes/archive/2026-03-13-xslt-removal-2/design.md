## Context

XLT currently uses a two-step process for report generation:
1. Data processing and XML generation.
2. XSLT transformation of XML to HTML.

The XSLT step is executed in `ReportTransformer` using `XSLTUtils`. The transformation rules are configured in `ReportGeneratorConfiguration` via `com.xceptance.xlt.reportgenerator.transformations.N.*` properties.

## Goals / Non-Goals

**Goals:**
- Decouple report rendering from the underlying engine.
- Implement FreeMarker as the primary rendering engine.
- Maintain XSLT support for backward compatibility (mutually exclusive).
- Configuration parity with XSLT for FreeMarker templates.
- 100% output parity (ignoring non-functional differences).
- High-quality code using JDK 21 features and extensive documentation.
- Provide comprehensive migration documentation for users.

**Non-Goals:**
- Mixing XSLT and FreeMarker templates in a single reporting run.
- Changing the XML data structure.
- Modifying report styling or adding new features/visuals to the reports.

## Decisions

### 1. Pluggable Rendering Architecture
Introduce a `ReportRenderer` interface to abstract the rendering process.

```java
public interface ReportRenderer {
    void render(File inputXmlFile, File outputDir, Map<String, Object> parameters);
}
```

- `XsltReportRenderer`: Wraps existing XSLT logic.
- `FreeMarkerReportRenderer`: New implementation using FreeMarker.

### 2. Configuration Toggle
Add `com.xceptance.xlt.reportgenerator.renderingEngine` property to `reportgenerator.properties`.
Values: `xslt` (default for backward compatibility, though user said FreeMarker should be default - I'll set it to `freemarker` as requested).

### 3. Template Configuration
Extend transformation properties to support FreeMarker templates:
`com.xceptance.xlt.reportgenerator.transformations.N.templateFileName` will specify the `.ftl` file.

### 4. Parity Testing
Use the existing test data to run both XSLT and FreeMarker pipelines and compare the resulting HTML.
Implement a specialized assertion that ignores whitespace and non-functional differences (e.g., timestamps).

## Risks / Trade-offs

- **[Risk]** Subtle differences in HTML output → **[Mitigation]** Robust parity tests and manual verification of critical reports.
- **[Risk]** Memory usage with FreeMarker → **[Mitigation]** Use streaming when possible and monitor memory during verification.

## Implementation Details (JDK 21)
- Use `SequencedCollection` for managing transformation orders.
- Use pattern matching for switch in the `ReportRenderer` factory.
- Use `Record` for passing rendering parameters if applicable.
