package io.github.omochice.pinosu.feature.feed.domain.model.error

/** The user logged in read-only, so there is no signer to sign a change with */
class ReadOnlyLoginException : Exception("Read-only login cannot change feed subscriptions")
