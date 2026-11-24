package io.github.omochice.pinosu.core.result

/**
 * Continue a successful result with a step that can fail on its own
 *
 * Unlike [mapCatching], an exception thrown by [transform] is not caught, so cancellation and bugs
 * still propagate.
 *
 * @return The result of [transform], or this failure unchanged
 */
inline fun <T, R> Result<T>.andThen(transform: (T) -> Result<R>): Result<R> =
    fold(onSuccess = transform, onFailure = { Result.failure(it) })
