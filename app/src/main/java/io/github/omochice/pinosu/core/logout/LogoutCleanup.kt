package io.github.omochice.pinosu.core.logout

/**
 * Data a feature keeps for the logged-in user that must not outlive the login
 *
 * Features contribute an implementation to the set of them with Hilt's `@IntoSet`, so that logging
 * out deletes it without the auth feature knowing the feature.
 */
interface LogoutCleanup {

  /**
   * Delete the data, after the user has been logged out
   *
   * A failure to delete it is handled here rather than thrown, because the user is already logged
   * out and the other cleanups must still run.
   */
  suspend fun cleanUp()
}
