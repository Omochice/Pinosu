# Product Overview

Pinosu is a decentralized feed reader and web bookmark app for Android.
It reads RSS and Atom feeds, and saves the articles worth keeping as Nostr Kind 39701 bookmark events.
The feed subscription list is stored on Nostr relays as well, so both follow the user across devices.

## Core Capabilities

- **Feed Reading**: Subscribe to RSS and Atom feeds by feed or site URL, read the merged timeline by category, and read articles in the app with their images and tables
- **Decentralized Subscriptions**: The subscription list stored as NIP-78 app data (Kind 30078), shared across the user's devices
- **Decentralized Bookmarks**: Bookmarks stored as Nostr Kind 39701 events, accessible across relays
- **Secure Authentication**: Integration with NIP-55 external signer (e.g., Amber) for key management without exposing private keys; alternatively, read-only login via direct npub entry for browse-only access
- **Bookmark Posting**: Create and publish bookmark events via NIP-55 signing workflow, from the bookmark form or straight from a feed item
- **Dynamic Relay Discovery**: NIP-65 relay list fetching from user's kind 10002 events for personalized relay preferences
- **Comments & Engagement**: View and post comments on bookmarks using NIP-22 kind 1111 comments and kind 1 text notes
- **Share Intent Integration**: Receive URLs and text from other Android apps for quick bookmark creation

## Target Use Cases

- **Feed Readers on Nostr**: Follow blogs and news sites, and keep the articles worth rereading as bookmarks
- **Cross-Platform Users**: Access bookmarks and subscriptions from any Nostr-compatible client, not locked to a single vendor
- **Nostr Ecosystem**: Bring reading and bookmarking into existing Nostr workflows and identity

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
- **NIP-78 Integration**: Feed subscription list as Kind 30078 app data
- **Android Native**: Built with Jetpack Compose and Material Design 3
- **Encrypted Storage**: Tink Android with DataStore for secure local caching

---
