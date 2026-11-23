package io.github.omochice.pinosu.feature.feed.data.parser

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.Locale

/** Reads the date formats found in feeds: RFC 822 (RSS) and ISO 8601 (Atom, Dublin Core) */
internal object FeedDateParser {

  private const val TWO_DIGIT_YEAR_BASE = 1970
  private const val UTC_OFFSET = "+0000"

  private val RFC_822: DateTimeFormatter =
      DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("d MMM ")
          .appendValueReduced(ChronoField.YEAR, 2, 4, TWO_DIGIT_YEAR_BASE)
          .appendPattern(" HH:mm[:ss] ")
          .appendOffset("+HHMM", UTC_OFFSET)
          .toFormatter(Locale.ENGLISH)

  private val NAMED_ZONE_OFFSETS =
      mapOf(
          "GMT" to UTC_OFFSET,
          "UT" to UTC_OFFSET,
          "UTC" to UTC_OFFSET,
          "Z" to UTC_OFFSET,
          "EST" to "-0500",
          "EDT" to "-0400",
          "CST" to "-0600",
          "CDT" to "-0500",
          "MST" to "-0700",
          "MDT" to "-0600",
          "PST" to "-0800",
          "PDT" to "-0700",
          "JST" to "+0900",
      )

  /**
   * Parse a feed date
   *
   * @param text Date text as written in the feed
   * @return The instant, or null when the text is not a recognized date
   */
  fun parse(text: String): Instant? {
    val trimmed = text.trim()
    return parseRfc822(trimmed) ?: parseIsoDateTime(trimmed) ?: parseIsoDate(trimmed)
  }

  private fun parseRfc822(text: String): Instant? = tryParse {
    OffsetDateTime.parse(normalizeRfc822(text), RFC_822).toInstant()
  }

  private fun normalizeRfc822(text: String): String {
    val withoutWeekday = text.substringAfter(',').trim()
    val zone = withoutWeekday.substringAfterLast(' ')
    val offset = NAMED_ZONE_OFFSETS[zone.uppercase(Locale.ROOT)]
    return if (offset == null) withoutWeekday else withoutWeekday.removeSuffix(zone) + offset
  }

  private fun parseIsoDateTime(text: String): Instant? = tryParse {
    OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
  }

  private fun parseIsoDate(text: String): Instant? = tryParse {
    LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneOffset.UTC).toInstant()
  }

  private inline fun tryParse(parse: () -> Instant): Instant? =
      try {
        parse()
      } catch (_: DateTimeParseException) {
        null
      }
}
