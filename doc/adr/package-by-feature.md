# Package-by-Feature with Clean Architecture

Date: 2026-01-30

## Status

Accepted

## Context

The Pinosu codebase originally organized source files by technical layer (e.g., `data/`, `presentation/`, `di/`). As the number of features grew, this flat layer-based structure made it difficult to understand which files belonged to which feature. Navigating between related domain, data, and presentation classes required jumping across distant directories. Adding a new feature touched many top-level packages, increasing the risk of unintended coupling between unrelated features.

## Decision

We will adopt a package-by-feature structure where each feature resides under `feature.<name>` (e.g., `feature.auth`, `feature.bookmark`, `feature.settings`) and contains its own `domain/`, `data/`, and `presentation/` sub-packages. Cross-cutting infrastructure that multiple features depend on will live under `core/` (e.g., `core.di`, `core.model`, `core.nip`). Each feature will have its own Hilt dependency injection module, and repository interfaces will reside in the domain layer to follow the Dependency Inversion Principle.

## Consequences

Positive consequences:

- Feature boundaries are explicit in the package hierarchy, making it easier to locate related code.
- Each feature can evolve independently with its own domain, data, and presentation layers.
- Feature-scoped Hilt modules improve clarity about which dependencies belong to which feature.
- Moving repository interfaces to the domain layer enforces the Dependency Inversion Principle at the package level.
- The structure lays groundwork for future modularization into separate Gradle modules if needed.

Negative consequences:

- The initial migration required moving many files and updating imports across the entire codebase.
- Some shared concepts may be duplicated across features if not carefully placed in `core/`.
- Developers unfamiliar with the convention need to learn where to place new code (feature vs. core).
