package io.github.omochice.pinosu.domain.model

/**
 * Domain events related to authentication (for future extension)
 *
 * Sealed class representing authentication-related events such as login and logout. Designed for
 * future extension to event sourcing or event-driven architecture.
 */
sealed class AuthEvent {
  /**
   * Event representing user login
   *
   * @property user The logged-in user
   */
  data class UserLoggedIn(val user: User) : AuthEvent()

  /**
   * Event representing user logout
   *
   * Defined as a data object and treated as a singleton
   */
  data object UserLoggedOut : AuthEvent()
}
