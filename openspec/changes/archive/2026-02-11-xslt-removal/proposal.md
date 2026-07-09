## Why

The current report generation pipeline produces XML reports as an intermediate format, then uses XSLT to transform that XML into HTML. The XML format is consumed by other tools and must remain unchanged. However, the XSLT transformation step (XML→HTML) is complex, difficult to maintain for Java developers, and makes the codebase harder to reason about. By adding a direct HTML generation alternative that consumes the same XML format, we provide a simpler, more maintainable path while preserving backward compatibility for existing XSLT-based workflows and custom templates.

## What Changes

- **XML structure preserved** - existing XML format remains valid; new optional elements may be added for pre-computed values (XSLT pipeline ignores unknown elements)
- **HTML output remains the same** - no redesign or rework of HTML structure/styling, only changing how it's generated
- **Move calculations to Java** - calculations currently done in XSLT should be performed upfront in Java as much as possible, enriching XML with pre-computed values; templates focus on presentation
- **Audit existing business logic** - audit current XSLT templates to identify and migrate any embedded business logic to Java
- **Resources preserved** - `css/` and `js/` directories in the report output will be byte-for-byte identical (copied from the same source), ensuring custom user CSS overrides work exactly as before
- Add direct HTML generation capability as an alternative to XSLT transformation (XML→HTML step only)
- **Use FreeMarker** for templating (already in use for ScriptDocGenerator)
- Introduce configuration option to choose between XSLT-based and direct HTML rendering
- **Applies to all report types**: Load reports, Scorecard, Trend reports, and Diff reports
- Keep `XSLTUtils`, `ReportTransformer`, and XSLT templates for backward compatibility
- Default to the new direct HTML generation approach for new installations
- Existing XSLT-based workflows remain fully functional when configured

## Capabilities

### New Capabilities

- `direct-html-generation`: Report generation can produce HTML output directly without XSLT transformation step
- `configurable-report-rendering`: Users can choose between XSLT-based and direct HTML generation via configuration

### Modified Capabilities

- None (this is purely additive, existing functionality is preserved)

## Impact

- **Code**:
  - **Package restructure**:
    - Move existing XSLT code from `com.xceptance.xlt.report` → `com.xceptance.xlt.report.rendering.xsl`
    - New templating code in `com.xceptance.xlt.report.rendering.templating`
  - `ReportTransformer.java` - Refactor to support pluggable rendering strategies
  - `ReportGenerator.java` - Add logic to choose rendering strategy based on configuration
  - `TrendReportGeneratorMain.java` - Update to use new rendering approach
  - `DiffReportGeneratorMain.java` - Update to use new rendering approach
  - New classes in `com.xceptance.xlt.report.rendering.templating`: `TemplateRenderer`, `FreeMarkerReportRenderer`
  - `ReportGeneratorConfiguration.java` - Add property for rendering mode selection
  - **Context exposure**: Ensure new FreeMarker context exposes the same utility functions or equivalent Java beans that custom XSLT templates might currently be calling (note: `XSLTUtils` itself does not contain XSLT extension functions, but templates may access other beans)
- **Configuration**:
  - `config/xsl/` - All XSLT templates remain for backward compatibility
  - **New template directory**: `config/report-templates/` - Contains FreeMarker templates for:
    - Load reports (`loadreport/`)
    - Scorecard (`scorecard/`)
    - Trend reports (`trendreport/`)
    - Diff reports (`diffreport/`)
  - Templates must be **user-customizable** like current XSLT templates
  - New configuration property: `com.xceptance.xlt.reportgenerator.renderingMode` (values: `xslt`, `template`)
    - Property-based configuration (no CLI changes needed)
  - Configuration for custom template locations/overrides
- **Dependencies**:
  - FreeMarker (already present in project)
  - XSLT dependencies remain for backward compatibility
- **Users**:
  - **Non-breaking**: Existing XSLT-based workflows continue working
  - New option to use simpler, Java-native HTML generation
  - Custom XSLT templates remain supported when XSLT mode is enabled
  - **New templates are customizable** - users can modify rendering templates just like XSLT

## Testing Strategy

- **Baseline tests** validate current XSLT rendering behavior (already implemented)
- **Output comparison** - FreeMarker templates must produce HTML structurally equivalent to XSLT output
- Test both rendering modes with same XML input to ensure compatibility
- Scorecard rendering included in test coverage
- Test custom template override functionality

## Performance Expectations

- **Faster rendering** - Java pre-computation eliminates XSLT processing overhead
- **Lower memory usage** - Simpler template processing vs. XSLT transformation engine
- Pre-computed values in XML reduce template complexity
- Actual performance gains TBD during implementation

## Migration Path

- **Phase 1**: New installations default to FreeMarker rendering
- **Phase 2**: Existing installations can opt-in via configuration
- **No forced migration** - XSLT remains fully supported
- **Comparison mode** (optional): Generate both outputs side-by-side for validation
- Users with custom XSLT templates can:
  - Continue using XSLT mode
  - Migrate templates to FreeMarker at their own pace
  - Use provided migration guide

## Error Handling

- **Template not found**: Fail with clear error message indicating missing template file
- **Rendering failure**: Log detailed error, optionally fall back to XSLT if configured
- **Configuration error**: Validate rendering mode property, reject invalid values
- **Template syntax errors**: Report FreeMarker syntax errors with line numbers

## Documentation Updates

- Update user manual with new configuration properties
- Create migration guide for custom template users
- Document template customization for FreeMarker
- Add troubleshooting section for common rendering issues
- Examples of pre-computed values available to templates
