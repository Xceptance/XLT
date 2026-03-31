# Agent Guidelines

Welcome AI Agent! When contributing to the XLT codebase, please adhere to the following coding standards and instructions.

## Coding Style
- **Allman Indentation**: You must strictly use the Allman (BSD) style for all brace placements. Opening and closing braces (`{` and `}`) should always be placed on a new line, indented to the same level as the control statement that precedes them. Never place an opening curly brace on the same line as the statement.

### Example:
```java
// Correct (Allman Style)
public void doSomething()
{
    if (condition)
    {
        execute();
    }
    else
    {
        fallback();
    }
}

// Incorrect
public void doSomething() {
    if (condition) {
        execute();
    } else {
        fallback();
    }
}
```
