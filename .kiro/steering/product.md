# Product Overview

Pinosu is a decentralized memo/note-taking app for Android that uses Nostr Kind 39701 events for storing and syncing notes, similar to Google Keep but with Nostr's decentralized infrastructure.

## Core Capabilities

- **Decentralized Note Storage**: Notes stored as Nostr Kind 39701 (bookmark list) events, accessible across relays
- **Secure Authentication**: Integration with NIP-55 external signer (e.g., Amber) for key management without exposing private keys
- **Bookmark Posting**: Create and publish bookmark events via NIP-55 signing workflow
- **Dynamic Relay Discovery**: NIP-65 relay list fetching from user's kind 10002 events for personalized relay preferences
- **Cross-Device Sync**: Notes automatically sync across devices via Nostr relays
- **Rich Note Features**: Create, edit, organize, and search notes with tags and metadata
- **Offline-First**: Local encrypted storage with background sync when online
- **Comments & Engagement**: View and post comments on bookmarks using NIP-22 kind 1111 comments and kind 1 text notes
- **Share Intent Integration**: Receive URLs and text from other Android apps for quick bookmark creation

## Target Use Cases

- **Personal Note-Taking**: Quick memos, to-do lists, ideas, and reminders stored decentralized on Nostr
- **Cross-Platform Users**: Access notes from any Nostr-compatible client, not locked to a single vendor
- **Privacy-Conscious Users**: Notes encrypted with user's keys, stored on relays of their choice
- **Nostr Ecosystem**: Integrate note-taking into existing Nostr workflows and identity

## Value Proposition

Provides a Google Keep-like experience with Nostr's decentralization benefits: own your data, choose your relays, access notes from any compatible client, and never lose access due to vendor lock-in or account suspension.

## Technical Foundation

- **Nostr Protocol**: Kind 39701 (bookmarks), Kind 10002 (relay metadata), Kind 1111 (NIP-22 comments), Kind 1 (text notes)
- **NIP-55 Integration**: Secure external signing without key exposure (default: Amber)
- **NIP-65 Integration**: Dynamic relay list discovery from user preferences
- **NIP-22 Integration**: Comment system for bookmark discussions with kind 1111 and kind 1 support
- **Android Native**: Built with Jetpack Compose and Material Design 3
- **Encrypted Storage**: Tink Android with DataStore for secure local caching

---

_Updated: 2026-02-04 - Added comment capabilities (kind 1111/1) and share intent integration_
