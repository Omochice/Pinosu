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

- **Amethyst Quartz**: com.vitorpamplona:quartz-android
- **NIP-55**: External signer integration via Amber
- **Amber Package**: com.greenart7c3.nostrsigner

### Testing

- **Unit Tests**: JUnit 4, MockK, Robolectric
- **Coroutine Testing**: kotlinx-coroutines-test
- **Instrumentation Tests**: AndroidX Test (JUnit, Espresso), Compose UI Test
- **DI Testing**: Hilt Android Testing
