# NIP-55 External Signer for Authentication

Date: 2026-01-18

## Status

Accepted

## Context

Pinosu needs to authenticate users with their Nostr identity to read and publish bookmark events. The straightforward approach would be to ask users to enter or generate a private key directly within the app. However, managing private keys in-app introduces significant security risks: the key could be leaked through memory dumps, insecure storage, or application vulnerabilities. The Nostr ecosystem has established NIP-55, a protocol for Android apps to delegate signing operations to a dedicated external signer application such as Amber (com.greenart7c3.nostrsigner). This allows apps to request signatures via Android intents without ever handling the private key.

The initial implementation in PR #1 introduced Amber-specific login with intent-based signing, encrypted session persistence, and a prompt to install Amber if it is not present. PR #20 generalized the naming from "Amber" to "NIP-55 compatible app" throughout the codebase and UI strings, making the architecture signer-agnostic. PR #24 extended the NIP-55 integration to fetch the user's relay list (kind 10002) from the signer, enabling multi-relay communication based on user preferences.

## Decision

We will use the NIP-55 external signer protocol for all authentication and signing operations. The app delegates cryptographic signing to a NIP-55-compatible signer app (defaulting to Amber) via Android intents, and never stores or handles private keys. The implementation references the signer generically as a "NIP-55 compatible app" rather than a specific application, allowing users to choose any conforming signer.

## Consequences

Users must have a NIP-55-compatible signer app installed on their device. If no signer is found, the app displays an install prompt, which adds friction to the onboarding experience.

The app never touches private keys, which substantially reduces the attack surface for key compromise. Signing operations depend on inter-process communication, which introduces latency and the possibility of timeout errors that must be handled gracefully.

By abstracting over the NIP-55 protocol rather than a specific signer, the app remains compatible with any future NIP-55 signer applications without code changes.
