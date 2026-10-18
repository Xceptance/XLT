# Code Quality, Commenting, Coverage, and Mutation Testing Standards

1. **Human-Oriented Code Comments**:
   - Always write clear, helpful Javadoc and inline comments for human maintainers.
   - Focus on the **why** and **how**: data structure layouts, bit-packing/indexing math, lazy-initialization invariants, ordering constraints, concurrency models, and memory/GC lifecycle reasons.

2. **Code Coverage (JaCoCo)**:
   - Measure both **line coverage** and **branch coverage** on all new or modified classes using JaCoCo (`mvn test -Dtest=...` automatically runs `jacoco:prepare-agent` and `jacoco:report`).
   - Inspect `target/site/jacoco/index.html` (or `jacoco.csv` / `jacoco.xml`) to ensure fast-paths, fallback paths, and edge cases are exercised.

3. **Mutation Testing (PITest)**:
   - Verify test suite strength on touched classes using targeted PITest runs:
     ```bash
     mvn test-compile pitest:mutationCoverage \
       -DtargetClasses=com.xceptance.xlt.path.ToClass* \
       -DtargetTests=com.xceptance.xlt.path.ToClass*Test
     ```
   - Inspect surviving mutants in `target/pit-reports/index.html` (or `mutations.xml`) and add targeted assertions to kill any meaningful surviving mutants (boundary conditions, bitwise shifts/masks, state transitions).
