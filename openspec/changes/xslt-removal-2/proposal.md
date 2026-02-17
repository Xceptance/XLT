## Why

XSLT transformation for report generation is increasingly difficult to maintain, debug, and extend. Modern templating engines like FreeMarker offer better performance, improved developer experience, and more powerful features for generating HTML reports from XML data. This migration aims to provide a more robust and faster reporting pipeline while maintaining backward compatibility with existing XSLT templates.

## What Changes

- **Pluggable Rendering Architecture**: Introduce a `ReportRenderer` interface to decouple the report generation logic from the underlying engine (XSLT or FreeMarker).
- **FreeMarker Integration**: Add FreeMarker as a new reporting engine and implement a `FreeMarkerReportRenderer`.
- **Configurable Rendering Engine**: Add a new property `com.xceptance.xlt.reportgenerator.renderingEngine` to `reportgenerator.properties` to allow users to switch between `xslt` and `freemarker`. The default will be `freemarker`.
- **Template Parity**: Migrate all existing XSLT templates (Load Test, Trend, Diff/Comparison, and Scorecards) to FreeMarker templates.
- **Enhanced Testing**: Implement a parity testing framework that compares the output of the new FreeMarker engine against the existing XSLT engine to ensure 100% functional equivalence (ignoring whitespace).

## Capabilities

### New Capabilities
- `freemarker-rendering`: Implements the FreeMarker-based report generation engine and templates.

### Modified Capabilities
- `direct-html-generation`: Refine the requirements for the template-based rendering to specifically include FreeMarker and configuration options via `reportgenerator.properties`.

## Impact

- **Core Classes**: `ReportGenerator`, `ReportTransformer`, `ReportGeneratorConfiguration` will be refactored to use the new abstraction.
- **Configuration**: `reportgenerator.properties` will have new entries for engine selection and template configuration.
- **Dependencies**: Add `org.freemarker:freemarker` to the project dependencies.
- **Resources**: New `.ftl` template files will be added to the configuration directory, alongside existing `.xsl` files.

## Implementation Guidelines

- **JDK 21**: Leverage modern Java 21 features (e.g., Records, Pattern Matching for `switch`, `SequencedCollection`) where they improve readability or performance.
- **Coding Style**: Strictly adhere to the existing project coding style (Xceptance standards).
- **Documentation**: Provide thorough Javadoc and inline comments for all new core components and logic.
- **Migration Documentation**: Provide final user-facing documentation detailing the move from XSLT to FreeMarker, including configuration changes and benefits.
- **Strict Visual/Functional Parity**: No changes to report styling or features are allowed. The output must remain 100% equivalent to the current XSLT reports.
