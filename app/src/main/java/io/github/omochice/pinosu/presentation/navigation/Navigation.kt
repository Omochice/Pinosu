package io.github.omochice.pinosu.presentation.navigation

import kotlinx.serialization.Serializable

/** Login screen route */
@Serializable object Login

/** Main screen route */
@Serializable object Main

/** Bookmark list screen route */
@Serializable object Bookmark

/** License screen route */
@Serializable object License

/** App info screen route */
@Serializable object AppInfo

// Legacy string constants (to be removed after migration)
@Deprecated("Use Login object instead", ReplaceWith("Login")) const val LOGIN_ROUTE = "login"

@Deprecated("Use Main object instead", ReplaceWith("Main")) const val MAIN_ROUTE = "main"

@Deprecated("Use Bookmark object instead", ReplaceWith("Bookmark"))
const val BOOKMARK_ROUTE = "bookmark"

@Deprecated("Use License object instead", ReplaceWith("License"))
const val LICENSE_ROUTE = "license"

@Deprecated("Use AppInfo object instead", ReplaceWith("AppInfo"))
const val APP_INFO_ROUTE = "appinfo"
