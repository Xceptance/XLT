# Markdown Comments in Reports

XLT reports support optional Markdown formatting for test comments. When a comment
value is prefixed with `::markdown::`, the content after the prefix is converted from
Markdown to HTML and embedded in the report inside a styled `<div class="markdown">`.

## Usage

Set the comment property in your test configuration:

```properties
# Plain text (unchanged, raw HTML also accepted)
com.xceptance.xlt.loadtests.comment = This is a plain comment

# Markdown-formatted comment
com.xceptance.xlt.loadtests.comment = ::markdown::## Test Run Notes\n\n- **Environment**: staging\n- **Build**: 1.2.3\n- **Duration**: 2 hours
```

Multiple numbered comments are also supported:

```properties
com.xceptance.xlt.loadtests.comment.1 = ::markdown::## Summary\nAll scenarios passed.
com.xceptance.xlt.loadtests.comment.2 = ::markdown::## Known Issues\n- Slow DNS on agent 3
```

## Behavior

| Input | Output |
|-------|--------|
| No prefix | Passed through as-is (raw HTML allowed) |
| `::markdown::` + content | Markdown → HTML, wrapped in `<div class="markdown">` |
| `::markdown::` only (no content) | Returned unchanged |
| `null` | Returned as `null` |

The `::markdown::` prefix is **case-insensitive** — `::Markdown::`, `::MARKDOWN::`, etc. all work.

## Supported Markdown Features

Powered by [flexmark-java](https://github.com/vsch/flexmark-java) with these extensions:

- **Headings** (`# H1` through `###### H6`)
- **Bold** / *Italic* / ~~Strikethrough~~
- **Lists** (ordered and unordered)
- **Tables** (GitHub-flavored)
- **Code** (inline and fenced code blocks)
- **Blockquotes**
- **Links** (including auto-linking of URLs)
- **Horizontal rules**

## Styling

The rendered Markdown is wrapped in `<div class="markdown">` and styled via the report's
`default.css`. The styles use the report's existing CSS custom properties for consistent
appearance. Headings, tables, code blocks, blockquotes, and lists are all covered.
