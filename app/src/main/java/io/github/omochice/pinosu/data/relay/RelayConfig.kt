package io.github.omochice.pinosu.data.relay

/**
 * Configuration for a Nostr relay
 *
 * @property url WebSocket URL of the relay (e.g., "wss://relay.example.com")
 * @property read Whether the relay is used for reading events
 * @property write Whether the relay is used for writing events
 */
data class RelayConfig(val url: String, val read: Boolean = true, val write: Boolean = true)
