# Product Overview

Pinosu is a decentralized memo/note-taking app for Android that uses Nostr events for storing and syncing notes, similar to Google Keep but with Nostr's decentralized infrastructure.

## Core Capabilities

- **Decentralized Note Storage**: Notes stored as Nostr events, accessible across relays
- **Secure Authentication**: Integration with Amber external signer (NIP-55) for key management without exposing private keys
- **Cross-Device Sync**: Notes automatically sync across devices via Nostr relays
- **Rich Note Features**: Create, edit, organize, and search notes with tags and metadata
- **Offline-First**: Local encrypted storage with background sync when online

## Target Use Cases

- **Personal Note-Taking**: Quick memos, to-do lists, ideas, and reminders stored decentralized on Nostr
- **Cross-Platform Users**: Access notes from any Nostr-compatible client, not locked to a single vendor
- **Privacy-Conscious Users**: Notes encrypted with user's keys, stored on relays of their choice
- **Nostr Ecosystem**: Integrate note-taking into existing Nostr workflows and identity

## Value Proposition

Provides a Google Keep-like experience with Nostr's decentralization benefits: own your data, choose your relays, access notes from any compatible client, and never lose access due to vendor lock-in or account suspension.

## Technical Foundation

- **Nostr Protocol**: Kind 39701 events for bookmark list storage
- **Amber Integration**: Secure external signing without key exposure
- **Android Native**: Built with Jetpack Compose and Material Design 3
- **Encrypted Storage**: Android Keystore with AES256-GCM for local caching
