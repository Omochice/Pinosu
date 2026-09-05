# Product Overview

Pinosu is a decentralized memo/note-taking app for Android that uses Nostr Kind 39701 events for storing and syncing notes, similar to Google Keep but with Nostr's decentralized infrastructure.

## Core Capabilities

- **Decentralized Note Storage**: Notes stored as Nostr Kind 39701 (bookmark list) events, accessible across relays
- **Secure Authentication**: Integration with NIP-55 external signer (e.g., Amber) for key management without exposing private keys; alternatively, read-only login via direct npub entry for browse-only access
- **Bookmark Posting**: Create and publish bookmark events via NIP-55 signing workflow
- **Dynamic Relay Discovery**: NIP-65 relay list fetching from user's kind 10002 events for personalized relay preferences
- **Cross-Device Sync**: Notes automatically sync across devices via Nostr relays
- **Rich Note Features**: Create, edit, organize, and search notes with tags and metadata
- **Offline-First**: Local encrypted storage with background sync when online
- **Comments & Engagement**: View and post comments on bookmarks using NIP-22 kind 1111 comments and kind 1 text notes
- **Share Intent Integration**: Receive URLs and text from other Android apps for quick bookmark creation
- **Customizable Appearance & Language**: Light/Dark/System theme mode and English/Japanese/System language mode, independent of device locale
- **Configurable Relays**: User-defined bootstrap relay list (falls back to app defaults) alongside NIP-65 personalized relay discovery
- **Stable Bookmark Identity**: Tracking query parameters (e.g. `fbclid`, `gclid`, `utm_*`) stripped from URLs before posting, so the same page bookmarked via different referral links produces one identifiable event instead of duplicates

## Target Use Cases

- **Personal Note-Taking**: Quick memos, to-do lists, ideas, and reminders stored decentralized on Nostr
- **Cross-Platform Users**: Access notes from any Nostr-compatible client, not locked to a single vendor
- **Privacy-Conscious Users**: Notes encrypted with user's keys, stored on relays of their choice
- **Nostr Ecosystem**: Integrate note-taking into existing Nostr workflows and identity

## Value Proposition

Nostr's decentralization means users own their data and never lose access due to vendor lock-in or account suspension.

## Technical Foundation

- **NIP-01 Integration**: User profile metadata (kind 0) for enriching comment author display (name, avatar)
- **NIP-19 Integration**: Bech32-encoded entity resolution (nevent references)
- **NIP-B0 Integration**: Formal specification for Kind 39701 bookmark list storage and sync
- **NIP-22 Integration**: Comment system for bookmark discussions with kind 1111 and kind 1 support
- **NIP-55 Integration**: Secure external signing without key exposure (default: Amber)
- **NIP-89 Integration**: Client tag identification for published events (opt-in via settings)
- **NIP-65 Integration**: Dynamic relay list discovery from user preferences
- **Android Native**: Built with Jetpack Compose and Material Design 3
- **Encrypted Storage**: Tink Android with DataStore for secure local caching

---
