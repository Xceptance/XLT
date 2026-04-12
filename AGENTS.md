# Agent Instructions

Please adhere to the following rules when contributing to this repository:

## General Behavior
- **Explicit Confirmation:** Do not implement anything unless told and confirmed by the user.

## Coding Standards
- **Test-Driven Development (TDD):** Write unit and integration tests before implementing new functionality. Ensure comprehensive test coverage for all new code.
- **Code Style:** Prefer the Allman code style (braces on a new line).
- **Braces:** Always use braces `{}` for `if`, `for`, `while`, and `do` bodies, even for single-line statements.
- **Documentation:** Always comment code thoroughly.
- **AI Attribution:** If a file is exclusively created by AI, mark it in the class comment accordingly with the specific model you used.
- **License Headers:** Always add an Apache license header to all new source code files.
- **Final:** Prefer final for methods and fields in new code only.
- **Java Language Features:** Use JDK 21 features and syntax where appropriate.

## Third-Party Dependencies
- **Attribution:** If you use an open-source library, document it in `NOTICE.md` and include the appropriate license references inside `doc/3rd-party-licenses`.

## GIT

- **Merge:** Never fast-forward, never stash.
- **Stashing:** Ask for permission every time. 

## Testing & Specifications
- **Manual & Automated Tests:** You MUST create test cases as part of any specification process or implementation work.
- **Defect Resolution:** When fixing a bug, you MUST write a test case first to reproduce the defect, and implement the fix afterwards.
- **Maintenance:** You MUST review existing test cases and update them to reflect any logic, UI, or specification changes made during implementation.
- **Format & Standards:** All functional test documentation MUST explicitly adhere to the standards outlined in `doc/tests/README.md`. When creating new test cases, you MUST use `doc/tests/TEMPLATE.md` as your starting point.
