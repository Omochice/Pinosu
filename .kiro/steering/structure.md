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
        State[LoginUiState<br/>Sealed Interface]
        UI -->|observes state| VM
        VM -->|emits| State
        State -->|renders| UI
    end

    subgraph "Domain Layer"
        UC_Login[LoginUseCase<br/>Interface]
        UC_Logout[LogoutUseCase<br/>Interface]
        UC_GetState[GetLoginStateUseCase<br/>Interface]
        UC_PostBookmark[PostBookmarkUseCase<br/>Interface]
        Model[User, AuthEvent<br/>ErrorTypes]

        UC_Login -.implements.- UC_Login_Impl[Nip55LoginUseCase]
        UC_Logout -.implements.- UC_Logout_Impl[Nip55LogoutUseCase]
        UC_GetState -.implements.- UC_GetState_Impl[Nip55GetLoginStateUseCase]
        UC_PostBookmark -.implements.- UC_PostBookmark_Impl[PostBookmarkUseCaseImpl]
    end

    subgraph "Data Layer"
        Repo[AuthRepository<br/>Interface]
        RepoImpl[Nip55AuthRepository]
        LocalDS[LocalAuthDataSource<br/>DataStore]
        Nip55Client[Nip55SignerClient<br/>NIP-55]
        Nip65Fetcher[Nip65RelayListFetcher<br/>Kind 10002]
        RelayPool[RelayPool<br/>WebSocket]

        Repo -.implements.- RepoImpl
        RepoImpl --> LocalDS
        RepoImpl --> Nip55Client
        Nip65Fetcher --> RelayPool
    end

    subgraph "External Dependencies"
        Nip55Signer[NIP-55 Signer App<br/>com.greenart7c3.nostrsigner]
        TinkKeyManager[TinkKeyManager<br/>AES256-GCM AEAD]
        AndroidKeystore[Android Keystore<br/>Master Key]

        Nip55Client -->|Intent/ActivityResult| Nip55Signer
        LocalDS -->|encrypted via| TinkKeyManager
        TinkKeyManager -->|master key from| AndroidKeystore
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

    class UI,VM,State presentation
    class UC_Login,UC_Logout,UC_GetState,UC_PostBookmark,UC_Login_Impl,UC_Logout_Impl,UC_GetState_Impl,UC_PostBookmark_Impl,Model domain
    class Repo,RepoImpl,LocalDS,Nip55Client,Nip65Fetcher,RelayPool data
    class Nip55Signer,TinkKeyManager,AndroidKeystore external
    class Hilt,RepoModule,UCModule di
```

**Flow**: UI → ViewModel → UseCase → Repository → DataSource → External Services

**Dependency Direction**: Outer layers depend on inner layers (Presentation → Domain ← Data)

## Directory Patterns

### Domain Layer (`domain/`)

**Location**: `app/src/main/java/io/github/omochice/pinosu/domain/`
**Purpose**: Business logic, entities, and use case interfaces
**Example**: `usecase/Nip55LoginUseCase.kt`, `model/User.kt`, `model/error/ErrorTypes.kt`

**Pattern**: Interface-based design, no Android/framework dependencies

### Data Layer (`data/`)

**Location**: `app/src/main/java/io/github/omochice/pinosu/data/`
**Purpose**: Repository implementations, data sources, external service clients
**Example**: `repository/Nip55AuthRepository.kt`, `local/LocalAuthDataSource.kt`, `nip55/Nip55SignerClient.kt`

**Pattern**: Repository pattern with separate local/remote data sources

**Subpackages**:

- `repository/`: Repository implementations (Auth, Bookmark, Settings)
- `local/`: DataStore data sources with encrypted serializers (migrated from EncryptedSharedPreferences)
- `crypto/`: Encryption utilities (TinkKeyManager for DataStore encryption)
- `nip55/`: NIP-55 signer client
- `nip65/`: NIP-65 relay list fetcher (Nip65RelayListFetcher, Nip65EventParser)
- `relay/`: WebSocket relay client for Nostr events (RelayPool, PublishResult)
- `metadata/`: URL metadata fetching (Open Graph)
- `model/`: Data transfer objects (NostrEvent, UnsignedNostrEvent)
- `util/`: Utilities (Bech32 encoding via Quartz)

### Presentation Layer (`presentation/`)

**Location**: `app/src/main/java/io/github/omochice/pinosu/presentation/`
**Purpose**: ViewModels and UI state management
**Example**: `viewmodel/LoginViewModel.kt`, `viewmodel/PostBookmarkViewModel.kt`

**Pattern**: MVVM with StateFlow, Hilt-injected ViewModels

**UI State Pattern**: Separate files for UI state classes (e.g., `LoginUiState.kt`, `MainUiState.kt`, `BookmarkUiState.kt`). Use sealed interfaces for type-safe state management with exhaustive when expressions.

### UI Layer (`presentation/ui/`)

**Location**: `app/src/main/java/io/github/omochice/pinosu/presentation/ui/`
**Purpose**: Jetpack Compose screens and components
**Example**: `LoginScreen.kt`, `MainScreen.kt`, `BookmarkScreen.kt`, `PostBookmarkScreen.kt`, `SettingsScreen.kt`, `AppInfoScreen.kt`, `LicenseScreen.kt`

**Pattern**: Composable functions observing ViewModel state

**Subdirectories**:

- `component/`: Reusable Compose dialogs and widgets (ErrorDialog, UrlSelectionDialog)
- `drawer/`: Navigation drawer components (AppDrawer, DrawerMenuItem)

### Dependency Injection (`di/`)

**Location**: `app/src/main/java/io/github/omochice/pinosu/di/`
**Purpose**: Hilt modules for dependency provision
**Example**: `RepositoryModule.kt`, `UseCaseModule.kt`, `NetworkModule.kt`, `DataStoreModule.kt`

**Pattern**: Separate modules per layer (Repository, UseCase, Network, DataStore, RelayPool)

- `NetworkModule`: Provides singleton OkHttpClient with timeout configuration
- `RepositoryModule`: Binds repository interfaces to implementations
- `UseCaseModule`: Binds use case interfaces to implementations
- `DataStoreModule`: Provides encrypted DataStore instances
- `RelayPoolModule`: Binds RelayPool interface to implementation (separated for test replacement)

## Naming Conventions

- **Files**: PascalCase matching class names (e.g., `AmberLoginUseCase.kt`)
- **Packages**: Lowercase, grouped by layer then feature (`domain/usecase`, `data/repository`)
- **Classes**: PascalCase with descriptive suffixes (`LoginViewModel`, `AuthRepository`)
- **Interfaces**: PascalCase without "I" prefix (e.g., `LoginUseCase`, `AuthRepository`)

## Package Organization

```kotlin
io.github.omochice.pinosu/
├── domain/          // Core business logic
│   ├── model/       // Entities and value objects (User, Bookmark, AuthEvent)
│   └── usecase/     // Business use cases (Login, Logout, GetBookmarkList, PostBookmark)
├── data/            // Data access implementations
│   ├── repository/  // Repository implementations (Auth, Bookmark, Settings)
│   ├── local/       // Local storage (DataStore with encrypted serializers)
│   ├── crypto/      // Encryption utilities (TinkKeyManager)
│   ├── nip55/       // NIP-55 signer client
│   ├── nip65/       // NIP-65 relay list fetcher
│   ├── relay/       // WebSocket relay client, PublishResult
│   ├── metadata/    // URL metadata fetcher (Open Graph)
│   ├── model/       // Data transfer objects (NostrEvent, UnsignedNostrEvent)
│   └── util/        // Utilities (Bech32)
├── presentation/    // UI layer
│   ├── viewmodel/   // State management (ViewModels + separate *UiState files)
│   ├── ui/          // Compose screens (Login, Main, Bookmark, PostBookmark, Settings, AppInfo, License)
│   │   ├── component/   // Reusable dialogs (ErrorDialog, UrlSelectionDialog)
│   │   └── drawer/      // Navigation drawer UI
│   └── navigation/  // Navigation graphs
├── di/              // Dependency injection (Network, Repository, UseCase, DataStore, RelayPool)
└── ui/              // Theme and design system
```

## Code Organization Principles

- **Dependency Rule**: Inner layers (domain) have no dependencies on outer layers (data, presentation)
- **Interface Segregation**: Use cases and repositories defined as interfaces in domain layer
- **Single Responsibility**: Each class/file has one clear purpose
- **Feature Organization**: Group by technical layer first, then by feature within layers
- **State Management**: Immutable data classes with StateFlow for reactive UI updates
