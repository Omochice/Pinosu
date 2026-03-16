# Encrypted Storage with DataStore and Tink

Date: 2026-01-31

## Status

Accepted

## Context

Pinosu stores sensitive authentication data locally, including the user's public key and relay list. The initial implementation used Android's `EncryptedSharedPreferences` from the `androidx.security:security-crypto` library. While functional, `EncryptedSharedPreferences` has known issues: it can produce corrupted files that are unrecoverable, it lacks support for structured data beyond simple key-value pairs, and its API has been effectively deprecated in favor of newer approaches. A corruption bug was encountered and worked around (commit b9f70c8), which reinforced the need to migrate.

PR #113 migrated the secure storage layer from `EncryptedSharedPreferences` to `androidx.datastore.DataStore` with encryption provided by Google Tink (`com.google.crypto.tink:tink-android`). A custom `TinkKeyManager` manages encryption keys backed by the Android Keystore using AES256-GCM. PR #161 updated the `TinkKeyManager` to use the non-deprecated `getPrimitive` overload. PR #163 removed all remaining references to `EncryptedSharedPreferences`, including the legacy migration path and the unused `security-crypto` dependency.

## Decision

We will use Jetpack DataStore with Tink Android encryption for all local secure storage. A `TinkKeyManager` component manages AEAD (Authenticated Encryption with Associated Data) primitives backed by hardware-backed Android Keystore keys. DataStore handles structured, coroutine-friendly data persistence, while Tink provides the encryption layer using AEAD with the AES256-GCM key template, backed by an Android Keystore master key managed via `AndroidKeysetManager`.

## Consequences

DataStore provides structured, type-safe, and coroutine-native data access, which integrates naturally with the app's Kotlin coroutine and Flow-based architecture. Tink's AEAD primitives are well-maintained and avoid the corruption issues observed with `EncryptedSharedPreferences`.

The migration removed the `androidx.security:security-crypto` dependency entirely, reducing the dependency surface and eliminating the deprecated API usage. Tests no longer require an Android runtime environment for storage verification, improving test execution speed and reliability.

The `TinkKeyManager` introduces a project-specific component that must be maintained, but its responsibilities are narrow and well-tested. Hardware-backed Keystore integration means encryption keys cannot be extracted from the device, though this also means data is not portable between devices by design.
