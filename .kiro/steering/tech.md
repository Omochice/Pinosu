# Technical Steering

## Development Environment

### Java/Gradle Execution

This project uses **devbox** for managing development dependencies including Java.

**IMPORTANT:** Always use `devbox run ./gradlew` to execute Gradle commands:

```bash
# Build
devbox run ./gradlew :app:assembleDebug

# Test
devbox run ./gradlew :app:test

# Android Instrumentation Test
devbox run ./gradlew :app:connectedDebugAndroidTest

# Lint
devbox run ./gradlew :app:lintDebug
```

Do NOT use `./gradlew` directly, as Java may not be available in the system PATH.

## Technology Stack

### Android

- **Min SDK**: 30
- **Target SDK**: 36
- **Kotlin**: 2.3.10
- **Compose BOM**: 2026.02.01
- **Gradle Plugin**: 9.0.1
- **Hilt**: 2.59.2 (requires metadata compatibility workaround for Kotlin 2.3.x)

### Architecture

- **Clean Architecture**: Domain, Data, Presentation layers
- **MVVM**: ViewModel + UI State pattern
- **Dependency Injection**: Hilt

### Security

- **Tink Android**: com.google.crypto.tink:tink-android (v1.20.0) for encryption
- **DataStore**: androidx.datastore with encrypted serializer (TinkKeyManager)
- **EncryptedSharedPreferences**: androidx.security:security-crypto (legacy, migrated to DataStore)
- **Android Keystore**: Hardware-backed key storage with AES256_GCM
- **Encryption Schemes**: AES256-SIV (keys) + AES256-GCM (values)

### Nostr Integration

- **Amethyst Quartz**: com.vitorpamplona.quartz:quartz (v1.05.1)
- **NIP-01**: User metadata fetching from kind 0 events; in-memory cached batch profile fetcher (`Nip01ProfileFetcher`)
- **NIP-19**: Bech32 entity parsing for nevent references (via Quartz Nip19Parser)
- **NIP-22**: Comment system for kind 1111 replies and kind 1 text note references
- **NIP-55**: External signer integration (e.g., Amber: com.greenart7c3.nostrsigner)
- **NIP-65**: Relay list fetching from kind 10002 events (bootstrap relay: wss://yabu.me)
- **Default Signer Package**: com.greenart7c3.nostrsigner (Amber)
- **WebSocket Client**: OkHttp for relay connections
- **Event Types**: Kind 0 (NIP-01 user metadata), Kind 39701 (bookmark lists), Kind 10002 (relay list metadata), Kind 1111 (NIP-22 comments), Kind 1 (text notes)
- **Auth Modes**: `LoginMode` sealed interface (`Nip55Signer` for full access, `ReadOnly` for browse-only via npub entry)

### Serialization

- **kotlinx-serialization-json**: 1.10.0 for type-safe JSON handling
- **Custom Serializers**: `KSerializer<T>` for Nostr protocol array-based messages
- **Lazy Initialization**: Use `by lazy { }` for Json instances interacting with EncryptedSharedPreferences

### Network & HTTP

- **OkHttp**: 5.3.2 with Hilt singleton injection
- **Connection Timeouts**: 10s connect, 10s read (configured in NetworkModule)
- **HTML Parsing**: Jsoup 1.22.1 for Open Graph metadata extraction
- **Caching**: LruCache for URL metadata (max 100 entries)
- **Image Loading**: Coil 3.4.0 with OkHttp integration (coil-compose, coil-network-okhttp)

### Testing

- **Unit Tests**: JUnit 4, MockK, Robolectric
- **Coroutine Testing**: kotlinx-coroutines-test
- **Instrumentation Tests**: AndroidX Test (JUnit, Espresso), Compose UI Test
- **DI Testing**: Hilt Android Testing

## Technical Conventions

### Dependency Injection Pattern

- **Singleton Pattern**: Network clients (OkHttpClient, RelayPool) are @Singleton scoped
- **Constructor Injection**: Prefer @Inject constructor over field injection
- **Interface Binding**: Repositories and use cases defined as interfaces, bound in Hilt modules
- **Feature-Scoped Modules**: Each feature has its own Hilt module (e.g., `AuthModule`, `BookmarkModule`, `SettingsModule`)
- **Core Modules**: Cross-feature infrastructure in `core/di/` (NetworkModule, RelayPoolModule)
- **Root Modules**: Cross-feature bindings in `di/` (RepositoryModule for NIP-65)

### Network & Async Patterns

- **Flow for Streaming**: WebSocket events exposed as Kotlin Flow for reactive handling
- **Timeout Handling**: Use `withTimeoutOrNull` for network operations (typically 10s)
- **Error Propagation**: Wrap operations in Result<T> for explicit error handling
- **Coroutine Contexts**: IO operations use `Dispatchers.IO`, safe time API (java.time) instead of Date
- **Resource Cleanup**: Flow cleanup with `awaitClose {}` for WebSocket connections

### UI State Pattern

- **Sealed Interface**: Use sealed interfaces for UI state (e.g., `LoginUiState`, `PostBookmarkUiState`)
- **Separate Files**: Extract UI state classes to dedicated `*UiState.kt` files for clarity
- **Exhaustive When**: Sealed interfaces enable exhaustive when expressions for type-safe state handling
- **State Variants**: Common states include `Idle`, `Loading`, `Success`, and nested `Error` sealed interfaces

### Code Quality

- **Documentation**: Public APIs documented with KDoc comments
- **Null Safety**: Prefer elvis operator (?:) over !! assertion
- **Thread Safety**: Use java.time API instead of SimpleDateFormat for thread safety
- **External Libraries**: Use established libraries (Quartz for Bech32) instead of custom implementations
- **Code Coverage**: Kover for unit test coverage, Jacoco for instrumentation test reports
- **Version Tracking**: BuildConfig.COMMIT_HASH for git commit identification
- **License Management**: AboutLibraries 13.2.1 for open-source license display

### Development Tooling

- **Code Formatting**: ktfmt (Kotlin), biome (JSON/YAML), tombi (TOML), treefmt (orchestration)
- **Linting**: detekt (Kotlin static analysis), actionlint (GitHub Actions)
- **Spell Check**: typos for typo detection
- **Scripts**: Managed via devbox shell scripts (fmt, check, test, version-up)

---

_Updated: 2026-03-02 - Added NIP-01 profile fetching; tombi TOML formatter to tooling_
