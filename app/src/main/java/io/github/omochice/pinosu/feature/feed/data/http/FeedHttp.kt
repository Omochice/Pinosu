package io.github.omochice.pinosu.feature.feed.data.http

import javax.inject.Qualifier

/**
 * Qualifies the HTTP client that subscribed feeds are downloaded with, and its cache
 *
 * Site pages and article pages are fetched with the unqualified client, so that only feeds are kept
 * on disk.
 */
@Qualifier annotation class FeedHttp
