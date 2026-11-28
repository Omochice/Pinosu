package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

/** The signer answered with a bare signature, but the event it signs is no longer known */
class UnusableSignatureException : Exception("No pending event to attach the signature to")
