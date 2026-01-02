# Project Structure

## Organization Philosophy

**Clean Architecture** with strict layer separation: Domain → Data → Presentation.
Business logic is independent of frameworks, UI, and external dependencies.

## Architecture Diagram

```mermaid
graph TB
    subgraph "Presentation Layer"
        UI[LoginScreen<br/>Composable]
        VM[LoginViewModel<br/>StateFlow]
        UI -->|observes state| VM
        VM -->|emits| UI
    end

    subgraph "Domain Layer"
        UC_Login[LoginUseCase<br/>Interface]
        UC_Logout[LogoutUseCase<br/>Interface]
        UC_GetState[GetLoginStateUseCase<br/>Interface]
        Model[User, AuthEvent<br/>ErrorTypes]

        UC_Login -.implements.- UC_Login_Impl[AmberLoginUseCase]
        UC_Logout -.implements.- UC_Logout_Impl[AmberLogoutUseCase]
        UC_GetState -.implements.- UC_GetState_Impl[AmberGetLoginStateUseCase]
    end

    subgraph "Data Layer"
        Repo[AuthRepository<br/>Interface]
        RepoImpl[AmberAuthRepository]
        LocalDS[LocalAuthDataSource<br/>EncryptedSharedPreferences]
        AmberClient[AmberSignerClient<br/>NIP-55]

        Repo -.implements.- RepoImpl
        RepoImpl --> LocalDS
        RepoImpl --> AmberClient
    end

    subgraph "External Dependencies"
        Amber[Amber Signer App<br/>com.greenart7c3.nostrsigner]
        Keystore[Android Keystore<br/>AES256-GCM]

        AmberClient -->|Intent/ActivityResult| Amber
        LocalDS -->|encrypted storage| Keystore
    end

    subgraph "Dependency Injection"
        Hilt[Hilt Modules]
        RepoModule[RepositoryModule]
        UCModule[UseCaseModule]

        Hilt --> RepoModule
        Hilt --> UCModule
    end

    VM -->|calls| UC_Login
    VM -->|calls| UC_Logout
    VM -->|calls| UC_GetState
    VM -->|direct call| Repo

    UC_Login_Impl -->|uses| Repo
    UC_Logout_Impl -->|uses| Repo
    UC_GetState_Impl -->|uses| Repo

    RepoModule -->|provides| RepoImpl
    UCModule -->|provides| UC_Login_Impl
    UCModule -->|provides| UC_Logout_Impl
    UCModule -->|provides| UC_GetState_Impl

    classDef presentation fill:#e1f5ff,stroke:#01579b
    classDef domain fill:#f3e5f5,stroke:#4a148c
    classDef data fill:#fff3e0,stroke:#e65100
    classDef external fill:#f1f8e9,stroke:#33691e
    classDef di fill:#fce4ec,stroke:#880e4f

    class UI,VM presentation
    class UC_Login,UC_Logout,UC_GetState,UC_Login_Impl,UC_Logout_Impl,UC_GetState_Impl,Model domain
    class Repo,RepoImpl,LocalDS,AmberClient data
    class Amber,Keystore external
    class Hilt,RepoModule,UCModule di
```

**Flow**: UI → ViewModel → UseCase → Repository → DataSource → External Services

**Dependency Direction**: Outer layers depend on inner layers (Presentation → Domain ← Data)

## Directory Patterns

### Domain Layer (`domain/`)

**Location**: `app/src/main/java/io/github/omochice/pinosu/domain/`
**Purpose**: Business logic, entities, and use case interfaces
**Example**: `usecase/AmberLoginUseCase.kt`, `model/User.kt`, `model/error/ErrorTypes.kt`

**Pattern**: Interface-based design, no Android/framework dependencies

### Data Layer (`data/`)

**Location**: `app/src/main/java/io/github/omochice/pinosu/data/`
**Purpose**: Repository implementations, data sources, external service clients
**Example**: `repository/AmberAuthRepository.kt`, `local/LocalAuthDataSource.kt`, `amber/AmberSignerClient.kt`

**Pattern**: Repository pattern with separate local/remote data sources

### Presentation Layer (`presentation/`)

**Location**: `app/src/main/java/io/github/omochice/pinosu/presentation/`
**Purpose**: ViewModels and UI state management
**Example**: `viewmodel/LoginViewModel.kt` with `LoginUiState`, `MainUiState`

**Pattern**: MVVM with StateFlow, Hilt-injected ViewModels

### UI Layer (`presentation/ui/`)

**Location**: `app/src/main/java/io/github/omochice/pinosu/presentation/ui/`
**Purpose**: Jetpack Compose screens and components
**Example**: `LoginScreen.kt`

**Pattern**: Composable functions observing ViewModel state

### Dependency Injection (`di/`)

**Location**: `app/src/main/java/io/github/omochice/pinosu/di/`
**Purpose**: Hilt modules for dependency provision
**Example**: `RepositoryModule.kt`, `UseCaseModule.kt`

**Pattern**: Separate modules per layer (Repository, UseCase)

## Naming Conventions

- **Files**: PascalCase matching class names (e.g., `AmberLoginUseCase.kt`)
- **Packages**: Lowercase, grouped by layer then feature (`domain/usecase`, `data/repository`)
- **Classes**: PascalCase with descriptive suffixes (`LoginViewModel`, `AuthRepository`)
- **Interfaces**: PascalCase without "I" prefix (e.g., `LoginUseCase`, `AuthRepository`)

## Package Organization

```kotlin
io.github.omochice.pinosu/
├── domain/          // Core business logic
│   ├── model/       // Entities and value objects
│   └── usecase/     // Business use cases
├── data/            // Data access implementations
│   ├── repository/  // Repository implementations
│   ├── local/       // Local storage (EncryptedSharedPreferences)
│   └── amber/       // External service clients
├── presentation/    // UI layer
│   ├── viewmodel/   // State management
│   ├── ui/          // Compose screens
│   └── navigation/  // Navigation graphs
├── di/              // Dependency injection
└── ui/              // Theme and design system
```

## Code Organization Principles

- **Dependency Rule**: Inner layers (domain) have no dependencies on outer layers (data, presentation)
- **Interface Segregation**: Use cases and repositories defined as interfaces in domain layer
- **Single Responsibility**: Each class/file has one clear purpose
- **Feature Organization**: Group by technical layer first, then by feature within layers
- **State Management**: Immutable data classes with StateFlow for reactive UI updates
