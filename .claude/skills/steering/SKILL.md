---
description: Manage doc/steering/ as persistent project knowledge
allowed-tools: Bash, Read, Write, Edit, MultiEdit, Glob, Grep, LS
---

# Steering Management

<background_information>
**Role**: Maintain `doc/steering/` as persistent project memory.

**Mission**:

- Bootstrap: Generate core steering from codebase (first-time)
- Sync: Keep steering and codebase aligned (maintenance)
- Preserve: User customizations are sacred, updates are additive

**Success Criteria**:

- Steering captures patterns and principles, not exhaustive lists
- Code drift detected and reported
- All `doc/steering/*.md` treated equally (core + custom)
</background_information>

<instructions>

## Scenario Detection

Check `doc/steering/` status:

**Bootstrap Mode**: Empty OR missing core files (product.md, tech.md, structure.md)
**Sync Mode**: All core files exist

---

## Bootstrap Flow

1. Analyze codebase (JIT):
   - `glob_file_search` for source files
   - `read_file` for README, package.json, etc.
   - `grep` for patterns
2. Extract patterns (not lists):
   - Product: Purpose, value, core capabilities
   - Tech: Frameworks, decisions, conventions
   - Structure: Organization, naming, imports
3. Generate steering files following the templates below
4. Apply the steering principles below
5. Present summary for review

**Focus**: Patterns that guide decisions, not catalogs of files/dependencies.

---

## Sync Flow

1. Load all existing steering (`doc/steering/*.md`)
2. Analyze codebase for changes (JIT)
3. Detect drift:
   - **Steering -> Code**: Missing elements -> Warning
   - **Code -> Steering**: New patterns -> Update candidate
   - **Custom files**: Check relevance
4. Propose updates (additive, preserve user content)
5. Report: Updates, warnings, recommendations

**Update Philosophy**: Add, don't replace. Preserve user sections.

---

## Steering Principles

Steering files are **project memory**, not exhaustive specifications.

### Golden Rule

> "If new code follows existing patterns, steering shouldn't need updating."

### Document

- Organizational patterns (feature-first, layered)
- Naming conventions (PascalCase rules)
- Import strategies (absolute vs relative)
- Architectural decisions (state management)
- Technology standards (key frameworks)

### Avoid

- Complete file listings
- Every component description
- All dependencies
- Implementation details
- Agent-specific tooling directories (e.g. `.cursor/`, `.gemini/`, `.claude/`)

### Example Comparison

**Bad** (Specification-like):

```markdown
- /components/Button.tsx - Primary button with variants
- /components/Input.tsx - Text input with validation
- /components/Modal.tsx - Modal dialog
... (50+ files)
```

**Good** (Project Memory):

```markdown
## UI Components (`/components/ui/`)

Reusable, design-system aligned primitives

- Named by function (Button, Input, Modal)
- Export component + TypeScript interface
- No business logic
```

### Quality Standards

- **Single domain**: One topic per file
- **Concrete examples**: Show patterns with code
- **Explain rationale**: Why decisions were made
- **Maintainable size**: 100-200 lines typical

### Security

Never include: API keys, passwords, credentials, database URLs, internal IPs, secrets or sensitive data.

### Preservation (when updating)

- Preserve user sections and custom examples
- Additive by default (add, don't replace)
- Note why changes were made

### Prohibitions

- Do NOT add `_Updated: ...` lines at the bottom of steering files
- Do NOT include specific version numbers for libraries or dependencies (document choices and patterns only)
    - SDK target levels (e.g. Min SDK, Target SDK) are acceptable because they represent architectural decisions

---

## Templates

### product.md

```markdown
# Product Overview

[Brief description of what this product does and who it serves]

## Core Capabilities

[3-5 key capabilities, not exhaustive features]

## Target Use Cases

[Primary scenarios this product addresses]

## Value Proposition

[What makes this product unique or valuable]
```

### tech.md

```markdown
# Technology Stack

## Architecture

[High-level system design approach]

## Core Technologies

- **Language**: [e.g., TypeScript, Python]
- **Framework**: [e.g., React, Next.js, Django]
- **Runtime**: [e.g., Node.js 20+]

## Key Libraries

[Only major libraries that influence development patterns]

## Development Standards

### Type Safety

[e.g., TypeScript strict mode, no `any`]

### Code Quality

[e.g., ESLint, Prettier rules]

### Testing

[e.g., Jest, coverage requirements]

## Development Environment

### Required Tools

[Key tools and version requirements]

### Common Commands

\```bash

## Dev: [command]

## Build: [command]

## Test: [command]

\```

### Key Technical Decisions

[Important architectural choices and rationale]
```

### structure.md

```markdown
# Project Structure

## Organization Philosophy

[Describe approach: feature-first, layered, domain-driven, etc.]

## Directory Patterns

### [Pattern Name]

**Location**: `/path/`
**Purpose**: [What belongs here]
**Example**: [Brief example]

## Naming Conventions

- **Files**: [Pattern, e.g., PascalCase, kebab-case]
- **Components**: [Pattern]
- **Functions**: [Pattern]

## Import Organization

[Example import patterns]

## Code Organization Principles

[Key architectural patterns and dependency rules]
```

_Focus on patterns, not file trees. New files following patterns should not require updates._

</instructions>

## Tool guidance

- `glob_file_search`: Find source/config files
- `read_file`: Read steering, docs, configs
- `grep`: Search patterns
- `list_dir`: Analyze structure

**JIT Strategy**: Fetch when needed, not upfront.

## Output description

Chat summary only (files updated directly).

### Bootstrap

```text
Steering Created

## Generated:
- product.md: [Brief description]
- tech.md: [Key stack]
- structure.md: [Organization]

Review and approve as Source of Truth.
```

### Sync

```text
Steering Updated

## Changes:
- tech.md: Updated architecture section
- structure.md: Added API pattern

## Code Drift:
- Components not following import conventions

## Recommendations:
- Consider api-standards.md
```

## Safety & Fallback

- **Security**: Never include keys, passwords, secrets
- **Uncertainty**: Report both states, ask user
- **Preservation**: Add rather than replace when in doubt

## Notes

- All `doc/steering/*.md` loaded as project memory
- Principles are embedded above for self-containment
- Focus on patterns, not catalogs
- "Golden Rule": New code following patterns should not require steering updates
- Avoid documenting agent-specific tooling directories (e.g. `.cursor/`, `.gemini/`, `.claude/`)
