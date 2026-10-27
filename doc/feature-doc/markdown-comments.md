# Markdown Comments in Reports

XLT reports support optional Markdown formatting for test comments. When a comment
value is prefixed with `::markdown::`, the content after the prefix is converted from
Markdown to HTML and embedded in the report inside a styled `<div class="markdown">`.

## Configuration & Usage

Set comment properties in your test suite configuration (`config/test.properties` or
`config/project.properties`), or provide them during test execution via MasterController.

### Supported Properties

Comment properties under `com.xceptance.xlt.loadtests` follow the pattern
`comment`, `comment.<number>`, `comment<number>`, or `comment.commandLine`:

```properties
# Single plain text comment (raw HTML accepted)
com.xceptance.xlt.loadtests.comment = This is a plain comment

# Single Markdown-formatted comment (using \n or line continuations)
com.xceptance.xlt.loadtests.comment = ::markdown::## Test Run Notes\n\n\
  - **Environment**: staging\n\
  - **Build**: 1.2.3\n\
  - **Duration**: 2 hours
```

Multiple comments can be specified using numbers:

```properties
com.xceptance.xlt.loadtests.comment.1 = ::markdown::## Summary\nAll scenarios passed.
com.xceptance.xlt.loadtests.comment.2 = ::markdown::## Known Issues\n- Slow DNS on agent 3
```

When passing a comment via the MasterController command-line (`-c` or interactive prompt),
it is automatically recorded as `com.xceptance.xlt.loadtests.comment.commandLine`.

### Ordering & Display

Multiple comment properties are sorted lexicographically by property name and each
rendered into its own `<div class="paragraph">`. If no comment is defined or all comments
are empty, the report displays *"No comment was given."*.

## Behavior

| Input | Output |
|-------|--------|
| No prefix | Passed through as-is (raw HTML allowed) |
| `::markdown::` + content | Markdown → HTML, wrapped in `<div class="markdown">` |
| `::markdown::` only (or whitespace only) | Evaluates to `null` (treated as no comment) |
| `null` | Returned as `null` |

- **Case-insensitive prefix**: `::markdown::`, `::Markdown::`, `::MARKDOWN::`, etc. are all recognized.
- **Whitespace trimming**: Leading and trailing whitespace around the prefix and content is stripped.
- **HTML in Markdown**: Raw HTML tags embedded within Markdown content are preserved by the renderer.

## Supported Markdown Features

Powered by [flexmark-java](https://github.com/vsch/flexmark-java) with tables, autolink, and strikethrough extensions:

- **Headings** (`# H1` through `###### H6`)
- **Bold** (`**text**`) / *Italic* (`*text*`) / ~~Strikethrough~~ (`~~text~~`)
- **Lists** (ordered and unordered)
- **Tables** (GitHub-flavored pipe tables)
- **Code** (inline `` `code` `` and fenced code blocks ` ``` `)
- **Blockquotes** (`> quote`)
- **Links** (including auto-linking of URLs)
- **Horizontal rules** (`---` or `***`)

## Integration Across Reports

- **HTML Load Report (`index.html`)**: Displayed in the *Test Comment* section (`#comment`), wrapped in `<div class="markdown">` and styled via `default.css`. The styles cover headings, paragraphs, lists, code, tables, blockquotes, and rules using the report's design tokens.
- **PDF Report (`load-report.pdf`)**: Rendered in the *Test Comment* section and styled for print via `pdf.css`.
- **AI Data Export (`ai-data.md`)**: The XML report model (`testreport.xml`) carries both `<comments>` (rendered HTML) and `<rawComments>` (the authored Markdown text with `::markdown::` stripped). The AI data export reads `rawComments`, preserving the author's original Markdown formatting for LLMs while blockquoting each line to protect the document structure.
- **Trend & Diff Reports**: Test comments are tracked in trend run records and displayed in diff reports.
