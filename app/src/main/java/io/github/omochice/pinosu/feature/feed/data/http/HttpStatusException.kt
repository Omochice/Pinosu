package io.github.omochice.pinosu.feature.feed.data.http

/** The server answered with a status other than 2xx */
class HttpStatusException(val code: Int) : Exception("HTTP $code")
