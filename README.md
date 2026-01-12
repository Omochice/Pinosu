# Pinosu

A decentralized note-taking app for Android using Nostr.

Aims to provide a Google Keep-like experience with the benefits of decentralization.

## Overview

Pinosu stores your notes as Nostr Kind 39701 events, allowing you to access them from any Nostr-compatible client.

Your notes are synced across devices via Nostr relays, and you maintain full ownership of your data.

## Supports

- Android 8.0 (API 26) or higher
- NIP-55 compatible signer app (e.g., [greenart7c3/Amber](https://github.com/greenart7c3/Amber))

## Inspirations

This project was inspired by the following apps:

- [nikolat/kuchiyose](https://github.com/nikolat/kuchiyose)

## Development

This project uses [devbox](https://www.jetify.com/devbox) for development environment management.

```console
# Testing
devbox run ./gradlew :app:test

# Building
devbox run ./gradlew :app:assembleDebug
```

See [CONTRIBUTING.md](./CONTRIBUTING.md).

## License

[zlib](./LICENSE)
