# R8 Minification and Resource Shrinking

Date: 2026-03-08

## Status

Accepted

## Context

Release builds of Pinosu were not using R8 code shrinking or resource shrinking. The APK contained unused code paths from dependencies (such as Quartz, OkHttp, and Jsoup) and unused resources, resulting in a larger-than-necessary download size. As the dependency tree grew, the APK size impact became more significant.

## Decision

We will enable R8 minification (`isMinifyEnabled = true`) and resource shrinking (`isShrinkResources = true`) for release builds. ProGuard rules will suppress warnings from optional transitive dependencies (e.g., re2j from Jsoup). We will preserve `SourceFile` and `LineNumberTable` attributes so that crash stack traces remain readable without requiring a separate mapping file lookup. Obfuscation is effectively disabled by Quartz's consumer ProGuard rules, which preserve class names.

## Consequences

Positive consequences:

- Smaller APK size through dead code elimination and unused resource removal.
- Reduced attack surface by stripping unused code paths from third-party libraries.
- Readable crash stack traces are preserved, maintaining debuggability in production.

Negative consequences:

- ProGuard/R8 rules must be maintained as dependencies change; missing rules can cause runtime crashes.
- Build times for release variants increase due to the additional shrinking and optimization passes.
- Obfuscation benefits are limited because Quartz's consumer rules preserve class names, so R8 primarily provides size reduction rather than code protection.
