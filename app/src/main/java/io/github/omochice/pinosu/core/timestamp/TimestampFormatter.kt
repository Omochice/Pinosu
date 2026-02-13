package io.github.omochice.pinosu.core.timestamp

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val formatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

fun formatTimestamp(timestamp: Long): String = formatter.format(Instant.ofEpochSecond(timestamp))
