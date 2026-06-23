## MODIFIED Requirements

### Requirement: Direct HTML Generation

The system SHALL generate HTML reports directly from the XML data using a Java-based template engine (FreeMarker), bypassing the XSLT transformation step. The rendering engine SHALL be selectable via configuration.

#### Scenario: Generate Load Report with FreeMarker
- **WHEN** the report generator is configured to use the FreeMarker engine
- **AND** a load report is generated
- **THEN** the system produces HTML output files using FreeMarker templates
- **AND** the output is generated without invoking the XSLT processor

#### Scenario: Generate Load Report with XSLT (Fallback)
- **WHEN** the report generator is configured to use the XSLT engine
- **AND** a load report is generated
- **THEN** the system produces HTML output files using the existing XSLT pipeline

### Requirement: Support All Report Types

The direct HTML generation capability SHALL support all standard XLT report types including Load Reports, Scorecards, Trend Reports, and Diff Reports, using FreeMarker as the primary template engine.

#### Scenario: Generate Scorecard with FreeMarker
- **WHEN** the report generator is configured to use FreeMarker
- **AND** a scorecard is generated
- **THEN** the system produces a scorecard HTML file using `.ftl` templates

### Requirement: Output Parity

The direct HTML generation capability SHALL produce HTML output that is functionally and visually equivalent to the output produced by the XSLT pipeline. Whitespace differences and non-functional metadata (like generation timestamps) SHALL be ignored during comparison.

#### Scenario: Verify Output Parity
- **WHEN** a report is generated using both XSLT and FreeMarker modes for the same test data
- **THEN** the resulting HTML files are identical in structure and content (ignoring whitespace)
- **AND** automated tests verify this parity for all report types

## ADDED Requirements

### Requirement: Configurable Rendering Engine
The system SHALL allow selecting the rendering engine via a configuration property.

#### Scenario: Select Rendering Engine
- **WHEN** `com.xceptance.xlt.reportgenerator.renderingEngine` is set to `freemarker`
- **THEN** FreeMarker is used for all transformations in the report run

#### Scenario: Configure Individual Templates
- **WHEN** a transformation is configured with `templateFileName`
- **THEN** the specified FreeMarker template is used for that transformation
