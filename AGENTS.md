# Agent Guidelines

## Development Workflow

- **Test-Driven Development (TDD)**: Prefer TDD style development whenever possible. If it is not possible or practical for a specific task, you must explicitly explain why before proceeding.
- **Build Process**: A complete and full build of the project requires `ant`, not just Maven. Always consult the `README.md` file for specific build instructions and targets.
- **Dependency Management**: When updating or adding new dependencies, you must accurately update both the `doc/3rd-party-licenses/` directory for the specific library/license and the root `NOTICE.md` file.
- **Code Review**: When you code, always run a code review at the end from a different angle. If the review takes too long, ask for permission first, but always insist when a review is missing and ask the user if they want to skip.
- **AI Attribution**: Always mark changes (e.g., commit messages, comments) as AI-generated. If you know your model name, include it.

## Documentation & Comments

- **Apache License Header**: Always include the standard Apache License header at the top of a file when creating new code (if it's our code).
- **Method Javadocs**: Always properly comment methods using Javadoc, including comprehensive descriptions for `@param` and `@return` tags, unless the method is being overridden (e.g., has an `@Override` annotation or implements an interface or abstract method that has documentation).
- **Inline Comments**: Always comment the code properly. It is better to provide too much information than too little. Do not be shy about extensively commenting code blocks that are not immediately self-explanatory or that contain complex logic.

## Coding Style

- **Java 21 Features**: Prefer using modern Java 21 coding styles, features, and idioms unless using them demonstrably harms performance.

- **Allman Indentation**: You must strictly use the Allman (BSD) style for all brace placements. Opening and closing braces (`{` and `}`) must always be placed on their own new line. Never place an opening curly brace on the same line as the statement.

### Example

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
```
