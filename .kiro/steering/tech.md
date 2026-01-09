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

- **Min SDK**: 26
- **Target SDK**: 36
- **Kotlin**: 2.1.0
- **Compose BOM**: 2025.12.00
- **Gradle Plugin**: 8.13.2

### Architecture

- **Clean Architecture**: Domain, Data, Presentation layers
- **MVVM**: ViewModel + UI State pattern
- **Dependency Injection**: Hilt

### Security

- **EncryptedSharedPreferences**: androidx.security:security-crypto
- **Android Keystore**: Hardware-backed key storage with AES256_GCM
- **Encryption Schemes**: AES256-SIV (keys) + AES256-GCM (values)

### Nostr Integration

- **Amethyst Quartz**: com.vitorpamplona:quartz-android (v1.03.0)
- **NIP-55**: External signer integration via Amber
- **Amber Package**: com.greenart7c3.nostrsigner
- **WebSocket Client**: OkHttp 4.12.0 for relay connections
- **Event Types**: Kind 10003 (bookmarks), Kind 39701 (bookmark lists)

### Network & HTTP

- **OkHttp**: 4.12.0 with Hilt singleton injection
- **Connection Timeouts**: 10s connect, 10s read (configured in NetworkModule)
- **HTML Parsing**: Jsoup 1.18.1 for Open Graph metadata extraction
- **Caching**: LruCache for URL metadata (max 100 entries)

### Testing

- **Unit Tests**: JUnit 4, MockK, Robolectric
- **Coroutine Testing**: kotlinx-coroutines-test
- **Instrumentation Tests**: AndroidX Test (JUnit, Espresso), Compose UI Test
- **DI Testing**: Hilt Android Testing

## Technical Conventions

### Dependency Injection Pattern

- **Singleton Pattern**: Network clients (OkHttpClient, RelayClient) are @Singleton scoped
- **Constructor Injection**: Prefer @Inject constructor over field injection
- **Interface Binding**: Repositories and use cases defined as interfaces, bound in Hilt modules
- **Module Organization**: Separate modules by technical concern (Network, Repository, UseCase)

### Network & Async Patterns

- **Flow for Streaming**: WebSocket events exposed as Kotlin Flow for reactive handling
- **Timeout Handling**: Use `withTimeoutOrNull` for network operations (typically 10s)
- **Error Propagation**: Wrap operations in Result<T> for explicit error handling
- **Coroutine Contexts**: IO operations use `Dispatchers.IO`, safe time API (java.time) instead of Date
- **Resource Cleanup**: Flow cleanup with `awaitClose {}` for WebSocket connections

### Code Quality

- **Documentation**: Public APIs documented with KDoc comments
- **Null Safety**: Prefer elvis operator (?:) over !! assertion
- **Thread Safety**: Use java.time API instead of SimpleDateFormat for thread safety
- **External Libraries**: Use established libraries (Quartz for Bech32) instead of custom implementations
