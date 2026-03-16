# Kind 39701 for Bookmark Data Model

Date: 2026-01-20

## Status

Accepted

## Context

Pinosu needs a Nostr event kind to represent bookmark data. Early in the project, kind 10003 (a generic bookmark list defined informally in the Nostr ecosystem) was considered but rejected. Kind 10003 is a replaceable event that stores bookmarks as a flat list of references, which does not support per-bookmark metadata such as titles, descriptions, or tags.

Kind 39701 is defined by NIP-B0 as a parameterized replaceable event for bookmark lists. Each bookmark is a distinct event that can carry structured tags (title, URL, description) and can be individually created, updated, or deleted without replacing the entire list. This aligns with Pinosu's need to treat bookmarks as individually addressable notes with rich metadata.

PR #7 implemented the initial bookmark fetching and display, establishing kind 39701 as the core data model. The `RelayBookmarkRepository` subscribes to kind 39701 events from the user's relays, processes the structured tags, and enriches bookmarks with Open Graph metadata fetched from URLs. PR #9 removed all remaining references to kind 10003 from the documentation, confirming the decision.

## Decision

We will use Nostr kind 39701 (NIP-B0 bookmark list) as the data model for all bookmark storage and synchronization. Bookmarks are stored as parameterized replaceable events, with structured tags for URL, title, and description metadata. Constants for the kind number and tag structure are maintained in `core/nip/nipb0/NipB0`.

## Consequences

Using a parameterized replaceable event kind means each bookmark can be individually addressed and updated without affecting other bookmarks, which supports fine-grained synchronization across relays.

Kind 39701 is defined by NIP-B0, a formal specification within the Nostr protocol. This provides interoperability with other clients that implement NIP-B0, allowing users to access their bookmarks from any compatible application.

The choice of a less widely adopted kind (compared to kind 10003 or kind 30023) means fewer existing clients display these events, which limits cross-client visibility in practice. However, as NIP-B0 adoption grows, this limitation diminishes. Pinosu's role as an early NIP-B0 implementer means the app may need to adapt if the specification evolves.
