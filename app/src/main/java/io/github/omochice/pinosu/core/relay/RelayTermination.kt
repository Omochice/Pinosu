package io.github.omochice.pinosu.core.relay

/** How a query against a single relay ended */
sealed interface RelayTermination {

  /** The relay sent EOSE, so it has delivered every stored event matching the filters */
  data object Eose : RelayTermination

  /**
   * The relay ended the subscription with CLOSED
   *
   * @property message Reason given by the relay, conventionally `prefix: description` (NIP-01)
   */
  data class Closed(val message: String) : RelayTermination {

    /** Whether the relay refused to serve the query for this client */
    val isAccessDenied: Boolean
      get() = message.substringBefore(':', missingDelimiterValue = "") in ACCESS_DENIED_PREFIXES

    private companion object {
      val ACCESS_DENIED_PREFIXES = setOf("auth-required", "restricted")
    }
  }

  /**
   * The connection failed or ended before EOSE or CLOSED arrived
   *
   * @property message Description of the failure
   */
  data class Failed(val message: String) : RelayTermination

  /** Neither EOSE nor CLOSED arrived within the timeout */
  data object TimedOut : RelayTermination
}
